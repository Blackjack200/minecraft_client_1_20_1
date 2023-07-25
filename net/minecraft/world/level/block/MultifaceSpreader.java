package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class MultifaceSpreader {
   public static final MultifaceSpreader.SpreadType[] DEFAULT_SPREAD_ORDER = new MultifaceSpreader.SpreadType[]{MultifaceSpreader.SpreadType.SAME_POSITION, MultifaceSpreader.SpreadType.SAME_PLANE, MultifaceSpreader.SpreadType.WRAP_AROUND};
   private final MultifaceSpreader.SpreadConfig config;

   public MultifaceSpreader(MultifaceBlock multifaceblock) {
      this(new MultifaceSpreader.DefaultSpreaderConfig(multifaceblock));
   }

   public MultifaceSpreader(MultifaceSpreader.SpreadConfig multifacespreader_spreadconfig) {
      this.config = multifacespreader_spreadconfig;
   }

   public boolean canSpreadInAnyDirection(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return Direction.stream().anyMatch((direction2) -> this.getSpreadFromFaceTowardDirection(blockstate, blockgetter, blockpos, direction, direction2, this.config::canSpreadInto).isPresent());
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadFromRandomFaceTowardRandomDirection(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource) {
      return Direction.allShuffled(randomsource).stream().filter((direction1) -> this.config.canSpreadFrom(blockstate, direction1)).map((direction) -> this.spreadFromFaceTowardRandomDirection(blockstate, levelaccessor, blockpos, direction, randomsource, false)).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
   }

   public long spreadAll(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, boolean flag) {
      return Direction.stream().filter((direction1) -> this.config.canSpreadFrom(blockstate, direction1)).map((direction) -> this.spreadFromFaceTowardAllDirections(blockstate, levelaccessor, blockpos, direction, flag)).reduce(0L, Long::sum);
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardRandomDirection(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, Direction direction, RandomSource randomsource, boolean flag) {
      return Direction.allShuffled(randomsource).stream().map((direction2) -> this.spreadFromFaceTowardDirection(blockstate, levelaccessor, blockpos, direction, direction2, flag)).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
   }

   private long spreadFromFaceTowardAllDirections(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, Direction direction, boolean flag) {
      return Direction.stream().map((direction2) -> this.spreadFromFaceTowardDirection(blockstate, levelaccessor, blockpos, direction, direction2, flag)).filter(Optional::isPresent).count();
   }

   @VisibleForTesting
   public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardDirection(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, Direction direction, Direction direction1, boolean flag) {
      return this.getSpreadFromFaceTowardDirection(blockstate, levelaccessor, blockpos, direction, direction1, this.config::canSpreadInto).flatMap((multifacespreader_spreadpos) -> this.spreadToFace(levelaccessor, multifacespreader_spreadpos, flag));
   }

   public Optional<MultifaceSpreader.SpreadPos> getSpreadFromFaceTowardDirection(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction, Direction direction1, MultifaceSpreader.SpreadPredicate multifacespreader_spreadpredicate) {
      if (direction1.getAxis() == direction.getAxis()) {
         return Optional.empty();
      } else if (this.config.isOtherBlockValidAsSource(blockstate) || this.config.hasFace(blockstate, direction) && !this.config.hasFace(blockstate, direction1)) {
         for(MultifaceSpreader.SpreadType multifacespreader_spreadtype : this.config.getSpreadTypes()) {
            MultifaceSpreader.SpreadPos multifacespreader_spreadpos = multifacespreader_spreadtype.getSpreadPos(blockpos, direction1, direction);
            if (multifacespreader_spreadpredicate.test(blockgetter, blockpos, multifacespreader_spreadpos)) {
               return Optional.of(multifacespreader_spreadpos);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadToFace(LevelAccessor levelaccessor, MultifaceSpreader.SpreadPos multifacespreader_spreadpos, boolean flag) {
      BlockState blockstate = levelaccessor.getBlockState(multifacespreader_spreadpos.pos());
      return this.config.placeBlock(levelaccessor, multifacespreader_spreadpos, blockstate, flag) ? Optional.of(multifacespreader_spreadpos) : Optional.empty();
   }

   public static class DefaultSpreaderConfig implements MultifaceSpreader.SpreadConfig {
      protected MultifaceBlock block;

      public DefaultSpreaderConfig(MultifaceBlock multifaceblock) {
         this.block = multifaceblock;
      }

      @Nullable
      public BlockState getStateForPlacement(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
         return this.block.getStateForPlacement(blockstate, blockgetter, blockpos, direction);
      }

      protected boolean stateCanBeReplaced(BlockGetter blockgetter, BlockPos blockpos, BlockPos blockpos1, Direction direction, BlockState blockstate) {
         return blockstate.isAir() || blockstate.is(this.block) || blockstate.is(Blocks.WATER) && blockstate.getFluidState().isSource();
      }

      public boolean canSpreadInto(BlockGetter blockgetter, BlockPos blockpos, MultifaceSpreader.SpreadPos multifacespreader_spreadpos) {
         BlockState blockstate = blockgetter.getBlockState(multifacespreader_spreadpos.pos());
         return this.stateCanBeReplaced(blockgetter, blockpos, multifacespreader_spreadpos.pos(), multifacespreader_spreadpos.face(), blockstate) && this.block.isValidStateForPlacement(blockgetter, blockstate, multifacespreader_spreadpos.pos(), multifacespreader_spreadpos.face());
      }
   }

   public interface SpreadConfig {
      @Nullable
      BlockState getStateForPlacement(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction);

      boolean canSpreadInto(BlockGetter blockgetter, BlockPos blockpos, MultifaceSpreader.SpreadPos multifacespreader_spreadpos);

      default MultifaceSpreader.SpreadType[] getSpreadTypes() {
         return MultifaceSpreader.DEFAULT_SPREAD_ORDER;
      }

      default boolean hasFace(BlockState blockstate, Direction direction) {
         return MultifaceBlock.hasFace(blockstate, direction);
      }

      default boolean isOtherBlockValidAsSource(BlockState blockstate) {
         return false;
      }

      default boolean canSpreadFrom(BlockState blockstate, Direction direction) {
         return this.isOtherBlockValidAsSource(blockstate) || this.hasFace(blockstate, direction);
      }

      default boolean placeBlock(LevelAccessor levelaccessor, MultifaceSpreader.SpreadPos multifacespreader_spreadpos, BlockState blockstate, boolean flag) {
         BlockState blockstate1 = this.getStateForPlacement(blockstate, levelaccessor, multifacespreader_spreadpos.pos(), multifacespreader_spreadpos.face());
         if (blockstate1 != null) {
            if (flag) {
               levelaccessor.getChunk(multifacespreader_spreadpos.pos()).markPosForPostprocessing(multifacespreader_spreadpos.pos());
            }

            return levelaccessor.setBlock(multifacespreader_spreadpos.pos(), blockstate1, 2);
         } else {
            return false;
         }
      }
   }

   public static record SpreadPos(BlockPos pos, Direction face) {
   }

   @FunctionalInterface
   public interface SpreadPredicate {
      boolean test(BlockGetter blockgetter, BlockPos blockpos, MultifaceSpreader.SpreadPos multifacespreader_spreadpos);
   }

   public static enum SpreadType {
      SAME_POSITION {
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockpos, Direction direction, Direction direction1) {
            return new MultifaceSpreader.SpreadPos(blockpos, direction);
         }
      },
      SAME_PLANE {
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockpos, Direction direction, Direction direction1) {
            return new MultifaceSpreader.SpreadPos(blockpos.relative(direction), direction1);
         }
      },
      WRAP_AROUND {
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockpos, Direction direction, Direction direction1) {
            return new MultifaceSpreader.SpreadPos(blockpos.relative(direction).relative(direction1), direction.getOpposite());
         }
      };

      public abstract MultifaceSpreader.SpreadPos getSpreadPos(BlockPos blockpos, Direction direction, Direction direction1);
   }
}
