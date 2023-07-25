package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BoneMealItem extends Item {
   public static final int GRASS_SPREAD_WIDTH = 3;
   public static final int GRASS_SPREAD_HEIGHT = 1;
   public static final int GRASS_COUNT_MULTIPLIER = 3;

   public BoneMealItem(Item.Properties item_properties) {
      super(item_properties);
   }

   public InteractionResult useOn(UseOnContext useoncontext) {
      Level level = useoncontext.getLevel();
      BlockPos blockpos = useoncontext.getClickedPos();
      BlockPos blockpos1 = blockpos.relative(useoncontext.getClickedFace());
      if (growCrop(useoncontext.getItemInHand(), level, blockpos)) {
         if (!level.isClientSide) {
            level.levelEvent(1505, blockpos, 0);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         BlockState blockstate = level.getBlockState(blockpos);
         boolean flag = blockstate.isFaceSturdy(level, blockpos, useoncontext.getClickedFace());
         if (flag && growWaterPlant(useoncontext.getItemInHand(), level, blockpos1, useoncontext.getClickedFace())) {
            if (!level.isClientSide) {
               level.levelEvent(1505, blockpos1, 0);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public static boolean growCrop(ItemStack itemstack, Level level, BlockPos blockpos) {
      BlockState blockstate = level.getBlockState(blockpos);
      if (blockstate.getBlock() instanceof BonemealableBlock) {
         BonemealableBlock bonemealableblock = (BonemealableBlock)blockstate.getBlock();
         if (bonemealableblock.isValidBonemealTarget(level, blockpos, blockstate, level.isClientSide)) {
            if (level instanceof ServerLevel) {
               if (bonemealableblock.isBonemealSuccess(level, level.random, blockpos, blockstate)) {
                  bonemealableblock.performBonemeal((ServerLevel)level, level.random, blockpos, blockstate);
               }

               itemstack.shrink(1);
            }

            return true;
         }
      }

      return false;
   }

   public static boolean growWaterPlant(ItemStack itemstack, Level level, BlockPos blockpos, @Nullable Direction direction) {
      if (level.getBlockState(blockpos).is(Blocks.WATER) && level.getFluidState(blockpos).getAmount() == 8) {
         if (!(level instanceof ServerLevel)) {
            return true;
         } else {
            RandomSource randomsource = level.getRandom();

            label78:
            for(int i = 0; i < 128; ++i) {
               BlockPos blockpos1 = blockpos;
               BlockState blockstate = Blocks.SEAGRASS.defaultBlockState();

               for(int j = 0; j < i / 16; ++j) {
                  blockpos1 = blockpos1.offset(randomsource.nextInt(3) - 1, (randomsource.nextInt(3) - 1) * randomsource.nextInt(3) / 2, randomsource.nextInt(3) - 1);
                  if (level.getBlockState(blockpos1).isCollisionShapeFullBlock(level, blockpos1)) {
                     continue label78;
                  }
               }

               Holder<Biome> holder = level.getBiome(blockpos1);
               if (holder.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                  if (i == 0 && direction != null && direction.getAxis().isHorizontal()) {
                     blockstate = BuiltInRegistries.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap((holderset_named1) -> holderset_named1.getRandomElement(level.random)).map((holder2) -> holder2.value().defaultBlockState()).orElse(blockstate);
                     if (blockstate.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        blockstate = blockstate.setValue(BaseCoralWallFanBlock.FACING, direction);
                     }
                  } else if (randomsource.nextInt(4) == 0) {
                     blockstate = BuiltInRegistries.BLOCK.getTag(BlockTags.UNDERWATER_BONEMEALS).flatMap((holderset_named) -> holderset_named.getRandomElement(level.random)).map((holder1) -> holder1.value().defaultBlockState()).orElse(blockstate);
                  }
               }

               if (blockstate.is(BlockTags.WALL_CORALS, (blockbehaviour_blockstatebase) -> blockbehaviour_blockstatebase.hasProperty(BaseCoralWallFanBlock.FACING))) {
                  for(int k = 0; !blockstate.canSurvive(level, blockpos1) && k < 4; ++k) {
                     blockstate = blockstate.setValue(BaseCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomsource));
                  }
               }

               if (blockstate.canSurvive(level, blockpos1)) {
                  BlockState blockstate1 = level.getBlockState(blockpos1);
                  if (blockstate1.is(Blocks.WATER) && level.getFluidState(blockpos1).getAmount() == 8) {
                     level.setBlock(blockpos1, blockstate, 3);
                  } else if (blockstate1.is(Blocks.SEAGRASS) && randomsource.nextInt(10) == 0) {
                     ((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)level, randomsource, blockpos1, blockstate1);
                  }
               }
            }

            itemstack.shrink(1);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void addGrowthParticles(LevelAccessor levelaccessor, BlockPos blockpos, int i) {
      if (i == 0) {
         i = 15;
      }

      BlockState blockstate = levelaccessor.getBlockState(blockpos);
      if (!blockstate.isAir()) {
         double d0 = 0.5D;
         double d1;
         if (blockstate.is(Blocks.WATER)) {
            i *= 3;
            d1 = 1.0D;
            d0 = 3.0D;
         } else if (blockstate.isSolidRender(levelaccessor, blockpos)) {
            blockpos = blockpos.above();
            i *= 3;
            d0 = 3.0D;
            d1 = 1.0D;
         } else {
            d1 = blockstate.getShape(levelaccessor, blockpos).max(Direction.Axis.Y);
         }

         levelaccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
         RandomSource randomsource = levelaccessor.getRandom();

         for(int j = 0; j < i; ++j) {
            double d4 = randomsource.nextGaussian() * 0.02D;
            double d5 = randomsource.nextGaussian() * 0.02D;
            double d6 = randomsource.nextGaussian() * 0.02D;
            double d7 = 0.5D - d0;
            double d8 = (double)blockpos.getX() + d7 + randomsource.nextDouble() * d0 * 2.0D;
            double d9 = (double)blockpos.getY() + randomsource.nextDouble() * d1;
            double d10 = (double)blockpos.getZ() + d7 + randomsource.nextDouble() * d0 * 2.0D;
            if (!levelaccessor.getBlockState(BlockPos.containing(d8, d9, d10).below()).isAir()) {
               levelaccessor.addParticle(ParticleTypes.HAPPY_VILLAGER, d8, d9, d10, d4, d5, d6);
            }
         }

      }
   }
}
