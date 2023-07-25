package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CanyonCarverConfiguration extends CarverConfiguration {
   public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(CarverConfiguration.CODEC.forGetter((canyoncarverconfiguration2) -> canyoncarverconfiguration2), FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter((canyoncarverconfiguration1) -> canyoncarverconfiguration1.verticalRotation), CanyonCarverConfiguration.CanyonShapeConfiguration.CODEC.fieldOf("shape").forGetter((canyoncarverconfiguration) -> canyoncarverconfiguration.shape)).apply(recordcodecbuilder_instance, CanyonCarverConfiguration::new));
   public final FloatProvider verticalRotation;
   public final CanyonCarverConfiguration.CanyonShapeConfiguration shape;

   public CanyonCarverConfiguration(float f, HeightProvider heightprovider, FloatProvider floatprovider, VerticalAnchor verticalanchor, CarverDebugSettings carverdebugsettings, HolderSet<Block> holderset, FloatProvider floatprovider1, CanyonCarverConfiguration.CanyonShapeConfiguration canyoncarverconfiguration_canyonshapeconfiguration) {
      super(f, heightprovider, floatprovider, verticalanchor, carverdebugsettings, holderset);
      this.verticalRotation = floatprovider1;
      this.shape = canyoncarverconfiguration_canyonshapeconfiguration;
   }

   public CanyonCarverConfiguration(CarverConfiguration carverconfiguration, FloatProvider floatprovider, CanyonCarverConfiguration.CanyonShapeConfiguration canyoncarverconfiguration_canyonshapeconfiguration) {
      this(carverconfiguration.probability, carverconfiguration.y, carverconfiguration.yScale, carverconfiguration.lavaLevel, carverconfiguration.debugSettings, carverconfiguration.replaceable, floatprovider, canyoncarverconfiguration_canyonshapeconfiguration);
   }

   public static class CanyonShapeConfiguration {
      public static final Codec<CanyonCarverConfiguration.CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(FloatProvider.CODEC.fieldOf("distance_factor").forGetter((canyoncarverconfiguration_canyonshapeconfiguration5) -> canyoncarverconfiguration_canyonshapeconfiguration5.distanceFactor), FloatProvider.CODEC.fieldOf("thickness").forGetter((canyoncarverconfiguration_canyonshapeconfiguration4) -> canyoncarverconfiguration_canyonshapeconfiguration4.thickness), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("width_smoothness").forGetter((canyoncarverconfiguration_canyonshapeconfiguration3) -> canyoncarverconfiguration_canyonshapeconfiguration3.widthSmoothness), FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter((canyoncarverconfiguration_canyonshapeconfiguration2) -> canyoncarverconfiguration_canyonshapeconfiguration2.horizontalRadiusFactor), Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter((canyoncarverconfiguration_canyonshapeconfiguration1) -> canyoncarverconfiguration_canyonshapeconfiguration1.verticalRadiusDefaultFactor), Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter((canyoncarverconfiguration_canyonshapeconfiguration) -> canyoncarverconfiguration_canyonshapeconfiguration.verticalRadiusCenterFactor)).apply(recordcodecbuilder_instance, CanyonCarverConfiguration.CanyonShapeConfiguration::new));
      public final FloatProvider distanceFactor;
      public final FloatProvider thickness;
      public final int widthSmoothness;
      public final FloatProvider horizontalRadiusFactor;
      public final float verticalRadiusDefaultFactor;
      public final float verticalRadiusCenterFactor;

      public CanyonShapeConfiguration(FloatProvider floatprovider, FloatProvider floatprovider1, int i, FloatProvider floatprovider2, float f, float f1) {
         this.widthSmoothness = i;
         this.horizontalRadiusFactor = floatprovider2;
         this.verticalRadiusDefaultFactor = f;
         this.verticalRadiusCenterFactor = f1;
         this.distanceFactor = floatprovider;
         this.thickness = floatprovider1;
      }
   }
}
