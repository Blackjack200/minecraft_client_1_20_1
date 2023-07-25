package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.apache.commons.lang3.mutable.MutableBoolean;

public record PlacedFeature(Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
   public static final Codec<PlacedFeature> DIRECT_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(ConfiguredFeature.CODEC.fieldOf("feature").forGetter((placedfeature1) -> placedfeature1.feature), PlacementModifier.CODEC.listOf().fieldOf("placement").forGetter((placedfeature) -> placedfeature.placement)).apply(recordcodecbuilder_instance, PlacedFeature::new));
   public static final Codec<Holder<PlacedFeature>> CODEC = RegistryFileCodec.create(Registries.PLACED_FEATURE, DIRECT_CODEC);
   public static final Codec<HolderSet<PlacedFeature>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC);
   public static final Codec<List<HolderSet<PlacedFeature>>> LIST_OF_LISTS_CODEC = RegistryCodecs.homogeneousList(Registries.PLACED_FEATURE, DIRECT_CODEC, true).listOf();

   public boolean place(WorldGenLevel worldgenlevel, ChunkGenerator chunkgenerator, RandomSource randomsource, BlockPos blockpos) {
      return this.placeWithContext(new PlacementContext(worldgenlevel, chunkgenerator, Optional.empty()), randomsource, blockpos);
   }

   public boolean placeWithBiomeCheck(WorldGenLevel worldgenlevel, ChunkGenerator chunkgenerator, RandomSource randomsource, BlockPos blockpos) {
      return this.placeWithContext(new PlacementContext(worldgenlevel, chunkgenerator, Optional.of(this)), randomsource, blockpos);
   }

   private boolean placeWithContext(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      Stream<BlockPos> stream = Stream.of(blockpos);

      for(PlacementModifier placementmodifier : this.placement) {
         stream = stream.flatMap((blockpos2) -> placementmodifier.getPositions(placementcontext, randomsource, blockpos2));
      }

      ConfiguredFeature<?, ?> configuredfeature = this.feature.value();
      MutableBoolean mutableboolean = new MutableBoolean();
      stream.forEach((blockpos1) -> {
         if (configuredfeature.place(placementcontext.getLevel(), placementcontext.generator(), randomsource, blockpos1)) {
            mutableboolean.setTrue();
         }

      });
      return mutableboolean.isTrue();
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return this.feature.value().getFeatures();
   }

   public String toString() {
      return "Placed " + this.feature;
   }
}
