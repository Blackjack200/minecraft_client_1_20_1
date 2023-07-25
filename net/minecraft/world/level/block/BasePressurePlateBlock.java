package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BasePressurePlateBlock extends Block {
   protected static final VoxelShape PRESSED_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 0.5D, 15.0D);
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
   protected static final AABB TOUCH_AABB = new AABB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.25D, 0.9375D);
   private final BlockSetType type;

   protected BasePressurePlateBlock(BlockBehaviour.Properties blockbehaviour_properties, BlockSetType blocksettype) {
      super(blockbehaviour_properties.sound(blocksettype.soundType()));
      this.type = blocksettype;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.getSignalForState(blockstate) > 0 ? PRESSED_AABB : AABB;
   }

   protected int getPressedTime() {
      return 20;
   }

   public boolean isPossibleToRespawnInThis(BlockState blockstate) {
      return true;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return direction == Direction.DOWN && !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      return canSupportRigidBlock(levelreader, blockpos1) || canSupportCenter(levelreader, blockpos1, Direction.UP);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      int i = this.getSignalForState(blockstate);
      if (i > 0) {
         this.checkPressed((Entity)null, serverlevel, blockpos, blockstate, i);
      }

   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide) {
         int i = this.getSignalForState(blockstate);
         if (i == 0) {
            this.checkPressed(entity, level, blockpos, blockstate, i);
         }

      }
   }

   private void checkPressed(@Nullable Entity entity, Level level, BlockPos blockpos, BlockState blockstate, int i) {
      int j = this.getSignalStrength(level, blockpos);
      boolean flag = i > 0;
      boolean flag1 = j > 0;
      if (i != j) {
         BlockState blockstate1 = this.setSignalForState(blockstate, j);
         level.setBlock(blockpos, blockstate1, 2);
         this.updateNeighbours(level, blockpos);
         level.setBlocksDirty(blockpos, blockstate, blockstate1);
      }

      if (!flag1 && flag) {
         level.playSound((Player)null, blockpos, this.type.pressurePlateClickOff(), SoundSource.BLOCKS);
         level.gameEvent(entity, GameEvent.BLOCK_DEACTIVATE, blockpos);
      } else if (flag1 && !flag) {
         level.playSound((Player)null, blockpos, this.type.pressurePlateClickOn(), SoundSource.BLOCKS);
         level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, blockpos);
      }

      if (flag1) {
         level.scheduleTick(new BlockPos(blockpos), this, this.getPressedTime());
      }

   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag && !blockstate.is(blockstate1.getBlock())) {
         if (this.getSignalForState(blockstate) > 0) {
            this.updateNeighbours(level, blockpos);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   protected void updateNeighbours(Level level, BlockPos blockpos) {
      level.updateNeighborsAt(blockpos, this);
      level.updateNeighborsAt(blockpos.below(), this);
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return this.getSignalForState(blockstate);
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return direction == Direction.UP ? this.getSignalForState(blockstate) : 0;
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   protected static int getEntityCount(Level level, AABB aabb, Class<? extends Entity> oclass) {
      return level.getEntitiesOfClass(oclass, aabb, EntitySelector.NO_SPECTATORS.and((entity) -> !entity.isIgnoringBlockTriggers())).size();
   }

   protected abstract int getSignalStrength(Level level, BlockPos blockpos);

   protected abstract int getSignalForState(BlockState blockstate);

   protected abstract BlockState setSignalForState(BlockState blockstate, int i);
}
