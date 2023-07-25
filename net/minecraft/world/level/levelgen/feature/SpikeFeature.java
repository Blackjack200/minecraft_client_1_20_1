package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.AABB;

public class SpikeFeature extends Feature<SpikeConfiguration> {
   public static final int NUMBER_OF_SPIKES = 10;
   private static final int SPIKE_DISTANCE = 42;
   private static final LoadingCache<Long, List<SpikeFeature.EndSpike>> SPIKE_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.MINUTES).build(new SpikeFeature.SpikeCacheLoader());

   public SpikeFeature(Codec<SpikeConfiguration> codec) {
      super(codec);
   }

   public static List<SpikeFeature.EndSpike> getSpikesForLevel(WorldGenLevel worldgenlevel) {
      RandomSource randomsource = RandomSource.create(worldgenlevel.getSeed());
      long i = randomsource.nextLong() & 65535L;
      return SPIKE_CACHE.getUnchecked(i);
   }

   public boolean place(FeaturePlaceContext<SpikeConfiguration> featureplacecontext) {
      SpikeConfiguration spikeconfiguration = featureplacecontext.config();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      RandomSource randomsource = featureplacecontext.random();
      BlockPos blockpos = featureplacecontext.origin();
      List<SpikeFeature.EndSpike> list = spikeconfiguration.getSpikes();
      if (list.isEmpty()) {
         list = getSpikesForLevel(worldgenlevel);
      }

      for(SpikeFeature.EndSpike spikefeature_endspike : list) {
         if (spikefeature_endspike.isCenterWithinChunk(blockpos)) {
            this.placeSpike(worldgenlevel, randomsource, spikeconfiguration, spikefeature_endspike);
         }
      }

      return true;
   }

   private void placeSpike(ServerLevelAccessor serverlevelaccessor, RandomSource randomsource, SpikeConfiguration spikeconfiguration, SpikeFeature.EndSpike spikefeature_endspike) {
      int i = spikefeature_endspike.getRadius();

      for(BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(spikefeature_endspike.getCenterX() - i, serverlevelaccessor.getMinBuildHeight(), spikefeature_endspike.getCenterZ() - i), new BlockPos(spikefeature_endspike.getCenterX() + i, spikefeature_endspike.getHeight() + 10, spikefeature_endspike.getCenterZ() + i))) {
         if (blockpos.distToLowCornerSqr((double)spikefeature_endspike.getCenterX(), (double)blockpos.getY(), (double)spikefeature_endspike.getCenterZ()) <= (double)(i * i + 1) && blockpos.getY() < spikefeature_endspike.getHeight()) {
            this.setBlock(serverlevelaccessor, blockpos, Blocks.OBSIDIAN.defaultBlockState());
         } else if (blockpos.getY() > 65) {
            this.setBlock(serverlevelaccessor, blockpos, Blocks.AIR.defaultBlockState());
         }
      }

      if (spikefeature_endspike.isGuarded()) {
         int j = -2;
         int k = 2;
         int l = 3;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int i1 = -2; i1 <= 2; ++i1) {
            for(int j1 = -2; j1 <= 2; ++j1) {
               for(int k1 = 0; k1 <= 3; ++k1) {
                  boolean flag = Mth.abs(i1) == 2;
                  boolean flag1 = Mth.abs(j1) == 2;
                  boolean flag2 = k1 == 3;
                  if (flag || flag1 || flag2) {
                     boolean flag3 = i1 == -2 || i1 == 2 || flag2;
                     boolean flag4 = j1 == -2 || j1 == 2 || flag2;
                     BlockState blockstate = Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, Boolean.valueOf(flag3 && j1 != -2)).setValue(IronBarsBlock.SOUTH, Boolean.valueOf(flag3 && j1 != 2)).setValue(IronBarsBlock.WEST, Boolean.valueOf(flag4 && i1 != -2)).setValue(IronBarsBlock.EAST, Boolean.valueOf(flag4 && i1 != 2));
                     this.setBlock(serverlevelaccessor, blockpos_mutableblockpos.set(spikefeature_endspike.getCenterX() + i1, spikefeature_endspike.getHeight() + k1, spikefeature_endspike.getCenterZ() + j1), blockstate);
                  }
               }
            }
         }
      }

      EndCrystal endcrystal = EntityType.END_CRYSTAL.create(serverlevelaccessor.getLevel());
      if (endcrystal != null) {
         endcrystal.setBeamTarget(spikeconfiguration.getCrystalBeamTarget());
         endcrystal.setInvulnerable(spikeconfiguration.isCrystalInvulnerable());
         endcrystal.moveTo((double)spikefeature_endspike.getCenterX() + 0.5D, (double)(spikefeature_endspike.getHeight() + 1), (double)spikefeature_endspike.getCenterZ() + 0.5D, randomsource.nextFloat() * 360.0F, 0.0F);
         serverlevelaccessor.addFreshEntity(endcrystal);
         this.setBlock(serverlevelaccessor, new BlockPos(spikefeature_endspike.getCenterX(), spikefeature_endspike.getHeight(), spikefeature_endspike.getCenterZ()), Blocks.BEDROCK.defaultBlockState());
      }

   }

   public static class EndSpike {
      public static final Codec<SpikeFeature.EndSpike> CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.INT.fieldOf("centerX").orElse(0).forGetter((spikefeature_endspike4) -> spikefeature_endspike4.centerX), Codec.INT.fieldOf("centerZ").orElse(0).forGetter((spikefeature_endspike3) -> spikefeature_endspike3.centerZ), Codec.INT.fieldOf("radius").orElse(0).forGetter((spikefeature_endspike2) -> spikefeature_endspike2.radius), Codec.INT.fieldOf("height").orElse(0).forGetter((spikefeature_endspike1) -> spikefeature_endspike1.height), Codec.BOOL.fieldOf("guarded").orElse(false).forGetter((spikefeature_endspike) -> spikefeature_endspike.guarded)).apply(recordcodecbuilder_instance, SpikeFeature.EndSpike::new));
      private final int centerX;
      private final int centerZ;
      private final int radius;
      private final int height;
      private final boolean guarded;
      private final AABB topBoundingBox;

      public EndSpike(int i, int j, int k, int l, boolean flag) {
         this.centerX = i;
         this.centerZ = j;
         this.radius = k;
         this.height = l;
         this.guarded = flag;
         this.topBoundingBox = new AABB((double)(i - k), (double)DimensionType.MIN_Y, (double)(j - k), (double)(i + k), (double)DimensionType.MAX_Y, (double)(j + k));
      }

      public boolean isCenterWithinChunk(BlockPos blockpos) {
         return SectionPos.blockToSectionCoord(blockpos.getX()) == SectionPos.blockToSectionCoord(this.centerX) && SectionPos.blockToSectionCoord(blockpos.getZ()) == SectionPos.blockToSectionCoord(this.centerZ);
      }

      public int getCenterX() {
         return this.centerX;
      }

      public int getCenterZ() {
         return this.centerZ;
      }

      public int getRadius() {
         return this.radius;
      }

      public int getHeight() {
         return this.height;
      }

      public boolean isGuarded() {
         return this.guarded;
      }

      public AABB getTopBoundingBox() {
         return this.topBoundingBox;
      }
   }

   static class SpikeCacheLoader extends CacheLoader<Long, List<SpikeFeature.EndSpike>> {
      public List<SpikeFeature.EndSpike> load(Long olong) {
         IntArrayList intarraylist = Util.toShuffledList(IntStream.range(0, 10), RandomSource.create(olong));
         List<SpikeFeature.EndSpike> list = Lists.newArrayList();

         for(int i = 0; i < 10; ++i) {
            int j = Mth.floor(42.0D * Math.cos(2.0D * (-Math.PI + (Math.PI / 10D) * (double)i)));
            int k = Mth.floor(42.0D * Math.sin(2.0D * (-Math.PI + (Math.PI / 10D) * (double)i)));
            int l = intarraylist.get(i);
            int i1 = 2 + l / 3;
            int j1 = 76 + l * 3;
            boolean flag = l == 1 || l == 2;
            list.add(new SpikeFeature.EndSpike(j, k, i1, j1, flag));
         }

         return list;
      }
   }
}
