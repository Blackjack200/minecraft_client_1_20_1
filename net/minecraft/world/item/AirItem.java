package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class AirItem extends Item {
   private final Block block;

   public AirItem(Block block, Item.Properties item_properties) {
      super(item_properties);
      this.block = block;
   }

   public String getDescriptionId() {
      return this.block.getDescriptionId();
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, level, list, tooltipflag);
      this.block.appendHoverText(itemstack, level, list, tooltipflag);
   }
}
