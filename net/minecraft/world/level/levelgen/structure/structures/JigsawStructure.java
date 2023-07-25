package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public final class JigsawStructure extends Structure {
   public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
   public static final Codec<JigsawStructure> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance), StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((jigsawstructure6) -> jigsawstructure6.startPool), ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((jigsawstructure5) -> jigsawstructure5.startJigsawName), Codec.intRange(0, 7).fieldOf("size").forGetter((jigsawstructure4) -> jigsawstructure4.maxDepth), HeightProvider.CODEC.fieldOf("start_height").forGetter((jigsawstructure3) -> jigsawstructure3.startHeight), Codec.BOOL.fieldOf("use_expansion_hack").forGetter((jigsawstructure2) -> jigsawstructure2.useExpansionHack), Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter((jigsawstructure1) -> jigsawstructure1.projectStartToHeightmap), Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter((jigsawstructure) -> jigsawstructure.maxDistanceFromCenter)).apply(recordcodecbuilder_instance, JigsawStructure::new)), JigsawStructure::verifyRange).codec();
   private final Holder<StructureTemplatePool> startPool;
   private final Optional<ResourceLocation> startJigsawName;
   private final int maxDepth;
   private final HeightProvider startHeight;
   private final boolean useExpansionHack;
   private final Optional<Heightmap.Types> projectStartToHeightmap;
   private final int maxDistanceFromCenter;

   private static DataResult<JigsawStructure> verifyRange(JigsawStructure jigsawstructure) {
      byte var10000;
      switch (jigsawstructure.terrainAdaptation()) {
         case NONE:
            var10000 = 0;
            break;
         case BURY:
         case BEARD_THIN:
         case BEARD_BOX:
            var10000 = 12;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      int i = var10000;
      return jigsawstructure.maxDistanceFromCenter + i > 128 ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128") : DataResult.success(jigsawstructure);
   }

   public JigsawStructure(Structure.StructureSettings structure_structuresettings, Holder<StructureTemplatePool> holder, Optional<ResourceLocation> optional, int i, HeightProvider heightprovider, boolean flag, Optional<Heightmap.Types> optional1, int j) {
      super(structure_structuresettings);
      this.startPool = holder;
      this.startJigsawName = optional;
      this.maxDepth = i;
      this.startHeight = heightprovider;
      this.useExpansionHack = flag;
      this.projectStartToHeightmap = optional1;
      this.maxDistanceFromCenter = j;
   }

   public JigsawStructure(Structure.StructureSettings structure_structuresettings, Holder<StructureTemplatePool> holder, int i, HeightProvider heightprovider, boolean flag, Heightmap.Types heightmap_types) {
      this(structure_structuresettings, holder, Optional.empty(), i, heightprovider, flag, Optional.of(heightmap_types), 80);
   }

   public JigsawStructure(Structure.StructureSettings structure_structuresettings, Holder<StructureTemplatePool> holder, int i, HeightProvider heightprovider, boolean flag) {
      this(structure_structuresettings, holder, Optional.empty(), i, heightprovider, flag, Optional.empty(), 80);
   }

   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      int i = this.startHeight.sample(structure_generationcontext.random(), new WorldGenerationContext(structure_generationcontext.chunkGenerator(), structure_generationcontext.heightAccessor()));
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
      return JigsawPlacement.addPieces(structure_generationcontext, this.startPool, this.startJigsawName, this.maxDepth, blockpos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter);
   }

   public StructureType<?> type() {
      return StructureType.JIGSAW;
   }
}
