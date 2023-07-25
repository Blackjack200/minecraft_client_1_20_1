package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;

public class StructurePiecesBuilder implements StructurePieceAccessor {
   private final List<StructurePiece> pieces = Lists.newArrayList();

   public void addPiece(StructurePiece structurepiece) {
      this.pieces.add(structurepiece);
   }

   @Nullable
   public StructurePiece findCollisionPiece(BoundingBox boundingbox) {
      return StructurePiece.findCollisionPiece(this.pieces, boundingbox);
   }

   /** @deprecated */
   @Deprecated
   public void offsetPiecesVertically(int i) {
      for(StructurePiece structurepiece : this.pieces) {
         structurepiece.move(0, i, 0);
      }

   }

   /** @deprecated */
   @Deprecated
   public int moveBelowSeaLevel(int i, int j, RandomSource randomsource, int k) {
      int l = i - k;
      BoundingBox boundingbox = this.getBoundingBox();
      int i1 = boundingbox.getYSpan() + j + 1;
      if (i1 < l) {
         i1 += randomsource.nextInt(l - i1);
      }

      int j1 = i1 - boundingbox.maxY();
      this.offsetPiecesVertically(j1);
      return j1;
   }

   /** @deprecated */
   public void moveInsideHeights(RandomSource randomsource, int i, int j) {
      BoundingBox boundingbox = this.getBoundingBox();
      int k = j - i + 1 - boundingbox.getYSpan();
      int l;
      if (k > 1) {
         l = i + randomsource.nextInt(k);
      } else {
         l = i;
      }

      int j1 = l - boundingbox.minY();
      this.offsetPiecesVertically(j1);
   }

   public PiecesContainer build() {
      return new PiecesContainer(this.pieces);
   }

   public void clear() {
      this.pieces.clear();
   }

   public boolean isEmpty() {
      return this.pieces.isEmpty();
   }

   public BoundingBox getBoundingBox() {
      return StructurePiece.createBoundingBox(this.pieces.stream());
   }
}
