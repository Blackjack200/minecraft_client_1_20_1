package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece extends StructurePiece {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final String templateName;
   protected StructureTemplate template;
   protected StructurePlaceSettings placeSettings;
   protected BlockPos templatePosition;

   public TemplateStructurePiece(StructurePieceType structurepiecetype, int i, StructureTemplateManager structuretemplatemanager, ResourceLocation resourcelocation, String s, StructurePlaceSettings structureplacesettings, BlockPos blockpos) {
      super(structurepiecetype, i, structuretemplatemanager.getOrCreate(resourcelocation).getBoundingBox(structureplacesettings, blockpos));
      this.setOrientation(Direction.NORTH);
      this.templateName = s;
      this.templatePosition = blockpos;
      this.template = structuretemplatemanager.getOrCreate(resourcelocation);
      this.placeSettings = structureplacesettings;
   }

   public TemplateStructurePiece(StructurePieceType structurepiecetype, CompoundTag compoundtag, StructureTemplateManager structuretemplatemanager, Function<ResourceLocation, StructurePlaceSettings> function) {
      super(structurepiecetype, compoundtag);
      this.setOrientation(Direction.NORTH);
      this.templateName = compoundtag.getString("Template");
      this.templatePosition = new BlockPos(compoundtag.getInt("TPX"), compoundtag.getInt("TPY"), compoundtag.getInt("TPZ"));
      ResourceLocation resourcelocation = this.makeTemplateLocation();
      this.template = structuretemplatemanager.getOrCreate(resourcelocation);
      this.placeSettings = function.apply(resourcelocation);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
   }

   protected ResourceLocation makeTemplateLocation() {
      return new ResourceLocation(this.templateName);
   }

   protected void addAdditionalSaveData(StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag compoundtag) {
      compoundtag.putInt("TPX", this.templatePosition.getX());
      compoundtag.putInt("TPY", this.templatePosition.getY());
      compoundtag.putInt("TPZ", this.templatePosition.getZ());
      compoundtag.putString("Template", this.templateName);
   }

   public void postProcess(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, BlockPos blockpos) {
      this.placeSettings.setBoundingBox(boundingbox);
      this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
      if (this.template.placeInWorld(worldgenlevel, this.templatePosition, blockpos, this.placeSettings, randomsource, 2)) {
         for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK)) {
            if (structuretemplate_structureblockinfo.nbt() != null) {
               StructureMode structuremode = StructureMode.valueOf(structuretemplate_structureblockinfo.nbt().getString("mode"));
               if (structuremode == StructureMode.DATA) {
                  this.handleDataMarker(structuretemplate_structureblockinfo.nbt().getString("metadata"), structuretemplate_structureblockinfo.pos(), worldgenlevel, randomsource, boundingbox);
               }
            }
         }

         for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1 : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW)) {
            if (structuretemplate_structureblockinfo1.nbt() != null) {
               String s = structuretemplate_structureblockinfo1.nbt().getString("final_state");
               BlockState blockstate = Blocks.AIR.defaultBlockState();

               try {
                  blockstate = BlockStateParser.parseForBlock(worldgenlevel.holderLookup(Registries.BLOCK), s, true).blockState();
               } catch (CommandSyntaxException var15) {
                  LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", s, structuretemplate_structureblockinfo1.pos());
               }

               worldgenlevel.setBlock(structuretemplate_structureblockinfo1.pos(), blockstate, 3);
            }
         }
      }

   }

   protected abstract void handleDataMarker(String s, BlockPos blockpos, ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, BoundingBox boundingbox);

   /** @deprecated */
   @Deprecated
   public void move(int i, int j, int k) {
      super.move(i, j, k);
      this.templatePosition = this.templatePosition.offset(i, j, k);
   }

   public Rotation getRotation() {
      return this.placeSettings.getRotation();
   }

   public StructureTemplate template() {
      return this.template;
   }

   public BlockPos templatePosition() {
      return this.templatePosition;
   }

   public StructurePlaceSettings placeSettings() {
      return this.placeSettings;
   }
}
