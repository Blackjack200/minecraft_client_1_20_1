package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.slf4j.Logger;

public class MineshaftPieces {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int DEFAULT_SHAFT_WIDTH = 3;
   private static final int DEFAULT_SHAFT_HEIGHT = 3;
   private static final int DEFAULT_SHAFT_LENGTH = 5;
   private static final int MAX_PILLAR_HEIGHT = 20;
   private static final int MAX_CHAIN_HEIGHT = 50;
   private static final int MAX_DEPTH = 8;
   public static final int MAGIC_START_Y = 50;

   private static MineshaftPieces.MineShaftPiece createRandomShaftPiece(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, @Nullable Direction direction, int l, MineshaftStructure.Type mineshaftstructure_type) {
      int i1 = randomsource.nextInt(100);
      if (i1 >= 80) {
         BoundingBox boundingbox = MineshaftPieces.MineShaftCrossing.findCrossing(structurepieceaccessor, randomsource, i, j, k, direction);
         if (boundingbox != null) {
            return new MineshaftPieces.MineShaftCrossing(l, boundingbox, direction, mineshaftstructure_type);
         }
      } else if (i1 >= 70) {
         BoundingBox boundingbox1 = MineshaftPieces.MineShaftStairs.findStairs(structurepieceaccessor, randomsource, i, j, k, direction);
         if (boundingbox1 != null) {
            return new MineshaftPieces.MineShaftStairs(l, boundingbox1, direction, mineshaftstructure_type);
         }
      } else {
         BoundingBox boundingbox2 = MineshaftPieces.MineShaftCorridor.findCorridorSize(structurepieceaccessor, randomsource, i, j, k, direction);
         if (boundingbox2 != null) {
            return new MineshaftPieces.MineShaftCorridor(l, randomsource, boundingbox2, direction, mineshaftstructure_type);
         }
      }

      return null;
   }

   static MineshaftPieces.MineShaftPiece generateAndAddPiece(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction, int l) {
      if (l > 8) {
         return null;
      } else if (Math.abs(i - structurepiece.getBoundingBox().minX()) <= 80 && Math.abs(k - structurepiece.getBoundingBox().minZ()) <= 80) {
         MineshaftStructure.Type mineshaftstructure_type = ((MineshaftPieces.MineShaftPiece)structurepiece).type;
         MineshaftPieces.MineShaftPiece mineshaftpieces_mineshaftpiece = createRandomShaftPiece(structurepieceaccessor, randomsource, i, j, k, direction, l + 1, mineshaftstructure_type);
         if (mineshaftpieces_mineshaftpiece != null) {
            structurepieceaccessor.addPiece(mineshaftpieces_mineshaftpiece);
            mineshaftpieces_mineshaftpiece.addChildren(structurepiece, structurepieceaccessor, randomsource);
         }

         return mineshaftpieces_mineshaftpiece;
      } else {
         return null;
      }
   }

   public static class MineShaftCorridor extends MineshaftPieces.MineShaftPiece {
      private final boolean hasRails;
      private final boolean spiderCorridor;
      private boolean hasPlacedSpider;
      private final int numSections;

      public MineShaftCorridor(CompoundTag compoundtag) {
         super(StructurePieceType.MINE_SHAFT_CORRIDOR, compoundtag);
         this.hasRails = compoundtag.getBoolean("hr");
         this.spiderCorridor = compoundtag.getBoolean("sc");
         this.hasPlacedSpider = compoundtag.getBoolean("hps");
         this.numSections = compoundtag.getInt("Num");
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("hr", this.hasRails);
         compoundtag.putBoolean("sc", this.spiderCorridor);
         compoundtag.putBoolean("hps", this.hasPlacedSpider);
         compoundtag.putInt("Num", this.numSections);
      }

      public MineShaftCorridor(int i, RandomSource randomsource, BoundingBox boundingbox, Direction direction, MineshaftStructure.Type mineshaftstructure_type) {
         super(StructurePieceType.MINE_SHAFT_CORRIDOR, i, mineshaftstructure_type, boundingbox);
         this.setOrientation(direction);
         this.hasRails = randomsource.nextInt(3) == 0;
         this.spiderCorridor = !this.hasRails && randomsource.nextInt(23) == 0;
         if (this.getOrientation().getAxis() == Direction.Axis.Z) {
            this.numSections = boundingbox.getZSpan() / 5;
         } else {
            this.numSections = boundingbox.getXSpan() / 5;
         }

      }

      @Nullable
      public static BoundingBox findCorridorSize(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction) {
         for(int l = randomsource.nextInt(3) + 2; l > 0; --l) {
            int i1 = l * 5;
            BoundingBox boundingbox;
            switch (direction) {
               case NORTH:
               default:
                  boundingbox = new BoundingBox(0, 0, -(i1 - 1), 2, 2, 0);
                  break;
               case SOUTH:
                  boundingbox = new BoundingBox(0, 0, 0, 2, 2, i1 - 1);
                  break;
               case WEST:
                  boundingbox = new BoundingBox(-(i1 - 1), 0, 0, 0, 2, 2);
                  break;
               case EAST:
                  boundingbox = new BoundingBox(0, 0, 0, i1 - 1, 2, 2);
            }

            boundingbox.move(i, j, k);
            if (structurepieceaccessor.findCollisionPiece(boundingbox) == null) {
               return boundingbox;
            }
         }

         return null;
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         int i = this.getGenDepth();
         int j = randomsource.nextInt(4);
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
               default:
                  if (j <= 1) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ() - 1, direction, i);
                  } else if (j == 2) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ(), Direction.WEST, i);
                  } else {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ(), Direction.EAST, i);
                  }
                  break;
               case SOUTH:
                  if (j <= 1) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.maxZ() + 1, direction, i);
                  } else if (j == 2) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.maxZ() - 3, Direction.WEST, i);
                  } else {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.maxZ() - 3, Direction.EAST, i);
                  }
                  break;
               case WEST:
                  if (j <= 1) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ(), direction, i);
                  } else if (j == 2) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                  } else {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                  }
                  break;
               case EAST:
                  if (j <= 1) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ(), direction, i);
                  } else if (j == 2) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                  } else {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + randomsource.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                  }
            }
         }

         if (i < 8) {
            if (direction != Direction.NORTH && direction != Direction.SOUTH) {
               for(int i1 = this.boundingBox.minX() + 3; i1 + 3 <= this.boundingBox.maxX(); i1 += 5) {
                  int j1 = randomsource.nextInt(5);
                  if (j1 == 0) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, i1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i + 1);
                  } else if (j1 == 1) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, i1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i + 1);
                  }
               }
            } else {
               for(int k = this.boundingBox.minZ() + 3; k + 3 <= this.boundingBox.maxZ(); k += 5) {
                  int l = randomsource.nextInt(5);
                  if (l == 0) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY(), k, Direction.WEST, i + 1);
                  } else if (l == 1) {
                     MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), k, Direction.EAST, i + 1);
                  }
               }
            }
         }

      }

      protected boolean createChest(WorldGenLevel worldgenlevel, BoundingBox boundingbox, RandomSource randomsource, int i, int j, int k, ResourceLocation resourcelocation) {
         BlockPos blockpos = this.getWorldPos(i, j, k);
         if (boundingbox.isInside(blockpos) && worldgenlevel.getBlockState(blockpos).isAir() && !worldgenlevel.getBlockState(blockpos.below()).isAir()) {
            BlockState blockstate = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, randomsource.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
            this.placeBlock(worldgenlevel, blockstate, i, j, k, boundingbox);
            MinecartChest minecartchest = new MinecartChest(worldgenlevel.getLevel(), (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D);
            minecartchest.setLootTable(resourcelocation, randomsource.nextLong());
            worldgenlevel.addFreshEntity(minecartchest);
            return true;
         } else {
            return false;
         }
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (!this.isInInvalidLocation(worldgenlevel, boundingbox)) {
            int i = 0;
            int j = 2;
            int k = 0;
            int l = 2;
            int i1 = this.numSections * 5 - 1;
            BlockState blockstate = this.type.getPlanksState();
            this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 2, 1, i1, CAVE_AIR, CAVE_AIR, false);
            this.generateMaybeBox(worldgenlevel, boundingbox, randomsource, 0.8F, 0, 2, 0, 2, 2, i1, CAVE_AIR, CAVE_AIR, false, false);
            if (this.spiderCorridor) {
               this.generateMaybeBox(worldgenlevel, boundingbox, randomsource, 0.6F, 0, 0, 0, 2, 1, i1, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
            }

            for(int j1 = 0; j1 < this.numSections; ++j1) {
               int k1 = 2 + j1 * 5;
               this.placeSupport(worldgenlevel, boundingbox, 0, 0, k1, 2, 2, randomsource);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.1F, 0, 2, k1 - 1);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.1F, 2, 2, k1 - 1);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.1F, 0, 2, k1 + 1);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.1F, 2, 2, k1 + 1);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.05F, 0, 2, k1 - 2);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.05F, 2, 2, k1 - 2);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.05F, 0, 2, k1 + 2);
               this.maybePlaceCobWeb(worldgenlevel, boundingbox, randomsource, 0.05F, 2, 2, k1 + 2);
               if (randomsource.nextInt(100) == 0) {
                  this.createChest(worldgenlevel, boundingbox, randomsource, 2, 0, k1 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
               }

               if (randomsource.nextInt(100) == 0) {
                  this.createChest(worldgenlevel, boundingbox, randomsource, 0, 0, k1 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
               }

               if (this.spiderCorridor && !this.hasPlacedSpider) {
                  int l1 = 1;
                  int i2 = k1 - 1 + randomsource.nextInt(3);
                  BlockPos blockpos1 = this.getWorldPos(1, 0, i2);
                  if (boundingbox.isInside(blockpos1) && this.isInterior(worldgenlevel, 1, 0, i2, boundingbox)) {
                     this.hasPlacedSpider = true;
                     worldgenlevel.setBlock(blockpos1, Blocks.SPAWNER.defaultBlockState(), 2);
                     BlockEntity blockentity = worldgenlevel.getBlockEntity(blockpos1);
                     if (blockentity instanceof SpawnerBlockEntity) {
                        SpawnerBlockEntity spawnerblockentity = (SpawnerBlockEntity)blockentity;
                        spawnerblockentity.setEntityId(EntityType.CAVE_SPIDER, randomsource);
                     }
                  }
               }
            }

            for(int j2 = 0; j2 <= 2; ++j2) {
               for(int k2 = 0; k2 <= i1; ++k2) {
                  this.setPlanksBlock(worldgenlevel, boundingbox, blockstate, j2, -1, k2);
               }
            }

            int l2 = 2;
            this.placeDoubleLowerOrUpperSupport(worldgenlevel, boundingbox, 0, -1, 2);
            if (this.numSections > 1) {
               int i3 = i1 - 2;
               this.placeDoubleLowerOrUpperSupport(worldgenlevel, boundingbox, 0, -1, i3);
            }

            if (this.hasRails) {
               BlockState blockstate1 = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);

               for(int j3 = 0; j3 <= i1; ++j3) {
                  BlockState blockstate2 = this.getBlock(worldgenlevel, 1, -1, j3, boundingbox);
                  if (!blockstate2.isAir() && blockstate2.isSolidRender(worldgenlevel, this.getWorldPos(1, -1, j3))) {
                     float f = this.isInterior(worldgenlevel, 1, 0, j3, boundingbox) ? 0.7F : 0.9F;
                     this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, f, 1, 0, j3, blockstate1);
                  }
               }
            }

         }
      }

      private void placeDoubleLowerOrUpperSupport(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k) {
         BlockState blockstate = this.type.getWoodState();
         BlockState blockstate1 = this.type.getPlanksState();
         if (this.getBlock(worldgenlevel, i, j, k, boundingbox).is(blockstate1.getBlock())) {
            this.fillPillarDownOrChainUp(worldgenlevel, blockstate, i, j, k, boundingbox);
         }

         if (this.getBlock(worldgenlevel, i + 2, j, k, boundingbox).is(blockstate1.getBlock())) {
            this.fillPillarDownOrChainUp(worldgenlevel, blockstate, i + 2, j, k, boundingbox);
         }

      }

      protected void fillColumnDown(WorldGenLevel worldgenlevel, BlockState blockstate, int i, int j, int k, BoundingBox boundingbox) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = this.getWorldPos(i, j, k);
         if (boundingbox.isInside(blockpos_mutableblockpos)) {
            int l = blockpos_mutableblockpos.getY();

            while(this.isReplaceableByStructures(worldgenlevel.getBlockState(blockpos_mutableblockpos)) && blockpos_mutableblockpos.getY() > worldgenlevel.getMinBuildHeight() + 1) {
               blockpos_mutableblockpos.move(Direction.DOWN);
            }

            if (this.canPlaceColumnOnTopOf(worldgenlevel, blockpos_mutableblockpos, worldgenlevel.getBlockState(blockpos_mutableblockpos))) {
               while(blockpos_mutableblockpos.getY() < l) {
                  blockpos_mutableblockpos.move(Direction.UP);
                  worldgenlevel.setBlock(blockpos_mutableblockpos, blockstate, 2);
               }

            }
         }
      }

      protected void fillPillarDownOrChainUp(WorldGenLevel worldgenlevel, BlockState blockstate, int i, int j, int k, BoundingBox boundingbox) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = this.getWorldPos(i, j, k);
         if (boundingbox.isInside(blockpos_mutableblockpos)) {
            int l = blockpos_mutableblockpos.getY();
            int i1 = 1;
            boolean flag = true;

            for(boolean flag1 = true; flag || flag1; ++i1) {
               if (flag) {
                  blockpos_mutableblockpos.setY(l - i1);
                  BlockState blockstate1 = worldgenlevel.getBlockState(blockpos_mutableblockpos);
                  boolean flag2 = this.isReplaceableByStructures(blockstate1) && !blockstate1.is(Blocks.LAVA);
                  if (!flag2 && this.canPlaceColumnOnTopOf(worldgenlevel, blockpos_mutableblockpos, blockstate1)) {
                     fillColumnBetween(worldgenlevel, blockstate, blockpos_mutableblockpos, l - i1 + 1, l);
                     return;
                  }

                  flag = i1 <= 20 && flag2 && blockpos_mutableblockpos.getY() > worldgenlevel.getMinBuildHeight() + 1;
               }

               if (flag1) {
                  blockpos_mutableblockpos.setY(l + i1);
                  BlockState blockstate2 = worldgenlevel.getBlockState(blockpos_mutableblockpos);
                  boolean flag3 = this.isReplaceableByStructures(blockstate2);
                  if (!flag3 && this.canHangChainBelow(worldgenlevel, blockpos_mutableblockpos, blockstate2)) {
                     worldgenlevel.setBlock(blockpos_mutableblockpos.setY(l + 1), this.type.getFenceState(), 2);
                     fillColumnBetween(worldgenlevel, Blocks.CHAIN.defaultBlockState(), blockpos_mutableblockpos, l + 2, l + i1);
                     return;
                  }

                  flag1 = i1 <= 50 && flag3 && blockpos_mutableblockpos.getY() < worldgenlevel.getMaxBuildHeight() - 1;
               }
            }

         }
      }

      private static void fillColumnBetween(WorldGenLevel worldgenlevel, BlockState blockstate, BlockPos.MutableBlockPos blockpos_mutableblockpos, int i, int j) {
         for(int k = i; k < j; ++k) {
            worldgenlevel.setBlock(blockpos_mutableblockpos.setY(k), blockstate, 2);
         }

      }

      private boolean canPlaceColumnOnTopOf(LevelReader levelreader, BlockPos blockpos, BlockState blockstate) {
         return blockstate.isFaceSturdy(levelreader, blockpos, Direction.UP);
      }

      private boolean canHangChainBelow(LevelReader levelreader, BlockPos blockpos, BlockState blockstate) {
         return Block.canSupportCenter(levelreader, blockpos, Direction.DOWN) && !(blockstate.getBlock() instanceof FallingBlock);
      }

      private void placeSupport(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1, RandomSource randomsource) {
         if (this.isSupportingBox(worldgenlevel, boundingbox, i, i1, l, k)) {
            BlockState blockstate = this.type.getPlanksState();
            BlockState blockstate1 = this.type.getFenceState();
            this.generateBox(worldgenlevel, boundingbox, i, j, k, i, l - 1, k, blockstate1.setValue(FenceBlock.WEST, Boolean.valueOf(true)), CAVE_AIR, false);
            this.generateBox(worldgenlevel, boundingbox, i1, j, k, i1, l - 1, k, blockstate1.setValue(FenceBlock.EAST, Boolean.valueOf(true)), CAVE_AIR, false);
            if (randomsource.nextInt(4) == 0) {
               this.generateBox(worldgenlevel, boundingbox, i, l, k, i, l, k, blockstate, CAVE_AIR, false);
               this.generateBox(worldgenlevel, boundingbox, i1, l, k, i1, l, k, blockstate, CAVE_AIR, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, i, l, k, i1, l, k, blockstate, CAVE_AIR, false);
               this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, 0.05F, i + 1, l, k - 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));
               this.maybeGenerateBlock(worldgenlevel, boundingbox, randomsource, 0.05F, i + 1, l, k + 1, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH));
            }

         }
      }

      private void maybePlaceCobWeb(WorldGenLevel worldgenlevel, BoundingBox boundingbox, RandomSource randomsource, float f, int i, int j, int k) {
         if (this.isInterior(worldgenlevel, i, j, k, boundingbox) && randomsource.nextFloat() < f && this.hasSturdyNeighbours(worldgenlevel, boundingbox, i, j, k, 2)) {
            this.placeBlock(worldgenlevel, Blocks.COBWEB.defaultBlockState(), i, j, k, boundingbox);
         }

      }

      private boolean hasSturdyNeighbours(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = this.getWorldPos(i, j, k);
         int i1 = 0;

         for(Direction direction : Direction.values()) {
            blockpos_mutableblockpos.move(direction);
            if (boundingbox.isInside(blockpos_mutableblockpos) && worldgenlevel.getBlockState(blockpos_mutableblockpos).isFaceSturdy(worldgenlevel, blockpos_mutableblockpos, direction.getOpposite())) {
               ++i1;
               if (i1 >= l) {
                  return true;
               }
            }

            blockpos_mutableblockpos.move(direction.getOpposite());
         }

         return false;
      }
   }

   public static class MineShaftCrossing extends MineshaftPieces.MineShaftPiece {
      private final Direction direction;
      private final boolean isTwoFloored;

      public MineShaftCrossing(CompoundTag compoundtag) {
         super(StructurePieceType.MINE_SHAFT_CROSSING, compoundtag);
         this.isTwoFloored = compoundtag.getBoolean("tf");
         this.direction = Direction.from2DDataValue(compoundtag.getInt("D"));
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putBoolean("tf", this.isTwoFloored);
         compoundtag.putInt("D", this.direction.get2DDataValue());
      }

      public MineShaftCrossing(int i, BoundingBox boundingbox, @Nullable Direction direction, MineshaftStructure.Type mineshaftstructure_type) {
         super(StructurePieceType.MINE_SHAFT_CROSSING, i, mineshaftstructure_type, boundingbox);
         this.direction = direction;
         this.isTwoFloored = boundingbox.getYSpan() > 3;
      }

      @Nullable
      public static BoundingBox findCrossing(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction) {
         int l;
         if (randomsource.nextInt(4) == 0) {
            l = 6;
         } else {
            l = 2;
         }

         BoundingBox boundingbox;
         switch (direction) {
            case NORTH:
            default:
               boundingbox = new BoundingBox(-1, 0, -4, 3, l, 0);
               break;
            case SOUTH:
               boundingbox = new BoundingBox(-1, 0, 0, 3, l, 4);
               break;
            case WEST:
               boundingbox = new BoundingBox(-4, 0, -1, 0, l, 3);
               break;
            case EAST:
               boundingbox = new BoundingBox(0, 0, -1, 4, l, 3);
         }

         boundingbox.move(i, j, k);
         return structurepieceaccessor.findCollisionPiece(boundingbox) != null ? null : boundingbox;
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         int i = this.getGenDepth();
         switch (this.direction) {
            case NORTH:
            default:
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, i);
               break;
            case SOUTH:
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, i);
               break;
            case WEST:
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, i);
               break;
            case EAST:
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, i);
         }

         if (this.isTwoFloored) {
            if (randomsource.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() - 1, Direction.NORTH, i);
            }

            if (randomsource.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.WEST, i);
            }

            if (randomsource.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.EAST, i);
            }

            if (randomsource.nextBoolean()) {
               MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
            }
         }

      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (!this.isInInvalidLocation(worldgenlevel, boundingbox)) {
            BlockState blockstate = this.type.getPlanksState();
            if (this.isTwoFloored) {
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX() + 1, this.boundingBox.maxY() - 2, this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX(), this.boundingBox.maxY() - 2, this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3, this.boundingBox.minZ() + 1, this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
            } else {
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
               this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
            }

            this.placeSupportPillar(worldgenlevel, boundingbox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldgenlevel, boundingbox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldgenlevel, boundingbox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldgenlevel, boundingbox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            int i = this.boundingBox.minY() - 1;

            for(int j = this.boundingBox.minX(); j <= this.boundingBox.maxX(); ++j) {
               for(int k = this.boundingBox.minZ(); k <= this.boundingBox.maxZ(); ++k) {
                  this.setPlanksBlock(worldgenlevel, boundingbox, blockstate, j, i, k);
               }
            }

         }
      }

      private void placeSupportPillar(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l) {
         if (!this.getBlock(worldgenlevel, i, l + 1, k, boundingbox).isAir()) {
            this.generateBox(worldgenlevel, boundingbox, i, j, k, i, l, k, this.type.getPlanksState(), CAVE_AIR, false);
         }

      }
   }

   abstract static class MineShaftPiece extends StructurePiece {
      protected MineshaftStructure.Type type;

      public MineShaftPiece(StructurePieceType structurepiecetype, int i, MineshaftStructure.Type mineshaftstructure_type, BoundingBox boundingbox) {
         super(structurepiecetype, i, boundingbox);
         this.type = mineshaftstructure_type;
      }

      public MineShaftPiece(StructurePieceType structurepiecetype, CompoundTag compoundtag) {
         super(structurepiecetype, compoundtag);
         this.type = MineshaftStructure.Type.byId(compoundtag.getInt("MST"));
      }

      protected boolean canBeReplaced(LevelReader levelreader, int i, int j, int k, BoundingBox boundingbox) {
         BlockState blockstate = this.getBlock(levelreader, i, j, k, boundingbox);
         return !blockstate.is(this.type.getPlanksState().getBlock()) && !blockstate.is(this.type.getWoodState().getBlock()) && !blockstate.is(this.type.getFenceState().getBlock()) && !blockstate.is(Blocks.CHAIN);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         compoundtag.putInt("MST", this.type.ordinal());
      }

      protected boolean isSupportingBox(BlockGetter blockgetter, BoundingBox boundingbox, int i, int j, int k, int l) {
         for(int i1 = i; i1 <= j; ++i1) {
            if (this.getBlock(blockgetter, i1, k + 1, l, boundingbox).isAir()) {
               return false;
            }
         }

         return true;
      }

      protected boolean isInInvalidLocation(LevelAccessor levelaccessor, BoundingBox boundingbox) {
         int i = Math.max(this.boundingBox.minX() - 1, boundingbox.minX());
         int j = Math.max(this.boundingBox.minY() - 1, boundingbox.minY());
         int k = Math.max(this.boundingBox.minZ() - 1, boundingbox.minZ());
         int l = Math.min(this.boundingBox.maxX() + 1, boundingbox.maxX());
         int i1 = Math.min(this.boundingBox.maxY() + 1, boundingbox.maxY());
         int j1 = Math.min(this.boundingBox.maxZ() + 1, boundingbox.maxZ());
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos((i + l) / 2, (j + i1) / 2, (k + j1) / 2);
         if (levelaccessor.getBiome(blockpos_mutableblockpos).is(BiomeTags.MINESHAFT_BLOCKING)) {
            return true;
         } else {
            for(int k1 = i; k1 <= l; ++k1) {
               for(int l1 = k; l1 <= j1; ++l1) {
                  if (levelaccessor.getBlockState(blockpos_mutableblockpos.set(k1, j, l1)).liquid()) {
                     return true;
                  }

                  if (levelaccessor.getBlockState(blockpos_mutableblockpos.set(k1, i1, l1)).liquid()) {
                     return true;
                  }
               }
            }

            for(int i2 = i; i2 <= l; ++i2) {
               for(int j2 = j; j2 <= i1; ++j2) {
                  if (levelaccessor.getBlockState(blockpos_mutableblockpos.set(i2, j2, k)).liquid()) {
                     return true;
                  }

                  if (levelaccessor.getBlockState(blockpos_mutableblockpos.set(i2, j2, j1)).liquid()) {
                     return true;
                  }
               }
            }

            for(int k2 = k; k2 <= j1; ++k2) {
               for(int l2 = j; l2 <= i1; ++l2) {
                  if (levelaccessor.getBlockState(blockpos_mutableblockpos.set(i, l2, k2)).liquid()) {
                     return true;
                  }

                  if (levelaccessor.getBlockState(blockpos_mutableblockpos.set(l, l2, k2)).liquid()) {
                     return true;
                  }
               }
            }

            return false;
         }
      }

      protected void setPlanksBlock(WorldGenLevel worldgenlevel, BoundingBox boundingbox, BlockState blockstate, int i, int j, int k) {
         if (this.isInterior(worldgenlevel, i, j, k, boundingbox)) {
            BlockPos blockpos = this.getWorldPos(i, j, k);
            BlockState blockstate1 = worldgenlevel.getBlockState(blockpos);
            if (!blockstate1.isFaceSturdy(worldgenlevel, blockpos, Direction.UP)) {
               worldgenlevel.setBlock(blockpos, blockstate, 2);
            }

         }
      }
   }

   public static class MineShaftRoom extends MineshaftPieces.MineShaftPiece {
      private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

      public MineShaftRoom(int i, RandomSource randomsource, int j, int k, MineshaftStructure.Type mineshaftstructure_type) {
         super(StructurePieceType.MINE_SHAFT_ROOM, i, mineshaftstructure_type, new BoundingBox(j, 50, k, j + 7 + randomsource.nextInt(6), 54 + randomsource.nextInt(6), k + 7 + randomsource.nextInt(6)));
         this.type = mineshaftstructure_type;
      }

      public MineShaftRoom(CompoundTag compoundtag) {
         super(StructurePieceType.MINE_SHAFT_ROOM, compoundtag);
         BoundingBox.CODEC.listOf().parse(NbtOps.INSTANCE, compoundtag.getList("Entrances", 11)).resultOrPartial(MineshaftPieces.LOGGER::error).ifPresent(this.childEntranceBoxes::addAll);
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         int i = this.getGenDepth();
         int j = this.boundingBox.getYSpan() - 3 - 1;
         if (j <= 0) {
            j = 1;
         }

         int k;
         for(k = 0; k < this.boundingBox.getXSpan(); k += 4) {
            k += randomsource.nextInt(this.boundingBox.getXSpan());
            if (k + 3 > this.boundingBox.getXSpan()) {
               break;
            }

            MineshaftPieces.MineShaftPiece mineshaftpieces_mineshaftpiece = MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + k, this.boundingBox.minY() + randomsource.nextInt(j) + 1, this.boundingBox.minZ() - 1, Direction.NORTH, i);
            if (mineshaftpieces_mineshaftpiece != null) {
               BoundingBox boundingbox = mineshaftpieces_mineshaftpiece.getBoundingBox();
               this.childEntranceBoxes.add(new BoundingBox(boundingbox.minX(), boundingbox.minY(), this.boundingBox.minZ(), boundingbox.maxX(), boundingbox.maxY(), this.boundingBox.minZ() + 1));
            }
         }

         for(k = 0; k < this.boundingBox.getXSpan(); k += 4) {
            k += randomsource.nextInt(this.boundingBox.getXSpan());
            if (k + 3 > this.boundingBox.getXSpan()) {
               break;
            }

            MineshaftPieces.MineShaftPiece mineshaftpieces_mineshaftpiece1 = MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() + k, this.boundingBox.minY() + randomsource.nextInt(j) + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
            if (mineshaftpieces_mineshaftpiece1 != null) {
               BoundingBox boundingbox1 = mineshaftpieces_mineshaftpiece1.getBoundingBox();
               this.childEntranceBoxes.add(new BoundingBox(boundingbox1.minX(), boundingbox1.minY(), this.boundingBox.maxZ() - 1, boundingbox1.maxX(), boundingbox1.maxY(), this.boundingBox.maxZ()));
            }
         }

         for(k = 0; k < this.boundingBox.getZSpan(); k += 4) {
            k += randomsource.nextInt(this.boundingBox.getZSpan());
            if (k + 3 > this.boundingBox.getZSpan()) {
               break;
            }

            MineshaftPieces.MineShaftPiece mineshaftpieces_mineshaftpiece2 = MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY() + randomsource.nextInt(j) + 1, this.boundingBox.minZ() + k, Direction.WEST, i);
            if (mineshaftpieces_mineshaftpiece2 != null) {
               BoundingBox boundingbox2 = mineshaftpieces_mineshaftpiece2.getBoundingBox();
               this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.minX(), boundingbox2.minY(), boundingbox2.minZ(), this.boundingBox.minX() + 1, boundingbox2.maxY(), boundingbox2.maxZ()));
            }
         }

         for(k = 0; k < this.boundingBox.getZSpan(); k += 4) {
            k += randomsource.nextInt(this.boundingBox.getZSpan());
            if (k + 3 > this.boundingBox.getZSpan()) {
               break;
            }

            StructurePiece structurepiece1 = MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + randomsource.nextInt(j) + 1, this.boundingBox.minZ() + k, Direction.EAST, i);
            if (structurepiece1 != null) {
               BoundingBox boundingbox3 = structurepiece1.getBoundingBox();
               this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.maxX() - 1, boundingbox3.minY(), boundingbox3.minZ(), this.boundingBox.maxX(), boundingbox3.maxY(), boundingbox3.maxZ()));
            }
         }

      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (!this.isInInvalidLocation(worldgenlevel, boundingbox)) {
            this.generateBox(worldgenlevel, boundingbox, this.boundingBox.minX(), this.boundingBox.minY() + 1, this.boundingBox.minZ(), this.boundingBox.maxX(), Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);

            for(BoundingBox boundingbox1 : this.childEntranceBoxes) {
               this.generateBox(worldgenlevel, boundingbox, boundingbox1.minX(), boundingbox1.maxY() - 2, boundingbox1.minZ(), boundingbox1.maxX(), boundingbox1.maxY(), boundingbox1.maxZ(), CAVE_AIR, CAVE_AIR, false);
            }

            this.generateUpperHalfSphere(worldgenlevel, boundingbox, this.boundingBox.minX(), this.boundingBox.minY() + 4, this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, false);
         }
      }

      public void move(int i, int j, int k) {
         super.move(i, j, k);

         for(BoundingBox boundingbox : this.childEntranceBoxes) {
            boundingbox.move(i, j, k);
         }

      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         BoundingBox.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.childEntranceBoxes).resultOrPartial(MineshaftPieces.LOGGER::error).ifPresent((tag) -> compoundtag.put("Entrances", tag));
      }
   }

   public static class MineShaftStairs extends MineshaftPieces.MineShaftPiece {
      public MineShaftStairs(int i, BoundingBox boundingbox, Direction direction, MineshaftStructure.Type mineshaftstructure_type) {
         super(StructurePieceType.MINE_SHAFT_STAIRS, i, mineshaftstructure_type, boundingbox);
         this.setOrientation(direction);
      }

      public MineShaftStairs(CompoundTag compoundtag) {
         super(StructurePieceType.MINE_SHAFT_STAIRS, compoundtag);
      }

      @Nullable
      public static BoundingBox findStairs(StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, int i, int j, int k, Direction direction) {
         BoundingBox boundingbox;
         switch (direction) {
            case NORTH:
            default:
               boundingbox = new BoundingBox(0, -5, -8, 2, 2, 0);
               break;
            case SOUTH:
               boundingbox = new BoundingBox(0, -5, 0, 2, 2, 8);
               break;
            case WEST:
               boundingbox = new BoundingBox(-8, -5, 0, 0, 2, 2);
               break;
            case EAST:
               boundingbox = new BoundingBox(0, -5, 0, 8, 2, 2);
         }

         boundingbox.move(i, j, k);
         return structurepieceaccessor.findCollisionPiece(boundingbox) != null ? null : boundingbox;
      }

      public void addChildren(StructurePiece structurepiece, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
         int i = this.getGenDepth();
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch (direction) {
               case NORTH:
               default:
                  MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, i);
                  break;
               case SOUTH:
                  MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, i);
                  break;
               case WEST:
                  MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, i);
                  break;
               case EAST:
                  MineshaftPieces.generateAndAddPiece(structurepiece, structurepieceaccessor, randomsource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, i);
            }
         }

      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         if (!this.isInInvalidLocation(worldgenlevel, boundingbox)) {
            this.generateBox(worldgenlevel, boundingbox, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldgenlevel, boundingbox, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);

            for(int i = 0; i < 5; ++i) {
               this.generateBox(worldgenlevel, boundingbox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
            }

         }
      }
   }
}
