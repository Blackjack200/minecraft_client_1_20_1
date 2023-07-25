package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public interface Condition extends Supplier<JsonElement> {
   void validate(StateDefinition<?, ?> statedefinition);

   static Condition.TerminalCondition condition() {
      return new Condition.TerminalCondition();
   }

   static Condition and(Condition... acondition) {
      return new Condition.CompositeCondition(Condition.Operation.AND, Arrays.asList(acondition));
   }

   static Condition or(Condition... acondition) {
      return new Condition.CompositeCondition(Condition.Operation.OR, Arrays.asList(acondition));
   }

   public static class CompositeCondition implements Condition {
      private final Condition.Operation operation;
      private final List<Condition> subconditions;

      CompositeCondition(Condition.Operation condition_operation, List<Condition> list) {
         this.operation = condition_operation;
         this.subconditions = list;
      }

      public void validate(StateDefinition<?, ?> statedefinition) {
         this.subconditions.forEach((condition) -> condition.validate(statedefinition));
      }

      public JsonElement get() {
         JsonArray jsonarray = new JsonArray();
         this.subconditions.stream().map(Supplier::get).forEach(jsonarray::add);
         JsonObject jsonobject = new JsonObject();
         jsonobject.add(this.operation.id, jsonarray);
         return jsonobject;
      }
   }

   public static enum Operation {
      AND("AND"),
      OR("OR");

      final String id;

      private Operation(String s) {
         this.id = s;
      }
   }

   public static class TerminalCondition implements Condition {
      private final Map<Property<?>, String> terms = Maps.newHashMap();

      private static <T extends Comparable<T>> String joinValues(Property<T> property, Stream<T> stream) {
         return stream.map(property::getName).collect(Collectors.joining("|"));
      }

      private static <T extends Comparable<T>> String getTerm(Property<T> property, T comparable, T[] acomparable) {
         return joinValues(property, Stream.concat(Stream.of(comparable), Stream.of(acomparable)));
      }

      private <T extends Comparable<T>> void putValue(Property<T> property, String s) {
         String s1 = this.terms.put(property, s);
         if (s1 != null) {
            throw new IllegalStateException("Tried to replace " + property + " value from " + s1 + " to " + s);
         }
      }

      public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> property, T comparable) {
         this.putValue(property, property.getName(comparable));
         return this;
      }

      @SafeVarargs
      public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> property, T comparable, T... acomparable) {
         this.putValue(property, getTerm(property, comparable, acomparable));
         return this;
      }

      public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> property, T comparable) {
         this.putValue(property, "!" + property.getName(comparable));
         return this;
      }

      @SafeVarargs
      public final <T extends Comparable<T>> Condition.TerminalCondition negatedTerm(Property<T> property, T comparable, T... acomparable) {
         this.putValue(property, "!" + getTerm(property, comparable, acomparable));
         return this;
      }

      public JsonElement get() {
         JsonObject jsonobject = new JsonObject();
         this.terms.forEach((property, s) -> jsonobject.addProperty(property.getName(), s));
         return jsonobject;
      }

      public void validate(StateDefinition<?, ?> statedefinition) {
         List<Property<?>> list = this.terms.keySet().stream().filter((property) -> statedefinition.getProperty(property.getName()) != property).collect(Collectors.toList());
         if (!list.isEmpty()) {
            throw new IllegalStateException("Properties " + list + " are missing from " + statedefinition);
         }
      }
   }
}
