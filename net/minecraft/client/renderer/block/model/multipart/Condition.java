package net.minecraft.client.renderer.block.model.multipart;

import java.util.function.Predicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@FunctionalInterface
public interface Condition {
   Condition TRUE = (statedefinition) -> (blockstate) -> true;
   Condition FALSE = (statedefinition) -> (blockstate) -> false;

   Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> statedefinition);
}
