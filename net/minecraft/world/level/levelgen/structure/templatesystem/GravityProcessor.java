package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;

public class GravityProcessor extends StructureProcessor {
   public static final Codec<GravityProcessor> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Heightmap.Types.CODEC.fieldOf("heightmap").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter((gravityprocessor1) -> gravityprocessor1.heightmap), Codec.INT.fieldOf("offset").orElse(0).forGetter((gravityprocessor) -> gravityprocessor.offset)).apply(recordcodecbuilder_instance, GravityProcessor::new));
   private final Heightmap.Types heightmap;
   private final int offset;

   public GravityProcessor(Heightmap.Types heightmap_types, int i) {
      this.heightmap = heightmap_types;
      this.offset = i;
   }

   @Nullable
   public StructureTemplate.StructureBlockInfo processBlock(LevelReader levelreader, BlockPos blockpos, BlockPos blockpos1, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo, StructureTemplate.StructureBlockInfo structuretemplate_structureblockinfo1, StructurePlaceSettings structureplacesettings) {
      Heightmap.Types heightmap_types;
      if (levelreader instanceof ServerLevel) {
         if (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG) {
            heightmap_types = Heightmap.Types.WORLD_SURFACE;
         } else if (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
            heightmap_types = Heightmap.Types.OCEAN_FLOOR;
         } else {
            heightmap_types = this.heightmap;
         }
      } else {
         heightmap_types = this.heightmap;
      }

      BlockPos blockpos2 = structuretemplate_structureblockinfo1.pos();
      int i = levelreader.getHeight(heightmap_types, blockpos2.getX(), blockpos2.getZ()) + this.offset;
      int j = structuretemplate_structureblockinfo.pos().getY();
      return new StructureTemplate.StructureBlockInfo(new BlockPos(blockpos2.getX(), i + j, blockpos2.getZ()), structuretemplate_structureblockinfo1.state(), structuretemplate_structureblockinfo1.nbt());
   }

   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.GRAVITY;
   }
}
