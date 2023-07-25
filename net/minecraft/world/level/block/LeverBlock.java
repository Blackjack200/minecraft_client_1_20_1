package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeverBlock extends FaceAttachedHorizontalDirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   protected static final int DEPTH = 6;
   protected static final int WIDTH = 6;
   protected static final int HEIGHT = 8;
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 4.0D, 10.0D, 11.0D, 12.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 4.0D, 0.0D, 11.0D, 12.0D, 6.0D);
   protected static final VoxelShape WEST_AABB = Block.box(10.0D, 4.0D, 5.0D, 16.0D, 12.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 4.0D, 5.0D, 6.0D, 12.0D, 11.0D);
   protected static final VoxelShape UP_AABB_Z = Block.box(5.0D, 0.0D, 4.0D, 11.0D, 6.0D, 12.0D);
   protected static final VoxelShape UP_AABB_X = Block.box(4.0D, 0.0D, 5.0D, 12.0D, 6.0D, 11.0D);
   protected static final VoxelShape DOWN_AABB_Z = Block.box(5.0D, 10.0D, 4.0D, 11.0D, 16.0D, 12.0D);
   protected static final VoxelShape DOWN_AABB_X = Block.box(4.0D, 10.0D, 5.0D, 12.0D, 16.0D, 11.0D);

   protected LeverBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      switch ((AttachFace)blockstate.getValue(FACE)) {
         case FLOOR:
            switch (blockstate.getValue(FACING).getAxis()) {
               case X:
                  return UP_AABB_X;
               case Z:
               default:
                  return UP_AABB_Z;
            }
         case WALL:
            switch ((Direction)blockstate.getValue(FACING)) {
               case EAST:
                  return EAST_AABB;
               case WEST:
                  return WEST_AABB;
               case SOUTH:
                  return SOUTH_AABB;
               case NORTH:
               default:
                  return NORTH_AABB;
            }
         case CEILING:
         default:
            switch (blockstate.getValue(FACING).getAxis()) {
               case X:
                  return DOWN_AABB_X;
               case Z:
               default:
                  return DOWN_AABB_Z;
            }
      }
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         BlockState blockstate1 = blockstate.cycle(POWERED);
         if (blockstate1.getValue(POWERED)) {
            makeParticle(blockstate1, level, blockpos, 1.0F);
         }

         return InteractionResult.SUCCESS;
      } else {
         BlockState blockstate2 = this.pull(blockstate, level, blockpos);
         float f = blockstate2.getValue(POWERED) ? 0.6F : 0.5F;
         level.playSound((Player)null, blockpos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
         level.gameEvent(player, blockstate2.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, blockpos);
         return InteractionResult.CONSUME;
      }
   }

   public BlockState pull(BlockState blockstate, Level level, BlockPos blockpos) {
      blockstate = blockstate.cycle(POWERED);
      level.setBlock(blockpos, blockstate, 3);
      this.updateNeighbours(blockstate, level, blockpos);
      return blockstate;
   }

   private static void makeParticle(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, float f) {
      Direction direction = blockstate.getValue(FACING).getOpposite();
      Direction direction1 = getConnectedDirection(blockstate).getOpposite();
      double d0 = (double)blockpos.getX() + 0.5D + 0.1D * (double)direction.getStepX() + 0.2D * (double)direction1.getStepX();
      double d1 = (double)blockpos.getY() + 0.5D + 0.1D * (double)direction.getStepY() + 0.2D * (double)direction1.getStepY();
      double d2 = (double)blockpos.getZ() + 0.5D + 0.1D * (double)direction.getStepZ() + 0.2D * (double)direction1.getStepZ();
      levelaccessor.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, f), d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(POWERED) && randomsource.nextFloat() < 0.25F) {
         makeParticle(blockstate, level, blockpos, 0.5F);
      }

   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag && !blockstate.is(blockstate1.getBlock())) {
         if (blockstate.getValue(POWERED)) {
            this.updateNeighbours(blockstate, level, blockpos);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) ? 15 : 0;
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWERED) && getConnectedDirection(blockstate) == direction ? 15 : 0;
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   private void updateNeighbours(BlockState blockstate, Level level, BlockPos blockpos) {
      level.updateNeighborsAt(blockpos, this);
      level.updateNeighborsAt(blockpos.relative(getConnectedDirection(blockstate).getOpposite()), this);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACE, FACING, POWERED);
   }
}
