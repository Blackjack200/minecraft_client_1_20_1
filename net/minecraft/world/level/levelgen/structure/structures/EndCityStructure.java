package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class EndCityStructure extends Structure {
   public static final Codec<EndCityStructure> CODEC = simpleCodec(EndCityStructure::new);

   public EndCityStructure(Structure.StructureSettings structure_structuresettings) {
      super(structure_structuresettings);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      Rotation rotation = Rotation.getRandom(structure_generationcontext.random());
      BlockPos blockpos = this.getLowestYIn5by5BoxOffset7Blocks(structure_generationcontext, rotation);
      return blockpos.getY() < 60 ? Optional.empty() : Optional.of(new Structure.GenerationStub(blockpos, (structurepiecesbuilder) -> this.generatePieces(structurepiecesbuilder, blockpos, rotation, structure_generationcontext)));
   }

   private void generatePieces(StructurePiecesBuilder structurepiecesbuilder, BlockPos blockpos, Rotation rotation, Structure.GenerationContext structure_generationcontext) {
      List<StructurePiece> list = Lists.newArrayList();
      EndCityPieces.startHouseTower(structure_generationcontext.structureTemplateManager(), blockpos, rotation, list, structure_generationcontext.random());
      list.forEach(structurepiecesbuilder::addPiece);
   }

   public StructureType<?> type() {
      return StructureType.END_CITY;
   }
}
