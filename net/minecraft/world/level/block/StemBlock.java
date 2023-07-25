package net.minecraft.world.level.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StemBlock extends BushBlock implements BonemealableBlock {
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   protected static final float AABB_OFFSET = 1.0F;
   protected static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{Block.box(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
   private final StemGrownBlock fruit;
   private final Supplier<Item> seedSupplier;

   protected StemBlock(StemGrownBlock stemgrownblock, Supplier<Item> supplier, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.fruit = stemgrownblock;
      this.seedSupplier = supplier;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPE_BY_AGE[blockstate.getValue(AGE)];
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.is(Blocks.FARMLAND);
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.getRawBrightness(blockpos, 0) >= 9) {
         float f = CropBlock.getGrowthSpeed(this, serverlevel, blockpos);
         if (randomsource.nextInt((int)(25.0F / f) + 1) == 0) {
            int i = blockstate.getValue(AGE);
            if (i < 7) {
               blockstate = blockstate.setValue(AGE, Integer.valueOf(i + 1));
               serverlevel.setBlock(blockpos, blockstate, 2);
            } else {
               Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(randomsource);
               BlockPos blockpos1 = blockpos.relative(direction);
               BlockState blockstate1 = serverlevel.getBlockState(blockpos1.below());
               if (serverlevel.getBlockState(blockpos1).isAir() && (blockstate1.is(Blocks.FARMLAND) || blockstate1.is(BlockTags.DIRT))) {
                  serverlevel.setBlockAndUpdate(blockpos1, this.fruit.defaultBlockState());
                  serverlevel.setBlockAndUpdate(blockpos, this.fruit.getAttachedStem().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction));
               }
            }
         }

      }
   }

   public ItemStack getCloneItemStack(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return new ItemStack(this.seedSupplier.get());
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      return blockstate.getValue(AGE) != 7;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      int i = Math.min(7, blockstate.getValue(AGE) + Mth.nextInt(serverlevel.random, 2, 5));
      BlockState blockstate1 = blockstate.setValue(AGE, Integer.valueOf(i));
      serverlevel.setBlock(blockpos, blockstate1, 2);
      if (i == 7) {
         blockstate1.randomTick(serverlevel, blockpos, serverlevel.random);
      }

   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
   }

   public StemGrownBlock getFruit() {
      return this.fruit;
   }
}
