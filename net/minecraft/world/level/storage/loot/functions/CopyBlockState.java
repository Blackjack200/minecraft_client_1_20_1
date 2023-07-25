package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class CopyBlockState extends LootItemConditionalFunction {
   final Block block;
   final Set<Property<?>> properties;

   CopyBlockState(LootItemCondition[] alootitemcondition, Block block, Set<Property<?>> set) {
      super(alootitemcondition);
      this.block = block;
      this.properties = set;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.COPY_STATE;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.BLOCK_STATE);
   }

   protected ItemStack run(ItemStack itemstack, LootContext lootcontext) {
      BlockState blockstate = lootcontext.getParamOrNull(LootContextParams.BLOCK_STATE);
      if (blockstate != null) {
         CompoundTag compoundtag = itemstack.getOrCreateTag();
         CompoundTag compoundtag1;
         if (compoundtag.contains("BlockStateTag", 10)) {
            compoundtag1 = compoundtag.getCompound("BlockStateTag");
         } else {
            compoundtag1 = new CompoundTag();
            compoundtag.put("BlockStateTag", compoundtag1);
         }

         this.properties.stream().filter(blockstate::hasProperty).forEach((property) -> compoundtag1.putString(property.getName(), serialize(blockstate, property)));
      }

      return itemstack;
   }

   public static CopyBlockState.Builder copyState(Block block) {
      return new CopyBlockState.Builder(block);
   }

   private static <T extends Comparable<T>> String serialize(BlockState blockstate, Property<T> property) {
      T comparable = blockstate.getValue(property);
      return property.getName(comparable);
   }

   public static class Builder extends LootItemConditionalFunction.Builder<CopyBlockState.Builder> {
      private final Block block;
      private final Set<Property<?>> properties = Sets.newHashSet();

      Builder(Block block) {
         this.block = block;
      }

      public CopyBlockState.Builder copy(Property<?> property) {
         if (!this.block.getStateDefinition().getProperties().contains(property)) {
            throw new IllegalStateException("Property " + property + " is not present on block " + this.block);
         } else {
            this.properties.add(property);
            return this;
         }
      }

      protected CopyBlockState.Builder getThis() {
         return this;
      }

      public LootItemFunction build() {
         return new CopyBlockState(this.getConditions(), this.block, this.properties);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<CopyBlockState> {
      public void serialize(JsonObject jsonobject, CopyBlockState copyblockstate, JsonSerializationContext jsonserializationcontext) {
         super.serialize(jsonobject, copyblockstate, jsonserializationcontext);
         jsonobject.addProperty("block", BuiltInRegistries.BLOCK.getKey(copyblockstate.block).toString());
         JsonArray jsonarray = new JsonArray();
         copyblockstate.properties.forEach((property) -> jsonarray.add(property.getName()));
         jsonobject.add("properties", jsonarray);
      }

      public CopyBlockState deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext, LootItemCondition[] alootitemcondition) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "block"));
         Block block = BuiltInRegistries.BLOCK.getOptional(resourcelocation).orElseThrow(() -> new IllegalArgumentException("Can't find block " + resourcelocation));
         StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
         Set<Property<?>> set = Sets.newHashSet();
         JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "properties", (JsonArray)null);
         if (jsonarray != null) {
            jsonarray.forEach((jsonelement) -> set.add(statedefinition.getProperty(GsonHelper.convertToString(jsonelement, "property"))));
         }

         return new CopyBlockState(alootitemcondition, block, set);
      }
   }
}
