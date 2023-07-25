package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelSource extends ChunkGenerator {
   public static final Codec<FlatLevelSource> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings)).apply(recordcodecbuilder_instance, recordcodecbuilder_instance.stable(FlatLevelSource::new)));
   private final FlatLevelGeneratorSettings settings;

   public FlatLevelSource(FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      super(new FixedBiomeSource(flatlevelgeneratorsettings.getBiome()), Util.memoize(flatlevelgeneratorsettings::adjustGenerationSettings));
      this.settings = flatlevelgeneratorsettings;
   }

   public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderlookup, RandomState randomstate, long i) {
      Stream<Holder<StructureSet>> stream = this.settings.structureOverrides().map(HolderSet::stream).orElseGet(() -> holderlookup.listElements().map((holder_reference) -> holder_reference));
      return ChunkGeneratorStructureState.createForFlat(randomstate, i, this.biomeSource, stream);
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public FlatLevelGeneratorSettings settings() {
      return this.settings;
   }

   public void buildSurface(WorldGenRegion worldgenregion, StructureManager structuremanager, RandomState randomstate, ChunkAccess chunkaccess) {
   }

   public int getSpawnHeight(LevelHeightAccessor levelheightaccessor) {
      return levelheightaccessor.getMinBuildHeight() + Math.min(levelheightaccessor.getHeight(), this.settings.getLayers().size());
   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomstate, StructureManager structuremanager, ChunkAccess chunkaccess) {
      List<BlockState> list = this.settings.getLayers();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      Heightmap heightmap = chunkaccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap heightmap1 = chunkaccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

      for(int i = 0; i < Math.min(chunkaccess.getHeight(), list.size()); ++i) {
         BlockState blockstate = list.get(i);
         if (blockstate != null) {
            int j = chunkaccess.getMinBuildHeight() + i;

            for(int k = 0; k < 16; ++k) {
               for(int l = 0; l < 16; ++l) {
                  chunkaccess.setBlockState(blockpos_mutableblockpos.set(k, j, l), blockstate, false);
                  heightmap.update(k, j, l, blockstate);
                  heightmap1.update(k, j, l, blockstate);
               }
            }
         }
      }

      return CompletableFuture.completedFuture(chunkaccess);
   }

   public int getBaseHeight(int i, int j, Heightmap.Types heightmap_types, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      List<BlockState> list = this.settings.getLayers();

      for(int k = Math.min(list.size(), levelheightaccessor.getMaxBuildHeight()) - 1; k >= 0; --k) {
         BlockState blockstate = list.get(k);
         if (blockstate != null && heightmap_types.isOpaque().test(blockstate)) {
            return levelheightaccessor.getMinBuildHeight() + k + 1;
         }
      }

      return levelheightaccessor.getMinBuildHeight();
   }

   public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      return new NoiseColumn(levelheightaccessor.getMinBuildHeight(), this.settings.getLayers().stream().limit((long)levelheightaccessor.getHeight()).map((blockstate) -> blockstate == null ? Blocks.AIR.defaultBlockState() : blockstate).toArray((k) -> new BlockState[k]));
   }

   public void addDebugScreenInfo(List<String> list, RandomState randomstate, BlockPos blockpos) {
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
      return -63;
   }
}
