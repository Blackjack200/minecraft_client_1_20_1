package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class FishingRodItem extends Item implements Vanishable {
   public FishingRodItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (player.fishing != null) {
         if (!level.isClientSide) {
            int i = player.fishing.retrieve(itemstack);
            itemstack.hurtAndBreak(i, player, (player1) -> player1.broadcastBreakEvent(interactionhand));
         }

         level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
         player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
      } else {
         level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
         if (!level.isClientSide) {
            int j = EnchantmentHelper.getFishingSpeedBonus(itemstack);
            int k = EnchantmentHelper.getFishingLuckBonus(itemstack);
            level.addFreshEntity(new FishingHook(player, level, k, j));
         }

         player.awardStat(Stats.ITEM_USED.get(this));
         player.gameEvent(GameEvent.ITEM_INTERACT_START);
      }

      return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
   }

   public int getEnchantmentValue() {
      return 1;
   }
}
