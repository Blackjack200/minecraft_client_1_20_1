package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndTintGetter extends BlockGetter {
   float getShade(Direction direction, boolean flag);

   LevelLightEngine getLightEngine();

   int getBlockTint(BlockPos blockpos, ColorResolver colorresolver);

   default int getBrightness(LightLayer lightlayer, BlockPos blockpos) {
      return this.getLightEngine().getLayerListener(lightlayer).getLightValue(blockpos);
   }

   default int getRawBrightness(BlockPos blockpos, int i) {
      return this.getLightEngine().getRawBrightness(blockpos, i);
   }

   default boolean canSeeSky(BlockPos blockpos) {
      return this.getBrightness(LightLayer.SKY, blockpos) >= this.getMaxLightLevel();
   }
}
