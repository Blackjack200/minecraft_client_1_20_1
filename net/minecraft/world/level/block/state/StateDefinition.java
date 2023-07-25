package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<O, S>> {
   static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final O owner;
   private final ImmutableSortedMap<String, Property<?>> propertiesByName;
   private final ImmutableList<S> states;

   protected StateDefinition(Function<O, S> function, O object, StateDefinition.Factory<O, S> statedefinition_factory, Map<String, Property<?>> map) {
      this.owner = object;
      this.propertiesByName = ImmutableSortedMap.copyOf(map);
      Supplier<S> supplier = () -> function.apply(object);
      MapCodec<S> mapcodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

      for(Map.Entry<String, Property<?>> map_entry : this.propertiesByName.entrySet()) {
         mapcodec = appendPropertyCodec(mapcodec, supplier, map_entry.getKey(), map_entry.getValue());
      }

      MapCodec<S> mapcodec1 = mapcodec;
      Map<Map<Property<?>, Comparable<?>>, S> map1 = Maps.newLinkedHashMap();
      List<S> list = Lists.newArrayList();
      Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

      for(Property<?> property : this.propertiesByName.values()) {
         stream = stream.flatMap((list3) -> property.getPossibleValues().stream().map((comparable) -> {
               List<Pair<Property<?>, Comparable<?>>> list5 = Lists.newArrayList(list3);
               list5.add(Pair.of(property, comparable));
               return list5;
            }));
      }

      stream.forEach((list2) -> {
         ImmutableMap<Property<?>, Comparable<?>> immutablemap = list2.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
         S stateholder1 = statedefinition_factory.create(object, immutablemap, mapcodec1);
         map1.put(immutablemap, stateholder1);
         list.add(stateholder1);
      });

      for(S stateholder : list) {
         stateholder.populateNeighbours(map1);
      }

      this.states = ImmutableList.copyOf(list);
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(MapCodec<S> mapcodec, Supplier<S> supplier, String s, Property<T> property) {
      return Codec.mapPair(mapcodec, property.valueCodec().fieldOf(s).orElseGet((s1) -> {
      }, () -> property.value(supplier.get()))).xmap((pair) -> pair.getFirst().setValue(property, pair.getSecond().value()), (stateholder) -> Pair.of(stateholder, property.value(stateholder)));
   }

   public ImmutableList<S> getPossibleStates() {
      return this.states;
   }

   public S any() {
      return this.states.get(0);
   }

   public O getOwner() {
      return this.owner;
   }

   public Collection<Property<?>> getProperties() {
      return this.propertiesByName.values();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
   }

   @Nullable
   public Property<?> getProperty(String s) {
      return this.propertiesByName.get(s);
   }

   public static class Builder<O, S extends StateHolder<O, S>> {
      private final O owner;
      private final Map<String, Property<?>> properties = Maps.newHashMap();

      public Builder(O object) {
         this.owner = object;
      }

      public StateDefinition.Builder<O, S> add(Property<?>... aproperty) {
         for(Property<?> property : aproperty) {
            this.validateProperty(property);
            this.properties.put(property.getName(), property);
         }

         return this;
      }

      private <T extends Comparable<T>> void validateProperty(Property<T> property) {
         String s = property.getName();
         if (!StateDefinition.NAME_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + s);
         } else {
            Collection<T> collection = property.getPossibleValues();
            if (collection.size() <= 1) {
               throw new IllegalArgumentException(this.owner + " attempted use property " + s + " with <= 1 possible values");
            } else {
               for(T comparable : collection) {
                  String s1 = property.getName(comparable);
                  if (!StateDefinition.NAME_PATTERN.matcher(s1).matches()) {
                     throw new IllegalArgumentException(this.owner + " has property: " + s + " with invalidly named value: " + s1);
                  }
               }

               if (this.properties.containsKey(s)) {
                  throw new IllegalArgumentException(this.owner + " has duplicate property: " + s);
               }
            }
         }
      }

      public StateDefinition<O, S> create(Function<O, S> function, StateDefinition.Factory<O, S> statedefinition_factory) {
         return new StateDefinition<>(function, this.owner, statedefinition_factory, this.properties);
      }
   }

   public interface Factory<O, S> {
      S create(O object, ImmutableMap<Property<?>, Comparable<?>> immutablemap, MapCodec<S> mapcodec);
   }
}
