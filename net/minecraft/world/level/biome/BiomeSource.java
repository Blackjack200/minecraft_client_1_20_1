package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public abstract class BiomeSource implements BiomeResolver {
   public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
   private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(() -> this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet()));

   protected BiomeSource() {
   }

   protected abstract Codec<? extends BiomeSource> codec();

   protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

   public Set<Holder<Biome>> possibleBiomes() {
      return this.possibleBiomes.get();
   }

   public Set<Holder<Biome>> getBiomesWithin(int i, int j, int k, int l, Climate.Sampler climate_sampler) {
      int i1 = QuartPos.fromBlock(i - l);
      int j1 = QuartPos.fromBlock(j - l);
      int k1 = QuartPos.fromBlock(k - l);
      int l1 = QuartPos.fromBlock(i + l);
      int i2 = QuartPos.fromBlock(j + l);
      int j2 = QuartPos.fromBlock(k + l);
      int k2 = l1 - i1 + 1;
      int l2 = i2 - j1 + 1;
      int i3 = j2 - k1 + 1;
      Set<Holder<Biome>> set = Sets.newHashSet();

      for(int j3 = 0; j3 < i3; ++j3) {
         for(int k3 = 0; k3 < k2; ++k3) {
            for(int l3 = 0; l3 < l2; ++l3) {
               int i4 = i1 + k3;
               int j4 = j1 + l3;
               int k4 = k1 + j3;
               set.add(this.getNoiseBiome(i4, j4, k4, climate_sampler));
            }
         }
      }

      return set;
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int i, int j, int k, int l, Predicate<Holder<Biome>> predicate, RandomSource randomsource, Climate.Sampler climate_sampler) {
      return this.findBiomeHorizontal(i, j, k, l, 1, predicate, randomsource, false, climate_sampler);
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos blockpos, int i, int j, int k, Predicate<Holder<Biome>> predicate, Climate.Sampler climate_sampler, LevelReader levelreader) {
      Set<Holder<Biome>> set = this.possibleBiomes().stream().filter(predicate).collect(Collectors.toUnmodifiableSet());
      if (set.isEmpty()) {
         return null;
      } else {
         int l = Math.floorDiv(i, j);
         int[] aint = Mth.outFromOrigin(blockpos.getY(), levelreader.getMinBuildHeight() + 1, levelreader.getMaxBuildHeight(), k).toArray();

         for(BlockPos.MutableBlockPos blockpos_mutableblockpos : BlockPos.spiralAround(BlockPos.ZERO, l, Direction.EAST, Direction.SOUTH)) {
            int i1 = blockpos.getX() + blockpos_mutableblockpos.getX() * j;
            int j1 = blockpos.getZ() + blockpos_mutableblockpos.getZ() * j;
            int k1 = QuartPos.fromBlock(i1);
            int l1 = QuartPos.fromBlock(j1);

            for(int i2 : aint) {
               int j2 = QuartPos.fromBlock(i2);
               Holder<Biome> holder = this.getNoiseBiome(k1, j2, l1, climate_sampler);
               if (set.contains(holder)) {
                  return Pair.of(new BlockPos(i1, i2, j1), holder);
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int i, int j, int k, int l, int i1, Predicate<Holder<Biome>> predicate, RandomSource randomsource, boolean flag, Climate.Sampler climate_sampler) {
      int j1 = QuartPos.fromBlock(i);
      int k1 = QuartPos.fromBlock(k);
      int l1 = QuartPos.fromBlock(l);
      int i2 = QuartPos.fromBlock(j);
      Pair<BlockPos, Holder<Biome>> pair = null;
      int j2 = 0;
      int k2 = flag ? 0 : l1;

      for(int l2 = k2; l2 <= l1; l2 += i1) {
         for(int i3 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -l2; i3 <= l2; i3 += i1) {
            boolean flag1 = Math.abs(i3) == l2;

            for(int j3 = -l2; j3 <= l2; j3 += i1) {
               if (flag) {
                  boolean flag2 = Math.abs(j3) == l2;
                  if (!flag2 && !flag1) {
                     continue;
                  }
               }

               int k3 = j1 + j3;
               int l3 = k1 + i3;
               Holder<Biome> holder = this.getNoiseBiome(k3, i2, l3, climate_sampler);
               if (predicate.test(holder)) {
                  if (pair == null || randomsource.nextInt(j2 + 1) == 0) {
                     BlockPos blockpos = new BlockPos(QuartPos.toBlock(k3), j, QuartPos.toBlock(l3));
                     if (flag) {
                        return Pair.of(blockpos, holder);
                     }

                     pair = Pair.of(blockpos, holder);
                  }

                  ++j2;
               }
            }
         }
      }

      return pair;
   }

   public abstract Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler climate_sampler);

   public void addDebugInfo(List<String> list, BlockPos blockpos, Climate.Sampler climate_sampler) {
   }
}
