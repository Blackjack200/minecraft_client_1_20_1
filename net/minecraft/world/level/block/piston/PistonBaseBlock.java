package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock extends DirectionalBlock {
   public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
   public static final int TRIGGER_EXTEND = 0;
   public static final int TRIGGER_CONTRACT = 1;
   public static final int TRIGGER_DROP = 2;
   public static final float PLATFORM_THICKNESS = 4.0F;
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape UP_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private final boolean isSticky;

   public PistonBaseBlock(boolean flag, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = flag;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (blockstate.getValue(EXTENDED)) {
         switch ((Direction)blockstate.getValue(FACING)) {
            case DOWN:
               return DOWN_AABB;
            case UP:
            default:
               return UP_AABB;
            case NORTH:
               return NORTH_AABB;
            case SOUTH:
               return SOUTH_AABB;
            case WEST:
               return WEST_AABB;
            case EAST:
               return EAST_AABB;
         }
      } else {
         return Shapes.block();
      }
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
      if (!level.isClientSide) {
         this.checkIfExtend(level, blockpos, blockstate);
      }

   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (!level.isClientSide) {
         this.checkIfExtend(level, blockpos, blockstate);
      }

   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock())) {
         if (!level.isClientSide && level.getBlockEntity(blockpos) == null) {
            this.checkIfExtend(level, blockpos, blockstate);
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
   }

   private void checkIfExtend(Level level, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      boolean flag = this.getNeighborSignal(level, blockpos, direction);
      if (flag && !blockstate.getValue(EXTENDED)) {
         if ((new PistonStructureResolver(level, blockpos, direction, true)).resolve()) {
            level.blockEvent(blockpos, this, 0, direction.get3DDataValue());
         }
      } else if (!flag && blockstate.getValue(EXTENDED)) {
         BlockPos blockpos1 = blockpos.relative(direction, 2);
         BlockState blockstate1 = level.getBlockState(blockpos1);
         int i = 1;
         if (blockstate1.is(Blocks.MOVING_PISTON) && blockstate1.getValue(FACING) == direction) {
            BlockEntity blockentity = level.getBlockEntity(blockpos1);
            if (blockentity instanceof PistonMovingBlockEntity) {
               PistonMovingBlockEntity pistonmovingblockentity = (PistonMovingBlockEntity)blockentity;
               if (pistonmovingblockentity.isExtending() && (pistonmovingblockentity.getProgress(0.0F) < 0.5F || level.getGameTime() == pistonmovingblockentity.getLastTicked() || ((ServerLevel)level).isHandlingTick())) {
                  i = 2;
               }
            }
         }

         level.blockEvent(blockpos, this, i, direction.get3DDataValue());
      }

   }

   private boolean getNeighborSignal(SignalGetter signalgetter, BlockPos blockpos, Direction direction) {
      for(Direction direction1 : Direction.values()) {
         if (direction1 != direction && signalgetter.hasSignal(blockpos.relative(direction1), direction1)) {
            return true;
         }
      }

      if (signalgetter.hasSignal(blockpos, Direction.DOWN)) {
         return true;
      } else {
         BlockPos blockpos1 = blockpos.above();

         for(Direction direction2 : Direction.values()) {
            if (direction2 != Direction.DOWN && signalgetter.hasSignal(blockpos1.relative(direction2), direction2)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean triggerEvent(BlockState blockstate, Level level, BlockPos blockpos, int i, int j) {
      Direction direction = blockstate.getValue(FACING);
      BlockState blockstate1 = blockstate.setValue(EXTENDED, Boolean.valueOf(true));
      if (!level.isClientSide) {
         boolean flag = this.getNeighborSignal(level, blockpos, direction);
         if (flag && (i == 1 || i == 2)) {
            level.setBlock(blockpos, blockstate1, 2);
            return false;
         }

         if (!flag && i == 0) {
            return false;
         }
      }

      if (i == 0) {
         if (!this.moveBlocks(level, blockpos, direction, true)) {
            return false;
         }

         level.setBlock(blockpos, blockstate1, 67);
         level.playSound((Player)null, blockpos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.25F + 0.6F);
         level.gameEvent(GameEvent.BLOCK_ACTIVATE, blockpos, GameEvent.Context.of(blockstate1));
      } else if (i == 1 || i == 2) {
         BlockEntity blockentity = level.getBlockEntity(blockpos.relative(direction));
         if (blockentity instanceof PistonMovingBlockEntity) {
            ((PistonMovingBlockEntity)blockentity).finalTick();
         }

         BlockState blockstate2 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
         level.setBlock(blockpos, blockstate2, 20);
         level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockpos, blockstate2, this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(j & 7)), direction, false, true));
         level.blockUpdated(blockpos, blockstate2.getBlock());
         blockstate2.updateNeighbourShapes(level, blockpos, 2);
         if (this.isSticky) {
            BlockPos blockpos1 = blockpos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
            BlockState blockstate3 = level.getBlockState(blockpos1);
            boolean flag1 = false;
            if (blockstate3.is(Blocks.MOVING_PISTON)) {
               BlockEntity blockentity1 = level.getBlockEntity(blockpos1);
               if (blockentity1 instanceof PistonMovingBlockEntity) {
                  PistonMovingBlockEntity pistonmovingblockentity = (PistonMovingBlockEntity)blockentity1;
                  if (pistonmovingblockentity.getDirection() == direction && pistonmovingblockentity.isExtending()) {
                     pistonmovingblockentity.finalTick();
                     flag1 = true;
                  }
               }
            }

            if (!flag1) {
               if (i != 1 || blockstate3.isAir() || !isPushable(blockstate3, level, blockpos1, direction.getOpposite(), false, direction) || blockstate3.getPistonPushReaction() != PushReaction.NORMAL && !blockstate3.is(Blocks.PISTON) && !blockstate3.is(Blocks.STICKY_PISTON)) {
                  level.removeBlock(blockpos.relative(direction), false);
               } else {
                  this.moveBlocks(level, blockpos, direction, false);
               }
            }
         } else {
            level.removeBlock(blockpos.relative(direction), false);
         }

         level.playSound((Player)null, blockpos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.15F + 0.6F);
         level.gameEvent(GameEvent.BLOCK_DEACTIVATE, blockpos, GameEvent.Context.of(blockstate2));
      }

      return true;
   }

   public static boolean isPushable(BlockState blockstate, Level level, BlockPos blockpos, Direction direction, boolean flag, Direction direction1) {
      if (blockpos.getY() >= level.getMinBuildHeight() && blockpos.getY() <= level.getMaxBuildHeight() - 1 && level.getWorldBorder().isWithinBounds(blockpos)) {
         if (blockstate.isAir()) {
            return true;
         } else if (!blockstate.is(Blocks.OBSIDIAN) && !blockstate.is(Blocks.CRYING_OBSIDIAN) && !blockstate.is(Blocks.RESPAWN_ANCHOR) && !blockstate.is(Blocks.REINFORCED_DEEPSLATE)) {
            if (direction == Direction.DOWN && blockpos.getY() == level.getMinBuildHeight()) {
               return false;
            } else if (direction == Direction.UP && blockpos.getY() == level.getMaxBuildHeight() - 1) {
               return false;
            } else {
               if (!blockstate.is(Blocks.PISTON) && !blockstate.is(Blocks.STICKY_PISTON)) {
                  if (blockstate.getDestroySpeed(level, blockpos) == -1.0F) {
                     return false;
                  }

                  switch (blockstate.getPistonPushReaction()) {
                     case BLOCK:
                        return false;
                     case DESTROY:
                        return flag;
                     case PUSH_ONLY:
                        return direction == direction1;
                  }
               } else if (blockstate.getValue(EXTENDED)) {
                  return false;
               }

               return !blockstate.hasBlockEntity();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean moveBlocks(Level level, BlockPos blockpos, Direction direction, boolean flag) {
      BlockPos blockpos1 = blockpos.relative(direction);
      if (!flag && level.getBlockState(blockpos1).is(Blocks.PISTON_HEAD)) {
         level.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 20);
      }

      PistonStructureResolver pistonstructureresolver = new PistonStructureResolver(level, blockpos, direction, flag);
      if (!pistonstructureresolver.resolve()) {
         return false;
      } else {
         Map<BlockPos, BlockState> map = Maps.newHashMap();
         List<BlockPos> list = pistonstructureresolver.getToPush();
         List<BlockState> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockpos2 = list.get(i);
            BlockState blockstate = level.getBlockState(blockpos2);
            list1.add(blockstate);
            map.put(blockpos2, blockstate);
         }

         List<BlockPos> list2 = pistonstructureresolver.getToDestroy();
         BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
         Direction direction1 = flag ? direction : direction.getOpposite();
         int j = 0;

         for(int k = list2.size() - 1; k >= 0; --k) {
            BlockPos blockpos3 = list2.get(k);
            BlockState blockstate1 = level.getBlockState(blockpos3);
            BlockEntity blockentity = blockstate1.hasBlockEntity() ? level.getBlockEntity(blockpos3) : null;
            dropResources(blockstate1, level, blockpos3, blockentity);
            level.setBlock(blockpos3, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, blockpos3, GameEvent.Context.of(blockstate1));
            if (!blockstate1.is(BlockTags.FIRE)) {
               level.addDestroyBlockEffect(blockpos3, blockstate1);
            }

            ablockstate[j++] = blockstate1;
         }

         for(int l = list.size() - 1; l >= 0; --l) {
            BlockPos blockpos4 = list.get(l);
            BlockState blockstate2 = level.getBlockState(blockpos4);
            blockpos4 = blockpos4.relative(direction1);
            map.remove(blockpos4);
            BlockState blockstate3 = Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction);
            level.setBlock(blockpos4, blockstate3, 68);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockpos4, blockstate3, list1.get(l), direction, flag, false));
            ablockstate[j++] = blockstate2;
         }

         if (flag) {
            PistonType pistontype = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockstate4 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction).setValue(PistonHeadBlock.TYPE, pistontype);
            BlockState blockstate5 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(blockpos1);
            level.setBlock(blockpos1, blockstate5, 68);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockpos1, blockstate5, blockstate4, direction, true, true));
         }

         BlockState blockstate6 = Blocks.AIR.defaultBlockState();

         for(BlockPos blockpos5 : map.keySet()) {
            level.setBlock(blockpos5, blockstate6, 82);
         }

         for(Map.Entry<BlockPos, BlockState> map_entry : map.entrySet()) {
            BlockPos blockpos6 = map_entry.getKey();
            BlockState blockstate7 = map_entry.getValue();
            blockstate7.updateIndirectNeighbourShapes(level, blockpos6, 2);
            blockstate6.updateNeighbourShapes(level, blockpos6, 2);
            blockstate6.updateIndirectNeighbourShapes(level, blockpos6, 2);
         }

         j = 0;

         for(int i1 = list2.size() - 1; i1 >= 0; --i1) {
            BlockState blockstate8 = ablockstate[j++];
            BlockPos blockpos7 = list2.get(i1);
            blockstate8.updateIndirectNeighbourShapes(level, blockpos7, 2);
            level.updateNeighborsAt(blockpos7, blockstate8.getBlock());
         }

         for(int j1 = list.size() - 1; j1 >= 0; --j1) {
            level.updateNeighborsAt(list.get(j1), ablockstate[j++].getBlock());
         }

         if (flag) {
            level.updateNeighborsAt(blockpos1, Blocks.PISTON_HEAD);
         }

         return true;
      }
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, EXTENDED);
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return blockstate.getValue(EXTENDED);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }
}
