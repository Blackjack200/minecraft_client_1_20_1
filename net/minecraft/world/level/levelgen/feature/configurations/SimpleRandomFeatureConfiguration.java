package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class SimpleRandomFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<SimpleRandomFeatureConfiguration> CODEC = ExtraCodecs.nonEmptyHolderSet(PlacedFeature.LIST_CODEC).fieldOf("features").xmap(SimpleRandomFeatureConfiguration::new, (simplerandomfeatureconfiguration) -> simplerandomfeatureconfiguration.features).codec();
   public final HolderSet<PlacedFeature> features;

   public SimpleRandomFeatureConfiguration(HolderSet<PlacedFeature> holderset) {
      this.features = holderset;
   }

   public Stream<ConfiguredFeature<?, ?>> getFeatures() {
      return this.features.stream().flatMap((holder) -> holder.value().getFeatures());
   }
}
