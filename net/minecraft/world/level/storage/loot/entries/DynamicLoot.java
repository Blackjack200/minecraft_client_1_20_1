package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer {
   final ResourceLocation name;

   DynamicLoot(ResourceLocation resourcelocation, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
      super(i, j, alootitemcondition, alootitemfunction);
      this.name = resourcelocation;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.DYNAMIC;
   }

   public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
      lootcontext.addDynamicDrops(this.name, consumer);
   }

   public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation resourcelocation) {
      return simpleBuilder((i, j, alootitemcondition, alootitemfunction) -> new DynamicLoot(resourcelocation, i, j, alootitemcondition, alootitemfunction));
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
      public void serializeCustom(JsonObject jsonobject, DynamicLoot dynamicloot, JsonSerializationContext jsonserializationcontext) {
         super.serializeCustom(jsonobject, dynamicloot, jsonserializationcontext);
         jsonobject.addProperty("name", dynamicloot.name.toString());
      }

      protected DynamicLoot deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "name"));
         return new DynamicLoot(resourcelocation, i, j, alootitemcondition, alootitemfunction);
      }
   }
}
