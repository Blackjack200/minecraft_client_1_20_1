package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PointedDripstoneConfiguration implements FeatureConfiguration {
   public static final Codec<PointedDripstoneConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_taller_dripstone").orElse(0.2F).forGetter((pointeddripstoneconfiguration3) -> pointeddripstoneconfiguration3.chanceOfTallerDripstone), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_directional_spread").orElse(0.7F).forGetter((pointeddripstoneconfiguration2) -> pointeddripstoneconfiguration2.chanceOfDirectionalSpread), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius2").orElse(0.5F).forGetter((pointeddripstoneconfiguration1) -> pointeddripstoneconfiguration1.chanceOfSpreadRadius2), Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_spread_radius3").orElse(0.5F).forGetter((pointeddripstoneconfiguration) -> pointeddripstoneconfiguration.chanceOfSpreadRadius3)).apply(recordcodecbuilder_instance, PointedDripstoneConfiguration::new));
   public final float chanceOfTallerDripstone;
   public final float chanceOfDirectionalSpread;
   public final float chanceOfSpreadRadius2;
   public final float chanceOfSpreadRadius3;

   public PointedDripstoneConfiguration(float f, float f1, float f2, float f3) {
      this.chanceOfTallerDripstone = f;
      this.chanceOfDirectionalSpread = f1;
      this.chanceOfSpreadRadius2 = f2;
      this.chanceOfSpreadRadius3 = f3;
   }
}
