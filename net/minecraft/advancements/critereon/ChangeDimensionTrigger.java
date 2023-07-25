package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

public class ChangeDimensionTrigger extends SimpleCriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("changed_dimension");

   public ResourceLocation getId() {
      return ID;
   }

   public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ResourceKey<Level> resourcekey = jsonobject.has("from") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonobject, "from"))) : null;
      ResourceKey<Level> resourcekey1 = jsonobject.has("to") ? ResourceKey.create(Registries.DIMENSION, new ResourceLocation(GsonHelper.getAsString(jsonobject, "to"))) : null;
      return new ChangeDimensionTrigger.TriggerInstance(contextawarepredicate, resourcekey, resourcekey1);
   }

   public void trigger(ServerPlayer serverplayer, ResourceKey<Level> resourcekey, ResourceKey<Level> resourcekey1) {
      this.trigger(serverplayer, (changedimensiontrigger_triggerinstance) -> changedimensiontrigger_triggerinstance.matches(resourcekey, resourcekey1));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final ResourceKey<Level> from;
      @Nullable
      private final ResourceKey<Level> to;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, @Nullable ResourceKey<Level> resourcekey, @Nullable ResourceKey<Level> resourcekey1) {
         super(ChangeDimensionTrigger.ID, contextawarepredicate);
         this.from = resourcekey;
         this.to = resourcekey1;
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimension() {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, (ResourceKey<Level>)null, (ResourceKey<Level>)null);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimension(ResourceKey<Level> resourcekey, ResourceKey<Level> resourcekey1) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourcekey, resourcekey1);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(ResourceKey<Level> resourcekey) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, (ResourceKey<Level>)null, resourcekey);
      }

      public static ChangeDimensionTrigger.TriggerInstance changedDimensionFrom(ResourceKey<Level> resourcekey) {
         return new ChangeDimensionTrigger.TriggerInstance(ContextAwarePredicate.ANY, resourcekey, (ResourceKey<Level>)null);
      }

      public boolean matches(ResourceKey<Level> resourcekey, ResourceKey<Level> resourcekey1) {
         if (this.from != null && this.from != resourcekey) {
            return false;
         } else {
            return this.to == null || this.to == resourcekey1;
         }
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         if (this.from != null) {
            jsonobject.addProperty("from", this.from.location().toString());
         }

         if (this.to != null) {
            jsonobject.addProperty("to", this.to.location().toString());
         }

         return jsonobject;
      }
   }
}
