package net.minecraft.advancements;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Criterion {
   @Nullable
   private final CriterionTriggerInstance trigger;

   public Criterion(CriterionTriggerInstance criteriontriggerinstance) {
      this.trigger = criteriontriggerinstance;
   }

   public Criterion() {
      this.trigger = null;
   }

   public void serializeToNetwork(FriendlyByteBuf friendlybytebuf) {
   }

   public static Criterion criterionFromJson(JsonObject jsonobject, DeserializationContext deserializationcontext) {
      ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "trigger"));
      CriterionTrigger<?> criteriontrigger = CriteriaTriggers.getCriterion(resourcelocation);
      if (criteriontrigger == null) {
         throw new JsonSyntaxException("Invalid criterion trigger: " + resourcelocation);
      } else {
         CriterionTriggerInstance criteriontriggerinstance = criteriontrigger.createInstance(GsonHelper.getAsJsonObject(jsonobject, "conditions", new JsonObject()), deserializationcontext);
         return new Criterion(criteriontriggerinstance);
      }
   }

   public static Criterion criterionFromNetwork(FriendlyByteBuf friendlybytebuf) {
      return new Criterion();
   }

   public static Map<String, Criterion> criteriaFromJson(JsonObject jsonobject, DeserializationContext deserializationcontext) {
      Map<String, Criterion> map = Maps.newHashMap();

      for(Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
         map.put(map_entry.getKey(), criterionFromJson(GsonHelper.convertToJsonObject(map_entry.getValue(), "criterion"), deserializationcontext));
      }

      return map;
   }

   public static Map<String, Criterion> criteriaFromNetwork(FriendlyByteBuf friendlybytebuf) {
      return friendlybytebuf.readMap(FriendlyByteBuf::readUtf, Criterion::criterionFromNetwork);
   }

   public static void serializeToNetwork(Map<String, Criterion> map, FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeMap(map, FriendlyByteBuf::writeUtf, (friendlybytebuf1, criterion) -> criterion.serializeToNetwork(friendlybytebuf1));
   }

   @Nullable
   public CriterionTriggerInstance getTrigger() {
      return this.trigger;
   }

   public JsonElement serializeToJson() {
      if (this.trigger == null) {
         throw new JsonSyntaxException("Missing trigger");
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("trigger", this.trigger.getCriterion().toString());
         JsonObject jsonobject1 = this.trigger.serializeToJson(SerializationContext.INSTANCE);
         if (jsonobject1.size() != 0) {
            jsonobject.add("conditions", jsonobject1);
         }

         return jsonobject;
      }
   }
}
