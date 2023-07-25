package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature extends Feature<TreeConfiguration> {
   private static final int BLOCK_UPDATE_FLAGS = 19;

   public TreeFeature(Codec<TreeConfiguration> codec) {
      super(codec);
   }

   private static boolean isVine(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> blockstate.is(Blocks.VINE));
   }

   public static boolean isAirOrLeaves(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> blockstate.isAir() || blockstate.is(BlockTags.LEAVES));
   }

   private static void setBlockKnownShape(LevelWriter levelwriter, BlockPos blockpos, BlockState blockstate) {
      levelwriter.setBlock(blockpos, blockstate, 19);
   }

   public static boolean validTreePos(LevelSimulatedReader levelsimulatedreader, BlockPos blockpos) {
      return levelsimulatedreader.isStateAtPosition(blockpos, (blockstate) -> blockstate.isAir() || blockstate.is(BlockTags.REPLACEABLE_BY_TREES));
   }

   private boolean doPlace(WorldGenLevel worldgenlevel, RandomSource randomsource, BlockPos blockpos, BiConsumer<BlockPos, BlockState> biconsumer, BiConsumer<BlockPos, BlockState> biconsumer1, FoliagePlacer.FoliageSetter foliageplacer_foliagesetter, TreeConfiguration treeconfiguration) {
      int i = treeconfiguration.trunkPlacer.getTreeHeight(randomsource);
      int j = treeconfiguration.foliagePlacer.foliageHeight(randomsource, i, treeconfiguration);
      int k = i - j;
      int l = treeconfiguration.foliagePlacer.foliageRadius(randomsource, k);
      BlockPos blockpos1 = treeconfiguration.rootPlacer.map((rootplacer) -> rootplacer.getTrunkOrigin(blockpos, randomsource)).orElse(blockpos);
      int i1 = Math.min(blockpos.getY(), blockpos1.getY());
      int j1 = Math.max(blockpos.getY(), blockpos1.getY()) + i + 1;
      if (i1 >= worldgenlevel.getMinBuildHeight() + 1 && j1 <= worldgenlevel.getMaxBuildHeight()) {
         OptionalInt optionalint = treeconfiguration.minimumSize.minClippedHeight();
         int k1 = this.getMaxFreeTreeHeight(worldgenlevel, i, blockpos1, treeconfiguration);
         if (k1 >= i || !optionalint.isEmpty() && k1 >= optionalint.getAsInt()) {
            if (treeconfiguration.rootPlacer.isPresent() && !treeconfiguration.rootPlacer.get().placeRoots(worldgenlevel, biconsumer, randomsource, blockpos, blockpos1, treeconfiguration)) {
               return false;
            } else {
               List<FoliagePlacer.FoliageAttachment> list = treeconfiguration.trunkPlacer.placeTrunk(worldgenlevel, biconsumer1, randomsource, k1, blockpos1, treeconfiguration);
               list.forEach((foliageplacer_foliageattachment) -> treeconfiguration.foliagePlacer.createFoliage(worldgenlevel, foliageplacer_foliagesetter, randomsource, treeconfiguration, k1, foliageplacer_foliageattachment, j, l));
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int getMaxFreeTreeHeight(LevelSimulatedReader levelsimulatedreader, int i, BlockPos blockpos, TreeConfiguration treeconfiguration) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = 0; j <= i + 1; ++j) {
         int k = treeconfiguration.minimumSize.getSizeAtHeight(i, j);

         for(int l = -k; l <= k; ++l) {
            for(int i1 = -k; i1 <= k; ++i1) {
               blockpos_mutableblockpos.setWithOffset(blockpos, l, j, i1);
               if (!treeconfiguration.trunkPlacer.isFree(levelsimulatedreader, blockpos_mutableblockpos) || !treeconfiguration.ignoreVines && isVine(levelsimulatedreader, blockpos_mutableblockpos)) {
                  return j - 2;
               }
            }
         }
      }

      return i;
   }

   protected void setBlock(LevelWriter levelwriter, BlockPos blockpos, BlockState blockstate) {
      setBlockKnownShape(levelwriter, blockpos, blockstate);
   }

   public final boolean place(FeaturePlaceContext<TreeConfiguration> featureplacecontext) {
      final WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      BlockPos blockpos = featureplacecontext.origin();
      TreeConfiguration treeconfiguration = featureplacecontext.config();
      Set<BlockPos> set = Sets.newHashSet();
      Set<BlockPos> set1 = Sets.newHashSet();
      final Set<BlockPos> set2 = Sets.newHashSet();
      Set<BlockPos> set3 = Sets.newHashSet();
      BiConsumer<BlockPos, BlockState> biconsumer = (blockpos3, blockstate2) -> {
         set.add(blockpos3.immutable());
         worldgenlevel.setBlock(blockpos3, blockstate2, 19);
      };
      BiConsumer<BlockPos, BlockState> biconsumer1 = (blockpos2, blockstate1) -> {
         set1.add(blockpos2.immutable());
         worldgenlevel.setBlock(blockpos2, blockstate1, 19);
      };
      FoliagePlacer.FoliageSetter foliageplacer_foliagesetter = new FoliagePlacer.FoliageSetter() {
         public void set(BlockPos blockpos, BlockState blockstate) {
            set2.add(blockpos.immutable());
            worldgenlevel.setBlock(blockpos, blockstate, 19);
         }

         public boolean isSet(BlockPos blockpos) {
            return set2.contains(blockpos);
         }
      };
      BiConsumer<BlockPos, BlockState> biconsumer2 = (blockpos1, blockstate) -> {
         set3.add(blockpos1.immutable());
         worldgenlevel.setBlock(blockpos1, blockstate, 19);
      };
      boolean flag = this.doPlace(worldgenlevel, randomsource, blockpos, biconsumer, biconsumer1, foliageplacer_foliagesetter, treeconfiguration);
      if (flag && (!set1.isEmpty() || !set2.isEmpty())) {
         if (!treeconfiguration.decorators.isEmpty()) {
            TreeDecorator.Context treedecorator_context = new TreeDecorator.Context(worldgenlevel, biconsumer2, randomsource, set1, set2, set);
            treeconfiguration.decorators.forEach((treedecorator) -> treedecorator.place(treedecorator_context));
         }

         return BoundingBox.encapsulatingPositions(Iterables.concat(set, set1, set2, set3)).map((boundingbox) -> {
            DiscreteVoxelShape discretevoxelshape = updateLeaves(worldgenlevel, boundingbox, set1, set3, set);
            StructureTemplate.updateShapeAtEdge(worldgenlevel, 3, discretevoxelshape, boundingbox.minX(), boundingbox.minY(), boundingbox.minZ());
            return true;
         }).orElse(false);
      } else {
         return false;
      }
   }

   private static DiscreteVoxelShape updateLeaves(LevelAccessor levelaccessor, BoundingBox boundingbox, Set<BlockPos> set, Set<BlockPos> set1, Set<BlockPos> set2) {
      DiscreteVoxelShape discretevoxelshape = new BitSetDiscreteVoxelShape(boundingbox.getXSpan(), boundingbox.getYSpan(), boundingbox.getZSpan());
      int i = 7;
      List<Set<BlockPos>> list = Lists.newArrayList();

      for(int j = 0; j < 7; ++j) {
         list.add(Sets.newHashSet());
      }

      for(BlockPos blockpos : Lists.newArrayList(Sets.union(set1, set2))) {
         if (boundingbox.isInside(blockpos)) {
            discretevoxelshape.fill(blockpos.getX() - boundingbox.minX(), blockpos.getY() - boundingbox.minY(), blockpos.getZ() - boundingbox.minZ());
         }
      }

      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int k = 0;
      list.get(0).addAll(set);

      while(true) {
         while(k >= 7 || !list.get(k).isEmpty()) {
            if (k >= 7) {
               return discretevoxelshape;
            }

            Iterator<BlockPos> iterator = list.get(k).iterator();
            BlockPos blockpos1 = iterator.next();
            iterator.remove();
            if (boundingbox.isInside(blockpos1)) {
               if (k != 0) {
                  BlockState blockstate = levelaccessor.getBlockState(blockpos1);
                  setBlockKnownShape(levelaccessor, blockpos1, blockstate.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(k)));
               }

               discretevoxelshape.fill(blockpos1.getX() - boundingbox.minX(), blockpos1.getY() - boundingbox.minY(), blockpos1.getZ() - boundingbox.minZ());

               for(Direction direction : Direction.values()) {
                  blockpos_mutableblockpos.setWithOffset(blockpos1, direction);
                  if (boundingbox.isInside(blockpos_mutableblockpos)) {
                     int l = blockpos_mutableblockpos.getX() - boundingbox.minX();
                     int i1 = blockpos_mutableblockpos.getY() - boundingbox.minY();
                     int j1 = blockpos_mutableblockpos.getZ() - boundingbox.minZ();
                     if (!discretevoxelshape.isFull(l, i1, j1)) {
                        BlockState blockstate1 = levelaccessor.getBlockState(blockpos_mutableblockpos);
                        OptionalInt optionalint = LeavesBlock.getOptionalDistanceAt(blockstate1);
                        if (!optionalint.isEmpty()) {
                           int k1 = Math.min(optionalint.getAsInt(), k + 1);
                           if (k1 < 7) {
                              list.get(k1).add(blockpos_mutableblockpos.immutable());
                              k = Math.min(k, k1);
                           }
                        }
                     }
                  }
               }
            }
         }

         ++k;
      }
   }
}
