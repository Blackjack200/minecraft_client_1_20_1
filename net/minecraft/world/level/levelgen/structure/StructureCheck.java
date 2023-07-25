package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkScanAccess;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class StructureCheck {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int NO_STRUCTURE = -1;
   private final ChunkScanAccess storageAccess;
   private final RegistryAccess registryAccess;
   private final Registry<Biome> biomes;
   private final Registry<Structure> structureConfigs;
   private final StructureTemplateManager structureTemplateManager;
   private final ResourceKey<Level> dimension;
   private final ChunkGenerator chunkGenerator;
   private final RandomState randomState;
   private final LevelHeightAccessor heightAccessor;
   private final BiomeSource biomeSource;
   private final long seed;
   private final DataFixer fixerUpper;
   private final Long2ObjectMap<Object2IntMap<Structure>> loadedChunks = new Long2ObjectOpenHashMap<>();
   private final Map<Structure, Long2BooleanMap> featureChecks = new HashMap<>();

   public StructureCheck(ChunkScanAccess chunkscanaccess, RegistryAccess registryaccess, StructureTemplateManager structuretemplatemanager, ResourceKey<Level> resourcekey, ChunkGenerator chunkgenerator, RandomState randomstate, LevelHeightAccessor levelheightaccessor, BiomeSource biomesource, long i, DataFixer datafixer) {
      this.storageAccess = chunkscanaccess;
      this.registryAccess = registryaccess;
      this.structureTemplateManager = structuretemplatemanager;
      this.dimension = resourcekey;
      this.chunkGenerator = chunkgenerator;
      this.randomState = randomstate;
      this.heightAccessor = levelheightaccessor;
      this.biomeSource = biomesource;
      this.seed = i;
      this.fixerUpper = datafixer;
      this.biomes = registryaccess.registryOrThrow(Registries.BIOME);
      this.structureConfigs = registryaccess.registryOrThrow(Registries.STRUCTURE);
   }

   public StructureCheckResult checkStart(ChunkPos chunkpos, Structure structure, boolean flag) {
      long i = chunkpos.toLong();
      Object2IntMap<Structure> object2intmap = this.loadedChunks.get(i);
      if (object2intmap != null) {
         return this.checkStructureInfo(object2intmap, structure, flag);
      } else {
         StructureCheckResult structurecheckresult = this.tryLoadFromStorage(chunkpos, structure, flag, i);
         if (structurecheckresult != null) {
            return structurecheckresult;
         } else {
            boolean flag1 = this.featureChecks.computeIfAbsent(structure, (structure2) -> new Long2BooleanOpenHashMap()).computeIfAbsent(i, (j) -> this.canCreateStructure(chunkpos, structure));
            return !flag1 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.CHUNK_LOAD_NEEDED;
         }
      }
   }

   private boolean canCreateStructure(ChunkPos chunkpos, Structure structure) {
      return structure.findValidGenerationPoint(new Structure.GenerationContext(this.registryAccess, this.chunkGenerator, this.biomeSource, this.randomState, this.structureTemplateManager, this.seed, chunkpos, this.heightAccessor, structure.biomes()::contains)).isPresent();
   }

   @Nullable
   private StructureCheckResult tryLoadFromStorage(ChunkPos chunkpos, Structure structure, boolean flag, long i) {
      CollectFields collectfields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector("Level", "Structures", CompoundTag.TYPE, "Starts"), new FieldSelector("structures", CompoundTag.TYPE, "starts"));

      try {
         this.storageAccess.scanChunk(chunkpos, collectfields).join();
      } catch (Exception var13) {
         LOGGER.warn("Failed to read chunk {}", chunkpos, var13);
         return StructureCheckResult.CHUNK_LOAD_NEEDED;
      }

      Tag tag = collectfields.getResult();
      if (!(tag instanceof CompoundTag compoundtag)) {
         return null;
      } else {
         int j = ChunkStorage.getVersion(compoundtag);
         if (j <= 1493) {
            return StructureCheckResult.CHUNK_LOAD_NEEDED;
         } else {
            ChunkStorage.injectDatafixingContext(compoundtag, this.dimension, this.chunkGenerator.getTypeNameForDataFixer());

            CompoundTag compoundtag1;
            try {
               compoundtag1 = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, compoundtag, j);
            } catch (Exception var12) {
               LOGGER.warn("Failed to partially datafix chunk {}", chunkpos, var12);
               return StructureCheckResult.CHUNK_LOAD_NEEDED;
            }

            Object2IntMap<Structure> object2intmap = this.loadStructures(compoundtag1);
            if (object2intmap == null) {
               return null;
            } else {
               this.storeFullResults(i, object2intmap);
               return this.checkStructureInfo(object2intmap, structure, flag);
            }
         }
      }
   }

   @Nullable
   private Object2IntMap<Structure> loadStructures(CompoundTag compoundtag) {
      if (!compoundtag.contains("structures", 10)) {
         return null;
      } else {
         CompoundTag compoundtag1 = compoundtag.getCompound("structures");
         if (!compoundtag1.contains("starts", 10)) {
            return null;
         } else {
            CompoundTag compoundtag2 = compoundtag1.getCompound("starts");
            if (compoundtag2.isEmpty()) {
               return Object2IntMaps.emptyMap();
            } else {
               Object2IntMap<Structure> object2intmap = new Object2IntOpenHashMap<>();
               Registry<Structure> registry = this.registryAccess.registryOrThrow(Registries.STRUCTURE);

               for(String s : compoundtag2.getAllKeys()) {
                  ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
                  if (resourcelocation != null) {
                     Structure structure = registry.get(resourcelocation);
                     if (structure != null) {
                        CompoundTag compoundtag3 = compoundtag2.getCompound(s);
                        if (!compoundtag3.isEmpty()) {
                           String s1 = compoundtag3.getString("id");
                           if (!"INVALID".equals(s1)) {
                              int i = compoundtag3.getInt("references");
                              object2intmap.put(structure, i);
                           }
                        }
                     }
                  }
               }

               return object2intmap;
            }
         }
      }
   }

   private static Object2IntMap<Structure> deduplicateEmptyMap(Object2IntMap<Structure> object2intmap) {
      return object2intmap.isEmpty() ? Object2IntMaps.emptyMap() : object2intmap;
   }

   private StructureCheckResult checkStructureInfo(Object2IntMap<Structure> object2intmap, Structure structure, boolean flag) {
      int i = object2intmap.getOrDefault(structure, -1);
      return i == -1 || flag && i != 0 ? StructureCheckResult.START_NOT_PRESENT : StructureCheckResult.START_PRESENT;
   }

   public void onStructureLoad(ChunkPos chunkpos, Map<Structure, StructureStart> map) {
      long i = chunkpos.toLong();
      Object2IntMap<Structure> object2intmap = new Object2IntOpenHashMap<>();
      map.forEach((structure, structurestart) -> {
         if (structurestart.isValid()) {
            object2intmap.put(structure, structurestart.getReferences());
         }

      });
      this.storeFullResults(i, object2intmap);
   }

   private void storeFullResults(long i, Object2IntMap<Structure> object2intmap) {
      this.loadedChunks.put(i, deduplicateEmptyMap(object2intmap));
      this.featureChecks.values().forEach((long2booleanmap) -> long2booleanmap.remove(i));
   }

   public void incrementReference(ChunkPos chunkpos, Structure structure) {
      this.loadedChunks.compute(chunkpos.toLong(), (olong, object2intmap) -> {
         if (object2intmap == null || object2intmap.isEmpty()) {
            object2intmap = new Object2IntOpenHashMap<>();
         }

         object2intmap.computeInt(structure, (structure2, integer) -> integer == null ? 1 : integer + 1);
         return object2intmap;
      });
   }
}
