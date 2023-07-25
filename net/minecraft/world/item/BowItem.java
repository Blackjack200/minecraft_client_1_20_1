package net.minecraft.world.item;

import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class BowItem extends ProjectileWeaponItem implements Vanishable {
   public static final int MAX_DRAW_DURATION = 20;
   public static final int DEFAULT_RANGE = 15;

   public BowItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public void releaseUsing(ItemStack itemstack, Level level, LivingEntity livingentity, int i) {
      if (livingentity instanceof Player player) {
         boolean flag = player.getAbilities().instabuild || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, itemstack) > 0;
         ItemStack itemstack1 = player.getProjectile(itemstack);
         if (!itemstack1.isEmpty() || flag) {
            if (itemstack1.isEmpty()) {
               itemstack1 = new ItemStack(Items.ARROW);
            }

            int j = this.getUseDuration(itemstack) - i;
            float f = getPowerForTime(j);
            if (!((double)f < 0.1D)) {
               boolean flag1 = flag && itemstack1.is(Items.ARROW);
               if (!level.isClientSide) {
                  ArrowItem arrowitem = (ArrowItem)(itemstack1.getItem() instanceof ArrowItem ? itemstack1.getItem() : Items.ARROW);
                  AbstractArrow abstractarrow = arrowitem.createArrow(level, itemstack1, player);
                  abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, f * 3.0F, 1.0F);
                  if (f == 1.0F) {
                     abstractarrow.setCritArrow(true);
                  }

                  int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, itemstack);
                  if (k > 0) {
                     abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double)k * 0.5D + 0.5D);
                  }

                  int l = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, itemstack);
                  if (l > 0) {
                     abstractarrow.setKnockback(l);
                  }

                  if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, itemstack) > 0) {
                     abstractarrow.setSecondsOnFire(100);
                  }

                  itemstack.hurtAndBreak(1, player, (player2) -> player2.broadcastBreakEvent(player.getUsedItemHand()));
                  if (flag1 || player.getAbilities().instabuild && (itemstack1.is(Items.SPECTRAL_ARROW) || itemstack1.is(Items.TIPPED_ARROW))) {
                     abstractarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                  }

                  level.addFreshEntity(abstractarrow);
               }

               level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
               if (!flag1 && !player.getAbilities().instabuild) {
                  itemstack1.shrink(1);
                  if (itemstack1.isEmpty()) {
                     player.getInventory().removeItem(itemstack1);
                  }
               }

               player.awardStat(Stats.ITEM_USED.get(this));
            }
         }
      }
   }

   public static float getPowerForTime(int i) {
      float f = (float)i / 20.0F;
      f = (f * f + f * 2.0F) / 3.0F;
      if (f > 1.0F) {
         f = 1.0F;
      }

      return f;
   }

   public int getUseDuration(ItemStack itemstack) {
      return 72000;
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.BOW;
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      boolean flag = !player.getProjectile(itemstack).isEmpty();
      if (!player.getAbilities().instabuild && !flag) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         player.startUsingItem(interactionhand);
         return InteractionResultHolder.consume(itemstack);
      }
   }

   public Predicate<ItemStack> getAllSupportedProjectiles() {
      return ARROW_ONLY;
   }

   public int getDefaultProjectileRange() {
      return 15;
   }
}
