package net.minecraft.world.level.levelgen.structure.structures;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherFossilPieces {
   private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{new ResourceLocation("nether_fossils/fossil_1"), new ResourceLocation("nether_fossils/fossil_2"), new ResourceLocation("nether_fossils/fossil_3"), new ResourceLocation("nether_fossils/fossil_4"), new ResourceLocation("nether_fossils/fossil_5"), new ResourceLocation("nether_fossils/fossil_6"), new ResourceLocation("nether_fossils/fossil_7"), new ResourceLocation("nether_fossils/fossil_8"), new ResourceLocation("nether_fossils/fossil_9"), new ResourceLocation("nether_fossils/fossil_10"), new ResourceLocation("nether_fossils/fossil_11"), new ResourceLocation("nether_fossils/fossil_12"), new ResourceLocation("nether_fossils/fossil_13"), new ResourceLocation("nether_fossils/fossil_14")};

   public static void addPieces(StructureTemplateManager structuretemplatemanager, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource, BlockPos blockpos) {
      Rotation rotation = Rotation.getRandom(randomsource);
      structurepieceaccessor.addPiece(new NetherFossilPieces.NetherFossilPiece(structuretemplatemanager, Util.getRandom(FOSSILS, randomsource), blockpos, rotation));
   }

   public static class NetherFossilPiece extends TemplateStructurePiece {
      public NetherFossilPiece(StructureTemplateManager structuretemplatemanager, ResourceLocation resourcelocation, BlockPos blockpos, Rotation rotation) {
         super(StructurePieceType.NETHER_FOSSIL, 0, structuretemplatemanager, resourcelocation, resourcelocation.toString(), makeSettings(rotation), blockpos);
      }

      public NetherFossilPiece(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag) {
         super(StructurePieceType.NETHER_FOSSIL, compoundtag, structuretemplatemanager, (resourcelocation) -> makeSettings(Rotation.valueOf(compoundtag.getString("Rot"))));
      }

      private static StructurePlaceSettings makeSettings(Rotation rotation) {
         return (new StructurePlaceSettings()).setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putString("Rot", this.placeSettings.getRotation().name());
      }

      protected void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox) {
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         boundingbox.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
         super.postProcess(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, blockpos);
      }
   }
}
