package net.minecraft.world.item;

public class SimpleFoiledItem extends Item {
   public SimpleFoiledItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean isFoil(ItemStack itemstack) {
      return true;
   }
}
