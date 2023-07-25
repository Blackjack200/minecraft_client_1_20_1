package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class FunctionReference extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final ResourceLocation name;

   FunctionReference(LootItemCondition[] alootitemcondition, ResourceLocation resourcelocation) {
      super(alootitemcondition);
      this.name = resourcelocation;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.REFERENCE;
   }

   public void validate(ValidationContext validationcontext) {
      LootDataId<LootItemFunction> lootdataid = new LootDataId<>(LootDataType.MODIFIER, this.name);
      if (validationcontext.hasVisitedElement(lootdataid)) {
         validationcontext.reportProblem("Function " + this.name + " is recursively called");
      } else {
         super.validate(validationcontext);
         validationcontext.resolver().getElementOptional(lootdataid).ifPresentOrElse((lootitemfunction) -> lootitemfunction.validate(validationcontext.enterElement(".{" + this.name + "}", lootdataid)), () -> validationcontext.reportProblem("Unknown function table called " + this.name));
      }
   }

   protected ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      LootItemFunction lootitemfunction = lootcontext.getResolver().getElement(LootDataType.MODIFIER, this.name);
      if (lootitemfunction == null) {
         LOGGER.warn("Unknown function: {}", (Object)this.name);
         return itemstack;
      } else {
         LootContext.VisitedEntry<?> lootcontext_visitedentry = LootContext.createVisitedEntry(lootitemfunction);
         if (lootcontext.pushVisitedElement(lootcontext_visitedentry)) {
            ItemStack var5;
            try {
               var5 = lootitemfunction.apply(itemstack, lootcontext);
            } finally {
               lootcontext.popVisitedElement(lootcontext_visitedentry);
            }

            return var5;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return itemstack;
         }
      }
   }

   public static LootItemConditionalFunction.Builder<?> functionReference(ResourceLocation resourcelocation) {
      return simpleBuilder((alootitemcondition) -> new FunctionReference(alootitemcondition, resourcelocation));
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<FunctionReference> {
      public void serialize(JsonObject jsonobject, FunctionReference functionreference, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("name", functionreference.name.toString());
      }

      public FunctionReference deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "name"));
         return new FunctionReference(alootitemcondition, resourcelocation);
      }
   }
}
