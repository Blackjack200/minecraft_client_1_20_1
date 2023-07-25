package net.minecraft.world.level.entity;

import javax.annotation.Nullable;

public interface EntityTypeTest<B, T extends B> {
   static <B, T extends B> EntityTypeTest<B, T> forClass(final Class<T> oclass) {
      return new EntityTypeTest<B, T>() {
         @Nullable
         public T tryCast(B object) {
            return (T)(oclass.isInstance(object) ? object : null);
         }

         public Class<? extends B> getBaseClass() {
            return oclass;
         }
      };
   }

   @Nullable
   T tryCast(B object);

   Class<? extends B> getBaseClass();
}
