package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class CanyonWorldCarver extends WorldCarver<CanyonCarverConfiguration> {
   public CanyonWorldCarver(Codec<CanyonCarverConfiguration> codec) {
      super(codec);
   }

   public boolean isStartChunk(CanyonCarverConfiguration canyoncarverconfiguration, RandomSource randomsource) {
      return randomsource.nextFloat() <= canyoncarverconfiguration.probability;
   }

   public boolean carve(CarvingContext carvingcontext, CanyonCarverConfiguration canyoncarverconfiguration, ChunkAccess chunkaccess, Function<BlockPos, Holder<Biome>> function, RandomSource randomsource, Aquifer aquifer, ChunkPos chunkpos, CarvingMask carvingmask) {
      int i = (this.getRange() * 2 - 1) * 16;
      double d0 = (double)chunkpos.getBlockX(randomsource.nextInt(16));
      int j = canyoncarverconfiguration.y.sample(randomsource, carvingcontext);
      double d1 = (double)chunkpos.getBlockZ(randomsource.nextInt(16));
      float f = randomsource.nextFloat() * ((float)Math.PI * 2F);
      float f1 = canyoncarverconfiguration.verticalRotation.sample(randomsource);
      double d2 = (double)canyoncarverconfiguration.yScale.sample(randomsource);
      float f2 = canyoncarverconfiguration.shape.thickness.sample(randomsource);
      int k = (int)((float)i * canyoncarverconfiguration.shape.distanceFactor.sample(randomsource));
      int l = 0;
      this.doCarve(carvingcontext, canyoncarverconfiguration, chunkaccess, function, randomsource.nextLong(), aquifer, d0, (double)j, d1, f2, f, f1, 0, k, d2, carvingmask);
      return true;
   }

   private void doCarve(CarvingContext carvingcontext, CanyonCarverConfiguration canyoncarverconfiguration, ChunkAccess chunkaccess, Function<BlockPos, Holder<Biome>> function, long i, Aquifer aquifer, double d0, double d1, double d2, float f, float f1, float f2, int j, int k, double d3, CarvingMask carvingmask) {
      RandomSource randomsource = RandomSource.create(i);
      float[] afloat = this.initWidthFactors(carvingcontext, canyoncarverconfiguration, randomsource);
      float f3 = 0.0F;
      float f4 = 0.0F;

      for(int l = j; l < k; ++l) {
         double d4 = 1.5D + (double)(Mth.sin((float)l * (float)Math.PI / (float)k) * f);
         double d5 = d4 * d3;
         d4 *= (double)canyoncarverconfiguration.shape.horizontalRadiusFactor.sample(randomsource);
         d5 = this.updateVerticalRadius(canyoncarverconfiguration, randomsource, d5, (float)k, (float)l);
         float f5 = Mth.cos(f2);
         float f6 = Mth.sin(f2);
         d0 += (double)(Mth.cos(f1) * f5);
         d1 += (double)f6;
         d2 += (double)(Mth.sin(f1) * f5);
         f2 *= 0.7F;
         f2 += f4 * 0.05F;
         f1 += f3 * 0.05F;
         f4 *= 0.8F;
         f3 *= 0.5F;
         f4 += (randomsource.nextFloat() - randomsource.nextFloat()) * randomsource.nextFloat() * 2.0F;
         f3 += (randomsource.nextFloat() - randomsource.nextFloat()) * randomsource.nextFloat() * 4.0F;
         if (randomsource.nextInt(4) != 0) {
            if (!canReach(chunkaccess.getPos(), d0, d2, l, k, f)) {
               return;
            }

            this.carveEllipsoid(carvingcontext, canyoncarverconfiguration, chunkaccess, function, aquifer, d0, d1, d2, d4, d5, carvingmask, (carvingcontext1, d6, d7, d8, i1) -> this.shouldSkip(carvingcontext1, afloat, d6, d7, d8, i1));
         }
      }

   }

   private float[] initWidthFactors(CarvingContext carvingcontext, CanyonCarverConfiguration canyoncarverconfiguration, RandomSource randomsource) {
      int i = carvingcontext.getGenDepth();
      float[] afloat = new float[i];
      float f = 1.0F;

      for(int j = 0; j < i; ++j) {
         if (j == 0 || randomsource.nextInt(canyoncarverconfiguration.shape.widthSmoothness) == 0) {
            f = 1.0F + randomsource.nextFloat() * randomsource.nextFloat();
         }

         afloat[j] = f * f;
      }

      return afloat;
   }

   private double updateVerticalRadius(CanyonCarverConfiguration canyoncarverconfiguration, RandomSource randomsource, double d0, float f, float f1) {
      float f2 = 1.0F - Mth.abs(0.5F - f1 / f) * 2.0F;
      float f3 = canyoncarverconfiguration.shape.verticalRadiusDefaultFactor + canyoncarverconfiguration.shape.verticalRadiusCenterFactor * f2;
      return (double)f3 * d0 * (double)Mth.randomBetween(randomsource, 0.75F, 1.0F);
   }

   private boolean shouldSkip(CarvingContext carvingcontext, float[] afloat, double d0, double d1, double d2, int i) {
      int j = i - carvingcontext.getMinGenY();
      return (d0 * d0 + d2 * d2) * (double)afloat[j - 1] + d1 * d1 / 6.0D >= 1.0D;
   }
}
