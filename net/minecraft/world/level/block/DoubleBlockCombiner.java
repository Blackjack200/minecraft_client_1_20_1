package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class DoubleBlockCombiner {
   public static <S extends BlockEntity> DoubleBlockCombiner.NeighborCombineResult<S> combineWithNeigbour(BlockEntityType<S> blockentitytype, Function<BlockState, DoubleBlockCombiner.BlockType> function, Function<BlockState, Direction> function1, DirectionProperty directionproperty, BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, BiPredicate<LevelAccessor, BlockPos> bipredicate) {
      S blockentity = blockentitytype.getBlockEntity(levelaccessor, blockpos);
      if (blockentity == null) {
         return DoubleBlockCombiner.Combiner::acceptNone;
      } else if (bipredicate.test(levelaccessor, blockpos)) {
         return DoubleBlockCombiner.Combiner::acceptNone;
      } else {
         DoubleBlockCombiner.BlockType doubleblockcombiner_blocktype = function.apply(blockstate);
         boolean flag = doubleblockcombiner_blocktype == DoubleBlockCombiner.BlockType.SINGLE;
         boolean flag1 = doubleblockcombiner_blocktype == DoubleBlockCombiner.BlockType.FIRST;
         if (flag) {
            return new DoubleBlockCombiner.NeighborCombineResult.Single<>(blockentity);
         } else {
            BlockPos blockpos1 = blockpos.relative(function1.apply(blockstate));
            BlockState blockstate1 = levelaccessor.getBlockState(blockpos1);
            if (blockstate1.is(blockstate.getBlock())) {
               DoubleBlockCombiner.BlockType doubleblockcombiner_blocktype1 = function.apply(blockstate1);
               if (doubleblockcombiner_blocktype1 != DoubleBlockCombiner.BlockType.SINGLE && doubleblockcombiner_blocktype != doubleblockcombiner_blocktype1 && blockstate1.getValue(directionproperty) == blockstate.getValue(directionproperty)) {
                  if (bipredicate.test(levelaccessor, blockpos1)) {
                     return DoubleBlockCombiner.Combiner::acceptNone;
                  }

                  S blockentity1 = blockentitytype.getBlockEntity(levelaccessor, blockpos1);
                  if (blockentity1 != null) {
                     S blockentity2 = flag1 ? blockentity : blockentity1;
                     S blockentity3 = flag1 ? blockentity1 : blockentity;
                     return new DoubleBlockCombiner.NeighborCombineResult.Double<>(blockentity2, blockentity3);
                  }
               }
            }

            return new DoubleBlockCombiner.NeighborCombineResult.Single<>(blockentity);
         }
      }
   }

   public static enum BlockType {
      SINGLE,
      FIRST,
      SECOND;
   }

   public interface Combiner<S, T> {
      T acceptDouble(S object, S object1);

      T acceptSingle(S object);

      T acceptNone();
   }

   public interface NeighborCombineResult<S> {
      <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> doubleblockcombiner_combiner);

      public static final class Double<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
         private final S first;
         private final S second;

         public Double(S object, S object1) {
            this.first = object;
            this.second = object1;
         }

         public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> doubleblockcombiner_combiner) {
            return doubleblockcombiner_combiner.acceptDouble(this.first, this.second);
         }
      }

      public static final class Single<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
         private final S single;

         public Single(S object) {
            this.single = object;
         }

         public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> doubleblockcombiner_combiner) {
            return doubleblockcombiner_combiner.acceptSingle(this.single);
         }
      }
   }
}
