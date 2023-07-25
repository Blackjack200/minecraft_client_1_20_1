package net.minecraft.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public interface DefaultedRegistry<T> extends Registry<T> {
   @Nonnull
   ResourceLocation getKey(T object);

   @Nonnull
   T get(@Nullable ResourceLocation resourcelocation);

   @Nonnull
   T byId(int i);

   ResourceLocation getDefaultKey();
}
