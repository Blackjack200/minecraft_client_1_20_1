package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;

public record WorldGenSettings(WorldOptions options, WorldDimensions dimensions) {
   public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(WorldOptions.CODEC.forGetter(WorldGenSettings::options), WorldDimensions.CODEC.forGetter(WorldGenSettings::dimensions)).apply(recordcodecbuilder_instance, recordcodecbuilder_instance.stable(WorldGenSettings::new)));

   public static <T> DataResult<T> encode(DynamicOps<T> dynamicops, WorldOptions worldoptions, WorldDimensions worlddimensions) {
      return CODEC.encodeStart(dynamicops, new WorldGenSettings(worldoptions, worlddimensions));
   }

   public static <T> DataResult<T> encode(DynamicOps<T> dynamicops, WorldOptions worldoptions, RegistryAccess registryaccess) {
      return encode(dynamicops, worldoptions, new WorldDimensions(registryaccess.registryOrThrow(Registries.LEVEL_STEM)));
   }
}
