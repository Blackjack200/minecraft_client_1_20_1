package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShearsItem extends Item {
   public ShearsItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean mineBlock(ItemStack itemstack, Level level, BlockState blockstate, BlockPos blockpos, LivingEntity livingentity) {
      if (!level.isClientSide && !blockstate.is(BlockTags.FIRE)) {
         itemstack.hurtAndBreak(1, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      }

      return !blockstate.is(BlockTags.LEAVES) && !blockstate.is(Blocks.COBWEB) && !blockstate.is(Blocks.GRASS) && !blockstate.is(Blocks.FERN) && !blockstate.is(Blocks.DEAD_BUSH) && !blockstate.is(Blocks.HANGING_ROOTS) && !blockstate.is(Blocks.VINE) && !blockstate.is(Blocks.TRIPWIRE) && !blockstate.is(BlockTags.WOOL) ? super.mineBlock(itemstack, level, blockstate, blockpos, livingentity) : true;
   }

   public boolean isCorrectToolForDrops(BlockState blockstate) {
      return blockstate.is(Blocks.COBWEB) || blockstate.is(Blocks.REDSTONE_WIRE) || blockstate.is(Blocks.TRIPWIRE);
   }

   public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
      if (!blockstate.is(Blocks.COBWEB) && !blockstate.is(BlockTags.LEAVES)) {
         if (blockstate.is(BlockTags.WOOL)) {
            return 5.0F;
         } else {
            return !blockstate.is(Blocks.VINE) && !blockstate.is(Blocks.GLOW_LICHEN) ? super.getDestroySpeed(itemstack, blockstate) : 2.0F;
         }
      } else {
         return 15.0F;
      }
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      if (block instanceof GrowingPlantHeadBlock growingplantheadblock) {
         if (!growingplantheadblock.isMaxAge(blockstate)) {
            Player player = useoncontext.getPlayer();
            ItemStack itemstack = useoncontext.getItemInHand();
            if (player instanceof ServerPlayer) {
               CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
            }

            level.playSound(player, blockpos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockState blockstate1 = growingplantheadblock.getMaxAgeState(blockstate);
            level.setBlockAndUpdate(blockpos, blockstate1);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(useoncontext.getPlayer(), blockstate1));
            if (player != null) {
               itemstack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(useoncontext.getHand()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }

      return super.useOn(useoncontext);
   }
}
