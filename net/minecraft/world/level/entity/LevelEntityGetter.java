package net.minecraft.world.level.entity;

import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.phys.AABB;

public interface LevelEntityGetter<T extends EntityAccess> {
   @Nullable
   T get(int i);

   @Nullable
   T get(UUID uuid);

   Iterable<T> getAll();

   <U extends T> void get(EntityTypeTest<T, U> entitytypetest, AbortableIterationConsumer<U> abortableiterationconsumer);

   void get(AABB aabb, Consumer<T> consumer);

   <U extends T> void get(EntityTypeTest<T, U> entitytypetest, AABB aabb, AbortableIterationConsumer<U> abortableiterationconsumer);
}
