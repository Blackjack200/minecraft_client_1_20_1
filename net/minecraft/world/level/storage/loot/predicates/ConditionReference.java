package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.ValidationContext;
import org.slf4j.Logger;

public class ConditionReference implements LootItemCondition {
   private static final Logger LOGGER = LogUtils.getLogger();
   final ResourceLocation name;

   ConditionReference(ResourceLocation resourcelocation) {
      this.name = resourcelocation;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.REFERENCE;
   }

   public void validate(ValidationContext validationcontext) {
      LootDataId<LootItemCondition> lootdataid = new LootDataId<>(LootDataType.PREDICATE, this.name);
      if (validationcontext.hasVisitedElement(lootdataid)) {
         validationcontext.reportProblem("Condition " + this.name + " is recursively called");
      } else {
         LootItemCondition.super.validate(validationcontext);
         validationcontext.resolver().getElementOptional(lootdataid).ifPresentOrElse((lootitemcondition) -> lootitemcondition.validate(validationcontext.enterElement(".{" + this.name + "}", lootdataid)), () -> validationcontext.reportProblem("Unknown condition table called " + this.name));
      }
   }

   public boolean test(LootContext lootcontext) {
      LootItemCondition lootitemcondition = lootcontext.getResolver().getElement(LootDataType.PREDICATE, this.name);
      if (lootitemcondition == null) {
         LOGGER.warn("Tried using unknown condition table called {}", (Object)this.name);
         return false;
      } else {
         LootContext.VisitedEntry<?> lootcontext_visitedentry = LootContext.createVisitedEntry(lootitemcondition);
         if (lootcontext.pushVisitedElement(lootcontext_visitedentry)) {
            boolean var4;
            try {
               var4 = lootitemcondition.test(lootcontext);
            } finally {
               lootcontext.popVisitedElement(lootcontext_visitedentry);
            }

            return var4;
         } else {
            LOGGER.warn("Detected infinite loop in loot tables");
            return false;
         }
      }
   }

   public static LootItemCondition.Builder conditionReference(ResourceLocation resourcelocation) {
      return () -> new ConditionReference(resourcelocation);
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ConditionReference> {
      public void serialize(JsonObject jsonobject, ConditionReference conditionreference, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("name", conditionreference.name.toString());
      }

      public ConditionReference deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "name"));
         return new ConditionReference(resourcelocation);
      }
   }
}
