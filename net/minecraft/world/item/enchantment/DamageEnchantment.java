package net.minecraft.world.item.enchantment;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

public class DamageEnchantment extends Enchantment {
   public static final int ALL = 0;
   public static final int UNDEAD = 1;
   public static final int ARTHROPODS = 2;
   private static final String[] NAMES = new String[]{"all", "undead", "arthropods"};
   private static final int[] MIN_COST = new int[]{1, 5, 5};
   private static final int[] LEVEL_COST = new int[]{11, 8, 8};
   private static final int[] LEVEL_COST_SPAN = new int[]{20, 20, 20};
   public final int type;

   public DamageEnchantment(Enchantment.Rarity enchantment_rarity, int i, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.WEAPON, aequipmentslot);
      this.type = i;
   }

   public int getMinCost(int i) {
      return MIN_COST[this.type] + (i - 1) * LEVEL_COST[this.type];
   }

   public int getMaxCost(int i) {
      return this.getMinCost(i) + LEVEL_COST_SPAN[this.type];
   }

   public int getMaxLevel() {
      return 5;
   }

   public float getDamageBonus(int i, MobType mobtype) {
      if (this.type == 0) {
         return 1.0F + (float)Math.max(0, i - 1) * 0.5F;
      } else if (this.type == 1 && mobtype == MobType.UNDEAD) {
         return (float)i * 2.5F;
      } else {
         return this.type == 2 && mobtype == MobType.ARTHROPOD ? (float)i * 2.5F : 0.0F;
      }
   }

   public boolean checkCompatibility(Enchantment enchantment) {
      return !(enchantment instanceof DamageEnchantment);
   }

   public boolean canEnchant(ItemStack itemstack) {
      return itemstack.getItem() instanceof AxeItem ? true : super.canEnchant(itemstack);
   }

   public void doPostAttack(LivingEntity livingentity, Entity entity, int i) {
      if (entity instanceof LivingEntity livingentity1) {
         if (this.type == 2 && i > 0 && livingentity1.getMobType() == MobType.ARTHROPOD) {
            int j = 20 + livingentity.getRandom().nextInt(10 * i);
            livingentity1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, j, 3));
         }
      }

   }
}
