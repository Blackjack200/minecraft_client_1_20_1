package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.phys.BlockHitResult;

public class StructureBlock extends BaseEntityBlock implements GameMasterBlock {
   public static final EnumProperty<StructureMode> MODE = BlockStateProperties.STRUCTUREBLOCK_MODE;

   protected StructureBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(MODE, StructureMode.LOAD));
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new StructureBlockEntity(blockpos, blockstate);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof StructureBlockEntity) {
         return ((StructureBlockEntity)blockentity).usedBy(player) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      if (!level.isClientSide) {
         if (livingentity != null) {
            BlockEntity blockentity = level.getBlockEntity(blockpos);
            if (blockentity instanceof StructureBlockEntity) {
               ((StructureBlockEntity)blockentity).createdBy(livingentity);
            }
         }

      }
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.MODEL;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(MODE);
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (level instanceof ServerLevel) {
         BlockEntity blockentity = level.getBlockEntity(blockpos);
         if (blockentity instanceof StructureBlockEntity) {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)blockentity;
            boolean flag1 = level.hasNeighborSignal(blockpos);
            boolean flag2 = structureblockentity.isPowered();
            if (flag1 && !flag2) {
               structureblockentity.setPowered(true);
               this.trigger((ServerLevel)level, structureblockentity);
            } else if (!flag1 && flag2) {
               structureblockentity.setPowered(false);
            }

         }
      }
   }

   private void trigger(ServerLevel serverlevel, StructureBlockEntity structureblockentity) {
      switch (structureblockentity.getMode()) {
         case SAVE:
            structureblockentity.saveStructure(false);
            break;
         case LOAD:
            structureblockentity.loadStructure(serverlevel, false);
            break;
         case CORNER:
            structureblockentity.unloadStructure();
         case DATA:
      }

   }
}
