package net.minecraft.world.level.block.grower;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class CherryTreeGrower extends AbstractTreeGrower {
   protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomsource, boolean flag) {
      return flag ? TreeFeatures.CHERRY_BEES_005 : TreeFeatures.CHERRY;
   }
}
