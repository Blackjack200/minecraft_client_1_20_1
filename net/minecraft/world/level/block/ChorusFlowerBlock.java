package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ChorusFlowerBlock extends Block {
   public static final int DEAD_AGE = 5;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
   private final ChorusPlantBlock plant;

   protected ChorusFlowerBlock(ChorusPlantBlock chorusplantblock, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.plant = chorusplantblock;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      }

   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(AGE) < 5;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockPos blockpos1 = blockpos.above();
      if (serverlevel.isEmptyBlock(blockpos1) && blockpos1.getY() < serverlevel.getMaxBuildHeight()) {
         int i = blockstate.getValue(AGE);
         if (i < 5) {
            boolean flag = false;
            boolean flag1 = false;
            BlockState blockstate1 = serverlevel.getBlockState(blockpos.below());
            if (blockstate1.is(Blocks.END_STONE)) {
               flag = true;
            } else if (blockstate1.is(this.plant)) {
               int j = 1;

               for(int k = 0; k < 4; ++k) {
                  BlockState blockstate2 = serverlevel.getBlockState(blockpos.below(j + 1));
                  if (!blockstate2.is(this.plant)) {
                     if (blockstate2.is(Blocks.END_STONE)) {
                        flag1 = true;
                     }
                     break;
                  }

                  ++j;
               }

               if (j < 2 || j <= randomsource.nextInt(flag1 ? 5 : 4)) {
                  flag = true;
               }
            } else if (blockstate1.isAir()) {
               flag = true;
            }

            if (flag && allNeighborsEmpty(serverlevel, blockpos1, (Direction)null) && serverlevel.isEmptyBlock(blockpos.above(2))) {
               serverlevel.setBlock(blockpos, this.plant.getStateForPlacement(serverlevel, blockpos), 2);
               this.placeGrownFlower(serverlevel, blockpos1, i);
            } else if (i < 4) {
               int l = randomsource.nextInt(4);
               if (flag1) {
                  ++l;
               }

               boolean flag2 = false;

               for(int i1 = 0; i1 < l; ++i1) {
                  Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
                  BlockPos blockpos2 = blockpos.relative(direction);
                  if (serverlevel.isEmptyBlock(blockpos2) && serverlevel.isEmptyBlock(blockpos2.below()) && allNeighborsEmpty(serverlevel, blockpos2, direction.getOpposite())) {
                     this.placeGrownFlower(serverlevel, blockpos2, i + 1);
                     flag2 = true;
                  }
               }

               if (flag2) {
                  serverlevel.setBlock(blockpos, this.plant.getStateForPlacement(serverlevel, blockpos), 2);
               } else {
                  this.placeDeadFlower(serverlevel, blockpos);
               }
            } else {
               this.placeDeadFlower(serverlevel, blockpos);
            }

         }
      }
   }

   private void placeGrownFlower(Level level, BlockPos blockpos, int i) {
      level.setBlock(blockpos, this.defaultBlockState().setValue(AGE, Integer.valueOf(i)), 2);
      level.levelEvent(1033, blockpos, 0);
   }

   private void placeDeadFlower(Level level, BlockPos blockpos) {
      level.setBlock(blockpos, this.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
      level.levelEvent(1034, blockpos, 0);
   }

   private static boolean allNeighborsEmpty(LevelReader levelreader, BlockPos blockpos, @Nullable Direction direction) {
      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         if (direction1 != direction && !levelreader.isEmptyBlock(blockpos.relative(direction1))) {
            return false;
         }
      }

      return true;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction != Direction.UP && !blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.below());
      if (!blockstate1.is(this.plant) && !blockstate1.is(Blocks.END_STONE)) {
         if (!blockstate1.isAir()) {
            return false;
         } else {
            boolean flag = false;

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               BlockState blockstate2 = levelreader.getBlockState(blockpos.relative(direction));
               if (blockstate2.is(this.plant)) {
                  if (flag) {
                     return false;
                  }

                  flag = true;
               } else if (!blockstate2.isAir()) {
                  return false;
               }
            }

            return flag;
         }
      } else {
         return true;
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }

   public static void generatePlant(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, int i) {
      levelaccessor.setBlock(blockpos, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).getStateForPlacement(levelaccessor, blockpos), 2);
      growTreeRecursive(levelaccessor, blockpos, randomsource, blockpos, i, 0);
   }

   private static void growTreeRecursive(LevelAccessor levelaccessor, BlockPos blockpos, RandomSource randomsource, BlockPos blockpos1, int i, int j) {
      ChorusPlantBlock chorusplantblock = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
      int k = randomsource.nextInt(4) + 1;
      if (j == 0) {
         ++k;
      }

      for(int l = 0; l < k; ++l) {
         BlockPos blockpos2 = blockpos.above(l + 1);
         if (!allNeighborsEmpty(levelaccessor, blockpos2, (Direction)null)) {
            return;
         }

         levelaccessor.setBlock(blockpos2, chorusplantblock.getStateForPlacement(levelaccessor, blockpos2), 2);
         levelaccessor.setBlock(blockpos2.below(), chorusplantblock.getStateForPlacement(levelaccessor, blockpos2.below()), 2);
      }

      boolean flag = false;
      if (j < 4) {
         int i1 = randomsource.nextInt(4);
         if (j == 0) {
            ++i1;
         }

         for(int j1 = 0; j1 < i1; ++j1) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
            BlockPos blockpos3 = blockpos.above(k).relative(direction);
            if (Math.abs(blockpos3.getX() - blockpos1.getX()) < i && Math.abs(blockpos3.getZ() - blockpos1.getZ()) < i && levelaccessor.isEmptyBlock(blockpos3) && levelaccessor.isEmptyBlock(blockpos3.below()) && allNeighborsEmpty(levelaccessor, blockpos3, direction.getOpposite())) {
               flag = true;
               levelaccessor.setBlock(blockpos3, chorusplantblock.getStateForPlacement(levelaccessor, blockpos3), 2);
               levelaccessor.setBlock(blockpos3.relative(direction.getOpposite()), chorusplantblock.getStateForPlacement(levelaccessor, blockpos3.relative(direction.getOpposite())), 2);
               growTreeRecursive(levelaccessor, blockpos3, randomsource, blockpos1, i, j + 1);
            }
         }
      }

      if (!flag) {
         levelaccessor.setBlock(blockpos.above(k), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
      }

   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      BlockPos blockpos = blockhitresult.getBlockPos();
      if (!level.isClientSide && projectile.mayInteract(level, blockpos) && projectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
         level.destroyBlock(blockpos, true, projectile);
      }

   }
}
