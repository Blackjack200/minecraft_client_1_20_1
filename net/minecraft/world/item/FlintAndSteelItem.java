package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;

public class FlintAndSteelItem extends Item {
   public FlintAndSteelItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Player player = useoncontext.getPlayer();
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (!CampfireBlock.canLight(blockstate) && !CandleBlock.canLight(blockstate) && !CandleCakeBlock.canLight(blockstate)) {
         BlockPos blockpos1 = blockpos.relative(useoncontext.getClickedFace());
         if (BaseFireBlock.canBePlacedAt(level, blockpos1, useoncontext.getHorizontalDirection())) {
            level.playSound(player, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            BlockState blockstate1 = BaseFireBlock.getState(level, blockpos1);
            level.setBlock(blockpos1, blockstate1, 11);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
            ItemStack itemstack = useoncontext.getItemInHand();
            if (player instanceof ServerPlayer) {
               CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos1, itemstack);
               itemstack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(useoncontext.getHand()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
         } else {
            return InteractionResult.FAIL;
         }
      } else {
         level.playSound(player, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
         level.setBlock(blockpos, blockstate.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
         level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
         if (player != null) {
            useoncontext.getItemInHand().hurtAndBreak(1, player, (player2) -> player2.broadcastBreakEvent(useoncontext.getHand()));
         }

         return InteractionResult.sidedSuccess(level.isClientSide());
      }
   }
}
