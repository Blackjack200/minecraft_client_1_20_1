package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class GrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
   public GrassBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return levelreader.getBlockState(blockpos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      BlockPos blockpos1 = blockpos.above();
      BlockState blockstate1 = Blocks.GRASS.defaultBlockState();
      Optional<Holder.Reference<PlacedFeature>> optional = serverlevel.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getHolder(VegetationPlacements.GRASS_BONEMEAL);

      label49:
      for(int i = 0; i < 128; ++i) {
         BlockPos blockpos2 = blockpos1;

         for(int j = 0; j < i / 16; ++j) {
            blockpos2 = blockpos2.offset(randomsource.nextInt(3) - 1, (randomsource.nextInt(3) - 1) * randomsource.nextInt(3) / 2, randomsource.nextInt(3) - 1);
            if (!serverlevel.getBlockState(blockpos2.below()).is(this) || serverlevel.getBlockState(blockpos2).isCollisionShapeFullBlock(serverlevel, blockpos2)) {
               continue label49;
            }
         }

         BlockState blockstate2 = serverlevel.getBlockState(blockpos2);
         if (blockstate2.is(blockstate1.getBlock()) && randomsource.nextInt(10) == 0) {
            ((BonemealableBlock)blockstate1.getBlock()).performBonemeal(serverlevel, randomsource, blockpos2, blockstate2);
         }

         if (blockstate2.isAir()) {
            Holder<PlacedFeature> holder;
            if (randomsource.nextInt(8) == 0) {
               List<ConfiguredFeature<?, ?>> list = serverlevel.getBiome(blockpos2).value().getGenerationSettings().getFlowerFeatures();
               if (list.isEmpty()) {
                  continue;
               }

               holder = ((RandomPatchConfiguration)list.get(0).config()).feature();
            } else {
               if (!optional.isPresent()) {
                  continue;
               }

               holder = optional.get();
            }

            holder.value().place(serverlevel, serverlevel.getChunkSource().getGenerator(), randomsource, blockpos2);
         }
      }

   }
}
