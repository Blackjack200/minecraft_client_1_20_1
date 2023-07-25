package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class LoomBlock extends HorizontalDirectionalBlock {
   private static final Component CONTAINER_TITLE = Component.translatable("container.loom");

   protected LoomBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         player.openMenu(blockstate.getMenuProvider(level, blockpos));
         player.awardStat(Stats.INTERACT_WITH_LOOM);
         return InteractionResult.CONSUME;
      }
   }

   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      return new SimpleMenuProvider((i, inventory, player) -> new LoomMenu(i, inventory, ContainerLevelAccess.create(level, blockpos)), CONTAINER_TITLE);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState().setValue(FACING, blockplacecontext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING);
   }
}
