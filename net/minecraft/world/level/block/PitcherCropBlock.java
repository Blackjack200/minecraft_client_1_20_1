package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PitcherCropBlock extends DoublePlantBlock implements BonemealableBlock {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
   public static final int MAX_AGE = 4;
   private static final int DOUBLE_PLANT_AGE_INTERSECTION = 3;
   private static final int BONEMEAL_INCREASE = 1;
   private static final VoxelShape FULL_UPPER_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 15.0D, 13.0D);
   private static final VoxelShape FULL_LOWER_SHAPE = Block.box(3.0D, -1.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   private static final VoxelShape COLLISION_SHAPE_BULB = Block.box(5.0D, -1.0D, 5.0D, 11.0D, 3.0D, 11.0D);
   private static final VoxelShape COLLISION_SHAPE_CROP = Block.box(3.0D, -1.0D, 3.0D, 13.0D, 5.0D, 13.0D);
   private static final VoxelShape[] UPPER_SHAPE_BY_AGE = new VoxelShape[]{Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D), FULL_UPPER_SHAPE};
   private static final VoxelShape[] LOWER_SHAPE_BY_AGE = new VoxelShape[]{COLLISION_SHAPE_BULB, Block.box(3.0D, -1.0D, 3.0D, 13.0D, 14.0D, 13.0D), FULL_LOWER_SHAPE, FULL_LOWER_SHAPE, FULL_LOWER_SHAPE};

   public PitcherCropBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   private boolean isMaxAge(BlockState blockstate) {
      return blockstate.getValue(AGE) >= 4;
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(blockstate);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.defaultBlockState();
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return !blockstate.canSurvive(levelaccessor, blockpos) ? Blocks.AIR.defaultBlockState() : blockstate;
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      if (blockstate.getValue(AGE) == 0) {
         return COLLISION_SHAPE_BULB;
      } else {
         return blockstate.getValue(HALF) == DoubleBlockHalf.LOWER ? COLLISION_SHAPE_CROP : super.getCollisionShape(blockstate, blockgetter, blockpos, collisioncontext);
      }
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      if (!isLower(blockstate)) {
         return super.canSurvive(blockstate, levelreader, blockpos);
      } else {
         return this.mayPlaceOn(levelreader.getBlockState(blockpos.below()), levelreader, blockpos.below()) && sufficientLight(levelreader, blockpos) && (blockstate.getValue(AGE) < 3 || isUpper(levelreader.getBlockState(blockpos.above())));
      }
   }

   protected boolean mayPlaceOn(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return blockstate.is(Blocks.FARMLAND);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(AGE);
      super.createBlockStateDefinition(statedefinition_builder);
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return blockstate.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE_BY_AGE[Math.min(Math.abs(4 - (blockstate.getValue(AGE) + 1)), UPPER_SHAPE_BY_AGE.length - 1)] : LOWER_SHAPE_BY_AGE[blockstate.getValue(AGE)];
   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (entity instanceof Ravager && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         level.destroyBlock(blockpos, true, entity);
      }

      super.entityInside(blockstate, level, blockpos, entity);
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return false;
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, LivingEntity livingentity, ItemStack itemstack) {
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      float f = CropBlock.getGrowthSpeed(this, serverlevel, blockpos);
      boolean flag = randomsource.nextInt((int)(25.0F / f) + 1) == 0;
      if (flag) {
         this.grow(serverlevel, blockstate, blockpos, 1);
      }

   }

   private void grow(ServerLevel serverlevel, BlockState blockstate, BlockPos blockpos, int i) {
      int j = Math.min(blockstate.getValue(AGE) + i, 4);
      if (this.canGrow(serverlevel, blockpos, blockstate, j)) {
         serverlevel.setBlock(blockpos, blockstate.setValue(AGE, Integer.valueOf(j)), 2);
         if (j >= 3) {
            BlockPos blockpos1 = blockpos.above();
            serverlevel.setBlock(blockpos1, copyWaterloggedFrom(serverlevel, blockpos, this.defaultBlockState().setValue(AGE, Integer.valueOf(j)).setValue(HALF, DoubleBlockHalf.UPPER)), 3);
         }

      }
   }

   private static boolean canGrowInto(LevelReader levelreader, BlockPos blockpos) {
      BlockState blockstate = levelreader.getBlockState(blockpos);
      return blockstate.isAir() || blockstate.is(Blocks.PITCHER_CROP);
   }

   private static boolean sufficientLight(LevelReader levelreader, BlockPos blockpos) {
      return levelreader.getRawBrightness(blockpos, 0) >= 8 || levelreader.canSeeSky(blockpos);
   }

   private static boolean isLower(BlockState blockstate) {
      return blockstate.is(Blocks.PITCHER_CROP) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
   }

   private static boolean isUpper(BlockState blockstate) {
      return blockstate.is(Blocks.PITCHER_CROP) && blockstate.getValue(HALF) == DoubleBlockHalf.UPPER;
   }

   private boolean canGrow(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, int i) {
      return !this.isMaxAge(blockstate) && sufficientLight(levelreader, blockpos) && (i < 3 || canGrowInto(levelreader, blockpos.above()));
   }

   @Nullable
   private PitcherCropBlock.PosAndState getLowerHalf(LevelReader levelreader, BlockPos blockpos, BlockState blockstate) {
      if (isLower(blockstate)) {
         return new PitcherCropBlock.PosAndState(blockpos, blockstate);
      } else {
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = levelreader.getBlockState(blockpos1);
         return isLower(blockstate1) ? new PitcherCropBlock.PosAndState(blockpos1, blockstate1) : null;
      }
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      PitcherCropBlock.PosAndState pitchercropblock_posandstate = this.getLowerHalf(levelreader, blockpos, blockstate);
      return pitchercropblock_posandstate == null ? false : this.canGrow(levelreader, pitchercropblock_posandstate.pos, pitchercropblock_posandstate.state, pitchercropblock_posandstate.state.getValue(AGE) + 1);
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      PitcherCropBlock.PosAndState pitchercropblock_posandstate = this.getLowerHalf(serverlevel, blockpos, blockstate);
      if (pitchercropblock_posandstate != null) {
         this.grow(serverlevel, pitchercropblock_posandstate.state, pitchercropblock_posandstate.pos, 1);
      }
   }

   static record PosAndState(BlockPos pos, BlockState state) {
      final BlockPos pos;
      final BlockState state;
   }
}
