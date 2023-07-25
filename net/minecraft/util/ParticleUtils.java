package net.minecraft.util;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
   public static void spawnParticlesOnBlockFaces(Level level, BlockPos blockpos, ParticleOptions particleoptions, IntProvider intprovider) {
      for(Direction direction : Direction.values()) {
         spawnParticlesOnBlockFace(level, blockpos, particleoptions, intprovider, direction, () -> getRandomSpeedRanges(level.random), 0.55D);
      }

   }

   public static void spawnParticlesOnBlockFace(Level level, BlockPos blockpos, ParticleOptions particleoptions, IntProvider intprovider, Direction direction, Supplier<Vec3> supplier, double d0) {
      int i = intprovider.sample(level.random);

      for(int j = 0; j < i; ++j) {
         spawnParticleOnFace(level, blockpos, direction, particleoptions, supplier.get(), d0);
      }

   }

   private static Vec3 getRandomSpeedRanges(RandomSource randomsource) {
      return new Vec3(Mth.nextDouble(randomsource, -0.5D, 0.5D), Mth.nextDouble(randomsource, -0.5D, 0.5D), Mth.nextDouble(randomsource, -0.5D, 0.5D));
   }

   public static void spawnParticlesAlongAxis(Direction.Axis direction_axis, Level level, BlockPos blockpos, double d0, ParticleOptions particleoptions, UniformInt uniformint) {
      Vec3 vec3 = Vec3.atCenterOf(blockpos);
      boolean flag = direction_axis == Direction.Axis.X;
      boolean flag1 = direction_axis == Direction.Axis.Y;
      boolean flag2 = direction_axis == Direction.Axis.Z;
      int i = uniformint.sample(level.random);

      for(int j = 0; j < i; ++j) {
         double d1 = vec3.x + Mth.nextDouble(level.random, -1.0D, 1.0D) * (flag ? 0.5D : d0);
         double d2 = vec3.y + Mth.nextDouble(level.random, -1.0D, 1.0D) * (flag1 ? 0.5D : d0);
         double d3 = vec3.z + Mth.nextDouble(level.random, -1.0D, 1.0D) * (flag2 ? 0.5D : d0);
         double d4 = flag ? Mth.nextDouble(level.random, -1.0D, 1.0D) : 0.0D;
         double d5 = flag1 ? Mth.nextDouble(level.random, -1.0D, 1.0D) : 0.0D;
         double d6 = flag2 ? Mth.nextDouble(level.random, -1.0D, 1.0D) : 0.0D;
         level.addParticle(particleoptions, d1, d2, d3, d4, d5, d6);
      }

   }

   public static void spawnParticleOnFace(Level level, BlockPos blockpos, Direction direction, ParticleOptions particleoptions, Vec3 vec3, double d0) {
      Vec3 vec31 = Vec3.atCenterOf(blockpos);
      int i = direction.getStepX();
      int j = direction.getStepY();
      int k = direction.getStepZ();
      double d1 = vec31.x + (i == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : (double)i * d0);
      double d2 = vec31.y + (j == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : (double)j * d0);
      double d3 = vec31.z + (k == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : (double)k * d0);
      double d4 = i == 0 ? vec3.x() : 0.0D;
      double d5 = j == 0 ? vec3.y() : 0.0D;
      double d6 = k == 0 ? vec3.z() : 0.0D;
      level.addParticle(particleoptions, d1, d2, d3, d4, d5, d6);
   }

   public static void spawnParticleBelow(Level level, BlockPos blockpos, RandomSource randomsource, ParticleOptions particleoptions) {
      double d0 = (double)blockpos.getX() + randomsource.nextDouble();
      double d1 = (double)blockpos.getY() - 0.05D;
      double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
      level.addParticle(particleoptions, d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }
}
