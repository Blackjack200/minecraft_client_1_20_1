package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {
   private static final BlockPos PLATFORM_OFFSET = new BlockPos(8, 3, 8);
   private static final ChunkPos PLATFORM_ORIGIN_CHUNK = new ChunkPos(PLATFORM_OFFSET);
   private static final int PLATFORM_RADIUS = 16;
   private static final int PLATFORM_RADIUS_CHUNKS = 1;

   public VoidStartPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   private static int checkerboardDistance(int i, int j, int k, int l) {
      return Math.max(Math.abs(i - k), Math.abs(j - l));
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      ChunkPos chunkpos = new ChunkPos(featureplacecontext.origin());
      if (checkerboardDistance(chunkpos.x, chunkpos.z, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
         return true;
      } else {
         BlockPos blockpos = PLATFORM_OFFSET.atY(featureplacecontext.origin().getY() + PLATFORM_OFFSET.getY());
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

         for(int i = chunkpos.getMinBlockZ(); i <= chunkpos.getMaxBlockZ(); ++i) {
            for(int j = chunkpos.getMinBlockX(); j <= chunkpos.getMaxBlockX(); ++j) {
               if (checkerboardDistance(blockpos.getX(), blockpos.getZ(), j, i) <= 16) {
                  blockpos_mutableblockpos.set(j, blockpos.getY(), i);
                  if (blockpos_mutableblockpos.equals(blockpos)) {
                     worldgenlevel.setBlock(blockpos_mutableblockpos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                  } else {
                     worldgenlevel.setBlock(blockpos_mutableblockpos, Blocks.STONE.defaultBlockState(), 2);
                  }
               }
            }
         }

         return true;
      }
   }
}
