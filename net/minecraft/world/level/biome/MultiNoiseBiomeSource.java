package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource extends BiomeSource {
   private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
   public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.<Holder<Biome>>codec(ENTRY_CODEC).fieldOf("biomes");
   private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC = MultiNoiseBiomeSourceParameterList.CODEC.fieldOf("preset").withLifecycle(Lifecycle.stable());
   public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC).xmap(MultiNoiseBiomeSource::new, (multinoisebiomesource) -> multinoisebiomesource.parameters).codec();
   private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

   private MultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> either) {
      this.parameters = either;
   }

   public static MultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> climate_parameterlist) {
      return new MultiNoiseBiomeSource(Either.left(climate_parameterlist));
   }

   public static MultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> holder) {
      return new MultiNoiseBiomeSource(Either.right(holder));
   }

   private Climate.ParameterList<Holder<Biome>> parameters() {
      return this.parameters.map((climate_parameterlist) -> climate_parameterlist, (holder) -> holder.value().parameters());
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return this.parameters().values().stream().map(Pair::getSecond);
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> resourcekey) {
      Optional<Holder<MultiNoiseBiomeSourceParameterList>> optional = this.parameters.right();
      return optional.isPresent() && optional.get().is(resourcekey);
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler climate_sampler) {
      return this.getNoiseBiome(climate_sampler.sample(i, j, k));
   }

   @VisibleForDebug
   public Holder<Biome> getNoiseBiome(Climate.TargetPoint climate_targetpoint) {
      return this.parameters().findValue(climate_targetpoint);
   }

   public void addDebugInfo(List<String> list, BlockPos blockpos, Climate.Sampler climate_sampler) {
      int i = QuartPos.fromBlock(blockpos.getX());
      int j = QuartPos.fromBlock(blockpos.getY());
      int k = QuartPos.fromBlock(blockpos.getZ());
      Climate.TargetPoint climate_targetpoint = climate_sampler.sample(i, j, k);
      float f = Climate.unquantizeCoord(climate_targetpoint.continentalness());
      float f1 = Climate.unquantizeCoord(climate_targetpoint.erosion());
      float f2 = Climate.unquantizeCoord(climate_targetpoint.temperature());
      float f3 = Climate.unquantizeCoord(climate_targetpoint.humidity());
      float f4 = Climate.unquantizeCoord(climate_targetpoint.weirdness());
      double d0 = (double)NoiseRouterData.peaksAndValleys(f4);
      OverworldBiomeBuilder overworldbiomebuilder = new OverworldBiomeBuilder();
      list.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d0) + " C: " + overworldbiomebuilder.getDebugStringForContinentalness((double)f) + " E: " + overworldbiomebuilder.getDebugStringForErosion((double)f1) + " T: " + overworldbiomebuilder.getDebugStringForTemperature((double)f2) + " H: " + overworldbiomebuilder.getDebugStringForHumidity((double)f3));
   }
}
