package net.minecraft.world.level.storage.loot.predicates;

public class AnyOfCondition extends CompositeLootItemCondition {
   AnyOfCondition(LootItemCondition[] alootitemcondition) {
      super(alootitemcondition, LootItemConditions.orConditions(alootitemcondition));
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ANY_OF;
   }

   public static AnyOfCondition.Builder anyOf(LootItemCondition.Builder... alootitemcondition_builder) {
      return new AnyOfCondition.Builder(alootitemcondition_builder);
   }

   public static class Builder extends CompositeLootItemCondition.Builder {
      public Builder(LootItemCondition.Builder... alootitemcondition_builder) {
         super(alootitemcondition_builder);
      }

      public AnyOfCondition.Builder or(LootItemCondition.Builder lootitemcondition_builder) {
         this.addTerm(lootitemcondition_builder);
         return this;
      }

      protected LootItemCondition create(LootItemCondition[] alootitemcondition) {
         return new AnyOfCondition(alootitemcondition);
      }
   }

   public static class Serializer extends CompositeLootItemCondition.Serializer<AnyOfCondition> {
      protected AnyOfCondition create(LootItemCondition[] alootitemcondition) {
         return new AnyOfCondition(alootitemcondition);
      }
   }
}
