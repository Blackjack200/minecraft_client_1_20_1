package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

public class DiscFragmentItem extends Item {
   public DiscFragmentItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
   }

   public MutableComponent getDisplayName() {
      return Component.translatable(this.getDescriptionId() + ".desc");
   }
}
