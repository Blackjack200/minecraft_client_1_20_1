package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ChunkStatus {
   public static final int MAX_STRUCTURE_DISTANCE = 8;
   private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
   public static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
   private static final ChunkStatus.LoadingTask PASSTHROUGH_LOAD_TASK = (chunkstatus, serverlevel, structuretemplatemanager, threadedlevellightengine, function, chunkaccess) -> CompletableFuture.completedFuture(Either.left(chunkaccess));
   public static final ChunkStatus EMPTY = registerSimple("empty", (ChunkStatus)null, -1, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, serverlevel, chunkgenerator, list, chunkaccess) -> {
   });
   public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", EMPTY, 0, false, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess) -> {
      if (serverlevel.getServer().getWorldData().worldGenOptions().generateStructures()) {
         chunkgenerator.createStructures(serverlevel.registryAccess(), serverlevel.getChunkSource().getGeneratorState(), serverlevel.structureManager(), chunkaccess, structuretemplatemanager);
      }

      serverlevel.onStructureStartsAvailable(chunkaccess);
      return CompletableFuture.completedFuture(Either.left(chunkaccess));
   }, (chunkstatus, serverlevel, structuretemplatemanager, threadedlevellightengine, function, chunkaccess) -> {
      serverlevel.onStructureStartsAvailable(chunkaccess);
      return CompletableFuture.completedFuture(Either.left(chunkaccess));
   });
   public static final ChunkStatus STRUCTURE_REFERENCES = registerSimple("structure_references", STRUCTURE_STARTS, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, serverlevel, chunkgenerator, list, chunkaccess) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, list, chunkstatus, -1);
      chunkgenerator.createReferences(worldgenregion, serverlevel.structureManager().forWorldGenRegion(worldgenregion), chunkaccess);
   });
   public static final ChunkStatus BIOMES = register("biomes", STRUCTURE_REFERENCES, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, list, chunkstatus, -1);
      return chunkgenerator.createBiomes(executor, serverlevel.getChunkSource().randomState(), Blender.of(worldgenregion), serverlevel.structureManager().forWorldGenRegion(worldgenregion), chunkaccess).thenApply((chunkaccess1) -> Either.left(chunkaccess1));
   });
   public static final ChunkStatus NOISE = register("noise", BIOMES, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, list, chunkstatus, 0);
      return chunkgenerator.fillFromNoise(executor, Blender.of(worldgenregion), serverlevel.getChunkSource().randomState(), serverlevel.structureManager().forWorldGenRegion(worldgenregion), chunkaccess).thenApply((chunkaccess1) -> {
         if (chunkaccess1 instanceof ProtoChunk protochunk) {
            BelowZeroRetrogen belowzeroretrogen = protochunk.getBelowZeroRetrogen();
            if (belowzeroretrogen != null) {
               BelowZeroRetrogen.replaceOldBedrock(protochunk);
               if (belowzeroretrogen.hasBedrockHoles()) {
                  belowzeroretrogen.applyBedrockMask(protochunk);
               }
            }
         }

         return Either.left(chunkaccess1);
      });
   });
   public static final ChunkStatus SURFACE = registerSimple("surface", NOISE, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, serverlevel, chunkgenerator, list, chunkaccess) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, list, chunkstatus, 0);
      chunkgenerator.buildSurface(worldgenregion, serverlevel.structureManager().forWorldGenRegion(worldgenregion), serverlevel.getChunkSource().randomState(), chunkaccess);
   });
   public static final ChunkStatus CARVERS = registerSimple("carvers", SURFACE, 8, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, serverlevel, chunkgenerator, list, chunkaccess) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, list, chunkstatus, 0);
      if (chunkaccess instanceof ProtoChunk protochunk) {
         Blender.addAroundOldChunksCarvingMaskFilter(worldgenregion, protochunk);
      }

      chunkgenerator.applyCarvers(worldgenregion, serverlevel.getSeed(), serverlevel.getChunkSource().randomState(), serverlevel.getBiomeManager(), serverlevel.structureManager().forWorldGenRegion(worldgenregion), chunkaccess, GenerationStep.Carving.AIR);
   });
   public static final ChunkStatus FEATURES = registerSimple("features", CARVERS, 8, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, serverlevel, chunkgenerator, list, chunkaccess) -> {
      Heightmap.primeHeightmaps(chunkaccess, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
      WorldGenRegion worldgenregion = new WorldGenRegion(serverlevel, list, chunkstatus, 1);
      chunkgenerator.applyBiomeDecoration(worldgenregion, chunkaccess, serverlevel.structureManager().forWorldGenRegion(worldgenregion));
      Blender.generateBorderTicks(worldgenregion, chunkaccess);
   });
   public static final ChunkStatus INITIALIZE_LIGHT = register("initialize_light", FEATURES, 0, false, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess) -> initializeLight(threadedlevellightengine, chunkaccess), (chunkstatus, serverlevel, structuretemplatemanager, threadedlevellightengine, function, chunkaccess) -> initializeLight(threadedlevellightengine, chunkaccess));
   public static final ChunkStatus LIGHT = register("light", INITIALIZE_LIGHT, 1, true, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess) -> lightChunk(threadedlevellightengine, chunkaccess), (chunkstatus, serverlevel, structuretemplatemanager, threadedlevellightengine, function, chunkaccess) -> lightChunk(threadedlevellightengine, chunkaccess));
   public static final ChunkStatus SPAWN = registerSimple("spawn", LIGHT, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (chunkstatus, serverlevel, chunkgenerator, list, chunkaccess) -> {
      if (!chunkaccess.isUpgrading()) {
         chunkgenerator.spawnOriginalMobs(new WorldGenRegion(serverlevel, list, chunkstatus, -1));
      }

   });
   public static final ChunkStatus FULL = register("full", SPAWN, 0, false, POST_FEATURES, ChunkStatus.ChunkType.LEVELCHUNK, (chunkstatus, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess) -> function.apply(chunkaccess), (chunkstatus, serverlevel, structuretemplatemanager, threadedlevellightengine, function, chunkaccess) -> function.apply(chunkaccess));
   private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(FULL, INITIALIZE_LIGHT, CARVERS, BIOMES, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS);
   private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(getStatusList().size()), (intarraylist) -> {
      int i = 0;

      for(int j = getStatusList().size() - 1; j >= 0; --j) {
         while(i + 1 < STATUS_BY_RANGE.size() && j <= STATUS_BY_RANGE.get(i + 1).getIndex()) {
            ++i;
         }

         intarraylist.add(0, i);
      }

   });
   private final int index;
   private final ChunkStatus parent;
   private final ChunkStatus.GenerationTask generationTask;
   private final ChunkStatus.LoadingTask loadingTask;
   private final int range;
   private final boolean hasLoadDependencies;
   private final ChunkStatus.ChunkType chunkType;
   private final EnumSet<Heightmap.Types> heightmapsAfter;

   private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> initializeLight(ThreadedLevelLightEngine threadedlevellightengine, ChunkAccess chunkaccess) {
      chunkaccess.initializeLightSources();
      ((ProtoChunk)chunkaccess).setLightEngine(threadedlevellightengine);
      boolean flag = isLighted(chunkaccess);
      return threadedlevellightengine.initializeLight(chunkaccess, flag).thenApply(Either::left);
   }

   private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(ThreadedLevelLightEngine threadedlevellightengine, ChunkAccess chunkaccess) {
      boolean flag = isLighted(chunkaccess);
      return threadedlevellightengine.lightChunk(chunkaccess, flag).thenApply(Either::left);
   }

   private static ChunkStatus registerSimple(String s, @Nullable ChunkStatus chunkstatus, int i, EnumSet<Heightmap.Types> enumset, ChunkStatus.ChunkType chunkstatus_chunktype, ChunkStatus.SimpleGenerationTask chunkstatus_simplegenerationtask) {
      return register(s, chunkstatus, i, enumset, chunkstatus_chunktype, chunkstatus_simplegenerationtask);
   }

   private static ChunkStatus register(String s, @Nullable ChunkStatus chunkstatus, int i, EnumSet<Heightmap.Types> enumset, ChunkStatus.ChunkType chunkstatus_chunktype, ChunkStatus.GenerationTask chunkstatus_generationtask) {
      return register(s, chunkstatus, i, false, enumset, chunkstatus_chunktype, chunkstatus_generationtask, PASSTHROUGH_LOAD_TASK);
   }

   private static ChunkStatus register(String s, @Nullable ChunkStatus chunkstatus, int i, boolean flag, EnumSet<Heightmap.Types> enumset, ChunkStatus.ChunkType chunkstatus_chunktype, ChunkStatus.GenerationTask chunkstatus_generationtask, ChunkStatus.LoadingTask chunkstatus_loadingtask) {
      return Registry.register(BuiltInRegistries.CHUNK_STATUS, s, new ChunkStatus(chunkstatus, i, flag, enumset, chunkstatus_chunktype, chunkstatus_generationtask, chunkstatus_loadingtask));
   }

   public static List<ChunkStatus> getStatusList() {
      List<ChunkStatus> list = Lists.newArrayList();

      ChunkStatus chunkstatus;
      for(chunkstatus = FULL; chunkstatus.getParent() != chunkstatus; chunkstatus = chunkstatus.getParent()) {
         list.add(chunkstatus);
      }

      list.add(chunkstatus);
      Collections.reverse(list);
      return list;
   }

   private static boolean isLighted(ChunkAccess chunkaccess) {
      return chunkaccess.getStatus().isOrAfter(LIGHT) && chunkaccess.isLightCorrect();
   }

   public static ChunkStatus getStatusAroundFullChunk(int i) {
      if (i >= STATUS_BY_RANGE.size()) {
         return EMPTY;
      } else {
         return i < 0 ? FULL : STATUS_BY_RANGE.get(i);
      }
   }

   public static int maxDistance() {
      return STATUS_BY_RANGE.size();
   }

   public static int getDistance(ChunkStatus chunkstatus) {
      return RANGE_BY_STATUS.getInt(chunkstatus.getIndex());
   }

   ChunkStatus(@Nullable ChunkStatus chunkstatus, int i, boolean flag, EnumSet<Heightmap.Types> enumset, ChunkStatus.ChunkType chunkstatus_chunktype, ChunkStatus.GenerationTask chunkstatus_generationtask, ChunkStatus.LoadingTask chunkstatus_loadingtask) {
      this.parent = chunkstatus == null ? this : chunkstatus;
      this.generationTask = chunkstatus_generationtask;
      this.loadingTask = chunkstatus_loadingtask;
      this.range = i;
      this.hasLoadDependencies = flag;
      this.chunkType = chunkstatus_chunktype;
      this.heightmapsAfter = enumset;
      this.index = chunkstatus == null ? 0 : chunkstatus.getIndex() + 1;
   }

   public int getIndex() {
      return this.index;
   }

   public ChunkStatus getParent() {
      return this.parent;
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(Executor executor, ServerLevel serverlevel, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, ThreadedLevelLightEngine threadedlevellightengine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list) {
      ChunkAccess chunkaccess = list.get(list.size() / 2);
      ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onChunkGenerate(chunkaccess.getPos(), serverlevel.dimension(), this.toString());
      return this.generationTask.doWork(this, executor, serverlevel, chunkgenerator, structuretemplatemanager, threadedlevellightengine, function, list, chunkaccess).thenApply((either) -> {
         either.ifLeft((chunkaccess1) -> {
            if (chunkaccess1 instanceof ProtoChunk protochunk) {
               if (!protochunk.getStatus().isOrAfter(this)) {
                  protochunk.setStatus(this);
               }
            }

         });
         if (profiledduration != null) {
            profiledduration.finish();
         }

         return either;
      });
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(ServerLevel serverlevel, StructureTemplateManager structuretemplatemanager, ThreadedLevelLightEngine threadedlevellightengine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, ChunkAccess chunkaccess) {
      return this.loadingTask.doWork(this, serverlevel, structuretemplatemanager, threadedlevellightengine, function, chunkaccess);
   }

   public int getRange() {
      return this.range;
   }

   public boolean hasLoadDependencies() {
      return this.hasLoadDependencies;
   }

   public ChunkStatus.ChunkType getChunkType() {
      return this.chunkType;
   }

   public static ChunkStatus byName(String s) {
      return BuiltInRegistries.CHUNK_STATUS.get(ResourceLocation.tryParse(s));
   }

   public EnumSet<Heightmap.Types> heightmapsAfter() {
      return this.heightmapsAfter;
   }

   public boolean isOrAfter(ChunkStatus chunkstatus) {
      return this.getIndex() >= chunkstatus.getIndex();
   }

   public String toString() {
      return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
   }

   public static enum ChunkType {
      PROTOCHUNK,
      LEVELCHUNK;
   }

   interface GenerationTask {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus chunkstatus, Executor executor, ServerLevel serverlevel, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, ThreadedLevelLightEngine threadedlevellightengine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, ChunkAccess chunkaccess);
   }

   interface LoadingTask {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus chunkstatus, ServerLevel serverlevel, StructureTemplateManager structuretemplatemanager, ThreadedLevelLightEngine threadedlevellightengine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, ChunkAccess chunkaccess);
   }

   interface SimpleGenerationTask extends ChunkStatus.GenerationTask {
      default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus chunkstatus, Executor executor, ServerLevel serverlevel, ChunkGenerator chunkgenerator, StructureTemplateManager structuretemplatemanager, ThreadedLevelLightEngine threadedlevellightengine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, List<ChunkAccess> list, ChunkAccess chunkaccess) {
         this.doWork(chunkstatus, serverlevel, chunkgenerator, list, chunkaccess);
         return CompletableFuture.completedFuture(Either.left(chunkaccess));
      }

      void doWork(ChunkStatus chunkstatus, ServerLevel serverlevel, ChunkGenerator chunkgenerator, List<ChunkAccess> list, ChunkAccess chunkaccess);
   }
}
