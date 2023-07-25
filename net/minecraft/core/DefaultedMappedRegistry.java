package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class DefaultedMappedRegistry<T> extends MappedRegistry<T> implements DefaultedRegistry<T> {
   private final ResourceLocation defaultKey;
   private Holder.Reference<T> defaultValue;

   public DefaultedMappedRegistry(String s, ResourceKey<? extends Registry<T>> resourcekey, Lifecycle lifecycle, boolean flag) {
      super(resourcekey, lifecycle, flag);
      this.defaultKey = new ResourceLocation(s);
   }

   public Holder.Reference<T> registerMapping(int i, ResourceKey<T> resourcekey, T object, Lifecycle lifecycle) {
      Holder.Reference<T> holder_reference = super.registerMapping(i, resourcekey, object, lifecycle);
      if (this.defaultKey.equals(resourcekey.location())) {
         this.defaultValue = holder_reference;
      }

      return holder_reference;
   }

   public int getId(@Nullable T object) {
      int i = super.getId(object);
      return i == -1 ? super.getId(this.defaultValue.value()) : i;
   }

   @Nonnull
   public ResourceLocation getKey(T object) {
      ResourceLocation resourcelocation = super.getKey(object);
      return resourcelocation == null ? this.defaultKey : resourcelocation;
   }

   @Nonnull
   public T get(@Nullable ResourceLocation resourcelocation) {
      T object = super.get(resourcelocation);
      return (T)(object == null ? this.defaultValue.value() : object);
   }

   public Optional<T> getOptional(@Nullable ResourceLocation resourcelocation) {
      return Optional.ofNullable(super.get(resourcelocation));
   }

   @Nonnull
   public T byId(int i) {
      T object = super.byId(i);
      return (T)(object == null ? this.defaultValue.value() : object);
   }

   public Optional<Holder.Reference<T>> getRandom(RandomSource randomsource) {
      return super.getRandom(randomsource).or(() -> Optional.of(this.defaultValue));
   }

   public ResourceLocation getDefaultKey() {
      return this.defaultKey;
   }
}
