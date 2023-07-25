package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidStructure extends SinglePieceStructure {
   public static final Codec<DesertPyramidStructure> CODEC = simpleCodec(DesertPyramidStructure::new);

   public DesertPyramidStructure(Structure.StructureSettings structure_structuresettings) {
      super(DesertPyramidPiece::new, 21, 21, structure_structuresettings);
   }

   public void afterPlace(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, PiecesContainer piecescontainer) {
      Set<BlockPos> set = SortedArraySet.create(Vec3i::compareTo);

      for(StructurePiece structurepiece : piecescontainer.pieces()) {
         if (structurepiece instanceof DesertPyramidPiece desertpyramidpiece) {
            set.addAll(desertpyramidpiece.getPotentialSuspiciousSandWorldPositions());
            placeSuspiciousSand(boundingbox, worldgenlevel, desertpyramidpiece.getRandomCollapsedRoofPos());
         }
      }

      ObjectArrayList<BlockPos> objectarraylist = new ObjectArrayList<>(set.stream().toList());
      RandomSource randomsource1 = RandomSource.create(worldgenlevel.getSeed()).forkPositional().at(piecescontainer.calculateBoundingBox().getCenter());
      Util.shuffle(objectarraylist, randomsource1);
      int i = Math.min(set.size(), randomsource1.nextInt(5, 8));

      for(BlockPos blockpos : objectarraylist) {
         if (i > 0) {
            --i;
            placeSuspiciousSand(boundingbox, worldgenlevel, blockpos);
         } else if (boundingbox.isInside(blockpos)) {
            worldgenlevel.setBlock(blockpos, Blocks.SAND.defaultBlockState(), 2);
         }
      }

   }

   private static void placeSuspiciousSand(BoundingBox boundingbox, WorldGenLevel worldgenlevel, BlockPos blockpos) {
      if (boundingbox.isInside(blockpos)) {
         worldgenlevel.setBlock(blockpos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
         worldgenlevel.getBlockEntity(blockpos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent((brushableblockentity) -> brushableblockentity.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, blockpos.asLong()));
      }

   }

   public StructureType<?> type() {
      return StructureType.DESERT_PYRAMID;
   }
}
