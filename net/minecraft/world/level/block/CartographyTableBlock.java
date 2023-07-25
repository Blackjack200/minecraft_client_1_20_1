package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CartographyTableBlock extends Block {
   private static final Component CONTAINER_TITLE = Component.translatable("container.cartography_table");

   protected CartographyTableBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         player.openMenu(blockstate.getMenuProvider(level, blockpos));
         player.awardStat(Stats.INTERACT_WITH_CARTOGRAPHY_TABLE);
         return InteractionResult.CONSUME;
      }
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState blockstate, Level level, BlockPos blockpos) {
      return new SimpleMenuProvider((i, inventory, player) -> new CartographyTableMenu(i, inventory, ContainerLevelAccess.create(level, blockpos)), CONTAINER_TITLE);
   }
}
