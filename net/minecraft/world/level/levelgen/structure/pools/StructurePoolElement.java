package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class StructurePoolElement {
   public static final Codec<StructurePoolElement> CODEC = BuiltInRegistries.STRUCTURE_POOL_ELEMENT.byNameCodec().dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
   private static final Holder<StructureProcessorList> EMPTY = Holder.direct(new StructureProcessorList(List.of()));
   @Nullable
   private volatile StructureTemplatePool.Projection projection;

   protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
      return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
   }

   protected StructurePoolElement(StructureTemplatePool.Projection structuretemplatepool_projection) {
      this.projection = structuretemplatepool_projection;
   }

   public abstract Vec3i getSize(StructureTemplateManager structuretemplatemanager, Rotation rotation);

   public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, RandomSource randomsource);

   public abstract BoundingBox getBoundingBox(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation);

   public abstract boolean place(StructureTemplateManager structuretemplatemanager, WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockPos blockpos1, Rotation rotation, BoundingBox boundingbox, RandomSource randomsource, boolean flag);

   public abstract StructurePoolElementType<?> getType();

   public void handleDataMarker(LevelAccessor levelaccessor, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, BlockPos blockpos, Rotation rotation, RandomSource randomsource, BoundingBox boundingbox) {
   }

   public StructurePoolElement setProjection(StructureTemplatePool.Projection structuretemplatepool_projection) {
      this.projection = structuretemplatepool_projection;
      return this;
   }

   public StructureTemplatePool.Projection getProjection() {
      StructureTemplatePool.Projection structuretemplatepool_projection = this.projection;
      if (structuretemplatepool_projection == null) {
         throw new IllegalStateException();
      } else {
         return structuretemplatepool_projection;
      }
   }

   public int getGroundLevelDelta() {
      return 1;
   }

   public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
      return (structuretemplatepool_projection) -> EmptyPoolElement.INSTANCE;
   }

   public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String s) {
      return (structuretemplatepool_projection) -> new LegacySinglePoolElement(Either.left(new ResourceLocation(s)), EMPTY, structuretemplatepool_projection);
   }

   public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String s, Holder<StructureProcessorList> holder) {
      return (structuretemplatepool_projection) -> new LegacySinglePoolElement(Either.left(new ResourceLocation(s)), holder, structuretemplatepool_projection);
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String s) {
      return (structuretemplatepool_projection) -> new SinglePoolElement(Either.left(new ResourceLocation(s)), EMPTY, structuretemplatepool_projection);
   }

   public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String s, Holder<StructureProcessorList> holder) {
      return (structuretemplatepool_projection) -> new SinglePoolElement(Either.left(new ResourceLocation(s)), holder, structuretemplatepool_projection);
   }

   public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(Holder<PlacedFeature> holder) {
      return (structuretemplatepool_projection) -> new FeaturePoolElement(holder, structuretemplatepool_projection);
   }

   public static Function<StructureTemplatePool.Projection, ListPoolElement> list(List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> list) {
      return (structuretemplatepool_projection) -> new ListPoolElement(list.stream().map((function) -> function.apply(structuretemplatepool_projection)).collect(Collectors.toList()), structuretemplatepool_projection);
   }
}
