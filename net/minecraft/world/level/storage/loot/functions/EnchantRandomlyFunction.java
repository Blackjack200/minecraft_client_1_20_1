package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class EnchantRandomlyFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final List<Enchantment> enchantments;

   EnchantRandomlyFunction(LootItemCondition[] alootitemcondition, Collection<Enchantment> collection) {
      super(alootitemcondition);
      this.enchantments = ImmutableList.copyOf(collection);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.ENCHANT_RANDOMLY;
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      RandomSource randomsource = lootcontext.getRandom();
      Enchantment enchantment;
      if (this.enchantments.isEmpty()) {
         boolean flag = itemstack.is(Items.BOOK);
         List<Enchantment> list = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).filter((enchantment2) -> flag || enchantment2.canEnchant(itemstack)).collect(Collectors.toList());
         if (list.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", (Object)itemstack);
            return itemstack;
         }

         enchantment = list.get(randomsource.nextInt(list.size()));
      } else {
         enchantment = this.enchantments.get(randomsource.nextInt(this.enchantments.size()));
      }

      return enchantItem(itemstack, enchantment, randomsource);
   }

   private static ItemStack enchantItem(ItemStack itemstack, Enchantment enchantment, RandomSource randomsource) {
      int i = Mth.nextInt(randomsource, enchantment.getMinLevel(), enchantment.getMaxLevel());
      if (itemstack.is(Items.BOOK)) {
         itemstack = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(itemstack, new EnchantmentInstance(enchantment, i));
      } else {
         itemstack.enchant(enchantment, i);
      }

      return itemstack;
   }

   public static EnchantRandomlyFunction.Builder randomEnchantment() {
      return new EnchantRandomlyFunction.Builder();
   }

   public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
      return simpleBuilder((alootitemcondition) -> new EnchantRandomlyFunction(alootitemcondition, ImmutableList.of()));
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
      private final Set<Enchantment> enchantments = Sets.newHashSet();

      protected EnchantRandomlyFunction.Builder getThis() {
         return this;
      }

      public EnchantRandomlyFunction.Builder withEnchantment(Enchantment enchantment) {
         this.enchantments.add(enchantment);
         return this;
      }

      public LootItemFunction build() {
         return new EnchantRandomlyFunction(this.getConditions(), this.enchantments);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantRandomlyFunction> {
      public void serialize(JsonObject jsonobject, EnchantRandomlyFunction enchantrandomlyfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, enchantrandomlyfunction, jsonserializationcontext);
         if (!enchantrandomlyfunction.enchantments.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Enchantment enchantment : enchantrandomlyfunction.enchantments) {
               ResourceLocation resourcelocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
               if (resourcelocation == null) {
                  throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
               }

               jsonarray.add(new JsonPrimitive(resourcelocation.toString()));
            }

            jsonobject.add("enchantments", jsonarray);
         }

      }

      public EnchantRandomlyFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         List<Enchantment> list = Lists.newArrayList();
         if (jsonobject.has("enchantments")) {
            for(JsonElement jsonelement : GsonHelper.getAsJsonArray(jsonobject, "enchantments")) {
               String s = GsonHelper.convertToString(jsonelement, "enchantment");
               Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(s)).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + s + "'"));
               list.add(enchantment);
            }
         }

         return new EnchantRandomlyFunction(alootitemcondition, list);
      }
   }
}
