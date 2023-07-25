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
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class NetherFortressPieces {
   private static final int MAX_DEPTH = 30;
   private static final int LOWEST_Y_POSITION = 10;
   public static final int MAGIC_START_Y = 64;
   static final NetherFortressPieces.PieceWeight[] BRIDGE_PIECE_WEIGHTS = new NetherFortressPieces.PieceWeight[]{new NetherFortressPieces.PieceWeight(NetherFortressPieces.BridgeStraight.class, 30, 0, true), new NetherFortressPieces.PieceWeight(NetherFortressPieces.BridgeCrossing.class, 10, 4), new NetherFortressPieces.PieceWeight(NetherFortressPieces.RoomCrossing.class, 10, 4), new NetherFortressPieces.PieceWeight(NetherFortressPieces.StairsRoom.class, 10, 3), new NetherFortressPieces.PieceWeight(NetherFortressPieces.MonsterThrone.class, 5, 2), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleEntrance.class, 5, 1)};
   static final NetherFortressPieces.PieceWeight[] CASTLE_PIECE_WEIGHTS = new NetherFortressPieces.PieceWeight[]{new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorPiece.class, 25, 0, true), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorCrossingPiece.class, 15, 5), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class, 5, 10), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class, 5, 10), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleCorridorStairsPiece.class, 10, 3, true), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleCorridorTBalconyPiece.class, 7, 2), new NetherFortressPieces.PieceWeight(NetherFortressPieces.CastleStalkRoom.class, 5, 2)};

   static NetherFortressPieces.NetherBridgePiece findAndCreateBridgePieceFactory(NetherFortressPieces.PieceWeight netherfortresspieces_pieceweight, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
      Class<? extends NetherFortressPieces.NetherBridgePiece> oclass = netherfortresspieces_pieceweight.pieceClass;
      NetherFortressPieces.NetherBridgePiece netherfortresspieces_netherbridgepiece = null;
      if (oclass == NetherFortressPieces.BridgeStraight.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.BridgeStraight.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.BridgeCrossing.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.BridgeCrossing.createPiece(structurepieceaccessor, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.RoomCrossing.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.RoomCrossing.createPiece(structurepieceaccessor, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.StairsRoom.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.StairsRoom.createPiece(structurepieceaccessor, i, j, k, l, direction);
      } else if (oclass == NetherFortressPieces.MonsterThrone.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.MonsterThrone.createPiece(structurepieceaccessor, i, j, k, l, direction);
      } else if (oclass == NetherFortressPieces.CastleEntrance.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleEntrance.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorPiece.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleSmallCorridorPiece.createPiece(structurepieceaccessor, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorRightTurnPiece.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleSmallCorridorRightTurnPiece.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleSmallCorridorLeftTurnPiece.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleCorridorStairsPiece.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleCorridorStairsPiece.createPiece(structurepieceaccessor, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleCorridorTBalconyPiece.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleCorridorTBalconyPiece.createPiece(structurepieceaccessor, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleSmallCorridorCrossingPiece.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleSmallCorridorCrossingPiece.createPiece(structurepieceaccessor, i, j, k, direction, l);
      } else if (oclass == NetherFortressPieces.CastleStalkRoom.class) {
         netherfortresspieces_netherbridgepiece = NetherFortressPieces.CastleStalkRoom.createPiece(structurepieceaccessor, i, j, k, direction, l);
      }

      return netherfortresspieces_netherbridgepiece;
   }

   public static class BridgeCrossing extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 19;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 19;

      public BridgeCrossing(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, i, boundingbox);
         this.setOrientation(direction);
      }

      protected BridgeCrossing(int i, int j, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0, StructurePiece.makeBoundingBox(i, 64, j, direction, 19, 10, 19));
         this.setOrientation(direction);
      }

      protected BridgeCrossing(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
      }

      public BridgeCrossing(CompoundTag compoundtag) {
         this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 8, 3, false);
         this.generateChildLeft((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 3, 8, false);
         this.generateChildRight((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 3, 8, false);
      }

      public static NetherFortressPieces.BridgeCrossing createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -8, -3, 0, 19, 10, 19, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.BridgeCrossing(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 7; i <= 11; ++i) {
            for(int j = 0; j <= 2; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, boundingbox);
            }
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int k = 0; k <= 2; ++k) {
            for(int l = 7; l <= 11; ++l) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), k, -1, l, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - k, -1, l, boundingbox);
            }
         }

      }
   }

   public static class BridgeEndFiller extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 8;
      private final int selfSeed;

      public BridgeEndFiller(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, i, boundingbox);
         this.setOrientation(direction);
         this.selfSeed = randomsource.nextInt();
      }

      public BridgeEndFiller(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, compoundtag);
         this.selfSeed = compoundtag.getInt("Seed");
      }

      public static NetherFortressPieces.BridgeEndFiller createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -3, 0, 5, 10, 8, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.BridgeEndFiller(l, randomsource, boundingbox, direction) : null;
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putInt("Seed", this.selfSeed);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         RandomSource randomsource1 = RandomSource.create((long)this.selfSeed);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 3; j <= 4; ++j) {
               int k = randomsource1.nextInt(8);
               this.generateBox(worldgenlevel, boundingbox, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }
         }

         int l = randomsource1.nextInt(8);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 0, 5, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         l = randomsource1.nextInt(8);
         this.generateBox(worldgenlevel, boundingbox, 4, 5, 0, 4, 5, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int j1 = 0; j1 <= 4; ++j1) {
            int k1 = randomsource1.nextInt(5);
            this.generateBox(worldgenlevel, boundingbox, j1, 2, 0, j1, 2, k1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         }

         for(int l1 = 0; l1 <= 4; ++l1) {
            for(int i2 = 0; i2 <= 1; ++i2) {
               int j2 = randomsource1.nextInt(3);
               this.generateBox(worldgenlevel, boundingbox, l1, i2, 0, l1, i2, j2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }
         }

      }
   }

   public static class BridgeStraight extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 10;
      private static final int DEPTH = 19;

      public BridgeStraight(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, i, boundingbox);
         this.setOrientation(direction);
      }

      public BridgeStraight(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 3, false);
      }

      public static NetherFortressPieces.BridgeStraight createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -3, 0, 5, 10, 19, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.BridgeStraight(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 2; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, boundingbox);
            }
         }

         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate1 = blockstate.setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate2 = blockstate.setValue(FenceBlock.WEST, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 1, 0, 4, 1, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 4, 0, 4, 4, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 14, 0, 4, 14, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 17, 0, 4, 17, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 1, 4, 4, 1, blockstate2, blockstate2, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 4, 4, 4, 4, blockstate2, blockstate2, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 14, 4, 4, 14, blockstate2, blockstate2, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 17, 4, 4, 17, blockstate2, blockstate2, false);
      }
   }

   public static class CastleCorridorStairsPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 10;

      public CastleCorridorStairsPiece(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, i, boundingbox);
         this.setOrientation(direction);
      }

      public CastleCorridorStairsPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 0, true);
      }

      public static NetherFortressPieces.CastleCorridorStairsPiece createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, -7, 0, 5, 14, 10, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleCorridorStairsPiece(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         BlockState blockstate = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

         for(int i = 0; i <= 9; ++i) {
            int j = Math.max(1, 7 - i);
            int k = Math.min(Math.max(j + 5, 14 - i), 13);
            int l = i;
            this.generateBox(worldgenlevel, boundingbox, 0, 0, i, 4, j, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 1, j + 1, i, 3, k - 1, i, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            if (i <= 6) {
               this.placeBlock(worldgenlevel, blockstate, 1, j + 1, i, boundingbox);
               this.placeBlock(worldgenlevel, blockstate, 2, j + 1, i, boundingbox);
               this.placeBlock(worldgenlevel, blockstate, 3, j + 1, i, boundingbox);
            }

            this.generateBox(worldgenlevel, boundingbox, 0, k, i, 4, k, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 0, j + 1, i, 0, k - 1, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, 4, j + 1, i, 4, k - 1, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            if ((i & 1) == 0) {
               this.generateBox(worldgenlevel, boundingbox, 0, j + 2, i, 0, j + 3, i, blockstate1, blockstate1, false);
               this.generateBox(worldgenlevel, boundingbox, 4, j + 2, i, 4, j + 3, i, blockstate1, blockstate1, false);
            }

            for(int i1 = 0; i1 <= 4; ++i1) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i1, -1, l, boundingbox);
            }
         }

      }
   }

   public static class CastleCorridorTBalconyPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 9;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 9;

      public CastleCorridorTBalconyPiece(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, i, boundingbox);
         this.setOrientation(direction);
      }

      public CastleCorridorTBalconyPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         int i = 1;
         Direction direction = this.getOrientation();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 5;
         }

         this.generateChildLeft((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, i, randomsource.nextInt(8) > 0);
         this.generateChildRight((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, i, randomsource.nextInt(8) > 0);
      }

      public static NetherFortressPieces.CastleCorridorTBalconyPiece createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -3, 0, 0, 9, 7, 9, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleCorridorTBalconyPiece(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 0, 1, 4, 0, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 3, 0, 7, 4, 0, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 8, 7, 3, 8, blockstate1, blockstate1, false);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 0, 3, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 8, 3, 8, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 6, 0, 3, 7, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 8, 3, 6, 8, 3, 7, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 5, 1, 5, 5, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 4, 5, 7, 5, 5, blockstate1, blockstate1, false);

         for(int i = 0; i <= 5; ++i) {
            for(int j = 0; j <= 8; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), j, -1, i, boundingbox);
            }
         }

      }
   }

   public static class CastleEntrance extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 13;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 13;

      public CastleEntrance(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, i, boundingbox);
         this.setOrientation(direction);
      }

      public CastleEntrance(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 5, 3, true);
      }

      public static NetherFortressPieces.CastleEntrance createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -5, -3, 0, 13, 14, 13, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleEntrance(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));

         for(int i = 1; i <= 11; i += 2) {
            this.generateBox(worldgenlevel, boundingbox, i, 10, 0, i, 11, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, i, 10, 12, i, 11, 12, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 10, i, 0, 11, i, blockstate1, blockstate1, false);
            this.generateBox(worldgenlevel, boundingbox, 12, 10, i, 12, 11, i, blockstate1, blockstate1, false);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, boundingbox);
            if (i != 11) {
               this.placeBlock(worldgenlevel, blockstate, i + 1, 13, 0, boundingbox);
               this.placeBlock(worldgenlevel, blockstate, i + 1, 13, 12, boundingbox);
               this.placeBlock(worldgenlevel, blockstate1, 0, 13, i + 1, boundingbox);
               this.placeBlock(worldgenlevel, blockstate1, 12, 13, i + 1, boundingbox);
            }
         }

         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 0, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 0, boundingbox);

         for(int j = 3; j <= 9; j += 2) {
            this.generateBox(worldgenlevel, boundingbox, 1, 7, j, 1, 8, j, blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), false);
            this.generateBox(worldgenlevel, boundingbox, 11, 7, j, 11, 8, j, blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), false);
         }

         this.generateBox(worldgenlevel, boundingbox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int k = 4; k <= 8; ++k) {
            for(int l = 0; l <= 2; ++l) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), k, -1, l, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), k, -1, 12 - l, boundingbox);
            }
         }

         for(int i1 = 0; i1 <= 2; ++i1) {
            for(int j1 = 4; j1 <= 8; ++j1) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i1, -1, j1, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i1, -1, j1, boundingbox);
            }
         }

         this.generateBox(worldgenlevel, boundingbox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.LAVA.defaultBlockState(), 6, 5, 6, boundingbox);
         BlockPos blockpos1 = this.getWorldPos(6, 5, 6);
         if (boundingbox.isInside(blockpos1)) {
            worldgenlevel.scheduleTick(blockpos1, Fluids.LAVA, 0);
         }

      }
   }

   public static class CastleSmallCorridorCrossingPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;

      public CastleSmallCorridorCrossingPiece(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, i, boundingbox);
         this.setOrientation(direction);
      }

      public CastleSmallCorridorCrossingPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 0, true);
         this.generateChildLeft((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, 1, true);
         this.generateChildRight((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, 1, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorCrossingPiece createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorCrossingPiece(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   public static class CastleSmallCorridorLeftTurnPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;
      private boolean isNeedingChest;

      public CastleSmallCorridorLeftTurnPiece(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, i, boundingbox);
         this.setOrientation(direction);
         this.isNeedingChest = randomsource.nextInt(3) == 0;
      }

      public CastleSmallCorridorLeftTurnPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, compoundtag);
         this.isNeedingChest = compoundtag.getBoolean("Chest");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Chest", this.isNeedingChest);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildLeft((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, 1, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorLeftTurnPiece createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorLeftTurnPiece(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 1, 4, 4, 1, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 3, 4, 4, 3, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 4, 1, 4, 4, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 3, 4, 3, 4, 4, blockstate, blockstate, false);
         if (this.isNeedingChest && boundingbox.isInside(this.getWorldPos(3, 2, 3))) {
            this.isNeedingChest = false;
            this.createChest(worldgenlevel, boundingbox, randomsource, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   public static class CastleSmallCorridorPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;

      public CastleSmallCorridorPiece(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, i, boundingbox);
         this.setOrientation(direction);
      }

      public CastleSmallCorridorPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 1, 0, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorPiece createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorPiece(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 1, 0, 4, 1, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 3, 0, 4, 3, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 1, 4, 4, 1, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 3, 4, 4, 3, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   public static class CastleSmallCorridorRightTurnPiece extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 5;
      private static final int HEIGHT = 7;
      private static final int DEPTH = 5;
      private boolean isNeedingChest;

      public CastleSmallCorridorRightTurnPiece(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, i, boundingbox);
         this.setOrientation(direction);
         this.isNeedingChest = randomsource.nextInt(3) == 0;
      }

      public CastleSmallCorridorRightTurnPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, compoundtag);
         this.isNeedingChest = compoundtag.getBoolean("Chest");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Chest", this.isNeedingChest);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildRight((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, 1, true);
      }

      public static NetherFortressPieces.CastleSmallCorridorRightTurnPiece createPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleSmallCorridorRightTurnPiece(l, randomsource, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 1, 0, 4, 1, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 3, 0, 4, 3, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 4, 1, 4, 4, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 3, 4, 3, 4, 4, blockstate, blockstate, false);
         if (this.isNeedingChest && boundingbox.isInside(this.getWorldPos(1, 2, 3))) {
            this.isNeedingChest = false;
            this.createChest(worldgenlevel, boundingbox, randomsource, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int i = 0; i <= 4; ++i) {
            for(int j = 0; j <= 4; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   public static class CastleStalkRoom extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 13;
      private static final int HEIGHT = 14;
      private static final int DEPTH = 13;

      public CastleStalkRoom(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, i, boundingbox);
         this.setOrientation(direction);
      }

      public CastleStalkRoom(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 5, 3, true);
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 5, 11, true);
      }

      public static NetherFortressPieces.CastleStalkRoom createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -5, -3, 0, 13, 14, 13, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.CastleStalkRoom(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate2 = blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true));
         BlockState blockstate3 = blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true));

         for(int i = 1; i <= 11; i += 2) {
            this.generateBox(worldgenlevel, boundingbox, i, 10, 0, i, 11, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, i, 10, 12, i, 11, 12, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 10, i, 0, 11, i, blockstate1, blockstate1, false);
            this.generateBox(worldgenlevel, boundingbox, 12, 10, i, 12, 11, i, blockstate1, blockstate1, false);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, boundingbox);
            if (i != 11) {
               this.placeBlock(worldgenlevel, blockstate, i + 1, 13, 0, boundingbox);
               this.placeBlock(worldgenlevel, blockstate, i + 1, 13, 12, boundingbox);
               this.placeBlock(worldgenlevel, blockstate1, 0, 13, i + 1, boundingbox);
               this.placeBlock(worldgenlevel, blockstate1, 12, 13, i + 1, boundingbox);
            }
         }

         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 0, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 0, 13, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 12, 13, 0, boundingbox);

         for(int j = 3; j <= 9; j += 2) {
            this.generateBox(worldgenlevel, boundingbox, 1, 7, j, 1, 8, j, blockstate2, blockstate2, false);
            this.generateBox(worldgenlevel, boundingbox, 11, 7, j, 11, 8, j, blockstate3, blockstate3, false);
         }

         BlockState blockstate4 = Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);

         for(int k = 0; k <= 6; ++k) {
            int l = k + 4;

            for(int i1 = 5; i1 <= 7; ++i1) {
               this.placeBlock(worldgenlevel, blockstate4, i1, 5 + k, l, boundingbox);
            }

            if (l >= 5 && l <= 8) {
               this.generateBox(worldgenlevel, boundingbox, 5, 5, l, 7, k + 4, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            } else if (l >= 9 && l <= 10) {
               this.generateBox(worldgenlevel, boundingbox, 5, 8, l, 7, k + 4, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }

            if (k >= 1) {
               this.generateBox(worldgenlevel, boundingbox, 5, 6 + k, l, 7, 9 + k, l, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }
         }

         for(int j1 = 5; j1 <= 7; ++j1) {
            this.placeBlock(worldgenlevel, blockstate4, j1, 12, 11, boundingbox);
         }

         this.generateBox(worldgenlevel, boundingbox, 5, 6, 7, 5, 7, 7, blockstate3, blockstate3, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 6, 7, 7, 7, 7, blockstate2, blockstate2, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate5 = blockstate4.setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate6 = blockstate4.setValue(StairBlock.FACING, Direction.WEST);
         this.placeBlock(worldgenlevel, blockstate6, 4, 5, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate6, 4, 5, 3, boundingbox);
         this.placeBlock(worldgenlevel, blockstate6, 4, 5, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate6, 4, 5, 10, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5, 8, 5, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5, 8, 5, 3, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5, 8, 5, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5, 8, 5, 10, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);

         for(int k1 = 4; k1 <= 8; ++k1) {
            for(int l1 = 0; l1 <= 2; ++l1) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), k1, -1, l1, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), k1, -1, 12 - l1, boundingbox);
            }
         }

         for(int i2 = 0; i2 <= 2; ++i2) {
            for(int j2 = 4; j2 <= 8; ++j2) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i2, -1, j2, boundingbox);
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i2, -1, j2, boundingbox);
            }
         }

      }
   }

   public static class MonsterThrone extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 8;
      private static final int DEPTH = 9;
      private boolean hasPlacedSpawner;

      public MonsterThrone(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, i, boundingbox);
         this.setOrientation(direction);
      }

      public MonsterThrone(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, compoundtag);
         this.hasPlacedSpawner = compoundtag.getBoolean("Mob");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("Mob", this.hasPlacedSpawner);
      }

      public static NetherFortressPieces.MonsterThrone createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, int l, Direction direction) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -2, 0, 0, 7, 8, 9, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.MonsterThrone(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 1, 6, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 5, 6, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 0, 6, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 6, 6, 3, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 0, 6, 4, 0, 6, 7, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 6, 4, 6, 6, 7, blockstate1, blockstate1, false);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 0, 6, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 6, 6, 8, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 1, 6, 8, 5, 6, 8, blockstate, blockstate, false);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 1, 7, 8, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 2, 7, 8, 4, 7, 8, blockstate, blockstate, false);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 5, 7, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true)), 2, 8, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate, 3, 8, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)), 4, 8, 8, boundingbox);
         if (!this.hasPlacedSpawner) {
            BlockPos blockpos1 = this.getWorldPos(3, 5, 5);
            if (boundingbox.isInside(blockpos1)) {
               this.hasPlacedSpawner = true;
               worldgenlevel.setBlock(blockpos1, Blocks.SPAWNER.defaultBlockState(), 2);
               BlockEntity blockentity = worldgenlevel.getBlockEntity(blockpos1);
               if (blockentity instanceof SpawnerBlockEntity) {
                  SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
                  spawnerblockentity.setEntityId(EntityType.BLAZE, randomsource);
               }
            }
         }

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   abstract static class NetherBridgePiece extends StructurePiece {
      protected NetherBridgePiece(StructurePieceType structurepiecetype, int i, BoundingBox boundingbox) {
         super(structurepiecetype, i, boundingbox);
      }

      public NetherBridgePiece(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      }

      private int updatePieceWeight(List<NetherFortressPieces.PieceWeight> list) {
         boolean flag = false;
         int i = 0;

         for(NetherFortressPieces.PieceWeight netherfortresspieces_pieceweight : list) {
            if (netherfortresspieces_pieceweight.maxPlaceCount > 0 && netherfortresspieces_pieceweight.placeCount < netherfortresspieces_pieceweight.maxPlaceCount) {
               flag = true;
            }

            i += netherfortresspieces_pieceweight.weight;
         }

         return flag ? i : -1;
      }

      private NetherFortressPieces.NetherBridgePiece generatePiece(NetherFortressPieces.StartPiece netherfortresspieces_startpiece, List<NetherFortressPieces.PieceWeight> list, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
         int i1 = this.updatePieceWeight(list);
         boolean flag = i1 > 0 && l <= 30;
         int j1 = 0;

         while(j1 < 5 && flag) {
            ++j1;
            int k1 = randomsource.nextInt(i1);

            for(NetherFortressPieces.PieceWeight netherfortresspieces_pieceweight : list) {
               k1 -= netherfortresspieces_pieceweight.weight;
               if (k1 < 0) {
                  if (!netherfortresspieces_pieceweight.doPlace(l) || netherfortresspieces_pieceweight == netherfortresspieces_startpiece.previousPiece && !netherfortresspieces_pieceweight.allowInRow) {
                     break;
                  }

                  NetherFortressPieces.NetherBridgePiece netherfortresspieces_netherbridgepiece = NetherFortressPieces.findAndCreateBridgePieceFactory(netherfortresspieces_pieceweight, structurepieceaccessor, randomsource, i, j, k, direction, l);
                  if (netherfortresspieces_netherbridgepiece != null) {
                     ++netherfortresspieces_pieceweight.placeCount;
                     netherfortresspieces_startpiece.previousPiece = netherfortresspieces_pieceweight;
                     if (!netherfortresspieces_pieceweight.isValid()) {
                        list.remove(netherfortresspieces_pieceweight);
                     }

                     return netherfortresspieces_netherbridgepiece;
                  }
               }
            }
         }

         return NetherFortressPieces.BridgeEndFiller.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
      }

      private StructurePiece generateAndAddPiece(NetherFortressPieces.StartPiece netherfortresspieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, @Nullable Direction direction, int l, boolean flag) {
         if (Math.abs(i - netherfortresspieces_startpiece.getBoundingBox().minX()) <= 112 && Math.abs(k - netherfortresspieces_startpiece.getBoundingBox().minZ()) <= 112) {
            List<NetherFortressPieces.PieceWeight> list = netherfortresspieces_startpiece.availableBridgePieces;
            if (flag) {
               list = netherfortresspieces_startpiece.availableCastlePieces;
            }

            StructurePiece structurepiece = this.generatePiece(netherfortresspieces_startpiece, list, structurepieceaccessor, randomsource, i, j, k, direction, l + 1);
            if (structurepiece != null) {
               structurepieceaccessor.addPiece(structurepiece);
               netherfortresspieces_startpiece.pendingChildren.add(structurepiece);
            }

            return structurepiece;
         } else {
            return NetherFortressPieces.BridgeEndFiller.createPiece(structurepieceaccessor, randomsource, i, j, k, direction, l);
         }
      }

      @Nullable
      protected StructurePiece generateChildForward(NetherFortressPieces.StartPiece netherfortresspieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, boolean flag) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + i, this.boundingBox.minY() + j, this.boundingBox.minZ() - 1, direction, this.getGenDepth(), flag);
               case SOUTH:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + i, this.boundingBox.minY() + j, this.boundingBox.maxZ() + 1, direction, this.getGenDepth(), flag);
               case WEST:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + j, this.boundingBox.minZ() + i, direction, this.getGenDepth(), flag);
               case EAST:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + j, this.boundingBox.minZ() + i, direction, this.getGenDepth(), flag);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateChildLeft(NetherFortressPieces.StartPiece netherfortresspieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, boolean flag) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.WEST, this.getGenDepth(), flag);
               case SOUTH:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.WEST, this.getGenDepth(), flag);
               case WEST:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), flag);
               case EAST:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), flag);
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateChildRight(NetherFortressPieces.StartPiece netherfortresspieces_startpiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, boolean flag) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.EAST, this.getGenDepth(), flag);
               case SOUTH:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.EAST, this.getGenDepth(), flag);
               case WEST:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), flag);
               case EAST:
                  return this.generateAndAddPiece(netherfortresspieces_startpiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), flag);
            }
         }

         return null;
      }

      protected static boolean isOkBox(BoundingBox boundingbox) {
         return boundingbox != null && boundingbox.minY() > 10;
      }
   }

   static class PieceWeight {
      public final Class<? extends NetherFortressPieces.NetherBridgePiece> pieceClass;
      public final int weight;
      public int placeCount;
      public final int maxPlaceCount;
      public final boolean allowInRow;

      public PieceWeight(Class<? extends NetherFortressPieces.NetherBridgePiece> oclass, int i, int j, boolean flag) {
         this.pieceClass = oclass;
         this.weight = i;
         this.maxPlaceCount = j;
         this.allowInRow = flag;
      }

      public PieceWeight(Class<? extends NetherFortressPieces.NetherBridgePiece> oclass, int i, int j) {
         this(oclass, i, j, false);
      }

      public boolean doPlace(int i) {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }

      public boolean isValid() {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }
   }

   public static class RoomCrossing extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 9;
      private static final int DEPTH = 7;

      public RoomCrossing(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, i, boundingbox);
         this.setOrientation(direction);
      }

      public RoomCrossing(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildForward((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 2, 0, false);
         this.generateChildLeft((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, 2, false);
         this.generateChildRight((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 0, 2, false);
      }

      public static NetherFortressPieces.RoomCrossing createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, Direction direction, int l) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -2, 0, 0, 7, 9, 7, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.RoomCrossing(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 0, 4, 5, 0, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 6, 4, 5, 6, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 5, 2, 0, 5, 4, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 5, 2, 6, 5, 4, blockstate1, blockstate1, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   public static class StairsRoom extends NetherFortressPieces.NetherBridgePiece {
      private static final int WIDTH = 7;
      private static final int HEIGHT = 11;
      private static final int DEPTH = 7;

      public StairsRoom(int i, BoundingBox boundingbox, Direction direction) {
         super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, i, boundingbox);
         this.setOrientation(direction);
      }

      public StairsRoom(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, compoundtag);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         this.generateChildRight((NetherFortressPieces.StartPiece)structurepiece, structurepieceaccessor, randomsource, 6, 2, false);
      }

      public static NetherFortressPieces.StairsRoom createPiece(StructurePieceAccessor structurepieceaccessor, int i, int j, int k, int l, Direction direction) {
         BoundingBox boundingbox = BoundingBox.orientBox(i, j, k, -2, 0, 0, 7, 11, 7, direction);
         return isOkBox(boundingbox) && structurepieceaccessor.findCollisionPiece(boundingbox) == null ? new NetherFortressPieces.StairsRoom(l, boundingbox, direction) : null;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         BlockState blockstate = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 2, 0, 5, 4, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 3, 2, 6, 5, 2, blockstate1, blockstate1, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 3, 4, 6, 5, 4, blockstate1, blockstate1, false);
         this.placeBlock(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 2, 5, 0, 4, 5, 0, blockstate, blockstate, false);

         for(int i = 0; i <= 6; ++i) {
            for(int j = 0; j <= 6; ++j) {
               this.fillColumnDown(worldgenlevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingbox);
            }
         }

      }
   }

   public static class StartPiece extends NetherFortressPieces.BridgeCrossing {
      public NetherFortressPieces.PieceWeight previousPiece;
      public List<NetherFortressPieces.PieceWeight> availableBridgePieces;
      public List<NetherFortressPieces.PieceWeight> availableCastlePieces;
      public final List<StructurePiece> pendingChildren = Lists.newArrayList();

      public StartPiece(RandomSource randomsource, int i, int j) {
         super(i, j, getRandomHorizontalDirection(randomsource));
         this.availableBridgePieces = Lists.newArrayList();

         for(NetherFortressPieces.PieceWeight netherfortresspieces_pieceweight : NetherFortressPieces.BRIDGE_PIECE_WEIGHTS) {
            netherfortresspieces_pieceweight.placeCount = 0;
            this.availableBridgePieces.add(netherfortresspieces_pieceweight);
         }

         this.availableCastlePieces = Lists.newArrayList();

         for(NetherFortressPieces.PieceWeight netherfortresspieces_pieceweight1 : NetherFortressPieces.CASTLE_PIECE_WEIGHTS) {
            netherfortresspieces_pieceweight1.placeCount = 0;
            this.availableCastlePieces.add(netherfortresspieces_pieceweight1);
         }

      }

      public StartPiece(CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FORTRESS_START, compoundtag);
      }
   }
}
