package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.Direction;

public class DirectionProperty extends EnumProperty<Direction> {
   protected DirectionProperty(String s, Collection<Direction> collection) {
      super(s, Direction.class, collection);
   }

   public static DirectionProperty create(String s) {
      return create(s, (direction) -> true);
   }

   public static DirectionProperty create(String s, Predicate<Direction> predicate) {
      return create(s, Arrays.stream(Direction.values()).filter(predicate).collect(Collectors.toList()));
   }

   public static DirectionProperty create(String s, Direction... adirection) {
      return create(s, Lists.newArrayList(adirection));
   }

   public static DirectionProperty create(String s, Collection<Direction> collection) {
      return new DirectionProperty(s, collection);
   }
}
