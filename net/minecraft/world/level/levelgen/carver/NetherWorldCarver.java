package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class NetherWorldCarver extends CaveWorldCarver {
   public NetherWorldCarver(Codec<CaveCarverConfiguration> codec) {
      super(codec);
      this.liquids = ImmutableSet.of(Fluids.LAVA, Fluids.WATER);
   }

   protected int getCaveBound() {
      return 10;
   }

   protected float getThickness(RandomSource randomsource) {
      return (randomsource.nextFloat() * 2.0F + randomsource.nextFloat()) * 2.0F;
   }

   protected double getYScale() {
      return 5.0D;
   }

   protected boolean carveBlock(CarvingContext carvingcontext, CaveCarverConfiguration cavecarverconfiguration, ChunkAccess chunkaccess, Function<BlockPos, Holder<Biome>> function, CarvingMask carvingmask, BlockPos.MutableBlockPos blockpos_mutableblockpos, BlockPos.MutableBlockPos blockpos_mutableblockpos1, Aquifer aquifer, MutableBoolean mutableboolean) {
      if (this.canReplaceBlock(cavecarverconfiguration, chunkaccess.getBlockState(blockpos_mutableblockpos))) {
         BlockState blockstate;
         if (blockpos_mutableblockpos.getY() <= carvingcontext.getMinGenY() + 31) {
            blockstate = LAVA.createLegacyBlock();
         } else {
            blockstate = CAVE_AIR;
         }

         chunkaccess.setBlockState(blockpos_mutableblockpos, blockstate, false);
         return true;
      } else {
         return false;
      }
   }
}
