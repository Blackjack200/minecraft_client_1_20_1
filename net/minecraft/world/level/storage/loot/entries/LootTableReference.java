package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootTableReference extends LootPoolSingletonContainer {
   final ResourceLocation name;

   LootTableReference(ResourceLocation resourcelocation, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
      super(i, j, alootitemcondition, alootitemfunction);
      this.name = resourcelocation;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.REFERENCE;
   }

   public void createItemStack(Consumer<ItemStack> consumer, LootContext lootcontext) {
      LootTable loottable = lootcontext.getResolver().getLootTable(this.name);
      loottable.getRandomItemsRaw(lootcontext, consumer);
   }

   public void validate(ValidationContext validationcontext) {
      LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);
      if (validationcontext.hasVisitedElement(lootdataid)) {
         validationcontext.reportProblem("Table " + this.name + " is recursively called");
      } else {
         super.validate(validationcontext);
         validationcontext.resolver().getElementOptional(lootdataid).ifPresentOrElse((loottable) -> loottable.validate(validationcontext.enterElement("->{" + this.name + "}", lootdataid)), () -> validationcontext.reportProblem("Unknown loot table called " + this.name));
      }
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation resourcelocation) {
      return simpleBuilder((i, j, alootitemcondition, alootitemfunction) -> new LootTableReference(resourcelocation, i, j, alootitemcondition, alootitemfunction));
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
      public void serializeCustom(JsonObject jsonobject, LootTableReference loottablereference, JsonSerializationContext jsonserializationcontext) {
         super.serializeCustom(jsonobject, loottablereference, jsonserializationcontext);
         jsonobject.addProperty("name", loottablereference.name.toString());
      }

      protected LootTableReference deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, int i, int j, LootItemCondition[] alootitemcondition, LootItemFunction[] alootitemfunction) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "name"));
         return new LootTableReference(resourcelocation, i, j, alootitemcondition, alootitemfunction);
      }
   }
}
