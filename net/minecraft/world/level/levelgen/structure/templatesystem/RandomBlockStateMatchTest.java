package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockStateMatchTest extends RuleTest {
   public static final Codec<RandomBlockStateMatchTest> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BlockState.CODEC.fieldOf("block_state").forGetter((randomblockstatematchtest1) -> randomblockstatematchtest1.blockState), Codec.FLOAT.fieldOf("probability").forGetter((randomblockstatematchtest) -> randomblockstatematchtest.probability)).apply(recordcodecbuilder_instance, RandomBlockStateMatchTest::new));
   private final BlockState blockState;
   private final float probability;

   public RandomBlockStateMatchTest(BlockState blockstate, float f) {
      this.blockState = blockstate;
      this.probability = f;
   }

   public boolean test(BlockState blockstate, RandomSource randomsource) {
      return blockstate == this.blockState && randomsource.nextFloat() < this.probability;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.RANDOM_BLOCKSTATE_TEST;
   }
}
