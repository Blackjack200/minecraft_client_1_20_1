package net.minecraft.world.level.timers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface TimerCallback<T> {
   void handle(T object, TimerQueue<T> timerqueue, long i);

   public abstract static class Serializer<T, C extends TimerCallback<T>> {
      private final ResourceLocation id;
      private final Class<?> cls;

      public Serializer(ResourceLocation resourcelocation, Class<?> oclass) {
         this.id = resourcelocation;
         this.cls = oclass;
      }

      public ResourceLocation getId() {
         return this.id;
      }

      public Class<?> getCls() {
         return this.cls;
      }

      public abstract void serialize(CompoundTag compoundtag, C timercallback);

      public abstract C deserialize(CompoundTag compoundtag);
   }
}
