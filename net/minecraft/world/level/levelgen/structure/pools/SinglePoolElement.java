package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SinglePoolElement extends StructurePoolElement {
   private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left));
   public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(recordcodecbuilder_instance, SinglePoolElement::new));
   protected final Either<ResourceLocation, StructureTemplate> template;
   protected final Holder<StructureProcessorList> processors;

   private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> either, DynamicOps<T> dynamicops, T object) {
      Optional<ResourceLocation> optional = either.left();
      return !optional.isPresent() ? DataResult.error(() -> "Can not serialize a runtime pool element") : ResourceLocation.CODEC.encode(optional.get(), dynamicops, object);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Holder<StructureProcessorList>> processorsCodec() {
      return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter((singlepoolelement) -> singlepoolelement.processors);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
      return TEMPLATE_CODEC.fieldOf("location").forGetter((singlepoolelement) -> singlepoolelement.template);
   }

   protected SinglePoolElement(Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> holder, StructureTemplatePool.Projection structuretemplatepool_projection) {
      super(structuretemplatepool_projection);
      this.template = either;
      this.processors = holder;
   }

   public Vec3i getSize(StructureTemplateManager structuretemplatemanager, Rotation rotation) {
      StructureTemplate structuretemplate = this.getTemplate(structuretemplatemanager);
      return structuretemplate.getSize(rotation);
   }

   private StructureTemplate getTemplate(StructureTemplateManager structuretemplatemanager) {
      return this.template.map(structuretemplatemanager::getOrCreate, Function.identity());
   }

   public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, boolean flag) {
      StructureTemplate structuretemplate = this.getTemplate(structuretemplatemanager);
      List<StructureTemplate.StructureBlockInfo> list = structuretemplate.filterBlocks(blockpos, (new StructurePlaceSettings()).setRotation(rotation), Blocks.STRUCTURE_BLOCK, flag);
      List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();

      for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : list) {
         CompoundTag compoundtag = structuretemplate_structureblockinfo.nbt();
         if (compoundtag != null) {
            StructureMode structuremode = StructureMode.valueOf(compoundtag.getString("mode"));
            if (structuremode == StructureMode.DATA) {
               list1.add(structuretemplate_structureblockinfo);
            }
         }
      }

      return list1;
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, RandomSource randomsource) {
      StructureTemplate structuretemplate = this.getTemplate(structuretemplatemanager);
      ObjectArrayList<StructureTemplate.StructureBlockInfo> objectarraylist = structuretemplate.filterBlocks(blockpos, (new StructurePlaceSettings()).setRotation(rotation), Blocks.JIGSAW, true);
      Util.shuffle(objectarraylist, randomsource);
      return objectarraylist;
   }

   public BoundingBox getBoundingBox(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation) {
      StructureTemplate structuretemplate = this.getTemplate(structuretemplatemanager);
      return structuretemplate.getBoundingBox((new StructurePlaceSettings()).setRotation(rotation), blockpos);
   }

   public boolean place(StructureTemplateManager structuretemplatemanager, WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockPos blockpos1, Rotation rotation, BoundingBox boundingbox, RandomSource randomsource, boolean flag) {
      StructureTemplate structuretemplate = this.getTemplate(structuretemplatemanager);
      StructurePlaceSettings structureplacesettings = this.getSettings(rotation, boundingbox, flag);
      if (!structuretemplate.placeInWorld(worldgenlevel, blockpos, blockpos1, structureplacesettings, randomsource, 18)) {
         return false;
      } else {
         for(StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo : StructureTemplate.processBlockInfos(worldgenlevel, blockpos, blockpos1, structureplacesettings, this.getDataMarkers(structuretemplatemanager, blockpos, rotation, false))) {
            this.handleDataMarker(worldgenlevel, structuretemplate_structureblockinfo, blockpos, rotation, randomsource, boundingbox);
         }

         return true;
      }
   }

   protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingbox, boolean flag) {
      StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
      structureplacesettings.setBoundingBox(boundingbox);
      structureplacesettings.setRotation(rotation);
      structureplacesettings.setKnownShape(true);
      structureplacesettings.setIgnoreEntities(false);
      structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      structureplacesettings.setFinalizeEntities(true);
      if (!flag) {
         structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
      }

      this.processors.value().list().forEach(structureplacesettings::addProcessor);
      this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
      return structureplacesettings;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.SINGLE;
   }

   public String toString() {
      return "Single[" + this.template + "]";
   }
}
