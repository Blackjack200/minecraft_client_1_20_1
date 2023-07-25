package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.rootplacers.RootPlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

public class TreeConfiguration implements FeatureConfiguration {
   public static final Codec<TreeConfiguration> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter((treeconfiguration9) -> treeconfiguration9.trunkProvider), TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter((treeconfiguration8) -> treeconfiguration8.trunkPlacer), BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter((treeconfiguration7) -> treeconfiguration7.foliageProvider), FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter((treeconfiguration6) -> treeconfiguration6.foliagePlacer), RootPlacer.CODEC.optionalFieldOf("root_placer").forGetter((treeconfiguration5) -> treeconfiguration5.rootPlacer), BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter((treeconfiguration4) -> treeconfiguration4.dirtProvider), FeatureSize.CODEC.fieldOf("minimum_size").forGetter((treeconfiguration3) -> treeconfiguration3.minimumSize), TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter((treeconfiguration2) -> treeconfiguration2.decorators), Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter((treeconfiguration1) -> treeconfiguration1.ignoreVines), Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter((treeconfiguration) -> treeconfiguration.forceDirt)).apply(recordcodecbuilder_instance, TreeConfiguration::new));
   public final BlockStateProvider trunkProvider;
   public final BlockStateProvider dirtProvider;
   public final TrunkPlacer trunkPlacer;
   public final BlockStateProvider foliageProvider;
   public final FoliagePlacer foliagePlacer;
   public final Optional<RootPlacer> rootPlacer;
   public final FeatureSize minimumSize;
   public final List<TreeDecorator> decorators;
   public final boolean ignoreVines;
   public final boolean forceDirt;

   protected TreeConfiguration(BlockStateProvider blockstateprovider, TrunkPlacer trunkplacer, BlockStateProvider blockstateprovider1, FoliagePlacer foliageplacer, Optional<RootPlacer> optional, BlockStateProvider blockstateprovider2, FeatureSize featuresize, List<TreeDecorator> list, boolean flag, boolean flag1) {
      this.trunkProvider = blockstateprovider;
      this.trunkPlacer = trunkplacer;
      this.foliageProvider = blockstateprovider1;
      this.foliagePlacer = foliageplacer;
      this.rootPlacer = optional;
      this.dirtProvider = blockstateprovider2;
      this.minimumSize = featuresize;
      this.decorators = list;
      this.ignoreVines = flag;
      this.forceDirt = flag1;
   }

   public static class TreeConfigurationBuilder {
      public final BlockStateProvider trunkProvider;
      private final TrunkPlacer trunkPlacer;
      public final BlockStateProvider foliageProvider;
      private final FoliagePlacer foliagePlacer;
      private final Optional<RootPlacer> rootPlacer;
      private BlockStateProvider dirtProvider;
      private final FeatureSize minimumSize;
      private List<TreeDecorator> decorators = ImmutableList.of();
      private boolean ignoreVines;
      private boolean forceDirt;

      public TreeConfigurationBuilder(BlockStateProvider blockstateprovider, TrunkPlacer trunkplacer, BlockStateProvider blockstateprovider1, FoliagePlacer foliageplacer, Optional<RootPlacer> optional, FeatureSize featuresize) {
         this.trunkProvider = blockstateprovider;
         this.trunkPlacer = trunkplacer;
         this.foliageProvider = blockstateprovider1;
         this.dirtProvider = BlockStateProvider.simple(Blocks.DIRT);
         this.foliagePlacer = foliageplacer;
         this.rootPlacer = optional;
         this.minimumSize = featuresize;
      }

      public TreeConfigurationBuilder(BlockStateProvider blockstateprovider, TrunkPlacer trunkplacer, BlockStateProvider blockstateprovider1, FoliagePlacer foliageplacer, FeatureSize featuresize) {
         this(blockstateprovider, trunkplacer, blockstateprovider1, foliageplacer, Optional.empty(), featuresize);
      }

      public TreeConfiguration.TreeConfigurationBuilder dirt(BlockStateProvider blockstateprovider) {
         this.dirtProvider = blockstateprovider;
         return this;
      }

      public TreeConfiguration.TreeConfigurationBuilder decorators(List<TreeDecorator> list) {
         this.decorators = list;
         return this;
      }

      public TreeConfiguration.TreeConfigurationBuilder ignoreVines() {
         this.ignoreVines = true;
         return this;
      }

      public TreeConfiguration.TreeConfigurationBuilder forceDirt() {
         this.forceDirt = true;
         return this;
      }

      public TreeConfiguration build() {
         return new TreeConfiguration(this.trunkProvider, this.trunkPlacer, this.foliageProvider, this.foliagePlacer, this.rootPlacer, this.dirtProvider, this.minimumSize, this.decorators, this.ignoreVines, this.forceDirt);
      }
   }
}
