package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinStructure extends Structure {
   public static final Codec<OceanRuinStructure> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance), OceanRuinStructure.Type.CODEC.fieldOf("biome_temp").forGetter((oceanruinstructure2) -> oceanruinstructure2.biomeTemp), Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter((oceanruinstructure1) -> oceanruinstructure1.largeProbability), Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter((oceanruinstructure) -> oceanruinstructure.clusterProbability)).apply(recordcodecbuilder_instance, OceanRuinStructure::new));
   public final OceanRuinStructure.Type biomeTemp;
   public final float largeProbability;
   public final float clusterProbability;

   public OceanRuinStructure(Structure.StructureSettings structure_structuresettings, OceanRuinStructure.Type oceanruinstructure_type, float f, float f1) {
      super(structure_structuresettings);
      this.biomeTemp = oceanruinstructure_type;
      this.largeProbability = f;
      this.clusterProbability = f1;
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return onTopOfChunkCenter(structure_generationcontext, Heightmap.Types.OCEAN_FLOOR_WG, (structurepiecesbuilder) -> this.generatePieces(structurepiecesbuilder, structure_generationcontext));
   }

   private void generatePieces(StructurePiecesBuilder structurepiecesbuilder, Structure.GenerationContext structure_generationcontext) {
      BlockPos blockpos = new BlockPos(structure_generationcontext.chunkPos().getMinBlockX(), 90, structure_generationcontext.chunkPos().getMinBlockZ());
      Rotation rotation = Rotation.getRandom(structure_generationcontext.random());
      OceanRuinPieces.addPieces(structure_generationcontext.structureTemplateManager(), blockpos, rotation, structurepiecesbuilder, structure_generationcontext.random(), this);
   }

   public StructureType<?> type() {
      return StructureType.OCEAN_RUIN;
   }

   public static enum Type implements StringRepresentable {
      WARM("warm"),
      COLD("cold");

      public static final Codec<OceanRuinStructure.Type> CODEC = StringRepresentable.fromEnum(OceanRuinStructure.Type::values);
      private final String name;

      private Type(String s) {
         this.name = s;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
