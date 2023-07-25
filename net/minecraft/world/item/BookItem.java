package net.minecraft.world.item;

public class BookItem extends Item {
   public BookItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean isEnchantable(ItemStack itemstack) {
      return itemstack.getCount() == 1;
   }

   public int getEnchantmentValue() {
      return 1;
   }
}
