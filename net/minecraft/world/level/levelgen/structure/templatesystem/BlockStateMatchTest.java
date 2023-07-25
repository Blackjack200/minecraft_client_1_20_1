package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateMatchTest extends RuleTest {
   public static final Codec<BlockStateMatchTest> CODEC = BlockState.CODEC.fieldOf("block_state").xmap(BlockStateMatchTest::new, (blockstatematchtest) -> blockstatematchtest.blockState).codec();
   private final BlockState blockState;

   public BlockStateMatchTest(BlockState blockstate) {
      this.blockState = blockstate;
   }

   public boolean test(BlockState blockstate, RandomSource randomsource) {
      return blockstate == this.blockState;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCKSTATE_TEST;
   }
}
