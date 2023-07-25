package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockMatchTest extends RuleTest {
   public static final Codec<RandomBlockMatchTest> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter((randomblockmatchtest1) -> randomblockmatchtest1.block), Codec.FLOAT.fieldOf("probability").forGetter((randomblockmatchtest) -> randomblockmatchtest.probability)).apply(recordcodecbuilder_instance, RandomBlockMatchTest::new));
   private final Block block;
   private final float probability;

   public RandomBlockMatchTest(Block block, float f) {
      this.block = block;
      this.probability = f;
   }

   public boolean test(BlockState blockstate, RandomSource randomsource) {
      return blockstate.is(this.block) && randomsource.nextFloat() < this.probability;
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.RANDOM_BLOCK_TEST;
   }
}
