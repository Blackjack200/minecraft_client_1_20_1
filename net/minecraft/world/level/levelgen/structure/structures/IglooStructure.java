package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooStructure extends Structure {
   public static final Codec<IglooStructure> CODEC = simpleCodec(IglooStructure::new);

   public IglooStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return onTopOfChunkCenter(structure_generationcontext, Heightmap.Types.WORLD_SURFACE_WG, (structurepiecesbuilder) -> this.generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      WorldgenRandom worldgenrandom = structure_generationcontext.random();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 90, chunkpos.getMinBlockZ());
      Rotation rotation = Rotation.getRandom(worldgenrandom);
      IglooPieces.addPieces(structure_generationcontext.structureTemplateManager(), blockpos, rotation, structurepiecesbuilder, worldgenrandom);
   }

   public StructureType<?> type() {
      return StructureType.IGLOO;
   }
}
