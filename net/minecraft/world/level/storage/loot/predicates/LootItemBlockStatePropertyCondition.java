package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class LootItemBlockStatePropertyCondition implements LootItemCondition {
   final Block block;
   final StatePropertiesPredicate properties;

   LootItemBlockStatePropertyCondition(Block block, StatePropertiesPredicate statepropertiespredicate) {
      this.block = block;
      this.properties = statepropertiespredicate;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.BLOCK_STATE_PROPERTY;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.BLOCK_STATE);
   }

   public boolean test(LootContext lootcontext) {
      BlockState blockstate = lootcontext.getParamOrNull(LootContextParams.BLOCK_STATE);
      return blockstate != null && blockstate.is(this.block) && this.properties.matches(blockstate);
   }

   public static LootItemBlockStatePropertyCondition.Builder hasBlockStateProperties(Block block) {
      return new LootItemBlockStatePropertyCondition.Builder(block);
   }

   public static class Builder implements LootItemCondition.Builder {
      private final Block block;
      private StatePropertiesPredicate properties = StatePropertiesPredicate.ANY;

      public Builder(Block block) {
         this.block = block;
      }

      public LootItemBlockStatePropertyCondition.Builder setProperties(StatePropertiesPredicate.Builder statepropertiespredicate_builder) {
         this.properties = statepropertiespredicate_builder.build();
         return this;
      }

      public LootItemCondition build() {
         return new LootItemBlockStatePropertyCondition(this.block, this.properties);
      }
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemBlockStatePropertyCondition> {
      public void serialize(JsonObject jsonobject, LootItemBlockStatePropertyCondition lootitemblockstatepropertycondition, JsonSerializationContext jsonserializationcontext) {
         jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(lootitemblockstatepropertycondition.block).toString());
         jsonobject.add("properties", lootitemblockstatepropertycondition.properties.serializeToJson());
      }

      public LootItemBlockStatePropertyCondition deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "block"));
         Block block = BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> new IllegalArgumentException("Can't find block " + resourcelocation));
         StatePropertiesPredicate statepropertiespredicate = StatePropertiesPredicate.fromJson(jsonobject.get("properties"));
         statepropertiespredicate.checkState(block.getStateDefinition(), (s) -> {
            throw new JsonSyntaxException("Block " + block + " has no property " + s);
         });
         return new LootItemBlockStatePropertyCondition(block, statepropertiespredicate);
      }
   }
}
