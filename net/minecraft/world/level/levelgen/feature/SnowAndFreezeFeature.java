package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SnowAndFreezeFeature extends Feature<NoneFeatureConfiguration> {
   public SnowAndFreezeFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      BlockPos blockpos = featureplacecontext.origin();
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = new BlockPos.MutableBlockPos();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            int k = blockpos.getX() + i;
            int l = blockpos.getZ() + j;
            int i1 = worldgenlevel.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
            blockpos_mutableblockpos.set(k, i1, l);
            blockpos_mutableblockpos1.set(blockpos_mutableblockpos).move(Direction.DOWN, 1);
            Biome biome = worldgenlevel.getBiome(blockpos_mutableblockpos).value();
            if (biome.shouldFreeze(worldgenlevel, blockpos_mutableblockpos1, false)) {
               worldgenlevel.setBlock(blockpos_mutableblockpos1, Blocks.ICE.defaultBlockState(), 2);
            }

            if (biome.shouldSnow(worldgenlevel, blockpos_mutableblockpos)) {
               worldgenlevel.setBlock(blockpos_mutableblockpos, Blocks.SNOW.defaultBlockState(), 2);
               BlockState blockstate = worldgenlevel.getBlockState(blockpos_mutableblockpos1);
               if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
                  worldgenlevel.setBlock(blockpos_mutableblockpos1, blockstate.setValue(SnowyDirtBlock.SNOWY, Boolean.valueOf(true)), 2);
               }
            }
         }
      }

      return true;
   }
}
