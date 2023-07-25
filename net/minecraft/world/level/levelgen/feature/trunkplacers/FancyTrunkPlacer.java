package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class FancyTrunkPlacer extends TrunkPlacer {
   public static final Codec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> trunkPlacerParts(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, FancyTrunkPlacer::new));
   private static final double TRUNK_HEIGHT_SCALE = 0.618D;
   private static final double CLUSTER_DENSITY_MAGIC = 1.382D;
   private static final double BRANCH_SLOPE = 0.381D;
   private static final double BRANCH_LENGTH_MAGIC = 0.328D;

   public FancyTrunkPlacer(int i, int j, int k) {
      super(i, j, k);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.FANCY_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      int j = 5;
      int k = i + 2;
      int l = Mth.floor((double)k * 0.618D);
      setDirtAt(levelsimulatedreader, biconsumer, randomsource, blockpos.below(), treeconfiguration);
      double d0 = 1.0D;
      int i1 = Math.min(1, Mth.floor(1.382D + Math.pow(1.0D * (double)k / 13.0D, 2.0D)));
      int j1 = blockpos.getY() + l;
      int k1 = k - 5;
      List<FancyTrunkPlacer.FoliageCoords> list = Lists.newArrayList();
      list.add(new FancyTrunkPlacer.FoliageCoords(blockpos.above(k1), j1));

      for(; k1 >= 0; --k1) {
         float f = treeShape(k, k1);
         if (!(f < 0.0F)) {
            for(int l1 = 0; l1 < i1; ++l1) {
               double d1 = 1.0D;
               double d2 = 1.0D * (double)f * ((double)randomsource.nextFloat() + 0.328D);
               double d3 = (double)(randomsource.nextFloat() * 2.0F) * Math.PI;
               double d4 = d2 * Math.sin(d3) + 0.5D;
               double d5 = d2 * Math.cos(d3) + 0.5D;
               BlockPos blockpos1 = blockpos.offset(Mth.floor(d4), k1 - 1, Mth.floor(d5));
               BlockPos blockpos2 = blockpos1.above(5);
               if (this.makeLimb(levelsimulatedreader, biconsumer, randomsource, blockpos1, blockpos2, false, treeconfiguration)) {
                  int i2 = blockpos.getX() - blockpos1.getX();
                  int j2 = blockpos.getZ() - blockpos1.getZ();
                  double d6 = (double)blockpos1.getY() - Math.sqrt((double)(i2 * i2 + j2 * j2)) * 0.381D;
                  int k2 = d6 > (double)j1 ? j1 : (int)d6;
                  BlockPos blockpos3 = new BlockPos(blockpos.getX(), k2, blockpos.getZ());
                  if (this.makeLimb(levelsimulatedreader, biconsumer, randomsource, blockpos3, blockpos1, false, treeconfiguration)) {
                     list.add(new FancyTrunkPlacer.FoliageCoords(blockpos1, blockpos3.getY()));
                  }
               }
            }
         }
      }

      this.makeLimb(levelsimulatedreader, biconsumer, randomsource, blockpos, blockpos.above(l), true, treeconfiguration);
      this.makeBranches(levelsimulatedreader, biconsumer, randomsource, k, blockpos, list, treeconfiguration);
      List<FoliagePlacer.FoliageAttachment> list1 = Lists.newArrayList();

      for(FancyTrunkPlacer.FoliageCoords fancytrunkplacer_foliagecoords : list) {
         if (this.trimBranches(k, fancytrunkplacer_foliagecoords.getBranchBase() - blockpos.getY())) {
            list1.add(fancytrunkplacer_foliagecoords.attachment);
         }
      }

      return list1;
   }

   private boolean makeLimb(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, BlockPos blockpos, BlockPos blockpos1, boolean flag, TreeConfiguration treeconfiguration) {
      if (!flag && Objects.equals(blockpos, blockpos1)) {
         return true;
      } else {
         BlockPos blockpos2 = blockpos1.offset(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ());
         int i = this.getSteps(blockpos2);
         float f = (float)blockpos2.getX() / (float)i;
         float f1 = (float)blockpos2.getY() / (float)i;
         float f2 = (float)blockpos2.getZ() / (float)i;

         for(int j = 0; j <= i; ++j) {
            BlockPos blockpos3 = blockpos.offset(Mth.floor(0.5F + (float)j * f), Mth.floor(0.5F + (float)j * f1), Mth.floor(0.5F + (float)j * f2));
            if (flag) {
               this.placeLog(levelsimulatedreader, biconsumer, randomsource, blockpos3, treeconfiguration, (blockstate) -> blockstate.trySetValue(RotatedPillarBlock.AXIS, this.getLogAxis(blockpos, blockpos3)));
            } else if (!this.isFree(levelsimulatedreader, blockpos3)) {
               return false;
            }
         }

         return true;
      }
   }

   private int getSteps(BlockPos blockpos) {
      int i = Mth.abs(blockpos.getX());
      int j = Mth.abs(blockpos.getY());
      int k = Mth.abs(blockpos.getZ());
      return Math.max(i, Math.max(j, k));
   }

   private Direction.Axis getLogAxis(BlockPos blockpos, BlockPos blockpos1) {
      Direction.Axis direction_axis = Direction.Axis.Y;
      int i = Math.abs(blockpos1.getX() - blockpos.getX());
      int j = Math.abs(blockpos1.getZ() - blockpos.getZ());
      int k = Math.max(i, j);
      if (k > 0) {
         if (i == k) {
            direction_axis = Direction.Axis.X;
         } else {
            direction_axis = Direction.Axis.Z;
         }
      }

      return direction_axis;
   }

   private boolean trimBranches(int i, int j) {
      return (double)j >= (double)i * 0.2D;
   }

   private void makeBranches(LevelSimulatedReader levelsimulatedreader, BiConsumer<BlockPos, BlockState> biconsumer, RandomSource randomsource, int i, BlockPos blockpos, List<FancyTrunkPlacer.FoliageCoords> list, TreeConfiguration treeconfiguration) {
      for(FancyTrunkPlacer.FoliageCoords fancytrunkplacer_foliagecoords : list) {
         int j = fancytrunkplacer_foliagecoords.getBranchBase();
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), j, blockpos.getZ());
         if (!blockpos1.equals(fancytrunkplacer_foliagecoords.attachment.pos()) && this.trimBranches(i, j - blockpos.getY())) {
            this.makeLimb(levelsimulatedreader, biconsumer, randomsource, blockpos1, fancytrunkplacer_foliagecoords.attachment.pos(), true, treeconfiguration);
         }
      }

   }

   private static float treeShape(int i, int j) {
      if ((float)j < (float)i * 0.3F) {
         return -1.0F;
      } else {
         float f = (float)i / 2.0F;
         float f1 = f - (float)j;
         float f2 = Mth.sqrt(f * f - f1 * f1);
         if (f1 == 0.0F) {
            f2 = f;
         } else if (Math.abs(f1) >= f) {
            return 0.0F;
         }

         return f2 * 0.5F;
      }
   }

   static class FoliageCoords {
      final FoliagePlacer.FoliageAttachment attachment;
      private final int branchBase;

      public FoliageCoords(BlockPos blockpos, int i) {
         this.attachment = new FoliagePlacer.FoliageAttachment(blockpos, 0, false);
         this.branchBase = i;
      }

      public int getBranchBase() {
         return this.branchBase;
      }
   }
}
