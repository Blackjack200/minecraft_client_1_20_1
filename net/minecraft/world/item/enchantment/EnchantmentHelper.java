package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper {
   private static final String TAG_ENCH_ID = "id";
   private static final String TAG_ENCH_LEVEL = "lvl";
   private static final float SWIFT_SNEAK_EXTRA_FACTOR = 0.15F;

   public static CompoundTag storeEnchantment(@Nullable ResourceLocation resourcelocation, int i) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("id", String.valueOf((Object)resourcelocation));
      compoundtag.putShort("lvl", (short)i);
      return compoundtag;
   }

   public static void setEnchantmentLevel(CompoundTag compoundtag, int i) {
      compoundtag.putShort("lvl", (short)i);
   }

   public static int getEnchantmentLevel(CompoundTag compoundtag) {
      return Mth.clamp(compoundtag.getInt("lvl"), 0, 255);
   }

   @Nullable
   public static ResourceLocation getEnchantmentId(CompoundTag compoundtag) {
      return ResourceLocation.tryParse(compoundtag.getString("id"));
   }

   @Nullable
   public static ResourceLocation getEnchantmentId(Enchantment enchantment) {
      return BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
   }

   public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemstack) {
      if (itemstack.isEmpty()) {
         return 0;
      } else {
         ResourceLocation resourcelocation = getEnchantmentId(enchantment);
         ListTag listtag = itemstack.getEnchantmentTags();

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            ResourceLocation resourcelocation1 = getEnchantmentId(compoundtag);
            if (resourcelocation1 != null && resourcelocation1.equals(resourcelocation)) {
               return getEnchantmentLevel(compoundtag);
            }
         }

         return 0;
      }
   }

   public static Map<Enchantment, Integer> getEnchantments(ItemStack itemstack) {
      ListTag listtag = itemstack.is(Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(itemstack) : itemstack.getEnchantmentTags();
      return deserializeEnchantments(listtag);
   }

   public static Map<Enchantment, Integer> deserializeEnchantments(ListTag listtag) {
      Map<Enchantment, Integer> map = Maps.newLinkedHashMap();

      for(int i = 0; i < listtag.size(); ++i) {
         CompoundTag compoundtag = listtag.getCompound(i);
         BuiltInRegistries.ENCHANTMENT.getOptional(getEnchantmentId(compoundtag)).ifPresent((enchantment) -> map.put(enchantment, getEnchantmentLevel(compoundtag)));
      }

      return map;
   }

   public static void setEnchantments(Map<Enchantment, Integer> map, ItemStack itemstack) {
      ListTag listtag = new ListTag();

      for(Map.Entry<Enchantment, Integer> map_entry : map.entrySet()) {
         Enchantment enchantment = map_entry.getKey();
         if (enchantment != null) {
            int i = map_entry.getValue();
            listtag.add(storeEnchantment(getEnchantmentId(enchantment), i));
            if (itemstack.is(Items.ENCHANTED_BOOK)) {
               EnchantedBookItem.addEnchantment(itemstack, new EnchantmentInstance(enchantment, i));
            }
         }
      }

      if (listtag.isEmpty()) {
         itemstack.removeTagKey("Enchantments");
      } else if (!itemstack.is(Items.ENCHANTED_BOOK)) {
         itemstack.addTagElement("Enchantments", listtag);
      }

   }

   private static void runIterationOnItem(EnchantmentHelper.EnchantmentVisitor enchantmenthelper_enchantmentvisitor, ItemStack itemstack) {
      if (!itemstack.isEmpty()) {
         ListTag listtag = itemstack.getEnchantmentTags();

         for(int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            BuiltInRegistries.ENCHANTMENT.getOptional(getEnchantmentId(compoundtag)).ifPresent((enchantment) -> enchantmenthelper_enchantmentvisitor.accept(enchantment, getEnchantmentLevel(compoundtag)));
         }

      }
   }

   private static void runIterationOnInventory(EnchantmentHelper.EnchantmentVisitor enchantmenthelper_enchantmentvisitor, Iterable<ItemStack> iterable) {
      for(ItemStack itemstack : iterable) {
         runIterationOnItem(enchantmenthelper_enchantmentvisitor, itemstack);
      }

   }

   public static int getDamageProtection(Iterable<ItemStack> iterable, DamageSource damagesource) {
      MutableInt mutableint = new MutableInt();
      runIterationOnInventory((enchantment, i) -> mutableint.add(enchantment.getDamageProtection(i, damagesource)), iterable);
      return mutableint.intValue();
   }

   public static float getDamageBonus(ItemStack itemstack, MobType mobtype) {
      MutableFloat mutablefloat = new MutableFloat();
      runIterationOnItem((enchantment, i) -> mutablefloat.add(enchantment.getDamageBonus(i, mobtype)), itemstack);
      return mutablefloat.floatValue();
   }

   public static float getSweepingDamageRatio(LivingEntity livingentity) {
      int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, livingentity);
      return i > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(i) : 0.0F;
   }

   public static void doPostHurtEffects(LivingEntity livingentity, Entity entity) {
      EnchantmentHelper.EnchantmentVisitor enchantmenthelper_enchantmentvisitor = (enchantment, i) -> enchantment.doPostHurt(livingentity, entity, i);
      if (livingentity != null) {
         runIterationOnInventory(enchantmenthelper_enchantmentvisitor, livingentity.getAllSlots());
      }

      if (entity instanceof Player) {
         runIterationOnItem(enchantmenthelper_enchantmentvisitor, livingentity.getMainHandItem());
      }

   }

   public static void doPostDamageEffects(LivingEntity livingentity, Entity entity) {
      EnchantmentHelper.EnchantmentVisitor enchantmenthelper_enchantmentvisitor = (enchantment, i) -> enchantment.doPostAttack(livingentity, entity, i);
      if (livingentity != null) {
         runIterationOnInventory(enchantmenthelper_enchantmentvisitor, livingentity.getAllSlots());
      }

      if (livingentity instanceof Player) {
         runIterationOnItem(enchantmenthelper_enchantmentvisitor, livingentity.getMainHandItem());
      }

   }

   public static int getEnchantmentLevel(Enchantment enchantment, LivingEntity livingentity) {
      Iterable<ItemStack> iterable = enchantment.getSlotItems(livingentity).values();
      if (iterable == null) {
         return 0;
      } else {
         int i = 0;

         for(ItemStack itemstack : iterable) {
            int j = getItemEnchantmentLevel(enchantment, itemstack);
            if (j > i) {
               i = j;
            }
         }

         return i;
      }
   }

   public static float getSneakingSpeedBonus(LivingEntity livingentity) {
      return (float)getEnchantmentLevel(Enchantments.SWIFT_SNEAK, livingentity) * 0.15F;
   }

   public static int getKnockbackBonus(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.KNOCKBACK, livingentity);
   }

   public static int getFireAspect(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.FIRE_ASPECT, livingentity);
   }

   public static int getRespiration(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.RESPIRATION, livingentity);
   }

   public static int getDepthStrider(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, livingentity);
   }

   public static int getBlockEfficiency(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, livingentity);
   }

   public static int getFishingLuckBonus(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, itemstack);
   }

   public static int getFishingSpeedBonus(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, itemstack);
   }

   public static int getMobLooting(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.MOB_LOOTING, livingentity);
   }

   public static boolean hasAquaAffinity(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, livingentity) > 0;
   }

   public static boolean hasFrostWalker(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.FROST_WALKER, livingentity) > 0;
   }

   public static boolean hasSoulSpeed(LivingEntity livingentity) {
      return getEnchantmentLevel(Enchantments.SOUL_SPEED, livingentity) > 0;
   }

   public static boolean hasBindingCurse(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemstack) > 0;
   }

   public static boolean hasVanishingCurse(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemstack) > 0;
   }

   public static boolean hasSilkTouch(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) > 0;
   }

   public static int getLoyalty(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.LOYALTY, itemstack);
   }

   public static int getRiptide(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.RIPTIDE, itemstack);
   }

   public static boolean hasChanneling(ItemStack itemstack) {
      return getItemEnchantmentLevel(Enchantments.CHANNELING, itemstack) > 0;
   }

   @Nullable
   public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingentity) {
      return getRandomItemWith(enchantment, livingentity, (itemstack) -> true);
   }

   @Nullable
   public static Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingentity, Predicate<ItemStack> predicate) {
      Map<EquipmentSlot, ItemStack> map = enchantment.getSlotItems(livingentity);
      if (map.isEmpty()) {
         return null;
      } else {
         List<Map.Entry<EquipmentSlot, ItemStack>> list = Lists.newArrayList();

         for(Map.Entry<EquipmentSlot, ItemStack> map_entry : map.entrySet()) {
            ItemStack itemstack = map_entry.getValue();
            if (!itemstack.isEmpty() && getItemEnchantmentLevel(enchantment, itemstack) > 0 && predicate.test(itemstack)) {
               list.add(map_entry);
            }
         }

         return list.isEmpty() ? null : list.get(livingentity.getRandom().nextInt(list.size()));
      }
   }

   public static int getEnchantmentCost(RandomSource randomsource, int i, int j, ItemStack itemstack) {
      Item item = itemstack.getItem();
      int k = item.getEnchantmentValue();
      if (k <= 0) {
         return 0;
      } else {
         if (j > 15) {
            j = 15;
         }

         int l = randomsource.nextInt(8) + 1 + (j >> 1) + randomsource.nextInt(j + 1);
         if (i == 0) {
            return Math.max(l / 3, 1);
         } else {
            return i == 1 ? l * 2 / 3 + 1 : Math.max(l, j * 2);
         }
      }
   }

   public static ItemStack enchantItem(RandomSource randomsource, ItemStack itemstack, int i, boolean flag) {
      List<EnchantmentInstance> list = selectEnchantment(randomsource, itemstack, i, flag);
      boolean flag1 = itemstack.is(Items.BOOK);
      if (flag1) {
         itemstack = new ItemStack(Items.ENCHANTED_BOOK);
      }

      for(EnchantmentInstance enchantmentinstance : list) {
         if (flag1) {
            EnchantedBookItem.addEnchantment(itemstack, enchantmentinstance);
         } else {
            itemstack.enchant(enchantmentinstance.enchantment, enchantmentinstance.level);
         }
      }

      return itemstack;
   }

   public static List<EnchantmentInstance> selectEnchantment(RandomSource randomsource, ItemStack itemstack, int i, boolean flag) {
      List<EnchantmentInstance> list = Lists.newArrayList();
      Item item = itemstack.getItem();
      int j = item.getEnchantmentValue();
      if (j <= 0) {
         return list;
      } else {
         i += 1 + randomsource.nextInt(j / 4 + 1) + randomsource.nextInt(j / 4 + 1);
         float f = (randomsource.nextFloat() + randomsource.nextFloat() - 1.0F) * 0.15F;
         i = Mth.clamp(Math.round((float)i + (float)i * f), 1, Integer.MAX_VALUE);
         List<EnchantmentInstance> list1 = getAvailableEnchantmentResults(i, itemstack, flag);
         if (!list1.isEmpty()) {
            WeightedRandom.getRandomItem(randomsource, list1).ifPresent(list::add);

            while(randomsource.nextInt(50) <= i) {
               if (!list.isEmpty()) {
                  filterCompatibleEnchantments(list1, Util.lastOf(list));
               }

               if (list1.isEmpty()) {
                  break;
               }

               WeightedRandom.getRandomItem(randomsource, list1).ifPresent(list::add);
               i /= 2;
            }
         }

         return list;
      }
   }

   public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance enchantmentinstance) {
      Iterator<EnchantmentInstance> iterator = list.iterator();

      while(iterator.hasNext()) {
         if (!enchantmentinstance.enchantment.isCompatibleWith((iterator.next()).enchantment)) {
            iterator.remove();
         }
      }

   }

   public static boolean isEnchantmentCompatible(Collection<Enchantment> collection, Enchantment enchantment) {
      for(Enchantment enchantment1 : collection) {
         if (!enchantment1.isCompatibleWith(enchantment)) {
            return false;
         }
      }

      return true;
   }

   public static List<EnchantmentInstance> getAvailableEnchantmentResults(int i, ItemStack itemstack, boolean flag) {
      List<EnchantmentInstance> list = Lists.newArrayList();
      Item item = itemstack.getItem();
      boolean flag1 = itemstack.is(Items.BOOK);

      for(Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
         if ((!enchantment.isTreasureOnly() || flag) && enchantment.isDiscoverable() && (enchantment.category.canEnchant(item) || flag1)) {
            for(int j = enchantment.getMaxLevel(); j > enchantment.getMinLevel() - 1; --j) {
               if (i >= enchantment.getMinCost(j) && i <= enchantment.getMaxCost(j)) {
                  list.add(new EnchantmentInstance(enchantment, j));
                  break;
               }
            }
         }
      }

      return list;
   }

   @FunctionalInterface
   interface EnchantmentVisitor {
      void accept(Enchantment enchantment, int i);
   }
}
