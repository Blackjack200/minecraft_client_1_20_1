package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BambooStalkBlock extends Block implements BonemealableBlock {
   protected static final float SMALL_LEAVES_AABB_OFFSET = 3.0F;
   protected static final float LARGE_LEAVES_AABB_OFFSET = 5.0F;
   protected static final float COLLISION_AABB_OFFSET = 1.5F;
   protected static final VoxelShape SMALL_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
   protected static final VoxelShape LARGE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   protected static final VoxelShape COLLISION_SHAPE = Block.box(6.5D, 0.0D, 6.5D, 9.5D, 16.0D, 9.5D);
   public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
   public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
   public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
   public static final int MAX_HEIGHT = 16;
   public static final int STAGE_GROWING = 0;
   public static final int STAGE_DONE_GROWING = 1;
   public static final int AGE_THIN_BAMBOO = 0;
   public static final int AGE_THICK_BAMBOO = 1;

   public BambooStalkBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)).setValue(LEAVES, BambooLeaves.NONE).setValue(STAGE, Integer.valueOf(0)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE, LEAVES, STAGE);
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return true;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      VoxelShape voxelshape = blockstate.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
      Vec3 vec3 = blockstate.getOffset(blockgetter, blockpos);
      return voxelshape.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      Vec3 vec3 = blockstate.getOffset(blockgetter, blockpos);
      return COLLISION_SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   public boolean isCollisionShapeFullBlock(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return false;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      if (!fluidstate.isEmpty()) {
         return null;
      } else {
         BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos().below());
         if (blockstate.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockstate.is(Blocks.BAMBOO_SAPLING)) {
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(0));
            } else if (blockstate.is(Blocks.BAMBOO)) {
               int i = blockstate.getValue(AGE) > 0 ? 1 : 0;
               return this.defaultBlockState().setValue(AGE, Integer.valueOf(i));
            } else {
               BlockState blockstate1 = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos().above());
               return blockstate1.is(Blocks.BAMBOO) ? this.defaultBlockState().setValue(AGE, blockstate1.getValue(AGE)) : Blocks.BAMBOO_SAPLING.defaultBlockState();
            }
         } else {
            return null;
         }
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (!blockstate.canSurvive(serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      }

   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(STAGE) == 0;
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(STAGE) == 0) {
         if (randomsource.nextInt(3) == 0 && serverlevel.isEmptyBlock(blockpos.above()) && serverlevel.getRawBrightness(blockpos.above(), 0) >= 9) {
            int i = this.getHeightBelowUpToMax(serverlevel, blockpos) + 1;
            if (i < 16) {
               this.growBamboo(blockstate, serverlevel, blockpos, randomsource, i);
            }
         }

      }
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return levelreader.getBlockState(blockpos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!blockstate.canSurvive(levelaccessor, blockpos)) {
         levelaccessor.scheduleTick(blockpos, this, 1);
      }

      if (direction == Direction.UP && blockstate1.is(Blocks.BAMBOO) && blockstate1.getValue(AGE) > blockstate.getValue(AGE)) {
         levelaccessor.setBlock(blockpos, blockstate.cycle(AGE), 2);
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      int i = this.getHeightAboveUpToMax(levelreader, blockpos);
      int j = this.getHeightBelowUpToMax(levelreader, blockpos);
      return i + j + 1 < 16 && levelreader.getBlockState(blockpos.above(i)).getValue(STAGE) != 1;
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      int i = this.getHeightAboveUpToMax(serverlevel, blockpos);
      int j = this.getHeightBelowUpToMax(serverlevel, blockpos);
      int k = i + j + 1;
      int l = 1 + randomsource.nextInt(2);

      for(int i1 = 0; i1 < l; ++i1) {
         BlockPos blockpos1 = blockpos.above(i);
         BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
         if (k >= 16 || blockstate1.getValue(STAGE) == 1 || !serverlevel.isEmptyBlock(blockpos1.above())) {
            return;
         }

         this.growBamboo(blockstate1, serverlevel, blockpos1, randomsource, k);
         ++i;
         ++k;
      }

   }

   public float getDestroyProgress(BlockState blockstate, Player player, BlockGetter blockgetter, BlockPos blockpos) {
      return player.getMainHandItem().getItem() instanceof SwordItem ? 1.0F : super.getDestroyProgress(blockstate, player, blockgetter, blockpos);
   }

   protected void growBamboo(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource, int i) {
      BlockState blockstate1 = level.getBlockState(blockpos.below());
      BlockPos blockpos1 = blockpos.below(2);
      BlockState blockstate2 = level.getBlockState(blockpos1);
      BambooLeaves bambooleaves = BambooLeaves.NONE;
      if (i >= 1) {
         if (blockstate1.is(Blocks.BAMBOO) && blockstate1.getValue(LEAVES) != BambooLeaves.NONE) {
            if (blockstate1.is(Blocks.BAMBOO) && blockstate1.getValue(LEAVES) != BambooLeaves.NONE) {
               bambooleaves = BambooLeaves.LARGE;
               if (blockstate2.is(Blocks.BAMBOO)) {
                  level.setBlock(blockpos.below(), blockstate1.setValue(LEAVES, BambooLeaves.SMALL), 3);
                  level.setBlock(blockpos1, blockstate2.setValue(LEAVES, BambooLeaves.NONE), 3);
               }
            }
         } else {
            bambooleaves = BambooLeaves.SMALL;
         }
      }

      int j = blockstate.getValue(AGE) != 1 && !blockstate2.is(Blocks.BAMBOO) ? 0 : 1;
      int k = (i < 11 || !(randomsource.nextFloat() < 0.25F)) && i != 15 ? 0 : 1;
      level.setBlock(blockpos.above(), this.defaultBlockState().setValue(AGE, Integer.valueOf(j)).setValue(LEAVES, bambooleaves).setValue(STAGE, Integer.valueOf(k)), 3);
   }

   protected int getHeightAboveUpToMax(BlockGetter blockgetter, BlockPos blockpos) {
      int i;
      for(i = 0; i < 16 && blockgetter.getBlockState(blockpos.above(i + 1)).is(Blocks.BAMBOO); ++i) {
      }

      return i;
   }

   protected int getHeightBelowUpToMax(BlockGetter blockgetter, BlockPos blockpos) {
      int i;
      for(i = 0; i < 16 && blockgetter.getBlockState(blockpos.below(i + 1)).is(Blocks.BAMBOO); ++i) {
      }

      return i;
   }
}
