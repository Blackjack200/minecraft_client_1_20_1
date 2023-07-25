package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MushroomBlock extends BushBlock implements BonemealableBlock {
   protected static final float AABB_OFFSET = 3.0F;
   protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);
   private final ResourceKey<ConfiguredFeature<?, ?>> feature;

   public MushroomBlock(BlockBehaviour.Properties blockbehaviour_properties, ResourceKey<ConfiguredFeature<?, ?>> resourcekey) {
      super(blockbehaviour_properties);
      this.feature = resourcekey;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(25) == 0) {
         int i = 5;
         int j = 4;

         for(BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-4, -1, -4), blockpos.offset(4, 1, 4))) {
            if (serverlevel.getBlockState(blockpos1).is(this)) {
               --i;
               if (i <= 0) {
                  return;
               }
            }
         }

         BlockPos blockpos2 = blockpos.offset(randomsource.nextInt(3) - 1, randomsource.nextInt(2) - randomsource.nextInt(2), randomsource.nextInt(3) - 1);

         for(int k = 0; k < 4; ++k) {
            if (serverlevel.isEmptyBlock(blockpos2) && blockstate.canSurvive(serverlevel, blockpos2)) {
               blockpos = blockpos2;
            }

            blockpos2 = blockpos.offset(randomsource.nextInt(3) - 1, randomsource.nextInt(2) - randomsource.nextInt(2), randomsource.nextInt(3) - 1);
         }

         if (serverlevel.isEmptyBlock(blockpos2) && blockstate.canSurvive(serverlevel, blockpos2)) {
            serverlevel.setBlock(blockpos2, blockstate, 2);
         }
      }

   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.isSolidRender(blockgetter, blockpos);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      if (blockstate1.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
         return true;
      } else {
         return levelreader.getRawBrightness(blockpos, 0) < 13 && this.mayPlaceOn(blockstate1, levelreader, blockpos1);
      }
   }

   public boolean growMushroom(ServerLevel serverlevel, BlockPos blockpos, BlockState blockstate, RandomSource randomsource) {
      Optional<? extends Holder<ConfiguredFeature<?, ?>>> optional = serverlevel.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(this.feature);
      if (optional.isEmpty()) {
         return false;
      } else {
         serverlevel.removeBlock(blockpos, false);
         if (optional.get().value().place(serverlevel, serverlevel.getChunkSource().getGenerator(), randomsource, blockpos)) {
            return true;
         } else {
            serverlevel.setBlock(blockpos, blockstate, 3);
            return false;
         }
      }
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return true;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return (double)randomsource.nextFloat() < 0.4D;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      this.growMushroom(serverlevel, blockpos, blockstate, randomsource);
   }
}
