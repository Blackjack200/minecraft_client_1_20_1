package net.minecraft.world.level.block;

import java.util.OptionalInt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeavesBlock extends Block implements SimpleWaterloggedBlock {
   public static final int DECAY_DISTANCE = 7;
   public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
   public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final int TICK_DELAY = 1;

   public LeavesBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(PERSISTENT, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public VoxelShape getBlockSupportShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Shapes.empty();
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(DISTANCE) == 7 && !blockstate.getValue(PERSISTENT);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (this.decaying(blockstate)) {
         dropResources(blockstate, serverlevel, blockpos);
         serverlevel.removeBlock(blockpos, false);
      }

   }

   protected boolean decaying(BlockState blockstate) {
      return !blockstate.getValue(PERSISTENT) && blockstate.getValue(DISTANCE) == 7;
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      serverlevel.setBlock(blockpos, updateDistance(blockstate, serverlevel, blockpos), 3);
   }

   public int getLightBlock(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return 1;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      int i = getDistanceAt(blockstate1) + 1;
      if (i != 1 || blockstate.getValue(DISTANCE) != i) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      return blockstate;
   }

   private static BlockState updateDistance(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
      int i = 7;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.values()) {
         blockpos_mutableblockpos.setWithOffset(blockpos, direction);
         i = Math.min(i, getDistanceAt(levelaccessor.getBlockState(blockpos_mutableblockpos)) + 1);
         if (i == 1) {
            break;
         }
      }

      return blockstate.setValue(DISTANCE, Integer.valueOf(i));
   }

   private static int getDistanceAt(BlockState blockstate) {
      return getOptionalDistanceAt(blockstate).orElse(7);
   }

   public static OptionalInt getOptionalDistanceAt(BlockState blockstate) {
      if (blockstate.is(BlockTags.LOGS)) {
         return OptionalInt.of(0);
      } else {
         return blockstate.hasProperty(DISTANCE) ? OptionalInt.of(blockstate.getValue(DISTANCE)) : OptionalInt.empty();
      }
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (level.isRainingAt(blockpos.above())) {
         if (randomsource.nextInt(15) == 1) {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate1 = level.getBlockState(blockpos1);
            if (!blockstate1.canOcclude() || !blockstate1.isFaceSturdy(level, blockpos1, Direction.UP)) {
               ParticleUtils.spawnParticleBelow(level, blockpos, randomsource, ParticleTypes.DRIPPING_WATER);
            }
         }
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(DISTANCE, PERSISTENT, WATERLOGGED);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      BlockState blockstate = this.defaultBlockState().setValue(PERSISTENT, Boolean.valueOf(true)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
      return updateDistance(blockstate, blockplacecontext.getLevel(), blockplacecontext.getClickedPos());
   }
}
