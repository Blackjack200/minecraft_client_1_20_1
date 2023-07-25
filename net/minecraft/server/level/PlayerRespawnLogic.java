package net.minecraft.server.level;

import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class PlayerRespawnLogic {
   @Nullable
   protected static BlockPos getOverworldRespawnPos(ServerLevel serverlevel, int i, int j) {
      boolean flag = serverlevel.dimensionType().hasCeiling();
      LevelChunk levelchunk = serverlevel.getChunk(SectionPos.blockToSectionCoord(i), SectionPos.blockToSectionCoord(j));
      int k = flag ? serverlevel.getChunkSource().getGenerator().getSpawnHeight(serverlevel) : levelchunk.getHeight(Heightmap.Types.MOTION_BLOCKING, i & 15, j & 15);
      if (k < serverlevel.getMinBuildHeight()) {
         return null;
      } else {
         int l = levelchunk.getHeight(Heightmap.Types.WORLD_SURFACE, i & 15, j & 15);
         if (l <= k && l > levelchunk.getHeight(Heightmap.Types.OCEAN_FLOOR, i & 15, j & 15)) {
            return null;
         } else {
            BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

            for(int i1 = k + 1; i1 >= serverlevel.getMinBuildHeight(); --i1) {
               blockpos_mutableblockpos.set(i, i1, j);
               BlockState blockstate = serverlevel.getBlockState(blockpos_mutableblockpos);
               if (!blockstate.getFluidState().isEmpty()) {
                  break;
               }

               if (Block.isFaceFull(blockstate.getCollisionShape(serverlevel, blockpos_mutableblockpos), Direction.UP)) {
                  return blockpos_mutableblockpos.above().immutable();
               }
            }

            return null;
         }
      }
   }

   @Nullable
   public static BlockPos getSpawnPosInChunk(ServerLevel serverlevel, ChunkPos chunkpos) {
      if (SharedConstants.debugVoidTerrain(chunkpos)) {
         return null;
      } else {
         for(int i = chunkpos.getMinBlockX(); i <= chunkpos.getMaxBlockX(); ++i) {
            for(int j = chunkpos.getMinBlockZ(); j <= chunkpos.getMaxBlockZ(); ++j) {
               BlockPos blockpos = getOverworldRespawnPos(serverlevel, i, j);
               if (blockpos != null) {
                  return blockpos;
               }
            }
         }

         return null;
      }
   }
}
