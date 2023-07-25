package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;

public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
   T apply(LootItemFunction.Builder lootitemfunction_builder);

   default <E> T apply(Iterable<E> iterable, Function<E, LootItemFunction.Builder> function) {
      T functionuserbuilder = this.unwrap();

      for(E object : iterable) {
         functionuserbuilder = functionuserbuilder.apply(function.apply(object));
      }

      return functionuserbuilder;
   }

   default <E> T apply(E[] aobject, Function<E, LootItemFunction.Builder> function) {
      return this.apply(Arrays.asList(aobject), function);
   }

   T unwrap();
}
