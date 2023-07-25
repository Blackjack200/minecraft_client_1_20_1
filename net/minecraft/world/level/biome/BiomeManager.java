package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

public class BiomeManager {
   public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
   private static final int ZOOM_BITS = 2;
   private static final int ZOOM = 4;
   private static final int ZOOM_MASK = 3;
   private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
   private final long biomeZoomSeed;

   public BiomeManager(BiomeManager.NoiseBiomeSource biomemanager_noisebiomesource, long i) {
      this.noiseBiomeSource = biomemanager_noisebiomesource;
      this.biomeZoomSeed = i;
   }

   public static long obfuscateSeed(long i) {
      return Hashing.sha256().hashLong(i).asLong();
   }

   public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource biomemanager_noisebiomesource) {
      return new BiomeManager(biomemanager_noisebiomesource, this.biomeZoomSeed);
   }

   public Holder<Biome> getBiome(BlockPos blockpos) {
      int i = blockpos.getX() - 2;
      int j = blockpos.getY() - 2;
      int k = blockpos.getZ() - 2;
      int l = i >> 2;
      int i1 = j >> 2;
      int j1 = k >> 2;
      double d0 = (double)(i & 3) / 4.0D;
      double d1 = (double)(j & 3) / 4.0D;
      double d2 = (double)(k & 3) / 4.0D;
      int k1 = 0;
      double d3 = Double.POSITIVE_INFINITY;

      for(int l1 = 0; l1 < 8; ++l1) {
         boolean flag = (l1 & 4) == 0;
         boolean flag1 = (l1 & 2) == 0;
         boolean flag2 = (l1 & 1) == 0;
         int i2 = flag ? l : l + 1;
         int j2 = flag1 ? i1 : i1 + 1;
         int k2 = flag2 ? j1 : j1 + 1;
         double d4 = flag ? d0 : d0 - 1.0D;
         double d5 = flag1 ? d1 : d1 - 1.0D;
         double d6 = flag2 ? d2 : d2 - 1.0D;
         double d7 = getFiddledDistance(this.biomeZoomSeed, i2, j2, k2, d4, d5, d6);
         if (d3 > d7) {
            k1 = l1;
            d3 = d7;
         }
      }

      int l2 = (k1 & 4) == 0 ? l : l + 1;
      int i3 = (k1 & 2) == 0 ? i1 : i1 + 1;
      int j3 = (k1 & 1) == 0 ? j1 : j1 + 1;
      return this.noiseBiomeSource.getNoiseBiome(l2, i3, j3);
   }

   public Holder<Biome> getNoiseBiomeAtPosition(double d0, double d1, double d2) {
      int i = QuartPos.fromBlock(Mth.floor(d0));
      int j = QuartPos.fromBlock(Mth.floor(d1));
      int k = QuartPos.fromBlock(Mth.floor(d2));
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Holder<Biome> getNoiseBiomeAtPosition(BlockPos blockpos) {
      int i = QuartPos.fromBlock(blockpos.getX());
      int j = QuartPos.fromBlock(blockpos.getY());
      int k = QuartPos.fromBlock(blockpos.getZ());
      return this.getNoiseBiomeAtQuart(i, j, k);
   }

   public Holder<Biome> getNoiseBiomeAtQuart(int i, int j, int k) {
      return this.noiseBiomeSource.getNoiseBiome(i, j, k);
   }

   private static double getFiddledDistance(long i, int j, int k, int l, double d0, double d1, double d2) {
      long i1 = LinearCongruentialGenerator.next(i, (long)j);
      i1 = LinearCongruentialGenerator.next(i1, (long)k);
      i1 = LinearCongruentialGenerator.next(i1, (long)l);
      i1 = LinearCongruentialGenerator.next(i1, (long)j);
      i1 = LinearCongruentialGenerator.next(i1, (long)k);
      i1 = LinearCongruentialGenerator.next(i1, (long)l);
      double d3 = getFiddle(i1);
      i1 = LinearCongruentialGenerator.next(i1, i);
      double d4 = getFiddle(i1);
      i1 = LinearCongruentialGenerator.next(i1, i);
      double d5 = getFiddle(i1);
      return Mth.square(d2 + d5) + Mth.square(d1 + d4) + Mth.square(d0 + d3);
   }

   private static double getFiddle(long i) {
      double d0 = (double)Math.floorMod(i >> 24, 1024) / 1024.0D;
      return (d0 - 0.5D) * 0.9D;
   }

   public interface NoiseBiomeSource {
      Holder<Biome> getNoiseBiome(int i, int j, int k);
   }
}
