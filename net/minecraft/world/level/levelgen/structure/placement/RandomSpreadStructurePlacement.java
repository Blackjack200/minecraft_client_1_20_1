package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class RandomSpreadStructurePlacement extends StructurePlacement {
   public static final Codec<RandomSpreadStructurePlacement> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec((recordcodecbuilder_instance) -> placementCodec(recordcodecbuilder_instance).and(recordcodecbuilder_instance.group(Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing), Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation), RandomSpreadType.CODEC.optionalFieldOf("spread_type", RandomSpreadType.LINEAR).forGetter(RandomSpreadStructurePlacement::spreadType))).apply(recordcodecbuilder_instance, RandomSpreadStructurePlacement::new)), RandomSpreadStructurePlacement::validate).codec();
   private final int spacing;
   private final int separation;
   private final RandomSpreadType spreadType;

   private static DataResult<RandomSpreadStructurePlacement> validate(RandomSpreadStructurePlacement randomspreadstructureplacement) {
      return randomspreadstructureplacement.spacing <= randomspreadstructureplacement.separation ? DataResult.error(() -> "Spacing has to be larger than separation") : DataResult.success(randomspreadstructureplacement);
   }

   public RandomSpreadStructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod structureplacement_frequencyreductionmethod, float f, int i, Optional<StructurePlacement.ExclusionZone> optional, int j, int k, RandomSpreadType randomspreadtype) {
      super(vec3i, structureplacement_frequencyreductionmethod, f, i, optional);
      this.spacing = j;
      this.separation = k;
      this.spreadType = randomspreadtype;
   }

   public RandomSpreadStructurePlacement(int i, int j, RandomSpreadType randomspreadtype, int k) {
      this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, k, Optional.empty(), i, j, randomspreadtype);
   }

   public int spacing() {
      return this.spacing;
   }

   public int separation() {
      return this.separation;
   }

   public RandomSpreadType spreadType() {
      return this.spreadType;
   }

   public ChunkPos getPotentialStructureChunk(long i, int j, int k) {
      int l = Math.floorDiv(j, this.spacing);
      int i1 = Math.floorDiv(k, this.spacing);
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(i, l, i1, this.salt());
      int j1 = this.spacing - this.separation;
      int k1 = this.spreadType.evaluate(worldgenrandom, j1);
      int l1 = this.spreadType.evaluate(worldgenrandom, j1);
      return new ChunkPos(l * this.spacing + k1, i1 * this.spacing + l1);
   }

   protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j) {
      ChunkPos chunkpos = this.getPotentialStructureChunk(chunkgeneratorstructurestate.getLevelSeed(), i, j);
      return chunkpos.x == i && chunkpos.z == j;
   }

   public StructurePlacementType<?> type() {
      return StructurePlacementType.RANDOM_SPREAD;
   }
}
