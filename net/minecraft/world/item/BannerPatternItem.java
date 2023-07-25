package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item {
   private final TagKey<BannerPattern> bannerPattern;

   public BannerPatternItem(TagKey<BannerPattern> tagkey, Item.Properties item_properties) {
      super(item_properties);
      this.bannerPattern = tagkey;
   }

   public TagKey<BannerPattern> getBannerPattern() {
      return this.bannerPattern;
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
   }

   public MutableComponent getDisplayName() {
      return Component.translatable(this.getDescriptionId() + ".desc");
   }
}
