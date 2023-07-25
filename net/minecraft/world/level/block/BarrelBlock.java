package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BarrelBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.FACING;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

   public BarrelBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof BarrelBlockEntity) {
            player.openMenu((BarrelBlockEntity)blockentity);
            player.awardStat(Stats.OPEN_BARREL);
            PiglinAi.angerNearbyPiglins(player, true);
         }

         return InteractionResult.CONSUME;
      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate.is(blockstate1.getBlock())) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof Container) {
            Containers.dropContents(level, blockpos, (Container)blockentity);
            level.updateNeighbourForOutputSignal(blockpos, this);
         }

         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
      if (blockentity instanceof BarrelBlockEntity) {
         ((BarrelBlockEntity)blockentity).recheckOpen();
      }

   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new BarrelBlockEntity(blockpos, blockstate);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      if (itemstack.hasCustomHoverName()) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof BarrelBlockEntity) {
            ((BarrelBlockEntity)blockentity).setCustomName(itemstack.getHoverName());
         }
      }

   }

   public boolean hasAnalogOutputSignal(BlockState blockstate) {
      return true;
   }

   public int getAnalogOutputSignal(BlockState blockstate, Level level, BlockPos blockpos) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockpos));
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return blockstate.setValue(FACING, rotation.rotate(blockstate.getValue(FACING)));
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return blockstate.rotate(mirror.getRotation(blockstate.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, OPEN);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getNearestLookingDirection().getOpposite());
   }
}
