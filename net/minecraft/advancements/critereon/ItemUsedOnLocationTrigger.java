package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class ItemUsedOnLocationTrigger extends SimpleCriterionTrigger<ItemUsedOnLocationTrigger.TriggerInstance> {
   final ResourceLocation id;

   public ItemUsedOnLocationTrigger(ResourceLocation resourcelocation) {
      this.id = resourcelocation;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public ItemUsedOnLocationTrigger.TriggerInstance createInstance(JsonObject jsonobject, ContextAwarePredicate contextawarepredicate, DeserializationContext deserializationcontext) {
      ContextAwarePredicate contextawarepredicate1 = ContextAwarePredicate.fromElement("location", deserializationcontext, jsonobject.get("location"), LootContextParamSets.ADVANCEMENT_LOCATION);
      if (contextawarepredicate1 == null) {
         throw new JsonParseException("Failed to parse 'location' field");
      } else {
         return new ItemUsedOnLocationTrigger.TriggerInstance(this.id, contextawarepredicate, contextawarepredicate1);
      }
   }

   public void trigger(ServerPlayer serverplayer, BlockPos blockpos, ItemStack itemstack) {
      ServerLevel serverlevel = serverplayer.serverLevel();
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      LootParams lootparams = (new LootParams.Builder(serverlevel)).withParameter(LootContextParams.ORIGIN, blockpos.getCenter()).withParameter(LootContextParams.THIS_ENTITY, serverplayer).withParameter(LootContextParams.BLOCK_STATE, blockstate).withParameter(LootContextParams.TOOL, itemstack).create(LootContextParamSets.ADVANCEMENT_LOCATION);
      LootContext lootcontext = (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
      this.trigger(serverplayer, (itemusedonlocationtrigger_triggerinstance) -> itemusedonlocationtrigger_triggerinstance.matches(lootcontext));
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final ContextAwarePredicate location;

      public TriggerInstance(ResourceLocation resourcelocation, ContextAwarePredicate contextawarepredicate, ContextAwarePredicate contextawarepredicate1) {
         super(resourcelocation, contextawarepredicate);
         this.location = contextawarepredicate1;
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(Block block) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, ContextAwarePredicate.ANY, contextawarepredicate);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance placedBlock(LootItemCondition.Builder... alootitemcondition_builder) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(Arrays.stream(alootitemcondition_builder).map(LootItemCondition.Builder::build).toArray((i) -> new LootItemCondition[i]));
         return new ItemUsedOnLocationTrigger.TriggerInstance(CriteriaTriggers.PLACED_BLOCK.id, ContextAwarePredicate.ANY, contextawarepredicate);
      }

      private static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnLocation(LocationPredicate.Builder locationpredicate_builder, ItemPredicate.Builder itempredicate_builder, ResourceLocation resourcelocation) {
         ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.create(LocationCheck.checkLocation(locationpredicate_builder).build(), MatchTool.toolMatches(itempredicate_builder).build());
         return new ItemUsedOnLocationTrigger.TriggerInstance(resourcelocation, ContextAwarePredicate.ANY, contextawarepredicate);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(LocationPredicate.Builder locationpredicate_builder, ItemPredicate.Builder itempredicate_builder) {
         return itemUsedOnLocation(locationpredicate_builder, itempredicate_builder, CriteriaTriggers.ITEM_USED_ON_BLOCK.id);
      }

      public static ItemUsedOnLocationTrigger.TriggerInstance allayDropItemOnBlock(LocationPredicate.Builder locationpredicate_builder, ItemPredicate.Builder itempredicate_builder) {
         return itemUsedOnLocation(locationpredicate_builder, itempredicate_builder, CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.id);
      }

      public boolean matches(LootContext lootcontext) {
         return this.location.matches(lootcontext);
      }

      public JsonObject serializeToJson(SerializationContext serializationcontext) {
         JsonObject jsonobject = super.serializeToJson(serializationcontext);
         jsonobject.add("location", this.location.toJson(serializationcontext));
         return jsonobject;
      }
   }
}
