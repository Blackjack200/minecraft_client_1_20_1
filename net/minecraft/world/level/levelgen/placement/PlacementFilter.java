package net.minecraft.world.level.levelgen.placement;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public abstract class PlacementFilter extends PlacementModifier {
   public final Stream<BlockPos> getPositions(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos) {
      return this.shouldPlace(placementcontext, randomsource, blockpos) ? Stream.of(blockpos) : Stream.of();
   }

   protected abstract boolean shouldPlace(PlacementContext placementcontext, RandomSource randomsource, BlockPos blockpos);
}
