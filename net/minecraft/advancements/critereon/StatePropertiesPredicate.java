package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class StatePropertiesPredicate {
   public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(ImmutableList.of());
   private final List<StatePropertiesPredicate.PropertyMatcher> properties;

   private static StatePropertiesPredicate.PropertyMatcher fromJson(String s, JsonElement jsonelement) {
      if (jsonelement.isJsonPrimitive()) {
         String s1 = jsonelement.getAsString();
         return new StatePropertiesPredicate.ExactPropertyMatcher(s, s1);
      } else {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "value");
         String s2 = jsonobject.has("min") ? getStringOrNull(jsonobject.get("min")) : null;
         String s3 = jsonobject.has("max") ? getStringOrNull(jsonobject.get("max")) : null;
         return (StatePropertiesPredicate.PropertyMatcher)(s2 != null && s2.equals(s3) ? new StatePropertiesPredicate.ExactPropertyMatcher(s, s2) : new StatePropertiesPredicate.RangedPropertyMatcher(s, s2, s3));
      }
   }

   @Nullable
   private static String getStringOrNull(JsonElement jsonelement) {
      return jsonelement.isJsonNull() ? null : jsonelement.getAsString();
   }

   StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> list) {
      this.properties = ImmutableList.copyOf(list);
   }

   public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> statedefinition, S stateholder) {
      for(StatePropertiesPredicate.PropertyMatcher statepropertiespredicate_propertymatcher : this.properties) {
         if (!statepropertiespredicate_propertymatcher.match(statedefinition, stateholder)) {
            return false;
         }
      }

      return true;
   }

   public boolean matches(BlockState blockstate) {
      return this.matches(blockstate.getBlock().getStateDefinition(), blockstate);
   }

   public boolean matches(FluidState fluidstate) {
      return this.matches(fluidstate.getType().getStateDefinition(), fluidstate);
   }

   public void checkState(StateDefinition<?, ?> statedefinition, Consumer<String> consumer) {
      this.properties.forEach((statepropertiespredicate_propertymatcher) -> statepropertiespredicate_propertymatcher.checkState(statedefinition, consumer));
   }

   public static StatePropertiesPredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "properties");
         List<StatePropertiesPredicate.PropertyMatcher> list = Lists.newArrayList();

         for(Map.Entry<String, JsonElement> map_entry : jsonobject.entrySet()) {
            list.add(fromJson(map_entry.getKey(), map_entry.getValue()));
         }

         return new StatePropertiesPredicate(list);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.properties.isEmpty()) {
            this.properties.forEach((statepropertiespredicate_propertymatcher) -> jsonobject.add(statepropertiespredicate_propertymatcher.getName(), statepropertiespredicate_propertymatcher.toJson()));
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private final List<StatePropertiesPredicate.PropertyMatcher> matchers = Lists.newArrayList();

      private Builder() {
      }

      public static StatePropertiesPredicate.Builder properties() {
         return new StatePropertiesPredicate.Builder();
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<?> property, String s) {
         this.matchers.add(new StatePropertiesPredicate.ExactPropertyMatcher(property.getName(), s));
         return this;
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Integer> property, int i) {
         return this.hasProperty(property, Integer.toString(i));
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> property, boolean flag) {
         return this.hasProperty(property, Boolean.toString(flag));
      }

      public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> property, T comparable) {
         return this.hasProperty(property, comparable.getSerializedName());
      }

      public StatePropertiesPredicate build() {
         return new StatePropertiesPredicate(this.matchers);
      }
   }

   static class ExactPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
      private final String value;

      public ExactPropertyMatcher(String s, String s1) {
         super(s);
         this.value = s1;
      }

      protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateholder, Property<T> property) {
         T comparable = stateholder.getValue(property);
         Optional<T> optional = property.getValue(this.value);
         return optional.isPresent() && comparable.compareTo(optional.get()) == 0;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(this.value);
      }
   }

   abstract static class PropertyMatcher {
      private final String name;

      public PropertyMatcher(String s) {
         this.name = s;
      }

      public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> statedefinition, S stateholder) {
         Property<?> property = statedefinition.getProperty(this.name);
         return property == null ? false : this.match(stateholder, property);
      }

      protected abstract <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateholder, Property<T> property);

      public abstract JsonElement toJson();

      public String getName() {
         return this.name;
      }

      public void checkState(StateDefinition<?, ?> statedefinition, Consumer<String> consumer) {
         Property<?> property = statedefinition.getProperty(this.name);
         if (property == null) {
            consumer.accept(this.name);
         }

      }
   }

   static class RangedPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
      @Nullable
      private final String minValue;
      @Nullable
      private final String maxValue;

      public RangedPropertyMatcher(String s, @Nullable String s1, @Nullable String s2) {
         super(s);
         this.minValue = s1;
         this.maxValue = s2;
      }

      protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateholder, Property<T> property) {
         T comparable = stateholder.getValue(property);
         if (this.minValue != null) {
            Optional<T> optional = property.getValue(this.minValue);
            if (!optional.isPresent() || comparable.compareTo(optional.get()) < 0) {
               return false;
            }
         }

         if (this.maxValue != null) {
            Optional<T> optional1 = property.getValue(this.maxValue);
            if (!optional1.isPresent() || comparable.compareTo(optional1.get()) > 0) {
               return false;
            }
         }

         return true;
      }

      public JsonElement toJson() {
         JsonObject jsonobject = new JsonObject();
         if (this.minValue != null) {
            jsonobject.addProperty("min", this.minValue);
         }

         if (this.maxValue != null) {
            jsonobject.addProperty("max", this.maxValue);
         }

         return jsonobject;
      }
   }
}
