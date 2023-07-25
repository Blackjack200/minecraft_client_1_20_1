package net.minecraft.world.level.levelgen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class Structure {
   public static final Codec<Structure> DIRECT_CODEC = BuiltInRegistries.STRUCTURE_TYPE.byNameCodec().dispatch(Structure::type, StructureType::codec);
   public static final Codec<Holder<Structure>> CODEC = RegistryFileCodec.create(Registries.STRUCTURE, DIRECT_CODEC);
   protected final Structure.StructureSettings settings;

   public static <S extends Structure> RecordCodecBuilder<S, Structure.StructureSettings> settingsCodec(RecordCodecBuilder.Instance<S> recordcodecbuilder_instance) {
      return Structure.StructureSettings.CODEC.forGetter((structure) -> structure.settings);
   }

   public static <S extends Structure> Codec<S> simpleCodec(Function<Structure.StructureSettings, S> function) {
      return RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(settingsCodec(recordcodecbuilder_instance)).apply(recordcodecbuilder_instance, function));
   }

   protected Structure(Structure.StructureSettings structure_structuresettings) {
      this.settings = structure_structuresettings;
   }

   public HolderSet<Biome> biomes() {
      return this.settings.biomes;
   }

   public Map<MobCategory, StructureSpawnOverride> spawnOverrides() {
      return this.settings.spawnOverrides;
   }

   public GenerationStep.Decoration step() {
      return this.settings.step;
   }

   public TerrainAdjustment terrainAdaptation() {
      return this.settings.terrainAdaptation;
   }

   public BoundingBox adjustBoundingBox(BoundingBox boundingbox) {
      return this.terrainAdaptation() != TerrainAdjustment.NONE ? boundingbox.inflatedBy(12) : boundingbox;
   }

   public StructureStart generate(RegistryAccess registryaccess, ChunkGenerator chunkgenerator, BiomeSource biomesource, RandomState randomstate, StructureTemplateManager structuretemplatemanager, long i, ChunkPos chunkpos, int j, LevelHeightAccessor levelheightaccessor, Predicate<Holder<Biome>> predicate) {
      Structure.GenerationContext structure_generationcontext = new Structure.GenerationContext(registryaccess, chunkgenerator, biomesource, randomstate, structuretemplatemanager, i, chunkpos, levelheightaccessor, predicate);
      Optional<Structure.GenerationStub> optional = this.findValidGenerationPoint(structure_generationcontext);
      if (optional.isPresent()) {
         StructurePiecesBuilder structurepiecesbuilder = optional.get().getPiecesBuilder();
         StructureStart structurestart = new StructureStart(this, chunkpos, j, structurepiecesbuilder.build());
         if (structurestart.isValid()) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   protected static Optional<Structure.GenerationStub> onTopOfChunkCenter(Structure.GenerationContext structure_generationcontext, Heightmap.Types heightmap_types, Consumer<StructurePiecesBuilder> consumer) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      int i = chunkpos.getMiddleBlockX();
      int j = chunkpos.getMiddleBlockZ();
      int k = structure_generationcontext.chunkGenerator().getFirstOccupiedHeight(i, j, heightmap_types, structure_generationcontext.heightAccessor(), structure_generationcontext.randomState());
      return Optional.of(new Structure.GenerationStub(new BlockPos(i, k, j), consumer));
   }

   private static boolean isValidBiome(Structure.GenerationStub structure_generationstub, Structure.GenerationContext structure_generationcontext) {
      BlockPos blockpos = structure_generationstub.position();
      return structure_generationcontext.validBiome.test(structure_generationcontext.chunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(blockpos.getX()), QuartPos.fromBlock(blockpos.getY()), QuartPos.fromBlock(blockpos.getZ()), structure_generationcontext.randomState.sampler()));
   }

   public void afterPlace(WorldGenLevel worldgenlevel, StructureManager structuremanager, ChunkGenerator chunkgenerator, RandomSource randomsource, BoundingBox boundingbox, ChunkPos chunkpos, PiecesContainer piecescontainer) {
   }

   private static int[] getCornerHeights(Structure.GenerationContext structure_generationcontext, int i, int j, int k, int l) {
      ChunkGenerator chunkgenerator = structure_generationcontext.chunkGenerator();
      LevelHeightAccessor levelheightaccessor = structure_generationcontext.heightAccessor();
      RandomState randomstate = structure_generationcontext.randomState();
      return new int[]{chunkgenerator.getFirstOccupiedHeight(i, k, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(i, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(i + j, k, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate), chunkgenerator.getFirstOccupiedHeight(i + j, k + l, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor, randomstate)};
   }

   protected static int getLowestY(Structure.GenerationContext structure_generationcontext, int i, int j) {
      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      int k = chunkpos.getMinBlockX();
      int l = chunkpos.getMinBlockZ();
      return getLowestY(structure_generationcontext, k, l, i, j);
   }

   protected static int getLowestY(Structure.GenerationContext structure_generationcontext, int i, int j, int k, int l) {
      int[] aint = getCornerHeights(structure_generationcontext, i, k, j, l);
      return Math.min(Math.min(aint[0], aint[1]), Math.min(aint[2], aint[3]));
   }

   /** @deprecated */
   @Deprecated
   protected BlockPos getLowestYIn5by5BoxOffset7Blocks(Structure.GenerationContext structure_generationcontext, Rotation rotation) {
      int i = 5;
      int j = 5;
      if (rotation == Rotation.CLOCKWISE_90) {
         i = -5;
      } else if (rotation == Rotation.CLOCKWISE_180) {
         i = -5;
         j = -5;
      } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
         j = -5;
      }

      ChunkPos chunkpos = structure_generationcontext.chunkPos();
      int k = chunkpos.getBlockX(7);
      int l = chunkpos.getBlockZ(7);
      return new BlockPos(k, getLowestY(structure_generationcontext, k, l, i, j), l);
   }

   protected abstract Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext structure_generationcontext);

   public Optional<Structure.GenerationStub> findValidGenerationPoint(Structure.GenerationContext structure_generationcontext) {
      return this.findGenerationPoint(structure_generationcontext).filter((structure_generationstub) -> isValidBiome(structure_generationstub, structure_generationcontext));
   }

   public abstract StructureType<?> type();

   public static record GenerationContext(RegistryAccess registryAccess, ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, StructureTemplateManager structureTemplateManager, WorldgenRandom random, long seed, ChunkPos chunkPos, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome) {
      final ChunkGenerator chunkGenerator;
      final RandomState randomState;
      final Predicate<Holder<Biome>> validBiome;

      public GenerationContext(RegistryAccess registryaccess, ChunkGenerator chunkgenerator, BiomeSource biomesource, RandomState randomstate, StructureTemplateManager structuretemplatemanager, long i, ChunkPos chunkpos, LevelHeightAccessor levelheightaccessor, Predicate<Holder<Biome>> predicate) {
         this(registryaccess, chunkgenerator, biomesource, randomstate, structuretemplatemanager, makeRandom(i, chunkpos), i, chunkpos, levelheightaccessor, predicate);
      }

      private static WorldgenRandom makeRandom(long i, ChunkPos chunkpos) {
         WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
         worldgenrandom.setLargeFeatureSeed(i, chunkpos.x, chunkpos.z);
         return worldgenrandom;
      }
   }

   public static record GenerationStub(BlockPos position, Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder> generator) {
      public GenerationStub(BlockPos blockpos, Consumer<StructurePiecesBuilder> consumer) {
         this(blockpos, Either.left(consumer));
      }

      public StructurePiecesBuilder getPiecesBuilder() {
         return this.generator.map((consumer) -> {
            StructurePiecesBuilder structurepiecesbuilder1 = new StructurePiecesBuilder();
            consumer.accept(structurepiecesbuilder1);
            return structurepiecesbuilder1;
         }, (structurepiecesbuilder) -> structurepiecesbuilder);
      }
   }

   public static record StructureSettings(HolderSet<Biome> biomes, Map<MobCategory, StructureSpawnOverride> spawnOverrides, GenerationStep.Decoration step, TerrainAdjustment terrainAdaptation) {
      final HolderSet<Biome> biomes;
      final Map<MobCategory, StructureSpawnOverride> spawnOverrides;
      final GenerationStep.Decoration step;
      final TerrainAdjustment terrainAdaptation;
      public static final MapCodec<Structure.StructureSettings> CODEC = RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(Structure.StructureSettings::biomes), Codec.simpleMap(MobCategory.CODEC, StructureSpawnOverride.CODEC, StringRepresentable.keys(MobCategory.values())).fieldOf("spawn_overrides").forGetter(Structure.StructureSettings::spawnOverrides), GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(Structure.StructureSettings::step), TerrainAdjustment.CODEC.optionalFieldOf("terrain_adaptation", TerrainAdjustment.NONE).forGetter(Structure.StructureSettings::terrainAdaptation)).apply(recordcodecbuilder_instance, Structure.StructureSettings::new));
   }
}
