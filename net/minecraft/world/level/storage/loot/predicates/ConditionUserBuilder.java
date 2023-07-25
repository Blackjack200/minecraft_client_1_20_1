package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
   T when(LootItemCondition.Builder lootitemcondition_builder);

   default <E> T when(Iterable<E> iterable, Function<E, LootItemCondition.Builder> function) {
      T conditionuserbuilder = this.unwrap();

      for(E object : iterable) {
         conditionuserbuilder = conditionuserbuilder.when(function.apply(object));
      }

      return conditionuserbuilder;
   }

   T unwrap();
}
