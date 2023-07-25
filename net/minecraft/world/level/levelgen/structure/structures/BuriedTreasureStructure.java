package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class BuriedTreasureStructure extends Structure {
   public static final Codec<BuriedTreasureStructure> CODEC = simpleCodec(BuriedTreasureStructure::new);

   public BuriedTreasureStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return onTopOfChunkCenter(structure_generationcontext, Heightmap.Types.OCEAN_FLOOR_WG, (structurepiecesbuilder) -> generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private static void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      BlockPos blockpos = new BlockPos(structure_generationcontext.chunkPos().getBlockX(9), 90, structure_generationcontext.chunkPos().getBlockZ(9));
      structurepiecesbuilder.addPiece(new BuriedTreasurePieces.BuriedTreasurePiece(blockpos));
   }

   public StructureType<?> type() {
      return StructureType.BURIED_TREASURE;
   }
}
