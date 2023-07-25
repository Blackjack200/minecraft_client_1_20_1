package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class MatchTool implements LootItemCondition {
   final ItemPredicate predicate;

   public MatchTool(ItemPredicate itempredicate) {
      this.predicate = itempredicate;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.MATCH_TOOL;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   public boolean test(LootContext lootcontext) {
      ItemStack itemstack = lootcontext.getParamOrNull(LootContextParams.TOOL);
      return itemstack != null && this.predicate.matches(itemstack);
   }

   public static LootItemCondition.Builder toolMatches(ItemPredicate.Builder itempredicate_builder) {
      return () -> new MatchTool(itempredicate_builder.build());
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<MatchTool> {
      public void serialize(JsonObject jsonobject, MatchTool matchtool, JsonSerializationContext jsonserializationcontext) {
         jsonobject.add("predicate", matchtool.predicate.serializeToJson());
      }

      public MatchTool deserialize(JsonObject jsonobject, JsonDeserializationContext jsondeserializationcontext) {
         ItemPredicate itempredicate = ItemPredicate.fromJson(jsonobject.get("predicate"));
         return new MatchTool(itempredicate);
      }
   }
}
