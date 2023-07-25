package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final int ACTIVE_TICKS = 30;
   public static final int COOLDOWN_TICKS = 10;
   public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private static final float[] RESONANCE_PITCH_BEND = Util.make(new float[16], (afloat) -> {
      int[] aint = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

      for(int i = 0; i < 16; ++i) {
         afloat[i] = NoteBlock.getPitchFromNote(aint[i]);
      }

   });

   public SculkSensorBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(PHASE, SculkSensorPhase.INACTIVE).setValue(POWER, Integer.valueOf(0)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockPos blockpos = blockplacecontext.getClickedPos();
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockpos);
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (getPhase(blockstate) != SculkSensorPhase.ACTIVE) {
         if (getPhase(blockstate) == SculkSensorPhase.COOLDOWN) {
            serverlevel.setBlock(blockpos, blockstate.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
            if (!blockstate.getValue(WATERLOGGED)) {
               serverlevel.playSound((Player)null, blockpos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, serverlevel.random.nextFloat() * 0.2F + 0.8F);
            }
         }

      } else {
         deactivate(serverlevel, blockpos, blockstate);
      }
   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
      if (!level.isClientSide() && canActivate(blockstate) && entity.getType() != EntityType.WARDEN) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof SculkSensorBlockEntity) {
            SculkSensorBlockEntity sculksensorblockentity = (SculkSensorBlockEntity)blockentity;
            if (level instanceof ServerLevel) {
               ServerLevel serverlevel = (ServerLevel)level;
               if (sculksensorblockentity.getVibrationUser().canReceiveVibration(serverlevel, blockpos, GameEvent.STEP, GameEvent.Context.of(blockstate))) {
                  sculksensorblockentity.getListener().forceScheduleVibration(serverlevel, GameEvent.STEP, GameEvent.Context.of(entity), entity.position());
               }
            }
         }
      }

      super.stepOn(level, blockpos, blockstate, entity);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!level.isClientSide() && !blockstate.is(blockstate1.getBlock())) {
         if (blockstate.getValue(POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockpos, this)) {
            level.setBlock(blockpos, blockstate.setValue(POWER, Integer.valueOf(0)), 18);
         }

      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         if (getPhase(blockstate) == SculkSensorPhase.ACTIVE) {
            updateNeighbours(level, blockpos, blockstate);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   private static void updateNeighbours(Level level, BlockPos blockpos, BlockState blockstate) {
      Block block = blockstate.getBlock();
      level.updateNeighborsAt(blockpos, block);
      level.updateNeighborsAt(blockpos.below(), block);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SculkSensorBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return !level.isClientSide ? createTickerHelper(blockentitytype, BlockEntityType.SCULK_SENSOR, (level1, blockpos, blockstate1, sculksensorblockentity) -> VibrationSystem.Ticker.tick(level1, sculksensorblockentity.getVibrationData(), sculksensorblockentity.getVibrationUser())) : null;
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(POWER);
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return direction == Direction.UP ? blockstate.getSignal(blockgetter, blockpos, direction) : 0;
   }

   public static SculkSensorPhase getPhase(BlockState blockstate) {
      return blockstate.getValue(PHASE);
   }

   public static boolean canActivate(BlockState blockstate) {
      return getPhase(blockstate) == SculkSensorPhase.INACTIVE;
   }

   public static void deactivate(Level level, BlockPos blockpos, BlockState blockstate) {
      level.setBlock(blockpos, blockstate.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
      level.scheduleTick(blockpos, blockstate.getBlock(), 10);
      updateNeighbours(level, blockpos, blockstate);
   }

   @VisibleForTesting
   public int getActiveTicks() {
      return 30;
   }

   public void activate(@Nullable Entity entity, Level level, BlockPos blockpos, BlockState blockstate, int i, int j) {
      level.setBlock(blockpos, blockstate.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(i)), 3);
      level.scheduleTick(blockpos, blockstate.getBlock(), this.getActiveTicks());
      updateNeighbours(level, blockpos, blockstate);
      tryResonateVibration(entity, level, blockpos, j);
      level.gameEvent(entity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, blockpos);
      if (!blockstate.getValue(WATERLOGGED)) {
         level.playSound((Player)null, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, SoundEvents.SCULK_CLICKING, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.2F + 0.8F);
      }

   }

   public static void tryResonateVibration(@Nullable Entity entity, Level level, BlockPos blockpos, int i) {
      for(Direction direction : Direction.values()) {
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate = level.getBlockState(blockpos1);
         if (blockstate.is(BlockTags.VIBRATION_RESONATORS)) {
            level.gameEvent(VibrationSystem.getResonanceEventByFrequency(i), blockpos1, GameEvent.Context.of(entity, blockstate));
            float f = RESONANCE_PITCH_BEND[i];
            level.playSound((Player)null, blockpos1, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, f);
         }
      }

   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (getPhase(blockstate) == SculkSensorPhase.ACTIVE) {
         Direction direction = Direction.getRandom(randomsource);
         if (direction != Direction.UP && direction != Direction.DOWN) {
            double d0 = (double)blockpos.getX() + 0.5D + (direction.getStepX() == 0 ? 0.5D - randomsource.nextDouble() : (double)direction.getStepX() * 0.6D);
            double d1 = (double)blockpos.getY() + 0.25D;
            double d2 = (double)blockpos.getZ() + 0.5D + (direction.getStepZ() == 0 ? 0.5D - randomsource.nextDouble() : (double)direction.getStepZ() * 0.6D);
            double d3 = (double)randomsource.nextFloat() * 0.04D;
            level.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0D, d3, 0.0D);
         }
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(PHASE, POWER, WATERLOGGED);
   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof SculkSensorBlockEntity sculksensorblockentity) {
         return getPhase(blockstate) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
      } else {
         return 0;
      }
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (flag) {
         this.tryDropExperience(serverlevel, blockpos, itemstack, ConstantInt.of(5));
      }

   }
}
