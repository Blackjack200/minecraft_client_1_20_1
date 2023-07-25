package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanMonumentStructure extends Structure {
   public static final Codec<OceanMonumentStructure> CODEC = simpleCodec(OceanMonumentStructure::new);

   public OceanMonumentStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      int i = structure_generationcontext.chunkPos().getBlockX(9);
      int j = structure_generationcontext.chunkPos().getBlockZ(9);

      for(Holder<Biome> holder : structure_generationcontext.biomeSource().getBiomesWithin(i, structure_generationcontext.chunkGenerator().getSeaLevel(), j, 29, structure_generationcontext.randomState().sampler())) {
         if (!holder.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) {
            return Optional.empty();
         }
      }

      return onTopOfChunkCenter(structure_generationcontext, Heightmap.Types.OCEAN_FLOOR_WG, (structurepiecesbuilder) -> generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private static StructurePiece createTopPiece(ChunkPos chunkpos, WorldgenRandom worldgenrandom) {
      int i = chunkpos.getMinBlockX() - 29;
      int j = chunkpos.getMinBlockZ() - 29;
      Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenrandom);
      return new OceanMonumentPieces.MonumentBuilding(worldgenrandom, i, j, direction);
   }

   private static void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      structurepiecesbuilder.addPiece(createTopPiece(structure_generationcontext.chunkPos(), structure_generationcontext.random()));
   }

   public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos chunkpos, long i, PiecesContainer piecescontainer) {
      if (piecescontainer.isEmpty()) {
         return piecescontainer;
      } else {
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         worldgenrandom.setLargeFeatureSeed(i, chunkpos.x, chunkpos.z);
         StructurePiece structurepiece = piecescontainer.pieces().get(0);
         BoundingBox boundingbox = structurepiece.getBoundingBox();
         int j = boundingbox.minX();
         int k = boundingbox.minZ();
         Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenrandom);
         Direction direction1 = Objects.requireNonNullElse(structurepiece.getOrientation(), direction);
         StructurePiece structurepiece1 = new OceanMonumentPieces.MonumentBuilding(worldgenrandom, j, k, direction1);
         StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
         structurepiecesbuilder.addPiece(structurepiece1);
         return structurepiecesbuilder.build();
      }
   }

   public StructureType<?> type() {
      return StructureType.OCEAN_MONUMENT;
   }
}
