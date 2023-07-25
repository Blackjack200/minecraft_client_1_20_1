package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public abstract class StructurePlacement {
   public static final Codec<StructurePlacement> CODEC = BuiltInRegistries.STRUCTURE_PLACEMENT.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);
   private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
   private final Vec3i locateOffset;
   private final StructurePlacement.FrequencyReductionMethod frequencyReductionMethod;
   private final float frequency;
   private final int salt;
   private final Optional<StructurePlacement.ExclusionZone> exclusionZone;

   protected static <S extends StructurePlacement> Products.P5<RecordCodecBuilder.Mu<S>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> placementCodec(RecordCodecBuilder.Instance<S> recordcodecbuilder_instance) {
      return recordcodecbuilder_instance.group(Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(StructurePlacement::locateOffset), StructurePlacement.FrequencyReductionMethod.CODEC.optionalFieldOf("frequency_reduction_method", StructurePlacement.FrequencyReductionMethod.DEFAULT).forGetter(StructurePlacement::frequencyReductionMethod), Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(StructurePlacement::frequency), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StructurePlacement::salt), StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::exclusionZone));
   }

   protected StructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod structureplacement_frequencyreductionmethod, float f, int i, Optional<StructurePlacement.ExclusionZone> optional) {
      this.locateOffset = vec3i;
      this.frequencyReductionMethod = structureplacement_frequencyreductionmethod;
      this.frequency = f;
      this.salt = i;
      this.exclusionZone = optional;
   }

   protected Vec3i locateOffset() {
      return this.locateOffset;
   }

   protected StructurePlacement.FrequencyReductionMethod frequencyReductionMethod() {
      return this.frequencyReductionMethod;
   }

   protected float frequency() {
      return this.frequency;
   }

   protected int salt() {
      return this.salt;
   }

   protected Optional<StructurePlacement.ExclusionZone> exclusionZone() {
      return this.exclusionZone;
   }

   public boolean isStructureChunk(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j) {
      if (!this.isPlacementChunk(chunkgeneratorstructurestate, i, j)) {
         return false;
      } else if (this.frequency < 1.0F && !this.frequencyReductionMethod.shouldGenerate(chunkgeneratorstructurestate.getLevelSeed(), this.salt, i, j, this.frequency)) {
         return false;
      } else {
         return !this.exclusionZone.isPresent() || !this.exclusionZone.get().isPlacementForbidden(chunkgeneratorstructurestate, i, j);
      }
   }

   protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j);

   public BlockPos getLocatePos(ChunkPos chunkpos) {
      return (new BlockPos(chunkpos.getMinBlockX(), 0, chunkpos.getMinBlockZ())).offset(this.locateOffset());
   }

   public abstract StructurePlacementType<?> type();

   private static boolean probabilityReducer(long i, int j, int k, int l, float f) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(i, j, k, l);
      return worldgenrandom.nextFloat() < f;
   }

   private static boolean legacyProbabilityReducerWithDouble(long i, int j, int k, int l, float f) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureSeed(i, k, l);
      return worldgenrandom.nextDouble() < (double)f;
   }

   private static boolean legacyArbitrarySaltProbabilityReducer(long i, int j, int k, int l, float f) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(i, k, l, 10387320);
      return worldgenrandom.nextFloat() < f;
   }

   private static boolean legacyPillagerOutpostReducer(long i, int j, int k, int l, float f) {
      int i1 = k >> 4;
      int j1 = l >> 4;
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setSeed((long)(i1 ^ j1 << 4) ^ i);
      worldgenrandom.nextInt();
      return worldgenrandom.nextInt((int)(1.0F / f)) == 0;
   }

   /** @deprecated */
   @Deprecated
   public static record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
      public static final Codec<StructurePlacement.ExclusionZone> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryFileCodec.create(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false).fieldOf("other_set").forGetter(StructurePlacement.ExclusionZone::otherSet), Codec.intRange(1, 16).fieldOf("chunk_count").forGetter(StructurePlacement.ExclusionZone::chunkCount)).apply(recordcodecbuilder_instance, StructurePlacement.ExclusionZone::new));

      boolean isPlacementForbidden(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j) {
         return chunkgeneratorstructurestate.hasStructureChunkInRange(this.otherSet, i, j, this.chunkCount);
      }
   }

   @FunctionalInterface
   public interface FrequencyReducer {
      boolean shouldGenerate(long i, int j, int k, int l, float f);
   }

   public static enum FrequencyReductionMethod implements StringRepresentable {
      DEFAULT("default", StructurePlacement::probabilityReducer),
      LEGACY_TYPE_1("legacy_type_1", StructurePlacement::legacyPillagerOutpostReducer),
      LEGACY_TYPE_2("legacy_type_2", StructurePlacement::legacyArbitrarySaltProbabilityReducer),
      LEGACY_TYPE_3("legacy_type_3", StructurePlacement::legacyProbabilityReducerWithDouble);

      public static final Codec<StructurePlacement.FrequencyReductionMethod> CODEC = StringRepresentable.fromEnum(StructurePlacement.FrequencyReductionMethod::values);
      private final String name;
      private final StructurePlacement.FrequencyReducer reducer;

      private FrequencyReductionMethod(String s, StructurePlacement.FrequencyReducer structureplacement_frequencyreducer) {
         this.name = s;
         this.reducer = structureplacement_frequencyreducer;
      }

      public boolean shouldGenerate(long i, int j, int k, int l, float f) {
         return this.reducer.shouldGenerate(i, j, k, l, f);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
