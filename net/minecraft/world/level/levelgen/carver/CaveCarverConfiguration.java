package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration extends CarverConfiguration {
   public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(CarverConfiguration.CODEC.forGetter((cavecarverconfiguration3) -> cavecarverconfiguration3), FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier").forGetter((cavecarverconfiguration2) -> cavecarverconfiguration2.horizontalRadiusMultiplier), FloatProvider.CODEC.fieldOf("vertical_radius_multiplier").forGetter((cavecarverconfiguration1) -> cavecarverconfiguration1.verticalRadiusMultiplier), FloatProvider.codec(-1.0F, 1.0F).fieldOf("floor_level").forGetter((cavecarverconfiguration) -> cavecarverconfiguration.floorLevel)).apply(recordcodecbuilder_instance, CaveCarverConfiguration::new));
   public final FloatProvider horizontalRadiusMultiplier;
   public final FloatProvider verticalRadiusMultiplier;
   final FloatProvider floorLevel;

   public CaveCarverConfiguration(float f, HeightProvider heightprovider, FloatProvider floatprovider, VerticalAnchor verticalanchor, CarverDebugSettings carverdebugsettings, HolderSet<Block> holderset, FloatProvider floatprovider1, FloatProvider floatprovider2, FloatProvider floatprovider3) {
      super(f, heightprovider, floatprovider, verticalanchor, carverdebugsettings, holderset);
      this.horizontalRadiusMultiplier = floatprovider1;
      this.verticalRadiusMultiplier = floatprovider2;
      this.floorLevel = floatprovider3;
   }

   public CaveCarverConfiguration(float f, HeightProvider heightprovider, FloatProvider floatprovider, VerticalAnchor verticalanchor, HolderSet<Block> holderset, FloatProvider floatprovider1, FloatProvider floatprovider2, FloatProvider floatprovider3) {
      this(f, heightprovider, floatprovider, verticalanchor, CarverDebugSettings.DEFAULT, holderset, floatprovider1, floatprovider2, floatprovider3);
   }

   public CaveCarverConfiguration(CarverConfiguration carverconfiguration, FloatProvider floatprovider, FloatProvider floatprovider1, FloatProvider floatprovider2) {
      this(carverconfiguration.probability, carverconfiguration.y, carverconfiguration.yScale, carverconfiguration.lavaLevel, carverconfiguration.debugSettings, carverconfiguration.replaceable, floatprovider, floatprovider1, floatprovider2);
   }
}
