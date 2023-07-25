package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChunkSkyLightSources {
   private static final int SIZE = 16;
   public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
   private final int minY;
   private final BitStorage heightmap;
   private final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
   private final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();

   public ChunkSkyLightSources(LevelHeightAccessor levelheightaccessor) {
      this.minY = levelheightaccessor.getMinBuildHeight() - 1;
      int i = levelheightaccessor.getMaxBuildHeight();
      int j = Mth.ceillog2(i - this.minY + 1);
      this.heightmap = new SimpleBitStorage(j, 256);
   }

   public void fillFrom(ChunkAccess chunkaccess) {
      int i = chunkaccess.getHighestFilledSectionIndex();
      if (i == -1) {
         this.fill(this.minY);
      } else {
         for(int j = 0; j < 16; ++j) {
            for(int k = 0; k < 16; ++k) {
               int l = Math.max(this.findLowestSourceY(chunkaccess, i, k, j), this.minY);
               this.set(index(k, j), l);
            }
         }

      }
   }

   private int findLowestSourceY(ChunkAccess chunkaccess, int i, int j, int k) {
      int l = SectionPos.sectionToBlockCoord(chunkaccess.getSectionYFromSectionIndex(i) + 1);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = this.mutablePos1.set(j, l, k);
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = this.mutablePos2.setWithOffset(blockpos_mutableblockpos, Direction.DOWN);
      BlockState blockstate = Blocks.AIR.defaultBlockState();

      for(int i1 = i; i1 >= 0; --i1) {
         LevelChunkSection levelchunksection = chunkaccess.getSection(i1);
         if (levelchunksection.hasOnlyAir()) {
            blockstate = Blocks.AIR.defaultBlockState();
            int j1 = chunkaccess.getSectionYFromSectionIndex(i1);
            blockpos_mutableblockpos.setY(SectionPos.sectionToBlockCoord(j1));
            blockpos_mutableblockpos1.setY(blockpos_mutableblockpos.getY() - 1);
         } else {
            for(int k1 = 15; k1 >= 0; --k1) {
               BlockState blockstate1 = levelchunksection.getBlockState(j, k1, k);
               if (isEdgeOccluded(chunkaccess, blockpos_mutableblockpos, blockstate, blockpos_mutableblockpos1, blockstate1)) {
                  return blockpos_mutableblockpos.getY();
               }

               blockstate = blockstate1;
               blockpos_mutableblockpos.set(blockpos_mutableblockpos1);
               blockpos_mutableblockpos1.move(Direction.DOWN);
            }
         }
      }

      return this.minY;
   }

   public boolean update(BlockGetter blockgetter, int i, int j, int k) {
      int l = j + 1;
      int i1 = index(i, k);
      int j1 = this.get(i1);
      if (l < j1) {
         return false;
      } else {
         BlockPos blockpos = this.mutablePos1.set(i, j + 1, k);
         BlockState blockstate = blockgetter.getBlockState(blockpos);
         BlockPos blockpos1 = this.mutablePos2.set(i, j, k);
         BlockState blockstate1 = blockgetter.getBlockState(blockpos1);
         if (this.updateEdge(blockgetter, i1, j1, blockpos, blockstate, blockpos1, blockstate1)) {
            return true;
         } else {
            BlockPos blockpos2 = this.mutablePos1.set(i, j - 1, k);
            BlockState blockstate2 = blockgetter.getBlockState(blockpos2);
            return this.updateEdge(blockgetter, i1, j1, blockpos1, blockstate1, blockpos2, blockstate2);
         }
      }
   }

   private boolean updateEdge(BlockGetter blockgetter, int i, int j, BlockPos blockpos, BlockState blockstate, BlockPos blockpos1, BlockState blockstate1) {
      int k = blockpos.getY();
      if (isEdgeOccluded(blockgetter, blockpos, blockstate, blockpos1, blockstate1)) {
         if (k > j) {
            this.set(i, k);
            return true;
         }
      } else if (k == j) {
         this.set(i, this.findLowestSourceBelow(blockgetter, blockpos1, blockstate1));
         return true;
      }

      return false;
   }

   private int findLowestSourceBelow(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = this.mutablePos1.set(blockpos);
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = this.mutablePos2.setWithOffset(blockpos, Direction.DOWN);
      BlockState blockstate1 = blockstate;

      while(blockpos_mutableblockpos1.getY() >= this.minY) {
         BlockState blockstate2 = blockgetter.getBlockState(blockpos_mutableblockpos1);
         if (isEdgeOccluded(blockgetter, blockpos_mutableblockpos, blockstate1, blockpos_mutableblockpos1, blockstate2)) {
            return blockpos_mutableblockpos.getY();
         }

         blockstate1 = blockstate2;
         blockpos_mutableblockpos.set(blockpos_mutableblockpos1);
         blockpos_mutableblockpos1.move(Direction.DOWN);
      }

      return this.minY;
   }

   private static boolean isEdgeOccluded(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, BlockPos blockpos1, BlockState blockstate1) {
      if (blockstate1.getLightBlock(blockgetter, blockpos1) != 0) {
         return true;
      } else {
         VoxelShape voxelshape = LightEngine.getOcclusionShape(blockgetter, blockpos, blockstate, Direction.DOWN);
         VoxelShape voxelshape1 = LightEngine.getOcclusionShape(blockgetter, blockpos1, blockstate1, Direction.UP);
         return Shapes.faceShapeOccludes(voxelshape, voxelshape1);
      }
   }

   public int getLowestSourceY(int i, int j) {
      int k = this.get(index(i, j));
      return this.extendSourcesBelowWorld(k);
   }

   public int getHighestLowestSourceY() {
      int i = Integer.MIN_VALUE;

      for(int j = 0; j < this.heightmap.getSize(); ++j) {
         int k = this.heightmap.get(j);
         if (k > i) {
            i = k;
         }
      }

      return this.extendSourcesBelowWorld(i + this.minY);
   }

   private void fill(int i) {
      int j = i - this.minY;

      for(int k = 0; k < this.heightmap.getSize(); ++k) {
         this.heightmap.set(k, j);
      }

   }

   private void set(int i, int j) {
      this.heightmap.set(i, j - this.minY);
   }

   private int get(int i) {
      return this.heightmap.get(i) + this.minY;
   }

   private int extendSourcesBelowWorld(int i) {
      return i == this.minY ? Integer.MIN_VALUE : i;
   }

   private static int index(int i, int j) {
      return i + j * 16;
   }
}
