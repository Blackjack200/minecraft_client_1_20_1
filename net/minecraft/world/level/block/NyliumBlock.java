package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.lighting.LightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
   protected NyliumBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   private static boolean canBeNylium(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.above();
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      int i = LightEngine.getLightBlockInto(levelreader, blockstate, blockpos, blockstate1, blockpos1, Direction.UP, blockstate1.getLightBlock(levelreader, blockpos1));
      return i < levelreader.getMaxLightLevel();
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!canBeNylium(blockstate, serverlevel, blockpos)) {
         serverlevel.setBlockAndUpdate(blockpos, Blocks.NETHERRACK.defaultBlockState());
      }

   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return levelreader.getBlockState(blockpos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      BlockState blockstate1 = serverlevel.getBlockState(blockpos);
      BlockPos blockpos1 = blockpos.above();
      ChunkGenerator chunkgenerator = serverlevel.getChunkSource().getGenerator();
      Registry<ConfiguredFeature<?, ?>> registry = serverlevel.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
      if (blockstate1.is(Blocks.CRIMSON_NYLIUM)) {
         this.place(registry, NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL, serverlevel, chunkgenerator, randomsource, blockpos1);
      } else if (blockstate1.is(Blocks.WARPED_NYLIUM)) {
         this.place(registry, NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL, serverlevel, chunkgenerator, randomsource, blockpos1);
         this.place(registry, NetherFeatures.NETHER_SPROUTS_BONEMEAL, serverlevel, chunkgenerator, randomsource, blockpos1);
         if (randomsource.nextInt(8) == 0) {
            this.place(registry, NetherFeatures.TWISTING_VINES_BONEMEAL, serverlevel, chunkgenerator, randomsource, blockpos1);
         }
      }

   }

   private void place(Registry<ConfiguredFeature<?, ?>> registry, ResourceKey<ConfiguredFeature<?, ?>> resourcekey, ServerLevel serverlevel, ChunkGenerator chunkgenerator, RandomSource randomsource, BlockPos blockpos) {
      registry.getHolder(resourcekey).ifPresent((holder_reference) -> holder_reference.value().place(serverlevel, chunkgenerator, randomsource, blockpos));
   }
}
