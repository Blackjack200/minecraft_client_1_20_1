package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;

public interface WritableLevelData extends LevelData {
   void setXSpawn(int i);

   void setYSpawn(int i);

   void setZSpawn(int i);

   void setSpawnAngle(float f);

   default void setSpawn(BlockPos blockpos, float f) {
      this.setXSpawn(blockpos.getX());
      this.setYSpawn(blockpos.getY());
      this.setZSpawn(blockpos.getZ());
      this.setSpawnAngle(f);
   }
}
