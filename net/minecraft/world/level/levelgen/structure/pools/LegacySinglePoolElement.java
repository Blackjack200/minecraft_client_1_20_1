package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LegacySinglePoolElement extends SinglePoolElement {
   public static final Codec<LegacySinglePoolElement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(templateCodec(), processorsCodec(), projectionCodec()).apply(recordcodecbuilder_instance, LegacySinglePoolElement::new));

   protected LegacySinglePoolElement(Either<ResourceLocation, StructureTemplate> either, Holder<StructureProcessorList> holder, StructureTemplatePool.Projection structuretemplatepool_projection) {
      super(either, holder, structuretemplatepool_projection);
   }

   protected StructurePlaceSettings getSettings(Rotation rotation, BoundingBox boundingbox, boolean flag) {
      StructurePlaceSettings structureplacesettings = super.getSettings(rotation, boundingbox, flag);
      structureplacesettings.popProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
      return structureplacesettings;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.LEGACY;
   }

   public String toString() {
      return "LegacySingle[" + this.template + "]";
   }
}
