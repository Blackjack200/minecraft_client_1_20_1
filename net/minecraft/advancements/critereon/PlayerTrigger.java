package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerTrigger extends SimpleCriterionTrigger<PlayerTrigger.TriggerInstance> {
   final ResourceLocation id;

   public PlayerTrigger(ResourceLocation resourcelocation) {
      this.id = resourcelocation;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public PlayerTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      return new PlayerTrigger.TriggerInstance(this.id, contextawarepredicate);
   }

   public void trigger(ServerPlayer serverplayer) {
      this.trigger(serverplayer, (playertrigger_triggerinstance) -> true);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ResourceLocation resourcelocation, ContextAwarePredicate contextawarepredicate) {
         super(resourcelocation, contextawarepredicate);
      }

      public static PlayerTrigger.TriggerInstance located(LocationPredicate locationpredicate) {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(EntityPredicate.Builder.entity().located(locationpredicate).build()));
      }

      public static PlayerTrigger.TriggerInstance located(EntityPredicate entitypredicate) {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.wrap(entitypredicate));
      }

      public static PlayerTrigger.TriggerInstance sleptInBed() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance raidWon() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance avoidVibration() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.AVOID_VIBRATION.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance tick() {
         return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.id, ContextAwarePredicate.ANY);
      }

      public static PlayerTrigger.TriggerInstance walkOnBlockWithEquipment(Block block, Item item) {
         return located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item).build()).build()).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()).build()).build());
      }
   }
}
