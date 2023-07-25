package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CropBlock extends BushBlock implements BonemealableBlock {
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

   protected CropBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE_BY_AGE[this.getAge(blockstate)];
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.is(Blocks.FARMLAND);
   }

   protected IntegerProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   public int getAge(BlockState blockstate) {
      return blockstate.getValue(this.getAgeProperty());
   }

   public BlockState getStateForAge(int i) {
      return this.defaultBlockState().setValue(this.getAgeProperty(), Integer.valueOf(i));
   }

   public final boolean isMaxAge(BlockState blockstate) {
      return this.getAge(blockstate) >= this.getMaxAge();
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return !this.isMaxAge(blockstate);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.getRawBrightness(blockpos, 0) >= 9) {
         int i = this.getAge(blockstate);
         if (i < this.getMaxAge()) {
            float f = getGrowthSpeed(this, serverlevel, blockpos);
            if (randomsource.nextInt((int)(25.0F / f) + 1) == 0) {
               serverlevel.setBlock(blockpos, this.getStateForAge(i + 1), 2);
            }
         }
      }

   }

   public void growCrops(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.getAge(blockstate) + this.getBonemealAgeIncrease(level);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      level.setBlock(blockpos, this.getStateForAge(i), 2);
   }

   protected int getBonemealAgeIncrease(Level level) {
      return Mth.nextInt(level.random, 2, 5);
   }

   protected static float getGrowthSpeed(Block block, BlockGetter blockgetter, BlockPos blockpos) {
      float f = 1.0F;
      BlockPos blockpos1 = blockpos.below();

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            float f1 = 0.0F;
            BlockState blockstate = blockgetter.getBlockState(blockpos1.offset(i, 0, j));
            if (blockstate.is(Blocks.FARMLAND)) {
               f1 = 1.0F;
               if (blockstate.getValue(FarmBlock.MOISTURE) > 0) {
                  f1 = 3.0F;
               }
            }

            if (i != 0 || j != 0) {
               f1 /= 4.0F;
            }

            f += f1;
         }
      }

      BlockPos blockpos2 = blockpos.north();
      BlockPos blockpos3 = blockpos.south();
      BlockPos blockpos4 = blockpos.west();
      BlockPos blockpos5 = blockpos.east();
      boolean flag = blockgetter.getBlockState(blockpos4).is(block) || blockgetter.getBlockState(blockpos5).is(block);
      boolean flag1 = blockgetter.getBlockState(blockpos2).is(block) || blockgetter.getBlockState(blockpos3).is(block);
      if (flag && flag1) {
         f /= 2.0F;
      } else {
         boolean flag2 = blockgetter.getBlockState(blockpos4.north()).is(block) || blockgetter.getBlockState(blockpos5.north()).is(block) || blockgetter.getBlockState(blockpos5.south()).is(block) || blockgetter.getBlockState(blockpos4.south()).is(block);
         if (flag2) {
            f /= 2.0F;
         }
      }

      return f;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return (levelreader.getRawBrightness(blockpos, 0) >= 8 || levelreader.canSeeSky(blockpos)) && super.canSurvive(blockstate, levelreader, blockpos);
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (entity instanceof Ravager && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         level.destroyBlock(blockpos, true, entity);
      }

      super.entityInside(blockstate, level, blockpos, entity);
   }

   protected ItemLike getBaseSeedId() {
      return Items.WHEAT_SEEDS;
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(this.getBaseSeedId());
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return !this.isMaxAge(blockstate);
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      this.growCrops(serverlevel, blockpos, blockstate);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }
}
