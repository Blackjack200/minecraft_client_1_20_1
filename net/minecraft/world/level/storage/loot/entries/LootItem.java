package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem extends LootPoolSingletonContainer {
   final Item item;

   LootItem(Item item, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
      super(i, j, alootitemcondition, alootitemfunction);
      this.item = item;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.ITEM;
   }

   public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
      consumer.accept(new ItemStack(this.item));
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike itemlike) {
      return simpleBuilder((i, j, alootitemcondition, alootitemfunction) -> new LootItem(itemlike.asItem(), i, j, alootitemcondition, alootitemfunction));
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<LootItem> {
      public void serializeCustom(JsonObject jsonobject, LootItem lootitem, JsonSerializationContext jsonserializationcontext) {
         super.serializeCustom(jsonobject, lootitem, jsonserializationcontext);
         ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(lootitem.item);
         if (resourcelocation == null) {
            throw new IllegalArgumentException("Can't serialize unknown item " + lootitem.item);
         } else {
            jsonobject.addProperty("name", resourcelocation.toString());
         }
      }

      protected LootItem deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
         Item item = GsonHelper.getAsItem(jsonobject, "name");
         return new LootItem(item, i, j, alootitemcondition, alootitemfunction);
      }
   }
}
