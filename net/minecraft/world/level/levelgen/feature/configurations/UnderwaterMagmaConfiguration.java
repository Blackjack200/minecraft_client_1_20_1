package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UnderwaterMagmaConfiguration implements FeatureConfiguration {
   public static final Codec<UnderwaterMagmaConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.intRange(0, 512).fieldOf("floor_search_range").forGetter((underwatermagmaconfiguration2) -> underwatermagmaconfiguration2.floorSearchRange), Codec.intRange(0, 64).fieldOf("placement_radius_around_floor").forGetter((underwatermagmaconfiguration1) -> underwatermagmaconfiguration1.placementRadiusAroundFloor), Codec.floatRange(0.0F, 1.0F).fieldOf("placement_probability_per_valid_position").forGetter((underwatermagmaconfiguration) -> underwatermagmaconfiguration.placementProbabilityPerValidPosition)).apply(recordcodecbuilder_instance, UnderwaterMagmaConfiguration::new));
   public final int floorSearchRange;
   public final int placementRadiusAroundFloor;
   public final float placementProbabilityPerValidPosition;

   public UnderwaterMagmaConfiguration(int i, int j, float f) {
      this.floorSearchRange = i;
      this.placementRadiusAroundFloor = j;
      this.placementProbabilityPerValidPosition = f;
   }
}
