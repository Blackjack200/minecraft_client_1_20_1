package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public class EntitySectionStorage<T extends EntityAccess> {
   private final Class<T> entityClass;
   private final Long2ObjectFunction<Visibility> intialSectionVisibility;
   private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap<>();
   private final LongSortedSet sectionIds = new LongAVLTreeSet();

   public EntitySectionStorage(Class<T> oclass, Long2ObjectFunction<Visibility> long2objectfunction) {
      this.entityClass = oclass;
      this.intialSectionVisibility = long2objectfunction;
   }

   public void forEachAccessibleNonEmptySection(AABB aabb, AbortableIterationConsumer<EntitySection<T>> abortableiterationconsumer) {
      int i = 2;
      int j = SectionPos.posToSectionCoord(aabb.minX - 2.0D);
      int k = SectionPos.posToSectionCoord(aabb.minY - 4.0D);
      int l = SectionPos.posToSectionCoord(aabb.minZ - 2.0D);
      int i1 = SectionPos.posToSectionCoord(aabb.maxX + 2.0D);
      int j1 = SectionPos.posToSectionCoord(aabb.maxY + 0.0D);
      int k1 = SectionPos.posToSectionCoord(aabb.maxZ + 2.0D);

      for(int l1 = j; l1 <= i1; ++l1) {
         long i2 = SectionPos.asLong(l1, 0, 0);
         long j2 = SectionPos.asLong(l1, -1, -1);
         LongIterator longiterator = this.sectionIds.subSet(i2, j2 + 1L).iterator();

         while(longiterator.hasNext()) {
            long k2 = longiterator.nextLong();
            int l2 = SectionPos.y(k2);
            int i3 = SectionPos.z(k2);
            if (l2 >= k && l2 <= j1 && i3 >= l && i3 <= k1) {
               EntitySection<T> entitysection = this.sections.get(k2);
               if (entitysection != null && !entitysection.isEmpty() && entitysection.getStatus().isAccessible() && abortableiterationconsumer.accept(entitysection).shouldAbort()) {
                  return;
               }
            }
         }
      }

   }

   public LongStream getExistingSectionPositionsInChunk(long i) {
      int j = ChunkPos.getX(i);
      int k = ChunkPos.getZ(i);
      LongSortedSet longsortedset = this.getChunkSections(j, k);
      if (longsortedset.isEmpty()) {
         return LongStream.empty();
      } else {
         PrimitiveIterator.OfLong primitiveiterator_oflong = longsortedset.iterator();
         return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(primitiveiterator_oflong, 1301), false);
      }
   }

   private LongSortedSet getChunkSections(int i, int j) {
      long k = SectionPos.asLong(i, 0, j);
      long l = SectionPos.asLong(i, -1, j);
      return this.sectionIds.subSet(k, l + 1L);
   }

   public Stream<EntitySection<T>> getExistingSectionsInChunk(long i) {
      return this.getExistingSectionPositionsInChunk(i).mapToObj(this.sections::get).filter(Objects::nonNull);
   }

   private static long getChunkKeyFromSectionKey(long i) {
      return ChunkPos.asLong(SectionPos.x(i), SectionPos.z(i));
   }

   public EntitySection<T> getOrCreateSection(long i) {
      return this.sections.computeIfAbsent(i, this::createSection);
   }

   @Nullable
   public EntitySection<T> getSection(long i) {
      return this.sections.get(i);
   }

   private EntitySection<T> createSection(long j) {
      long k = getChunkKeyFromSectionKey(j);
      Visibility visibility = this.intialSectionVisibility.get(k);
      this.sectionIds.add(j);
      return new EntitySection<>(this.entityClass, visibility);
   }

   public LongSet getAllChunksWithExistingSections() {
      LongSet longset = new LongOpenHashSet();
      this.sections.keySet().forEach((i) -> longset.add(getChunkKeyFromSectionKey(i)));
      return longset;
   }

   public void getEntities(AABB aabb, AbortableIterationConsumer<T> abortableiterationconsumer) {
      this.forEachAccessibleNonEmptySection(aabb, (entitysection) -> entitysection.getEntities(aabb, abortableiterationconsumer));
   }

   public <U extends T> void getEntities(EntityTypeTest<T, U> entitytypetest, AABB aabb, AbortableIterationConsumer<U> abortableiterationconsumer) {
      this.forEachAccessibleNonEmptySection(aabb, (entitysection) -> entitysection.getEntities(entitytypetest, aabb, abortableiterationconsumer));
   }

   public void remove(long i) {
      this.sections.remove(i);
      this.sectionIds.remove(i);
   }

   @VisibleForDebug
   public int count() {
      return this.sectionIds.size();
   }
}
