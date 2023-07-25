package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class PropertyDispatch {
   private final Map<Selector, List<Variant>> values = Maps.newHashMap();

   protected void putValue(Selector selector, List<Variant> list) {
      List<Variant> list1 = this.values.put(selector, list);
      if (list1 != null) {
         throw new IllegalStateException("Value " + selector + " is already defined");
      }
   }

   Map<Selector, List<Variant>> getEntries() {
      this.verifyComplete();
      return ImmutableMap.copyOf(this.values);
   }

   private void verifyComplete() {
      List<Property<?>> list = this.getDefinedProperties();
      Stream<Selector> stream = Stream.of(Selector.empty());

      for(Property<?> property : list) {
         stream = stream.flatMap((selector1) -> property.getAllValues().map(selector1::extend));
      }

      List<Selector> list1 = stream.filter((selector) -> !this.values.containsKey(selector)).collect(Collectors.toList());
      if (!list1.isEmpty()) {
         throw new IllegalStateException("Missing definition for properties: " + list1);
      }
   }

   abstract List<Property<?>> getDefinedProperties();

   public static <T1 extends Comparable<T1>> PropertyDispatch.C1<T1> property(Property<T1> property) {
      return new PropertyDispatch.C1<>(property);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> PropertyDispatch.C2<T1, T2> properties(Property<T1> property, Property<T2> property1) {
      return new PropertyDispatch.C2<>(property, property1);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> PropertyDispatch.C3<T1, T2, T3> properties(Property<T1> property, Property<T2> property1, Property<T3> property2) {
      return new PropertyDispatch.C3<>(property, property1, property2);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> PropertyDispatch.C4<T1, T2, T3, T4> properties(Property<T1> property, Property<T2> property1, Property<T3> property2, Property<T4> property3) {
      return new PropertyDispatch.C4<>(property, property1, property2, property3);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> PropertyDispatch.C5<T1, T2, T3, T4, T5> properties(Property<T1> property, Property<T2> property1, Property<T3> property2, Property<T4> property3, Property<T5> property4) {
      return new PropertyDispatch.C5<>(property, property1, property2, property3, property4);
   }

   public static class C1<T1 extends Comparable<T1>> extends PropertyDispatch {
      private final Property<T1> property1;

      C1(Property<T1> property) {
         this.property1 = property;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1);
      }

      public PropertyDispatch.C1<T1> select(T1 comparable, List<Variant> list) {
         Selector selector = Selector.of(this.property1.value(comparable));
         this.putValue(selector, list);
         return this;
      }

      public PropertyDispatch.C1<T1> select(T1 comparable, Variant variant) {
         return this.select(comparable, Collections.singletonList(variant));
      }

      public PropertyDispatch generate(Function<T1, Variant> function) {
         this.property1.getPossibleValues().forEach((comparable) -> this.select(comparable, function.apply(comparable)));
         return this;
      }

      public PropertyDispatch generateList(Function<T1, List<Variant>> function) {
         this.property1.getPossibleValues().forEach((comparable) -> this.select(comparable, function.apply(comparable)));
         return this;
      }
   }

   public static class C2<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;

      C2(Property<T1> property, Property<T2> property1) {
         this.property1 = property;
         this.property2 = property1;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2);
      }

      public PropertyDispatch.C2<T1, T2> select(T1 comparable, T2 comparable1, List<Variant> list) {
         Selector selector = Selector.of(this.property1.value(comparable), this.property2.value(comparable1));
         this.putValue(selector, list);
         return this;
      }

      public PropertyDispatch.C2<T1, T2> select(T1 comparable, T2 comparable1, Variant variant) {
         return this.select(comparable, comparable1, Collections.singletonList(variant));
      }

      public PropertyDispatch generate(BiFunction<T1, T2, Variant> bifunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.select((T1)comparable, comparable2, bifunction.apply((T1)comparable, comparable2))));
         return this;
      }

      public PropertyDispatch generateList(BiFunction<T1, T2, List<Variant>> bifunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.select((T1)comparable, comparable2, bifunction.apply((T1)comparable, comparable2))));
         return this;
      }
   }

   public static class C3<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;

      C3(Property<T1> property, Property<T2> property1, Property<T3> property2) {
         this.property1 = property;
         this.property2 = property1;
         this.property3 = property2;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3);
      }

      public PropertyDispatch.C3<T1, T2, T3> select(T1 comparable, T2 comparable1, T3 comparable2, List<Variant> list) {
         Selector selector = Selector.of(this.property1.value(comparable), this.property2.value(comparable1), this.property3.value(comparable2));
         this.putValue(selector, list);
         return this;
      }

      public PropertyDispatch.C3<T1, T2, T3> select(T1 comparable, T2 comparable1, T3 comparable2, Variant variant) {
         return this.select(comparable, comparable1, comparable2, Collections.singletonList(variant));
      }

      public PropertyDispatch generate(PropertyDispatch.TriFunction<T1, T2, T3, Variant> propertydispatch_trifunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.property3.getPossibleValues().forEach((comparable5) -> this.select((T1)comparable, (T2)comparable2, comparable5, propertydispatch_trifunction.apply((T1)comparable, (T2)comparable2, comparable5)))));
         return this;
      }

      public PropertyDispatch generateList(PropertyDispatch.TriFunction<T1, T2, T3, List<Variant>> propertydispatch_trifunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.property3.getPossibleValues().forEach((comparable5) -> this.select((T1)comparable, (T2)comparable2, comparable5, propertydispatch_trifunction.apply((T1)comparable, (T2)comparable2, comparable5)))));
         return this;
      }
   }

   public static class C4<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;
      private final Property<T4> property4;

      C4(Property<T1> property, Property<T2> property1, Property<T3> property2, Property<T4> property3) {
         this.property1 = property;
         this.property2 = property1;
         this.property3 = property2;
         this.property4 = property3;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3, this.property4);
      }

      public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 comparable, T2 comparable1, T3 comparable2, T4 comparable3, List<Variant> list) {
         Selector selector = Selector.of(this.property1.value(comparable), this.property2.value(comparable1), this.property3.value(comparable2), this.property4.value(comparable3));
         this.putValue(selector, list);
         return this;
      }

      public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 comparable, T2 comparable1, T3 comparable2, T4 comparable3, Variant variant) {
         return this.select(comparable, comparable1, comparable2, comparable3, Collections.singletonList(variant));
      }

      public PropertyDispatch generate(PropertyDispatch.QuadFunction<T1, T2, T3, T4, Variant> propertydispatch_quadfunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.property3.getPossibleValues().forEach((comparable5) -> this.property4.getPossibleValues().forEach((comparable9) -> this.select((T1)comparable, (T2)comparable2, (T3)comparable5, comparable9, propertydispatch_quadfunction.apply((T1)comparable, (T2)comparable2, (T3)comparable5, comparable9))))));
         return this;
      }

      public PropertyDispatch generateList(PropertyDispatch.QuadFunction<T1, T2, T3, T4, List<Variant>> propertydispatch_quadfunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.property3.getPossibleValues().forEach((comparable5) -> this.property4.getPossibleValues().forEach((comparable9) -> this.select((T1)comparable, (T2)comparable2, (T3)comparable5, comparable9, propertydispatch_quadfunction.apply((T1)comparable, (T2)comparable2, (T3)comparable5, comparable9))))));
         return this;
      }
   }

   public static class C5<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;
      private final Property<T4> property4;
      private final Property<T5> property5;

      C5(Property<T1> property, Property<T2> property1, Property<T3> property2, Property<T4> property3, Property<T5> property4) {
         this.property1 = property;
         this.property2 = property1;
         this.property3 = property2;
         this.property4 = property3;
         this.property5 = property4;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3, this.property4, this.property5);
      }

      public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 comparable, T2 comparable1, T3 comparable2, T4 comparable3, T5 comparable4, List<Variant> list) {
         Selector selector = Selector.of(this.property1.value(comparable), this.property2.value(comparable1), this.property3.value(comparable2), this.property4.value(comparable3), this.property5.value(comparable4));
         this.putValue(selector, list);
         return this;
      }

      public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 comparable, T2 comparable1, T3 comparable2, T4 comparable3, T5 comparable4, Variant variant) {
         return this.select(comparable, comparable1, comparable2, comparable3, comparable4, Collections.singletonList(variant));
      }

      public PropertyDispatch generate(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, Variant> propertydispatch_pentafunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.property3.getPossibleValues().forEach((comparable5) -> this.property4.getPossibleValues().forEach((comparable9) -> this.property5.getPossibleValues().forEach((comparable14) -> this.select((T1)comparable, (T2)comparable2, (T3)comparable5, (T4)comparable9, comparable14, propertydispatch_pentafunction.apply((T1)comparable, (T2)comparable2, (T3)comparable5, (T4)comparable9, comparable14)))))));
         return this;
      }

      public PropertyDispatch generateList(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, List<Variant>> propertydispatch_pentafunction) {
         this.property1.getPossibleValues().forEach((comparable) -> this.property2.getPossibleValues().forEach((comparable2) -> this.property3.getPossibleValues().forEach((comparable5) -> this.property4.getPossibleValues().forEach((comparable9) -> this.property5.getPossibleValues().forEach((comparable14) -> this.select((T1)comparable, (T2)comparable2, (T3)comparable5, (T4)comparable9, comparable14, propertydispatch_pentafunction.apply((T1)comparable, (T2)comparable2, (T3)comparable5, (T4)comparable9, comparable14)))))));
         return this;
      }
   }

   @FunctionalInterface
   public interface PentaFunction<P1, P2, P3, P4, P5, R> {
      R apply(P1 object, P2 object1, P3 object2, P4 object3, P5 object4);
   }

   @FunctionalInterface
   public interface QuadFunction<P1, P2, P3, P4, R> {
      R apply(P1 object, P2 object1, P3 object2, P4 object3);
   }

   @FunctionalInterface
   public interface TriFunction<P1, P2, P3, R> {
      R apply(P1 object, P2 object1, P3 object2);
   }
}
