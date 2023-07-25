package net.minecraft.world.item;

public class TieredItem extends Item {
   private final Tier tier;

   public TieredItem(Tier tier, Item.Properties item_properties) {
      super(item_properties.defaultDurability(tier.getUses()));
      this.tier = tier;
   }

   public Tier getTier() {
      return this.tier;
   }

   public int getEnchantmentValue() {
      return this.tier.getEnchantmentValue();
   }

   public boolean isValidRepairItem(ItemStack itemstack, ItemStack itemstack1) {
      return this.tier.getRepairIngredient().test(itemstack1) || super.isValidRepairItem(itemstack, itemstack1);
   }
}
