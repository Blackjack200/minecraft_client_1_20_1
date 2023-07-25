package net.minecraft.world.item;

import net.minecraft.world.level.block.Block;

public class ItemNameBlockItem extends BlockItem {
   public ItemNameBlockItem(Block block, Item.Properties item_properties) {
      super(block, item_properties);
   }

   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }
}
