package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
   public static final Codec<RootPlacer> CODEC = BuiltInRegistries.ROOT_PLACER_TYPE.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
   protected final IntProvider trunkOffsetY;
   protected final BlockStateProvider rootProvider;
   protected final Optional<AboveRootPlacement> aboveRootPlacement;

   protected static <P extends RootPlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, BlockStateProvider, Optional<AboveRootPlacement>> rootPlacerParts(RecordCodecBuilder.Instance<P> recordcodecbuilder_instance) {
      return recordcodecbuilder_instance.group(IntProvider.CODEC.fieldOf("trunk_offset_y").forGetter((rootplacer2) -> rootplacer2.trunkOffsetY), BlockStateProvider.CODEC.fieldOf("root_provider").forGetter((rootplacer1) -> rootplacer1.rootProvider), AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter((rootplacer) -> rootplacer.aboveRootPlacement));
   }

   public RootPlacer(IntProvider intprovider, BlockStateProvider blockstateprovider, Optional<AboveRootPlacement> optional) {
      this.trunkOffsetY = intprovider;
      this.rootProvider = blockstateprovider;
      this.aboveRootPlacement = optional;
   }

   protected abstract RootPlacerType<?> type();

   public abstract boolean placeRoots(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, BlockPos blockpos1, TreeConfiguration treeconfiguration);

   protected boolean canPlaceRoot(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return TreeFeature.validTreePos(levelsimulatedreader, blockpos);
   }

   protected void placeRoot(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      if (this.canPlaceRoot(levelsimulatedreader, blockpos)) {
         biconsumer.accept(blockpos, this.getPotentiallyWaterloggedState(levelsimulatedreader, blockpos, this.rootProvider.getState(randomsource, blockpos)));
         if (this.aboveRootPlacement.isPresent()) {
            AboveRootPlacement aboverootplacement = this.aboveRootPlacement.get();
            BlockPos blockpos1 = blockpos.above();
            if (randomsource.nextFloat() < aboverootplacement.aboveRootPlacementChance() && levelsimulatedreader.isStateAtPosition(blockpos1, BlockBehaviour.BlockStateBase::isAir)) {
               biconsumer.accept(blockpos1, this.getPotentiallyWaterloggedState(levelsimulatedreader, blockpos1, aboverootplacement.aboveRootProvider().getState(randomsource, blockpos1)));
            }
         }

      }
   }

   protected BlockState getPotentiallyWaterloggedState(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED)) {
         boolean flag = levelsimulatedreader.isFluidAtPosition(blockpos, (fluidstate) -> fluidstate.is(FluidTags.WATER));
         return blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(flag));
      } else {
         return blockstate;
      }
   }

   public BlockPos getTrunkOrigin(BlockPos blockpos, RandomSource randomsource) {
      return blockpos.above(this.trunkOffsetY.sample(randomsource));
   }
}
