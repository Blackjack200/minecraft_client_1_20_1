package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ListPoolElement extends StructurePoolElement {
   public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter((listpoolelement) -> listpoolelement.elements), projectionCodec()).apply(recordcodecbuilder_instance, ListPoolElement::new));
   private final List<StructurePoolElement> elements;

   public ListPoolElement(List<StructurePoolElement> list, StructureTemplatePool.Projection structuretemplatepool_projection) {
      super(structuretemplatepool_projection);
      if (list.isEmpty()) {
         throw new IllegalArgumentException("Elements are empty");
      } else {
         this.elements = list;
         this.setProjectionOnEachElement(structuretemplatepool_projection);
      }
   }

   public Vec3i getSize(StructureTemplateManager structuretemplatemanager, Rotation rotation) {
      int i = 0;
      int j = 0;
      int k = 0;

      for(StructurePoolElement structurepoolelement : this.elements) {
         Vec3i vec3i = structurepoolelement.getSize(structuretemplatemanager, rotation);
         i = Math.max(i, vec3i.getX());
         j = Math.max(j, vec3i.getY());
         k = Math.max(k, vec3i.getZ());
      }

      return new Vec3i(i, j, k);
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation, RandomSource randomsource) {
      return this.elements.get(0).getShuffledJigsawBlocks(structuretemplatemanager, blockpos, rotation, randomsource);
   }

   public BoundingBox getBoundingBox(StructureTemplateManager structuretemplatemanager, BlockPos blockpos, Rotation rotation) {
      Stream<BoundingBox> stream = this.elements.stream().filter((structurepoolelement1) -> structurepoolelement1 != EmptyPoolElement.INSTANCE).map((structurepoolelement) -> structurepoolelement.getBoundingBox(structuretemplatemanager, blockpos, rotation));
      return BoundingBox.encapsulatingBoxes(stream::iterator).orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox for ListPoolElement"));
   }

   public boolean place(StructureTemplateManager structuretemplatemanager, WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, BlockPos blockpos, BlockPos blockpos1, Rotation rotation, BoundingBox boundingbox, RandomSource randomsource, boolean flag) {
      for(StructurePoolElement structurepoolelement : this.elements) {
         if (!structurepoolelement.place(structuretemplatemanager, worldgenlevel, structuremanager, chunkgenerator, blockpos, blockpos1, rotation, boundingbox, randomsource, flag)) {
            return false;
         }
      }

      return true;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.LIST;
   }

   public StructurePoolElement setProjection(StructureTemplatePool.Projection structuretemplatepool_projection) {
      super.setProjection(structuretemplatepool_projection);
      this.setProjectionOnEachElement(structuretemplatepool_projection);
      return this;
   }

   public String toString() {
      return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
   }

   private void setProjectionOnEachElement(StructureTemplatePool.Projection structuretemplatepool_projection) {
      this.elements.forEach((structurepoolelement) -> structurepoolelement.setProjection(structuretemplatepool_projection));
   }
}
