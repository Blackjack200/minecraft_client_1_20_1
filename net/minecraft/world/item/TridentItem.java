package net.minecraft.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TridentItem extends Item implements Vanishable {
   public static final int THROW_THRESHOLD_TIME = 10;
   public static final float BASE_DAMAGE = 8.0F;
   public static final float SHOOT_POWER = 2.5F;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public TridentItem(Item.Properties item_properties) {
      super(item_properties);
      ImmutableMultimap.Builder<Attribute, AttributeModifier> immutablemultimap_builder = ImmutableMultimap.builder();
      immutablemultimap_builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D, AttributeModifier.Operation.ADDITION));
      immutablemultimap_builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)-2.9F, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = immutablemultimap_builder.build();
   }

   public boolean canAttackBlock(BlockState blockstate, Level level, BlockPos blockpos, Player player) {
      return !player.isCreative();
   }

   public UseAnim getUseAnimation(ItemStack itemstack) {
      return UseAnim.SPEAR;
   }

   public int getUseDuration(ItemStack itemstack) {
      return 72000;
   }

   public void releaseUsing(ItemStack itemstack, Level level, LivingEntity livingentity, int i) {
      if (livingentity instanceof Player player) {
         int j = this.getUseDuration(itemstack) - i;
         if (j >= 10) {
            int k = EnchantmentHelper.getRiptide(itemstack);
            if (k <= 0 || player.isInWaterOrRain()) {
               if (!level.isClientSide) {
                  itemstack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(livingentity.getUsedItemHand()));
                  if (k == 0) {
                     ThrownTrident throwntrident = new ThrownTrident(level, player, itemstack);
                     throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F + (float)k * 0.5F, 1.0F);
                     if (player.getAbilities().instabuild) {
                        throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                     }

                     level.addFreshEntity(throwntrident);
                     level.playSound((Player)null, throwntrident, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
                     if (!player.getAbilities().instabuild) {
                        player.getInventory().removeItem(itemstack);
                     }
                  }
               }

               player.awardStat(Stats.ITEM_USED.get(this));
               if (k > 0) {
                  float f = player.getYRot();
                  float f1 = player.getXRot();
                  float f2 = -Mth.sin(f * ((float)Math.PI / 180F)) * Mth.cos(f1 * ((float)Math.PI / 180F));
                  float f3 = -Mth.sin(f1 * ((float)Math.PI / 180F));
                  float f4 = Mth.cos(f * ((float)Math.PI / 180F)) * Mth.cos(f1 * ((float)Math.PI / 180F));
                  float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
                  float f6 = 3.0F * ((1.0F + (float)k) / 4.0F);
                  f2 *= f6 / f5;
                  f3 *= f6 / f5;
                  f4 *= f6 / f5;
                  player.push((double)f2, (double)f3, (double)f4);
                  player.startAutoSpinAttack(20);
                  if (player.onGround()) {
                     float f7 = 1.1999999F;
                     player.move(MoverType.SELF, new Vec3(0.0D, (double)1.1999999F, 0.0D));
                  }

                  SoundEvent soundevent;
                  if (k >= 3) {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                  } else if (k == 2) {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                  } else {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                  }

                  level.playSound((Player)null, player, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
               }

            }
         }
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
         return InteractionResultHolder.fail(itemstack);
      } else if (EnchantmentHelper.getRiptide(itemstack) > 0 && !player.isInWaterOrRain()) {
         return InteractionResultHolder.fail(itemstack);
      } else {
         player.startUsingItem(interactionhand);
         return InteractionResultHolder.consume(itemstack);
      }
   }

   public boolean hurtEnemy(ItemStack itemstack, LivingEntity livingentity, LivingEntity livingentity1) {
      itemstack.hurtAndBreak(1, livingentity1, (livingentity2) -> livingentity2.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      return true;
   }

   public boolean mineBlock(ItemStack itemstack, Level level, BlockState blockstate, BlockPos blockpos, LivingEntity livingentity) {
      if ((double)blockstate.getDestroySpeed(level, blockpos) != 0.0D) {
         itemstack.hurtAndBreak(2, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(EquipmentSlot.MAINHAND));
      }

      return true;
   }

   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentslot) {
      return equipmentslot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(equipmentslot);
   }

   public int getEnchantmentValue() {
      return 1;
   }
}
