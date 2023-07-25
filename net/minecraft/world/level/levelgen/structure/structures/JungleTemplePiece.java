package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class JungleTemplePiece extends ScatteredFeaturePiece {
   public static final int WIDTH = 12;
   public static final int DEPTH = 15;
   private boolean placedMainChest;
   private boolean placedHiddenChest;
   private boolean placedTrap1;
   private boolean placedTrap2;
   private static final JungleTemplePiece.MossStoneSelector STONE_SELECTOR = new JungleTemplePiece.MossStoneSelector();

   public JungleTemplePiece(RandomSource randomsource, int i, int j) {
      super(StructurePieceType.JUNGLE_PYRAMID_PIECE, i, 64, j, 12, 10, 15, getRandomHorizontalDirection(randomsource));
   }

   public JungleTemplePiece(CompoundTag compoundtag) {
      super(StructurePieceType.JUNGLE_PYRAMID_PIECE, compoundtag);
      this.placedMainChest = compoundtag.getBoolean("placedMainChest");
      this.placedHiddenChest = compoundtag.getBoolean("placedHiddenChest");
      this.placedTrap1 = compoundtag.getBoolean("placedTrap1");
      this.placedTrap2 = compoundtag.getBoolean("placedTrap2");
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
      compoundtag.putBoolean("placedMainChest", this.placedMainChest);
      compoundtag.putBoolean("placedHiddenChest", this.placedHiddenChest);
      compoundtag.putBoolean("placedTrap1", this.placedTrap1);
      compoundtag.putBoolean("placedTrap2", this.placedTrap2);
   }

   public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
      if (this.updateAverageGroundHeight(worldgenlevel, boundingbox, 0)) {
         this.generateBox(worldgenlevel, boundingbox, 0, -4, 0, this.width - 1, 0, this.depth - 1, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 2, 9, 2, 2, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 12, 9, 2, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 1, 3, 2, 2, 11, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 9, 1, 3, 9, 2, 11, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 1, 10, 6, 1, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 13, 10, 6, 13, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 1, 3, 2, 1, 6, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 10, 3, 2, 10, 6, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 3, 2, 9, 3, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 6, 2, 9, 6, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 3, 7, 3, 8, 7, 11, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 8, 4, 7, 8, 10, false, randomsource, STONE_SELECTOR);
         this.generateAirBox(worldgenlevel, boundingbox, 3, 1, 3, 8, 2, 11);
         this.generateAirBox(worldgenlevel, boundingbox, 4, 3, 6, 7, 3, 9);
         this.generateAirBox(worldgenlevel, boundingbox, 2, 4, 2, 9, 5, 12);
         this.generateAirBox(worldgenlevel, boundingbox, 4, 6, 5, 7, 6, 9);
         this.generateAirBox(worldgenlevel, boundingbox, 5, 7, 6, 6, 7, 8);
         this.generateAirBox(worldgenlevel, boundingbox, 5, 1, 2, 6, 2, 2);
         this.generateAirBox(worldgenlevel, boundingbox, 5, 2, 12, 6, 2, 12);
         this.generateAirBox(worldgenlevel, boundingbox, 5, 5, 1, 6, 5, 1);
         this.generateAirBox(worldgenlevel, boundingbox, 5, 5, 13, 6, 5, 13);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 1, 5, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 10, 5, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 1, 5, 9, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.AIR.defaultBlockState(), 10, 5, 9, boundingbox);

         for(int i = 0; i <= 14; i += 14) {
            this.generateBox(worldgenlevel, boundingbox, 2, 4, i, 2, 5, i, false, randomsource, STONE_SELECTOR);
            this.generateBox(worldgenlevel, boundingbox, 4, 4, i, 4, 5, i, false, randomsource, STONE_SELECTOR);
            this.generateBox(worldgenlevel, boundingbox, 7, 4, i, 7, 5, i, false, randomsource, STONE_SELECTOR);
            this.generateBox(worldgenlevel, boundingbox, 9, 4, i, 9, 5, i, false, randomsource, STONE_SELECTOR);
         }

         this.generateBox(worldgenlevel, boundingbox, 5, 6, 0, 6, 6, 0, false, randomsource, STONE_SELECTOR);

         for(int j = 0; j <= 11; j += 11) {
            for(int k = 2; k <= 12; k += 2) {
               this.generateBox(worldgenlevel, boundingbox, j, 4, k, j, 5, k, false, randomsource, STONE_SELECTOR);
            }

            this.generateBox(worldgenlevel, boundingbox, j, 6, 5, j, 6, 5, false, randomsource, STONE_SELECTOR);
            this.generateBox(worldgenlevel, boundingbox, j, 6, 9, j, 6, 9, false, randomsource, STONE_SELECTOR);
         }

         this.generateBox(worldgenlevel, boundingbox, 2, 7, 2, 2, 9, 2, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 9, 7, 2, 9, 9, 2, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 2, 7, 12, 2, 9, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 9, 7, 12, 9, 9, 12, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 9, 4, 4, 9, 4, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 7, 9, 4, 7, 9, 4, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 9, 10, 4, 9, 10, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 7, 9, 10, 7, 9, 10, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 5, 9, 7, 6, 9, 7, false, randomsource, STONE_SELECTOR);
         BlockState blockstate = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST);
         BlockState blockstate1 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST);
         BlockState blockstate2 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
         BlockState blockstate3 = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         this.placeBlock(worldgenlevel, blockstate3, 5, 9, 6, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 6, 9, 6, boundingbox);
         this.placeBlock(worldgenlevel, blockstate2, 5, 9, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate2, 6, 9, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 4, 0, 0, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 5, 0, 0, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 6, 0, 0, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 7, 0, 0, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 4, 1, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 4, 2, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 4, 3, 10, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 7, 1, 8, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 7, 2, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate3, 7, 3, 10, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 9, 4, 1, 9, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 7, 1, 9, 7, 1, 9, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 4, 1, 10, 7, 2, 10, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 5, 4, 5, 6, 4, 5, false, randomsource, STONE_SELECTOR);
         this.placeBlock(worldgenlevel, blockstate, 4, 4, 5, boundingbox);
         this.placeBlock(worldgenlevel, blockstate1, 7, 4, 5, boundingbox);

         for(int l = 0; l < 4; ++l) {
            this.placeBlock(worldgenlevel, blockstate2, 5, 0 - l, 6 + l, boundingbox);
            this.placeBlock(worldgenlevel, blockstate2, 6, 0 - l, 6 + l, boundingbox);
            this.generateAirBox(worldgenlevel, boundingbox, 5, 0 - l, 7 + l, 6, 0 - l, 9 + l);
         }

         this.generateAirBox(worldgenlevel, boundingbox, 1, -3, 12, 10, -1, 13);
         this.generateAirBox(worldgenlevel, boundingbox, 1, -3, 1, 3, -1, 13);
         this.generateAirBox(worldgenlevel, boundingbox, 1, -3, 1, 9, -1, 5);

         for(int i1 = 1; i1 <= 13; i1 += 2) {
            this.generateBox(worldgenlevel, boundingbox, 1, -3, i1, 1, -2, i1, false, randomsource, STONE_SELECTOR);
         }

         for(int j1 = 2; j1 <= 12; j1 += 2) {
            this.generateBox(worldgenlevel, boundingbox, 1, -1, j1, 3, -1, j1, false, randomsource, STONE_SELECTOR);
         }

         this.generateBox(worldgenlevel, boundingbox, 2, -2, 1, 5, -2, 1, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 7, -2, 1, 9, -2, 1, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 6, -3, 1, 6, -3, 1, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 6, -1, 1, 6, -1, 1, false, randomsource, STONE_SELECTOR);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.EAST).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 1, -3, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.WEST).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 4, -3, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.EAST, Boolean.valueOf(true)).setValue(TripWireBlock.WEST, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 2, -3, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.EAST, Boolean.valueOf(true)).setValue(TripWireBlock.WEST, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 3, -3, 8, boundingbox);
         BlockState blockstate4 = Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE);
         this.placeBlock(worldgenlevel, blockstate4, 5, -3, 7, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 5, -3, 6, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 5, -3, 5, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 5, -3, 4, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 5, -3, 3, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 5, -3, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 5, -3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 4, -3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 3, -3, 1, boundingbox);
         if (!this.placedTrap1) {
            this.placedTrap1 = this.createDispenser(worldgenlevel, boundingbox, randomsource, 3, -2, 1, Direction.NORTH, BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
         }

         this.placeBlock(worldgenlevel, Blocks.VINE.defaultBlockState().setValue(VineBlock.SOUTH, Boolean.valueOf(true)), 3, -2, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.NORTH).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE_HOOK.defaultBlockState().setValue(TripWireHookBlock.FACING, Direction.SOUTH).setValue(TripWireHookBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.NORTH, Boolean.valueOf(true)).setValue(TripWireBlock.SOUTH, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.NORTH, Boolean.valueOf(true)).setValue(TripWireBlock.SOUTH, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.NORTH, Boolean.valueOf(true)).setValue(TripWireBlock.SOUTH, Boolean.valueOf(true)).setValue(TripWireBlock.ATTACHED, Boolean.valueOf(true)), 7, -3, 4, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 8, -3, 6, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE), 9, -3, 6, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.UP), 9, -3, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 9, -3, 4, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 9, -2, 4, boundingbox);
         if (!this.placedTrap2) {
            this.placedTrap2 = this.createDispenser(worldgenlevel, boundingbox, randomsource, 9, -2, 3, Direction.WEST, BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER);
         }

         this.placeBlock(worldgenlevel, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, Boolean.valueOf(true)), 8, -1, 3, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.VINE.defaultBlockState().setValue(VineBlock.EAST, Boolean.valueOf(true)), 8, -2, 3, boundingbox);
         if (!this.placedMainChest) {
            this.placedMainChest = this.createChest(worldgenlevel, boundingbox, randomsource, 8, -3, 3, BuiltInLootTables.JUNGLE_TEMPLE);
         }

         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 9, -3, 2, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 8, -3, 1, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 4, -3, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 5, -2, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 5, -1, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 6, -3, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 7, -2, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 7, -1, 5, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 8, -3, 5, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 9, -1, 1, 9, -1, 5, false, randomsource, STONE_SELECTOR);
         this.generateAirBox(worldgenlevel, boundingbox, 8, -3, 8, 10, -1, 10);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 8, -2, 11, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 9, -2, 11, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 10, -2, 11, boundingbox);
         BlockState blockstate5 = Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACING, Direction.NORTH).setValue(LeverBlock.FACE, AttachFace.WALL);
         this.placeBlock(worldgenlevel, blockstate5, 8, -2, 12, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5, 9, -2, 12, boundingbox);
         this.placeBlock(worldgenlevel, blockstate5, 10, -2, 12, boundingbox);
         this.generateBox(worldgenlevel, boundingbox, 8, -3, 8, 8, -3, 10, false, randomsource, STONE_SELECTOR);
         this.generateBox(worldgenlevel, boundingbox, 10, -3, 8, 10, -3, 10, false, randomsource, STONE_SELECTOR);
         this.placeBlock(worldgenlevel, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 10, -2, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 8, -2, 9, boundingbox);
         this.placeBlock(worldgenlevel, blockstate4, 8, -2, 10, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REDSTONE_WIRE.defaultBlockState().setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE).setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE).setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE), 10, -1, 9, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.UP), 9, -2, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), 10, -2, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, Direction.WEST), 10, -1, 8, boundingbox);
         this.placeBlock(worldgenlevel, Blocks.REPEATER.defaultBlockState().setValue(RepeaterBlock.FACING, Direction.NORTH), 10, -2, 10, boundingbox);
         if (!this.placedHiddenChest) {
            this.placedHiddenChest = this.createChest(worldgenlevel, boundingbox, randomsource, 9, -3, 10, BuiltInLootTables.JUNGLE_TEMPLE);
         }

      }
   }

   static class MossStoneSelector extends StructurePiece.BlockSelector {
      public void next(RandomSource randomsource, int i, int j, int k, boolean flag) {
         if (randomsource.nextFloat() < 0.4F) {
            this.next = Blocks.COBBLESTONE.defaultBlockState();
         } else {
            this.next = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
         }

      }
   }
}
