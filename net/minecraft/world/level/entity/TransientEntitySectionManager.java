package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class TransientEntitySectionManager<T extends EntityAccess> {
   static final Logger LOGGER = LogUtils.getLogger();
   final LevelCallback<T> callbacks;
   final EntityLookup<T> entityStorage;
   final EntitySectionStorage<T> sectionStorage;
   private final LongSet tickingChunks = new LongOpenHashSet();
   private final LevelEntityGetter<T> entityGetter;

   public TransientEntitySectionManager(Class<T> oclass, LevelCallback<T> levelcallback) {
      this.entityStorage = new EntityLookup<>();
      this.sectionStorage = new EntitySectionStorage<>(oclass, (i) -> this.tickingChunks.contains(i) ? Visibility.TICKING : Visibility.TRACKED);
      this.callbacks = levelcallback;
      this.entityGetter = new LevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
   }

   public void startTicking(ChunkPos chunkpos) {
      long i = chunkpos.toLong();
      this.tickingChunks.add(i);
      this.sectionStorage.getExistingSectionsInChunk(i).forEach((entitysection) -> {
         Visibility visibility = entitysection.updateChunkStatus(Visibility.TICKING);
         if (!visibility.isTicking()) {
            entitysection.getEntities().filter((entityaccess) -> !entityaccess.isAlwaysTicking()).forEach(this.callbacks::onTickingStart);
         }

      });
   }

   public void stopTicking(ChunkPos chunkpos) {
      long i = chunkpos.toLong();
      this.tickingChunks.remove(i);
      this.sectionStorage.getExistingSectionsInChunk(i).forEach((entitysection) -> {
         Visibility visibility = entitysection.updateChunkStatus(Visibility.TRACKED);
         if (visibility.isTicking()) {
            entitysection.getEntities().filter((entityaccess) -> !entityaccess.isAlwaysTicking()).forEach(this.callbacks::onTickingEnd);
         }

      });
   }

   public LevelEntityGetter<T> getEntityGetter() {
      return this.entityGetter;
   }

   public void addEntity(T entityaccess) {
      this.entityStorage.add(entityaccess);
      long i = SectionPos.asLong(entityaccess.blockPosition());
      EntitySection<T> entitysection = this.sectionStorage.getOrCreateSection(i);
      entitysection.add(entityaccess);
      entityaccess.setLevelCallback(new TransientEntitySectionManager.Callback(entityaccess, i, entitysection));
      this.callbacks.onCreated(entityaccess);
      this.callbacks.onTrackingStart(entityaccess);
      if (entityaccess.isAlwaysTicking() || entitysection.getStatus().isTicking()) {
         this.callbacks.onTickingStart(entityaccess);
      }

   }

   @VisibleForDebug
   public int count() {
      return this.entityStorage.count();
   }

   void removeSectionIfEmpty(long i, EntitySection<T> entitysection) {
      if (entitysection.isEmpty()) {
         this.sectionStorage.remove(i);
      }

   }

   @VisibleForDebug
   public String gatherStats() {
      return this.entityStorage.count() + "," + this.sectionStorage.count() + "," + this.tickingChunks.size();
   }

   class Callback implements EntityInLevelCallback {
      private final T entity;
      private long currentSectionKey;
      private EntitySection<T> currentSection;

      Callback(T entityaccess, long i, EntitySection<T> entitysection) {
         this.entity = entityaccess;
         this.currentSectionKey = i;
         this.currentSection = entitysection;
      }

      public void onMove() {
         BlockPos blockpos = this.entity.blockPosition();
         long i = SectionPos.asLong(blockpos);
         if (i != this.currentSectionKey) {
            Visibility visibility = this.currentSection.getStatus();
            if (!this.currentSection.remove(this.entity)) {
               TransientEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", this.entity, SectionPos.of(this.currentSectionKey), i);
            }

            TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
            EntitySection<T> entitysection = TransientEntitySectionManager.this.sectionStorage.getOrCreateSection(i);
            entitysection.add(this.entity);
            this.currentSection = entitysection;
            this.currentSectionKey = i;
            TransientEntitySectionManager.this.callbacks.onSectionChange(this.entity);
            if (!this.entity.isAlwaysTicking()) {
               boolean flag = visibility.isTicking();
               boolean flag1 = entitysection.getStatus().isTicking();
               if (flag && !flag1) {
                  TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
               } else if (!flag && flag1) {
                  TransientEntitySectionManager.this.callbacks.onTickingStart(this.entity);
               }
            }
         }

      }

      public void onRemove(Entity.RemovalReason entity_removalreason) {
         if (!this.currentSection.remove(this.entity)) {
            TransientEntitySectionManager.LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", this.entity, SectionPos.of(this.currentSectionKey), entity_removalreason);
         }

         Visibility visibility = this.currentSection.getStatus();
         if (visibility.isTicking() || this.entity.isAlwaysTicking()) {
            TransientEntitySectionManager.this.callbacks.onTickingEnd(this.entity);
         }

         TransientEntitySectionManager.this.callbacks.onTrackingEnd(this.entity);
         TransientEntitySectionManager.this.callbacks.onDestroyed(this.entity);
         TransientEntitySectionManager.this.entityStorage.remove(this.entity);
         this.entity.setLevelCallback(NULL);
         TransientEntitySectionManager.this.removeSectionIfEmpty(this.currentSectionKey, this.currentSection);
      }
   }
}
