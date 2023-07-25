package net.minecraft.world.item.enchantment;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class ProtectionEnchantment extends Enchantment {
   public final ProtectionEnchantment.Type type;

   public ProtectionEnchantment(Enchantment.Rarity enchantment_rarity, ProtectionEnchantment.Type protectionenchantment_type, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, protectionenchantment_type == ProtectionEnchantment.Type.FALL ? EnchantmentCategory.ARMOR_FEET : EnchantmentCategory.ARMOR, aequipmentslot);
      this.type = protectionenchantment_type;
   }

   public int getMinCost(int i) {
      return this.type.getMinCost() + (i - 1) * this.type.getLevelCost();
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + this.type.getLevelCost();
   }

   public int getMaxLevel() {
      return 4;
   }

   public int getDamageProtection(int i, DamageSource damagesource) {
      if (damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         return 0;
      } else if (this.type == ProtectionEnchantment.Type.ALL) {
         return i;
      } else if (this.type == ProtectionEnchantment.Type.FIRE && damagesource.is(DamageTypeTags.IS_FIRE)) {
         return i * 2;
      } else if (this.type == ProtectionEnchantment.Type.FALL && damagesource.is(DamageTypeTags.IS_FALL)) {
         return i * 3;
      } else if (this.type == ProtectionEnchantment.Type.EXPLOSION && damagesource.is(DamageTypeTags.IS_EXPLOSION)) {
         return i * 2;
      } else {
         return this.type == ProtectionEnchantment.Type.PROJECTILE && damagesource.is(DamageTypeTags.IS_PROJECTILE) ? i * 2 : 0;
      }
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      if (enchantment instanceof ProtectionEnchantment protectionenchantment) {
         if (this.type == protectionenchantment.type) {
            return false;
         } else {
            return this.type == ProtectionEnchantment.Type.FALL || protectionenchantment.type == ProtectionEnchantment.Type.FALL;
         }
      } else {
         return super.checkCompatibility(enchantment);
      }
   }

   public static int getFireAfterDampener(LivingEntity livingentity, int i) {
      int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_PROTECTION, livingentity);
      if (j > 0) {
         i -= Mth.floor((float)i * (float)j * 0.15F);
      }

      return i;
   }

   public static double getExplosionKnockbackAfterDampener(LivingEntity livingentity, double d0) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, livingentity);
      if (i > 0) {
         d0 *= Mth.clamp(1.0D - (double)i * 0.15D, 0.0D, 1.0D);
      }

      return d0;
   }

   public static enum Type {
      ALL(1, 11),
      FIRE(10, 8),
      FALL(5, 6),
      EXPLOSION(5, 8),
      PROJECTILE(3, 6);

      private final int minCost;
      private final int levelCost;

      private Type(int i, int j) {
         this.minCost = i;
         this.levelCost = j;
      }

      public int getMinCost() {
         return this.minCost;
      }

      public int getLevelCost() {
         return this.levelCost;
      }
   }
}
