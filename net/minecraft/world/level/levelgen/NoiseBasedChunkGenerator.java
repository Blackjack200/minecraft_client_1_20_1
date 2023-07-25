package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
   public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(BiomeSource.CODEC.fieldOf("biome_source").forGetter((noisebasedchunkgenerator1) -> noisebasedchunkgenerator1.biomeSource), NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter((noisebasedchunkgenerator) -> noisebasedchunkgenerator.settings)).apply(recordcodecbuilder_instance, recordcodecbuilder_instance.stable(NoiseBasedChunkGenerator::new)));
   private static final BlockState AIR = Blocks.AIR.defaultBlockState();
   private final Holder<NoiseGeneratorSettings> settings;
   private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

   public NoiseBasedChunkGenerator(BiomeSource biomesource, Holder<NoiseGeneratorSettings> holder) {
      super(biomesource);
      this.settings = holder;
      this.globalFluidPicker = Suppliers.memoize(() -> createFluidPicker(holder.value()));
   }

   private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings noisegeneratorsettings) {
      Aquifer.FluidStatus aquifer_fluidstatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
      int i = noisegeneratorsettings.seaLevel();
      Aquifer.FluidStatus aquifer_fluidstatus1 = new Aquifer.FluidStatus(i, noisegeneratorsettings.defaultFluid());
      Aquifer.FluidStatus aquifer_fluidstatus2 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
      return (k, l, i1) -> l < Math.min(-54, i) ? aquifer_fluidstatus : aquifer_fluidstatus1;
   }

   public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState randomstate, Blender blender, StructureManager structuremanager, ChunkAccess chunkaccess) {
      return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
         this.doCreateBiomes(blender, randomstate, structuremanager, chunkaccess);
         return chunkaccess;
      }), Util.backgroundExecutor());
   }

   private void doCreateBiomes(Blender blender, RandomState randomstate, StructureManager structuremanager, ChunkAccess chunkaccess) {
      NoiseChunk noisechunk = chunkaccess.getOrCreateNoiseChunk((chunkaccess1) -> this.createNoiseChunk(chunkaccess1, structuremanager, blender, randomstate));
      BiomeResolver biomeresolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunkaccess);
      chunkaccess.fillBiomesFromNoise(biomeresolver, noisechunk.cachedClimateSampler(randomstate.router(), this.settings.value().spawnTarget()));
   }

   private NoiseChunk createNoiseChunk(ChunkAccess chunkaccess, StructureManager structuremanager, Blender blender, RandomState randomstate) {
      return NoiseChunk.forChunk(chunkaccess, randomstate, Beardifier.forStructuresInChunk(structuremanager, chunkaccess.getPos()), this.settings.value(), this.globalFluidPicker.get(), blender);
   }

   protected Codec<? extends ChunkGenerator> codec() {
      return CODEC;
   }

   public Holder<NoiseGeneratorSettings> generatorSettings() {
      return this.settings;
   }

   public boolean stable(ResourceKey<NoiseGeneratorSettings> resourcekey) {
      return this.settings.is(resourcekey);
   }

   public int getBaseHeight(int i, int j, Heightmap.Types heightmap_types, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      return this.iterateNoiseColumn(levelheightaccessor, randomstate, i, j, (MutableObject<NoiseColumn>)null, heightmap_types.isOpaque()).orElse(levelheightaccessor.getMinBuildHeight());
   }

   public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      MutableObject<NoiseColumn> mutableobject = new MutableObject<>();
      this.iterateNoiseColumn(levelheightaccessor, randomstate, i, j, mutableobject, (Predicate<BlockState>)null);
      return mutableobject.getValue();
   }

   public void addDebugScreenInfo(List<String> list, RandomState randomstate, BlockPos blockpos) {
      DecimalFormat decimalformat = new DecimalFormat("0.000");
      NoiseRouter noiserouter = randomstate.router();
      DensityFunction.SinglePointContext densityfunction_singlepointcontext = new DensityFunction.SinglePointContext(blockpos.getX(), blockpos.getY(), blockpos.getZ());
      double d0 = noiserouter.ridges().compute(densityfunction_singlepointcontext);
      list.add("NoiseRouter T: " + decimalformat.format(noiserouter.temperature().compute(densityfunction_singlepointcontext)) + " V: " + decimalformat.format(noiserouter.vegetation().compute(densityfunction_singlepointcontext)) + " C: " + decimalformat.format(noiserouter.continents().compute(densityfunction_singlepointcontext)) + " E: " + decimalformat.format(noiserouter.erosion().compute(densityfunction_singlepointcontext)) + " D: " + decimalformat.format(noiserouter.depth().compute(densityfunction_singlepointcontext)) + " W: " + decimalformat.format(d0) + " PV: " + decimalformat.format((double)NoiseRouterData.peaksAndValleys((float)d0)) + " AS: " + decimalformat.format(noiserouter.initialDensityWithoutJaggedness().compute(densityfunction_singlepointcontext)) + " N: " + decimalformat.format(noiserouter.finalDensity().compute(densityfunction_singlepointcontext)));
   }

   private OptionalInt iterateNoiseColumn(LevelHeightAccessor levelheightaccessor, RandomState randomstate, int i, int j, @Nullable MutableObject<NoiseColumn> mutableobject, @Nullable Predicate<BlockState> predicate) {
      NoiseSettings noisesettings = this.settings.value().noiseSettings().clampToHeightAccessor(levelheightaccessor);
      int k = noisesettings.getCellHeight();
      int l = noisesettings.minY();
      int i1 = Mth.floorDiv(l, k);
      int j1 = Mth.floorDiv(noisesettings.height(), k);
      if (j1 <= 0) {
         return OptionalInt.empty();
      } else {
         BlockState[] ablockstate;
         if (mutableobject == null) {
            ablockstate = null;
         } else {
            ablockstate = new BlockState[noisesettings.height()];
            mutableobject.setValue(new NoiseColumn(l, ablockstate));
         }

         int k1 = noisesettings.getCellWidth();
         int l1 = Math.floorDiv(i, k1);
         int i2 = Math.floorDiv(j, k1);
         int j2 = Math.floorMod(i, k1);
         int k2 = Math.floorMod(j, k1);
         int l2 = l1 * k1;
         int i3 = i2 * k1;
         double d0 = (double)j2 / (double)k1;
         double d1 = (double)k2 / (double)k1;
         NoiseChunk noisechunk = new NoiseChunk(1, randomstate, l2, i3, noisesettings, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker.get(), Blender.empty());
         noisechunk.initializeForFirstCellX();
         noisechunk.advanceCellX(0);

         for(int j3 = j1 - 1; j3 >= 0; --j3) {
            noisechunk.selectCellYZ(j3, 0);

            for(int k3 = k - 1; k3 >= 0; --k3) {
               int l3 = (i1 + j3) * k + k3;
               double d2 = (double)k3 / (double)k;
               noisechunk.updateForY(l3, d2);
               noisechunk.updateForX(i, d0);
               noisechunk.updateForZ(j, d1);
               BlockState blockstate = noisechunk.getInterpolatedState();
               BlockState blockstate1 = blockstate == null ? this.settings.value().defaultBlock() : blockstate;
               if (ablockstate != null) {
                  int i4 = j3 * k + k3;
                  ablockstate[i4] = blockstate1;
               }

               if (predicate != null && predicate.test(blockstate1)) {
                  noisechunk.stopInterpolation();
                  return OptionalInt.of(l3 + 1);
               }
            }
         }

         noisechunk.stopInterpolation();
         return OptionalInt.empty();
      }
   }

   public void buildSurface(WorldGenRegion worldgenregion, StructureManager structuremanager, RandomState randomstate, ChunkAccess chunkaccess) {
      if (!SharedConstants.debugVoidTerrain(chunkaccess.getPos())) {
         WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(this, worldgenregion);
         this.buildSurface(chunkaccess, worldgenerationcontext, randomstate, structuremanager, worldgenregion.getBiomeManager(), worldgenregion.registryAccess().registryOrThrow(Registries.BIOME), Blender.of(worldgenregion));
      }
   }

   @VisibleForTesting
   public void buildSurface(ChunkAccess chunkaccess, WorldGenerationContext worldgenerationcontext, RandomState randomstate, StructureManager structuremanager, BiomeManager biomemanager, Registry<Biome> registry, Blender blender) {
      NoiseChunk noisechunk = chunkaccess.getOrCreateNoiseChunk((chunkaccess1) -> this.createNoiseChunk(chunkaccess1, structuremanager, blender, randomstate));
      NoiseGeneratorSettings noisegeneratorsettings = this.settings.value();
      randomstate.surfaceSystem().buildSurface(randomstate, biomemanager, registry, noisegeneratorsettings.useLegacyRandomSource(), worldgenerationcontext, chunkaccess, noisechunk, noisegeneratorsettings.surfaceRule());
   }

   public void applyCarvers(WorldGenRegion worldgenregion, long i, RandomState randomstate, BiomeManager biomemanager, StructureManager structuremanager, ChunkAccess chunkaccess, GenerationStep.Carving generationstep_carving) {
      BiomeManager biomemanager1 = biomemanager.withDifferentSource((j1, k1, l1) -> this.biomeSource.getNoiseBiome(j1, k1, l1, randomstate.sampler()));
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
      int j = 8;
      ChunkPos chunkpos = chunkaccess.getPos();
      NoiseChunk noisechunk = chunkaccess.getOrCreateNoiseChunk((chunkaccess2) -> this.createNoiseChunk(chunkaccess2, structuremanager, Blender.of(worldgenregion), randomstate));
      Aquifer aquifer = noisechunk.aquifer();
      CarvingContext carvingcontext = new CarvingContext(this, worldgenregion.registryAccess(), chunkaccess.getHeightAccessorForGeneration(), noisechunk, randomstate, this.settings.value().surfaceRule());
      CarvingMask carvingmask = ((ProtoChunk)chunkaccess).getOrCreateCarvingMask(generationstep_carving);

      for(int k = -8; k <= 8; ++k) {
         for(int l = -8; l <= 8; ++l) {
            ChunkPos chunkpos1 = new ChunkPos(chunkpos.x + k, chunkpos.z + l);
            ChunkAccess chunkaccess1 = worldgenregion.getChunk(chunkpos1.x, chunkpos1.z);
            BiomeGenerationSettings biomegenerationsettings = chunkaccess1.carverBiome(() -> this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkpos1.getMinBlockX()), 0, QuartPos.fromBlock(chunkpos1.getMinBlockZ()), randomstate.sampler())));
            Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomegenerationsettings.getCarvers(generationstep_carving);
            int i1 = 0;

            for(Holder<ConfiguredWorldCarver<?>> holder : iterable) {
               ConfiguredWorldCarver<?> configuredworldcarver = holder.value();
               worldgenrandom.setLargeFeatureSeed(i + (long)i1, chunkpos1.x, chunkpos1.z);
               if (configuredworldcarver.isStartChunk(worldgenrandom)) {
                  configuredworldcarver.carve(carvingcontext, chunkaccess, biomemanager1::getBiome, worldgenrandom, aquifer, chunkpos1, carvingmask);
               }

               ++i1;
            }
         }
      }

   }

   public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomstate, StructureManager structuremanager, ChunkAccess chunkaccess) {
      NoiseSettings noisesettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunkaccess.getHeightAccessorForGeneration());
      int i = noisesettings.minY();
      int j = Mth.floorDiv(i, noisesettings.getCellHeight());
      int k = Mth.floorDiv(noisesettings.height(), noisesettings.getCellHeight());
      if (k <= 0) {
         return CompletableFuture.completedFuture(chunkaccess);
      } else {
         int l = chunkaccess.getSectionIndex(k * noisesettings.getCellHeight() - 1 + i);
         int i1 = chunkaccess.getSectionIndex(i);
         Set<LevelChunkSection> set = Sets.newHashSet();

         for(int j1 = l; j1 >= i1; --j1) {
            LevelChunkSection levelchunksection = chunkaccess.getSection(j1);
            levelchunksection.acquire();
            set.add(levelchunksection);
         }

         return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> this.doFill(blender, structuremanager, randomstate, chunkaccess, j, k)), Util.backgroundExecutor()).whenCompleteAsync((chunkaccess1, throwable) -> {
            for(LevelChunkSection levelchunksection1 : set) {
               levelchunksection1.release();
            }

         }, executor);
      }
   }

   private ChunkAccess doFill(Blender blender, StructureManager structuremanager, RandomState randomstate, ChunkAccess chunkaccess, int i, int j) {
      NoiseChunk noisechunk = chunkaccess.getOrCreateNoiseChunk((chunkaccess1) -> this.createNoiseChunk(chunkaccess1, structuremanager, blender, randomstate));
      Heightmap heightmap = chunkaccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
      Heightmap heightmap1 = chunkaccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
      ChunkPos chunkpos = chunkaccess.getPos();
      int k = chunkpos.getMinBlockX();
      int l = chunkpos.getMinBlockZ();
      Aquifer aquifer = noisechunk.aquifer();
      noisechunk.initializeForFirstCellX();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int i1 = noisechunk.cellWidth();
      int j1 = noisechunk.cellHeight();
      int k1 = 16 / i1;
      int l1 = 16 / i1;

      for(int i2 = 0; i2 < k1; ++i2) {
         noisechunk.advanceCellX(i2);

         for(int j2 = 0; j2 < l1; ++j2) {
            int k2 = chunkaccess.getSectionsCount() - 1;
            LevelChunkSection levelchunksection = chunkaccess.getSection(k2);

            for(int l2 = j - 1; l2 >= 0; --l2) {
               noisechunk.selectCellYZ(l2, j2);

               for(int i3 = j1 - 1; i3 >= 0; --i3) {
                  int j3 = (i + l2) * j1 + i3;
                  int k3 = j3 & 15;
                  int l3 = chunkaccess.getSectionIndex(j3);
                  if (k2 != l3) {
                     k2 = l3;
                     levelchunksection = chunkaccess.getSection(l3);
                  }

                  double d0 = (double)i3 / (double)j1;
                  noisechunk.updateForY(j3, d0);

                  for(int i4 = 0; i4 < i1; ++i4) {
                     int j4 = k + i2 * i1 + i4;
                     int k4 = j4 & 15;
                     double d1 = (double)i4 / (double)i1;
                     noisechunk.updateForX(j4, d1);

                     for(int l4 = 0; l4 < i1; ++l4) {
                        int i5 = l + j2 * i1 + l4;
                        int j5 = i5 & 15;
                        double d2 = (double)l4 / (double)i1;
                        noisechunk.updateForZ(i5, d2);
                        BlockState blockstate = noisechunk.getInterpolatedState();
                        if (blockstate == null) {
                           blockstate = this.settings.value().defaultBlock();
                        }

                        blockstate = this.debugPreliminarySurfaceLevel(noisechunk, j4, j3, i5, blockstate);
                        if (blockstate != AIR && !SharedConstants.debugVoidTerrain(chunkaccess.getPos())) {
                           levelchunksection.setBlockState(k4, k3, j5, blockstate, false);
                           heightmap.update(k4, j3, j5, blockstate);
                           heightmap1.update(k4, j3, j5, blockstate);
                           if (aquifer.shouldScheduleFluidUpdate() && !blockstate.getFluidState().isEmpty()) {
                              blockpos_mutableblockpos.set(j4, j3, i5);
                              chunkaccess.markPosForPostprocessing(blockpos_mutableblockpos);
                           }
                        }
                     }
                  }
               }
            }
         }

         noisechunk.swapSlices();
      }

      noisechunk.stopInterpolation();
      return chunkaccess;
   }

   private BlockState debugPreliminarySurfaceLevel(NoiseChunk noisechunk, int i, int j, int k, BlockState blockstate) {
      return blockstate;
   }

   public int getGenDepth() {
      return this.settings.value().noiseSettings().height();
   }

   public int getSeaLevel() {
      return this.settings.value().seaLevel();
   }

   public int getMinY() {
      return this.settings.value().noiseSettings().minY();
   }

   public void spawnOriginalMobs(WorldGenRegion worldgenregion) {
      if (!this.settings.value().disableMobGeneration()) {
         ChunkPos chunkpos = worldgenregion.getCenter();
         Holder<Biome> holder = worldgenregion.getBiome(chunkpos.getWorldPosition().atY(worldgenregion.getMaxBuildHeight() - 1));
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
         worldgenrandom.setDecorationSeed(worldgenregion.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
         NaturalSpawner.spawnMobsForChunkGeneration(worldgenregion, holder, chunkpos, worldgenrandom);
      }
   }
}
