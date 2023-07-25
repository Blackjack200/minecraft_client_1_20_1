package net.minecraft.world.item.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BindingCurseEnchantment extends Enchantment {
   public BindingCurseEnchantment(Enchantment.Rarity enchantment_rarity, EquipmentSlot... aequipmentslot) {
      super(enchantment_rarity, EnchantmentCategory.WEARABLE, aequipmentslot);
   }

   public int getMinCost(int i) {
      return 25;
   }

   public int getMaxCost(int i) {
      return 50;
   }

   public boolean isTreasureOnly() {
      return true;
   }

   public boolean isCurse() {
      return true;
   }

   public boolean canEnchant(ItemStack itemstack) {
      return !itemstack.is(Items.SHIELD) && super.canEnchant(itemstack);
   }
}
