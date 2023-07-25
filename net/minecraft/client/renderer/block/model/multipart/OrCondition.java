package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class OrCondition implements Condition {
   public static final String TOKEN = "OR";
   private final Iterable<? extends Condition> conditions;

   public OrCondition(Iterable<? extends Condition> iterable) {
      this.conditions = iterable;
   }

   public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> statedefinition) {
      List<Predicate<BlockState>> list = Streams.stream(this.conditions).map((condition) -> condition.getPredicate(statedefinition)).collect(Collectors.toList());
      return (blockstate) -> list.stream().anyMatch((predicate) -> predicate.test(blockstate));
   }
}
