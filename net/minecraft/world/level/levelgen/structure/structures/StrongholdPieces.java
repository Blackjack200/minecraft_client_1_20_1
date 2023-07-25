package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class StrongholdPieces {
   private static final int SMALL_DOOR_WIDTH = 3;
   private static final int SMALL_DOOR_HEIGHT = 3;
   private static final int MAX_DEPTH = 50;
   private static final int LOWEST_Y_POSITION = 10;
   private static final boolean CHECK_AIR = true;
   public static final int MAGIC_START_Y = 64;
   private static final StrongholdPieces.PieceWeight[] STRONGHOLD_PIECE_WEIGHTS = new StrongholdPieces.PieceWeight[]{new StrongholdPieces.PieceWeight(StrongholdPieces.Straight.class, 40, 0), new StrongholdPieces.PieceWeight(StrongholdPieces.PrisonHall.class, 5, 5), new StrongholdPieces.PieceWeight(StrongholdPieces.LeftTurn.class, 20, 0), new StrongholdPieces.PieceWeight(StrongholdPieces.RightTurn.class, 20, 0), new StrongholdPieces.PieceWeight(StrongholdPieces.RoomCrossing.class, 10, 6), new StrongholdPieces.PieceWeight(StrongholdPieces.StraightStairsDown.class, 5, 5), new StrongholdPieces.PieceWeight(StrongholdPieces.StairsDown.class, 5, 5), new StrongholdPieces.PieceWeight(StrongholdPieces.FiveCrossing.class, 5, 4), new StrongholdPieces.PieceWeight(StrongholdPieces.ChestCorridor.class, 5, 4), new StrongholdPieces.PieceWeight(StrongholdPieces.Library.class, 10, 2) {
      public boolean doPlace(int i) {
         return super.doPlace(i) && i > 4;
      }
   }, new StrongholdPieces.PieceWeight(StrongholdPieces.PortalRoom.class, 20, 1) {
      public boolean doPlace(int i) {
         return super.doPlace(i) && i > 5;
      }
   }};
   private static List<StrongholdPieces.PieceWeight> currentPieces;
   static Class<? extends StrongholdPieces.StrongholdPiece> imposedPiece;
   private static int totalWeight;
   static final StrongholdPieces.SmoothStoneSelector SMOOTH_STONE_SELECTOR = new StrongholdPieces.SmoothStoneSelector();

   public static void resetPieces() {
      currentPieces = Lists.newArrayList();

      for(StrongholdPieces.PieceWeight strongholdpieces_pieceweight : STRONGHOLD_PIECE_WEIGHTS) {
         strongholdpieces_pieceweight.placeCount = 0;
         currentPieces.add(strongholdpieces_pieceweight);
      }

      imposedPiece = null;
   }

   private static boolean updatePieceWeight() {
      boolean flag = false;
      totalWeight = 0;

      for(StrongholdPieces.PieceWeight strongholdpieces_pieceweight : currentPieces) {
         if (strongholdpieces_pieceweight.maxPlaceCount > 0 && strongholdpieces_pieceweight.placeCount < strongholdpieces_pieceweight.maxPlaceCount) {
            flag = true;
         }

         totalWeight += strongholdpieces_pieceweight.weight;
      }

      return flag;
   }

   private static StrongholdPieces.StrongholdPiece findAndCreatePieceFactory(Class<? extends StrongholdPieces.StrongholdPiece> oclass, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, @Nullable Direction direction, int l) {
      StrongholdPieces.StrongholdPiece strongholdpieces_strongholdpiece = null;
      if (oclass == StrongholdPieces.Straight.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.Straight.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.PrisonHall.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.PrisonHall.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.LeftTurn.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.LeftTurn.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.RightTurn.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.RightTurn.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.RoomCrossing.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.RoomCrossing.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.StraightStairsDown.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.StraightStairsDown.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.StairsDown.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.StairsDown.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.FiveCrossing.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.FiveCrossing.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.ChestCorridor.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.ChestCorridor.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.Library.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.Library.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == StrongholdPieces.PortalRoom.class) {
         strongholdpieces_strongholdpiece = StrongholdPieces.PortalRoom.createPiece(structurepieceaccessor, i, j, k, direction, l);
      }

      return strongholdpieces_strongholdpiece;
   }

   private static StrongholdPieces.StrongholdPiece generatePieceFromSmallDoor(StrongholdPieces.StartPiece strongholdpieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
      if (!updatePieceWeight()) {
         return null;
      } else {
         if (imposedPiece != null) {
            StrongholdPieces.StrongholdPiece strongholdpieces_strongholdpiece = findAndCreatePieceFactory(imposedPiece, structurepieceaccessor, randomsource, i, j, k, direction, l);
            imposedPiece = null;
            if (strongholdpieces_strongholdpiece != null) {
               return strongholdpieces_strongholdpiece;
            }
         }

         int i1 = 0;

         while(i1 < 5) {
            ++i1;
            int j1 = randomsource.nextInt(totalWeight);

            for(StrongholdPieces.PieceWeight strongholdpieces_pieceweight : currentPieces) {
               j1 -= strongholdpieces_pieceweight.weight;
               if (j1 < 0) {
                  if (!strongholdpieces_pieceweight.doPlace(l) || strongholdpieces_pieceweight == strongholdpieces_startpiece.previousPiece) {
                     break;
                  }

                  StrongholdPieces.StrongholdPiece strongholdpieces_strongholdpiece1 = findAndCreatePieceFactory(strongholdpieces_pieceweight.pieceClass, structurepieceaccessor, randomsource, i, j, k, direction, l);
                  if (strongholdpieces_strongholdpiece1 != null) {
                     ++strongholdpieces_pieceweight.placeCount;
                     strongholdpieces_startpiece.previousPiece = strongholdpieces_pieceweight;
                     if (!strongholdpieces_pieceweight.isValid()) {
                        currentPieces.remove(strongholdpieces_pieceweight);
                     }

                     return strongholdpieces_strongholdpiece1;
                  }
               }
            }
         }

         BoundingBox boundingbox = StrongholdPieces.FillerCorridor.findPieceBox(structurepieceaccessor, randomsource, i, j, k, direction);
         return boundingbox != null && boundingbox.minY() > 1 ? new StrongholdPieces.FillerCorridor(l, boundingbox, direction) : null;
      }
   }

   static StructurePiece generateAndAddPiece(StrongholdPieces.StartPiece strongholdpieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, @Nullable Direction direction, int l) {
      if (l > 50) {
         return null;
      } else if (Math.abs(i - strongholdpieces_startpiece.getBoundingBox().minX()) <= 112 && Math.abs(k - strongholdpieces_startpiece.getBoundingBox().minZ()) <= 112) {
         StructurePiece structurepiece = generatePieceFromSmallDoor(strongholdpieces_startpiece, structurepieceaccessor, randomsource, i, j, k, direction, l + 1);
         if (structurepiece != null) {
            structurepieceaccessor.addPiece(structurepiece);
            strongholdpieces_startpiece.pendingChildren.add(structurepiece);
         }

         return structurepiece;
      } else {
         return null;
      }
   }

   public static class ChestCorridor extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 5;
      private static final int DEPTH = 7;
      private boolean hasPlacedChest;

      public ChestCorridor(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
      }

      public ChestCorridor(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_CHEST_CORRIDOR, compoundtag);
         this.hasPlacedChest = compoundtag.getBoolean("Chest");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Chest", this.hasPlacedChest);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
      }

      public static StrongholdPieces.ChestCorridor createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 7, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.ChestCorridor(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 4, 6, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 1, 0);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, boundingbox);

         for(int i = 2; i <= 4; ++i) {
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, i, boundingbox);
         }

         if (!this.hasPlacedChest && boundingbox.isInside(this.getWorldPos(3, 2, 3))) {
            this.hasPlacedChest = true;
            this.createChest(worldgenlevel, boundingbox, randomsource, 3, 2, 3, BuiltInLootTables.STRONGHOLD_CORRIDOR);
         }

      }
   }

   public static class FillerCorridor extends StrongholdPieces.StrongholdPiece {
      private final int steps;

      public FillerCorridor(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, i, boundingbox);
         this.setOrientation(direction);
         this.steps = direction != Direction.NORTH && direction != Direction.SOUTH ? boundingbox.getXSpan() : boundingbox.getZSpan();
      }

      public FillerCorridor(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR, compoundtag);
         this.steps = compoundtag.getInt("Steps");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putInt("Steps", this.steps);
      }

      public static BoundingBox findPieceBox(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction) {
         int l = 3;
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 4, direction);
         StructurePiece structurepiece = structurepieceaccessor.findCollisionPiece(boundingbox);
         if (structurepiece == null) {
            return null;
         } else {
            if (structurepiece.getBoundingBox().minY() == boundingbox.minY()) {
               for(int i1 = 2; i1 >= 1; --i1) {
                  boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, i1, direction);
                  if (!structurepiece.getBoundingBox().intersects(boundingbox)) {
                     return BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, i1 + 1, direction);
                  }
               }
            }

            return null;
         }
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         for(int i = 0; i < this.steps; ++i) {
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, i, boundingbox);

            for(int j = 1; j <= 3; ++j) {
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, j, i, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.CAVE_AIR.defaultBlockState(), 1, j, i, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.CAVE_AIR.defaultBlockState(), 2, j, i, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.CAVE_AIR.defaultBlockState(), 3, j, i, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, j, i, boundingbox);
            }

            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, i, boundingbox);
         }

      }
   }

   public static class FiveCrossing extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 10;
      protected static final int HEIGHT = 9;
      protected static final int DEPTH = 11;
      private final boolean leftLow;
      private final boolean leftHigh;
      private final boolean rightLow;
      private final boolean rightHigh;

      public FiveCrossing(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
         this.leftLow = randomsource.nextBoolean();
         this.leftHigh = randomsource.nextBoolean();
         this.rightLow = randomsource.nextBoolean();
         this.rightHigh = randomsource.nextInt(3) > 0;
      }

      public FiveCrossing(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_FIVE_CROSSING, compoundtag);
         this.leftLow = compoundtag.getBoolean("leftLow");
         this.leftHigh = compoundtag.getBoolean("leftHigh");
         this.rightLow = compoundtag.getBoolean("rightLow");
         this.rightHigh = compoundtag.getBoolean("rightHigh");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("leftLow", this.leftLow);
         compoundtag.putBoolean("leftHigh", this.leftHigh);
         compoundtag.putBoolean("rightLow", this.rightLow);
         compoundtag.putBoolean("rightHigh", this.rightHigh);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         int i = 3;
         int j = 5;
         Direction direction = this.getOrientation();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
         }

         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 5, 1);
         if (this.leftLow) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, i, 1);
         }

         if (this.leftHigh) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, j, 7);
         }

         if (this.rightLow) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, i, 1);
         }

         if (this.rightHigh) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, j, 7);
         }

      }

      public static StrongholdPieces.FiveCrossing createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -4, -3, 0, 10, 9, 11, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.FiveCrossing(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 9, 8, 10, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 4, 3, 0);
         if (this.leftLow) {
            this.generateBox(worldgenlevel, boundingbox, 0, 3, 1, 0, 5, 3, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightLow) {
            this.generateBox(worldgenlevel, boundingbox, 9, 3, 1, 9, 5, 3, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.leftHigh) {
            this.generateBox(worldgenlevel, boundingbox, 0, 5, 7, 0, 7, 9, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightHigh) {
            this.generateBox(worldgenlevel, boundingbox, 9, 5, 7, 9, 7, 9, CAVE_AIR, CAVE_AIR, false);
         }

         this.generateBox(worldgenlevel, boundingbox, 5, 1, 10, 7, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 1, 8, 2, 6, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 5, 4, 4, 9, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 8, 1, 5, 8, 4, 9, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 7, 3, 4, 9, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 5, 3, 3, 6, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 7, 7, 1, 8, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 5, 7, 7, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), false);
         this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, boundingbox);
      }
   }

   public static class LeftTurn extends StrongholdPieces.Turn {
      public LeftTurn(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
      }

      public LeftTurn(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_LEFT_TURN, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
         } else {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
         }

      }

      public static StrongholdPieces.LeftTurn createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.LeftTurn(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 4, 4, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateBox(worldgenlevel, boundingbox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
         } else {
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
         }

      }
   }

   public static class Library extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 14;
      protected static final int HEIGHT = 6;
      protected static final int TALL_HEIGHT = 11;
      protected static final int DEPTH = 15;
      private final boolean isTall;

      public Library(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
         this.isTall = boundingbox.getYSpan() > 6;
      }

      public Library(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_LIBRARY, compoundtag);
         this.isTall = compoundtag.getBoolean("Tall");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Tall", this.isTall);
      }

      public static StrongholdPieces.Library createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 14, 11, 15, direction);
         if (!isOkBox(boundingbox) || structurepieceaccessor.findCollisionPiece(boundingbox) != null) {
            boundingbox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 14, 6, 15, direction);
            if (!isOkBox(boundingbox) || structurepieceaccessor.findCollisionPiece(boundingbox) != null) {
               return null;
            }
         }

         return new StrongholdPieces.Library(l, randomsource, boundingbox, direction);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         int i = 11;
         if (!this.isTall) {
            i = 6;
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 13, i - 1, 14, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 4, 1, 0);
         this.generateMaybeBox(worldgenlevel, boundingbox, randomsource, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false);
         int j = 1;
         int k = 12;

         for(int l = 1; l <= 13; ++l) {
            if ((l - 1) % 4 == 0) {
               this.generateBox(worldgenlevel, boundingbox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.generateBox(worldgenlevel, boundingbox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, boundingbox);
               if (this.isTall) {
                  this.generateBox(worldgenlevel, boundingbox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                  this.generateBox(worldgenlevel, boundingbox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               }
            } else {
               this.generateBox(worldgenlevel, boundingbox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               this.generateBox(worldgenlevel, boundingbox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               if (this.isTall) {
                  this.generateBox(worldgenlevel, boundingbox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                  this.generateBox(worldgenlevel, boundingbox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               }
            }
         }

         for(int i1 = 3; i1 < 12; i1 += 2) {
            this.generateBox(worldgenlevel, boundingbox, 3, 1, i1, 4, 3, i1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, i1, 7, 3, i1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 9, 1, i1, 10, 3, i1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
         }

         if (this.isTall) {
            this.generateBox(worldgenlevel, boundingbox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, boundingbox);
            BlockState blockstate = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate1 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(worldgenlevel, boundingbox, 3, 6, 3, 3, 6, 11, blockstate1, blockstate1, false);
            this.generateBox(worldgenlevel, boundingbox, 10, 6, 3, 10, 6, 9, blockstate1, blockstate1, false);
            this.generateBox(worldgenlevel, boundingbox, 4, 6, 2, 9, 6, 2, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 4, 6, 12, 7, 6, 12, blockstate, blockstate, false);
            this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 3, 6, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 3, 6, 12, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 10, 6, 2, boundingbox);

            for(int j1 = 0; j1 <= 2; ++j1) {
               this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 8 + j1, 6, 12 - j1, boundingbox);
               if (j1 != 2) {
                  this.placeBlock(worldgenlevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 8 + j1, 6, 11 - j1, boundingbox);
               }
            }

            BlockState blockstate2 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
            this.placeBlock(worldgenlevel, blockstate2, 10, 1, 13, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 10, 2, 13, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 10, 3, 13, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 10, 4, 13, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 10, 5, 13, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 10, 6, 13, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 10, 7, 13, boundingbox);
            int k1 = 7;
            int l1 = 7;
            BlockState blockstate3 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true));
            this.placeBlock(worldgenlevel, blockstate3, 6, 9, 7, boundingbox);
            BlockState blockstate4 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true));
            this.placeBlock(worldgenlevel, blockstate4, 7, 9, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate3, 6, 8, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate4, 7, 8, 7, boundingbox);
            BlockState blockstate5 = blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            this.placeBlock(worldgenlevel, blockstate5, 6, 7, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate5, 7, 7, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate3, 5, 7, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate4, 8, 7, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate3.setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 6, 7, 6, boundingbox);
            this.placeBlock(worldgenlevel, blockstate3.setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 6, 7, 8, boundingbox);
            this.placeBlock(worldgenlevel, blockstate4.setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 7, 7, 6, boundingbox);
            this.placeBlock(worldgenlevel, blockstate4.setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 7, 7, 8, boundingbox);
            BlockState blockstate6 = Blocks.TORCH.defaultBlockState();
            this.placeBlock(worldgenlevel, blockstate6, 5, 8, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate6, 8, 8, 7, boundingbox);
            this.placeBlock(worldgenlevel, blockstate6, 6, 8, 6, boundingbox);
            this.placeBlock(worldgenlevel, blockstate6, 6, 8, 8, boundingbox);
            this.placeBlock(worldgenlevel, blockstate6, 7, 8, 6, boundingbox);
            this.placeBlock(worldgenlevel, blockstate6, 7, 8, 8, boundingbox);
         }

         this.createChest(worldgenlevel, boundingbox, randomsource, 3, 3, 5, BuiltInLootTables.STRONGHOLD_LIBRARY);
         if (this.isTall) {
            this.placeBlock(worldgenlevel, CAVE_AIR, 12, 9, 1, boundingbox);
            this.createChest(worldgenlevel, boundingbox, randomsource, 12, 8, 1, BuiltInLootTables.STRONGHOLD_LIBRARY);
         }

      }
   }

   static class PieceWeight {
      public final Class<? extends StrongholdPieces.StrongholdPiece> pieceClass;
      public final int weight;
      public int placeCount;
      public final int maxPlaceCount;

      public PieceWeight(Class<? extends StrongholdPieces.StrongholdPiece> oclass, int i, int j) {
         this.pieceClass = oclass;
         this.weight = i;
         this.maxPlaceCount = j;
      }

      public boolean doPlace(int i) {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }

      public boolean isValid() {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }
   }

   public static class PortalRoom extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 11;
      protected static final int HEIGHT = 8;
      protected static final int DEPTH = 16;
      private boolean hasPlacedSpawner;

      public PortalRoom(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, i, boundingbox);
         this.setOrientation(direction);
      }

      public PortalRoom(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, compoundtag);
         this.hasPlacedSpawner = compoundtag.getBoolean("Mob");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Mob", this.hasPlacedSpawner);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         if (structurepiece != null) {
            ((StrongholdPieces.StartPiece)structurepiece).portalRoomPiece = this;
         }

      }

      public static StrongholdPieces.PortalRoom createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 11, 8, 16, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.PortalRoom(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 10, 7, 15, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, StrongholdPieces.StrongholdPiece.SmallDoorType.GRATES, 4, 1, 0);
         int i = 6;
         this.generateBox(worldgenlevel, boundingbox, 1, 6, 1, 1, 6, 14, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 9, 6, 1, 9, 6, 14, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 6, 1, 8, 6, 2, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 6, 14, 8, 6, 14, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 1, 2, 1, 4, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 8, 1, 1, 9, 1, 4, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 8, 7, 1, 12, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         BlockState blockstate = Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true));

         for(int j = 3; j < 14; j += 2) {
            this.generateBox(worldgenlevel, boundingbox, 0, 3, j, 0, 4, j, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 10, 3, j, 10, 4, j, blockstate, blockstate, false);
         }

         for(int k = 2; k < 9; k += 2) {
            this.generateBox(worldgenlevel, boundingbox, k, 3, 15, k, 4, 15, blockstate1, blockstate1, false);
         }

         BlockState blockstate2 = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 5, 6, 1, 7, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 6, 6, 2, 7, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 7, 6, 3, 7, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);

         for(int l = 4; l <= 6; ++l) {
            this.placeBlock(worldgenlevel, blockstate2, l, 1, 4, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, l, 2, 5, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, l, 3, 6, boundingbox);
         }

         BlockState blockstate3 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState blockstate4 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState blockstate5 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState blockstate6 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean flag = true;
         boolean[] aboolean = new boolean[12];

         for(int i1 = 0; i1 < aboolean.length; ++i1) {
            aboolean[i1] = randomsource.nextFloat() > 0.9F;
            flag &= aboolean[i1];
         }

         this.placeBlock(worldgenlevel, blockstate3.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[0])), 4, 3, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[1])), 5, 3, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[2])), 6, 3, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[3])), 4, 3, 12, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[4])), 5, 3, 12, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[5])), 6, 3, 12, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[6])), 3, 3, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[7])), 3, 3, 10, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[8])), 3, 3, 11, boundingbox);
         this.placeBlock(worldgenlevel, blockstate6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[9])), 7, 3, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[10])), 7, 3, 10, boundingbox);
         this.placeBlock(worldgenlevel, blockstate6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[11])), 7, 3, 11, boundingbox);
         if (flag) {
            BlockState blockstate7 = Blocks.END_PORTAL.defaultBlockState();
            this.placeBlock(worldgenlevel, blockstate7, 4, 3, 9, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 5, 3, 9, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 6, 3, 9, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 4, 3, 10, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 5, 3, 10, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 6, 3, 10, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 4, 3, 11, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 5, 3, 11, boundingbox);
            this.placeBlock(worldgenlevel, blockstate7, 6, 3, 11, boundingbox);
         }

         if (!this.hasPlacedSpawner) {
            BlockPos blockpos1 = this.getWorldPos(5, 3, 6);
            if (boundingbox.isInside(blockpos1)) {
               this.hasPlacedSpawner = true;
               worldgenlevel.setBlock(blockpos1, Blocks.SPAWNER.defaultBlockState(), 2);
               BlockEntity blockentity = worldgenlevel.getBlockEntity(blockpos1);
               if (blockentity instanceof SpawnerBlockEntity) {
                  SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
                  spawnerblockentity.setEntityId(EntityType.SILVERFISH, randomsource);
               }
            }
         }

      }
   }

   public static class PrisonHall extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 9;
      protected static final int HEIGHT = 5;
      protected static final int DEPTH = 11;

      public PrisonHall(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
      }

      public PrisonHall(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_PRISON_HALL, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
      }

      public static StrongholdPieces.PrisonHall createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 9, 5, 11, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.PrisonHall(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 8, 4, 10, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 1, 0);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 10, 3, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 1, 4, 3, 1, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 3, 4, 3, 3, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 7, 4, 3, 7, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 9, 4, 3, 9, false, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);

         for(int i = 1; i <= 3; ++i) {
            this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)), 4, i, 4, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), 4, i, 5, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)), 4, i, 6, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), 5, i, 5, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), 6, i, 5, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)).setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), 7, i, 5, boundingbox);
         }

         this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)), 4, 3, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(true)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(true)), 4, 3, 8, boundingbox);
         BlockState blockstate = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
         BlockState blockstate1 = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
         this.placeBlock(worldgenlevel, blockstate, 4, 1, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate1, 4, 2, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate, 4, 1, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate1, 4, 2, 8, boundingbox);
      }
   }

   public static class RightTurn extends StrongholdPieces.Turn {
      public RightTurn(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
      }

      public RightTurn(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_RIGHT_TURN, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
         } else {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
         }

      }

      public static StrongholdPieces.RightTurn createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.RightTurn(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 4, 4, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
         } else {
            this.generateBox(worldgenlevel, boundingbox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
         }

      }
   }

   public static class RoomCrossing extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 11;
      protected static final int HEIGHT = 7;
      protected static final int DEPTH = 11;
      protected final int type;

      public RoomCrossing(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
         this.type = randomsource.nextInt(5);
      }

      public RoomCrossing(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_ROOM_CROSSING, compoundtag);
         this.type = compoundtag.getInt("Type");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putInt("Type", this.type);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 4, 1);
         this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 4);
         this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 4);
      }

      public static StrongholdPieces.RoomCrossing createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -4, -1, 0, 11, 7, 11, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.RoomCrossing(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 10, 6, 10, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 4, 1, 0);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 10, 6, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 4, 0, 3, 6, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 1, 4, 10, 3, 6, CAVE_AIR, CAVE_AIR, false);
         switch (this.type) {
            case 0:
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, boundingbox);
               break;
            case 1:
               for(int i = 0; i < 5; ++i) {
                  this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + i, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + i, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 3, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i, 1, 7, boundingbox);
               }

               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.WATER.defaultBlockState(), 5, 4, 5, boundingbox);
               break;
            case 2:
               for(int j = 1; j <= 9; ++j) {
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, j, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, j, boundingbox);
               }

               for(int k = 1; k <= 9; ++k) {
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), k, 3, 1, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), k, 3, 9, boundingbox);
               }

               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, boundingbox);

               for(int l = 1; l <= 3; ++l) {
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 4, l, 4, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 6, l, 4, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 4, l, 6, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.COBBLESTONE.defaultBlockState(), 6, l, 6, boundingbox);
               }

               this.placeBlock(worldgenlevel, Blocks.WALL_TORCH.defaultBlockState(), 5, 3, 5, boundingbox);

               for(int i1 = 2; i1 <= 8; ++i1) {
                  this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, i1, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, i1, boundingbox);
                  if (i1 <= 3 || i1 >= 7) {
                     this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, i1, boundingbox);
                     this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, i1, boundingbox);
                     this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, i1, boundingbox);
                  }

                  this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, i1, boundingbox);
                  this.placeBlock(worldgenlevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, i1, boundingbox);
               }

               BlockState blockstate = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
               this.placeBlock(worldgenlevel, blockstate, 9, 1, 3, boundingbox);
               this.placeBlock(worldgenlevel, blockstate, 9, 2, 3, boundingbox);
               this.placeBlock(worldgenlevel, blockstate, 9, 3, 3, boundingbox);
               this.createChest(worldgenlevel, boundingbox, randomsource, 3, 4, 8, BuiltInLootTables.STRONGHOLD_CROSSING);
         }

      }
   }

   static class SmoothStoneSelector extends StructurePiece.BlockSelector {
      public void next(RandomSource randomsource, int i, int j, int k, boolean flag) {
         if (flag) {
            float f = randomsource.nextFloat();
            if (f < 0.2F) {
               this.next = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            } else if (f < 0.5F) {
               this.next = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            } else if (f < 0.55F) {
               this.next = Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
            } else {
               this.next = Blocks.STONE_BRICKS.defaultBlockState();
            }
         } else {
            this.next = Blocks.CAVE_AIR.defaultBlockState();
         }

      }
   }

   public static class StairsDown extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 5;
      private final boolean isSource;

      public StairsDown(StructurePieceType structurepiecetype, int i, int j, int k, Direction direction) {
         super(structurepiecetype, i, makeBoundingBox(j, 64, k, direction, 5, 11, 5));
         this.isSource = true;
         this.setOrientation(direction);
         this.entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;
      }

      public StairsDown(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_STAIRS_DOWN, i, boundingbox);
         this.isSource = false;
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
      }

      public StairsDown(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
         this.isSource = compoundtag.getBoolean("Source");
      }

      public StairsDown(CompoundTag compoundtag) {
         this(StructurePieceType.STRONGHOLD_STAIRS_DOWN, compoundtag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Source", this.isSource);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         if (this.isSource) {
            StrongholdPieces.imposedPiece = StrongholdPieces.FiveCrossing.class;
         }

         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
      }

      public static StrongholdPieces.StairsDown createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -7, 0, 5, 11, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.StairsDown(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 10, 4, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 7, 0);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 4);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, boundingbox);
      }
   }

   public static class StartPiece extends StrongholdPieces.StairsDown {
      public StrongholdPieces.PieceWeight previousPiece;
      @Nullable
      public StrongholdPieces.PortalRoom portalRoomPiece;
      public final List<StructurePiece> pendingChildren = Lists.newArrayList();

      public StartPiece(RandomSource randomsource, int i, int j) {
         super(StructurePieceType.STRONGHOLD_START, 0, i, j, getRandomHorizontalDirection(randomsource));
      }

      public StartPiece(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_START, compoundtag);
      }

      public BlockPos getLocatorPosition() {
         return this.portalRoomPiece != null ? this.portalRoomPiece.getLocatorPosition() : super.getLocatorPosition();
      }
   }

   public static class Straight extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 5;
      private static final int DEPTH = 7;
      private final boolean leftChild;
      private final boolean rightChild;

      public Straight(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
         this.leftChild = randomsource.nextInt(2) == 0;
         this.rightChild = randomsource.nextInt(2) == 0;
      }

      public Straight(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT, compoundtag);
         this.leftChild = compoundtag.getBoolean("Left");
         this.rightChild = compoundtag.getBoolean("Right");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Left", this.leftChild);
         compoundtag.putBoolean("Right", this.rightChild);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
         if (this.leftChild) {
            this.generateSmallDoorChildLeft((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 2);
         }

         if (this.rightChild) {
            this.generateSmallDoorChildRight((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 2);
         }

      }

      public static StrongholdPieces.Straight createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -1, 0, 5, 5, 7, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.Straight(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 4, 6, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 1, 0);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 6);
         BlockState blockstate = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
         BlockState blockstate1 = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
         this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, 0.1F, 1, 2, 1, blockstate);
         this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, 0.1F, 3, 2, 1, blockstate1);
         this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, 0.1F, 1, 2, 5, blockstate);
         this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, 0.1F, 3, 2, 5, blockstate1);
         if (this.leftChild) {
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 2, 0, 3, 4, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightChild) {
            this.generateBox(worldgenlevel, boundingbox, 4, 1, 2, 4, 3, 4, CAVE_AIR, CAVE_AIR, false);
         }

      }
   }

   public static class StraightStairsDown extends StrongholdPieces.StrongholdPiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 8;

      public StraightStairsDown(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, i, boundingbox);
         this.setOrientation(direction);
         this.entryDoor = this.randomSmallDoor(randomsource);
      }

      public StraightStairsDown(CompoundTag compoundtag) {
         super(StructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateSmallDoorChildForward((StrongholdPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 1);
      }

      public static StrongholdPieces.StraightStairsDown createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -7, 0, 5, 11, 8, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new StrongholdPieces.StraightStairsDown(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 10, 7, true, randomsource, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, this.entryDoor, 1, 7, 0);
         this.generateSmallDoor(worldgenlevel, randomsource, boundingbox, StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING, 1, 1, 7);
         BlockState blockstate = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);

         for(int i = 0; i < 6; ++i) {
            this.placeBlock(worldgenlevel, blockstate, 1, 6 - i, 1 + i, boundingbox);
            this.placeBlock(worldgenlevel, blockstate, 2, 6 - i, 1 + i, boundingbox);
            this.placeBlock(worldgenlevel, blockstate, 3, 6 - i, 1 + i, boundingbox);
            if (i < 5) {
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - i, 1 + i, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - i, 1 + i, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - i, 1 + i, boundingbox);
            }
         }

      }
   }

   abstract static class StrongholdPiece extends StructurePiece {
      protected StrongholdPieces.StrongholdPiece.SmallDoorType entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;

      protected StrongholdPiece(StructurePieceType structurepiecetype, int i, BoundingBox boundingbox) {
         super(structurepiecetype, i, boundingbox);
      }

      public StrongholdPiece(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
         this.entryDoor = StrongholdPieces.StrongholdPiece.SmallDoorType.valueOf(compoundtag.getString("EntryDoor"));
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         compoundtag.putString("EntryDoor", this.entryDoor.name());
      }

      protected void generateSmallDoor(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox, StrongholdPieces.StrongholdPiece.SmallDoorType strongholdpieces_strongholdpiece_smalldoortype, int i, int j, int k) {
         switch (strongholdpieces_strongholdpiece_smalldoortype) {
            case OPENING:
               this.generateBox(worldgenlevel, boundingbox, i, j, k, i + 3 - 1, j + 3 - 1, k, CAVE_AIR, CAVE_AIR, false);
               break;
            case WOOD_DOOR:
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.OAK_DOOR.defaultBlockState(), i + 1, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, j + 1, k, boundingbox);
               break;
            case GRATES:
               this.placeBlock(worldgenlevel, Blocks.CAVE_AIR.defaultBlockState(), i + 1, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.CAVE_AIR.defaultBlockState(), i + 1, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i + 1, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)).setValue(IronBarsBlock.WEST, Boolean.valueOf(true)), i + 2, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), i + 2, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.EAST, Boolean.valueOf(true)), i + 2, j, k, boundingbox);
               break;
            case IRON_DOOR:
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 2, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_DOOR.defaultBlockState(), i + 1, j, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, j + 1, k, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH), i + 2, j + 1, k + 1, boundingbox);
               this.placeBlock(worldgenlevel, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH), i + 2, j + 1, k - 1, boundingbox);
         }

      }

      protected StrongholdPieces.StrongholdPiece.SmallDoorType randomSmallDoor(RandomSource randomsource) {
         int i = randomsource.nextInt(5);
         switch (i) {
            case 0:
            case 1:
            default:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.OPENING;
            case 2:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.WOOD_DOOR;
            case 3:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.GRATES;
            case 4:
               return StrongholdPieces.StrongholdPiece.SmallDoorType.IRON_DOOR;
         }
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildForward(StrongholdPieces.StartPiece strongholdpieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + i, this.boundingBox.minY() + j, this.boundingBox.minZ() - 1, direction, this.getGenDepth());
               case SOUTH:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + i, this.boundingBox.minY() + j, this.boundingBox.maxZ() + 1, direction, this.getGenDepth());
               case WEST:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + j, this.boundingBox.minZ() + i, direction, this.getGenDepth());
               case EAST:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + j, this.boundingBox.minZ() + i, direction, this.getGenDepth());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildLeft(StrongholdPieces.StartPiece strongholdpieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.WEST, this.getGenDepth());
               case SOUTH:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.WEST, this.getGenDepth());
               case WEST:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth());
               case EAST:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildRight(StrongholdPieces.StartPiece strongholdpieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.EAST, this.getGenDepth());
               case SOUTH:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.EAST, this.getGenDepth());
               case WEST:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
               case EAST:
                  return StrongholdPieces.generateAndAddPiece(strongholdpieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth());
            }
         }

         return null;
      }

      protected static boolean isOkBox(BoundingBox boundingbox) {
         return boundingbox != null && boundingbox.minY() > 10;
      }

      protected static enum SmallDoorType {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }

   public abstract static class Turn extends StrongholdPieces.StrongholdPiece {
      protected static final int WIDTH = 5;
      protected static final int HEIGHT = 5;
      protected static final int DEPTH = 5;

      protected Turn(StructurePieceType structurepiecetype, int i, BoundingBox boundingbox) {
         super(structurepiecetype, i, boundingbox);
      }

      public Turn(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
      }
   }
}
