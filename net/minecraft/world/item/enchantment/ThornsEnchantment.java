package net.minecraft.world.item.enchantment;

import java.util.Map;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ThornsEnchantment extends Enchantment {
   private static final float CHANCE_PER_LEVEL = 0.15F;

   public ThornsEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.ARMOR_CHEST, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 10 + 20 * (i - 1);
   }

   public int getMaxCost(int i) {
      return super.getMinCost(i) + 50;
   }

   public int getMaxLevel() {
      return 3;
   }

   public boolean canEnchant(ItemStack itemstack) {
      return itemstack.getItem() instanceof ArmorItem ? true : super.canEnchant(itemstack);
   }

   public void doPostHurt(LivingEntity livingentity, Entity entity, int i) {
      RandomSource randomsource = livingentity.getRandom();
      Map.Entry<EquipmentSlot, ItemStack> map_entry = EnchantmentHelper.getRandomItemWith(Enchantments.THORNS, livingentity);
      if (shouldHit(i, randomsource)) {
         if (entity != null) {
            entity.hurt(livingentity.damageSources().thorns(livingentity), (float)getDamage(i, randomsource));
         }

         if (map_entry != null) {
            map_entry.getValue().hurtAndBreak(2, livingentity, (livingentity1) -> livingentity1.broadcastBreakEvent(map_entry.getKey()));
         }
      }

   }

   public static boolean shouldHit(int i, RandomSource randomsource) {
      if (i <= 0) {
         return false;
      } else {
         return randomsource.nextFloat() < 0.15F * (float)i;
      }
   }

   public static int getDamage(int i, RandomSource randomsource) {
      return i > 10 ? i - 10 : 1 + randomsource.nextInt(4);
   }
}
