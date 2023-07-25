package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class SwampHutStructure extends Structure {
   public static final Codec<SwampHutStructure> CODEC = simpleCodec(SwampHutStructure::new);

   public SwampHutStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return onTopOfChunkCenter(structure_generationcontext, Heightmap.Types.WORLD_SURFACE_WG, (structurepiecesbuilder) -> generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private static void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      structurepiecesbuilder.addPiece(new SwampHutPiece(structure_generationcontext.random(), structure_generationcontext.chunkPos().getMinBlockX(), structure_generationcontext.chunkPos().getMinBlockZ()));
   }

   public StructureType<?> type() {
      return StructureType.SWAMP_HUT;
   }
}
