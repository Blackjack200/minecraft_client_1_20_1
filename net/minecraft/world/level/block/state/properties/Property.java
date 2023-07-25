package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
   private final Class<T> clazz;
   private final String name;
   @Nullable
   private Integer hashCode;
   private final Codec<T> codec = Codec.STRING.comapFlatMap((s1) -> this.getValue(s1).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unable to read property: " + this + " with value: " + s1)), this::getName);
   private final Codec<Property.Value<T>> valueCodec = this.codec.xmap(this::value, Property.Value::value);

   protected Property(String s, Class<T> oclass) {
      this.clazz = oclass;
      this.name = s;
   }

   public Property.Value<T> value(T comparable) {
      return new Property.Value<>(this, comparable);
   }

   public Property.Value<T> value(StateHolder<?, ?> stateholder) {
      return new Property.Value<>(this, stateholder.getValue(this));
   }

   public Stream<Property.Value<T>> getAllValues() {
      return this.getPossibleValues().stream().map(this::value);
   }

   public Codec<T> codec() {
      return this.codec;
   }

   public Codec<Property.Value<T>> valueCodec() {
      return this.valueCodec;
   }

   public String getName() {
      return this.name;
   }

   public Class<T> getValueClass() {
      return this.clazz;
   }

   public abstract Collection<T> getPossibleValues();

   public abstract String getName(T comparable);

   public abstract Optional<T> getValue(String s);

   public String toString() {
      return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Property)) {
         return false;
      } else {
         Property<?> property = (Property)object;
         return this.clazz.equals(property.clazz) && this.name.equals(property.name);
      }
   }

   public final int hashCode() {
      if (this.hashCode == null) {
         this.hashCode = this.generateHashCode();
      }

      return this.hashCode;
   }

   public int generateHashCode() {
      return 31 * this.clazz.hashCode() + this.name.hashCode();
   }

   public <U, S extends StateHolder<?, S>> DataResult<S> parseValue(DynamicOps<U> dynamicops, S stateholder, U object) {
      DataResult<T> dataresult = this.codec.parse(dynamicops, object);
      return dataresult.map((comparable) -> stateholder.setValue(this, comparable)).setPartial(stateholder);
   }

   public static record Value<T extends Comparable<T>>(Property<T> property, T value) {
      public Value {
         if (!property.getPossibleValues().contains(comparable)) {
            throw new IllegalArgumentException("Value " + comparable + " does not belong to property " + property);
         }
      }

      public String toString() {
         return this.property.getName() + "=" + this.property.getName(this.value);
      }
   }
}
