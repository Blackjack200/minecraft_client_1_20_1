package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier implements DensityFunctions.BeardifierOrMarker {
   public static final int BEARD_KERNEL_RADIUS = 12;
   private static final int BEARD_KERNEL_SIZE = 24;
   private static final float[] BEARD_KERNEL = Util.make(new float[13824], (afloat) -> {
      for(int i = 0; i < 24; ++i) {
         for(int j = 0; j < 24; ++j) {
            for(int k = 0; k < 24; ++k) {
               afloat[i * 24 * 24 + j * 24 + k] = (float)computeBeardContribution(j - 12, k - 12, i - 12);
            }
         }
      }

   });
   private final ObjectListIterator<Beardifier.Rigid> pieceIterator;
   private final ObjectListIterator<JigsawJunction> junctionIterator;

   public static Beardifier forStructuresInChunk(StructureManager structuremanager, ChunkPos chunkpos) {
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      ObjectList<Beardifier.Rigid> objectlist = new ObjectArrayList<>(10);
      ObjectList<JigsawJunction> objectlist1 = new ObjectArrayList<>(32);
      structuremanager.startsForStructure(chunkpos, (structure) -> structure.terrainAdaptation() != TerrainAdjustment.NONE).forEach((structurestart) -> {
         TerrainAdjustment terrainadjustment = structurestart.getStructure().terrainAdaptation();

         for(StructurePiece structurepiece : structurestart.getPieces()) {
            if (structurepiece.isCloseToChunk(chunkpos, 12)) {
               if (structurepiece instanceof PoolElementStructurePiece) {
                  PoolElementStructurePiece poolelementstructurepiece = (PoolElementStructurePiece)structurepiece;
                  StructureTemplatePool.Projection structuretemplatepool_projection = poolelementstructurepiece.getElement().getProjection();
                  if (structuretemplatepool_projection == StructureTemplatePool.Projection.RIGID) {
                     objectlist.add(new Beardifier.Rigid(poolelementstructurepiece.getBoundingBox(), terrainadjustment, poolelementstructurepiece.getGroundLevelDelta()));
                  }

                  for(JigsawJunction jigsawjunction : poolelementstructurepiece.getJunctions()) {
                     int i1 = jigsawjunction.getSourceX();
                     int j1 = jigsawjunction.getSourceZ();
                     if (i1 > i - 12 && j1 > j - 12 && i1 < i + 15 + 12 && j1 < j + 15 + 12) {
                        objectlist1.add(jigsawjunction);
                     }
                  }
               } else {
                  objectlist.add(new Beardifier.Rigid(structurepiece.getBoundingBox(), terrainadjustment, 0));
               }
            }
         }

      });
      return new Beardifier(objectlist.iterator(), objectlist1.iterator());
   }

   @VisibleForTesting
   public Beardifier(ObjectListIterator<Beardifier.Rigid> objectlistiterator, ObjectListIterator<JigsawJunction> objectlistiterator1) {
      this.pieceIterator = objectlistiterator;
      this.junctionIterator = objectlistiterator1;
   }

   public double compute(DensityFunction.FunctionContext densityfunction_functioncontext) {
      int i = densityfunction_functioncontext.blockX();
      int j = densityfunction_functioncontext.blockY();
      int k = densityfunction_functioncontext.blockZ();

      double d0;
      double var10001;
      for(d0 = 0.0D; this.pieceIterator.hasNext(); d0 += var10001) {
         Beardifier.Rigid beardifier_rigid = this.pieceIterator.next();
         BoundingBox boundingbox = beardifier_rigid.box();
         int l = beardifier_rigid.groundLevelDelta();
         int i1 = Math.max(0, Math.max(boundingbox.minX() - i, i - boundingbox.maxX()));
         int j1 = Math.max(0, Math.max(boundingbox.minZ() - k, k - boundingbox.maxZ()));
         int k1 = boundingbox.minY() + l;
         int l1 = j - k1;
         int var10000;
         switch (beardifier_rigid.terrainAdjustment()) {
            case NONE:
               var10000 = 0;
               break;
            case BURY:
            case BEARD_THIN:
               var10000 = l1;
               break;
            case BEARD_BOX:
               var10000 = Math.max(0, Math.max(k1 - j, j - boundingbox.maxY()));
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         int i2 = var10000;
         switch (beardifier_rigid.terrainAdjustment()) {
            case NONE:
               var10001 = 0.0D;
               break;
            case BURY:
               var10001 = getBuryContribution(i1, i2, j1);
               break;
            case BEARD_THIN:
            case BEARD_BOX:
               var10001 = getBeardContribution(i1, i2, j1, l1) * 0.8D;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }
      }

      this.pieceIterator.back(Integer.MAX_VALUE);

      while(this.junctionIterator.hasNext()) {
         JigsawJunction jigsawjunction = this.junctionIterator.next();
         int j2 = i - jigsawjunction.getSourceX();
         int k2 = j - jigsawjunction.getSourceGroundY();
         int l2 = k - jigsawjunction.getSourceZ();
         d0 += getBeardContribution(j2, k2, l2, k2) * 0.4D;
      }

      this.junctionIterator.back(Integer.MAX_VALUE);
      return d0;
   }

   public double minValue() {
      return Double.NEGATIVE_INFINITY;
   }

   public double maxValue() {
      return Double.POSITIVE_INFINITY;
   }

   private static double getBuryContribution(int i, int j, int k) {
      double d0 = Mth.length((double)i, (double)j / 2.0D, (double)k);
      return Mth.clampedMap(d0, 0.0D, 6.0D, 1.0D, 0.0D);
   }

   private static double getBeardContribution(int i, int j, int k, int l) {
      int i1 = i + 12;
      int j1 = j + 12;
      int k1 = k + 12;
      if (isInKernelRange(i1) && isInKernelRange(j1) && isInKernelRange(k1)) {
         double d0 = (double)l + 0.5D;
         double d1 = Mth.lengthSquared((double)i, d0, (double)k);
         double d2 = -d0 * Mth.fastInvSqrt(d1 / 2.0D) / 2.0D;
         return d2 * (double)BEARD_KERNEL[k1 * 24 * 24 + i1 * 24 + j1];
      } else {
         return 0.0D;
      }
   }

   private static boolean isInKernelRange(int i) {
      return i >= 0 && i < 24;
   }

   private static double computeBeardContribution(int i, int j, int k) {
      return computeBeardContribution(i, (double)j + 0.5D, k);
   }

   private static double computeBeardContribution(int i, double d0, int j) {
      double d1 = Mth.lengthSquared((double)i, d0, (double)j);
      return Math.pow(Math.E, -d1 / 16.0D);
   }

   @VisibleForTesting
   public static record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
   }
}
