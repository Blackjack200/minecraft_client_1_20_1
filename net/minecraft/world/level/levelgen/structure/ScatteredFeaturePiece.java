package net.minecraft.world.level.levelgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public abstract class ScatteredFeaturePiece extends StructurePiece {
   protected final int width;
   protected final int height;
   protected final int depth;
   protected int heightPosition = -1;

   protected ScatteredFeaturePiece(StructurePieceType structurepiecetype, int i, int j, int k, int l, int i1, int j1, Direction direction) {
      super(structurepiecetype, 0, StructurePiece.makeBoundingBox(i, j, k, direction, l, i1, j1));
      this.width = l;
      this.height = i1;
      this.depth = j1;
      this.setOrientation(direction);
   }

   protected ScatteredFeaturePiece(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
      super(structurepiecetype, compoundtag);
      this.width = compoundtag.getInt("Width");
      this.height = compoundtag.getInt("Height");
      this.depth = compoundtag.getInt("Depth");
      this.heightPosition = compoundtag.getInt("HPos");
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      compoundtag.putInt("Width", this.width);
      compoundtag.putInt("Height", this.height);
      compoundtag.putInt("Depth", this.depth);
      compoundtag.putInt("HPos", this.heightPosition);
   }

   protected boolean updateAverageGroundHeight(LevelAccessor levelaccessor, BoundingBox boundingbox, int i) {
      if (this.heightPosition >= 0) {
         return true;
      } else {
         int j = 0;
         int k = 0;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int l = this.boundingBox.minZ(); l <= this.boundingBox.maxZ(); ++l) {
            for(int i1 = this.boundingBox.minX(); i1 <= this.boundingBox.maxX(); ++i1) {
               blockpos_mutableblockpos.set(i1, 64, l);
               if (boundingbox.isInside(blockpos_mutableblockpos)) {
                  j += levelaccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos_mutableblockpos).getY();
                  ++k;
               }
            }
         }

         if (k == 0) {
            return false;
         } else {
            this.heightPosition = j / k;
            this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + i, 0);
            return true;
         }
      }
   }

   protected boolean updateHeightPositionToLowestGroundHeight(LevelAccessor levelaccessor, int i) {
      if (this.heightPosition >= 0) {
         return true;
      } else {
         int j = levelaccessor.getMaxBuildHeight();
         boolean flag = false;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); ++k) {
            for(int l = this.boundingBox.minX(); l <= this.boundingBox.maxX(); ++l) {
               blockpos_mutableblockpos.set(l, 0, k);
               j = Math.min(j, levelaccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos_mutableblockpos).getY());
               flag = true;
            }
         }

         if (!flag) {
            return false;
         } else {
            this.heightPosition = j;
            this.boundingBox.move(0, this.heightPosition - this.boundingBox.minY() + i, 0);
            return true;
         }
      }
   }
}
