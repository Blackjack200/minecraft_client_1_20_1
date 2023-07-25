package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(IntProvider.codec(0, 3).fieldOf("reach").forGetter((columnfeatureconfiguration1) -> columnfeatureconfiguration1.reach), IntProvider.codec(1, 10).fieldOf("height").forGetter((columnfeatureconfiguration) -> columnfeatureconfiguration.height)).apply(recordcodecbuilder_instance, ColumnFeatureConfiguration::new));
   private final IntProvider reach;
   private final IntProvider height;

   public ColumnFeatureConfiguration(IntProvider intprovider, IntProvider intprovider1) {
      this.reach = intprovider;
      this.height = intprovider1;
   }

   public IntProvider reach() {
      return this.reach;
   }

   public IntProvider height() {
      return this.height;
   }
}
