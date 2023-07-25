package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class WoodlandMansionPieces {
   public static void generateMansion(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, List<WoodlandMansionPieces.WoodlandMansionPiece> list, RandomSource randomsource) {
      WoodlandMansionPieces.MansionGrid woodlandmansionpieces_mansiongrid = new WoodlandMansionPieces.MansionGrid(randomsource);
      WoodlandMansionPieces.MansionPiecePlacer woodlandmansionpieces_mansionpieceplacer = new WoodlandMansionPieces.MansionPiecePlacer(structuretemplatemanager, randomsource);
      woodlandmansionpieces_mansionpieceplacer.createMansion(blockpos, rotation, list, woodlandmansionpieces_mansiongrid);
   }

   public static void main(String[] astring) {
      RandomSource randomsource = RandomSource.create();
      long i = randomsource.nextLong();
      System.out.println("Seed: " + i);
      randomsource.setSeed(i);
      WoodlandMansionPieces.MansionGrid woodlandmansionpieces_mansiongrid = new WoodlandMansionPieces.MansionGrid(randomsource);
      woodlandmansionpieces_mansiongrid.print();
   }

   static class FirstFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
      public String get1x1(RandomSource randomsource) {
         return "1x1_a" + (randomsource.nextInt(5) + 1);
      }

      public String get1x1Secret(RandomSource randomsource) {
         return "1x1_as" + (randomsource.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(RandomSource randomsource, boolean flag) {
         return "1x2_a" + (randomsource.nextInt(9) + 1);
      }

      public String get1x2FrontEntrance(RandomSource randomsource, boolean flag) {
         return "1x2_b" + (randomsource.nextInt(5) + 1);
      }

      public String get1x2Secret(RandomSource randomsource) {
         return "1x2_s" + (randomsource.nextInt(2) + 1);
      }

      public String get2x2(RandomSource randomsource) {
         return "2x2_a" + (randomsource.nextInt(4) + 1);
      }

      public String get2x2Secret(RandomSource randomsource) {
         return "2x2_s1";
      }
   }

   abstract static class FloorRoomCollection {
      public abstract String get1x1(RandomSource randomsource);

      public abstract String get1x1Secret(RandomSource randomsource);

      public abstract String get1x2SideEntrance(RandomSource randomsource, boolean flag);

      public abstract String get1x2FrontEntrance(RandomSource randomsource, boolean flag);

      public abstract String get1x2Secret(RandomSource randomsource);

      public abstract String get2x2(RandomSource randomsource);

      public abstract String get2x2Secret(RandomSource randomsource);
   }

   static class MansionGrid {
      private static final int DEFAULT_SIZE = 11;
      private static final int CLEAR = 0;
      private static final int CORRIDOR = 1;
      private static final int ROOM = 2;
      private static final int START_ROOM = 3;
      private static final int TEST_ROOM = 4;
      private static final int BLOCKED = 5;
      private static final int ROOM_1x1 = 65536;
      private static final int ROOM_1x2 = 131072;
      private static final int ROOM_2x2 = 262144;
      private static final int ROOM_ORIGIN_FLAG = 1048576;
      private static final int ROOM_DOOR_FLAG = 2097152;
      private static final int ROOM_STAIRS_FLAG = 4194304;
      private static final int ROOM_CORRIDOR_FLAG = 8388608;
      private static final int ROOM_TYPE_MASK = 983040;
      private static final int ROOM_ID_MASK = 65535;
      private final RandomSource random;
      final WoodlandMansionPieces.SimpleGrid baseGrid;
      final WoodlandMansionPieces.SimpleGrid thirdFloorGrid;
      final WoodlandMansionPieces.SimpleGrid[] floorRooms;
      final int entranceX;
      final int entranceY;

      public MansionGrid(RandomSource randomsource) {
         this.random = randomsource;
         int i = 11;
         this.entranceX = 7;
         this.entranceY = 4;
         this.baseGrid = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
         this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
         this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
         this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
         this.baseGrid.set(0, 0, 11, 1, 5);
         this.baseGrid.set(0, 9, 11, 11, 5);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

         while(this.cleanEdges(this.baseGrid)) {
         }

         this.floorRooms = new WoodlandMansionPieces.SimpleGrid[3];
         this.floorRooms[0] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[1] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[2] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.identifyRooms(this.baseGrid, this.floorRooms[0]);
         this.identifyRooms(this.baseGrid, this.floorRooms[1]);
         this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.thirdFloorGrid = new WoodlandMansionPieces.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
         this.setupThirdFloor();
         this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
      }

      public static boolean isHouse(WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, int i, int j) {
         int k = woodlandmansionpieces_simplegrid.get(i, j);
         return k == 1 || k == 2 || k == 3 || k == 4;
      }

      public boolean isRoomId(WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, int i, int j, int k, int l) {
         return (this.floorRooms[k].get(i, j) & '\uffff') == l;
      }

      @Nullable
      public Direction get1x2RoomDirection(WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, int i, int j, int k, int l) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.isRoomId(woodlandmansionpieces_simplegrid, i + direction.getStepX(), j + direction.getStepZ(), k, l)) {
               return direction;
            }
         }

         return null;
      }

      private void recursiveCorridor(WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, int i, int j, Direction direction, int k) {
         if (k > 0) {
            woodlandmansionpieces_simplegrid.set(i, j, 1);
            woodlandmansionpieces_simplegrid.setif(i + direction.getStepX(), j + direction.getStepZ(), 0, 1);

            for(int l = 0; l < 8; ++l) {
               Direction direction1 = Direction.from2DDataValue(this.random.nextInt(4));
               if (direction1 != direction.getOpposite() && (direction1 != Direction.EAST || !this.random.nextBoolean())) {
                  int i1 = i + direction.getStepX();
                  int j1 = j + direction.getStepZ();
                  if (woodlandmansionpieces_simplegrid.get(i1 + direction1.getStepX(), j1 + direction1.getStepZ()) == 0 && woodlandmansionpieces_simplegrid.get(i1 + direction1.getStepX() * 2, j1 + direction1.getStepZ() * 2) == 0) {
                     this.recursiveCorridor(woodlandmansionpieces_simplegrid, i + direction.getStepX() + direction1.getStepX(), j + direction.getStepZ() + direction1.getStepZ(), direction1, k - 1);
                     break;
                  }
               }
            }

            Direction direction2 = direction.getClockWise();
            Direction direction3 = direction.getCounterClockWise();
            woodlandmansionpieces_simplegrid.setif(i + direction2.getStepX(), j + direction2.getStepZ(), 0, 2);
            woodlandmansionpieces_simplegrid.setif(i + direction3.getStepX(), j + direction3.getStepZ(), 0, 2);
            woodlandmansionpieces_simplegrid.setif(i + direction.getStepX() + direction2.getStepX(), j + direction.getStepZ() + direction2.getStepZ(), 0, 2);
            woodlandmansionpieces_simplegrid.setif(i + direction.getStepX() + direction3.getStepX(), j + direction.getStepZ() + direction3.getStepZ(), 0, 2);
            woodlandmansionpieces_simplegrid.setif(i + direction.getStepX() * 2, j + direction.getStepZ() * 2, 0, 2);
            woodlandmansionpieces_simplegrid.setif(i + direction2.getStepX() * 2, j + direction2.getStepZ() * 2, 0, 2);
            woodlandmansionpieces_simplegrid.setif(i + direction3.getStepX() * 2, j + direction3.getStepZ() * 2, 0, 2);
         }
      }

      private boolean cleanEdges(WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid) {
         boolean flag = false;

         for(int i = 0; i < woodlandmansionpieces_simplegrid.height; ++i) {
            for(int j = 0; j < woodlandmansionpieces_simplegrid.width; ++j) {
               if (woodlandmansionpieces_simplegrid.get(j, i) == 0) {
                  int k = 0;
                  k += isHouse(woodlandmansionpieces_simplegrid, j + 1, i) ? 1 : 0;
                  k += isHouse(woodlandmansionpieces_simplegrid, j - 1, i) ? 1 : 0;
                  k += isHouse(woodlandmansionpieces_simplegrid, j, i + 1) ? 1 : 0;
                  k += isHouse(woodlandmansionpieces_simplegrid, j, i - 1) ? 1 : 0;
                  if (k >= 3) {
                     woodlandmansionpieces_simplegrid.set(j, i, 2);
                     flag = true;
                  } else if (k == 2) {
                     int l = 0;
                     l += isHouse(woodlandmansionpieces_simplegrid, j + 1, i + 1) ? 1 : 0;
                     l += isHouse(woodlandmansionpieces_simplegrid, j - 1, i + 1) ? 1 : 0;
                     l += isHouse(woodlandmansionpieces_simplegrid, j + 1, i - 1) ? 1 : 0;
                     l += isHouse(woodlandmansionpieces_simplegrid, j - 1, i - 1) ? 1 : 0;
                     if (l <= 1) {
                        woodlandmansionpieces_simplegrid.set(j, i, 2);
                        flag = true;
                     }
                  }
               }
            }
         }

         return flag;
      }

      private void setupThirdFloor() {
         List<Tuple<Integer, Integer>> list = Lists.newArrayList();
         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid = this.floorRooms[1];

         for(int i = 0; i < this.thirdFloorGrid.height; ++i) {
            for(int j = 0; j < this.thirdFloorGrid.width; ++j) {
               int k = woodlandmansionpieces_simplegrid.get(j, i);
               int l = k & 983040;
               if (l == 131072 && (k & 2097152) == 2097152) {
                  list.add(new Tuple<>(j, i));
               }
            }
         }

         if (list.isEmpty()) {
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
         } else {
            Tuple<Integer, Integer> tuple = list.get(this.random.nextInt(list.size()));
            int i1 = woodlandmansionpieces_simplegrid.get(tuple.getA(), tuple.getB());
            woodlandmansionpieces_simplegrid.set(tuple.getA(), tuple.getB(), i1 | 4194304);
            Direction direction = this.get1x2RoomDirection(this.baseGrid, tuple.getA(), tuple.getB(), 1, i1 & '\uffff');
            int j1 = tuple.getA() + direction.getStepX();
            int k1 = tuple.getB() + direction.getStepZ();

            for(int l1 = 0; l1 < this.thirdFloorGrid.height; ++l1) {
               for(int i2 = 0; i2 < this.thirdFloorGrid.width; ++i2) {
                  if (!isHouse(this.baseGrid, i2, l1)) {
                     this.thirdFloorGrid.set(i2, l1, 5);
                  } else if (i2 == tuple.getA() && l1 == tuple.getB()) {
                     this.thirdFloorGrid.set(i2, l1, 3);
                  } else if (i2 == j1 && l1 == k1) {
                     this.thirdFloorGrid.set(i2, l1, 3);
                     this.floorRooms[2].set(i2, l1, 8388608);
                  }
               }
            }

            List<Direction> list1 = Lists.newArrayList();

            for(Direction direction1 : Direction.Plane.HORIZONTAL) {
               if (this.thirdFloorGrid.get(j1 + direction1.getStepX(), k1 + direction1.getStepZ()) == 0) {
                  list1.add(direction1);
               }
            }

            if (list1.isEmpty()) {
               this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
               woodlandmansionpieces_simplegrid.set(tuple.getA(), tuple.getB(), i1);
            } else {
               Direction direction2 = list1.get(this.random.nextInt(list1.size()));
               this.recursiveCorridor(this.thirdFloorGrid, j1 + direction2.getStepX(), k1 + direction2.getStepZ(), direction2, 4);

               while(this.cleanEdges(this.thirdFloorGrid)) {
               }

            }
         }
      }

      private void identifyRooms(WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid1) {
         ObjectArrayList<Tuple<Integer, Integer>> objectarraylist = new ObjectArrayList<>();

         for(int i = 0; i < woodlandmansionpieces_simplegrid.height; ++i) {
            for(int j = 0; j < woodlandmansionpieces_simplegrid.width; ++j) {
               if (woodlandmansionpieces_simplegrid.get(j, i) == 2) {
                  objectarraylist.add(new Tuple<>(j, i));
               }
            }
         }

         Util.shuffle(objectarraylist, this.random);
         int k = 10;

         for(Tuple<Integer, Integer> tuple : objectarraylist) {
            int l = tuple.getA();
            int i1 = tuple.getB();
            if (woodlandmansionpieces_simplegrid1.get(l, i1) == 0) {
               int j1 = l;
               int k1 = l;
               int l1 = i1;
               int i2 = i1;
               int j2 = 65536;
               if (woodlandmansionpieces_simplegrid1.get(l + 1, i1) == 0 && woodlandmansionpieces_simplegrid1.get(l, i1 + 1) == 0 && woodlandmansionpieces_simplegrid1.get(l + 1, i1 + 1) == 0 && woodlandmansionpieces_simplegrid.get(l + 1, i1) == 2 && woodlandmansionpieces_simplegrid.get(l, i1 + 1) == 2 && woodlandmansionpieces_simplegrid.get(l + 1, i1 + 1) == 2) {
                  k1 = l + 1;
                  i2 = i1 + 1;
                  j2 = 262144;
               } else if (woodlandmansionpieces_simplegrid1.get(l - 1, i1) == 0 && woodlandmansionpieces_simplegrid1.get(l, i1 + 1) == 0 && woodlandmansionpieces_simplegrid1.get(l - 1, i1 + 1) == 0 && woodlandmansionpieces_simplegrid.get(l - 1, i1) == 2 && woodlandmansionpieces_simplegrid.get(l, i1 + 1) == 2 && woodlandmansionpieces_simplegrid.get(l - 1, i1 + 1) == 2) {
                  j1 = l - 1;
                  i2 = i1 + 1;
                  j2 = 262144;
               } else if (woodlandmansionpieces_simplegrid1.get(l - 1, i1) == 0 && woodlandmansionpieces_simplegrid1.get(l, i1 - 1) == 0 && woodlandmansionpieces_simplegrid1.get(l - 1, i1 - 1) == 0 && woodlandmansionpieces_simplegrid.get(l - 1, i1) == 2 && woodlandmansionpieces_simplegrid.get(l, i1 - 1) == 2 && woodlandmansionpieces_simplegrid.get(l - 1, i1 - 1) == 2) {
                  j1 = l - 1;
                  l1 = i1 - 1;
                  j2 = 262144;
               } else if (woodlandmansionpieces_simplegrid1.get(l + 1, i1) == 0 && woodlandmansionpieces_simplegrid.get(l + 1, i1) == 2) {
                  k1 = l + 1;
                  j2 = 131072;
               } else if (woodlandmansionpieces_simplegrid1.get(l, i1 + 1) == 0 && woodlandmansionpieces_simplegrid.get(l, i1 + 1) == 2) {
                  i2 = i1 + 1;
                  j2 = 131072;
               } else if (woodlandmansionpieces_simplegrid1.get(l - 1, i1) == 0 && woodlandmansionpieces_simplegrid.get(l - 1, i1) == 2) {
                  j1 = l - 1;
                  j2 = 131072;
               } else if (woodlandmansionpieces_simplegrid1.get(l, i1 - 1) == 0 && woodlandmansionpieces_simplegrid.get(l, i1 - 1) == 2) {
                  l1 = i1 - 1;
                  j2 = 131072;
               }

               int k2 = this.random.nextBoolean() ? j1 : k1;
               int l2 = this.random.nextBoolean() ? l1 : i2;
               int i3 = 2097152;
               if (!woodlandmansionpieces_simplegrid.edgesTo(k2, l2, 1)) {
                  k2 = k2 == j1 ? k1 : j1;
                  l2 = l2 == l1 ? i2 : l1;
                  if (!woodlandmansionpieces_simplegrid.edgesTo(k2, l2, 1)) {
                     l2 = l2 == l1 ? i2 : l1;
                     if (!woodlandmansionpieces_simplegrid.edgesTo(k2, l2, 1)) {
                        k2 = k2 == j1 ? k1 : j1;
                        l2 = l2 == l1 ? i2 : l1;
                        if (!woodlandmansionpieces_simplegrid.edgesTo(k2, l2, 1)) {
                           i3 = 0;
                           k2 = j1;
                           l2 = l1;
                        }
                     }
                  }
               }

               for(int j3 = l1; j3 <= i2; ++j3) {
                  for(int k3 = j1; k3 <= k1; ++k3) {
                     if (k3 == k2 && j3 == l2) {
                        woodlandmansionpieces_simplegrid1.set(k3, j3, 1048576 | i3 | j2 | k);
                     } else {
                        woodlandmansionpieces_simplegrid1.set(k3, j3, j2 | k);
                     }
                  }
               }

               ++k;
            }
         }

      }

      public void print() {
         for(int i = 0; i < 2; ++i) {
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid = i == 0 ? this.baseGrid : this.thirdFloorGrid;

            for(int j = 0; j < woodlandmansionpieces_simplegrid.height; ++j) {
               for(int k = 0; k < woodlandmansionpieces_simplegrid.width; ++k) {
                  int l = woodlandmansionpieces_simplegrid.get(k, j);
                  if (l == 1) {
                     System.out.print("+");
                  } else if (l == 4) {
                     System.out.print("x");
                  } else if (l == 2) {
                     System.out.print("X");
                  } else if (l == 3) {
                     System.out.print("O");
                  } else if (l == 5) {
                     System.out.print("#");
                  } else {
                     System.out.print(" ");
                  }
               }

               System.out.println("");
            }

            System.out.println("");
         }

      }
   }

   static class MansionPiecePlacer {
      private final StructureTemplateManager structureTemplateManager;
      private final RandomSource random;
      private int startX;
      private int startY;

      public MansionPiecePlacer(StructureTemplateManager structuretemplatemanager, RandomSource randomsource) {
         this.structureTemplateManager = structuretemplatemanager;
         this.random = randomsource;
      }

      public void createMansion(BlockPos blockpos, Rotation rotation, List<WoodlandMansionPieces.WoodlandMansionPiece> list, WoodlandMansionPieces.MansionGrid woodlandmansionpieces_mansiongrid) {
         WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata = new WoodlandMansionPieces.PlacementData();
         woodlandmansionpieces_placementdata.position = blockpos;
         woodlandmansionpieces_placementdata.rotation = rotation;
         woodlandmansionpieces_placementdata.wallType = "wall_flat";
         WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata1 = new WoodlandMansionPieces.PlacementData();
         this.entrance(list, woodlandmansionpieces_placementdata);
         woodlandmansionpieces_placementdata1.position = woodlandmansionpieces_placementdata.position.above(8);
         woodlandmansionpieces_placementdata1.rotation = woodlandmansionpieces_placementdata.rotation;
         woodlandmansionpieces_placementdata1.wallType = "wall_window";
         if (!list.isEmpty()) {
         }

         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid = woodlandmansionpieces_mansiongrid.baseGrid;
         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid1 = woodlandmansionpieces_mansiongrid.thirdFloorGrid;
         this.startX = woodlandmansionpieces_mansiongrid.entranceX + 1;
         this.startY = woodlandmansionpieces_mansiongrid.entranceY + 1;
         int i = woodlandmansionpieces_mansiongrid.entranceX + 1;
         int j = woodlandmansionpieces_mansiongrid.entranceY;
         this.traverseOuterWalls(list, woodlandmansionpieces_placementdata, woodlandmansionpieces_simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         this.traverseOuterWalls(list, woodlandmansionpieces_placementdata1, woodlandmansionpieces_simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata2 = new WoodlandMansionPieces.PlacementData();
         woodlandmansionpieces_placementdata2.position = woodlandmansionpieces_placementdata.position.above(19);
         woodlandmansionpieces_placementdata2.rotation = woodlandmansionpieces_placementdata.rotation;
         woodlandmansionpieces_placementdata2.wallType = "wall_window";
         boolean flag = false;

         for(int k = 0; k < woodlandmansionpieces_simplegrid1.height && !flag; ++k) {
            for(int l = woodlandmansionpieces_simplegrid1.width - 1; l >= 0 && !flag; --l) {
               if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid1, l, k)) {
                  woodlandmansionpieces_placementdata2.position = woodlandmansionpieces_placementdata2.position.relative(rotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  woodlandmansionpieces_placementdata2.position = woodlandmansionpieces_placementdata2.position.relative(rotation.rotate(Direction.EAST), (l - this.startX) * 8);
                  this.traverseWallPiece(list, woodlandmansionpieces_placementdata2);
                  this.traverseOuterWalls(list, woodlandmansionpieces_placementdata2, woodlandmansionpieces_simplegrid1, Direction.SOUTH, l, k, l, k);
                  flag = true;
               }
            }
         }

         this.createRoof(list, blockpos.above(16), rotation, woodlandmansionpieces_simplegrid, woodlandmansionpieces_simplegrid1);
         this.createRoof(list, blockpos.above(27), rotation, woodlandmansionpieces_simplegrid1, (WoodlandMansionPieces.SimpleGrid)null);
         if (!list.isEmpty()) {
         }

         WoodlandMansionPieces.FloorRoomCollection[] awoodlandmansionpieces_floorroomcollection = new WoodlandMansionPieces.FloorRoomCollection[]{new WoodlandMansionPieces.FirstFloorRoomCollection(), new WoodlandMansionPieces.SecondFloorRoomCollection(), new WoodlandMansionPieces.ThirdFloorRoomCollection()};

         for(int i1 = 0; i1 < 3; ++i1) {
            BlockPos blockpos1 = blockpos.above(8 * i1 + (i1 == 2 ? 3 : 0));
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid2 = woodlandmansionpieces_mansiongrid.floorRooms[i1];
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid3 = i1 == 2 ? woodlandmansionpieces_simplegrid1 : woodlandmansionpieces_simplegrid;
            String s = i1 == 0 ? "carpet_south_1" : "carpet_south_2";
            String s1 = i1 == 0 ? "carpet_west_1" : "carpet_west_2";

            for(int j1 = 0; j1 < woodlandmansionpieces_simplegrid3.height; ++j1) {
               for(int k1 = 0; k1 < woodlandmansionpieces_simplegrid3.width; ++k1) {
                  if (woodlandmansionpieces_simplegrid3.get(k1, j1) == 1) {
                     BlockPos blockpos2 = blockpos1.relative(rotation.rotate(Direction.SOUTH), 8 + (j1 - this.startY) * 8);
                     blockpos2 = blockpos2.relative(rotation.rotate(Direction.EAST), (k1 - this.startX) * 8);
                     list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "corridor_floor", blockpos2, rotation));
                     if (woodlandmansionpieces_simplegrid3.get(k1, j1 - 1) == 1 || (woodlandmansionpieces_simplegrid2.get(k1, j1 - 1) & 8388608) == 8388608) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "carpet_north", blockpos2.relative(rotation.rotate(Direction.EAST), 1).above(), rotation));
                     }

                     if (woodlandmansionpieces_simplegrid3.get(k1 + 1, j1) == 1 || (woodlandmansionpieces_simplegrid2.get(k1 + 1, j1) & 8388608) == 8388608) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "carpet_east", blockpos2.relative(rotation.rotate(Direction.SOUTH), 1).relative(rotation.rotate(Direction.EAST), 5).above(), rotation));
                     }

                     if (woodlandmansionpieces_simplegrid3.get(k1, j1 + 1) == 1 || (woodlandmansionpieces_simplegrid2.get(k1, j1 + 1) & 8388608) == 8388608) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, s, blockpos2.relative(rotation.rotate(Direction.SOUTH), 5).relative(rotation.rotate(Direction.WEST), 1), rotation));
                     }

                     if (woodlandmansionpieces_simplegrid3.get(k1 - 1, j1) == 1 || (woodlandmansionpieces_simplegrid2.get(k1 - 1, j1) & 8388608) == 8388608) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, s1, blockpos2.relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.NORTH), 1), rotation));
                     }
                  }
               }
            }

            String s2 = i1 == 0 ? "indoors_wall_1" : "indoors_wall_2";
            String s3 = i1 == 0 ? "indoors_door_1" : "indoors_door_2";
            List<Direction> list1 = Lists.newArrayList();

            for(int l1 = 0; l1 < woodlandmansionpieces_simplegrid3.height; ++l1) {
               for(int i2 = 0; i2 < woodlandmansionpieces_simplegrid3.width; ++i2) {
                  boolean flag1 = i1 == 2 && woodlandmansionpieces_simplegrid3.get(i2, l1) == 3;
                  if (woodlandmansionpieces_simplegrid3.get(i2, l1) == 2 || flag1) {
                     int j2 = woodlandmansionpieces_simplegrid2.get(i2, l1);
                     int k2 = j2 & 983040;
                     int l2 = j2 & '\uffff';
                     flag1 = flag1 && (j2 & 8388608) == 8388608;
                     list1.clear();
                     if ((j2 & 2097152) == 2097152) {
                        for(Direction direction : Direction.Plane.HORIZONTAL) {
                           if (woodlandmansionpieces_simplegrid3.get(i2 + direction.getStepX(), l1 + direction.getStepZ()) == 1) {
                              list1.add(direction);
                           }
                        }
                     }

                     Direction direction1 = null;
                     if (!list1.isEmpty()) {
                        direction1 = list1.get(this.random.nextInt(list1.size()));
                     } else if ((j2 & 1048576) == 1048576) {
                        direction1 = Direction.UP;
                     }

                     BlockPos blockpos3 = blockpos1.relative(rotation.rotate(Direction.SOUTH), 8 + (l1 - this.startY) * 8);
                     blockpos3 = blockpos3.relative(rotation.rotate(Direction.EAST), -1 + (i2 - this.startX) * 8);
                     if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid3, i2 - 1, l1) && !woodlandmansionpieces_mansiongrid.isRoomId(woodlandmansionpieces_simplegrid3, i2 - 1, l1, i1, l2)) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.WEST ? s3 : s2, blockpos3, rotation));
                     }

                     if (woodlandmansionpieces_simplegrid3.get(i2 + 1, l1) == 1 && !flag1) {
                        BlockPos blockpos4 = blockpos3.relative(rotation.rotate(Direction.EAST), 8);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.EAST ? s3 : s2, blockpos4, rotation));
                     }

                     if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid3, i2, l1 + 1) && !woodlandmansionpieces_mansiongrid.isRoomId(woodlandmansionpieces_simplegrid3, i2, l1 + 1, i1, l2)) {
                        BlockPos blockpos5 = blockpos3.relative(rotation.rotate(Direction.SOUTH), 7);
                        blockpos5 = blockpos5.relative(rotation.rotate(Direction.EAST), 7);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.SOUTH ? s3 : s2, blockpos5, rotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (woodlandmansionpieces_simplegrid3.get(i2, l1 - 1) == 1 && !flag1) {
                        BlockPos blockpos6 = blockpos3.relative(rotation.rotate(Direction.NORTH), 1);
                        blockpos6 = blockpos6.relative(rotation.rotate(Direction.EAST), 7);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, direction1 == Direction.NORTH ? s3 : s2, blockpos6, rotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (k2 == 65536) {
                        this.addRoom1x1(list, blockpos3, rotation, direction1, awoodlandmansionpieces_floorroomcollection[i1]);
                     } else if (k2 == 131072 && direction1 != null) {
                        Direction direction2 = woodlandmansionpieces_mansiongrid.get1x2RoomDirection(woodlandmansionpieces_simplegrid3, i2, l1, i1, l2);
                        boolean flag2 = (j2 & 4194304) == 4194304;
                        this.addRoom1x2(list, blockpos3, rotation, direction2, direction1, awoodlandmansionpieces_floorroomcollection[i1], flag2);
                     } else if (k2 == 262144 && direction1 != null && direction1 != Direction.UP) {
                        Direction direction3 = direction1.getClockWise();
                        if (!woodlandmansionpieces_mansiongrid.isRoomId(woodlandmansionpieces_simplegrid3, i2 + direction3.getStepX(), l1 + direction3.getStepZ(), i1, l2)) {
                           direction3 = direction3.getOpposite();
                        }

                        this.addRoom2x2(list, blockpos3, rotation, direction3, direction1, awoodlandmansionpieces_floorroomcollection[i1]);
                     } else if (k2 == 262144 && direction1 == Direction.UP) {
                        this.addRoom2x2Secret(list, blockpos3, rotation, awoodlandmansionpieces_floorroomcollection[i1]);
                     }
                  }
               }
            }
         }

      }

      private void traverseOuterWalls(List<WoodlandMansionPieces.WoodlandMansionPiece> list, WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata, WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, Direction direction, int i, int j, int k, int l) {
         int i1 = i;
         int j1 = j;
         Direction direction1 = direction;

         do {
            if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, i1 + direction.getStepX(), j1 + direction.getStepZ())) {
               this.traverseTurn(list, woodlandmansionpieces_placementdata);
               direction = direction.getClockWise();
               if (i1 != k || j1 != l || direction1 != direction) {
                  this.traverseWallPiece(list, woodlandmansionpieces_placementdata);
               }
            } else if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, i1 + direction.getStepX(), j1 + direction.getStepZ()) && WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, i1 + direction.getStepX() + direction.getCounterClockWise().getStepX(), j1 + direction.getStepZ() + direction.getCounterClockWise().getStepZ())) {
               this.traverseInnerTurn(list, woodlandmansionpieces_placementdata);
               i1 += direction.getStepX();
               j1 += direction.getStepZ();
               direction = direction.getCounterClockWise();
            } else {
               i1 += direction.getStepX();
               j1 += direction.getStepZ();
               if (i1 != k || j1 != l || direction1 != direction) {
                  this.traverseWallPiece(list, woodlandmansionpieces_placementdata);
               }
            }
         } while(i1 != k || j1 != l || direction1 != direction);

      }

      private void createRoof(List<WoodlandMansionPieces.WoodlandMansionPiece> list, BlockPos blockpos, Rotation rotation, WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid, @Nullable WoodlandMansionPieces.SimpleGrid woodlandmansionpieces_simplegrid1) {
         for(int i = 0; i < woodlandmansionpieces_simplegrid.height; ++i) {
            for(int j = 0; j < woodlandmansionpieces_simplegrid.width; ++j) {
               BlockPos blockpos15 = blockpos.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
               blockpos15 = blockpos15.relative(rotation.rotate(Direction.EAST), (j - this.startX) * 8);
               boolean flag = woodlandmansionpieces_simplegrid1 != null && WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid1, j, i);
               if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j, i) && !flag) {
                  list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof", blockpos15.above(3), rotation));
                  if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j + 1, i)) {
                     BlockPos blockpos2 = blockpos15.relative(rotation.rotate(Direction.EAST), 6);
                     list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos2, rotation));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j - 1, i)) {
                     BlockPos blockpos3 = blockpos15.relative(rotation.rotate(Direction.EAST), 0);
                     blockpos3 = blockpos3.relative(rotation.rotate(Direction.SOUTH), 7);
                     list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos3, rotation.getRotated(Rotation.CLOCKWISE_180)));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j, i - 1)) {
                     BlockPos blockpos4 = blockpos15.relative(rotation.rotate(Direction.WEST), 1);
                     list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos4, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j, i + 1)) {
                     BlockPos blockpos5 = blockpos15.relative(rotation.rotate(Direction.EAST), 6);
                     blockpos5 = blockpos5.relative(rotation.rotate(Direction.SOUTH), 6);
                     list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockpos5, rotation.getRotated(Rotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if (woodlandmansionpieces_simplegrid1 != null) {
            for(int k = 0; k < woodlandmansionpieces_simplegrid.height; ++k) {
               for(int l = 0; l < woodlandmansionpieces_simplegrid.width; ++l) {
                  BlockPos var17 = blockpos.relative(rotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  var17 = var17.relative(rotation.rotate(Direction.EAST), (l - this.startX) * 8);
                  boolean flag1 = WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid1, l, k);
                  if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k) && flag1) {
                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l + 1, k)) {
                        BlockPos blockpos7 = var17.relative(rotation.rotate(Direction.EAST), 7);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos7, rotation));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l - 1, k)) {
                        BlockPos blockpos8 = var17.relative(rotation.rotate(Direction.WEST), 1);
                        blockpos8 = blockpos8.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos8, rotation.getRotated(Rotation.CLOCKWISE_180)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k - 1)) {
                        BlockPos blockpos9 = var17.relative(rotation.rotate(Direction.WEST), 0);
                        blockpos9 = blockpos9.relative(rotation.rotate(Direction.NORTH), 1);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos9, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k + 1)) {
                        BlockPos blockpos10 = var17.relative(rotation.rotate(Direction.EAST), 6);
                        blockpos10 = blockpos10.relative(rotation.rotate(Direction.SOUTH), 7);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockpos10, rotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l + 1, k)) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k - 1)) {
                           BlockPos blockpos11 = var17.relative(rotation.rotate(Direction.EAST), 7);
                           blockpos11 = blockpos11.relative(rotation.rotate(Direction.NORTH), 2);
                           list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos11, rotation));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k + 1)) {
                           BlockPos blockpos12 = var17.relative(rotation.rotate(Direction.EAST), 8);
                           blockpos12 = blockpos12.relative(rotation.rotate(Direction.SOUTH), 7);
                           list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos12, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l - 1, k)) {
                        if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k - 1)) {
                           BlockPos blockpos13 = var17.relative(rotation.rotate(Direction.WEST), 2);
                           blockpos13 = blockpos13.relative(rotation.rotate(Direction.NORTH), 1);
                           list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos13, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }

                        if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, l, k + 1)) {
                           BlockPos blockpos14 = var17.relative(rotation.rotate(Direction.WEST), 1);
                           blockpos14 = blockpos14.relative(rotation.rotate(Direction.SOUTH), 8);
                           list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockpos14, rotation.getRotated(Rotation.CLOCKWISE_180)));
                        }
                     }
                  }
               }
            }
         }

         for(int i1 = 0; i1 < woodlandmansionpieces_simplegrid.height; ++i1) {
            for(int j1 = 0; j1 < woodlandmansionpieces_simplegrid.width; ++j1) {
               BlockPos var19 = blockpos.relative(rotation.rotate(Direction.SOUTH), 8 + (i1 - this.startY) * 8);
               var19 = var19.relative(rotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
               boolean flag2 = woodlandmansionpieces_simplegrid1 != null && WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid1, j1, i1);
               if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1, i1) && !flag2) {
                  if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1 + 1, i1)) {
                     BlockPos blockpos16 = var19.relative(rotation.rotate(Direction.EAST), 6);
                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1, i1 + 1)) {
                        BlockPos blockpos17 = blockpos16.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos17, rotation));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1 + 1, i1 + 1)) {
                        BlockPos blockpos18 = blockpos16.relative(rotation.rotate(Direction.SOUTH), 5);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos18, rotation));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1, i1 - 1)) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos16, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1 + 1, i1 - 1)) {
                        BlockPos blockpos19 = var19.relative(rotation.rotate(Direction.EAST), 9);
                        blockpos19 = blockpos19.relative(rotation.rotate(Direction.NORTH), 2);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos19, rotation.getRotated(Rotation.CLOCKWISE_90)));
                     }
                  }

                  if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1 - 1, i1)) {
                     BlockPos blockpos20 = var19.relative(rotation.rotate(Direction.EAST), 0);
                     blockpos20 = blockpos20.relative(rotation.rotate(Direction.SOUTH), 0);
                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1, i1 + 1)) {
                        BlockPos blockpos21 = blockpos20.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos21, rotation.getRotated(Rotation.CLOCKWISE_90)));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1 - 1, i1 + 1)) {
                        BlockPos blockpos22 = blockpos20.relative(rotation.rotate(Direction.SOUTH), 8);
                        blockpos22 = blockpos22.relative(rotation.rotate(Direction.WEST), 3);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos22, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1, i1 - 1)) {
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockpos20, rotation.getRotated(Rotation.CLOCKWISE_180)));
                     } else if (WoodlandMansionPieces.MansionGrid.isHouse(woodlandmansionpieces_simplegrid, j1 - 1, i1 - 1)) {
                        BlockPos blockpos23 = blockpos20.relative(rotation.rotate(Direction.SOUTH), 1);
                        list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockpos23, rotation.getRotated(Rotation.CLOCKWISE_180)));
                     }
                  }
               }
            }
         }

      }

      private void entrance(List<WoodlandMansionPieces.WoodlandMansionPiece> list, WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata) {
         Direction direction = woodlandmansionpieces_placementdata.rotation.rotate(Direction.WEST);
         list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "entrance", woodlandmansionpieces_placementdata.position.relative(direction, 9), woodlandmansionpieces_placementdata.rotation));
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.SOUTH), 16);
      }

      private void traverseWallPiece(List<WoodlandMansionPieces.WoodlandMansionPiece> list, WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata) {
         list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_placementdata.wallType, woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.EAST), 7), woodlandmansionpieces_placementdata.rotation));
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.SOUTH), 8);
      }

      private void traverseTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> list, WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata) {
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.SOUTH), -1);
         list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, "wall_corner", woodlandmansionpieces_placementdata.position, woodlandmansionpieces_placementdata.rotation));
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.SOUTH), -7);
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.WEST), -6);
         woodlandmansionpieces_placementdata.rotation = woodlandmansionpieces_placementdata.rotation.getRotated(Rotation.CLOCKWISE_90);
      }

      private void traverseInnerTurn(List<WoodlandMansionPieces.WoodlandMansionPiece> list, WoodlandMansionPieces.PlacementData woodlandmansionpieces_placementdata) {
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.SOUTH), 6);
         woodlandmansionpieces_placementdata.position = woodlandmansionpieces_placementdata.position.relative(woodlandmansionpieces_placementdata.rotation.rotate(Direction.EAST), 8);
         woodlandmansionpieces_placementdata.rotation = woodlandmansionpieces_placementdata.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
      }

      private void addRoom1x1(List<WoodlandMansionPieces.WoodlandMansionPiece> list, BlockPos blockpos, Rotation rotation, Direction direction, WoodlandMansionPieces.FloorRoomCollection woodlandmansionpieces_floorroomcollection) {
         Rotation rotation1 = Rotation.NONE;
         String s = woodlandmansionpieces_floorroomcollection.get1x1(this.random);
         if (direction != Direction.EAST) {
            if (direction == Direction.NORTH) {
               rotation1 = rotation1.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (direction == Direction.WEST) {
               rotation1 = rotation1.getRotated(Rotation.CLOCKWISE_180);
            } else if (direction == Direction.SOUTH) {
               rotation1 = rotation1.getRotated(Rotation.CLOCKWISE_90);
            } else {
               s = woodlandmansionpieces_floorroomcollection.get1x1Secret(this.random);
            }
         }

         BlockPos blockpos1 = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation1, 7, 7);
         rotation1 = rotation1.getRotated(rotation);
         blockpos1 = blockpos1.rotate(rotation);
         BlockPos blockpos2 = blockpos.offset(blockpos1.getX(), 0, blockpos1.getZ());
         list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, s, blockpos2, rotation1));
      }

      private void addRoom1x2(List<WoodlandMansionPieces.WoodlandMansionPiece> list, BlockPos blockpos, Rotation rotation, Direction direction, Direction direction1, WoodlandMansionPieces.FloorRoomCollection woodlandmansionpieces_floorroomcollection, boolean flag) {
         if (direction1 == Direction.EAST && direction == Direction.SOUTH) {
            BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos1, rotation));
         } else if (direction1 == Direction.EAST && direction == Direction.NORTH) {
            BlockPos blockpos2 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos2 = blockpos2.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos2, rotation, Mirror.LEFT_RIGHT));
         } else if (direction1 == Direction.WEST && direction == Direction.NORTH) {
            BlockPos blockpos3 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            blockpos3 = blockpos3.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos3, rotation.getRotated(Rotation.CLOCKWISE_180)));
         } else if (direction1 == Direction.WEST && direction == Direction.SOUTH) {
            BlockPos blockpos4 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos4, rotation, Mirror.FRONT_BACK));
         } else if (direction1 == Direction.SOUTH && direction == Direction.EAST) {
            BlockPos blockpos5 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos5, rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
         } else if (direction1 == Direction.SOUTH && direction == Direction.WEST) {
            BlockPos blockpos6 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos6, rotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (direction1 == Direction.NORTH && direction == Direction.WEST) {
            BlockPos blockpos7 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            blockpos7 = blockpos7.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos7, rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
         } else if (direction1 == Direction.NORTH && direction == Direction.EAST) {
            BlockPos blockpos8 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos8 = blockpos8.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2SideEntrance(this.random, flag), blockpos8, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
         } else if (direction1 == Direction.SOUTH && direction == Direction.NORTH) {
            BlockPos blockpos9 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos9 = blockpos9.relative(rotation.rotate(Direction.NORTH), 8);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2FrontEntrance(this.random, flag), blockpos9, rotation));
         } else if (direction1 == Direction.NORTH && direction == Direction.SOUTH) {
            BlockPos blockpos10 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            blockpos10 = blockpos10.relative(rotation.rotate(Direction.SOUTH), 14);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2FrontEntrance(this.random, flag), blockpos10, rotation.getRotated(Rotation.CLOCKWISE_180)));
         } else if (direction1 == Direction.WEST && direction == Direction.EAST) {
            BlockPos blockpos11 = blockpos.relative(rotation.rotate(Direction.EAST), 15);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2FrontEntrance(this.random, flag), blockpos11, rotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (direction1 == Direction.EAST && direction == Direction.WEST) {
            BlockPos blockpos12 = blockpos.relative(rotation.rotate(Direction.WEST), 7);
            blockpos12 = blockpos12.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2FrontEntrance(this.random, flag), blockpos12, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
         } else if (direction1 == Direction.UP && direction == Direction.EAST) {
            BlockPos blockpos13 = blockpos.relative(rotation.rotate(Direction.EAST), 15);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2Secret(this.random), blockpos13, rotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (direction1 == Direction.UP && direction == Direction.SOUTH) {
            BlockPos blockpos14 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos14 = blockpos14.relative(rotation.rotate(Direction.NORTH), 0);
            list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get1x2Secret(this.random), blockpos14, rotation));
         }

      }

      private void addRoom2x2(List<WoodlandMansionPieces.WoodlandMansionPiece> list, BlockPos blockpos, Rotation rotation, Direction direction, Direction direction1, WoodlandMansionPieces.FloorRoomCollection woodlandmansionpieces_floorroomcollection) {
         int i = 0;
         int j = 0;
         Rotation rotation1 = rotation;
         Mirror mirror = Mirror.NONE;
         if (direction1 == Direction.EAST && direction == Direction.SOUTH) {
            i = -7;
         } else if (direction1 == Direction.EAST && direction == Direction.NORTH) {
            i = -7;
            j = 6;
            mirror = Mirror.LEFT_RIGHT;
         } else if (direction1 == Direction.NORTH && direction == Direction.EAST) {
            i = 1;
            j = 14;
            rotation1 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
         } else if (direction1 == Direction.NORTH && direction == Direction.WEST) {
            i = 7;
            j = 14;
            rotation1 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (direction1 == Direction.SOUTH && direction == Direction.WEST) {
            i = 7;
            j = -8;
            rotation1 = rotation.getRotated(Rotation.CLOCKWISE_90);
         } else if (direction1 == Direction.SOUTH && direction == Direction.EAST) {
            i = 1;
            j = -8;
            rotation1 = rotation.getRotated(Rotation.CLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (direction1 == Direction.WEST && direction == Direction.NORTH) {
            i = 15;
            j = 6;
            rotation1 = rotation.getRotated(Rotation.CLOCKWISE_180);
         } else if (direction1 == Direction.WEST && direction == Direction.SOUTH) {
            i = 15;
            mirror = Mirror.FRONT_BACK;
         }

         BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.EAST), i);
         blockpos1 = blockpos1.relative(rotation.rotate(Direction.SOUTH), j);
         list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get2x2(this.random), blockpos1, rotation1, mirror));
      }

      private void addRoom2x2Secret(List<WoodlandMansionPieces.WoodlandMansionPiece> list, BlockPos blockpos, Rotation rotation, WoodlandMansionPieces.FloorRoomCollection woodlandmansionpieces_floorroomcollection) {
         BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
         list.add(new WoodlandMansionPieces.WoodlandMansionPiece(this.structureTemplateManager, woodlandmansionpieces_floorroomcollection.get2x2Secret(this.random), blockpos1, rotation, Mirror.NONE));
      }
   }

   static class PlacementData {
      public Rotation rotation;
      public BlockPos position;
      public String wallType;
   }

   static class SecondFloorRoomCollection extends WoodlandMansionPieces.FloorRoomCollection {
      public String get1x1(RandomSource randomsource) {
         return "1x1_b" + (randomsource.nextInt(4) + 1);
      }

      public String get1x1Secret(RandomSource randomsource) {
         return "1x1_as" + (randomsource.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(RandomSource randomsource, boolean flag) {
         return flag ? "1x2_c_stairs" : "1x2_c" + (randomsource.nextInt(4) + 1);
      }

      public String get1x2FrontEntrance(RandomSource randomsource, boolean flag) {
         return flag ? "1x2_d_stairs" : "1x2_d" + (randomsource.nextInt(5) + 1);
      }

      public String get1x2Secret(RandomSource randomsource) {
         return "1x2_se" + (randomsource.nextInt(1) + 1);
      }

      public String get2x2(RandomSource randomsource) {
         return "2x2_b" + (randomsource.nextInt(5) + 1);
      }

      public String get2x2Secret(RandomSource randomsource) {
         return "2x2_s1";
      }
   }

   static class SimpleGrid {
      private final int[][] grid;
      final int width;
      final int height;
      private final int valueIfOutside;

      public SimpleGrid(int i, int j, int k) {
         this.width = i;
         this.height = j;
         this.valueIfOutside = k;
         this.grid = new int[i][j];
      }

      public void set(int i, int j, int k) {
         if (i >= 0 && i < this.width && j >= 0 && j < this.height) {
            this.grid[i][j] = k;
         }

      }

      public void set(int i, int j, int k, int l, int i1) {
         for(int j1 = j; j1 <= l; ++j1) {
            for(int k1 = i; k1 <= k; ++k1) {
               this.set(k1, j1, i1);
            }
         }

      }

      public int get(int i, int j) {
         return i >= 0 && i < this.width && j >= 0 && j < this.height ? this.grid[i][j] : this.valueIfOutside;
      }

      public void setif(int i, int j, int k, int l) {
         if (this.get(i, j) == k) {
            this.set(i, j, l);
         }

      }

      public boolean edgesTo(int i, int j, int k) {
         return this.get(i - 1, j) == k || this.get(i + 1, j) == k || this.get(i, j + 1) == k || this.get(i, j - 1) == k;
      }
   }

   static class ThirdFloorRoomCollection extends WoodlandMansionPieces.SecondFloorRoomCollection {
   }

   public static class WoodlandMansionPiece extends TemplateStructurePiece {
      public WoodlandMansionPiece(StructureTemplateManager structuretemplatemanager, String s, BlockPos blockpos, Rotation rotation) {
         this(structuretemplatemanager, s, blockpos, rotation, Mirror.NONE);
      }

      public WoodlandMansionPiece(StructureTemplateManager structuretemplatemanager, String s, BlockPos blockpos, Rotation rotation, Mirror mirror) {
         super(StructurePieceType.WOODLAND_MANSION_PIECE, 0, structuretemplatemanager, makeLocation(s), s, makeSettings(mirror, rotation), blockpos);
      }

      public WoodlandMansionPiece(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag) {
         super(StructurePieceType.WOODLAND_MANSION_PIECE, compoundtag, structuretemplatemanager, (resourcelocation) -> makeSettings(Mirror.valueOf(compoundtag.getString("Mi")), Rotation.valueOf(compoundtag.getString("Rot"))));
      }

      protected ResourceLocation makeTemplateLocation() {
         return makeLocation(this.templateName);
      }

      private static ResourceLocation makeLocation(String s) {
         return new ResourceLocation("woodland_mansion/" + s);
      }

      private static StructurePlaceSettings makeSettings(Mirror mirror, Rotation rotation) {
         return (new StructurePlaceSettings()).setIgnoreEntities(true).setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putString("Rot", this.placeSettings.getRotation().name());
         compoundtag.putString("Mi", this.placeSettings.getMirror().name());
      }

      protected void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox) {
         if (s.startsWith("Chest")) {
            Rotation rotation = this.placeSettings.getRotation();
            BlockState blockstate = Blocks.CHEST.defaultBlockState();
            if ("ChestWest".equals(s)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.WEST));
            } else if ("ChestEast".equals(s)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.EAST));
            } else if ("ChestSouth".equals(s)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.SOUTH));
            } else if ("ChestNorth".equals(s)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.NORTH));
            }

            this.createChest(serverlevelaccessor, boundingbox, randomsource, blockpos, BuiltInLootTables.WOODLAND_MANSION, blockstate);
         } else {
            List<Mob> list = new ArrayList<>();
            switch (s) {
               case "Mage":
                  list.add(EntityType.EVOKER.create(serverlevelaccessor.getLevel()));
                  break;
               case "Warrior":
                  list.add(EntityType.VINDICATOR.create(serverlevelaccessor.getLevel()));
                  break;
               case "Group of Allays":
                  int i = serverlevelaccessor.getRandom().nextInt(3) + 1;

                  for(int j = 0; j < i; ++j) {
                     list.add(EntityType.ALLAY.create(serverlevelaccessor.getLevel()));
                  }
                  break;
               default:
                  return;
            }

            for(Mob mob : list) {
               if (mob != null) {
                  mob.setPersistenceRequired();
                  mob.moveTo(blockpos, 0.0F, 0.0F);
                  mob.finalizeSpawn(serverlevelaccessor, serverlevelaccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.STRUCTURE, (SpawnGroupData)null, (CompoundTag)null);
                  serverlevelaccessor.addFreshEntityWithPassengers(mob);
                  serverlevelaccessor.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 2);
               }
            }
         }

      }
   }
}
