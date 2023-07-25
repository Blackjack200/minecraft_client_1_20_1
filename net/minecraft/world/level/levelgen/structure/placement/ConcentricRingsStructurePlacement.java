package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

public class ConcentricRingsStructurePlacement extends StructurePlacement {
   public static final Codec<ConcentricRingsStructurePlacement> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> codec(recordcodecbuilder_instance).apply(recordcodecbuilder_instance, ConcentricRingsStructurePlacement::new));
   private final int distance;
   private final int spread;
   private final int count;
   private final HolderSet<Biome> preferredBiomes;

   private static Products.P9<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>, Integer, Integer, Integer, HolderSet<Biome>> codec(RecordCodecBuilder.Instance<ConcentricRingsStructurePlacement> recordcodecbuilder_instance) {
      Products.P5<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> products_p5 = placementCodec(recordcodecbuilder_instance);
      Products.P4<RecordCodecBuilder.Mu<ConcentricRingsStructurePlacement>, Integer, Integer, Integer, HolderSet<Biome>> products_p4 = recordcodecbuilder_instance.group(Codec.intRange(0, 1023).fieldOf("distance").forGetter(ConcentricRingsStructurePlacement::distance), Codec.intRange(0, 1023).fieldOf("spread").forGetter(ConcentricRingsStructurePlacement::spread), Codec.intRange(1, 4095).fieldOf("count").forGetter(ConcentricRingsStructurePlacement::count), RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(ConcentricRingsStructurePlacement::preferredBiomes));
      return new Products.P9<>(products_p5.t1(), products_p5.t2(), products_p5.t3(), products_p5.t4(), products_p5.t5(), products_p4.t1(), products_p4.t2(), products_p4.t3(), products_p4.t4());
   }

   public ConcentricRingsStructurePlacement(Vec3i vec3i, StructurePlacement.FrequencyReductionMethod structureplacement_frequencyreductionmethod, float f, int i, Optional<StructurePlacement.ExclusionZone> optional, int j, int k, int l, HolderSet<Biome> holderset) {
      super(vec3i, structureplacement_frequencyreductionmethod, f, i, optional);
      this.distance = j;
      this.spread = k;
      this.count = l;
      this.preferredBiomes = holderset;
   }

   public ConcentricRingsStructurePlacement(int i, int j, int k, HolderSet<Biome> holderset) {
      this(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty(), i, j, k, holderset);
   }

   public int distance() {
      return this.distance;
   }

   public int spread() {
      return this.spread;
   }

   public int count() {
      return this.count;
   }

   public HolderSet<Biome> preferredBiomes() {
      return this.preferredBiomes;
   }

   protected boolean isPlacementChunk(ChunkGeneratorStructureState chunkgeneratorstructurestate, int i, int j) {
      List<ChunkPos> list = chunkgeneratorstructurestate.getRingPositionsFor(this);
      return list == null ? false : list.contains(new ChunkPos(i, j));
   }

   public StructurePlacementType<?> type() {
      return StructurePlacementType.CONCENTRIC_RINGS;
   }
}
