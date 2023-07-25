package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public interface PositionalRandomFactory {
   default RandomSource at(BlockPos blockpos) {
      return this.at(blockpos.getX(), blockpos.getY(), blockpos.getZ());
   }

   default RandomSource fromHashOf(ResourceLocation resourcelocation) {
      return this.fromHashOf(resourcelocation.toString());
   }

   RandomSource fromHashOf(String s);

   RandomSource at(int i, int j, int k);

   @VisibleForTesting
   void parityConfigString(StringBuilder stringbuilder);
}
