package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

   public ResourceLocation getId() {
      return ID;
   }

   public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate[] acontextawarepredicate = EntityPredicate.fromJsonArray(jsonobject, "victims", deserializationcontext);
      MinMaxBounds.Ints minmaxbounds_ints = MinMaxBounds.Ints.fromJson(jsonobject.get("unique_entity_types"));
      return new KilledByCrossbowTrigger.TriggerInstance(contextawarepredicate, acontextawarepredicate, minmaxbounds_ints);
   }

   public void trigger(ServerPlayer serverplayer, Collection<Entity> collection) {
      List<LootContext> list = Lists.newArrayList();
      Set<EntityType<?>> set = Sets.newHashSet();

      for(Entity entity : collection) {
         set.add(entity.getType());
         list.add(EntityPredicate.createContext(serverplayer, entity));
      }

      this.trigger(serverplayer, (killedbycrossbowtrigger_triggerinstance) -> killedbycrossbowtrigger_triggerinstance.matches(list, set.size()));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate[] victims;
      private final MinMaxBounds.Ints uniqueEntityTypes;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, ContextAwarePredicate[] acontextawarepredicate, MinMaxBounds.Ints minmaxbounds_ints) {
         super(KilledByCrossbowTrigger.ID, contextawarepredicate);
         this.victims = acontextawarepredicate;
         this.uniqueEntityTypes = minmaxbounds_ints;
      }

      public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... aentitypredicate_builder) {
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[aentitypredicate_builder.length];

         for(int i = 0; i < aentitypredicate_builder.length; ++i) {
            EntityPredicate.Builder entitypredicate_builder = aentitypredicate_builder[i];
            acontextawarepredicate[i] = EntityPredicate.wrap(entitypredicate_builder.build());
         }

         return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, acontextawarepredicate, MinMaxBounds.Ints.ANY);
      }

      public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints minmaxbounds_ints) {
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[0];
         return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, acontextawarepredicate, minmaxbounds_ints);
      }

      public boolean matches(Collection<LootContext> collection, int i) {
         if (this.victims.length > 0) {
            List<LootContext> list = Lists.newArrayList(collection);

            for(ContextAwarePredicate contextawarepredicate : this.victims) {
               boolean flag = false;
               Iterator<LootContext> iterator = list.iterator();

               while(iterator.hasNext()) {
                  LootContext lootcontext = iterator.next();
                  if (contextawarepredicate.matches(lootcontext)) {
                     iterator.remove();
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }

         return this.uniqueEntityTypes.matches(i);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("victims", ContextAwarePredicate.toJson(this.victims, serializationcontext));
         jsonobject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
         return jsonobject;
      }
   }
}
