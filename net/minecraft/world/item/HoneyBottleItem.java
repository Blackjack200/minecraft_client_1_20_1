package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HoneyBottleItem extends Item {
   private static final int DRINK_DURATION = 40;

   public HoneyBottleItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      super.finishUsingItem(itemstack, level, livingentity);
      if (livingentity instanceof ServerPlayer serverplayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, itemstack);
         serverplayer.awardStat(Stats.ITEM_USED.get(this));
      }

      if (!level.isClientSide) {
         livingentity.removeEffect(MobEffects.POISON);
      }

      if (itemstack.isEmpty()) {
         return new ItemStack(Items.GLASS_BOTTLE);
      } else {
         if (livingentity instanceof Player && !((Player)livingentity).getAbilities().instabuild) {
            ItemStack itemstack1 = new ItemStack(Items.GLASS_BOTTLE);
            Player player = (Player)livingentity;
            if (!player.getInventory().add(itemstack1)) {
               player.drop(itemstack1, false);
            }
         }

         return itemstack;
      }
   }

   public int getUseDuration(ItemStack itemstack) {
      return 40;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.DRINK;
   }

   public SoundEvent getDrinkingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   public SoundEvent getEatingSound() {
      return SoundEvents.HONEY_DRINK;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      return ItemUtils.startUsingInstantly(level, player, interactionhand);
   }
}
