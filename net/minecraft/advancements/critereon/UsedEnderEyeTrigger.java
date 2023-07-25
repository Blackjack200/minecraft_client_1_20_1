package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
   static final ResourceLocation ID = new ResourceLocation("used_ender_eye");

   public ResourceLocation getId() {
      return ID;
   }

   public UsedEnderEyeTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      MinMaxBounds.Doubles minmaxbounds_doubles = MinMaxBounds.Doubles.fromJson(jsonobject.get("distance"));
      return new UsedEnderEyeTrigger.TriggerInstance(contextawarepredicate, minmaxbounds_doubles);
   }

   public void trigger(ServerPlayer serverplayer, BlockPos blockpos) {
      double d0 = serverplayer.getX() - (double)blockpos.getX();
      double d1 = serverplayer.getZ() - (double)blockpos.getZ();
      double d2 = d0 * d0 + d1 * d1;
      this.trigger(serverplayer, (usedendereyetrigger_triggerinstance) -> usedendereyetrigger_triggerinstance.matches(d2));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MinMaxBounds.Doubles level;

      public TriggerInstance(ContextAwarePredicate contextawarepredicate, MinMaxBounds.Doubles minmaxbounds_doubles) {
         super(UsedEnderEyeTrigger.ID, contextawarepredicate);
         this.level = minmaxbounds_doubles;
      }

      public boolean matches(double d0) {
         return this.level.matchesSqr(d0);
      }
   }
}
