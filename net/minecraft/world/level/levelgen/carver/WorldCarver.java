package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
   public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
   protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
   protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
   protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
   private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

   private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String s, F worldcarver) {
      return Registry.register(BuiltInRegistries.CARVER, s, worldcarver);
   }

   public WorldCarver(Codec<C> codec) {
      this.configuredCodec = codec.fieldOf("config").xmap(this::configured, ConfiguredWorldCarver::config).codec();
   }

   public ConfiguredWorldCarver<C> configured(C carverconfiguration) {
      return new ConfiguredWorldCarver<>(this, carverconfiguration);
   }

   public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
      return this.configuredCodec;
   }

   public int getRange() {
      return 4;
   }

   protected boolean carveEllipsoid(CarvingContext carvingcontext, C carverconfiguration, ChunkAccess chunkaccess, Function<BlockPos, Holder<Biome>> function, Aquifer aquifer, double d0, double d1, double d2, double d3, double d4, CarvingMask carvingmask, WorldCarver.CarveSkipChecker worldcarver_carveskipchecker) {
      ChunkPos chunkpos = chunkaccess.getPos();
      double d5 = (double)chunkpos.getMiddleBlockX();
      double d6 = (double)chunkpos.getMiddleBlockZ();
      double d7 = 16.0D + d3 * 2.0D;
      if (!(Math.abs(d0 - d5) > d7) && !(Math.abs(d2 - d6) > d7)) {
         int i = chunkpos.getMinBlockX();
         int j = chunkpos.getMinBlockZ();
         int k = Math.max(Mth.floor(d0 - d3) - i - 1, 0);
         int l = Math.min(Mth.floor(d0 + d3) - i, 15);
         int i1 = Math.max(Mth.floor(d1 - d4) - 1, carvingcontext.getMinGenY() + 1);
         int j1 = chunkaccess.isUpgrading() ? 0 : 7;
         int k1 = Math.min(Mth.floor(d1 + d4) + 1, carvingcontext.getMinGenY() + carvingcontext.getGenDepth() - 1 - j1);
         int l1 = Math.max(Mth.floor(d2 - d3) - j - 1, 0);
         int i2 = Math.min(Mth.floor(d2 + d3) - j, 15);
         boolean flag = false;
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
         BlockPos.MutableBlockPos blockpos_mutableblockpos1 = new BlockPos.MutableBlockPos();

         for(int j2 = k; j2 <= l; ++j2) {
            int k2 = chunkpos.getBlockX(j2);
            double d8 = ((double)k2 + 0.5D - d0) / d3;

            for(int l2 = l1; l2 <= i2; ++l2) {
               int i3 = chunkpos.getBlockZ(l2);
               double d9 = ((double)i3 + 0.5D - d2) / d3;
               if (!(d8 * d8 + d9 * d9 >= 1.0D)) {
                  MutableBoolean mutableboolean = new MutableBoolean(false);

                  for(int j3 = k1; j3 > i1; --j3) {
                     double d10 = ((double)j3 - 0.5D - d1) / d4;
                     if (!worldcarver_carveskipchecker.shouldSkip(carvingcontext, d8, d10, d9, j3) && (!carvingmask.get(j2, j3, l2) || isDebugEnabled(carverconfiguration))) {
                        carvingmask.set(j2, j3, l2);
                        blockpos_mutableblockpos.set(k2, j3, i3);
                        flag |= this.carveBlock(carvingcontext, carverconfiguration, chunkaccess, function, carvingmask, blockpos_mutableblockpos, blockpos_mutableblockpos1, aquifer, mutableboolean);
                     }
                  }
               }
            }
         }

         return flag;
      } else {
         return false;
      }
   }

   protected boolean carveBlock(CarvingContext carvingcontext, C carverconfiguration, ChunkAccess chunkaccess, Function<BlockPos, Holder<Biome>> function, CarvingMask carvingmask, BlockPos.MutableBlockPos blockpos_mutableblockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos1, Aquifer aquifer, MutableBoolean mutableboolean) {
      BlockState blockstate = chunkaccess.getBlockState(blockpos_mutableblockpos);
      if (blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(Blocks.MYCELIUM)) {
         mutableboolean.setTrue();
      }

      if (!this.canReplaceBlock(carverconfiguration, blockstate) && !isDebugEnabled(carverconfiguration)) {
         return false;
      } else {
         BlockState blockstate1 = this.getCarveState(carvingcontext, carverconfiguration, blockpos_mutableblockpos, aquifer);
         if (blockstate1 == null) {
            return false;
         } else {
            chunkaccess.setBlockState(blockpos_mutableblockpos, blockstate1, false);
            if (aquifer.shouldScheduleFluidUpdate() && !blockstate1.getFluidState().isEmpty()) {
               chunkaccess.markPosForPostprocessing(blockpos_mutableblockpos);
            }

            if (mutableboolean.isTrue()) {
               blockpos_mutableblockpos1.setWithOffset(blockpos_mutableblockpos, Direction.DOWN);
               if (chunkaccess.getBlockState(blockpos_mutableblockpos1).is(Blocks.DIRT)) {
                  carvingcontext.topMaterial(function, chunkaccess, blockpos_mutableblockpos1, !blockstate1.getFluidState().isEmpty()).ifPresent((blockstate2) -> {
                     chunkaccess.setBlockState(blockpos_mutableblockpos1, blockstate2, false);
                     if (!blockstate2.getFluidState().isEmpty()) {
                        chunkaccess.markPosForPostprocessing(blockpos_mutableblockpos1);
                     }

                  });
               }
            }

            return true;
         }
      }
   }

   @Nullable
   private BlockState getCarveState(CarvingContext carvingcontext, C carverconfiguration, BlockPos blockpos, Aquifer aquifer) {
      if (blockpos.getY() <= carverconfiguration.lavaLevel.resolveY(carvingcontext)) {
         return LAVA.createLegacyBlock();
      } else {
         BlockState blockstate = aquifer.computeSubstance(new DensityFunction.SinglePointContext(blockpos.getX(), blockpos.getY(), blockpos.getZ()), 0.0D);
         if (blockstate == null) {
            return isDebugEnabled(carverconfiguration) ? carverconfiguration.debugSettings.getBarrierState() : null;
         } else {
            return isDebugEnabled(carverconfiguration) ? getDebugState(carverconfiguration, blockstate) : blockstate;
         }
      }
   }

   private static BlockState getDebugState(CarverConfiguration carverconfiguration, BlockState blockstate) {
      if (blockstate.is(Blocks.AIR)) {
         return carverconfiguration.debugSettings.getAirState();
      } else if (blockstate.is(Blocks.WATER)) {
         BlockState blockstate1 = carverconfiguration.debugSettings.getWaterState();
         return blockstate1.hasProperty(BlockStateProperties.WATERLOGGED) ? blockstate1.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)) : blockstate1;
      } else {
         return blockstate.is(Blocks.LAVA) ? carverconfiguration.debugSettings.getLavaState() : blockstate;
      }
   }

   public abstract boolean carve(CarvingContext carvingcontext, C carverconfiguration, ChunkAccess chunkaccess, Function<BlockPos, Holder<Biome>> function, RandomSource randomsource, Aquifer aquifer, ChunkPos chunkpos, CarvingMask carvingmask);

   public abstract boolean isStartChunk(C carverconfiguration, RandomSource randomsource);

   protected boolean canReplaceBlock(C carverconfiguration, BlockState blockstate) {
      return blockstate.is(carverconfiguration.replaceable);
   }

   protected static boolean canReach(ChunkPos chunkpos, double d0, double d1, int i, int j, float f) {
      double d2 = (double)chunkpos.getMiddleBlockX();
      double d3 = (double)chunkpos.getMiddleBlockZ();
      double d4 = d0 - d2;
      double d5 = d1 - d3;
      double d6 = (double)(j - i);
      double d7 = (double)(f + 2.0F + 16.0F);
      return d4 * d4 + d5 * d5 - d6 * d6 <= d7 * d7;
   }

   private static boolean isDebugEnabled(CarverConfiguration carverconfiguration) {
      return carverconfiguration.debugSettings.isDebugMode();
   }

   public interface CarveSkipChecker {
      boolean shouldSkip(CarvingContext carvingcontext, double d0, double d1, double d2, int i);
   }
}
