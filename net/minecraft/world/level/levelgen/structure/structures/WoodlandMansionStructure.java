package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class WoodlandMansionStructure extends Structure {
   public static final Codec<WoodlandMansionStructure> CODEC = simpleCodec(WoodlandMansionStructure::new);

   public WoodlandMansionStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      Rotation rotation = Rotation.getRandom(structure_generationcontext.random());
      BlockPos blockpos = this.getLowestYIn5by5BoxOffset7Blocks(structure_generationcontext, rotation);
      return blockpos.getY() < 60 ? Optional.empty() : Optional.of(new Structure.GenerationStub(blockpos, (structurepiecesbuilder) -> this.generatePieces(structurepiecesbuilder, structure_generationcontext, blockpos, rotation)));
   }

   private void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext, BlockPos blockpos, Rotation rotation) {
      List<WoodlandMansionPieces.WoodlandMansionPiece> list = Lists.newLinkedList();
      WoodlandMansionPieces.generateMansion(structure_generationcontext.structureTemplateManager(), blockpos, rotation, list, structure_generationcontext.random());
      list.forEach(structurepiecesbuilder::addPiece);
   }

   public void afterPlace(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, PiecesContainer piecescontainer) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int i = worldgenlevel.getMinBuildHeight();
      BoundingBox boundingbox1 = piecescontainer.calculateBoundingBox();
      int j = boundingbox1.minY();

      for(int k = boundingbox.minX(); k <= boundingbox.maxX(); ++k) {
         for(int l = boundingbox.minZ(); l <= boundingbox.maxZ(); ++l) {
            blockpos_mutableblockpos.set(k, j, l);
            if (!worldgenlevel.isEmptyBlock(blockpos_mutableblockpos) && boundingbox1.isInside(blockpos_mutableblockpos) && piecescontainer.isInsidePiece(blockpos_mutableblockpos)) {
               for(int i1 = j - 1; i1 > i; --i1) {
                  blockpos_mutableblockpos.setY(i1);
                  if (!worldgenlevel.isEmptyBlock(blockpos_mutableblockpos) && !worldgenlevel.getBlockState(blockpos_mutableblockpos).liquid()) {
                     break;
                  }

                  worldgenlevel.setBlock(blockpos_mutableblockpos, Blocks.COBBLESTONE.defaultBlockState(), 2);
               }
            }
         }
      }

   }

   public StructureType<?> type() {
      return StructureType.WOODLAND_MANSION;
   }
}
