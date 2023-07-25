package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int PRESSED_DEPTH = 1;
   private static final int UNPRESSED_DEPTH = 2;
   protected static final int HALF_AABB_HEIGHT = 2;
   protected static final int HALF_AABB_WIDTH = 3;
   protected static final VoxelShape CEILING_AABB_X = Block.box(6.0D, 14.0D, 5.0D, 10.0D, 16.0D, 11.0D);
   protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0D, 14.0D, 6.0D, 11.0D, 16.0D, 10.0D);
   protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0D, 0.0D, 5.0D, 10.0D, 2.0D, 11.0D);
   protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 2.0D, 10.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 6.0D, 14.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 6.0D, 0.0D, 11.0D, 10.0D, 2.0D);
   protected static final VoxelShape WEST_AABB = Block.box(14.0D, 6.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 6.0D, 5.0D, 2.0D, 10.0D, 11.0D);
   protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0D, 15.0D, 5.0D, 10.0D, 16.0D, 11.0D);
   protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0D, 15.0D, 6.0D, 11.0D, 16.0D, 10.0D);
   protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0D, 0.0D, 5.0D, 10.0D, 1.0D, 11.0D);
   protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 1.0D, 10.0D);
   protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0D, 6.0D, 15.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0D, 6.0D, 0.0D, 11.0D, 10.0D, 1.0D);
   protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0D, 6.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0D, 6.0D, 5.0D, 1.0D, 10.0D, 11.0D);
   private final BlockSetType type;
   private final int ticksToStayPressed;
   private final boolean arrowsCanPress;

   protected ButtonBlock(BlockBehaviour.Properties blockbehaviour_properties, BlockSetType blocksettype, int i, boolean flag) {
      super(blockbehaviour_properties.sound(blocksettype.soundType()));
      this.type = blocksettype;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL));
      this.ticksToStayPressed = i;
      this.arrowsCanPress = flag;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      Direction direction = blockstate.getValue(FACING);
      boolean flag = blockstate.getValue(POWERED);
      switch ((AttachFace)blockstate.getValue(FACE)) {
         case FLOOR:
            if (direction.getAxis() == Direction.Axis.X) {
               return flag ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
            }

            return flag ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
         case WALL:
            VoxelShape var10000;
            switch (direction) {
               case EAST:
                  var10000 = flag ? PRESSED_EAST_AABB : EAST_AABB;
                  break;
               case WEST:
                  var10000 = flag ? PRESSED_WEST_AABB : WEST_AABB;
                  break;
               case SOUTH:
                  var10000 = flag ? PRESSED_SOUTH_AABB : SOUTH_AABB;
                  break;
               case NORTH:
               case UP:
               case DOWN:
                  var10000 = flag ? PRESSED_NORTH_AABB : NORTH_AABB;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            return var10000;
         case CEILING:
         default:
            if (direction.getAxis() == Direction.Axis.X) {
               return flag ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
            } else {
               return flag ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
            }
      }
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (blockstate.getValue(POWERED)) {
         return InteractionResult.CONSUME;
      } else {
         this.press(blockstate, level, blockpos);
         this.playSound(player, level, blockpos, true);
         level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, blockpos);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public void press(BlockState blockstate, Level level, BlockPos blockpos) {
      level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(true)), 3);
      this.updateNeighbours(blockstate, level, blockpos);
      level.scheduleTick(blockpos, this, this.ticksToStayPressed);
   }

   protected void playSound(@Nullable Player player, LevelAccessor levelaccessor, BlockPos blockpos, boolean flag) {
      levelaccessor.playSound(flag ? player : null, blockpos, this.getSound(flag), SoundSource.BLOCKS);
   }

   protected SoundEvent getSound(boolean flag) {
      return flag ? this.type.buttonClickOn() : this.type.buttonClickOff();
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

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(POWERED)) {
         this.checkPressed(blockstate, serverlevel, blockpos);
      }
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide && this.arrowsCanPress && !blockstate.getValue(POWERED)) {
         this.checkPressed(blockstate, level, blockpos);
      }
   }

   protected void checkPressed(BlockState blockstate, Level level, BlockPos blockpos) {
      AbstractArrow abstractarrow = this.arrowsCanPress ? level.getEntitiesOfClass(AbstractArrow.class, blockstate.getShape(level, blockpos).bounds().move(blockpos)).stream().findFirst().orElse((AbstractArrow)null) : null;
      boolean flag = abstractarrow != null;
      boolean flag1 = blockstate.getValue(POWERED);
      if (flag != flag1) {
         level.setBlock(blockpos, blockstate.setValue(POWERED, Boolean.valueOf(flag)), 3);
         this.updateNeighbours(blockstate, level, blockpos);
         this.playSound((Player)null, level, blockpos, flag);
         level.gameEvent(abstractarrow, flag ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, blockpos);
      }

      if (flag) {
         level.scheduleTick(new BlockPos(blockpos), this, this.ticksToStayPressed);
      }

   }

   private void updateNeighbours(BlockState blockstate, Level level, BlockPos blockpos) {
      level.updateNeighborsAt(blockpos, this);
      level.updateNeighborsAt(blockpos.relative(getConnectedDirection(blockstate).getOpposite()), this);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, POWERED, FACE);
   }
}
