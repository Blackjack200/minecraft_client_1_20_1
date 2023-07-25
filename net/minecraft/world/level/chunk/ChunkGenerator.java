package net.minecraft.world.level.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class ChunkGenerator {
   public static final Codec<ChunkGenerator> CODEC = BuiltInRegistries.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
   protected final BiomeSource biomeSource;
   private final Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;
   private final Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter;

   public ChunkGenerator(BiomeSource biomesource) {
      this(biomesource, (holder) -> holder.value().getGenerationSettings());
   }

   public ChunkGenerator(BiomeSource biomesource, Function<Holder<Biome>, BiomeGenerationSettings> function) {
      this.biomeSource = biomesource;
      this.generationSettingsGetter = function;
      this.featuresPerStep = Suppliers.memoize(() -> FeatureSorter.buildFeaturesPerStep(List.copyOf(biomesource.possibleBiomes()), (holder) -> function.apply(holder).features(), true));
   }

   protected abstract Codec<? extends ChunkGenerator> codec();

   public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderlookup, RandomState randomstate, long i) {
      return ChunkGeneratorStructureState.createForNormal(randomstate, i, this.biomeSource, holderlookup);
   }

   public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
      return BuiltInRegistries.CHUNK_GENERATOR.getResourceKey(this.codec());
   }

   public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState randomstate, Blender blender, StructureManager structuremanager, ChunkAccess chunkaccess) {
      return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
         chunkaccess.fillBiomesFromNoise(this.biomeSource, randomstate.sampler());
         return chunkaccess;
      }), Util.backgroundExecutor());
   }

   public abstract void applyCarvers(WorldGenRegion worldgenregion, long i, RandomState randomstate, BiomeManager biomemanager, StructureManager structuremanager, ChunkAccess chunkaccess, GenerationStep.Carving generationstep_carving);

   @Nullable
   public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel serverlevel, HolderSet<Structure> holderset, BlockPos blockpos, int i, boolean flag) {
      ChunkGeneratorStructureState chunkgeneratorstructurestate = serverlevel.getChunkSource().getGeneratorState();
      Map<StructurePlacement, Set<Holder<Structure>>> map = new Object2ObjectArrayMap<>();

      for(Holder<Structure> holder : holderset) {
         for(StructurePlacement structureplacement : chunkgeneratorstructurestate.getPlacementsForStructure(holder)) {
            map.computeIfAbsent(structureplacement, (structureplacement2) -> new ObjectArraySet()).add(holder);
         }
      }

      if (map.isEmpty()) {
         return null;
      } else {
         Pair<BlockPos, Holder<Structure>> pair = null;
         double d0 = Double.MAX_VALUE;
         StructureManager structuremanager = serverlevel.structureManager();
         List<Map.Entry<StructurePlacement, Set<Holder<Structure>>>> list = new ArrayList<>(map.size());

         for(Map.Entry<StructurePlacement, Set<Holder<Structure>>> map_entry : map.entrySet()) {
            StructurePlacement structureplacement1 = map_entry.getKey();
            if (structureplacement1 instanceof ConcentricRingsStructurePlacement) {
               ConcentricRingsStructurePlacement concentricringsstructureplacement = (ConcentricRingsStructurePlacement)structureplacement1;
               Pair<BlockPos, Holder<Structure>> pair1 = this.getNearestGeneratedStructure(map_entry.getValue(), serverlevel, structuremanager, blockpos, flag, concentricringsstructureplacement);
               if (pair1 != null) {
                  BlockPos blockpos1 = pair1.getFirst();
                  double d1 = blockpos.distSqr(blockpos1);
                  if (d1 < d0) {
                     d0 = d1;
                     pair = pair1;
                  }
               }
            } else if (structureplacement1 instanceof RandomSpreadStructurePlacement) {
               list.add(map_entry);
            }
         }

         if (!list.isEmpty()) {
            int j = SectionPos.blockToSectionCoord(blockpos.getX());
            int k = SectionPos.blockToSectionCoord(blockpos.getZ());

            for(int l = 0; l <= i; ++l) {
               boolean flag1 = false;

               for(Map.Entry<StructurePlacement, Set<Holder<Structure>>> map_entry1 : list) {
                  RandomSpreadStructurePlacement randomspreadstructureplacement = (RandomSpreadStructurePlacement)map_entry1.getKey();
                  Pair<BlockPos, Holder<Structure>> pair2 = getNearestGeneratedStructure(map_entry1.getValue(), serverlevel, structuremanager, j, k, l, flag, chunkgeneratorstructurestate.getLevelSeed(), randomspreadstructureplacement);
                  if (pair2 != null) {
                     flag1 = true;
                     double d2 = blockpos.distSqr(pair2.getFirst());
                     if (d2 < d0) {
                        d0 = d2;
                        pair = pair2;
                     }
                  }
               }

               if (flag1) {
                  return pair;
               }
            }
         }

         return pair;
      }
   }

   @Nullable
   private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> set, ServerLevel serverlevel, StructureManager structuremanager, BlockPos blockpos, boolean flag, ConcentricRingsStructurePlacement concentricringsstructureplacement) {
      List<ChunkPos> list = serverlevel.getChunkSource().getGeneratorState().getRingPositionsFor(concentricringsstructureplacement);
      if (list == null) {
         throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
      } else {
         Pair<BlockPos, Holder<Structure>> pair = null;
         double d0 = Double.MAX_VALUE;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(ChunkPos chunkpos : list) {
            blockpos_mutableblockpos.set(SectionPos.sectionToBlockCoord(chunkpos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkpos.z, 8));
            double d1 = blockpos_mutableblockpos.distSqr(blockpos);
            boolean flag1 = pair == null || d1 < d0;
            if (flag1) {
               Pair<BlockPos, Holder<Structure>> pair1 = getStructureGeneratingAt(set, serverlevel, structuremanager, flag, concentricringsstructureplacement, chunkpos);
               if (pair1 != null) {
                  pair = pair1;
                  d0 = d1;
               }
            }
         }

         return pair;
      }
   }

   @Nullable
   private static Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> set, LevelReader levelreader, StructureManager structuremanager, int i, int j, int k, boolean flag, long l, RandomSpreadStructurePlacement randomspreadstructureplacement) {
      int i1 = randomspreadstructureplacement.spacing();

      for(int j1 = -k; j1 <= k; ++j1) {
         boolean flag1 = j1 == -k || j1 == k;

         for(int k1 = -k; k1 <= k; ++k1) {
            boolean flag2 = k1 == -k || k1 == k;
            if (flag1 || flag2) {
               int l1 = i + i1 * j1;
               int i2 = j + i1 * k1;
               ChunkPos chunkpos = randomspreadstructureplacement.getPotentialStructureChunk(l, l1, i2);
               Pair<BlockPos, Holder<Structure>> pair = getStructureGeneratingAt(set, levelreader, structuremanager, flag, randomspreadstructureplacement, chunkpos);
               if (pair != null) {
                  return pair;
               }
            }
         }
      }

      return null;
   }

   @Nullable
   private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(Set<Holder<Structure>> set, LevelReader levelreader, StructureManager structuremanager, boolean flag, StructurePlacement structureplacement, ChunkPos chunkpos) {
      for(Holder<Structure> holder : set) {
         StructureCheckResult structurecheckresult = structuremanager.checkStructurePresence(chunkpos, holder.value(), flag);
         if (structurecheckresult != StructureCheckResult.START_NOT_PRESENT) {
            if (!flag && structurecheckresult == StructureCheckResult.START_PRESENT) {
               return Pair.of(structureplacement.getLocatePos(chunkpos), holder);
            }

            ChunkAccess chunkaccess = levelreader.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structurestart = structuremanager.getStartForStructure(SectionPos.bottomOf(chunkaccess), holder.value(), chunkaccess);
            if (structurestart != null && structurestart.isValid() && (!flag || tryAddReference(structuremanager, structurestart))) {
               return Pair.of(structureplacement.getLocatePos(structurestart.getChunkPos()), holder);
            }
         }
      }

      return null;
   }

   private static boolean tryAddReference(StructureManager structuremanager, StructureStart structurestart) {
      if (structurestart.canBeReferenced()) {
         structuremanager.addReference(structurestart);
         return true;
      } else {
         return false;
      }
   }

   public void applyBiomeDecoration(WorldGenLevel worldgenlevel, ChunkAccess chunkaccess, StructureManager structuremanager) {
      ChunkPos chunkpos = chunkaccess.getPos();
      if (!SharedConstants.debugVoidTerrain(chunkpos)) {
         SectionPos sectionpos = SectionPos.of(chunkpos, worldgenlevel.getMinSection());
         BlockPos blockpos = sectionpos.origin();
         Registry<Structure> registry = worldgenlevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
         Map<Integer, List<Structure>> map = registry.stream().collect(Collectors.groupingBy((structure2) -> structure2.step().ordinal()));
         List<FeatureSorter.StepFeatureData> list = this.featuresPerStep.get();
         WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
         long i = worldgenrandom.setDecorationSeed(worldgenlevel.getSeed(), blockpos.getX(), blockpos.getZ());
         Set<Holder<Biome>> set = new ObjectArraySet<>();
         ChunkPos.rangeClosed(sectionpos.chunk(), 1).forEach((chunkpos2) -> {
            ChunkAccess chunkaccess2 = worldgenlevel.getChunk(chunkpos2.x, chunkpos2.z);

            for(LevelChunkSection levelchunksection : chunkaccess2.getSections()) {
               levelchunksection.getBiomes().getAll(set::add);
            }

         });
         set.retainAll(this.biomeSource.possibleBiomes());
         int j = list.size();

         try {
            Registry<PlacedFeature> registry1 = worldgenlevel.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            int k = Math.max(GenerationStep.Decoration.values().length, j);

            for(int l = 0; l < k; ++l) {
               int i1 = 0;
               if (structuremanager.shouldGenerateStructures()) {
                  for(Structure structure : map.getOrDefault(l, Collections.emptyList())) {
                     worldgenrandom.setFeatureSeed(i, i1, l);
                     Supplier<String> supplier = () -> registry.getResourceKey(structure).map(Object::toString).orElseGet(structure::toString);

                     try {
                        worldgenlevel.setCurrentlyGenerating(supplier);
                        structuremanager.startsForStructure(sectionpos, structure).forEach((structurestart) -> structurestart.placeInChunk(worldgenlevel, structuremanager, this, worldgenrandom, getWritableArea(chunkaccess), chunkpos));
                     } catch (Exception var29) {
                        CrashReport crashreport = CrashReport.forThrowable(var29, "Feature placement");
                        crashreport.addCategory("Feature").setDetail("Description", supplier::get);
                        throw new ReportedException(crashreport);
                     }

                     ++i1;
                  }
               }

               if (l < j) {
                  IntSet intset = new IntArraySet();

                  for(Holder<Biome> holder : set) {
                     List<HolderSet<PlacedFeature>> list2 = this.generationSettingsGetter.apply(holder).features();
                     if (l < list2.size()) {
                        HolderSet<PlacedFeature> holderset = list2.get(l);
                        FeatureSorter.StepFeatureData featuresorter_stepfeaturedata = list.get(l);
                        holderset.stream().map(Holder::value).forEach((placedfeature2) -> intset.add(featuresorter_stepfeaturedata.indexMapping().applyAsInt(placedfeature2)));
                     }
                  }

                  int j1 = intset.size();
                  int[] aint = intset.toIntArray();
                  Arrays.sort(aint);
                  FeatureSorter.StepFeatureData featuresorter_stepfeaturedata1 = list.get(l);

                  for(int k1 = 0; k1 < j1; ++k1) {
                     int l1 = aint[k1];
                     PlacedFeature placedfeature = featuresorter_stepfeaturedata1.features().get(l1);
                     Supplier<String> supplier1 = () -> registry1.getResourceKey(placedfeature).map(Object::toString).orElseGet(placedfeature::toString);
                     worldgenrandom.setFeatureSeed(i, l1, l);

                     try {
                        worldgenlevel.setCurrentlyGenerating(supplier1);
                        placedfeature.placeWithBiomeCheck(worldgenlevel, this, worldgenrandom, blockpos);
                     } catch (Exception var30) {
                        CrashReport crashreport1 = CrashReport.forThrowable(var30, "Feature placement");
                        crashreport1.addCategory("Feature").setDetail("Description", supplier1::get);
                        throw new ReportedException(crashreport1);
                     }
                  }
               }
            }

            worldgenlevel.setCurrentlyGenerating((Supplier<String>)null);
         } catch (Exception var31) {
            CrashReport crashreport2 = CrashReport.forThrowable(var31, "Biome decoration");
            crashreport2.addCategory("Generation").setDetail("CenterX", chunkpos.x).setDetail("CenterZ", chunkpos.z).setDetail("Seed", i);
            throw new ReportedException(crashreport2);
         }
      }
   }

   private static BoundingBox getWritableArea(ChunkAccess chunkaccess) {
      ChunkPos chunkpos = chunkaccess.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      LevelHeightAccessor levelheightaccessor = chunkaccess.getHeightAccessorForGeneration();
      int k = levelheightaccessor.getMinBuildHeight() + 1;
      int l = levelheightaccessor.getMaxBuildHeight() - 1;
      return new BoundingBox(i, k, j, i + 15, l, j + 15);
   }

   public abstract void buildSurface(WorldGenRegion worldgenregion, StructureManager structuremanager, RandomState randomstate, ChunkAccess chunkaccess);

   public abstract void spawnOriginalMobs(WorldGenRegion worldgenregion);

   public int getSpawnHeight(LevelHeightAccessor levelheightaccessor) {
      return 64;
   }

   public BiomeSource getBiomeSource() {
      return this.biomeSource;
   }

   public abstract int getGenDepth();

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureManager structuremanager, MobCategory mobcategory, BlockPos blockpos) {
      Map<Structure, LongSet> map = structuremanager.getAllStructuresAt(blockpos);

      for(Map.Entry<Structure, LongSet> map_entry : map.entrySet()) {
         Structure structure = map_entry.getKey();
         StructureSpawnOverride structurespawnoverride = structure.spawnOverrides().get(mobcategory);
         if (structurespawnoverride != null) {
            MutableBoolean mutableboolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structurespawnoverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE ? (structurestart2) -> structuremanager.structureHasPieceAt(blockpos, structurestart2) : (structurestart1) -> structurestart1.getBoundingBox().isInside(blockpos);
            structuremanager.fillStartsForStructure(structure, map_entry.getValue(), (structurestart) -> {
               if (mutableboolean.isFalse() && predicate.test(structurestart)) {
                  mutableboolean.setTrue();
               }

            });
            if (mutableboolean.isTrue()) {
               return structurespawnoverride.spawns();
            }
         }
      }

      return holder.value().getMobSettings().getMobs(mobcategory);
   }

   public void createStructures(RegistryAccess registryaccess, ChunkGeneratorStructureState chunkgeneratorstructurestate, StructureManager structuremanager, ChunkAccess chunkaccess, StructureTemplateManager structuretemplatemanager) {
      ChunkPos chunkpos = chunkaccess.getPos();
      SectionPos sectionpos = SectionPos.bottomOf(chunkaccess);
      RandomState randomstate = chunkgeneratorstructurestate.randomState();
      chunkgeneratorstructurestate.possibleStructureSets().forEach((holder) -> {
         StructurePlacement structureplacement = holder.value().placement();
         List<StructureSet.StructureSelectionEntry> list = holder.value().structures();

         for(StructureSet.StructureSelectionEntry structureset_structureselectionentry : list) {
            StructureStart structurestart = structuremanager.getStartForStructure(sectionpos, structureset_structureselectionentry.structure().value(), chunkaccess);
            if (structurestart != null && structurestart.isValid()) {
               return;
            }
         }

         if (structureplacement.isStructureChunk(chunkgeneratorstructurestate, chunkpos.x, chunkpos.z)) {
            if (list.size() == 1) {
               this.tryGenerateStructure(list.get(0), structuremanager, registryaccess, randomstate, structuretemplatemanager, chunkgeneratorstructurestate.getLevelSeed(), chunkaccess, chunkpos, sectionpos);
            } else {
               ArrayList<StructureSet.StructureSelectionEntry> arraylist = new ArrayList<>(list.size());
               arraylist.addAll(list);
               WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
               worldgenrandom.setLargeFeatureSeed(chunkgeneratorstructurestate.getLevelSeed(), chunkpos.x, chunkpos.z);
               int i = 0;

               for(StructureSet.StructureSelectionEntry structureset_structureselectionentry1 : arraylist) {
                  i += structureset_structureselectionentry1.weight();
               }

               while(!arraylist.isEmpty()) {
                  int j = worldgenrandom.nextInt(i);
                  int k = 0;

                  for(StructureSet.StructureSelectionEntry structureset_structureselectionentry2 : arraylist) {
                     j -= structureset_structureselectionentry2.weight();
                     if (j < 0) {
                        break;
                     }

                     ++k;
                  }

                  StructureSet.StructureSelectionEntry structureset_structureselectionentry3 = arraylist.get(k);
                  if (this.tryGenerateStructure(structureset_structureselectionentry3, structuremanager, registryaccess, randomstate, structuretemplatemanager, chunkgeneratorstructurestate.getLevelSeed(), chunkaccess, chunkpos, sectionpos)) {
                     return;
                  }

                  arraylist.remove(k);
                  i -= structureset_structureselectionentry3.weight();
               }

            }
         }
      });
   }

   private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureset_structureselectionentry, StructureManager structuremanager, RegistryAccess registryaccess, RandomState randomstate, StructureTemplateManager structuretemplatemanager, long i, ChunkAccess chunkaccess, ChunkPos chunkpos, SectionPos sectionpos) {
      Structure structure = structureset_structureselectionentry.structure().value();
      int j = fetchReferences(structuremanager, chunkaccess, sectionpos, structure);
      HolderSet<Biome> holderset = structure.biomes();
      Predicate<Holder<Biome>> predicate = holderset::contains;
      StructureStart structurestart = structure.generate(registryaccess, this, this.biomeSource, randomstate, structuretemplatemanager, i, chunkpos, j, chunkaccess, predicate);
      if (structurestart.isValid()) {
         structuremanager.setStartForStructure(sectionpos, structure, structurestart, chunkaccess);
         return true;
      } else {
         return false;
      }
   }

   private static int fetchReferences(StructureManager structuremanager, ChunkAccess chunkaccess, SectionPos sectionpos, Structure structure) {
      StructureStart structurestart = structuremanager.getStartForStructure(sectionpos, structure, chunkaccess);
      return structurestart != null ? structurestart.getReferences() : 0;
   }

   public void createReferences(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkAccess chunkaccess) {
      int i = 8;
      ChunkPos chunkpos = chunkaccess.getPos();
      int j = chunkpos.x;
      int k = chunkpos.z;
      int l = chunkpos.getMinBlockX();
      int i1 = chunkpos.getMinBlockZ();
      SectionPos sectionpos = SectionPos.bottomOf(chunkaccess);

      for(int j1 = j - 8; j1 <= j + 8; ++j1) {
         for(int k1 = k - 8; k1 <= k + 8; ++k1) {
            long l1 = ChunkPos.asLong(j1, k1);

            for(StructureStart structurestart : worldgenlevel.getChunk(j1, k1).getAllStarts().values()) {
               try {
                  if (structurestart.isValid() && structurestart.getBoundingBox().intersects(l, i1, l + 15, i1 + 15)) {
                     structuremanager.addReferenceForStructure(sectionpos, structurestart.getStructure(), l1, chunkaccess);
                     DebugPackets.sendStructurePacket(worldgenlevel, structurestart);
                  }
               } catch (Exception var21) {
                  CrashReport crashreport = CrashReport.forThrowable(var21, "Generating structure reference");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Structure");
                  Optional<? extends Registry<Structure>> optional = worldgenlevel.registryAccess().registry(Registries.STRUCTURE);
                  crashreportcategory.setDetail("Id", () -> optional.map((registry) -> registry.getKey(structurestart.getStructure()).toString()).orElse("UNKNOWN"));
                  crashreportcategory.setDetail("Name", () -> BuiltInRegistries.STRUCTURE_TYPE.getKey(structurestart.getStructure().type()).toString());
                  crashreportcategory.setDetail("Class", () -> structurestart.getStructure().getClass().getCanonicalName());
                  throw new ReportedException(crashreport);
               }
            }
         }
      }

   }

   public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomstate, StructureManager structuremanager, ChunkAccess chunkaccess);

   public abstract int getSeaLevel();

   public abstract int getMinY();

   public abstract int getBaseHeight(int i, int j, Heightmap.Types heightmap_types, LevelHeightAccessor levelheightaccessor, RandomState randomstate);

   public abstract NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate);

   public int getFirstFreeHeight(int i, int j, Heightmap.Types heightmap_types, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      return this.getBaseHeight(i, j, heightmap_types, levelheightaccessor, randomstate);
   }

   public int getFirstOccupiedHeight(int i, int j, Heightmap.Types heightmap_types, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
      return this.getBaseHeight(i, j, heightmap_types, levelheightaccessor, randomstate) - 1;
   }

   public abstract void addDebugScreenInfo(List<String> list, RandomState randomstate, BlockPos blockpos);

   /** @deprecated */
   @Deprecated
   public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> holder) {
      return this.generationSettingsGetter.apply(holder);
   }
}
