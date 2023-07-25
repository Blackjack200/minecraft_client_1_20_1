package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OceanMonumentPieces {
   private OceanMonumentPieces() {
   }

   static class FitDoubleXRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         return oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         oceanmonumentpieces_roomdefinition.claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleXRoom(direction, oceanmonumentpieces_roomdefinition);
      }
   }

   static class FitDoubleXYRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed && oceanmonumentpieces_roomdefinition.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()].claimed) {
            OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = oceanmonumentpieces_roomdefinition.connections[Direction.EAST.get3DDataValue()];
            return oceanmonumentpieces_roomdefinition1.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces_roomdefinition1.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         oceanmonumentpieces_roomdefinition.claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleXYRoom(direction, oceanmonumentpieces_roomdefinition);
      }
   }

   static class FitDoubleYRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         return oceanmonumentpieces_roomdefinition.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()].claimed;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         oceanmonumentpieces_roomdefinition.claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleYRoom(direction, oceanmonumentpieces_roomdefinition);
      }
   }

   static class FitDoubleYZRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed && oceanmonumentpieces_roomdefinition.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()].claimed) {
            OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = oceanmonumentpieces_roomdefinition.connections[Direction.NORTH.get3DDataValue()];
            return oceanmonumentpieces_roomdefinition1.hasOpening[Direction.UP.get3DDataValue()] && !oceanmonumentpieces_roomdefinition1.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         oceanmonumentpieces_roomdefinition.claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
         oceanmonumentpieces_roomdefinition.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleYZRoom(direction, oceanmonumentpieces_roomdefinition);
      }
   }

   static class FitDoubleZRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         return oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = oceanmonumentpieces_roomdefinition;
         if (!oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()] || oceanmonumentpieces_roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed) {
            oceanmonumentpieces_roomdefinition1 = oceanmonumentpieces_roomdefinition.connections[Direction.SOUTH.get3DDataValue()];
         }

         oceanmonumentpieces_roomdefinition1.claimed = true;
         oceanmonumentpieces_roomdefinition1.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         return new OceanMonumentPieces.OceanMonumentDoubleZRoom(direction, oceanmonumentpieces_roomdefinition1);
      }
   }

   static class FitSimpleRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         return true;
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         oceanmonumentpieces_roomdefinition.claimed = true;
         return new OceanMonumentPieces.OceanMonumentSimpleRoom(direction, oceanmonumentpieces_roomdefinition, randomsource);
      }
   }

   static class FitSimpleTopRoom implements OceanMonumentPieces.MonumentRoomFitter {
      public boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         return !oceanmonumentpieces_roomdefinition.hasOpening[Direction.WEST.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()] && !oceanmonumentpieces_roomdefinition.hasOpening[Direction.UP.get3DDataValue()];
      }

      public OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         oceanmonumentpieces_roomdefinition.claimed = true;
         return new OceanMonumentPieces.OceanMonumentSimpleTopRoom(direction, oceanmonumentpieces_roomdefinition);
      }
   }

   public static class MonumentBuilding extends OceanMonumentPieces.OceanMonumentPiece {
      private static final int WIDTH = 58;
      private static final int HEIGHT = 22;
      private static final int DEPTH = 58;
      public static final int BIOME_RANGE_CHECK = 29;
      private static final int TOP_POSITION = 61;
      private OceanMonumentPieces.RoomDefinition sourceRoom;
      private OceanMonumentPieces.RoomDefinition coreRoom;
      private final List<OceanMonumentPieces.OceanMonumentPiece> childPieces = Lists.newArrayList();

      public MonumentBuilding(RandomSource randomsource, int i, int j, Direction direction) {
         super(StructurePieceType.OCEAN_MONUMENT_BUILDING, direction, 0, makeBoundingBox(i, 39, j, direction, 58, 23, 58));
         this.setOrientation(direction);
         List<OceanMonumentPieces.RoomDefinition> list = this.generateRoomGraph(randomsource);
         this.sourceRoom.claimed = true;
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentEntryRoom(direction, this.sourceRoom));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentCoreRoom(direction, this.coreRoom));
         List<OceanMonumentPieces.MonumentRoomFitter> list1 = Lists.newArrayList();
         list1.add(new OceanMonumentPieces.FitDoubleXYRoom());
         list1.add(new OceanMonumentPieces.FitDoubleYZRoom());
         list1.add(new OceanMonumentPieces.FitDoubleZRoom());
         list1.add(new OceanMonumentPieces.FitDoubleXRoom());
         list1.add(new OceanMonumentPieces.FitDoubleYRoom());
         list1.add(new OceanMonumentPieces.FitSimpleTopRoom());
         list1.add(new OceanMonumentPieces.FitSimpleRoom());

         for(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition : list) {
            if (!oceanmonumentpieces_roomdefinition.claimed && !oceanmonumentpieces_roomdefinition.isSpecial()) {
               for(OceanMonumentPieces.MonumentRoomFitter oceanmonumentpieces_monumentroomfitter : list1) {
                  if (oceanmonumentpieces_monumentroomfitter.fits(oceanmonumentpieces_roomdefinition)) {
                     this.childPieces.add(oceanmonumentpieces_monumentroomfitter.create(direction, oceanmonumentpieces_roomdefinition, randomsource));
                     break;
                  }
               }
            }
         }

         BlockPos blockpos = this.getWorldPos(9, 0, 22);

         for(OceanMonumentPieces.OceanMonumentPiece oceanmonumentpieces_oceanmonumentpiece : this.childPieces) {
            oceanmonumentpieces_oceanmonumentpiece.getBoundingBox().move(blockpos);
         }

         BoundingBox boundingbox = BoundingBox.fromCorners(this.getWorldPos(1, 1, 1), this.getWorldPos(23, 8, 21));
         BoundingBox boundingbox1 = BoundingBox.fromCorners(this.getWorldPos(34, 1, 1), this.getWorldPos(56, 8, 21));
         BoundingBox boundingbox2 = BoundingBox.fromCorners(this.getWorldPos(22, 13, 22), this.getWorldPos(35, 17, 35));
         int k = randomsource.nextInt();
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(direction, boundingbox, k++));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentWingRoom(direction, boundingbox1, k++));
         this.childPieces.add(new OceanMonumentPieces.OceanMonumentPenthouse(direction, boundingbox2));
      }

      public MonumentBuilding(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_BUILDING, compoundtag);
      }

      private List<OceanMonumentPieces.RoomDefinition> generateRoomGraph(RandomSource randomsource) {
         OceanMonumentPieces.RoomDefinition[] aoceanmonumentpieces_roomdefinition = new OceanMonumentPieces.RoomDefinition[75];

         for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 4; ++j) {
               int k = 0;
               int l = getRoomIndex(i, 0, j);
               aoceanmonumentpieces_roomdefinition[l] = new OceanMonumentPieces.RoomDefinition(l);
            }
         }

         for(int i1 = 0; i1 < 5; ++i1) {
            for(int j1 = 0; j1 < 4; ++j1) {
               int k1 = 1;
               int l1 = getRoomIndex(i1, 1, j1);
               aoceanmonumentpieces_roomdefinition[l1] = new OceanMonumentPieces.RoomDefinition(l1);
            }
         }

         for(int i2 = 1; i2 < 4; ++i2) {
            for(int j2 = 0; j2 < 2; ++j2) {
               int k2 = 2;
               int l2 = getRoomIndex(i2, 2, j2);
               aoceanmonumentpieces_roomdefinition[l2] = new OceanMonumentPieces.RoomDefinition(l2);
            }
         }

         this.sourceRoom = aoceanmonumentpieces_roomdefinition[GRIDROOM_SOURCE_INDEX];

         for(int i3 = 0; i3 < 5; ++i3) {
            for(int j3 = 0; j3 < 5; ++j3) {
               for(int k3 = 0; k3 < 3; ++k3) {
                  int l3 = getRoomIndex(i3, k3, j3);
                  if (aoceanmonumentpieces_roomdefinition[l3] != null) {
                     for(Direction direction : Direction.values()) {
                        int i4 = i3 + direction.getStepX();
                        int j4 = k3 + direction.getStepY();
                        int k4 = j3 + direction.getStepZ();
                        if (i4 >= 0 && i4 < 5 && k4 >= 0 && k4 < 5 && j4 >= 0 && j4 < 3) {
                           int l4 = getRoomIndex(i4, j4, k4);
                           if (aoceanmonumentpieces_roomdefinition[l4] != null) {
                              if (k4 == j3) {
                                 aoceanmonumentpieces_roomdefinition[l3].setConnection(direction, aoceanmonumentpieces_roomdefinition[l4]);
                              } else {
                                 aoceanmonumentpieces_roomdefinition[l3].setConnection(direction.getOpposite(), aoceanmonumentpieces_roomdefinition[l4]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition = new OceanMonumentPieces.RoomDefinition(1003);
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = new OceanMonumentPieces.RoomDefinition(1001);
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition2 = new OceanMonumentPieces.RoomDefinition(1002);
         aoceanmonumentpieces_roomdefinition[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, oceanmonumentpieces_roomdefinition);
         aoceanmonumentpieces_roomdefinition[GRIDROOM_LEFTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, oceanmonumentpieces_roomdefinition1);
         aoceanmonumentpieces_roomdefinition[GRIDROOM_RIGHTWING_CONNECT_INDEX].setConnection(Direction.SOUTH, oceanmonumentpieces_roomdefinition2);
         oceanmonumentpieces_roomdefinition.claimed = true;
         oceanmonumentpieces_roomdefinition1.claimed = true;
         oceanmonumentpieces_roomdefinition2.claimed = true;
         this.sourceRoom.isSource = true;
         this.coreRoom = aoceanmonumentpieces_roomdefinition[getRoomIndex(randomsource.nextInt(4), 0, 2)];
         this.coreRoom.claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         ObjectArrayList<OceanMonumentPieces.RoomDefinition> objectarraylist = new ObjectArrayList<>();

         for(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition3 : aoceanmonumentpieces_roomdefinition) {
            if (oceanmonumentpieces_roomdefinition3 != null) {
               oceanmonumentpieces_roomdefinition3.updateOpenings();
               objectarraylist.add(oceanmonumentpieces_roomdefinition3);
            }
         }

         oceanmonumentpieces_roomdefinition.updateOpenings();
         Util.shuffle(objectarraylist, randomsource);
         int i5 = 1;

         for(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition4 : objectarraylist) {
            int j5 = 0;
            int k5 = 0;

            while(j5 < 2 && k5 < 5) {
               ++k5;
               int l5 = randomsource.nextInt(6);
               if (oceanmonumentpieces_roomdefinition4.hasOpening[l5]) {
                  int i6 = Direction.from3DDataValue(l5).getOpposite().get3DDataValue();
                  oceanmonumentpieces_roomdefinition4.hasOpening[l5] = false;
                  oceanmonumentpieces_roomdefinition4.connections[l5].hasOpening[i6] = false;
                  if (oceanmonumentpieces_roomdefinition4.findSource(i5++) && oceanmonumentpieces_roomdefinition4.connections[l5].findSource(i5++)) {
                     ++j5;
                  } else {
                     oceanmonumentpieces_roomdefinition4.hasOpening[l5] = true;
                     oceanmonumentpieces_roomdefinition4.connections[l5].hasOpening[i6] = true;
                  }
               }
            }
         }

         objectarraylist.add(oceanmonumentpieces_roomdefinition);
         objectarraylist.add(oceanmonumentpieces_roomdefinition1);
         objectarraylist.add(oceanmonumentpieces_roomdefinition2);
         return objectarraylist;
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         int i = Math.max(worldgenlevel.getSeaLevel(), 64) - this.boundingBox.minY();
         this.generateWaterBox(worldgenlevel, boundingbox, 0, 0, 0, 58, i, 58);
         this.generateWing(false, 0, worldgenlevel, randomsource, boundingbox);
         this.generateWing(true, 33, worldgenlevel, randomsource, boundingbox);
         this.generateEntranceArchs(worldgenlevel, randomsource, boundingbox);
         this.generateEntranceWall(worldgenlevel, randomsource, boundingbox);
         this.generateRoofPiece(worldgenlevel, randomsource, boundingbox);
         this.generateLowerWall(worldgenlevel, randomsource, boundingbox);
         this.generateMiddleWall(worldgenlevel, randomsource, boundingbox);
         this.generateUpperWall(worldgenlevel, randomsource, boundingbox);

         for(int j = 0; j < 7; ++j) {
            int k = 0;

            while(k < 7) {
               if (k == 0 && j == 3) {
                  k = 6;
               }

               int l = j * 9;
               int i1 = k * 9;

               for(int j1 = 0; j1 < 4; ++j1) {
                  for(int k1 = 0; k1 < 4; ++k1) {
                     this.placeBlock(worldgenlevel, BASE_LIGHT, l + j1, 0, i1 + k1, boundingbox);
                     this.fillColumnDown(worldgenlevel, BASE_LIGHT, l + j1, -1, i1 + k1, boundingbox);
                  }
               }

               if (j != 0 && j != 6) {
                  k += 6;
               } else {
                  ++k;
               }
            }
         }

         for(int l1 = 0; l1 < 5; ++l1) {
            this.generateWaterBox(worldgenlevel, boundingbox, -1 - l1, 0 + l1 * 2, -1 - l1, -1 - l1, 23, 58 + l1);
            this.generateWaterBox(worldgenlevel, boundingbox, 58 + l1, 0 + l1 * 2, -1 - l1, 58 + l1, 23, 58 + l1);
            this.generateWaterBox(worldgenlevel, boundingbox, 0 - l1, 0 + l1 * 2, -1 - l1, 57 + l1, 23, -1 - l1);
            this.generateWaterBox(worldgenlevel, boundingbox, 0 - l1, 0 + l1 * 2, 58 + l1, 57 + l1, 23, 58 + l1);
         }

         for(OceanMonumentPieces.OceanMonumentPiece oceanmonumentpieces_oceanmonumentpiece : this.childPieces) {
            if (oceanmonumentpieces_oceanmonumentpiece.getBoundingBox().intersects(boundingbox)) {
               oceanmonumentpieces_oceanmonumentpiece.postProcess(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, blockpos);
            }
         }

      }

      private void generateWing(boolean flag, int i, WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         int j = 24;
         if (this.chunkIntersects(boundingbox, i, 0, i + 23, 20)) {
            this.generateBox(worldgenlevel, boundingbox, i + 0, 0, 0, i + 24, 0, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, i + 0, 1, 0, i + 24, 10, 20);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(worldgenlevel, boundingbox, i + k, k + 1, k, i + k, k + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, i + k + 7, k + 5, k + 7, i + k + 7, k + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, i + 17 - k, k + 5, k + 7, i + 17 - k, k + 5, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, i + 24 - k, k + 1, k, i + 24 - k, k + 1, 20, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, i + k + 1, k + 1, k, i + 23 - k, k + 1, k, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, i + k + 8, k + 5, k + 7, i + 16 - k, k + 5, k + 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(worldgenlevel, boundingbox, i + 4, 4, 4, i + 6, 4, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 7, 4, 4, i + 17, 4, 6, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 18, 4, 4, i + 20, 4, 20, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 11, 8, 11, i + 13, 8, 20, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(worldgenlevel, DOT_DECO_DATA, i + 12, 9, 12, boundingbox);
            this.placeBlock(worldgenlevel, DOT_DECO_DATA, i + 12, 9, 15, boundingbox);
            this.placeBlock(worldgenlevel, DOT_DECO_DATA, i + 12, 9, 18, boundingbox);
            int l = i + (flag ? 19 : 5);
            int i1 = i + (flag ? 5 : 19);

            for(int j1 = 20; j1 >= 5; j1 -= 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, l, 5, j1, boundingbox);
            }

            for(int k1 = 19; k1 >= 7; k1 -= 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 5, k1, boundingbox);
            }

            for(int l1 = 0; l1 < 4; ++l1) {
               int i2 = flag ? i + 24 - (17 - l1 * 3) : i + 17 - l1 * 3;
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, i2, 5, 5, boundingbox);
            }

            this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 5, 5, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, i + 11, 1, 12, i + 13, 7, 12, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 12, 1, 11, i + 12, 7, 13, BASE_GRAY, BASE_GRAY, false);
         }

      }

      private void generateEntranceArchs(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         if (this.chunkIntersects(boundingbox, 22, 5, 35, 17)) {
            this.generateWaterBox(worldgenlevel, boundingbox, 25, 0, 0, 32, 8, 20);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(worldgenlevel, boundingbox, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock(worldgenlevel, BASE_LIGHT, 25, 5, 5 + i * 4, boundingbox);
               this.placeBlock(worldgenlevel, BASE_LIGHT, 26, 6, 5 + i * 4, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, 26, 5, 5 + i * 4, boundingbox);
               this.generateBox(worldgenlevel, boundingbox, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock(worldgenlevel, BASE_LIGHT, 32, 5, 5 + i * 4, boundingbox);
               this.placeBlock(worldgenlevel, BASE_LIGHT, 31, 6, 5 + i * 4, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, 31, 5, 5 + i * 4, boundingbox);
               this.generateBox(worldgenlevel, boundingbox, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, BASE_GRAY, BASE_GRAY, false);
            }
         }

      }

      private void generateEntranceWall(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         if (this.chunkIntersects(boundingbox, 15, 20, 42, 21)) {
            this.generateBox(worldgenlevel, boundingbox, 15, 0, 21, 42, 0, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 26, 1, 21, 31, 3, 21);
            this.generateBox(worldgenlevel, boundingbox, 21, 12, 21, 36, 12, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 17, 11, 21, 40, 11, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 16, 10, 21, 41, 10, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 15, 7, 21, 42, 9, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 16, 6, 21, 41, 6, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 17, 5, 21, 40, 5, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 21, 4, 21, 36, 4, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 22, 3, 21, 26, 3, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 31, 3, 21, 35, 3, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 23, 2, 21, 25, 2, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 32, 2, 21, 34, 2, 21, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 28, 4, 20, 29, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 27, 3, 21, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 30, 3, 21, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 26, 2, 21, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 31, 2, 21, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 25, 1, 21, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 32, 1, 21, boundingbox);

            for(int i = 0; i < 7; ++i) {
               this.placeBlock(worldgenlevel, BASE_BLACK, 28 - i, 6 + i, 21, boundingbox);
               this.placeBlock(worldgenlevel, BASE_BLACK, 29 + i, 6 + i, 21, boundingbox);
            }

            for(int j = 0; j < 4; ++j) {
               this.placeBlock(worldgenlevel, BASE_BLACK, 28 - j, 9 + j, 21, boundingbox);
               this.placeBlock(worldgenlevel, BASE_BLACK, 29 + j, 9 + j, 21, boundingbox);
            }

            this.placeBlock(worldgenlevel, BASE_BLACK, 28, 12, 21, boundingbox);
            this.placeBlock(worldgenlevel, BASE_BLACK, 29, 12, 21, boundingbox);

            for(int k = 0; k < 3; ++k) {
               this.placeBlock(worldgenlevel, BASE_BLACK, 22 - k * 2, 8, 21, boundingbox);
               this.placeBlock(worldgenlevel, BASE_BLACK, 22 - k * 2, 9, 21, boundingbox);
               this.placeBlock(worldgenlevel, BASE_BLACK, 35 + k * 2, 8, 21, boundingbox);
               this.placeBlock(worldgenlevel, BASE_BLACK, 35 + k * 2, 9, 21, boundingbox);
            }

            this.generateWaterBox(worldgenlevel, boundingbox, 15, 13, 21, 42, 15, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 15, 1, 21, 15, 6, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 16, 1, 21, 16, 5, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 17, 1, 21, 20, 4, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 21, 1, 21, 21, 3, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 22, 1, 21, 22, 2, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 23, 1, 21, 24, 1, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 42, 1, 21, 42, 6, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 41, 1, 21, 41, 5, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 37, 1, 21, 40, 4, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 36, 1, 21, 36, 3, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 33, 1, 21, 34, 1, 21);
            this.generateWaterBox(worldgenlevel, boundingbox, 35, 1, 21, 35, 2, 21);
         }

      }

      private void generateRoofPiece(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         if (this.chunkIntersects(boundingbox, 21, 21, 36, 36)) {
            this.generateBox(worldgenlevel, boundingbox, 21, 0, 22, 36, 0, 36, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 21, 1, 22, 36, 23, 36);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(worldgenlevel, boundingbox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(worldgenlevel, boundingbox, 25, 16, 25, 32, 16, 32, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 25, 17, 25, 25, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 32, 17, 25, 32, 19, 25, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 25, 17, 32, 25, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 32, 17, 32, 32, 19, 32, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 26, 20, 26, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 27, 21, 27, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 27, 20, 27, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 26, 20, 31, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 27, 21, 30, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 27, 20, 30, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 31, 20, 31, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 30, 21, 30, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 30, 20, 30, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 31, 20, 26, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 30, 21, 27, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 30, 20, 27, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 28, 21, 27, 29, 21, 27, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 27, 21, 28, 27, 21, 29, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 28, 21, 30, 29, 21, 30, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 30, 21, 28, 30, 21, 29, BASE_GRAY, BASE_GRAY, false);
         }

      }

      private void generateLowerWall(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         if (this.chunkIntersects(boundingbox, 0, 21, 6, 58)) {
            this.generateBox(worldgenlevel, boundingbox, 0, 0, 21, 6, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 21, 6, 7, 57);
            this.generateBox(worldgenlevel, boundingbox, 4, 4, 21, 6, 4, 53, BASE_GRAY, BASE_GRAY, false);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(worldgenlevel, boundingbox, i, i + 1, 21, i, i + 1, 57 - i, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j = 23; j < 53; j += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, 5, 5, j, boundingbox);
            }

            this.placeBlock(worldgenlevel, DOT_DECO_DATA, 5, 5, 52, boundingbox);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(worldgenlevel, boundingbox, k, k + 1, 21, k, k + 1, 57 - k, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(worldgenlevel, boundingbox, 4, 1, 52, 6, 3, 52, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 1, 51, 5, 3, 53, BASE_GRAY, BASE_GRAY, false);
         }

         if (this.chunkIntersects(boundingbox, 51, 21, 58, 58)) {
            this.generateBox(worldgenlevel, boundingbox, 51, 0, 21, 57, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 51, 1, 21, 57, 7, 57);
            this.generateBox(worldgenlevel, boundingbox, 51, 4, 21, 53, 4, 53, BASE_GRAY, BASE_GRAY, false);

            for(int l = 0; l < 4; ++l) {
               this.generateBox(worldgenlevel, boundingbox, 57 - l, l + 1, 21, 57 - l, l + 1, 57 - l, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int i1 = 23; i1 < 53; i1 += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, 52, 5, i1, boundingbox);
            }

            this.placeBlock(worldgenlevel, DOT_DECO_DATA, 52, 5, 52, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 51, 1, 52, 53, 3, 52, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 52, 1, 51, 52, 3, 53, BASE_GRAY, BASE_GRAY, false);
         }

         if (this.chunkIntersects(boundingbox, 0, 51, 57, 57)) {
            this.generateBox(worldgenlevel, boundingbox, 7, 0, 51, 50, 0, 57, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 51, 50, 10, 57);

            for(int j1 = 0; j1 < 4; ++j1) {
               this.generateBox(worldgenlevel, boundingbox, j1 + 1, j1 + 1, 57 - j1, 56 - j1, j1 + 1, 57 - j1, BASE_LIGHT, BASE_LIGHT, false);
            }
         }

      }

      private void generateMiddleWall(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         if (this.chunkIntersects(boundingbox, 7, 21, 13, 50)) {
            this.generateBox(worldgenlevel, boundingbox, 7, 0, 21, 13, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 21, 13, 10, 50);
            this.generateBox(worldgenlevel, boundingbox, 11, 8, 21, 13, 8, 53, BASE_GRAY, BASE_GRAY, false);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(worldgenlevel, boundingbox, i + 7, i + 5, 21, i + 7, i + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j = 21; j <= 45; j += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, 12, 9, j, boundingbox);
            }
         }

         if (this.chunkIntersects(boundingbox, 44, 21, 50, 54)) {
            this.generateBox(worldgenlevel, boundingbox, 44, 0, 21, 50, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 44, 1, 21, 50, 10, 50);
            this.generateBox(worldgenlevel, boundingbox, 44, 8, 21, 46, 8, 53, BASE_GRAY, BASE_GRAY, false);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(worldgenlevel, boundingbox, 50 - k, k + 5, 21, 50 - k, k + 5, 54, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int l = 21; l <= 45; l += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, 45, 9, l, boundingbox);
            }
         }

         if (this.chunkIntersects(boundingbox, 8, 44, 49, 54)) {
            this.generateBox(worldgenlevel, boundingbox, 14, 0, 44, 43, 0, 50, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 14, 1, 44, 43, 10, 50);

            for(int i1 = 12; i1 <= 45; i1 += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 9, 45, boundingbox);
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 9, 52, boundingbox);
               if (i1 == 12 || i1 == 18 || i1 == 24 || i1 == 33 || i1 == 39 || i1 == 45) {
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 9, 47, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 9, 50, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 10, 45, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 10, 46, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 10, 51, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 10, 52, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 11, 47, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 11, 50, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 12, 48, boundingbox);
                  this.placeBlock(worldgenlevel, DOT_DECO_DATA, i1, 12, 49, boundingbox);
               }
            }

            for(int j1 = 0; j1 < 3; ++j1) {
               this.generateBox(worldgenlevel, boundingbox, 8 + j1, 5 + j1, 54, 49 - j1, 5 + j1, 54, BASE_GRAY, BASE_GRAY, false);
            }

            this.generateBox(worldgenlevel, boundingbox, 11, 8, 54, 46, 8, 54, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 14, 8, 44, 43, 8, 53, BASE_GRAY, BASE_GRAY, false);
         }

      }

      private void generateUpperWall(WorldGenLevel worldgenlevel, RandomSource randomsource, BoundingBox boundingbox) {
         if (this.chunkIntersects(boundingbox, 14, 21, 20, 43)) {
            this.generateBox(worldgenlevel, boundingbox, 14, 0, 21, 20, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 14, 1, 22, 20, 14, 43);
            this.generateBox(worldgenlevel, boundingbox, 18, 12, 22, 20, 12, 39, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 18, 12, 21, 20, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

            for(int i = 0; i < 4; ++i) {
               this.generateBox(worldgenlevel, boundingbox, i + 14, i + 9, 21, i + 14, i + 9, 43 - i, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j = 23; j <= 39; j += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, 19, 13, j, boundingbox);
            }
         }

         if (this.chunkIntersects(boundingbox, 37, 21, 43, 43)) {
            this.generateBox(worldgenlevel, boundingbox, 37, 0, 21, 43, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 37, 1, 22, 43, 14, 43);
            this.generateBox(worldgenlevel, boundingbox, 37, 12, 22, 39, 12, 39, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 37, 12, 21, 39, 12, 21, BASE_LIGHT, BASE_LIGHT, false);

            for(int k = 0; k < 4; ++k) {
               this.generateBox(worldgenlevel, boundingbox, 43 - k, k + 9, 21, 43 - k, k + 9, 43 - k, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int l = 23; l <= 39; l += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, 38, 13, l, boundingbox);
            }
         }

         if (this.chunkIntersects(boundingbox, 15, 37, 42, 43)) {
            this.generateBox(worldgenlevel, boundingbox, 21, 0, 37, 36, 0, 43, BASE_GRAY, BASE_GRAY, false);
            this.generateWaterBox(worldgenlevel, boundingbox, 21, 1, 37, 36, 14, 43);
            this.generateBox(worldgenlevel, boundingbox, 21, 12, 37, 36, 12, 39, BASE_GRAY, BASE_GRAY, false);

            for(int i1 = 0; i1 < 4; ++i1) {
               this.generateBox(worldgenlevel, boundingbox, 15 + i1, i1 + 9, 43 - i1, 42 - i1, i1 + 9, 43 - i1, BASE_LIGHT, BASE_LIGHT, false);
            }

            for(int j1 = 21; j1 <= 36; j1 += 3) {
               this.placeBlock(worldgenlevel, DOT_DECO_DATA, j1, 13, 38, boundingbox);
            }
         }

      }
   }

   interface MonumentRoomFitter {
      boolean fits(OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition);

      OceanMonumentPieces.OceanMonumentPiece create(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource);
   }

   public static class OceanMonumentCoreRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentCoreRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 2, 2, 2);
      }

      public OceanMonumentCoreRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_CORE_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 8, 0, 14, 8, 14, BASE_GRAY);
         int i = 7;
         BlockState blockstate = BASE_LIGHT;
         this.generateBox(worldgenlevel, boundingbox, 0, 7, 0, 0, 7, 15, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 15, 7, 0, 15, 7, 15, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 7, 0, 15, 7, 0, blockstate, blockstate, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 7, 15, 14, 7, 15, blockstate, blockstate, false);

         for(int j = 1; j <= 6; ++j) {
            blockstate = BASE_LIGHT;
            if (j == 2 || j == 6) {
               blockstate = BASE_GRAY;
            }

            for(int k = 0; k <= 15; k += 15) {
               this.generateBox(worldgenlevel, boundingbox, k, j, 0, k, j, 1, blockstate, blockstate, false);
               this.generateBox(worldgenlevel, boundingbox, k, j, 6, k, j, 9, blockstate, blockstate, false);
               this.generateBox(worldgenlevel, boundingbox, k, j, 14, k, j, 15, blockstate, blockstate, false);
            }

            this.generateBox(worldgenlevel, boundingbox, 1, j, 0, 1, j, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 6, j, 0, 9, j, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 14, j, 0, 14, j, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 1, j, 15, 14, j, 15, blockstate, blockstate, false);
         }

         this.generateBox(worldgenlevel, boundingbox, 6, 3, 6, 9, 6, 9, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);

         for(int l = 3; l <= 6; l += 3) {
            for(int i1 = 6; i1 <= 9; i1 += 3) {
               this.placeBlock(worldgenlevel, LAMP_BLOCK, i1, l, 6, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, i1, l, 9, boundingbox);
            }
         }

         this.generateBox(worldgenlevel, boundingbox, 5, 1, 6, 5, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 9, 5, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 1, 6, 10, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 1, 9, 10, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 1, 5, 6, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 9, 1, 5, 9, 2, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 1, 10, 6, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 9, 1, 10, 9, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 5, 5, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 10, 5, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 2, 5, 10, 6, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 2, 10, 10, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 7, 1, 5, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 7, 1, 10, 7, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 7, 9, 5, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 7, 9, 10, 7, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 7, 5, 6, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 7, 10, 6, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 9, 7, 5, 14, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 9, 7, 10, 14, 7, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 2, 2, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 2, 3, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 13, 1, 2, 13, 1, 3, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 12, 1, 2, 12, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 12, 2, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 13, 3, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 13, 1, 12, 13, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 12, 1, 13, 12, 1, 13, BASE_LIGHT, BASE_LIGHT, false);
      }
   }

   public static class OceanMonumentDoubleXRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleXRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 2, 1, 1);
      }

      public OceanMonumentDoubleXRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_X_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 8, 0, oceanmonumentpieces_roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, oceanmonumentpieces_roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces_roomdefinition1.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 4, 1, 7, 4, 6, BASE_GRAY);
         }

         if (oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 8, 4, 1, 14, 4, 6, BASE_GRAY);
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 15, 3, 0, 15, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 0, 15, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 7, 14, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 15, 2, 0, 15, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 15, 2, 0, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 7, 14, 2, 7, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 15, 1, 0, 15, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 0, 15, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 0, 10, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 2, 0, 9, 2, 3, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 3, 0, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 6, 2, 3, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 9, 2, 3, boundingbox);
         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 7, 4, 2, 7);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 11, 1, 0, 12, 2, 0);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 11, 1, 7, 12, 2, 7);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 15, 1, 3, 15, 2, 4);
         }

      }
   }

   public static class OceanMonumentDoubleXYRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleXYRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 2, 2, 1);
      }

      public OceanMonumentDoubleXYRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_XY_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = this.roomDefinition;
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition2 = oceanmonumentpieces_roomdefinition1.connections[Direction.UP.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition3 = oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 8, 0, oceanmonumentpieces_roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, oceanmonumentpieces_roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces_roomdefinition2.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 8, 1, 7, 8, 6, BASE_GRAY);
         }

         if (oceanmonumentpieces_roomdefinition3.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 8, 8, 1, 14, 8, 6, BASE_GRAY);
         }

         for(int i = 1; i <= 7; ++i) {
            BlockState blockstate = BASE_LIGHT;
            if (i == 2 || i == 6) {
               blockstate = BASE_GRAY;
            }

            this.generateBox(worldgenlevel, boundingbox, 0, i, 0, 0, i, 7, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 15, i, 0, 15, i, 7, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 1, i, 0, 15, i, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 1, i, 7, 14, i, 7, blockstate, blockstate, false);
         }

         this.generateBox(worldgenlevel, boundingbox, 2, 1, 3, 2, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 2, 4, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 5, 4, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 13, 1, 3, 13, 7, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 11, 1, 2, 12, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 11, 1, 5, 12, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 3, 5, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 1, 3, 10, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 7, 2, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 5, 2, 5, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 5, 2, 10, 7, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 5, 5, 5, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 5, 5, 10, 7, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 6, 6, 2, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 9, 6, 2, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 6, 6, 5, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 9, 6, 5, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 5, 4, 3, 6, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 9, 4, 3, 10, 4, 4, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 5, 4, 2, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 5, 4, 5, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 10, 4, 2, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 10, 4, 5, boundingbox);
         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 7, 4, 2, 7);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 11, 1, 0, 12, 2, 0);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 11, 1, 7, 12, 2, 7);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 15, 1, 3, 15, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 5, 0, 4, 6, 0);
         }

         if (oceanmonumentpieces_roomdefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 5, 7, 4, 6, 7);
         }

         if (oceanmonumentpieces_roomdefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 5, 3, 0, 6, 4);
         }

         if (oceanmonumentpieces_roomdefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 11, 5, 0, 12, 6, 0);
         }

         if (oceanmonumentpieces_roomdefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 11, 5, 7, 12, 6, 7);
         }

         if (oceanmonumentpieces_roomdefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 15, 5, 3, 15, 6, 4);
         }

      }
   }

   public static class OceanMonumentDoubleYRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleYRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 1, 2, 1);
      }

      public OceanMonumentDoubleYRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Y_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
         if (oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 8, 1, 6, 8, 6, BASE_GRAY);
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 4, 0, 0, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 4, 0, 7, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 0, 6, 4, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 7, 6, 4, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 4, 1, 2, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 2, 1, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 4, 1, 5, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 4, 2, 6, 4, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 4, 5, 2, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 4, 5, 1, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 4, 5, 5, 4, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 4, 5, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = this.roomDefinition;

         for(int i = 1; i <= 5; i += 4) {
            int j = 0;
            if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 2, i, j, 2, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 5, i, j, 5, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 3, i + 2, j, 4, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, 0, i, j, 7, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 0, i + 1, j, 7, i + 1, j, BASE_GRAY, BASE_GRAY, false);
            }

            j = 7;
            if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 2, i, j, 2, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 5, i, j, 5, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 3, i + 2, j, 4, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, 0, i, j, 7, i + 2, j, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 0, i + 1, j, 7, i + 1, j, BASE_GRAY, BASE_GRAY, false);
            }

            int k = 0;
            if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, k, i, 2, k, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, i, 5, k, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, i + 2, 3, k, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, k, i, 0, k, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, i + 1, 0, k, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
            }

            k = 7;
            if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, k, i, 2, k, i + 2, 2, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, i, 5, k, i + 2, 5, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, i + 2, 3, k, i + 2, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, k, i, 0, k, i + 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, i + 1, 0, k, i + 1, 7, BASE_GRAY, BASE_GRAY, false);
            }

            oceanmonumentpieces_roomdefinition1 = oceanmonumentpieces_roomdefinition;
         }

      }
   }

   public static class OceanMonumentDoubleYZRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleYZRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 1, 2, 2);
      }

      public OceanMonumentDoubleYZRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_YZ_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = this.roomDefinition;
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition2 = oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition3 = oceanmonumentpieces_roomdefinition1.connections[Direction.UP.get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 8, oceanmonumentpieces_roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, oceanmonumentpieces_roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces_roomdefinition3.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 8, 1, 6, 8, 7, BASE_GRAY);
         }

         if (oceanmonumentpieces_roomdefinition2.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 8, 8, 6, 8, 14, BASE_GRAY);
         }

         for(int i = 1; i <= 7; ++i) {
            BlockState blockstate = BASE_LIGHT;
            if (i == 2 || i == 6) {
               blockstate = BASE_GRAY;
            }

            this.generateBox(worldgenlevel, boundingbox, 0, i, 0, 0, i, 15, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 7, i, 0, 7, i, 15, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 1, i, 0, 6, i, 0, blockstate, blockstate, false);
            this.generateBox(worldgenlevel, boundingbox, 1, i, 15, 6, i, 15, blockstate, blockstate, false);
         }

         for(int j = 1; j <= 7; ++j) {
            BlockState blockstate1 = BASE_BLACK;
            if (j == 2 || j == 6) {
               blockstate1 = LAMP_BLOCK;
            }

            this.generateBox(worldgenlevel, boundingbox, 3, j, 7, 4, j, 8, blockstate1, blockstate1, false);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 3, 7, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 15, 4, 2, 15);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 11, 0, 2, 12);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 11, 7, 2, 12);
         }

         if (oceanmonumentpieces_roomdefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 5, 0, 4, 6, 0);
         }

         if (oceanmonumentpieces_roomdefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 5, 3, 7, 6, 4);
            this.generateBox(worldgenlevel, boundingbox, 5, 4, 2, 6, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, 2, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, 5, 6, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
         }

         if (oceanmonumentpieces_roomdefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 5, 3, 0, 6, 4);
            this.generateBox(worldgenlevel, boundingbox, 1, 4, 2, 2, 4, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 1, 2, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 1, 5, 1, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
         }

         if (oceanmonumentpieces_roomdefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 5, 15, 4, 6, 15);
         }

         if (oceanmonumentpieces_roomdefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 5, 11, 0, 6, 12);
            this.generateBox(worldgenlevel, boundingbox, 1, 4, 10, 2, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 1, 10, 1, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 1, 13, 1, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
         }

         if (oceanmonumentpieces_roomdefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 5, 11, 7, 6, 12);
            this.generateBox(worldgenlevel, boundingbox, 5, 4, 10, 6, 4, 13, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, 10, 6, 3, 10, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, 13, 6, 3, 13, BASE_LIGHT, BASE_LIGHT, false);
         }

      }
   }

   public static class OceanMonumentDoubleZRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentDoubleZRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 1, 1, 2);
      }

      public OceanMonumentDoubleZRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_DOUBLE_Z_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition1 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 8, oceanmonumentpieces_roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, oceanmonumentpieces_roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (oceanmonumentpieces_roomdefinition1.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 4, 1, 6, 4, 7, BASE_GRAY);
         }

         if (oceanmonumentpieces_roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 4, 8, 6, 4, 14, BASE_GRAY);
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 0, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 3, 0, 7, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 15, 6, 3, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 2, 0, 7, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 7, 2, 0, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 15, 6, 2, 15, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 0, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 1, 0, 7, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 0, 7, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 15, 6, 1, 15, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 1, 1, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 1, 1, 6, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 1, 1, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 3, 1, 6, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 13, 1, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 1, 13, 6, 1, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 13, 1, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 3, 13, 6, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 6, 2, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 6, 5, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 9, 2, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 9, 5, 3, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 2, 6, 4, 2, 6, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 2, 9, 4, 2, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 2, 2, 7, 2, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 2, 7, 5, 2, 8, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 2, 2, 5, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 5, 2, 5, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 2, 2, 10, boundingbox);
         this.placeBlock(worldgenlevel, LAMP_BLOCK, 5, 2, 10, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 2, 3, 5, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 5, 3, 5, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 2, 3, 10, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 5, 3, 10, boundingbox);
         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 3, 7, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 15, 4, 2, 15);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 11, 0, 2, 12);
         }

         if (oceanmonumentpieces_roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 11, 7, 2, 12);
         }

      }
   }

   public static class OceanMonumentEntryRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentEntryRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 1, 1, 1);
      }

      public OceanMonumentEntryRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_ENTRY_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 1, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 2, 0, 7, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 0, 2, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 5, 1, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 7, 4, 2, 7);
         }

         if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 3, 1, 2, 4);
         }

         if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 6, 1, 3, 7, 2, 4);
         }

      }
   }

   public static class OceanMonumentPenthouse extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentPenthouse(Direction direction, BoundingBox boundingbox) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, direction, 1, boundingbox);
      }

      public OceanMonumentPenthouse(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_PENTHOUSE, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         this.generateBox(worldgenlevel, boundingbox, 2, -1, 2, 11, -1, 11, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, -1, 0, 1, -1, 11, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 12, -1, 0, 13, -1, 11, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 2, -1, 0, 11, -1, 1, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 2, -1, 12, 11, -1, 13, BASE_GRAY, BASE_GRAY, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 0, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 13, 0, 0, 13, 0, 13, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 0, 0, 12, 0, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 0, 13, 12, 0, 13, BASE_LIGHT, BASE_LIGHT, false);

         for(int i = 2; i <= 11; i += 3) {
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 0, 0, i, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 13, 0, i, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, i, 0, 0, boundingbox);
         }

         this.generateBox(worldgenlevel, boundingbox, 2, 0, 3, 4, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 9, 0, 3, 11, 0, 9, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 4, 0, 9, 9, 0, 11, BASE_LIGHT, BASE_LIGHT, false);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 5, 0, 8, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 8, 0, 8, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 10, 0, 10, boundingbox);
         this.placeBlock(worldgenlevel, BASE_LIGHT, 3, 0, 10, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 3, 0, 3, 3, 0, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 10, 0, 3, 10, 0, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 6, 0, 10, 7, 0, 10, BASE_BLACK, BASE_BLACK, false);
         int j = 3;

         for(int k = 0; k < 2; ++k) {
            for(int l = 2; l <= 8; l += 3) {
               this.generateBox(worldgenlevel, boundingbox, j, 0, l, j, 2, l, BASE_LIGHT, BASE_LIGHT, false);
            }

            j = 10;
         }

         this.generateBox(worldgenlevel, boundingbox, 5, 0, 10, 5, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 8, 0, 10, 8, 2, 10, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 6, -1, 7, 7, -1, 8, BASE_BLACK, BASE_BLACK, false);
         this.generateWaterBox(worldgenlevel, boundingbox, 6, -1, 3, 7, -1, 4);
         this.spawnElder(worldgenlevel, boundingbox, 6, 1, 6);
      }
   }

   protected abstract static class OceanMonumentPiece extends StructurePiece {
      protected static final BlockState BASE_GRAY = Blocks.PRISMARINE.defaultBlockState();
      protected static final BlockState BASE_LIGHT = Blocks.PRISMARINE_BRICKS.defaultBlockState();
      protected static final BlockState BASE_BLACK = Blocks.DARK_PRISMARINE.defaultBlockState();
      protected static final BlockState DOT_DECO_DATA = BASE_LIGHT;
      protected static final BlockState LAMP_BLOCK = Blocks.SEA_LANTERN.defaultBlockState();
      protected static final boolean DO_FILL = true;
      protected static final BlockState FILL_BLOCK = Blocks.WATER.defaultBlockState();
      protected static final Set<Block> FILL_KEEP = ImmutableSet.<Block>builder().add(Blocks.ICE).add(Blocks.PACKED_ICE).add(Blocks.BLUE_ICE).add(FILL_BLOCK.getBlock()).build();
      protected static final int GRIDROOM_WIDTH = 8;
      protected static final int GRIDROOM_DEPTH = 8;
      protected static final int GRIDROOM_HEIGHT = 4;
      protected static final int GRID_WIDTH = 5;
      protected static final int GRID_DEPTH = 5;
      protected static final int GRID_HEIGHT = 3;
      protected static final int GRID_FLOOR_COUNT = 25;
      protected static final int GRID_SIZE = 75;
      protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
      protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
      protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
      protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
      protected static final int LEFTWING_INDEX = 1001;
      protected static final int RIGHTWING_INDEX = 1002;
      protected static final int PENTHOUSE_INDEX = 1003;
      protected OceanMonumentPieces.RoomDefinition roomDefinition;

      protected static int getRoomIndex(int i, int j, int k) {
         return j * 25 + k * 5 + i;
      }

      public OceanMonumentPiece(StructurePieceType structurepiecetype, Direction direction, int i, BoundingBox boundingbox) {
         super(structurepiecetype, i, boundingbox);
         this.setOrientation(direction);
      }

      protected OceanMonumentPiece(StructurePieceType structurepiecetype, int i, Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, int j, int k, int l) {
         super(structurepiecetype, i, makeBoundingBox(direction, oceanmonumentpieces_roomdefinition, j, k, l));
         this.setOrientation(direction);
         this.roomDefinition = oceanmonumentpieces_roomdefinition;
      }

      private static BoundingBox makeBoundingBox(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, int i, int j, int k) {
         int l = oceanmonumentpieces_roomdefinition.index;
         int i1 = l % 5;
         int j1 = l / 5 % 5;
         int k1 = l / 25;
         BoundingBox boundingbox = makeBoundingBox(0, 0, 0, direction, i * 8, j * 4, k * 8);
         switch (direction) {
            case NORTH:
               boundingbox.move(i1 * 8, k1 * 4, -(j1 + k) * 8 + 1);
               break;
            case SOUTH:
               boundingbox.move(i1 * 8, k1 * 4, j1 * 8);
               break;
            case WEST:
               boundingbox.move(-(j1 + k) * 8 + 1, k1 * 4, i1 * 8);
               break;
            case EAST:
            default:
               boundingbox.move(j1 * 8, k1 * 4, i1 * 8);
         }

         return boundingbox;
      }

      public OceanMonumentPiece(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      }

      protected void generateWaterBox(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, int j1) {
         for(int k1 = j; k1 <= i1; ++k1) {
            for(int l1 = i; l1 <= l; ++l1) {
               for(int i2 = k; i2 <= j1; ++i2) {
                  BlockState blockstate = this.getBlock(worldgenlevel, l1, k1, i2, boundingbox);
                  if (!FILL_KEEP.contains(blockstate.getBlock())) {
                     if (this.getWorldY(k1) >= worldgenlevel.getSeaLevel() && blockstate != FILL_BLOCK) {
                        this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), l1, k1, i2, boundingbox);
                     } else {
                        this.placeBlock(worldgenlevel, FILL_BLOCK, l1, k1, i2, boundingbox);
                     }
                  }
               }
            }
         }

      }

      protected void generateDefaultFloor(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, boolean flag) {
         if (flag) {
            this.generateBox(worldgenlevel, boundingbox, i + 0, 0, j + 0, i + 2, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 5, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 3, 0, j + 0, i + 4, 0, j + 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 3, 0, j + 5, i + 4, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, i + 3, 0, j + 2, i + 4, 0, j + 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, i + 3, 0, j + 5, i + 4, 0, j + 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, i + 2, 0, j + 3, i + 2, 0, j + 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, i + 5, 0, j + 3, i + 5, 0, j + 4, BASE_LIGHT, BASE_LIGHT, false);
         } else {
            this.generateBox(worldgenlevel, boundingbox, i + 0, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, BASE_GRAY, BASE_GRAY, false);
         }

      }

      protected void generateBoxOnFillOnly(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, int j1, BlockState blockstate) {
         for(int k1 = j; k1 <= i1; ++k1) {
            for(int l1 = i; l1 <= l; ++l1) {
               for(int i2 = k; i2 <= j1; ++i2) {
                  if (this.getBlock(worldgenlevel, l1, k1, i2, boundingbox) == FILL_BLOCK) {
                     this.placeBlock(worldgenlevel, blockstate, l1, k1, i2, boundingbox);
                  }
               }
            }
         }

      }

      protected boolean chunkIntersects(BoundingBox boundingbox, int i, int j, int k, int l) {
         int i1 = this.getWorldX(i, j);
         int j1 = this.getWorldZ(i, j);
         int k1 = this.getWorldX(k, l);
         int l1 = this.getWorldZ(k, l);
         return boundingbox.intersects(Math.min(i1, k1), Math.min(j1, l1), Math.max(i1, k1), Math.max(j1, l1));
      }

      protected void spawnElder(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k) {
         BlockPos blockpos = this.getWorldPos(i, j, k);
         if (boundingbox.isInside(blockpos)) {
            ElderGuardian elderguardian = EntityType.ELDER_GUARDIAN.create(worldgenlevel.getLevel());
            if (elderguardian != null) {
               elderguardian.heal(elderguardian.getMaxHealth());
               elderguardian.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
               elderguardian.finalizeSpawn(worldgenlevel, worldgenlevel.getCurrentDifficultyAt(elderguardian.blockPosition()), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
               worldgenlevel.addFreshEntityWithPassengers(elderguardian);
            }
         }

      }
   }

   public static class OceanMonumentSimpleRoom extends OceanMonumentPieces.OceanMonumentPiece {
      private int mainDesign;

      public OceanMonumentSimpleRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition, RandomSource randomsource) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 1, 1, 1);
         this.mainDesign = randomsource.nextInt(3);
      }

      public OceanMonumentSimpleRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
         }

         boolean flag = this.mainDesign != 0 && randomsource.nextBoolean() && !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()] && !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()] && this.roomDefinition.countOpenings() > 1;
         if (this.mainDesign == 0) {
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 2, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 2, 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 2, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 1, 2, 1, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 5, 1, 0, 7, 1, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 3, 0, 7, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 2, 0, 7, 2, 2, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 6, 2, 1, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 5, 2, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 3, 5, 2, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 2, 5, 0, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 2, 7, 2, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 1, 2, 6, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 5, 1, 5, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 3, 5, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 2, 5, 7, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 6, 2, 6, boundingbox);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 3, 3, 0, 4, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, 3, 3, 0, 4, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 3, 2, 0, 4, 2, 0, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 1, 1, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 3, 3, 7, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, 3, 3, 6, 4, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 3, 2, 7, 4, 2, 7, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 3, 1, 6, 4, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 0, 3, 3, 0, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, 0, 3, 3, 1, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 0, 2, 3, 0, 2, 4, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 0, 1, 3, 1, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 7, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, 6, 3, 3, 7, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 7, 2, 3, 7, 2, 4, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 6, 1, 3, 7, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            }
         } else if (this.mainDesign == 1) {
            this.generateBox(worldgenlevel, boundingbox, 2, 1, 2, 2, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 2, 1, 5, 2, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 1, 5, 5, 3, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 5, 1, 2, 5, 3, 2, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 2, 2, 2, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 2, 2, 5, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 5, 2, 5, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 5, 2, 2, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 1, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 1, 0, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 7, 1, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 6, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, 7, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 1, 6, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 1, 0, 7, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 1, 1, 7, 3, 1, BASE_LIGHT, BASE_LIGHT, false);
            this.placeBlock(worldgenlevel, BASE_GRAY, 1, 2, 0, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 0, 2, 1, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 1, 2, 7, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 0, 2, 6, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 6, 2, 7, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 7, 2, 6, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 6, 2, 0, boundingbox);
            this.placeBlock(worldgenlevel, BASE_GRAY, 7, 2, 1, boundingbox);
            if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 6, 2, 0, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 1, 2, 7, 6, 2, 7, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 0, 3, 1, 0, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 0, 2, 1, 0, 2, 6, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 0, 1, 1, 0, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateBox(worldgenlevel, boundingbox, 7, 3, 1, 7, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, 7, 2, 1, 7, 2, 6, BASE_GRAY, BASE_GRAY, false);
               this.generateBox(worldgenlevel, boundingbox, 7, 1, 1, 7, 1, 6, BASE_LIGHT, BASE_LIGHT, false);
            }
         } else if (this.mainDesign == 2) {
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 7, 4, 2, 7);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateWaterBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateWaterBox(worldgenlevel, boundingbox, 7, 1, 3, 7, 2, 4);
            }
         }

         if (flag) {
            this.generateBox(worldgenlevel, boundingbox, 3, 1, 3, 4, 1, 4, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 3, 2, 3, 4, 2, 4, BASE_GRAY, BASE_GRAY, false);
            this.generateBox(worldgenlevel, boundingbox, 3, 3, 3, 4, 3, 4, BASE_LIGHT, BASE_LIGHT, false);
         }

      }
   }

   public static class OceanMonumentSimpleTopRoom extends OceanMonumentPieces.OceanMonumentPiece {
      public OceanMonumentSimpleTopRoom(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, 1, direction, oceanmonumentpieces_roomdefinition, 1, 1, 1);
      }

      public OceanMonumentSimpleTopRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_SIMPLE_TOP_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(worldgenlevel, boundingbox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(worldgenlevel, boundingbox, 1, 4, 1, 6, 4, 6, BASE_GRAY);
         }

         for(int i = 1; i <= 6; ++i) {
            for(int j = 1; j <= 6; ++j) {
               if (randomsource.nextInt(3) != 0) {
                  int k = 2 + (randomsource.nextInt(4) == 0 ? 0 : 1);
                  BlockState blockstate = Blocks.WET_SPONGE.defaultBlockState();
                  this.generateBox(worldgenlevel, boundingbox, i, k, j, i, 3, j, blockstate, blockstate, false);
               }
            }
         }

         this.generateBox(worldgenlevel, boundingbox, 0, 1, 0, 0, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 1, 0, 7, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 0, 6, 1, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 7, 6, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 2, 0, 0, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 2, 0, 7, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 0, 6, 2, 0, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 2, 7, 6, 2, 7, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 3, 0, 0, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 3, 0, 7, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 0, 6, 3, 0, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 7, 6, 3, 7, BASE_LIGHT, BASE_LIGHT, false);
         this.generateBox(worldgenlevel, boundingbox, 0, 1, 3, 0, 2, 4, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 7, 1, 3, 7, 2, 4, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0, BASE_BLACK, BASE_BLACK, false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 7, 4, 2, 7, BASE_BLACK, BASE_BLACK, false);
         if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(worldgenlevel, boundingbox, 3, 1, 0, 4, 2, 0);
         }

      }
   }

   public static class OceanMonumentWingRoom extends OceanMonumentPieces.OceanMonumentPiece {
      private int mainDesign;

      public OceanMonumentWingRoom(Direction direction, BoundingBox boundingbox, int i) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, direction, 1, boundingbox);
         this.mainDesign = i & 1;
      }

      public OceanMonumentWingRoom(CompoundTag compoundtag) {
         super(StructurePieceType.OCEAN_MONUMENT_WING_ROOM, compoundtag);
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (this.mainDesign == 0) {
            for(int i = 0; i < 4; ++i) {
               this.generateBox(worldgenlevel, boundingbox, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, BASE_LIGHT, BASE_LIGHT, false);
            }

            this.generateBox(worldgenlevel, boundingbox, 7, 0, 6, 15, 0, 16, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 6, 0, 6, 6, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 16, 0, 6, 16, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 1, 7, 7, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 15, 1, 7, 15, 1, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 7, 1, 6, 9, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 13, 1, 6, 15, 3, 6, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 8, 1, 7, 9, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 13, 1, 7, 14, 1, 7, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 9, 0, 5, 13, 0, 5, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 10, 0, 7, 12, 0, 7, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 8, 0, 10, 8, 0, 12, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 14, 0, 10, 14, 0, 12, BASE_BLACK, BASE_BLACK, false);

            for(int j = 18; j >= 7; j -= 3) {
               this.placeBlock(worldgenlevel, LAMP_BLOCK, 6, 3, j, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, 16, 3, j, boundingbox);
            }

            this.placeBlock(worldgenlevel, LAMP_BLOCK, 10, 0, 10, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 12, 0, 10, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 10, 0, 12, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 12, 0, 12, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 8, 3, 6, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 14, 3, 6, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 4, 2, 4, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 4, 1, 4, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 4, 0, 4, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 18, 2, 4, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 18, 1, 4, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 18, 0, 4, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 4, 2, 18, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 4, 1, 18, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 4, 0, 18, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 18, 2, 18, boundingbox);
            this.placeBlock(worldgenlevel, LAMP_BLOCK, 18, 1, 18, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 18, 0, 18, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 9, 7, 20, boundingbox);
            this.placeBlock(worldgenlevel, BASE_LIGHT, 13, 7, 20, boundingbox);
            this.generateBox(worldgenlevel, boundingbox, 6, 0, 21, 7, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 15, 0, 21, 16, 4, 21, BASE_LIGHT, BASE_LIGHT, false);
            this.spawnElder(worldgenlevel, boundingbox, 11, 2, 16);
         } else if (this.mainDesign == 1) {
            this.generateBox(worldgenlevel, boundingbox, 9, 3, 18, 13, 3, 20, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 9, 0, 18, 9, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
            this.generateBox(worldgenlevel, boundingbox, 13, 0, 18, 13, 2, 18, BASE_LIGHT, BASE_LIGHT, false);
            int k = 9;
            int l = 20;
            int i1 = 5;

            for(int j1 = 0; j1 < 2; ++j1) {
               this.placeBlock(worldgenlevel, BASE_LIGHT, k, 6, 20, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, k, 5, 20, boundingbox);
               this.placeBlock(worldgenlevel, BASE_LIGHT, k, 4, 20, boundingbox);
               k = 13;
            }

            this.generateBox(worldgenlevel, boundingbox, 7, 3, 7, 15, 3, 14, BASE_LIGHT, BASE_LIGHT, false);
            k = 10;

            for(int k1 = 0; k1 < 2; ++k1) {
               this.generateBox(worldgenlevel, boundingbox, k, 0, 10, k, 6, 10, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, 0, 12, k, 6, 12, BASE_LIGHT, BASE_LIGHT, false);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, k, 0, 10, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, k, 0, 12, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, k, 4, 10, boundingbox);
               this.placeBlock(worldgenlevel, LAMP_BLOCK, k, 4, 12, boundingbox);
               k = 12;
            }

            k = 8;

            for(int l1 = 0; l1 < 2; ++l1) {
               this.generateBox(worldgenlevel, boundingbox, k, 0, 7, k, 2, 7, BASE_LIGHT, BASE_LIGHT, false);
               this.generateBox(worldgenlevel, boundingbox, k, 0, 14, k, 2, 14, BASE_LIGHT, BASE_LIGHT, false);
               k = 14;
            }

            this.generateBox(worldgenlevel, boundingbox, 8, 3, 8, 8, 3, 13, BASE_BLACK, BASE_BLACK, false);
            this.generateBox(worldgenlevel, boundingbox, 14, 3, 8, 14, 3, 13, BASE_BLACK, BASE_BLACK, false);
            this.spawnElder(worldgenlevel, boundingbox, 11, 5, 13);
         }

      }
   }

   static class RoomDefinition {
      final int index;
      final OceanMonumentPieces.RoomDefinition[] connections = new OceanMonumentPieces.RoomDefinition[6];
      final boolean[] hasOpening = new boolean[6];
      boolean claimed;
      boolean isSource;
      private int scanIndex;

      public RoomDefinition(int i) {
         this.index = i;
      }

      public void setConnection(Direction direction, OceanMonumentPieces.RoomDefinition oceanmonumentpieces_roomdefinition) {
         this.connections[direction.get3DDataValue()] = oceanmonumentpieces_roomdefinition;
         oceanmonumentpieces_roomdefinition.connections[direction.getOpposite().get3DDataValue()] = this;
      }

      public void updateOpenings() {
         for(int i = 0; i < 6; ++i) {
            this.hasOpening[i] = this.connections[i] != null;
         }

      }

      public boolean findSource(int i) {
         if (this.isSource) {
            return true;
         } else {
            this.scanIndex = i;

            for(int j = 0; j < 6; ++j) {
               if (this.connections[j] != null && this.hasOpening[j] && this.connections[j].scanIndex != i && this.connections[j].findSource(i)) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean isSpecial() {
         return this.index >= 75;
      }

      public int countOpenings() {
         int i = 0;

         for(int j = 0; j < 6; ++j) {
            if (this.hasOpening[j]) {
               ++i;
            }
         }

         return i;
      }
   }
}
