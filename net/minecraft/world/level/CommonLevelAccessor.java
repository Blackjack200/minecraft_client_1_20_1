package net.minecraft.world.level;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
   default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockpos, BlockEntityType<T> blockentitytype) {
      return LevelReader.super.getBlockEntity(blockpos, blockentitytype);
   }

   default List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aabb) {
      return EntityGetter.super.getEntityCollisions(entity, aabb);
   }

   default boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelshape) {
      return EntityGetter.super.isUnobstructed(entity, voxelshape);
   }

   default BlockPos getHeightmapPos(Heightmap.Types heightmap_types, BlockPos blockpos) {
      return LevelReader.super.getHeightmapPos(heightmap_types, blockpos);
   }
}
