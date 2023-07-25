package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class SimpleCriterionTrigger<T extends AbstractCriterionTriggerInstance> implements CriterionTrigger<T> {
   private final Map<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

   public final void addPlayerListener(PlayerAdvancements playeradvancements, CriterionTrigger.Listener<T> criteriontrigger_listener) {
      this.players.computeIfAbsent(playeradvancements, (playeradvancements1) -> Sets.newHashSet()).add(criteriontrigger_listener);
   }

   public final void removePlayerListener(PlayerAdvancements playeradvancements, CriterionTrigger.Listener<T> criteriontrigger_listener) {
      Set<CriterionTrigger.Listener<T>> set = this.players.get(playeradvancements);
      if (set != null) {
         set.remove(criteriontrigger_listener);
         if (set.isEmpty()) {
            this.players.remove(playeradvancements);
         }
      }

   }

   public final void removePlayerListeners(PlayerAdvancements playeradvancements) {
      this.players.remove(playeradvancements);
   }

   protected abstract T createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext);

   public final T createInstance(JsonObject jsonobject, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate = EntityPredicate.fromJson(jsonobject, "player", deserializationcontext);
      return this.createInstance(jsonobject, contextawarepredicate, deserializationcontext);
   }

   protected void trigger(ServerPlayer serverplayer, Predicate<T> predicate) {
      PlayerAdvancements playeradvancements = serverplayer.getAdvancements();
      Set<CriterionTrigger.Listener<T>> set = this.players.get(playeradvancements);
      if (set != null && !set.isEmpty()) {
         LootContext lootcontext = EntityPredicate.createContext(serverplayer, serverplayer);
         List<CriterionTrigger.Listener<T>> list = null;

         for(CriterionTrigger.Listener<T> criteriontrigger_listener : set) {
            T abstractcriteriontriggerinstance = criteriontrigger_listener.getTriggerInstance();
            if (predicate.test(abstractcriteriontriggerinstance) && abstractcriteriontriggerinstance.getPlayerPredicate().matches(lootcontext)) {
               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(criteriontrigger_listener);
            }
         }

         if (list != null) {
            for(CriterionTrigger.Listener<T> criteriontrigger_listener1 : list) {
               criteriontrigger_listener1.run(playeradvancements);
            }
         }

      }
   }
}
