package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty CAN_SUMMON = BlockStateProperties.CAN_SUMMON;
   protected static final VoxelShape COLLIDER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final double TOP_Y = COLLIDER.max(Direction.Axis.Y);

   public SculkShriekerBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHRIEKING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(CAN_SUMMON, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(SHRIEKING);
      statedefinition_builder.add(WATERLOGGED);
      statedefinition_builder.add(CAN_SUMMON);
   }

   public void stepOn(Level level, BlockPos blockpos, BlockState blockstate, Entity entity) {
      if (level instanceof ServerLevel serverlevel) {
         ServerPlayer serverplayer = SculkShriekerBlockEntity.tryGetPlayer(entity);
         if (serverplayer != null) {
            serverlevel.getBlockEntity(blockpos, BlockEntityType.SCULK_SHRIEKER).ifPresent((sculkshriekerblockentity) -> sculkshriekerblockentity.tryShriek(serverlevel, serverplayer));
         }
      }

      super.stepOn(level, blockpos, blockstate, entity);
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (level instanceof ServerLevel serverlevel) {
         if (blockstate.getValue(SHRIEKING) && !blockstate.is(blockstate1.getBlock())) {
            serverlevel.getBlockEntity(blockpos, BlockEntityType.SCULK_SHRIEKER).ifPresent((sculkshriekerblockentity) -> sculkshriekerblockentity.tryRespond(serverlevel));
         }
      }

      super.onRemove(blockstate, level, blockpos, blockstate1, flag);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(SHRIEKING)) {
         serverlevel.setBlock(blockpos, blockstate.setValue(SHRIEKING, Boolean.valueOf(false)), 3);
         serverlevel.getBlockEntity(blockpos, BlockEntityType.SCULK_SHRIEKER).ifPresent((sculkshriekerblockentity) -> sculkshriekerblockentity.tryRespond(serverlevel));
      }

   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return COLLIDER;
   }

   public VoxelShape getOcclusionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return COLLIDER;
   }

   public boolean useShapeForLightOcclusion(BlockState blockstate) {
      return true;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SculkShriekerBlockEntity(blockpos, blockstate);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos()).getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public void spawnAfterBreak(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, ItemStack itemstack, boolean flag) {
      super.spawnAfterBreak(blockstate, serverlevel, blockpos, itemstack, flag);
      if (flag) {
         this.tryDropExperience(serverlevel, blockpos, itemstack, ConstantInt.of(5));
      }

   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return !level.isClientSide ? BaseEntityBlock.createTickerHelper(blockentitytype, BlockEntityType.SCULK_SHRIEKER, (level1, blockpos, blockstate1, sculkshriekerblockentity) -> VibrationSystem.Ticker.tick(level1, sculkshriekerblockentity.getVibrationData(), sculkshriekerblockentity.getVibrationUser())) : null;
   }
}
