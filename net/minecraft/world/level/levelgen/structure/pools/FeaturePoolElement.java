package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class FeaturePoolElement extends StructurePoolElement {
   public static final Codec<FeaturePoolElement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(PlacedFeature.CODEC.fieldOf("feature").forGetter((featurepoolelement) -> featurepoolelement.feature), projectionCodec()).apply(recordcodecbuilder_instance, FeaturePoolElement::new));
   private final Holder<PlacedFeature> feature;
   private final CompoundTag defaultJigsawNBT;

   protected FeaturePoolElement(Holder<PlacedFeature> holder, StructureTemplatePool.Projection structuretemplatepool_projection) {
      super(structuretemplatepool_projection);
      this.feature = holder;
      this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
   }

   private CompoundTag fillDefaultJigsawNBT() {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("name", "minecraft:bottom");
      compoundtag.putString("final_state", "minecraft:air");
      compoundtag.putString("pool", "minecraft:empty");
      compoundtag.putString("target", "minecraft:empty");
      compoundtag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
      return compoundtag;
   }

   public Vec3i getSize(StructureTemplateManager structuretemplatemanager, Rotation rotation) {
      return Vec3i.ZERO;
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, RandomSource randomsource) {
      List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
      list.add(new StructureTemplate.StructureBlockInfo(blockpos, Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)), this.defaultJigsawNBT));
      return list;
   }

   public BoundingBox getBoundingBox(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation) {
      Vec3i vec3i = this.getSize(structuretemplatemanager, rotation);
      return new BoundingBox(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() + vec3i.getX(), blockpos.getY() + vec3i.getY(), blockpos.getZ() + vec3i.getZ());
   }

   public boolean place(StructureTemplateManager structuretemplatemanager, WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockPos blockpos1, Rotation rotation, BoundingBox boundingbox, RandomSource randomsource, boolean flag) {
      return this.feature.value().place(worldgenlevel, chunkgenerator, randomsource, blockpos);
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.FEATURE;
   }

   public String toString() {
      return "Feature[" + this.feature + "]";
   }
}
