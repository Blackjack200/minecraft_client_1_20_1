package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMatchTest extends RuleTest {
   public static final Codec<BlockMatchTest> CODEC = BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").xmap(BlockMatchTest::new, (blockmatchtest) -> blockmatchtest.block).codec();
   private final Block block;

   public BlockMatchTest(Block block) {
      this.block = block;
   }

   public boolean test(BlockState blockstate, RandomSource randomsource) {
      return blockstate.is(this.block);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.BLOCK_TEST;
   }
}
