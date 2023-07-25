package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.DensityFunction;

public class TheEndBiomeSource extends BiomeSource {
   public static final Codec<TheEndBiomeSource> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(RegistryOps.retrieveElement(Biomes.THE_END), RegistryOps.retrieveElement(Biomes.END_HIGHLANDS), RegistryOps.retrieveElement(Biomes.END_MIDLANDS), RegistryOps.retrieveElement(Biomes.SMALL_END_ISLANDS), RegistryOps.retrieveElement(Biomes.END_BARRENS)).apply(recordcodecbuilder_instance, recordcodecbuilder_instance.stable(TheEndBiomeSource::new)));
   private final Holder<Biome> end;
   private final Holder<Biome> highlands;
   private final Holder<Biome> midlands;
   private final Holder<Biome> islands;
   private final Holder<Biome> barrens;

   public static TheEndBiomeSource create(HolderGetter<Biome> holdergetter) {
      return new TheEndBiomeSource(holdergetter.getOrThrow(Biomes.THE_END), holdergetter.getOrThrow(Biomes.END_HIGHLANDS), holdergetter.getOrThrow(Biomes.END_MIDLANDS), holdergetter.getOrThrow(Biomes.SMALL_END_ISLANDS), holdergetter.getOrThrow(Biomes.END_BARRENS));
   }

   private TheEndBiomeSource(Holder<Biome> holder, Holder<Biome> holder1, Holder<Biome> holder2, Holder<Biome> holder3, Holder<Biome> holder4) {
      this.end = holder;
      this.highlands = holder1;
      this.midlands = holder2;
      this.islands = holder3;
      this.barrens = holder4;
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return Stream.of(this.end, this.highlands, this.midlands, this.islands, this.barrens);
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler climate_sampler) {
      int l = QuartPos.toBlock(i);
      int i1 = QuartPos.toBlock(j);
      int j1 = QuartPos.toBlock(k);
      int k1 = SectionPos.blockToSectionCoord(l);
      int l1 = SectionPos.blockToSectionCoord(j1);
      if ((long)k1 * (long)k1 + (long)l1 * (long)l1 <= 4096L) {
         return this.end;
      } else {
         int i2 = (SectionPos.blockToSectionCoord(l) * 2 + 1) * 8;
         int j2 = (SectionPos.blockToSectionCoord(j1) * 2 + 1) * 8;
         double d0 = climate_sampler.erosion().compute(new DensityFunction.SinglePointContext(i2, i1, j2));
         if (d0 > 0.25D) {
            return this.highlands;
         } else if (d0 >= -0.0625D) {
            return this.midlands;
         } else {
            return d0 < -0.21875D ? this.islands : this.barrens;
         }
      }
   }
}
