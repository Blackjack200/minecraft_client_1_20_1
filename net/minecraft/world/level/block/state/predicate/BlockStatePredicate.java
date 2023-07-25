package net.minecraft.world.level.block.state.predicate;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockStatePredicate implements Predicate<BlockState> {
   public static final Predicate<BlockState> ANY = (blockstate) -> true;
   private final StateDefinition<Block, BlockState> definition;
   private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

   private BlockStatePredicate(StateDefinition<Block, BlockState> statedefinition) {
      this.definition = statedefinition;
   }

   public static BlockStatePredicate forBlock(Block block) {
      return new BlockStatePredicate(block.getStateDefinition());
   }

   public boolean test(@Nullable BlockState blockstate) {
      if (blockstate != null && blockstate.getBlock().equals(this.definition.getOwner())) {
         if (this.properties.isEmpty()) {
            return true;
         } else {
            for(Map.Entry<Property<?>, Predicate<Object>> map_entry : this.properties.entrySet()) {
               if (!this.applies(blockstate, map_entry.getKey(), map_entry.getValue())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   protected <T extends Comparable<T>> boolean applies(BlockState blockstate, Property<T> property, Predicate<Object> predicate) {
      T comparable = blockstate.getValue(property);
      return predicate.test(comparable);
   }

   public <V extends Comparable<V>> BlockStatePredicate where(Property<V> property, Predicate<Object> predicate) {
      if (!this.definition.getProperties().contains(property)) {
         throw new IllegalArgumentException(this.definition + " cannot support property " + property);
      } else {
         this.properties.put(property, predicate);
         return this;
      }
   }
}
