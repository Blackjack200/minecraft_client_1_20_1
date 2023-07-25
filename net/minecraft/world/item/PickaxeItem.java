package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
   protected PickaxeItem(Tier tier, int i, float f, Item.Properties item_properties) {
      super((float)i, f, tier, BlockTags.MINEABLE_WITH_PICKAXE, item_properties);
   }
}
