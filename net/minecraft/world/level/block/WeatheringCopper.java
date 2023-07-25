package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
   Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> ImmutableBiMap.<Block, Block>builder().put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER).put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER).put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER).put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER).put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER).put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB).put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS).build());
   Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());

   static Optional<Block> getPrevious(Block block) {
      return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(block));
   }

   static Block getFirst(Block block) {
      Block block1 = block;

      for(Block block2 = PREVIOUS_BY_BLOCK.get().get(block); block2 != null; block2 = PREVIOUS_BY_BLOCK.get().get(block2)) {
         block1 = block2;
      }

      return block1;
   }

   static Optional<BlockState> getPrevious(BlockState blockstate) {
      return getPrevious(blockstate.getBlock()).map((block) -> block.withPropertiesOf(blockstate));
   }

   static Optional<Block> getNext(Block block) {
      return Optional.ofNullable(NEXT_BY_BLOCK.get().get(block));
   }

   static BlockState getFirst(BlockState blockstate) {
      return getFirst(blockstate.getBlock()).withPropertiesOf(blockstate);
   }

   default Optional<BlockState> getNext(BlockState blockstate) {
      return getNext(blockstate.getBlock()).map((block) -> block.withPropertiesOf(blockstate));
   }

   default float getChanceModifier() {
      return this.getAge() == WeatheringCopper.WeatherState.UNAFFECTED ? 0.75F : 1.0F;
   }

   public static enum WeatherState {
      UNAFFECTED,
      EXPOSED,
      WEATHERED,
      OXIDIZED;
   }
}
