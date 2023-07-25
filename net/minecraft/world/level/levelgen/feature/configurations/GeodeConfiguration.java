package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;

public class GeodeConfiguration implements FeatureConfiguration {
   public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0D, 1.0D);
   public static final Codec<GeodeConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter((geodeconfiguration12) -> geodeconfiguration12.geodeBlockSettings), GeodeLayerSettings.CODEC.fieldOf("layers").forGetter((geodeconfiguration11) -> geodeconfiguration11.geodeLayerSettings), GeodeCrackSettings.CODEC.fieldOf("crack").forGetter((geodeconfiguration10) -> geodeconfiguration10.geodeCrackSettings), CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35D).forGetter((geodeconfiguration9) -> geodeconfiguration9.usePotentialPlacementsChance), CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0D).forGetter((geodeconfiguration8) -> geodeconfiguration8.useAlternateLayer0Chance), Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter((geodeconfiguration7) -> geodeconfiguration7.placementsRequireLayer0Alternate), IntProvider.codec(1, 20).fieldOf("outer_wall_distance").orElse(UniformInt.of(4, 5)).forGetter((geodeconfiguration6) -> geodeconfiguration6.outerWallDistance), IntProvider.codec(1, 20).fieldOf("distribution_points").orElse(UniformInt.of(3, 4)).forGetter((geodeconfiguration5) -> geodeconfiguration5.distributionPoints), IntProvider.codec(0, 10).fieldOf("point_offset").orElse(UniformInt.of(1, 2)).forGetter((geodeconfiguration4) -> geodeconfiguration4.pointOffset), Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter((geodeconfiguration3) -> geodeconfiguration3.minGenOffset), Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter((geodeconfiguration2) -> geodeconfiguration2.maxGenOffset), CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05D).forGetter((geodeconfiguration1) -> geodeconfiguration1.noiseMultiplier), Codec.INT.fieldOf("invalid_blocks_threshold").forGetter((geodeconfiguration) -> geodeconfiguration.invalidBlocksThreshold)).apply(recordcodecbuilder_instance, GeodeConfiguration::new));
   public final GeodeBlockSettings geodeBlockSettings;
   public final GeodeLayerSettings geodeLayerSettings;
   public final GeodeCrackSettings geodeCrackSettings;
   public final double usePotentialPlacementsChance;
   public final double useAlternateLayer0Chance;
   public final boolean placementsRequireLayer0Alternate;
   public final IntProvider outerWallDistance;
   public final IntProvider distributionPoints;
   public final IntProvider pointOffset;
   public final int minGenOffset;
   public final int maxGenOffset;
   public final double noiseMultiplier;
   public final int invalidBlocksThreshold;

   public GeodeConfiguration(GeodeBlockSettings geodeblocksettings, GeodeLayerSettings geodelayersettings, GeodeCrackSettings geodecracksettings, double d0, double d1, boolean flag, IntProvider intprovider, IntProvider intprovider1, IntProvider intprovider2, int i, int j, double d2, int k) {
      this.geodeBlockSettings = geodeblocksettings;
      this.geodeLayerSettings = geodelayersettings;
      this.geodeCrackSettings = geodecracksettings;
      this.usePotentialPlacementsChance = d0;
      this.useAlternateLayer0Chance = d1;
      this.placementsRequireLayer0Alternate = flag;
      this.outerWallDistance = intprovider;
      this.distributionPoints = intprovider1;
      this.pointOffset = intprovider2;
      this.minGenOffset = i;
      this.maxGenOffset = j;
      this.noiseMultiplier = d2;
      this.invalidBlocksThreshold = k;
   }
}
