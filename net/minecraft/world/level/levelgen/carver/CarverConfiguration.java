package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
   public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((carverconfiguration5) -> carverconfiguration5.probability), HeightProvider.CODEC.fieldOf("y").forGetter((carverconfiguration4) -> carverconfiguration4.y), FloatProvider.CODEC.fieldOf("yScale").forGetter((carverconfiguration3) -> carverconfiguration3.yScale), VerticalAnchor.CODEC.fieldOf("lava_level").forGetter((carverconfiguration2) -> carverconfiguration2.lavaLevel), CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter((carverconfiguration1) -> carverconfiguration1.debugSettings), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable").forGetter((carverconfiguration) -> carverconfiguration.replaceable)).apply(recordcodecbuilder_instance, CarverConfiguration::new));
   public final HeightProvider y;
   public final FloatProvider yScale;
   public final VerticalAnchor lavaLevel;
   public final CarverDebugSettings debugSettings;
   public final HolderSet<Block> replaceable;

   public CarverConfiguration(float f, HeightProvider heightprovider, FloatProvider floatprovider, VerticalAnchor verticalanchor, CarverDebugSettings carverdebugsettings, HolderSet<Block> holderset) {
      super(f);
      this.y = heightprovider;
      this.yScale = floatprovider;
      this.lavaLevel = verticalanchor;
      this.debugSettings = carverdebugsettings;
      this.replaceable = holderset;
   }
}
