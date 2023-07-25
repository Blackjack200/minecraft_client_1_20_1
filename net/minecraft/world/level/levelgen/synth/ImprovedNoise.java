package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class ImprovedNoise {
   private static final float SHIFT_UP_EPSILON = 1.0E-7F;
   private final byte[] p;
   public final double xo;
   public final double yo;
   public final double zo;

   public ImprovedNoise(RandomSource randomsource) {
      this.xo = randomsource.nextDouble() * 256.0D;
      this.yo = randomsource.nextDouble() * 256.0D;
      this.zo = randomsource.nextDouble() * 256.0D;
      this.p = new byte[256];

      for(int i = 0; i < 256; ++i) {
         this.p[i] = (byte)i;
      }

      for(int j = 0; j < 256; ++j) {
         int k = randomsource.nextInt(256 - j);
         byte b0 = this.p[j];
         this.p[j] = this.p[j + k];
         this.p[j + k] = b0;
      }

   }

   public double noise(double d0, double d1, double d2) {
      return this.noise(d0, d1, d2, 0.0D, 0.0D);
   }

   /** @deprecated */
   @Deprecated
   public double noise(double d0, double d1, double d2, double d3, double d4) {
      double d5 = d0 + this.xo;
      double d6 = d1 + this.yo;
      double d7 = d2 + this.zo;
      int i = Mth.floor(d5);
      int j = Mth.floor(d6);
      int k = Mth.floor(d7);
      double d8 = d5 - (double)i;
      double d9 = d6 - (double)j;
      double d10 = d7 - (double)k;
      double d13;
      if (d3 != 0.0D) {
         double d11;
         if (d4 >= 0.0D && d4 < d9) {
            d11 = d4;
         } else {
            d11 = d9;
         }

         d13 = (double)Mth.floor(d11 / d3 + (double)1.0E-7F) * d3;
      } else {
         d13 = 0.0D;
      }

      return this.sampleAndLerp(i, j, k, d8, d9 - d13, d10, d9);
   }

   public double noiseWithDerivative(double d0, double d1, double d2, double[] adouble) {
      double d3 = d0 + this.xo;
      double d4 = d1 + this.yo;
      double d5 = d2 + this.zo;
      int i = Mth.floor(d3);
      int j = Mth.floor(d4);
      int k = Mth.floor(d5);
      double d6 = d3 - (double)i;
      double d7 = d4 - (double)j;
      double d8 = d5 - (double)k;
      return this.sampleWithDerivative(i, j, k, d6, d7, d8, adouble);
   }

   private static double gradDot(int i, double d0, double d1, double d2) {
      return SimplexNoise.dot(SimplexNoise.GRADIENT[i & 15], d0, d1, d2);
   }

   private int p(int i) {
      return this.p[i & 255] & 255;
   }

   private double sampleAndLerp(int i, int j, int k, double d0, double d1, double d2, double d3) {
      int l = this.p(i);
      int i1 = this.p(i + 1);
      int j1 = this.p(l + j);
      int k1 = this.p(l + j + 1);
      int l1 = this.p(i1 + j);
      int i2 = this.p(i1 + j + 1);
      double d4 = gradDot(this.p(j1 + k), d0, d1, d2);
      double d5 = gradDot(this.p(l1 + k), d0 - 1.0D, d1, d2);
      double d6 = gradDot(this.p(k1 + k), d0, d1 - 1.0D, d2);
      double d7 = gradDot(this.p(i2 + k), d0 - 1.0D, d1 - 1.0D, d2);
      double d8 = gradDot(this.p(j1 + k + 1), d0, d1, d2 - 1.0D);
      double d9 = gradDot(this.p(l1 + k + 1), d0 - 1.0D, d1, d2 - 1.0D);
      double d10 = gradDot(this.p(k1 + k + 1), d0, d1 - 1.0D, d2 - 1.0D);
      double d11 = gradDot(this.p(i2 + k + 1), d0 - 1.0D, d1 - 1.0D, d2 - 1.0D);
      double d12 = Mth.smoothstep(d0);
      double d13 = Mth.smoothstep(d3);
      double d14 = Mth.smoothstep(d2);
      return Mth.lerp3(d12, d13, d14, d4, d5, d6, d7, d8, d9, d10, d11);
   }

   private double sampleWithDerivative(int i, int j, int k, double d0, double d1, double d2, double[] adouble) {
      int l = this.p(i);
      int i1 = this.p(i + 1);
      int j1 = this.p(l + j);
      int k1 = this.p(l + j + 1);
      int l1 = this.p(i1 + j);
      int i2 = this.p(i1 + j + 1);
      int j2 = this.p(j1 + k);
      int k2 = this.p(l1 + k);
      int l2 = this.p(k1 + k);
      int i3 = this.p(i2 + k);
      int j3 = this.p(j1 + k + 1);
      int k3 = this.p(l1 + k + 1);
      int l3 = this.p(k1 + k + 1);
      int i4 = this.p(i2 + k + 1);
      int[] aint = SimplexNoise.GRADIENT[j2 & 15];
      int[] aint1 = SimplexNoise.GRADIENT[k2 & 15];
      int[] aint2 = SimplexNoise.GRADIENT[l2 & 15];
      int[] aint3 = SimplexNoise.GRADIENT[i3 & 15];
      int[] aint4 = SimplexNoise.GRADIENT[j3 & 15];
      int[] aint5 = SimplexNoise.GRADIENT[k3 & 15];
      int[] aint6 = SimplexNoise.GRADIENT[l3 & 15];
      int[] aint7 = SimplexNoise.GRADIENT[i4 & 15];
      double d3 = SimplexNoise.dot(aint, d0, d1, d2);
      double d4 = SimplexNoise.dot(aint1, d0 - 1.0D, d1, d2);
      double d5 = SimplexNoise.dot(aint2, d0, d1 - 1.0D, d2);
      double d6 = SimplexNoise.dot(aint3, d0 - 1.0D, d1 - 1.0D, d2);
      double d7 = SimplexNoise.dot(aint4, d0, d1, d2 - 1.0D);
      double d8 = SimplexNoise.dot(aint5, d0 - 1.0D, d1, d2 - 1.0D);
      double d9 = SimplexNoise.dot(aint6, d0, d1 - 1.0D, d2 - 1.0D);
      double d10 = SimplexNoise.dot(aint7, d0 - 1.0D, d1 - 1.0D, d2 - 1.0D);
      double d11 = Mth.smoothstep(d0);
      double d12 = Mth.smoothstep(d1);
      double d13 = Mth.smoothstep(d2);
      double d14 = Mth.lerp3(d11, d12, d13, (double)aint[0], (double)aint1[0], (double)aint2[0], (double)aint3[0], (double)aint4[0], (double)aint5[0], (double)aint6[0], (double)aint7[0]);
      double d15 = Mth.lerp3(d11, d12, d13, (double)aint[1], (double)aint1[1], (double)aint2[1], (double)aint3[1], (double)aint4[1], (double)aint5[1], (double)aint6[1], (double)aint7[1]);
      double d16 = Mth.lerp3(d11, d12, d13, (double)aint[2], (double)aint1[2], (double)aint2[2], (double)aint3[2], (double)aint4[2], (double)aint5[2], (double)aint6[2], (double)aint7[2]);
      double d17 = Mth.lerp2(d12, d13, d4 - d3, d6 - d5, d8 - d7, d10 - d9);
      double d18 = Mth.lerp2(d13, d11, d5 - d3, d9 - d7, d6 - d4, d10 - d8);
      double d19 = Mth.lerp2(d11, d12, d7 - d3, d8 - d4, d9 - d5, d10 - d6);
      double d20 = Mth.smoothstepDerivative(d0);
      double d21 = Mth.smoothstepDerivative(d1);
      double d22 = Mth.smoothstepDerivative(d2);
      double d23 = d14 + d20 * d17;
      double d24 = d15 + d21 * d18;
      double d25 = d16 + d22 * d19;
      adouble[0] += d23;
      adouble[1] += d24;
      adouble[2] += d25;
      return Mth.lerp3(d11, d12, d13, d3, d4, d5, d6, d7, d8, d9, d10);
   }

   @VisibleForTesting
   public void parityConfigString(StringBuilder stringbuilder) {
      NoiseUtils.parityNoiseOctaveConfigString(stringbuilder, this.xo, this.yo, this.zo, this.p);
   }
}
