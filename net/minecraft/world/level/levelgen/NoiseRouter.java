package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction initialDensityWithoutJaggedness, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
   public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(field("barrier", NoiseRouter::barrierNoise), field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise), field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise), field("lava", NoiseRouter::lavaNoise), field("temperature", NoiseRouter::temperature), field("vegetation", NoiseRouter::vegetation), field("continents", NoiseRouter::continents), field("erosion", NoiseRouter::erosion), field("depth", NoiseRouter::depth), field("ridges", NoiseRouter::ridges), field("initial_density_without_jaggedness", NoiseRouter::initialDensityWithoutJaggedness), field("final_density", NoiseRouter::finalDensity), field("vein_toggle", NoiseRouter::veinToggle), field("vein_ridged", NoiseRouter::veinRidged), field("vein_gap", NoiseRouter::veinGap)).apply(recordcodecbuilder_instance, NoiseRouter::new));

   private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String s, Function<NoiseRouter, DensityFunction> function) {
      return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(s).forGetter(function);
   }

   public NoiseRouter mapAll(DensityFunction.Visitor densityfunction_visitor) {
      return new NoiseRouter(this.barrierNoise.mapAll(densityfunction_visitor), this.fluidLevelFloodednessNoise.mapAll(densityfunction_visitor), this.fluidLevelSpreadNoise.mapAll(densityfunction_visitor), this.lavaNoise.mapAll(densityfunction_visitor), this.temperature.mapAll(densityfunction_visitor), this.vegetation.mapAll(densityfunction_visitor), this.continents.mapAll(densityfunction_visitor), this.erosion.mapAll(densityfunction_visitor), this.depth.mapAll(densityfunction_visitor), this.ridges.mapAll(densityfunction_visitor), this.initialDensityWithoutJaggedness.mapAll(densityfunction_visitor), this.finalDensity.mapAll(densityfunction_visitor), this.veinToggle.mapAll(densityfunction_visitor), this.veinRidged.mapAll(densityfunction_visitor), this.veinGap.mapAll(densityfunction_visitor));
   }
}
