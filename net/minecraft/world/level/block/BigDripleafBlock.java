package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BigDripleafBlock extends HorizontalDirectionalBlock implements BonemealableBlock, SimpleWaterloggedBlock {
   private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final EnumProperty<Tilt> TILT = BlockStateProperties.TILT;
   private static final int NO_TICK = -1;
   private static final Object2IntMap<Tilt> DELAY_UNTIL_NEXT_TILT_STATE = Util.make(new Object2IntArrayMap<>(), (object2intarraymap) -> {
      object2intarraymap.defaultReturnValue(-1);
      object2intarraymap.put(Tilt.UNSTABLE, 10);
      object2intarraymap.put(Tilt.PARTIAL, 10);
      object2intarraymap.put(Tilt.FULL, 100);
   });
   private static final int MAX_GEN_HEIGHT = 5;
   private static final int STEM_WIDTH = 6;
   private static final int ENTITY_DETECTION_MIN_Y = 11;
   private static final int LOWEST_LEAF_TOP = 13;
   private static final Map<Tilt, VoxelShape> LEAF_SHAPES = ImmutableMap.of(Tilt.NONE, Block.box(0.0D, 11.0D, 0.0D, 16.0D, 15.0D, 16.0D), Tilt.UNSTABLE, Block.box(0.0D, 11.0D, 0.0D, 16.0D, 15.0D, 16.0D), Tilt.PARTIAL, Block.box(0.0D, 11.0D, 0.0D, 16.0D, 13.0D, 16.0D), Tilt.FULL, Shapes.empty());
   private static final VoxelShape STEM_SLICER = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final Map<Direction, VoxelShape> STEM_SHAPES = ImmutableMap.of(Direction.NORTH, Shapes.joinUnoptimized(BigDripleafStemBlock.NORTH_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST), Direction.SOUTH, Shapes.joinUnoptimized(BigDripleafStemBlock.SOUTH_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST), Direction.EAST, Shapes.joinUnoptimized(BigDripleafStemBlock.EAST_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST), Direction.WEST, Shapes.joinUnoptimized(BigDripleafStemBlock.WEST_SHAPE, STEM_SLICER, BooleanOp.ONLY_FIRST));
   private final Map<BlockState, VoxelShape> shapesCache;

   protected BigDripleafBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH).setValue(TILT, Tilt.NONE));
      this.shapesCache = this.getShapeForEachState(BigDripleafBlock::calculateShape);
   }

   private static VoxelShape calculateShape(BlockState blockstate) {
      return Shapes.or(LEAF_SHAPES.get(blockstate.getValue(TILT)), STEM_SHAPES.get(blockstate.getValue(FACING)));
   }

   public static void placeWithRandomHeight(LevelAccessor levelaccessor, RandomSource randomsource, BlockPos blockpos, Direction direction) {
      int i = Mth.nextInt(randomsource, 2, 5);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();
      int j = 0;

      while(j < i && canPlaceAt(levelaccessor, blockpos_mutableblockpos, levelaccessor.getBlockState(blockpos_mutableblockpos))) {
         ++j;
         blockpos_mutableblockpos.move(Direction.UP);
      }

      int k = blockpos.getY() + j - 1;
      blockpos_mutableblockpos.setY(blockpos.getY());

      while(blockpos_mutableblockpos.getY() < k) {
         BigDripleafStemBlock.place(levelaccessor, blockpos_mutableblockpos, levelaccessor.getFluidState(blockpos_mutableblockpos), direction);
         blockpos_mutableblockpos.move(Direction.UP);
      }

      place(levelaccessor, blockpos_mutableblockpos, levelaccessor.getFluidState(blockpos_mutableblockpos), direction);
   }

   private static boolean canReplace(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(Blocks.WATER) || blockstate.is(Blocks.SMALL_DRIPLEAF);
   }

   protected static boolean canPlaceAt(LevelHeightAccessor levelheightaccessor, BlockPos blockpos, BlockState blockstate) {
      return !levelheightaccessor.isOutsideBuildHeight(blockpos) && canReplace(blockstate);
   }

   protected static boolean place(LevelAccessor levelaccessor, BlockPos blockpos, FluidState fluidstate, Direction direction) {
      BlockState blockstate = Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.isSourceOfType(Fluids.WATER))).setValue(FACING, direction);
      return levelaccessor.setBlock(blockpos, blockstate, 3);
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      this.setTiltAndScheduleTick(blockstate, level, blockhitresult.getBlockPos(), Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      return blockstate1.is(this) || blockstate1.is(Blocks.BIG_DRIPLEAF_STEM) || blockstate1.is(BlockTags.BIG_DRIPLEAF_PLACEABLE);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == Direction.DOWN && !blockstate.canSurvive(levelaccessor, blockpos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (blockstate.getValue(WATERLOGGED)) {
            levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
         }

         return direction == Direction.UP && blockstate1.is(this) ? Blocks.BIG_DRIPLEAF_STEM.withPropertiesOf(blockstate) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      }
   }

   public boolean isValidBonemealTarget(LevelReader levelreader, BlockPos blockpos, BlockState blockstate, boolean flag) {
      BlockState blockstate1 = levelreader.getBlockState(blockpos.above());
      return canReplace(blockstate1);
   }

   public boolean isBonemealSuccess(Level level, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      return true;
   }

   public void performBonemeal(ServerLevel serverlevel, RandomSource randomsource, BlockPos blockpos, BlockState blockstate) {
      BlockPos blockpos1 = blockpos.above();
      BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
      if (canPlaceAt(serverlevel, blockpos1, blockstate1)) {
         Direction direction = blockstate.getValue(FACING);
         BigDripleafStemBlock.place(serverlevel, blockpos, blockstate.getFluidState(), direction);
         place(serverlevel, blockpos1, blockstate1.getFluidState(), direction);
      }

   }

   public void entityInside(BlockState blockstate, Level level, BlockPos blockpos, Entity entity) {
      if (!level.isClientSide) {
         if (blockstate.getValue(TILT) == Tilt.NONE && canEntityTilt(blockpos, entity) && !level.hasNeighborSignal(blockpos)) {
            this.setTiltAndScheduleTick(blockstate, level, blockpos, Tilt.UNSTABLE, (SoundEvent)null);
         }

      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (serverlevel.hasNeighborSignal(blockpos)) {
         resetTilt(blockstate, serverlevel, blockpos);
      } else {
         Tilt tilt = blockstate.getValue(TILT);
         if (tilt == Tilt.UNSTABLE) {
            this.setTiltAndScheduleTick(blockstate, serverlevel, blockpos, Tilt.PARTIAL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
         } else if (tilt == Tilt.PARTIAL) {
            this.setTiltAndScheduleTick(blockstate, serverlevel, blockpos, Tilt.FULL, SoundEvents.BIG_DRIPLEAF_TILT_DOWN);
         } else if (tilt == Tilt.FULL) {
            resetTilt(blockstate, serverlevel, blockpos);
         }

      }
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (level.hasNeighborSignal(blockpos)) {
         resetTilt(blockstate, level, blockpos);
      }

   }

   private static void playTiltSound(Level level, BlockPos blockpos, SoundEvent soundevent) {
      float f = Mth.randomBetween(level.random, 0.8F, 1.2F);
      level.playSound((Player)null, blockpos, soundevent, SoundSource.BLOCKS, 1.0F, f);
   }

   private static boolean canEntityTilt(BlockPos blockpos, Entity entity) {
      return entity.onGround() && entity.position().y > (double)((float)blockpos.getY() + 0.6875F);
   }

   private void setTiltAndScheduleTick(BlockState blockstate, Level level, BlockPos blockpos, Tilt tilt, @Nullable SoundEvent soundevent) {
      setTilt(blockstate, level, blockpos, tilt);
      if (soundevent != null) {
         playTiltSound(level, blockpos, soundevent);
      }

      int i = DELAY_UNTIL_NEXT_TILT_STATE.getInt(tilt);
      if (i != -1) {
         level.scheduleTick(blockpos, this, i);
      }

   }

   private static void resetTilt(BlockState blockstate, Level level, BlockPos blockpos) {
      setTilt(blockstate, level, blockpos, Tilt.NONE);
      if (blockstate.getValue(TILT) != Tilt.NONE) {
         playTiltSound(level, blockpos, SoundEvents.BIG_DRIPLEAF_TILT_UP);
      }

   }

   private static void setTilt(BlockState blockstate, Level level, BlockPos blockpos, Tilt tilt) {
      Tilt tilt1 = blockstate.getValue(TILT);
      level.setBlock(blockpos, blockstate.setValue(TILT, tilt), 2);
      if (tilt.causesVibration() && tilt != tilt1) {
         level.gameEvent((Entity)null, GameEvent.BLOCK_CHANGE, blockpos);
      }

   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return LEAF_SHAPES.get(blockstate.getValue(TILT));
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapesCache.get(blockstate);
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = blockplacecontext.getLevel().getBlockState(blockplacecontext.getClickedPos().below());
      FluidState fluidstate = blockplacecontext.getLevel().getFluidState(blockplacecontext.getClickedPos());
      boolean flag = blockstate.is(Blocks.BIG_DRIPLEAF) || blockstate.is(Blocks.BIG_DRIPLEAF_STEM);
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.isSourceOfType(Fluids.WATER))).setValue(FACING, flag ? blockstate.getValue(FACING) : blockplacecontext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(WATERLOGGED, FACING, TILT);
   }
}
