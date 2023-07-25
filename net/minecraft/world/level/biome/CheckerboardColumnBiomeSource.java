package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

public class CheckerboardColumnBiomeSource extends BiomeSource {
   public static final Codec<CheckerboardColumnBiomeSource> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter((checkerboardcolumnbiomesource1) -> checkerboardcolumnbiomesource1.allowedBiomes), Codec.intRange(0, 62).fieldOf("scale").orElse(2).forGetter((checkerboardcolumnbiomesource) -> checkerboardcolumnbiomesource.size)).apply(recordcodecbuilder_instance, CheckerboardColumnBiomeSource::new));
   private final HolderSet<Biome> allowedBiomes;
   private final int bitShift;
   private final int size;

   public CheckerboardColumnBiomeSource(HolderSet<Biome> holderset, int i) {
      this.allowedBiomes = holderset;
      this.bitShift = i + 2;
      this.size = i;
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return this.allowedBiomes.stream();
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler climate_sampler) {
      return this.allowedBiomes.get(Math.floorMod((i >> this.bitShift) + (k >> this.bitShift), this.allowedBiomes.size()));
   }
}
