package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class AxisAlignedLinearPosTest extends PosRuleTest {
   public static final Codec<AxisAlignedLinearPosTest> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter((axisalignedlinearpostest4) -> axisalignedlinearpostest4.minChance), Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter((axisalignedlinearpostest3) -> axisalignedlinearpostest3.maxChance), Codec.INT.fieldOf("min_dist").orElse(0).forGetter((axisalignedlinearpostest2) -> axisalignedlinearpostest2.minDist), Codec.INT.fieldOf("max_dist").orElse(0).forGetter((axisalignedlinearpostest1) -> axisalignedlinearpostest1.maxDist), Direction.Axis.CODEC.fieldOf("axis").orElse(Direction.Axis.Y).forGetter((axisalignedlinearpostest) -> axisalignedlinearpostest.axis)).apply(recordcodecbuilder_instance, AxisAlignedLinearPosTest::new));
   private final float minChance;
   private final float maxChance;
   private final int minDist;
   private final int maxDist;
   private final Direction.Axis axis;

   public AxisAlignedLinearPosTest(float f, float f1, int i, int j, Direction.Axis direction_axis) {
      if (i >= j) {
         throw new IllegalArgumentException("Invalid range: [" + i + "," + j + "]");
      } else {
         this.minChance = f;
         this.maxChance = f1;
         this.minDist = i;
         this.maxDist = j;
         this.axis = direction_axis;
      }
   }

   public boolean test(BlockPos blockpos, BlockPos blockpos1, BlockPos blockpos2, RandomSource randomsource) {
      Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
      float f = (float)Math.abs((blockpos1.getX() - blockpos2.getX()) * direction.getStepX());
      float f1 = (float)Math.abs((blockpos1.getY() - blockpos2.getY()) * direction.getStepY());
      float f2 = (float)Math.abs((blockpos1.getZ() - blockpos2.getZ()) * direction.getStepZ());
      int i = (int)(f + f1 + f2);
      float f3 = randomsource.nextFloat();
      return f3 <= Mth.clampedLerp(this.minChance, this.maxChance, Mth.inverseLerp((float)i, (float)this.minDist, (float)this.maxDist));
   }

   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
   }
}
