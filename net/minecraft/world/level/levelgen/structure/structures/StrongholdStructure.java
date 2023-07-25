package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class StrongholdStructure extends Structure {
   public static final Codec<StrongholdStructure> CODEC = simpleCodec(StrongholdStructure::new);

   public StrongholdStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return Optional.of(new Structure.GenerationStub(structure_generationcontext.chunkPos().getWorldPosition(), (structurepiecesbuilder) -> generatePieces(structurepiecesbuilder, structure_generationcontext)));
   }

   private static void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      int i = 0;

      StrongholdPieces.StartPiece strongholdpieces_startpiece;
      do {
         structurepiecesbuilder.clear();
         structure_generationcontext.random().setLargeFeatureSeed(structure_generationcontext.seed() + (long)(i++), structure_generationcontext.chunkPos().x, structure_generationcontext.chunkPos().z);
         StrongholdPieces.resetPieces();
         strongholdpieces_startpiece = new StrongholdPieces.StartPiece(structure_generationcontext.random(), structure_generationcontext.chunkPos().getBlockX(2), structure_generationcontext.chunkPos().getBlockZ(2));
         structurepiecesbuilder.addPiece(strongholdpieces_startpiece);
         strongholdpieces_startpiece.addChildren(strongholdpieces_startpiece, structurepiecesbuilder, structure_generationcontext.random());
         List<StructurePiece> list = strongholdpieces_startpiece.pendingChildren;

         while(!list.isEmpty()) {
            int j = structure_generationcontext.random().nextInt(list.size());
            StructurePiece structurepiece = list.remove(j);
            structurepiece.addChildren(strongholdpieces_startpiece, structurepiecesbuilder, structure_generationcontext.random());
         }

         structurepiecesbuilder.moveBelowSeaLevel(structure_generationcontext.chunkGenerator().getSeaLevel(), structure_generationcontext.chunkGenerator().getMinY(), structure_generationcontext.random(), 10);
      } while(structurepiecesbuilder.isEmpty() || strongholdpieces_startpiece.portalRoomPiece == null);

   }

   public StructureType<?> type() {
      return StructureType.STRONGHOLD;
   }
}
