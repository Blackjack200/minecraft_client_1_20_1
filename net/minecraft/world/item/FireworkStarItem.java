package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

public class FireworkStarItem extends Item {
   public FireworkStarItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      CompoundTag compoundtag = itemstack.getTagElement("Explosion");
      if (compoundtag != null) {
         appendHoverText(compoundtag, list);
      }

   }

   public static void appendHoverText(CompoundTag compoundtag, List<Component> list) {
      FireworkRocketItem.Shape fireworkrocketitem_shape = FireworkRocketItem.Shape.byId(compoundtag.getByte("Type"));
      list.add(Component.translatable("item.minecraft.firework_star.shape." + fireworkrocketitem_shape.getName()).withStyle(ChatFormatting.GRAY));
      int[] aint = compoundtag.getIntArray("Colors");
      if (aint.length > 0) {
         list.add(appendColors(Component.empty().withStyle(ChatFormatting.GRAY), aint));
      }

      int[] aint1 = compoundtag.getIntArray("FadeColors");
      if (aint1.length > 0) {
         list.add(appendColors(Component.translatable("item.minecraft.firework_star.fade_to").append(CommonComponents.SPACE).withStyle(ChatFormatting.GRAY), aint1));
      }

      if (compoundtag.getBoolean("Trail")) {
         list.add(Component.translatable("item.minecraft.firework_star.trail").withStyle(ChatFormatting.GRAY));
      }

      if (compoundtag.getBoolean("Flicker")) {
         list.add(Component.translatable("item.minecraft.firework_star.flicker").withStyle(ChatFormatting.GRAY));
      }

   }

   private static Component appendColors(MutableComponent mutablecomponent, int[] aint) {
      for(int i = 0; i < aint.length; ++i) {
         if (i > 0) {
            mutablecomponent.append(", ");
         }

         mutablecomponent.append(getColorName(aint[i]));
      }

      return mutablecomponent;
   }

   private static Component getColorName(int i) {
      DyeColor dyecolor = DyeColor.byFireworkColor(i);
      return dyecolor == null ? Component.translatable("item.minecraft.firework_star.custom_color") : Component.translatable("item.minecraft.firework_star." + dyecolor.getName());
   }
}
