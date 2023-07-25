package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class IglooPieces {
   public static final int GENERATION_HEIGHT = 90;
   static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
   private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
   private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
   static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7));
   static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2));

   public static void addPieces(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, StructurePieceAccessor structurepieceaccessor, RandomSource randomsource) {
      if (randomsource.nextDouble() < 0.5D) {
         int i = randomsource.nextInt(8) + 4;
         structurepieceaccessor.addPiece(new IglooPieces.IglooPiece(structuretemplatemanager, STRUCTURE_LOCATION_LABORATORY, blockpos, rotation, i * 3));

         for(int j = 0; j < i - 1; ++j) {
            structurepieceaccessor.addPiece(new IglooPieces.IglooPiece(structuretemplatemanager, STRUCTURE_LOCATION_LADDER, blockpos, rotation, j * 3));
         }
      }

      structurepieceaccessor.addPiece(new IglooPieces.IglooPiece(structuretemplatemanager, STRUCTURE_LOCATION_IGLOO, blockpos, rotation, 0));
   }

   public static class IglooPiece extends TemplateStructurePiece {
      public IglooPiece(StructureTemplateManager structuretemplatemanager, ResourceLocation resourcelocation, BlockPos blockpos, Rotation rotation, int i) {
         super(StructurePieceType.IGLOO, 0, structuretemplatemanager, resourcelocation, resourcelocation.toString(), makeSettings(rotation, resourcelocation), makePosition(resourcelocation, blockpos, i));
      }

      public IglooPiece(StructureTemplateManager structuretemplatemanager, CompoundTag compoundtag) {
         super(StructurePieceType.IGLOO, compoundtag, structuretemplatemanager, (resourcelocation) -> makeSettings(Rotation.valueOf(compoundtag.getString("Rot")), resourcelocation));
      }

      private static StructurePlaceSettings makeSettings(Rotation rotation, ResourceLocation resourcelocation) {
         return (new StructurePlaceSettings()).setRotation(rotation).setMirror(Mirror.NONE).setRotationPivot(IglooPieces.PIVOTS.get(resourcelocation)).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      }

      private static BlockPos makePosition(ResourceLocation resourcelocation, BlockPos blockpos, int i) {
         return blockpos.offset(IglooPieces.OFFSETS.get(resourcelocation)).below(i);
      }

      protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
         super.addAdditionalSaveData(structurepieceserializationcontext, compoundtag);
         compoundtag.putString("Rot", this.placeSettings.getRotation().name());
      }

      protected void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox) {
         if ("chest".equals(s)) {
            serverlevelaccessor.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
            BlockEntity blockentity = serverlevelaccessor.getBlockEntity(blockpos.below());
            if (blockentity instanceof ChestBlockEntity) {
               ((ChestBlockEntity)blockentity).setLootTable(BuiltInLootTables.IGLOO_CHEST, randomsource.nextLong());
            }

         }
      }

      public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
         ResourceLocation resourcelocation = new ResourceLocation(this.templateName);
         StructurePlaceSettings structureplacesettings = makeSettings(this.placeSettings.getRotation(), resourcelocation);
         BlockPos blockpos1 = IglooPieces.OFFSETS.get(resourcelocation);
         BlockPos blockpos2 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structureplacesettings, new BlockPos(3 - blockpos1.getX(), 0, -blockpos1.getZ())));
         int i = worldgenlevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos2.getX(), blockpos2.getZ());
         BlockPos blockpos3 = this.templatePosition;
         this.templatePosition = this.templatePosition.offset(0, i - 90 - 1, 0);
         super.postProcess(worldgenlevel, structuremanager, chunkgenerator, randomsource, boundingbox, chunkpos, blockpos);
         if (resourcelocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
            BlockPos blockpos4 = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structureplacesettings, new BlockPos(3, 0, 5)));
            BlockState blockstate = worldgenlevel.getBlockState(blockpos4.below());
            if (!blockstate.isAir() && !blockstate.is(Blocks.LADDER)) {
               worldgenlevel.setBlock(blockpos4, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
            }
         }

         this.templatePosition = blockpos3;
      }
   }
}
