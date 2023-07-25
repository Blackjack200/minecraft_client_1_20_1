package net.minecraft.world.level.storage.loot.predicates;

public class AllOfCondition extends CompositeLootItemCondition {
   AllOfCondition(LootItemCondition[] alootitemcondition) {
      super(alootitemcondition, LootItemConditions.andConditions(alootitemcondition));
   }

   public LootItemConditionType getType() {
      return LootItemConditions.ALL_OF;
   }

   public static AllOfCondition.Builder allOf(LootItemCondition.Builder... alootitemcondition_builder) {
      return new AllOfCondition.Builder(alootitemcondition_builder);
   }

   public static class Builder extends CompositeLootItemCondition.Builder {
      public Builder(LootItemCondition.Builder... alootitemcondition_builder) {
         super(alootitemcondition_builder);
      }

      public AllOfCondition.Builder and(LootItemCondition.Builder lootitemcondition_builder) {
         this.addTerm(lootitemcondition_builder);
         return this;
      }

      protected LootItemCondition create(LootItemCondition[] alootitemcondition) {
         return new AllOfCondition(alootitemcondition);
      }
   }

   public static class Serializer extends CompositeLootItemCondition.Serializer<AllOfCondition> {
      protected AllOfCondition create(LootItemCondition[] alootitemcondition) {
         return new AllOfCondition(alootitemcondition);
      }
   }
}
