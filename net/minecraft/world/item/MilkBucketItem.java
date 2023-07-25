package net.minecraft.world.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MilkBucketItem extends Item {
   private static final int DRINK_DURATION = 32;

   public MilkBucketItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public ItemStack finishUsingItem(ItemStack itemstack, Level level, LivingEntity livingentity) {
      if (livingentity instanceof ServerPlayer serverplayer) {
         CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, itemstack);
         serverplayer.awardStat(Stats.ITEM_USED.get(this));
      }

      if (livingentity instanceof Player && !((Player)livingentity).getAbilities().instabuild) {
         itemstack.shrink(1);
      }

      if (!level.isClientSide) {
         livingentity.removeAllEffects();
      }

      return itemstack.isEmpty() ? new ItemStack(Items.BUCKET) : itemstack;
   }

   public int getUseDuration(ItemStack itemstack) {
      return 32;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.DRINK;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      return ItemUtils.startUsingInstantly(level, player, interactionhand);
   }
}
