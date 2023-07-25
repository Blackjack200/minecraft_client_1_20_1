package net.minecraft.world.item;

public class EnchantedGoldenAppleItem extends Item {
   public EnchantedGoldenAppleItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean isFoil(ItemStack itemstack) {
      return true;
   }
}
