package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BonusChestFeature extends Feature<NoneFeatureConfiguration> {
   public BonusChestFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featureplacecontext) {
      RandomSource randomsource = featureplacecontext.random();
      WorldGenLevel worldgenlevel = featureplacecontext.level();
      ChunkPos chunkpos = new ChunkPos(featureplacecontext.origin());
      IntArrayList intarraylist = Util.toShuffledList(IntStream.rangeClosed(chunkpos.getMinBlockX(), chunkpos.getMaxBlockX()), randomsource);
      IntArrayList intarraylist1 = Util.toShuffledList(IntStream.rangeClosed(chunkpos.getMinBlockZ(), chunkpos.getMaxBlockZ()), randomsource);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Integer integer : intarraylist) {
         for(Integer integer1 : intarraylist1) {
            blockpos_mutableblockpos.set(integer, 0, integer1);
            BlockPos blockpos = worldgenlevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockpos_mutableblockpos);
            if (worldgenlevel.isEmptyBlock(blockpos) || worldgenlevel.getBlockState(blockpos).getCollisionShape(worldgenlevel, blockpos).isEmpty()) {
               worldgenlevel.setBlock(blockpos, Blocks.CHEST.defaultBlockState(), 2);
               RandomizableContainerBlockEntity.setLootTable(worldgenlevel, randomsource, blockpos, BuiltInLootTables.SPAWN_BONUS_CHEST);
               BlockState blockstate = Blocks.TORCH.defaultBlockState();

               for(Direction direction : Direction.Plane.HORIZONTAL) {
                  BlockPos blockpos1 = blockpos.relative(direction);
                  if (blockstate.canSurvive(worldgenlevel, blockpos1)) {
                     worldgenlevel.setBlock(blockpos1, blockstate, 2);
                  }
               }

               return true;
            }
         }
      }

      return false;
   }
}
