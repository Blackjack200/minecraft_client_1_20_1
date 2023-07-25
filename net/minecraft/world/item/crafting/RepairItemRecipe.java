package net.minecraft.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class RepairItemRecipe extends CustomRecipe {
   public RepairItemRecipe(ResourceLocation resourcelocation, CraftingBookCategory craftingbookcategory) {
      super(resourcelocation, craftingbookcategory);
   }

   public boolean matches(CraftingContainer craftingcontainer, Level level) {
      List<ItemStack> list = Lists.newArrayList();

      for(int i = 0; i < craftingcontainer.getContainerSize(); ++i) {
         ItemStack itemstack = craftingcontainer.getItem(i);
         if (!itemstack.isEmpty()) {
            list.add(itemstack);
            if (list.size() > 1) {
               ItemStack itemstack1 = list.get(0);
               if (!itemstack.is(itemstack1.getItem()) || itemstack1.getCount() != 1 || itemstack.getCount() != 1 || !itemstack1.getItem().canBeDepleted()) {
                  return false;
               }
            }
         }
      }

      return list.size() == 2;
   }

   public ItemStack assemble(CraftingContainer craftingcontainer, RegistryAccess registryaccess) {
      List<ItemStack> list = Lists.newArrayList();

      for(int i = 0; i < craftingcontainer.getContainerSize(); ++i) {
         ItemStack itemstack = craftingcontainer.getItem(i);
         if (!itemstack.isEmpty()) {
            list.add(itemstack);
            if (list.size() > 1) {
               ItemStack itemstack1 = list.get(0);
               if (!itemstack.is(itemstack1.getItem()) || itemstack1.getCount() != 1 || itemstack.getCount() != 1 || !itemstack1.getItem().canBeDepleted()) {
                  return ItemStack.EMPTY;
               }
            }
         }
      }

      if (list.size() == 2) {
         ItemStack itemstack2 = list.get(0);
         ItemStack itemstack3 = list.get(1);
         if (itemstack2.is(itemstack3.getItem()) && itemstack2.getCount() == 1 && itemstack3.getCount() == 1 && itemstack2.getItem().canBeDepleted()) {
            Item item = itemstack2.getItem();
            int j = item.getMaxDamage() - itemstack2.getDamageValue();
            int k = item.getMaxDamage() - itemstack3.getDamageValue();
            int l = j + k + item.getMaxDamage() * 5 / 100;
            int i1 = item.getMaxDamage() - l;
            if (i1 < 0) {
               i1 = 0;
            }

            ItemStack itemstack4 = new ItemStack(itemstack2.getItem());
            itemstack4.setDamageValue(i1);
            Map<Enchantment, Integer> map = Maps.newHashMap();
            Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
            Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemstack3);
            BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isCurse).forEach((enchantment) -> {
               int j1 = Math.max(map1.getOrDefault(enchantment, 0), map2.getOrDefault(enchantment, 0));
               if (j1 > 0) {
                  map.put(enchantment, j1);
               }

            });
            if (!map.isEmpty()) {
               EnchantmentHelper.setEnchantments(map, itemstack4);
            }

            return itemstack4;
         }
      }

      return ItemStack.EMPTY;
   }

   public boolean canCraftInDimensions(int i, int j) {
      return i * j >= 2;
   }

   public RecipeSerializer<?> getSerializer() {
      return RecipeSerializer.REPAIR_ITEM;
   }
}
