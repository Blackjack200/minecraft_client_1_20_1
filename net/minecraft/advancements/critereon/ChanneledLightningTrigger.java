package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

   public ResourceLocation getId() {
      return ID;
   }

   public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate[] acontextawarepredicate = EntityPredicate.fromJsonArray(jsonobject, "victims", deserializationcontext);
      return new ChanneledLightningTrigger.TriggerInstance(contextawarepredicate, acontextawarepredicate);
   }

   public void trigger(ServerPlayer serverplayer, Collection<? extends Entity> collection) {
      List<LootContext> list = collection.stream().map((entity) -> EntityPredicate.createContext(serverplayer, entity)).collect(Collectors.toList());
      this.trigger(serverplayer, (channeledlightningtrigger_triggerinstance) -> channeledlightningtrigger_triggerinstance.matches(list));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate[] victims;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate[] acontextawarepredicate) {
         super(ChanneledLightningTrigger.ID, contextawarepredicate);
         this.victims = acontextawarepredicate;
      }

      public static ChanneledLightningTrigger.TriggerInstance channeledLightning(EntityPredicate... aentitypredicate) {
         return new ChanneledLightningTrigger.TriggerInstance(ContextAwarePredicate.ANY, Stream.of(aentitypredicate).map(EntityPredicate::wrap).toArray((i) -> new ContextAwarePredicate[i]));
      }

      public boolean matches(Collection<? extends LootContext> collection) {
         for(ContextAwarePredicate contextawarepredicate : this.victims) {
            boolean flag = false;

            for(LootContext lootcontext : collection) {
               if (contextawarepredicate.matches(lootcontext)) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               return false;
            }
         }

         return true;
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims, serializationcontext));
         return jsonobject;
      }
   }
}
