package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class KeyValueCondition implements Condition {
   private static final Splitter PIPE_SPLITTER = Splitter.on('|').omitEmptyStrings();
   private final String key;
   private final String value;

   public KeyValueCondition(String s, String s1) {
      this.key = s;
      this.value = s1;
   }

   public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> statedefinition) {
      Property<?> property = statedefinition.getProperty(this.key);
      if (property == null) {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown property '%s' on '%s'", this.key, statedefinition.getOwner()));
      } else {
         String s = this.value;
         boolean flag = !s.isEmpty() && s.charAt(0) == '!';
         if (flag) {
            s = s.substring(1);
         }

         List<String> list = PIPE_SPLITTER.splitToList(s);
         if (list.isEmpty()) {
            throw new RuntimeException(String.format(Locale.ROOT, "Empty value '%s' for property '%s' on '%s'", this.value, this.key, statedefinition.getOwner()));
         } else {
            Predicate<BlockState> predicate;
            if (list.size() == 1) {
               predicate = this.getBlockStatePredicate(statedefinition, property, s);
            } else {
               List<Predicate<BlockState>> list1 = list.stream().map((s1) -> this.getBlockStatePredicate(statedefinition, property, s1)).collect(Collectors.toList());
               predicate = (blockstate) -> list1.stream().anyMatch((predicate2) -> predicate2.test(blockstate));
            }

            return flag ? predicate.negate() : predicate;
         }
      }
   }

   private Predicate<BlockState> getBlockStatePredicate(StateDefinition<Block, BlockState> statedefinition, Property<?> property, String s) {
      Optional<?> optional = property.getValue(s);
      if (!optional.isPresent()) {
         throw new RuntimeException(String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'", s, this.key, statedefinition.getOwner(), this.value));
      } else {
         return (blockstate) -> blockstate.getValue(property).equals(optional.get());
      }
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("key", this.key).add("value", this.value).toString();
   }
}
