package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckStructure extends Structure {
   public static final Codec<ShipwreckStructure> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance), Codec.BOOL.fieldOf("is_beached").forGetter((shipwreckstructure) -> shipwreckstructure.isBeached)).apply(recordcodecbuilder_instance, ShipwreckStructure::new));
   public final boolean isBeached;

   public ShipwreckStructure(Structure.StructureSettings structure_structuresettings, boolean flag) {
      super(structure_structuresettings);
      this.isBeached = flag;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      Heightmap.Types heightmap_types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
      return onTopOfChunkCenter(structure_generationcontext, heightmap_types, (structurepiecesbuilder) -> this.generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      Rotation rotation = Rotation.getRandom(structure_generationcontext.random());
      BlockPos blockpos = new BlockPos(structure_generationcontext.chunkPos().getMinBlockX(), 90, structure_generationcontext.chunkPos().getMinBlockZ());
      ShipwreckPieces.addPieces(structure_generationcontext.structureTemplateManager(), blockpos, rotation, structurepiecesbuilder, structure_generationcontext.random(), this.isBeached);
   }

   public StructureType<?> type() {
      return StructureType.SHIPWRECK;
   }
}
