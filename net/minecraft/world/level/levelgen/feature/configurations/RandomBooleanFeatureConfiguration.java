package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class RandomBooleanFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<RandomBooleanFeatureConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(PlacedFeature.CODEC.fieldOf("feature_true").forGetter((randombooleanfeatureconfiguration1) -> randombooleanfeatureconfiguration1.featureTrue), PlacedFeature.CODEC.fieldOf("feature_false").forGetter((randombooleanfeatureconfiguration) -> randombooleanfeatureconfiguration.featureFalse)).apply(recordcodecbuilder_instance, RandomBooleanFeatureConfiguration::new));
   public final Holder<PlacedFeature> featureTrue;
   public final Holder<PlacedFeature> featureFalse;

   public RandomBooleanFeatureConfiguration(Holder<PlacedFeature> holder, Holder<PlacedFeature> holder1) {
      this.featureTrue = holder;
      this.featureFalse = holder1;
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return Stream.concat(this.featureTrue.value().getFeatures(), this.featureFalse.value().getFeatures());
   }
}
