package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Optional;

public class BooleanProperty extends Property<Boolean> {
   private final ImmutableSet<Boolean> values = ImmutableSet.of(true, false);

   protected BooleanProperty(String s) {
      super(s, Boolean.class);
   }

   public Collection<Boolean> getPossibleValues() {
      return this.values;
   }

   public static BooleanProperty create(String s) {
      return new BooleanProperty(s);
   }

   public Optional<Boolean> getValue(String s) {
      return !"true".equals(s) && !"false".equals(s) ? Optional.empty() : Optional.of(Boolean.valueOf(s));
   }

   public String getName(Boolean obool) {
      return obool.toString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof BooleanProperty && super.equals(object)) {
         BooleanProperty booleanproperty = (BooleanProperty)object;
         return this.values.equals(booleanproperty.values);
      } else {
         return false;
      }
   }

   public int generateHashCode() {
      return 31 * super.generateHashCode() + this.values.hashCode();
   }
}
