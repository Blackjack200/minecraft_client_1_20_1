package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger extends SimpleCriterionTrigger<LootTableTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

   public ResourceLocation getId() {
      return ID;
   }

   protected LootTableTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "loot_table"));
      return new LootTableTrigger.TriggerInstance(contextawarepredicate, resourcelocation);
   }

   public void trigger(ServerPlayer serverplayer, ResourceLocation resourcelocation) {
      this.trigger(serverplayer, (loottabletrigger_triggerinstance) -> loottabletrigger_triggerinstance.matches(resourcelocation));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ResourceLocation lootTable;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ResourceLocation resourcelocation) {
         super(LootTableTrigger.ID, contextawarepredicate);
         this.lootTable = resourcelocation;
      }

      public static LootTableTrigger.TriggerInstance lootTableUsed(ResourceLocation resourcelocation) {
         return new LootTableTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourcelocation);
      }

      public boolean matches(ResourceLocation resourcelocation) {
         return this.lootTable.equals(resourcelocation);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.addProperty("loot_table", this.lootTable.toString());
         return jsonobject;
      }
   }
}
