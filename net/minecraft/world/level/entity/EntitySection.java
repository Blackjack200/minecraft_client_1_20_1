package net.minecraft.world.level.entity;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class EntitySection<T extends EntityAccess> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ClassInstanceMultiMap<T> storage;
   private Visibility chunkStatus;

   public EntitySection(Class<T> oclass, Visibility visibility) {
      this.chunkStatus = visibility;
      this.storage = new ClassInstanceMultiMap<>(oclass);
   }

   public void add(T entityaccess) {
      this.storage.add(entityaccess);
   }

   public boolean remove(T entityaccess) {
      return this.storage.remove(entityaccess);
   }

   public AbortableIterationConsumer.Continuation getEntities(AABB aabb, AbortableIterationConsumer<T> abortableiterationconsumer) {
      for(T entityaccess : this.storage) {
         if (entityaccess.getBoundingBox().intersects(aabb) && abortableiterationconsumer.accept(entityaccess).shouldAbort()) {
            return AbortableIterationConsumer.Continuation.ABORT;
         }
      }

      return AbortableIterationConsumer.Continuation.CONTINUE;
   }

   public <U extends T> AbortableIterationConsumer.Continuation getEntities(EntityTypeTest<T, U> entitytypetest, AABB aabb, AbortableIterationConsumer<? super U> abortableiterationconsumer) {
      Collection<? extends T> collection = this.storage.find(entitytypetest.getBaseClass());
      if (collection.isEmpty()) {
         return AbortableIterationConsumer.Continuation.CONTINUE;
      } else {
         for(T entityaccess : collection) {
            U entityaccess1 = (U)((EntityAccess)entitytypetest.tryCast(entityaccess));
            if (entityaccess1 != null && entityaccess.getBoundingBox().intersects(aabb) && abortableiterationconsumer.accept((T)entityaccess1).shouldAbort()) {
               return AbortableIterationConsumer.Continuation.ABORT;
            }
         }

         return AbortableIterationConsumer.Continuation.CONTINUE;
      }
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public Stream<T> getEntities() {
      return this.storage.stream();
   }

   public Visibility getStatus() {
      return this.chunkStatus;
   }

   public Visibility updateChunkStatus(Visibility visibility) {
      Visibility visibility1 = this.chunkStatus;
      this.chunkStatus = visibility;
      return visibility1;
   }

   @VisibleForDebug
   public int size() {
      return this.storage.size();
   }
}
