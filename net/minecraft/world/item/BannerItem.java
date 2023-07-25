package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem {
   private static final String PATTERN_PREFIX = "block.minecraft.banner.";

   public BannerItem(Block block, Block block1, Item.Properties item_properties) {
      super(block, block1, item_properties, Direction.DOWN);
      Validate.isInstanceOf(AbstractBannerBlock.class, block);
      Validate.isInstanceOf(AbstractBannerBlock.class, block1);
   }

   public static void appendHoverTextFromBannerBlockEntityTag(ItemStack itemstack, List<Component> list) {
      CompoundTag compoundtag = BlockItem.getBlockEntityData(itemstack);
      if (compoundtag != null && compoundtag.contains("Patterns")) {
         ListTag listtag = compoundtag.getList("Patterns", 10);

         for(int i = 0; i < listtag.size() && i < 6; ++i) {
            CompoundTag compoundtag1 = listtag.getCompound(i);
            DyeColor dyecolor = DyeColor.byId(compoundtag1.getInt("Color"));
            Holder<BannerPattern> holder = BannerPattern.byHash(compoundtag1.getString("Pattern"));
            if (holder != null) {
               holder.unwrapKey().map((resourcekey) -> resourcekey.location().toShortLanguageKey()).ifPresent((s) -> list.add(Component.translatable("block.minecraft.banner." + s + "." + dyecolor.getName()).withStyle(ChatFormatting.GRAY)));
            }
         }

      }
   }

   public DyeColor getColor() {
      return ((AbstractBannerBlock)this.getBlock()).getColor();
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      appendHoverTextFromBannerBlockEntityTag(itemstack, list);
   }
}
