package net.minecraft.world.level.levelgen.placement;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class RepeatingPlacement extends PlacementModifier {
   protected abstract int count(RandomSource randomsource, BlockPos blockpos);

   public Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      return IntStream.range(0, this.count(randomsource, blockpos)).mapToObj((i) -> blockpos);
   }
}
