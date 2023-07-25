package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetEnchantmentsFunction extends LootItemConditionalFunction {
   final Map<Enchantment, NumberProvider> enchantments;
   final boolean add;

   SetEnchantmentsFunction(LootItemCondition[] alootitemcondition, Map<Enchantment, NumberProvider> map, boolean flag) {
      super(alootitemcondition);
      this.enchantments = ImmutableMap.copyOf(map);
      this.add = flag;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_ENCHANTMENTS;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.enchantments.values().stream().flatMap((numberprovider) -> numberprovider.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
   }

   public ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      Object2IntMap<Enchantment> object2intmap = new Object2IntOpenHashMap<>();
      this.enchantments.forEach((enchantment3, numberprovider) -> object2intmap.put(enchantment3, numberprovider.getInt(lootcontext)));
      if (itemstack.getItem() == Items.BOOK) {
         ItemStack itemstack1 = new ItemStack(Items.ENCHANTED_BOOK);
         object2intmap.forEach((enchantment2, integer2) -> EnchantedBookItem.addEnchantment(itemstack1, new EnchantmentInstance(enchantment2, integer2)));
         return itemstack1;
      } else {
         Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
         if (this.add) {
            object2intmap.forEach((enchantment1, integer1) -> updateEnchantment(map, enchantment1, Math.max(map.getOrDefault(enchantment1, 0) + integer1, 0)));
         } else {
            object2intmap.forEach((enchantment, integer) -> updateEnchantment(map, enchantment, Math.max(integer, 0)));
         }

         EnchantmentHelper.setEnchantments(map, itemstack);
         return itemstack;
      }
   }

   private static void updateEnchantment(Map<Enchantment, Integer> map, Enchantment enchantment, int i) {
      if (i == 0) {
         map.remove(enchantment);
      } else {
         map.put(enchantment, i);
      }

   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetEnchantmentsFunction.Builder> {
      private final Map<Enchantment, NumberProvider> enchantments = Maps.newHashMap();
      private final boolean add;

      public Builder() {
         this(false);
      }

      public Builder(boolean flag) {
         this.add = flag;
      }

      protected SetEnchantmentsFunction.Builder getThis() {
         return this;
      }

      public SetEnchantmentsFunction.Builder withEnchantment(Enchantment enchantment, NumberProvider numberprovider) {
         this.enchantments.put(enchantment, numberprovider);
         return this;
      }

      public LootItemFunction build() {
         return new SetEnchantmentsFunction(this.getConditions(), this.enchantments, this.add);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetEnchantmentsFunction> {
      public void serialize(JsonObject jsonobject, SetEnchantmentsFunction setenchantmentsfunction, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, setenchantmentsfunction, jsonserializationcontext);
         JsonObject jsonobject1 = new JsonObject();
         setenchantmentsfunction.enchantments.forEach((enchantment, numberprovider) -> {
            ResourceLocation resourcelocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
            if (resourcelocation == null) {
               throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
            } else {
               jsonobject1.add(resourcelocation.toString(), jsonserializationcontext.serialize(numberprovider));
            }
         });
         jsonobject.add("enchantments", jsonobject1);
         jsonobject.addProperty("add", setenchantmentsfunction.add);
      }

      public SetEnchantmentsFunction deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         Map<Enchantment, NumberProvider> map = Maps.newHashMap();
         if (jsonobject.has("enchantments")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "enchantments");

            for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
               String s = map_entry.getKey();
               JsonElement jsonelement = map_entry.getValue();
               Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(s)).orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + s + "'"));
               NumberProvider numberprovider = jsondeserializationcontext.deserialize(jsonelement, NumberProvider.class);
               map.put(enchantment, numberprovider);
            }
         }

         boolean flag = GsonHelper.getAsBoolean(jsonobject, "add", false);
         return new SetEnchantmentsFunction(alootitemcondition, map, flag);
      }
   }
}
