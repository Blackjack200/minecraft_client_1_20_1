package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;

public class LevelEntityGetterAdapter<T extends EntityAccess> implements LevelEntityGetter<T> {
   private final EntityLookup<T> visibleEntities;
   private final EntitySectionStorage<T> sectionStorage;

   public LevelEntityGetterAdapter(EntityLookup<T> entitylookup, EntitySectionStorage<T> entitysectionstorage) {
      this.visibleEntities = entitylookup;
      this.sectionStorage = entitysectionstorage;
   }

   @Nullable
   public T get(int i) {
      return this.visibleEntities.getEntity(i);
   }

   @Nullable
   public T get(UUID uuid) {
      return this.visibleEntities.getEntity(uuid);
   }

   public Iterable<T> getAll() {
      return this.visibleEntities.getAllEntities();
   }

   public <U extends T> void get(EntityTypeTest<T, U> entitytypetest, AbortableIterationConsumer<U> abortableiterationconsumer) {
      this.visibleEntities.getEntities(entitytypetest, abortableiterationconsumer);
   }

   public void get(AABB aabb, Consumer<T> consumer) {
      this.sectionStorage.getEntities(aabb, AbortableIterationConsumer.forConsumer(consumer));
   }

   public <U extends T> void get(EntityTypeTest<T, U> entitytypetest, AABB aabb, AbortableIterationConsumer<U> abortableiterationconsumer) {
      this.sectionStorage.getEntities(entitytypetest, aabb, abortableiterationconsumer);
   }
}
