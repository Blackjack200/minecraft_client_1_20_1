package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

public class EnchantedBookItem extends Item {
   public static final String TAG_STORED_ENCHANTMENTS = "StoredEnchantments";

   public EnchantedBookItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public boolean isFoil(ItemStack itemstack) {
      return true;
   }

   public boolean isEnchantable(ItemStack itemstack) {
      return false;
   }

   public static ListTag getEnchantments(ItemStack itemstack) {
      CompoundTag compoundtag = itemstack.getTag();
      return compoundtag != null ? compoundtag.getList("StoredEnchantments", 10) : new ListTag();
   }

   public void appendHoverText(ItemStack itemstack, @Nullable Level level, List<Component> list, TooltipFlag tooltipflag) {
      super.appendHoverText(itemstack, level, list, tooltipflag);
      ItemStack.appendEnchantmentNames(list, getEnchantments(itemstack));
   }

   public static void addEnchantment(ItemStack itemstack, EnchantmentInstance enchantmentinstance) {
      ListTag listtag = getEnchantments(itemstack);
      boolean flag = true;
      ResourceLocation resourcelocation = EnchantmentHelper.getEnchantmentId(enchantmentinstance.enchantment);

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         ResourceLocation resourcelocation1 = EnchantmentHelper.getEnchantmentId(compoundtag);
         if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
            if (EnchantmentHelper.getEnchantmentLevel(compoundtag) < enchantmentinstance.level) {
               EnchantmentHelper.setEnchantmentLevel(compoundtag, enchantmentinstance.level);
            }

            flag = false;
            break;
         }
      }

      if (flag) {
         listtag.add(EnchantmentHelper.storeEnchantment(resourcelocation, enchantmentinstance.level));
      }

      itemstack.getOrCreateTag().put("StoredEnchantments", listtag);
   }

   public static ItemStack createForEnchantment(EnchantmentInstance enchantmentinstance) {
      ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
      addEnchantment(itemstack, enchantmentinstance);
      return itemstack;
   }
}
