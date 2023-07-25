package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
   EmptyLootItem(int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
      super(i, j, alootitemcondition, alootitemfunction);
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.EMPTY;
   }

   public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
   }

   public static LootPoolSingletonContainer.Builder<?> emptyItem() {
      return simpleBuilder(EmptyLootItem::new);
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<EmptyLootItem> {
      public EmptyLootItem deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
         return new EmptyLootItem(i, j, alootitemcondition, alootitemfunction);
      }
   }
}
