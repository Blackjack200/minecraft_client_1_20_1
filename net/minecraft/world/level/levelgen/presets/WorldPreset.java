package net.minecraft.world.level.levelgen.presets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

public class WorldPreset {
   public static final Codec<WorldPreset> DIRECT_CODEC = ExtraCodecs.validate(RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter((worldpreset) -> worldpreset.dimensions)).apply(recordcodecbuilder_instance, WorldPreset::new)), WorldPreset::requireOverworld);
   public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registries.WORLD_PRESET, DIRECT_CODEC);
   private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

   public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> map) {
      this.dimensions = map;
   }

   private Registry<LevelStem> createRegistry() {
      WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.experimental());
      WorldDimensions.keysInOrder(this.dimensions.keySet().stream()).forEach((resourcekey) -> {
         LevelStem levelstem = this.dimensions.get(resourcekey);
         if (levelstem != null) {
            writableregistry.register(resourcekey, levelstem, Lifecycle.stable());
         }

      });
      return writableregistry.freeze();
   }

   public WorldDimensions createWorldDimensions() {
      return new WorldDimensions(this.createRegistry());
   }

   public Optional<LevelStem> overworld() {
      return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
   }

   private static DataResult<WorldPreset> requireOverworld(WorldPreset worldpreset) {
      return worldpreset.overworld().isEmpty() ? DataResult.error(() -> "Missing overworld dimension") : DataResult.success(worldpreset, Lifecycle.stable());
   }
}
