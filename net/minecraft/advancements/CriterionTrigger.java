package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public interface CriterionTrigger<T extends CriterionTriggerInstance> {
   ResourceLocation getId();

   void addPlayerListener(PlayerAdvancements playeradvancements, CriterionTrigger.Listener<T> criteriontrigger_listener);

   void removePlayerListener(PlayerAdvancements playeradvancements, CriterionTrigger.Listener<T> criteriontrigger_listener);

   void removePlayerListeners(PlayerAdvancements playeradvancements);

   T createInstance(JsonObject jsonobject, DeserializationContext deserializationcontext);

   public static class Listener<T extends CriterionTriggerInstance> {
      private final T trigger;
      private final Advancement advancement;
      private final String criterion;

      public Listener(T criteriontriggerinstance, Advancement advancement, String s) {
         this.trigger = criteriontriggerinstance;
         this.advancement = advancement;
         this.criterion = s;
      }

      public T getTriggerInstance() {
         return this.trigger;
      }

      public void run(PlayerAdvancements playeradvancements) {
         playeradvancements.award(this.advancement, this.criterion);
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            CriterionTrigger.Listener<?> criteriontrigger_listener = (CriterionTrigger.Listener)object;
            if (!this.trigger.equals(criteriontrigger_listener.trigger)) {
               return false;
            } else {
               return !this.advancement.equals(criteriontrigger_listener.advancement) ? false : this.criterion.equals(criteriontrigger_listener.criterion);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.trigger.hashCode();
         i = 31 * i + this.advancement.hashCode();
         return 31 * i + this.criterion.hashCode();
      }
   }
}
