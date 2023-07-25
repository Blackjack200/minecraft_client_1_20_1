package net.minecraft.world.level.levelgen.structure;

import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public abstract class SinglePieceStructure extends Structure {
   private final SinglePieceStructure.PieceConstructor constructor;
   private final int width;
   private final int depth;

   protected SinglePieceStructure(SinglePieceStructure.PieceConstructor singlepiecestructure_piececonstructor, int i, int j, Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
      this.constructor = singlepiecestructure_piececonstructor;
      this.width = i;
      this.depth = j;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return getLowestY(structure_generationcontext, this.width, this.depth) < structure_generationcontext.chunkGenerator().getSeaLevel() ? Optional.empty() : onTopOfChunkCenter(structure_generationcontext, Heightmap.Types.WORLD_SURFACE_WG, (structurepiecesbuilder) -> this.generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      structurepiecesbuilder.addPiece(this.constructor.construct(structure_generationcontext.random(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ()));
   }

   @FunctionalInterface
   protected interface PieceConstructor {
      StructurePiece construct(WorldgenRandom worldgenrandom, int i, int j);
   }
}
