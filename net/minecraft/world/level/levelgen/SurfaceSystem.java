package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceSystem {
   private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
   private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
   private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
   private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
   private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
   private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
   private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
   private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
   private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
   private final BlockState defaultBlock;
   private final int seaLevel;
   private final BlockState[] clayBands;
   private final NormalNoise clayBandsOffsetNoise;
   private final NormalNoise badlandsPillarNoise;
   private final NormalNoise badlandsPillarRoofNoise;
   private final NormalNoise badlandsSurfaceNoise;
   private final NormalNoise icebergPillarNoise;
   private final NormalNoise icebergPillarRoofNoise;
   private final NormalNoise icebergSurfaceNoise;
   private final PositionalRandomFactory noiseRandom;
   private final NormalNoise surfaceNoise;
   private final NormalNoise surfaceSecondaryNoise;

   public SurfaceSystem(RandomState randomstate, BlockState blockstate, int i, PositionalRandomFactory positionalrandomfactory) {
      this.defaultBlock = blockstate;
      this.seaLevel = i;
      this.noiseRandom = positionalrandomfactory;
      this.clayBandsOffsetNoise = randomstate.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
      this.clayBands = generateBands(positionalrandomfactory.fromHashOf(new ResourceLocation("clay_bands")));
      this.surfaceNoise = randomstate.getOrCreateNoise(Noises.SURFACE);
      this.surfaceSecondaryNoise = randomstate.getOrCreateNoise(Noises.SURFACE_SECONDARY);
      this.badlandsPillarNoise = randomstate.getOrCreateNoise(Noises.BADLANDS_PILLAR);
      this.badlandsPillarRoofNoise = randomstate.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
      this.badlandsSurfaceNoise = randomstate.getOrCreateNoise(Noises.BADLANDS_SURFACE);
      this.icebergPillarNoise = randomstate.getOrCreateNoise(Noises.ICEBERG_PILLAR);
      this.icebergPillarRoofNoise = randomstate.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
      this.icebergSurfaceNoise = randomstate.getOrCreateNoise(Noises.ICEBERG_SURFACE);
   }

   public void buildSurface(RandomState randomstate, BiomeManager biomemanager, Registry<Biome> registry, boolean flag, WorldGenerationContext worldgenerationcontext, final ChunkAccess chunkaccess, NoiseChunk noisechunk, SurfaceRules.RuleSource surfacerules_rulesource) {
      final BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      final ChunkPos chunkpos = chunkaccess.getPos();
      int i = chunkpos.getMinBlockX();
      int j = chunkpos.getMinBlockZ();
      BlockColumn blockcolumn = new BlockColumn() {
         public BlockState getBlock(int i) {
            return chunkaccess.getBlockState(blockpos_mutableblockpos.setY(i));
         }

         public void setBlock(int i, BlockState blockstate) {
            LevelHeightAccessor levelheightaccessor = chunkaccess.getHeightAccessorForGeneration();
            if (i >= levelheightaccessor.getMinBuildHeight() && i < levelheightaccessor.getMaxBuildHeight()) {
               chunkaccess.setBlockState(blockpos_mutableblockpos.setY(i), blockstate, false);
               if (!blockstate.getFluidState().isEmpty()) {
                  chunkaccess.markPosForPostprocessing(blockpos_mutableblockpos);
               }
            }

         }

         public String toString() {
            return "ChunkBlockColumn " + chunkpos;
         }
      };
      SurfaceRules.Context surfacerules_context = new SurfaceRules.Context(this, randomstate, chunkaccess, noisechunk, biomemanager::getBiome, registry, worldgenerationcontext);
      SurfaceRules.SurfaceRule surfacerules_surfacerule = surfacerules_rulesource.apply(surfacerules_context);
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = new BlockPos.MutableBlockPos();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            int i1 = i + k;
            int j1 = j + l;
            int k1 = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
            blockpos_mutableblockpos.setX(i1).setZ(j1);
            Holder<Biome> holder = biomemanager.getBiome(blockpos_mutableblockpos1.set(i1, flag ? 0 : k1, j1));
            if (holder.is(Biomes.ERODED_BADLANDS)) {
               this.erodedBadlandsExtension(blockcolumn, i1, j1, k1, chunkaccess);
            }

            int l1 = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
            surfacerules_context.updateXZ(i1, j1);
            int i2 = 0;
            int j2 = Integer.MIN_VALUE;
            int k2 = Integer.MAX_VALUE;
            int l2 = chunkaccess.getMinBuildHeight();

            for(int i3 = l1; i3 >= l2; --i3) {
               BlockState blockstate = blockcolumn.getBlock(i3);
               if (blockstate.isAir()) {
                  i2 = 0;
                  j2 = Integer.MIN_VALUE;
               } else if (!blockstate.getFluidState().isEmpty()) {
                  if (j2 == Integer.MIN_VALUE) {
                     j2 = i3 + 1;
                  }
               } else {
                  if (k2 >= i3) {
                     k2 = DimensionType.WAY_BELOW_MIN_Y;

                     for(int j3 = i3 - 1; j3 >= l2 - 1; --j3) {
                        BlockState blockstate1 = blockcolumn.getBlock(j3);
                        if (!this.isStone(blockstate1)) {
                           k2 = j3 + 1;
                           break;
                        }
                     }
                  }

                  ++i2;
                  int k3 = i3 - k2 + 1;
                  surfacerules_context.updateY(i2, k3, j2, i1, i3, j1);
                  if (blockstate == this.defaultBlock) {
                     BlockState blockstate2 = surfacerules_surfacerule.tryApply(i1, i3, j1);
                     if (blockstate2 != null) {
                        blockcolumn.setBlock(i3, blockstate2);
                     }
                  }
               }
            }

            if (holder.is(Biomes.FROZEN_OCEAN) || holder.is(Biomes.DEEP_FROZEN_OCEAN)) {
               this.frozenOceanExtension(surfacerules_context.getMinSurfaceLevel(), holder.value(), blockcolumn, blockpos_mutableblockpos1, i1, j1, k1);
            }
         }
      }

   }

   protected int getSurfaceDepth(int i, int j) {
      double d0 = this.surfaceNoise.getValue((double)i, 0.0D, (double)j);
      return (int)(d0 * 2.75D + 3.0D + this.noiseRandom.at(i, 0, j).nextDouble() * 0.25D);
   }

   protected double getSurfaceSecondary(int i, int j) {
      return this.surfaceSecondaryNoise.getValue((double)i, 0.0D, (double)j);
   }

   private boolean isStone(BlockState blockstate) {
      return !blockstate.isAir() && blockstate.getFluidState().isEmpty();
   }

   /** @deprecated */
   @Deprecated
   public Optional<BlockState> topMaterial(SurfaceRules.RuleSource surfacerules_rulesource, CarvingContext carvingcontext, Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkaccess, NoiseChunk noisechunk, BlockPos blockpos, boolean flag) {
      SurfaceRules.Context surfacerules_context = new SurfaceRules.Context(this, carvingcontext.randomState(), chunkaccess, noisechunk, function, carvingcontext.registryAccess().registryOrThrow(Registries.BIOME), carvingcontext);
      SurfaceRules.SurfaceRule surfacerules_surfacerule = surfacerules_rulesource.apply(surfacerules_context);
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      surfacerules_context.updateXZ(i, k);
      surfacerules_context.updateY(1, 1, flag ? j + 1 : Integer.MIN_VALUE, i, j, k);
      BlockState blockstate = surfacerules_surfacerule.tryApply(i, j, k);
      return Optional.ofNullable(blockstate);
   }

   private void erodedBadlandsExtension(BlockColumn blockcolumn, int i, int j, int k, LevelHeightAccessor levelheightaccessor) {
      double d0 = 0.2D;
      double d1 = Math.min(Math.abs(this.badlandsSurfaceNoise.getValue((double)i, 0.0D, (double)j) * 8.25D), this.badlandsPillarNoise.getValue((double)i * 0.2D, 0.0D, (double)j * 0.2D) * 15.0D);
      if (!(d1 <= 0.0D)) {
         double d2 = 0.75D;
         double d3 = 1.5D;
         double d4 = Math.abs(this.badlandsPillarRoofNoise.getValue((double)i * 0.75D, 0.0D, (double)j * 0.75D) * 1.5D);
         double d5 = 64.0D + Math.min(d1 * d1 * 2.5D, Math.ceil(d4 * 50.0D) + 24.0D);
         int l = Mth.floor(d5);
         if (k <= l) {
            for(int i1 = l; i1 >= levelheightaccessor.getMinBuildHeight(); --i1) {
               BlockState blockstate = blockcolumn.getBlock(i1);
               if (blockstate.is(this.defaultBlock.getBlock())) {
                  break;
               }

               if (blockstate.is(Blocks.WATER)) {
                  return;
               }
            }

            for(int j1 = l; j1 >= levelheightaccessor.getMinBuildHeight() && blockcolumn.getBlock(j1).isAir(); --j1) {
               blockcolumn.setBlock(j1, this.defaultBlock);
            }

         }
      }
   }

   private void frozenOceanExtension(int i, Biome biome, BlockColumn blockcolumn, BlockPos.MutableBlockPos blockpos_mutableblockpos, int j, int k, int l) {
      double d0 = 1.28D;
      double d1 = Math.min(Math.abs(this.icebergSurfaceNoise.getValue((double)j, 0.0D, (double)k) * 8.25D), this.icebergPillarNoise.getValue((double)j * 1.28D, 0.0D, (double)k * 1.28D) * 15.0D);
      if (!(d1 <= 1.8D)) {
         double d2 = 1.17D;
         double d3 = 1.5D;
         double d4 = Math.abs(this.icebergPillarRoofNoise.getValue((double)j * 1.17D, 0.0D, (double)k * 1.17D) * 1.5D);
         double d5 = Math.min(d1 * d1 * 1.2D, Math.ceil(d4 * 40.0D) + 14.0D);
         if (biome.shouldMeltFrozenOceanIcebergSlightly(blockpos_mutableblockpos.set(j, 63, k))) {
            d5 -= 2.0D;
         }

         double d6;
         if (d5 > 2.0D) {
            d6 = (double)this.seaLevel - d5 - 7.0D;
            d5 += (double)this.seaLevel;
         } else {
            d5 = 0.0D;
            d6 = 0.0D;
         }

         double d8 = d5;
         RandomSource randomsource = this.noiseRandom.at(j, 0, k);
         int i1 = 2 + randomsource.nextInt(4);
         int j1 = this.seaLevel + 18 + randomsource.nextInt(10);
         int k1 = 0;

         for(int l1 = Math.max(l, (int)d5 + 1); l1 >= i; --l1) {
            if (blockcolumn.getBlock(l1).isAir() && l1 < (int)d8 && randomsource.nextDouble() > 0.01D || blockcolumn.getBlock(l1).is(Blocks.WATER) && l1 > (int)d6 && l1 < this.seaLevel && d6 != 0.0D && randomsource.nextDouble() > 0.15D) {
               if (k1 <= i1 && l1 > j1) {
                  blockcolumn.setBlock(l1, SNOW_BLOCK);
                  ++k1;
               } else {
                  blockcolumn.setBlock(l1, PACKED_ICE);
               }
            }
         }

      }
   }

   private static BlockState[] generateBands(RandomSource randomsource) {
      BlockState[] ablockstate = new BlockState[192];
      Arrays.fill(ablockstate, TERRACOTTA);

      for(int i = 0; i < ablockstate.length; ++i) {
         i += randomsource.nextInt(5) + 1;
         if (i < ablockstate.length) {
            ablockstate[i] = ORANGE_TERRACOTTA;
         }
      }

      makeBands(randomsource, ablockstate, 1, YELLOW_TERRACOTTA);
      makeBands(randomsource, ablockstate, 2, BROWN_TERRACOTTA);
      makeBands(randomsource, ablockstate, 1, RED_TERRACOTTA);
      int j = randomsource.nextIntBetweenInclusive(9, 15);
      int k = 0;

      for(int l = 0; k < j && l < ablockstate.length; l += randomsource.nextInt(16) + 4) {
         ablockstate[l] = WHITE_TERRACOTTA;
         if (l - 1 > 0 && randomsource.nextBoolean()) {
            ablockstate[l - 1] = LIGHT_GRAY_TERRACOTTA;
         }

         if (l + 1 < ablockstate.length && randomsource.nextBoolean()) {
            ablockstate[l + 1] = LIGHT_GRAY_TERRACOTTA;
         }

         ++k;
      }

      return ablockstate;
   }

   private static void makeBands(RandomSource randomsource, BlockState[] ablockstate, int i, BlockState blockstate) {
      int j = randomsource.nextIntBetweenInclusive(6, 15);

      for(int k = 0; k < j; ++k) {
         int l = i + randomsource.nextInt(3);
         int i1 = randomsource.nextInt(ablockstate.length);

         for(int j1 = 0; i1 + j1 < ablockstate.length && j1 < l; ++j1) {
            ablockstate[i1 + j1] = blockstate;
         }
      }

   }

   protected BlockState getBand(int i, int j, int k) {
      int l = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)i, 0.0D, (double)k) * 4.0D);
      return this.clayBands[(j + l + this.clayBands.length) % this.clayBands.length];
   }
}
