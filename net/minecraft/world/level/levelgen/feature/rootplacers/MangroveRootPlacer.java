package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer extends RootPlacer {
   public static final int ROOT_WIDTH_LIMIT = 8;
   public static final int ROOT_LENGTH_LIMIT = 15;
   public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> rootPlacerParts(recordcodecbuilder_instance).and(MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter((mangroverootplacer) -> mangroverootplacer.mangroveRootPlacement)).apply(recordcodecbuilder_instance, MangroveRootPlacer::new));
   private final MangroveRootPlacement mangroveRootPlacement;

   public MangroveRootPlacer(IntProvider intprovider, BlockStateProvider blockstateprovider, Optional<AboveRootPlacement> optional, MangroveRootPlacement mangroverootplacement) {
      super(intprovider, blockstateprovider, optional);
      this.mangroveRootPlacement = mangroverootplacement;
   }

   public boolean placeRoots(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, BlockPos blockpos1, TreeConfiguration treeconfiguration) {
      List<BlockPos> list = Lists.newArrayList();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      while(blockpos_mutableblockpos.getY() < blockpos1.getY()) {
         if (!this.canPlaceRoot(levelsimulatedreader, blockpos_mutableblockpos)) {
            return false;
         }

         blockpos_mutableblockpos.move(Direction.UP);
      }

      list.add(blockpos1.below());

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos2 = blockpos1.relative(direction);
         List<BlockPos> list1 = Lists.newArrayList();
         if (!this.simulateRoots(levelsimulatedreader, randomsource, blockpos2, direction, blockpos1, list1, 0)) {
            return false;
         }

         list.addAll(list1);
         list.add(blockpos1.relative(direction));
      }

      for(BlockPos blockpos3 : list) {
         this.placeRoot(levelsimulatedreader, biconsumer, randomsource, blockpos3, treeconfiguration);
      }

      return true;
   }

   private boolean simulateRoots(LevelSimulatedReader levelsimulatedreader, RandomSource randomsource, BlockPos blockpos, Direction direction, BlockPos blockpos1, List<BlockPos> list, int i) {
      int j = this.mangroveRootPlacement.maxRootLength();
      if (i != j && list.size() <= j) {
         for(BlockPos blockpos2 : this.potentialRootPositions(blockpos, direction, randomsource, blockpos1)) {
            if (this.canPlaceRoot(levelsimulatedreader, blockpos2)) {
               list.add(blockpos2);
               if (!this.simulateRoots(levelsimulatedreader, randomsource, blockpos2, direction, blockpos1, list, i + 1)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected List<BlockPos> potentialRootPositions(BlockPos blockpos, Direction direction, RandomSource randomsource, BlockPos blockpos1) {
      BlockPos blockpos2 = blockpos.below();
      BlockPos blockpos3 = blockpos.relative(direction);
      int i = blockpos.distManhattan(blockpos1);
      int j = this.mangroveRootPlacement.maxRootWidth();
      float f = this.mangroveRootPlacement.randomSkewChance();
      if (i > j - 3 && i <= j) {
         return randomsource.nextFloat() < f ? List.of(blockpos2, blockpos3.below()) : List.of(blockpos2);
      } else if (i > j) {
         return List.of(blockpos2);
      } else if (randomsource.nextFloat() < f) {
         return List.of(blockpos2);
      } else {
         return randomsource.nextBoolean() ? List.of(blockpos3) : List.of(blockpos2);
      }
   }

   protected boolean canPlaceRoot(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return super.canPlaceRoot(levelsimulatedreader, blockpos) || levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> blockstate.is(this.mangroveRootPlacement.canGrowThrough()));
   }

   protected void placeRoot(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      if (levelsimulatedreader.isStateAtPosition(blockpos, (blockstate1) -> blockstate1.is(this.mangroveRootPlacement.muddyRootsIn()))) {
         BlockState blockstate = this.mangroveRootPlacement.muddyRootsProvider().getState(randomsource, blockpos);
         biconsumer.accept(blockpos, this.getPotentiallyWaterloggedState(levelsimulatedreader, blockpos, blockstate));
      } else {
         super.placeRoot(levelsimulatedreader, biconsumer, randomsource, blockpos, treeconfiguration);
      }

   }

   protected RootPlacerType<?> type() {
      return RootPlacerType.MANGROVE_ROOT_PLACER;
   }
}
