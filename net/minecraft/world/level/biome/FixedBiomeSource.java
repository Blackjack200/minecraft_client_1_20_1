package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public class FixedBiomeSource extends BiomeSource implements BiomeManager.NoiseBiomeSource {
   public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, (fixedbiomesource) -> fixedbiomesource.biome).stable().codec();
   private final Holder<Biome> biome;

   public FixedBiomeSource(Holder<Biome> holder) {
      this.biome = holder;
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return Stream.of(this.biome);
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler climate_sampler) {
      return this.biome;
   }

   public Holder<Biome> getNoiseBiome(int i, int j, int k) {
      return this.biome;
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int i, int j, int k, int l, int i1, Predicate<Holder<Biome>> predicate, RandomSource randomsource, boolean flag, Climate.Sampler climate_sampler) {
      if (predicate.test(this.biome)) {
         return flag ? Pair.of(new BlockPos(i, j, k), this.biome) : Pair.of(new BlockPos(i - l + randomsource.nextInt(l * 2 + 1), j, k - l + randomsource.nextInt(l * 2 + 1)), this.biome);
      } else {
         return null;
      }
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos blockpos, int i, int j, int k, Predicate<Holder<Biome>> predicate, Climate.Sampler climate_sampler, LevelReader levelreader) {
      return predicate.test(this.biome) ? Pair.of(blockpos, this.biome) : null;
   }

   public Set<Holder<Biome>> getBiomesWithin(int i, int j, int k, int l, Climate.Sampler climate_sampler) {
      return Sets.newHashSet(Set.of(this.biome));
   }
}
