package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;

public class DebugLevelSource extends ChunkGenerator {
   public static final Codec<DebugLevelSource> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryOps.retrieveElement(Biomes.PLAINS)).apply(recordcodecbuilder_instance, recordcodecbuilder_instance.stable(DebugLevelSource::new)));
   private static final int BLOCK_MARGIN = 2;
   private static final List<BlockState> ALL_BLOCKS = StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false).flatMap((block) -> block.getStateDefinition().getPossibleStates().stream()).collect(Collectors.toList());
   private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
   private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
   protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
   public static final int HEIGHT = 70;
   public static final int BARRIER_HEIGHT = 60;

   public DebugLevelSource(Holder.Reference<Biome> holder_reference) {
      super(new FixedBiomeSource(holder_reference));
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public void buildSurface(WorldGenRegion worldgenregion, StructureManager structuremanager, RandomState randomstate, ChunkAccess chunkaccess) {
   }

   public void applyBiomeDecoration(WorldGenLevel worldgenlevel, ChunkAccess chunkaccess, StructureManager structuremanager) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      ChunkPos chunkpos = chunkaccess.getPos();
      int i = chunkpos.x;
      int j = chunkpos.z;

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int i1 = SectionPos.sectionToBlockCoord(i, k);
            int j1 = SectionPos.sectionToBlockCoord(j, l);
            worldgenlevel.setBlock(blockpos_mutableblockpos.set(i1, 60, j1), BARRIER, 2);
            BlockState blockstate = getBlockStateFor(i1, j1);
            worldgenlevel.setBlock(blockpos_mutableblockpos.set(i1, 70, j1), blockstate, 2);
         }
      }

   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomstate, StructureManager structuremanager, ChunkAccess chunkaccess) {
      return CompletableFuture.completedFuture(chunkaccess);
   }

   public int getBaseHeight(int i, int j, Heightmap.Types heightmap_types, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      return 0;
   }

   public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      return new NoiseColumn(0, new BlockState[0]);
   }

   public void addDebugScreenInfo(List<String> list, RandomState randomstate, BlockPos blockpos) {
   }

   public static BlockState getBlockStateFor(int i, int j) {
      BlockState blockstate = AIR;
      if (i > 0 && j > 0 && i % 2 != 0 && j % 2 != 0) {
         i /= 2;
         j /= 2;
         if (i <= GRID_WIDTH && j <= GRID_HEIGHT) {
            int k = Mth.abs(i * GRID_WIDTH + j);
            if (k < ALL_BLOCKS.size()) {
               blockstate = ALL_BLOCKS.get(k);
            }
         }
      }

      return blockstate;
   }

   public void applyCarvers(WorldGenRegion worldgenregion, long i, RandomState randomstate, BiomeManager biomemanager, StructureManager structuremanager, ChunkAccess chunkaccess, GenerationStep.Carving generationstep_carving) {
   }

   public void spawnOriginalMobs(WorldGenRegion worldgenregion) {
   }

   public int getMinY() {
      return 0;
   }

   public int getGenDepth() {
      return 384;
   }

   public int getSeaLevel() {
      return 63;
   }
}
