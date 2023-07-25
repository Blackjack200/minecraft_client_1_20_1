package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;

public class EnumProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {
   private final ImmutableSet<T> values;
   private final Map<String, T> names = Maps.newHashMap();

   protected EnumProperty(String s, Class<T> oclass, Collection<T> collection) {
      super(s, oclass);
      this.values = ImmutableSet.copyOf(collection);

      for(T oenum : collection) {
         String s1 = oenum.getSerializedName();
         if (this.names.containsKey(s1)) {
            throw new IllegalArgumentException("Multiple values have the same name '" + s1 + "'");
         }

         this.names.put(s1, oenum);
      }

   }

   public Collection<T> getPossibleValues() {
      return this.values;
   }

   public Optional<T> getValue(String s) {
      return Optional.ofNullable(this.names.get(s));
   }

   public String getName(T oenum) {
      return oenum.getSerializedName();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof EnumProperty && super.equals(object)) {
         EnumProperty<?> enumproperty = (EnumProperty)object;
         return this.values.equals(enumproperty.values) && this.names.equals(enumproperty.names);
      } else {
         return false;
      }
   }

   public int generateHashCode() {
      int i = super.generateHashCode();
      i = 31 * i + this.values.hashCode();
      return 31 * i + this.names.hashCode();
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String s, Class<T> oclass) {
      return create(s, oclass, (oenum) -> true);
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String s, Class<T> oclass, Predicate<T> predicate) {
      return create(s, oclass, Arrays.<T>stream(oclass.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String s, Class<T> oclass, T... aenum) {
      return create(s, oclass, Lists.newArrayList(aenum));
   }

   public static <T extends Enum<T> & StringRepresentable> EnumProperty<T> create(String s, Class<T> oclass, Collection<T> collection) {
      return new EnumProperty<>(s, oclass, collection);
   }
}
