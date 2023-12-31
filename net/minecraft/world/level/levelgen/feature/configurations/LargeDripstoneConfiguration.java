package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;

public class LargeDripstoneConfiguration implements FeatureConfiguration {
   public static final Codec<LargeDripstoneConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.intRange(1, 512).fieldOf("floor_to_ceiling_search_range").orElse(30).forGetter((largedripstoneconfiguration8) -> largedripstoneconfiguration8.floorToCeilingSearchRange), IntProvider.codec(1, 60).fieldOf("column_radius").forGetter((largedripstoneconfiguration7) -> largedripstoneconfiguration7.columnRadius), FloatProvider.codec(0.0F, 20.0F).fieldOf("height_scale").forGetter((largedripstoneconfiguration6) -> largedripstoneconfiguration6.heightScale), Codec.floatRange(0.1F, 1.0F).fieldOf("max_column_radius_to_cave_height_ratio").forGetter((largedripstoneconfiguration5) -> largedripstoneconfiguration5.maxColumnRadiusToCaveHeightRatio), FloatProvider.codec(0.1F, 10.0F).fieldOf("stalactite_bluntness").forGetter((largedripstoneconfiguration4) -> largedripstoneconfiguration4.stalactiteBluntness), FloatProvider.codec(0.1F, 10.0F).fieldOf("stalagmite_bluntness").forGetter((largedripstoneconfiguration3) -> largedripstoneconfiguration3.stalagmiteBluntness), FloatProvider.codec(0.0F, 2.0F).fieldOf("wind_speed").forGetter((largedripstoneconfiguration2) -> largedripstoneconfiguration2.windSpeed), Codec.intRange(0, 100).fieldOf("min_radius_for_wind").forGetter((largedripstoneconfiguration1) -> largedripstoneconfiguration1.minRadiusForWind), Codec.floatRange(0.0F, 5.0F).fieldOf("min_bluntness_for_wind").forGetter((largedripstoneconfiguration) -> largedripstoneconfiguration.minBluntnessForWind)).apply(recordcodecbuilder_instance, LargeDripstoneConfiguration::new));
   public final int floorToCeilingSearchRange;
   public final IntProvider columnRadius;
   public final FloatProvider heightScale;
   public final float maxColumnRadiusToCaveHeightRatio;
   public final FloatProvider stalactiteBluntness;
   public final FloatProvider stalagmiteBluntness;
   public final FloatProvider windSpeed;
   public final int minRadiusForWind;
   public final float minBluntnessForWind;

   public LargeDripstoneConfiguration(int i, IntProvider intprovider, FloatProvider floatprovider, float f, FloatProvider floatprovider1, FloatProvider floatprovider2, FloatProvider floatprovider3, int j, float f1) {
      this.floorToCeilingSearchRange = i;
      this.columnRadius = intprovider;
      this.heightScale = floatprovider;
      this.maxColumnRadiusToCaveHeightRatio = f;
      this.stalactiteBluntness = floatprovider1;
      this.stalagmiteBluntness = floatprovider2;
      this.windSpeed = floatprovider3;
      this.minRadiusForWind = j;
      this.minBluntnessForWind = f1;
   }
}
