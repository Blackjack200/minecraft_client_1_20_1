package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class LinearPosTest extends PosRuleTest {
   public static final Codec<LinearPosTest> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter((linearpostest3) -> linearpostest3.minChance), Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter((linearpostest2) -> linearpostest2.maxChance), Codec.INT.fieldOf("min_dist").orElse(0).forGetter((linearpostest1) -> linearpostest1.minDist), Codec.INT.fieldOf("max_dist").orElse(0).forGetter((linearpostest) -> linearpostest.maxDist)).apply(recordcodecbuilder_instance, LinearPosTest::new));
   private final float minChance;
   private final float maxChance;
   private final int minDist;
   private final int maxDist;

   public LinearPosTest(float f, float f1, int i, int j) {
      if (i >= j) {
         throw new IllegalArgumentException("Invalid range: [" + i + "," + j + "]");
      } else {
         this.minChance = f;
         this.maxChance = f1;
         this.minDist = i;
         this.maxDist = j;
      }
   }

   public boolean test(BlockPos blockpos, BlockPos blockpos1, BlockPos blockpos2, RandomSource randomsource) {
      int i = blockpos1.distManhattan(blockpos2);
      float f = randomsource.nextFloat();
      return f <= Mth.clampedLerp(this.minChance, this.maxChance, Mth.inverseLerp((float)i, (float)this.minDist, (float)this.maxDist));
   }

   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.LINEAR_POS_TEST;
   }
}
