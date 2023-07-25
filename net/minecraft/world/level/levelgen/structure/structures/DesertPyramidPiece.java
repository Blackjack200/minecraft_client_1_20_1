package net.minecraft.world.level.levelgen.structure.structures;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidPiece extends ScatteredFeaturePiece {
   public static final int WIDTH = 21;
   public static final int DEPTH = 21;
   private final boolean[] hasPlacedChest = new boolean[4];
   private final List<BlockPos> potentialSuspiciousSandWorldPositions = new ArrayList<>();
   private BlockPos randomCollapsedRoofPos = BlockPos.ZERO;

   public DesertPyramidPiece(RandomSource randomsource, int i, int j) {
      super(StructurePieceType.DESERT_PYRAMID_PIECE, i, 64, j, 21, 15, 21, getRandomHorizontalDirection(randomsource));
   }

   public DesertPyramidPiece(CompoundTag compoundtag) {
      super(StructurePieceType.DESERT_PYRAMID_PIECE, compoundtag);
      this.hasPlacedChest[0] = compoundtag.getBoolean("hasPlacedChest0");
      this.hasPlacedChest[1] = compoundtag.getBoolean("hasPlacedChest1");
      this.hasPlacedChest[2] = compoundtag.getBoolean("hasPlacedChest2");
      this.hasPlacedChest[3] = compoundtag.getBoolean("hasPlacedChest3");
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
      compoundtag.putBoolean("hasPlacedChest0", this.hasPlacedChest[0]);
      compoundtag.putBoolean("hasPlacedChest1", this.hasPlacedChest[1]);
      compoundtag.putBoolean("hasPlacedChest2", this.hasPlacedChest[2]);
      compoundtag.putBoolean("hasPlacedChest3", this.hasPlacedChest[3]);
   }

   public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
      if (this.updateHeightPositionToLowestGroundHeight(worldgenlevel, -randomsource.nextInt(3))) {
         this.generateBox(worldgenlevel, boundingbox, 0, -4, 0, this.width - 1, 0, this.depth - 1, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);

         for(int i = 1; i <= 9; ++i) {
            this.generateBox(worldgenlevel, boundingbox, i, i, i, this.width - 1 - i, i, this.depth - 1 - i, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
            this.generateBox(worldgenlevel, boundingbox, i + 1, i, i + 1, this.width - 2 - i, i, this.depth - 2 - i, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         }

         for(int j = 0; j < this.width; ++j) {
            for(int k = 0; k < this.depth; ++k) {
               int l = -5;
               this.fillColumnDown(worldgenlevel, Blocks.SANDSTONE.defaultBlockState(), j, -5, k, boundingbox);
            }
         }

         BlockState blockstate = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         BlockState blockstate1 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         BlockState blockstate2 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate3 = Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         this.generateBox(worldgenlevel, boundingbox, 0, 0, 0, 4, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 10, 1, 3, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, blockstate, 2, 10, 0, boundingbox);
         this.placeBlock(worldgenlevel, blockstate1, 2, 10, 4, boundingbox);
         this.placeBlock(worldgenlevel, blockstate2, 0, 10, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 4, 10, 2, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, this.width - 5, 0, 0, this.width - 1, 9, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 4, 10, 1, this.width - 2, 10, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, blockstate, this.width - 3, 10, 0, boundingbox);
         this.placeBlock(worldgenlevel, blockstate1, this.width - 3, 10, 4, boundingbox);
         this.placeBlock(worldgenlevel, blockstate2, this.width - 5, 10, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, this.width - 1, 10, 2, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 8, 0, 0, 12, 4, 4, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 1, 0, 11, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 1, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 2, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 9, 3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, 3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 2, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 11, 1, 1, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 1, 8, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 2, 8, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 12, 1, 1, 16, 3, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 12, 1, 2, 16, 2, 2, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 4, 5, this.width - 6, 4, this.depth - 6, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, 4, 9, 11, 4, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 1, 8, 8, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 12, 1, 8, 12, 3, 8, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, 1, 12, 8, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 12, 1, 12, 12, 3, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 5, 4, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 5, 1, 5, this.width - 2, 4, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 6, 7, 9, 6, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 7, 7, 9, this.width - 7, 7, 11, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 5, 5, 9, 5, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 6, 5, 9, this.width - 6, 7, 11, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 5, 5, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 5, 6, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 6, 6, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), this.width - 6, 5, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), this.width - 6, 6, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), this.width - 7, 6, 10, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 2, 4, 4, 2, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 3, 4, 4, this.width - 3, 6, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, blockstate, 2, 4, 5, boundingbox);
         this.placeBlock(worldgenlevel, blockstate, 2, 3, 4, boundingbox);
         this.placeBlock(worldgenlevel, blockstate, this.width - 3, 4, 5, boundingbox);
         this.placeBlock(worldgenlevel, blockstate, this.width - 3, 3, 4, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 1, 1, 3, 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 3, 1, 3, this.width - 2, 2, 3, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.SANDSTONE.defaultBlockState(), 1, 1, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SANDSTONE.defaultBlockState(), this.width - 2, 1, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), 1, 2, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.SANDSTONE_SLAB.defaultBlockState(), this.width - 2, 2, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 2, 1, 2, boundingbox);
         this.placeBlock(worldgenlevel, blockstate2, this.width - 3, 1, 2, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 4, 3, 5, 4, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 5, 3, 5, this.width - 5, 3, 17, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 3, 1, 5, 4, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, this.width - 6, 1, 5, this.width - 5, 2, 16, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);

         for(int i1 = 5; i1 <= 17; i1 += 2) {
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 4, 1, i1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 4, 2, i1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), this.width - 5, 1, i1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), this.width - 5, 2, i1, boundingbox);
         }

         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 7, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 9, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 9, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 8, 0, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 12, 0, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 7, 0, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 13, 0, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 0, 11, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 0, 11, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 10, 0, 13, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.BLUE_TERRACOTTA.defaultBlockState(), 10, 0, 10, boundingbox);

         for(int j1 = 0; j1 <= this.width - 1; j1 += this.width - 1) {
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 2, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 2, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 2, 3, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 3, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 3, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 3, 3, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 4, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), j1, 4, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 4, 3, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 5, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 5, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 5, 3, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 6, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), j1, 6, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 6, 3, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 7, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 7, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), j1, 7, 3, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 8, 1, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 8, 2, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), j1, 8, 3, boundingbox);
         }

         for(int k1 = 2; k1 <= this.width - 3; k1 += this.width - 3 - 2) {
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 - 1, 2, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1, 2, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 + 1, 2, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 - 1, 3, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1, 3, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 + 1, 3, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1 - 1, 4, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), k1, 4, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1 + 1, 4, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 - 1, 5, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1, 5, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 + 1, 5, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1 - 1, 6, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), k1, 6, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1 + 1, 6, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1 - 1, 7, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1, 7, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), k1 + 1, 7, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 - 1, 8, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1, 8, 0, boundingbox);
            this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), k1 + 1, 8, 0, boundingbox);
         }

         this.generateBox(worldgenlevel, boundingbox, 8, 4, 0, 12, 6, 0, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 8, 6, 0, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 12, 6, 0, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 9, 5, 0, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, 5, 0, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.ORANGE_TERRACOTTA.defaultBlockState(), 11, 5, 0, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 8, -14, 8, 12, -11, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, -10, 8, 12, -10, 12, Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, -9, 8, 12, -9, 12, Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.CUT_SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 8, -8, 8, 12, -1, 12, Blocks.SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState(), false);
         this.generateBox(worldgenlevel, boundingbox, 9, -11, 9, 11, -1, 11, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 10, -11, 10, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 9, -13, 9, 11, -13, 11, Blocks.TNT.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 8, -11, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 8, -10, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 7, -10, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 7, -11, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 12, -11, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 12, -10, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 13, -10, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 13, -11, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 10, -11, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 10, -10, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 7, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 7, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 10, -11, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 10, -10, 12, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_SANDSTONE.defaultBlockState(), 10, -10, 13, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CUT_SANDSTONE.defaultBlockState(), 10, -11, 13, boundingbox);

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (!this.hasPlacedChest[direction.get2DDataValue()]) {
               int l1 = direction.getStepX() * 2;
               int i2 = direction.getStepZ() * 2;
               this.hasPlacedChest[direction.get2DDataValue()] = this.createChest(worldgenlevel, boundingbox, randomsource, 10 + l1, -11, 10 + i2, BuiltInLootTables.DESERT_PYRAMID);
            }
         }

         this.addCellar(worldgenlevel, boundingbox);
      }
   }

   private void addCellar(WorldGenLevel worldgenlevel, BoundingBox boundingbox) {
      BlockPos blockpos = new BlockPos(16, -4, 13);
      this.addCellarStairs(blockpos, worldgenlevel, boundingbox);
      this.addCellarRoom(blockpos, worldgenlevel, boundingbox);
   }

   private void addCellarStairs(BlockPos blockpos, WorldGenLevel worldgenlevel, BoundingBox boundingbox) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      BlockState blockstate = Blocks.SANDSTONE_STAIRS.defaultBlockState();
      this.placeBlock(worldgenlevel, blockstate.rotate(Rotation.COUNTERCLOCKWISE_90), 13, -1, 17, boundingbox);
      this.placeBlock(worldgenlevel, blockstate.rotate(Rotation.COUNTERCLOCKWISE_90), 14, -2, 17, boundingbox);
      this.placeBlock(worldgenlevel, blockstate.rotate(Rotation.COUNTERCLOCKWISE_90), 15, -3, 17, boundingbox);
      BlockState blockstate1 = Blocks.SAND.defaultBlockState();
      BlockState blockstate2 = Blocks.SANDSTONE.defaultBlockState();
      boolean flag = worldgenlevel.getRandom().nextBoolean();
      this.placeBlock(worldgenlevel, blockstate1, i - 4, j + 4, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i - 3, j + 4, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i - 2, j + 4, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i - 1, j + 4, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i, j + 4, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i - 2, j + 3, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, flag ? blockstate1 : blockstate2, i - 1, j + 3, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, !flag ? blockstate1 : blockstate2, i, j + 3, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i - 1, j + 2, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i, j + 2, k + 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i, j + 1, k + 4, boundingbox);
   }

   private void addCellarRoom(BlockPos blockpos, WorldGenLevel worldgenlevel, BoundingBox boundingbox) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      BlockState blockstate = Blocks.CUT_SANDSTONE.defaultBlockState();
      BlockState blockstate1 = Blocks.CHISELED_SANDSTONE.defaultBlockState();
      this.generateBox(worldgenlevel, boundingbox, i - 3, j + 1, k - 3, i - 3, j + 1, k + 2, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i + 3, j + 1, k - 3, i + 3, j + 1, k + 2, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, j + 1, k - 3, i + 3, j + 1, k - 2, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, j + 1, k + 3, i + 3, j + 1, k + 3, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, j + 2, k - 3, i - 3, j + 2, k + 2, blockstate1, blockstate1, true);
      this.generateBox(worldgenlevel, boundingbox, i + 3, j + 2, k - 3, i + 3, j + 2, k + 2, blockstate1, blockstate1, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, j + 2, k - 3, i + 3, j + 2, k - 2, blockstate1, blockstate1, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, j + 2, k + 3, i + 3, j + 2, k + 3, blockstate1, blockstate1, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, -1, k - 3, i - 3, -1, k + 2, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i + 3, -1, k - 3, i + 3, -1, k + 2, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, -1, k - 3, i + 3, -1, k - 2, blockstate, blockstate, true);
      this.generateBox(worldgenlevel, boundingbox, i - 3, -1, k + 3, i + 3, -1, k + 3, blockstate, blockstate, true);
      this.placeSandBox(i - 2, j + 1, k - 2, i + 2, j + 3, k + 2);
      this.placeCollapsedRoof(worldgenlevel, boundingbox, i - 2, j + 4, k - 2, i + 2, k + 2);
      BlockState blockstate2 = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
      BlockState blockstate3 = Blocks.BLUE_TERRACOTTA.defaultBlockState();
      this.placeBlock(worldgenlevel, blockstate3, i, j, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i + 1, j, k - 1, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i + 1, j, k + 1, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i - 1, j, k - 1, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i - 1, j, k + 1, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i + 2, j, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i - 2, j, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i, j, k + 2, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i, j, k - 2, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i + 3, j, k, boundingbox);
      this.placeSand(i + 3, j + 1, k);
      this.placeSand(i + 3, j + 2, k);
      this.placeBlock(worldgenlevel, blockstate, i + 4, j + 1, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i + 4, j + 2, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i - 3, j, k, boundingbox);
      this.placeSand(i - 3, j + 1, k);
      this.placeSand(i - 3, j + 2, k);
      this.placeBlock(worldgenlevel, blockstate, i - 4, j + 1, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i - 4, j + 2, k, boundingbox);
      this.placeBlock(worldgenlevel, blockstate2, i, j, k + 3, boundingbox);
      this.placeSand(i, j + 1, k + 3);
      this.placeSand(i, j + 2, k + 3);
      this.placeBlock(worldgenlevel, blockstate2, i, j, k - 3, boundingbox);
      this.placeSand(i, j + 1, k - 3);
      this.placeSand(i, j + 2, k - 3);
      this.placeBlock(worldgenlevel, blockstate, i, j + 1, k - 4, boundingbox);
      this.placeBlock(worldgenlevel, blockstate1, i, -2, k - 4, boundingbox);
   }

   private void placeSand(int i, int j, int k) {
      BlockPos blockpos = this.getWorldPos(i, j, k);
      this.potentialSuspiciousSandWorldPositions.add(blockpos);
   }

   private void placeSandBox(int i, int j, int k, int l, int i1, int j1) {
      for(int k1 = j; k1 <= i1; ++k1) {
         for(int l1 = i; l1 <= l; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               this.placeSand(l1, k1, i2);
            }
         }
      }

   }

   private void placeCollapsedRoofPiece(WorldGenLevel worldgenlevel, int i, int j, int k, BoundingBox boundingbox) {
      if (worldgenlevel.getRandom().nextFloat() < 0.33F) {
         BlockState blockstate = Blocks.SANDSTONE.defaultBlockState();
         this.placeBlock(worldgenlevel, blockstate, i, j, k, boundingbox);
      } else {
         BlockState blockstate1 = Blocks.SAND.defaultBlockState();
         this.placeBlock(worldgenlevel, blockstate1, i, j, k, boundingbox);
      }

   }

   private void placeCollapsedRoof(WorldGenLevel worldgenlevel, BoundingBox boundingbox, int i, int j, int k, int l, int i1) {
      for(int j1 = i; j1 <= l; ++j1) {
         for(int k1 = k; k1 <= i1; ++k1) {
            this.placeCollapsedRoofPiece(worldgenlevel, j1, j, k1, boundingbox);
         }
      }

      RandomSource randomsource = RandomSource.create(worldgenlevel.getSeed()).forkPositional().at(this.getWorldPos(i, j, k));
      int l1 = randomsource.nextIntBetweenInclusive(i, l);
      int i2 = randomsource.nextIntBetweenInclusive(k, i1);
      this.randomCollapsedRoofPos = new BlockPos(this.getWorldX(l1, i2), this.getWorldY(j), this.getWorldZ(l1, i2));
   }

   public List<BlockPos> getPotentialSuspiciousSandWorldPositions() {
      return this.potentialSuspiciousSandWorldPositions;
   }

   public BlockPos getRandomCollapsedRoofPos() {
      return this.randomCollapsedRoofPos;
   }
}
