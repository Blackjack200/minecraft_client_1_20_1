package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PointedDripstoneBlock extends Block implements Fallable, SimpleWaterloggedBlock {
   public static final DirectionProperty TIP_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
   public static final EnumProperty<DripstoneThickness> THICKNESS = BlockStateProperties.DRIPSTONE_THICKNESS;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private static final int MAX_SEARCH_LENGTH_WHEN_CHECKING_DRIP_TYPE = 11;
   private static final int DELAY_BEFORE_FALLING = 2;
   private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK = 0.02F;
   private static final float DRIP_PROBABILITY_PER_ANIMATE_TICK_IF_UNDER_LIQUID_SOURCE = 0.12F;
   private static final int MAX_SEARCH_LENGTH_BETWEEN_STALACTITE_TIP_AND_CAULDRON = 11;
   private static final float WATER_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.17578125F;
   private static final float LAVA_TRANSFER_PROBABILITY_PER_RANDOM_TICK = 0.05859375F;
   private static final double MIN_TRIDENT_VELOCITY_TO_BREAK_DRIPSTONE = 0.6D;
   private static final float STALACTITE_DAMAGE_PER_FALL_DISTANCE_AND_SIZE = 1.0F;
   private static final int STALACTITE_MAX_DAMAGE = 40;
   private static final int MAX_STALACTITE_HEIGHT_FOR_DAMAGE_CALCULATION = 6;
   private static final float STALAGMITE_FALL_DISTANCE_OFFSET = 2.0F;
   private static final int STALAGMITE_FALL_DAMAGE_MODIFIER = 2;
   private static final float AVERAGE_DAYS_PER_GROWTH = 5.0F;
   private static final float GROWTH_PROBABILITY_PER_RANDOM_TICK = 0.011377778F;
   private static final int MAX_GROWTH_LENGTH = 7;
   private static final int MAX_STALAGMITE_SEARCH_RANGE_WHEN_GROWING = 10;
   private static final float STALACTITE_DRIP_START_PIXEL = 0.6875F;
   private static final VoxelShape TIP_MERGE_SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 16.0D, 11.0D);
   private static final VoxelShape TIP_SHAPE_UP = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 11.0D, 11.0D);
   private static final VoxelShape TIP_SHAPE_DOWN = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);
   private static final VoxelShape FRUSTUM_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
   private static final VoxelShape MIDDLE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   private static final VoxelShape BASE_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   private static final float MAX_HORIZONTAL_OFFSET = 0.125F;
   private static final VoxelShape REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);

   public PointedDripstoneBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.UP).setValue(THICKNESS, DripstoneThickness.TIP).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(TIP_DIRECTION, THICKNESS, WATERLOGGED);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return isValidPointedDripstonePlacement(levelreader, blockpos, blockstate.getValue(TIP_DIRECTION));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getValue(WATERLOGGED)) {
         levelaccessor.scheduleTick(blockpos, Fluids.WATER, Fluids.WATER.getTickDelay(levelaccessor));
      }

      if (direction != Direction.UP && direction != Direction.DOWN) {
         return blockstate;
      } else {
         Direction direction1 = blockstate.getValue(TIP_DIRECTION);
         if (direction1 == Direction.DOWN && levelaccessor.getBlockTicks().hasScheduledTick(blockpos, this)) {
            return blockstate;
         } else if (direction == direction1.getOpposite() && !this.canSurvive(blockstate, levelaccessor, blockpos)) {
            if (direction1 == Direction.DOWN) {
               levelaccessor.scheduleTick(blockpos, this, 2);
            } else {
               levelaccessor.scheduleTick(blockpos, this, 1);
            }

            return blockstate;
         } else {
            boolean flag = blockstate.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
            DripstoneThickness dripstonethickness = calculateDripstoneThickness(levelaccessor, blockpos, direction1, flag);
            return blockstate.setValue(THICKNESS, dripstonethickness);
         }
      }
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      BlockPos blockpos = blockhitresult.getBlockPos();
      if (!level.isClientSide && projectile.mayInteract(level, blockpos) && projectile instanceof ThrownTrident && projectile.getDeltaMovement().length() > 0.6D) {
         level.destroyBlock(blockpos, true);
      }

   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      if (blockstate.getValue(TIP_DIRECTION) == Direction.UP && blockstate.getValue(THICKNESS) == DripstoneThickness.TIP) {
         entity.causeFallDamage(f + 2.0F, 2.0F, level.damageSources().stalagmite());
      } else {
         super.fallOn(level, blockstate, blockpos, entity, f);
      }

   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (canDrip(blockstate)) {
         float f = randomsource.nextFloat();
         if (!(f > 0.12F)) {
            getFluidAboveStalactite(level, blockpos, blockstate).filter((pointeddripstoneblock_fluidinfo1) -> f < 0.02F || canFillCauldron(pointeddripstoneblock_fluidinfo1.fluid)).ifPresent((pointeddripstoneblock_fluidinfo) -> spawnDripParticle(level, blockpos, blockstate, pointeddripstoneblock_fluidinfo.fluid));
         }
      }
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (isStalagmite(blockstate) && !this.canSurvive(blockstate, serverlevel, blockpos)) {
         serverlevel.destroyBlock(blockpos, true);
      } else {
         spawnFallingStalactite(blockstate, serverlevel, blockpos);
      }

   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      maybeTransferFluid(blockstate, serverlevel, blockpos, randomsource.nextFloat());
      if (randomsource.nextFloat() < 0.011377778F && isStalactiteStartPos(blockstate, serverlevel, blockpos)) {
         growStalactiteOrStalagmiteIfPossible(blockstate, serverlevel, blockpos, randomsource);
      }

   }

   @VisibleForTesting
   public static void maybeTransferFluid(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, float f) {
      if (!(f > 0.17578125F) || !(f > 0.05859375F)) {
         if (isStalactiteStartPos(blockstate, serverlevel, blockpos)) {
            Optional<PointedDripstoneBlock.FluidInfo> optional = getFluidAboveStalactite(serverlevel, blockpos, blockstate);
            if (!optional.isEmpty()) {
               Fluid fluid = (optional.get()).fluid;
               float f1;
               if (fluid == Fluids.WATER) {
                  f1 = 0.17578125F;
               } else {
                  if (fluid != Fluids.LAVA) {
                     return;
                  }

                  f1 = 0.05859375F;
               }

               if (!(f >= f1)) {
                  BlockPos blockpos1 = findTip(blockstate, serverlevel, blockpos, 11, false);
                  if (blockpos1 != null) {
                     if ((optional.get()).sourceState.is(Blocks.MUD) && fluid == Fluids.WATER) {
                        BlockState blockstate1 = Blocks.CLAY.defaultBlockState();
                        serverlevel.setBlockAndUpdate((optional.get()).pos, blockstate1);
                        Block.pushEntitiesUp((optional.get()).sourceState, blockstate1, serverlevel, (optional.get()).pos);
                        serverlevel.gameEvent(GameEvent.BLOCK_CHANGE, (optional.get()).pos, GameEvent.Context.of(blockstate1));
                        serverlevel.levelEvent(1504, blockpos1, 0);
                     } else {
                        BlockPos blockpos2 = findFillableCauldronBelowStalactiteTip(serverlevel, blockpos1, fluid);
                        if (blockpos2 != null) {
                           serverlevel.levelEvent(1504, blockpos1, 0);
                           int i = blockpos1.getY() - blockpos2.getY();
                           int j = 50 + i;
                           BlockState blockstate2 = serverlevel.getBlockState(blockpos2);
                           serverlevel.scheduleTick(blockpos2, blockstate2.getBlock(), j);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      LevelAccessor levelaccessor = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      Direction direction = blockplacecontext.getNearestLookingVerticalDirection().getOpposite();
      Direction direction1 = calculateTipDirection(levelaccessor, blockpos, direction);
      if (direction1 == null) {
         return null;
      } else {
         boolean flag = !blockplacecontext.isSecondaryUseActive();
         DripstoneThickness dripstonethickness = calculateDripstoneThickness(levelaccessor, blockpos, direction1, flag);
         return dripstonethickness == null ? null : this.defaultBlockState().setValue(TIP_DIRECTION, direction1).setValue(THICKNESS, dripstonethickness).setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER));
      }
   }

   public FluidState getFluidState(BlockState blockstate) {
      return blockstate.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockstate);
   }

   public VoxelShape getOcclusionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return Shapes.empty();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      DripstoneThickness dripstonethickness = blockstate.getValue(THICKNESS);
      VoxelShape voxelshape;
      if (dripstonethickness == DripstoneThickness.TIP_MERGE) {
         voxelshape = TIP_MERGE_SHAPE;
      } else if (dripstonethickness == DripstoneThickness.TIP) {
         if (blockstate.getValue(TIP_DIRECTION) == Direction.DOWN) {
            voxelshape = TIP_SHAPE_DOWN;
         } else {
            voxelshape = TIP_SHAPE_UP;
         }
      } else if (dripstonethickness == DripstoneThickness.FRUSTUM) {
         voxelshape = FRUSTUM_SHAPE;
      } else if (dripstonethickness == DripstoneThickness.MIDDLE) {
         voxelshape = MIDDLE_SHAPE;
      } else {
         voxelshape = BASE_SHAPE;
      }

      Vec3 vec3 = blockstate.getOffset(blockgetter, blockpos);
      return voxelshape.move(vec3.x, 0.0D, vec3.z);
   }

   public boolean isCollisionShapeFullBlock(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return false;
   }

   public float getMaxHorizontalOffset() {
      return 0.125F;
   }

   public void onBrokenAfterFall(Level level, BlockPos blockpos, FallingBlockEntity fallingblockentity) {
      if (!fallingblockentity.isSilent()) {
         level.levelEvent(1045, blockpos, 0);
      }

   }

   public DamageSource getFallDamageSource(Entity entity) {
      return entity.damageSources().fallingStalactite(entity);
   }

   private static void spawnFallingStalactite(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(BlockState blockstate1 = blockstate; isStalactite(blockstate1); blockstate1 = serverlevel.getBlockState(blockpos_mutableblockpos)) {
         FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(serverlevel, blockpos_mutableblockpos, blockstate1);
         if (isTip(blockstate1, true)) {
            int i = Math.max(1 + blockpos.getY() - blockpos_mutableblockpos.getY(), 6);
            float f = 1.0F * (float)i;
            fallingblockentity.setHurtsEntities(f, 40);
            break;
         }

         blockpos_mutableblockpos.move(Direction.DOWN);
      }

   }

   @VisibleForTesting
   public static void growStalactiteOrStalagmiteIfPossible(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      BlockState blockstate1 = serverlevel.getBlockState(blockpos.above(1));
      BlockState blockstate2 = serverlevel.getBlockState(blockpos.above(2));
      if (canGrow(blockstate1, blockstate2)) {
         BlockPos blockpos1 = findTip(blockstate, serverlevel, blockpos, 7, false);
         if (blockpos1 != null) {
            BlockState blockstate3 = serverlevel.getBlockState(blockpos1);
            if (canDrip(blockstate3) && canTipGrow(blockstate3, serverlevel, blockpos1)) {
               if (randomsource.nextBoolean()) {
                  grow(serverlevel, blockpos1, Direction.DOWN);
               } else {
                  growStalagmiteBelow(serverlevel, blockpos1);
               }

            }
         }
      }
   }

   private static void growStalagmiteBelow(ServerLevel serverlevel, BlockPos blockpos) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int i = 0; i < 10; ++i) {
         blockpos_mutableblockpos.move(Direction.DOWN);
         BlockState blockstate = serverlevel.getBlockState(blockpos_mutableblockpos);
         if (!blockstate.getFluidState().isEmpty()) {
            return;
         }

         if (isUnmergedTipWithDirection(blockstate, Direction.UP) && canTipGrow(blockstate, serverlevel, blockpos_mutableblockpos)) {
            grow(serverlevel, blockpos_mutableblockpos, Direction.UP);
            return;
         }

         if (isValidPointedDripstonePlacement(serverlevel, blockpos_mutableblockpos, Direction.UP) && !serverlevel.isWaterAt(blockpos_mutableblockpos.below())) {
            grow(serverlevel, blockpos_mutableblockpos.below(), Direction.UP);
            return;
         }

         if (!canDripThrough(serverlevel, blockpos_mutableblockpos, blockstate)) {
            return;
         }
      }

   }

   private static void grow(ServerLevel serverlevel, BlockPos blockpos, Direction direction) {
      BlockPos blockpos1 = blockpos.relative(direction);
      BlockState blockstate = serverlevel.getBlockState(blockpos1);
      if (isUnmergedTipWithDirection(blockstate, direction.getOpposite())) {
         createMergedTips(blockstate, serverlevel, blockpos1);
      } else if (blockstate.isAir() || blockstate.is(Blocks.WATER)) {
         createDripstone(serverlevel, blockpos1, direction, DripstoneThickness.TIP);
      }

   }

   private static void createDripstone(LevelAccessor levelaccessor, BlockPos blockpos, Direction direction, DripstoneThickness dripstonethickness) {
      BlockState blockstate = Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(TIP_DIRECTION, direction).setValue(THICKNESS, dripstonethickness).setValue(WATERLOGGED, Boolean.valueOf(levelaccessor.getFluidState(blockpos).getType() == Fluids.WATER));
      levelaccessor.setBlock(blockpos, blockstate, 3);
   }

   private static void createMergedTips(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
      BlockPos blockpos2;
      BlockPos blockpos1;
      if (blockstate.getValue(TIP_DIRECTION) == Direction.UP) {
         blockpos1 = blockpos;
         blockpos2 = blockpos.above();
      } else {
         blockpos2 = blockpos;
         blockpos1 = blockpos.below();
      }

      createDripstone(levelaccessor, blockpos2, Direction.DOWN, DripstoneThickness.TIP_MERGE);
      createDripstone(levelaccessor, blockpos1, Direction.UP, DripstoneThickness.TIP_MERGE);
   }

   public static void spawnDripParticle(Level level, BlockPos blockpos, BlockState blockstate) {
      getFluidAboveStalactite(level, blockpos, blockstate).ifPresent((pointeddripstoneblock_fluidinfo) -> spawnDripParticle(level, blockpos, blockstate, pointeddripstoneblock_fluidinfo.fluid));
   }

   private static void spawnDripParticle(Level level, BlockPos blockpos, BlockState blockstate, Fluid fluid) {
      Vec3 vec3 = blockstate.getOffset(level, blockpos);
      double d0 = 0.0625D;
      double d1 = (double)blockpos.getX() + 0.5D + vec3.x;
      double d2 = (double)((float)(blockpos.getY() + 1) - 0.6875F) - 0.0625D;
      double d3 = (double)blockpos.getZ() + 0.5D + vec3.z;
      Fluid fluid1 = getDripFluid(level, fluid);
      ParticleOptions particleoptions = fluid1.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
      level.addParticle(particleoptions, d1, d2, d3, 0.0D, 0.0D, 0.0D);
   }

   @Nullable
   private static BlockPos findTip(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, int i, boolean flag) {
      if (isTip(blockstate, flag)) {
         return blockpos;
      } else {
         Direction direction = blockstate.getValue(TIP_DIRECTION);
         BiPredicate<BlockPos, BlockState> bipredicate = (blockpos1, blockstate2) -> blockstate2.is(Blocks.POINTED_DRIPSTONE) && blockstate2.getValue(TIP_DIRECTION) == direction;
         return findBlockVertical(levelaccessor, blockpos, direction.getAxisDirection(), bipredicate, (blockstate1) -> isTip(blockstate1, flag), i).orElse((BlockPos)null);
      }
   }

   @Nullable
   private static Direction calculateTipDirection(LevelReader levelreader, BlockPos blockpos, Direction direction) {
      Direction direction1;
      if (isValidPointedDripstonePlacement(levelreader, blockpos, direction)) {
         direction1 = direction;
      } else {
         if (!isValidPointedDripstonePlacement(levelreader, blockpos, direction.getOpposite())) {
            return null;
         }

         direction1 = direction.getOpposite();
      }

      return direction1;
   }

   private static DripstoneThickness calculateDripstoneThickness(LevelReader levelreader, BlockPos blockpos, Direction direction, boolean flag) {
      Direction direction1 = direction.getOpposite();
      BlockState blockstate = levelreader.getBlockState(blockpos.relative(direction));
      if (isPointedDripstoneWithDirection(blockstate, direction1)) {
         return !flag && blockstate.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
      } else if (!isPointedDripstoneWithDirection(blockstate, direction)) {
         return DripstoneThickness.TIP;
      } else {
         DripstoneThickness dripstonethickness = blockstate.getValue(THICKNESS);
         if (dripstonethickness != DripstoneThickness.TIP && dripstonethickness != DripstoneThickness.TIP_MERGE) {
            BlockState blockstate1 = levelreader.getBlockState(blockpos.relative(direction1));
            return !isPointedDripstoneWithDirection(blockstate1, direction) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
         } else {
            return DripstoneThickness.FRUSTUM;
         }
      }
   }

   public static boolean canDrip(BlockState blockstate) {
      return isStalactite(blockstate) && blockstate.getValue(THICKNESS) == DripstoneThickness.TIP && !blockstate.getValue(WATERLOGGED);
   }

   private static boolean canTipGrow(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos) {
      Direction direction = blockstate.getValue(TIP_DIRECTION);
      BlockPos blockpos1 = blockpos.relative(direction);
      BlockState blockstate1 = serverlevel.getBlockState(blockpos1);
      if (!blockstate1.getFluidState().isEmpty()) {
         return false;
      } else {
         return blockstate1.isAir() ? true : isUnmergedTipWithDirection(blockstate1, direction.getOpposite());
      }
   }

   private static Optional<BlockPos> findRootBlock(Level level, BlockPos blockpos, BlockState blockstate, int i) {
      Direction direction = blockstate.getValue(TIP_DIRECTION);
      BiPredicate<BlockPos, BlockState> bipredicate = (blockpos1, blockstate2) -> blockstate2.is(Blocks.POINTED_DRIPSTONE) && blockstate2.getValue(TIP_DIRECTION) == direction;
      return findBlockVertical(level, blockpos, direction.getOpposite().getAxisDirection(), bipredicate, (blockstate1) -> !blockstate1.is(Blocks.POINTED_DRIPSTONE), i);
   }

   private static boolean isValidPointedDripstonePlacement(LevelReader levelreader, BlockPos blockpos, Direction direction) {
      BlockPos blockpos1 = blockpos.relative(direction.getOpposite());
      BlockState blockstate = levelreader.getBlockState(blockpos1);
      return blockstate.isFaceSturdy(levelreader, blockpos1, direction) || isPointedDripstoneWithDirection(blockstate, direction);
   }

   private static boolean isTip(BlockState blockstate, boolean flag) {
      if (!blockstate.is(Blocks.POINTED_DRIPSTONE)) {
         return false;
      } else {
         DripstoneThickness dripstonethickness = blockstate.getValue(THICKNESS);
         return dripstonethickness == DripstoneThickness.TIP || flag && dripstonethickness == DripstoneThickness.TIP_MERGE;
      }
   }

   private static boolean isUnmergedTipWithDirection(BlockState blockstate, Direction direction) {
      return isTip(blockstate, false) && blockstate.getValue(TIP_DIRECTION) == direction;
   }

   private static boolean isStalactite(BlockState blockstate) {
      return isPointedDripstoneWithDirection(blockstate, Direction.DOWN);
   }

   private static boolean isStalagmite(BlockState blockstate) {
      return isPointedDripstoneWithDirection(blockstate, Direction.UP);
   }

   private static boolean isStalactiteStartPos(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return isStalactite(blockstate) && !levelreader.getBlockState(blockpos.above()).is(Blocks.POINTED_DRIPSTONE);
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   private static boolean isPointedDripstoneWithDirection(BlockState blockstate, Direction direction) {
      return blockstate.is(Blocks.POINTED_DRIPSTONE) && blockstate.getValue(TIP_DIRECTION) == direction;
   }

   @Nullable
   private static BlockPos findFillableCauldronBelowStalactiteTip(Level level, BlockPos blockpos, Fluid fluid) {
      Predicate<BlockState> predicate = (blockstate1) -> blockstate1.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)blockstate1.getBlock()).canReceiveStalactiteDrip(fluid);
      BiPredicate<BlockPos, BlockState> bipredicate = (blockpos1, blockstate) -> canDripThrough(level, blockpos1, blockstate);
      return findBlockVertical(level, blockpos, Direction.DOWN.getAxisDirection(), bipredicate, predicate, 11).orElse((BlockPos)null);
   }

   @Nullable
   public static BlockPos findStalactiteTipAboveCauldron(Level level, BlockPos blockpos) {
      BiPredicate<BlockPos, BlockState> bipredicate = (blockpos1, blockstate) -> canDripThrough(level, blockpos1, blockstate);
      return findBlockVertical(level, blockpos, Direction.UP.getAxisDirection(), bipredicate, PointedDripstoneBlock::canDrip, 11).orElse((BlockPos)null);
   }

   public static Fluid getCauldronFillFluidType(ServerLevel serverlevel, BlockPos blockpos) {
      return getFluidAboveStalactite(serverlevel, blockpos, serverlevel.getBlockState(blockpos)).map((pointeddripstoneblock_fluidinfo) -> pointeddripstoneblock_fluidinfo.fluid).filter(PointedDripstoneBlock::canFillCauldron).orElse(Fluids.EMPTY);
   }

   private static Optional<PointedDripstoneBlock.FluidInfo> getFluidAboveStalactite(Level level, BlockPos blockpos, BlockState blockstate) {
      return !isStalactite(blockstate) ? Optional.empty() : findRootBlock(level, blockpos, blockstate, 11).map((blockpos1) -> {
         BlockPos blockpos2 = blockpos1.above();
         BlockState blockstate1 = level.getBlockState(blockpos2);
         Fluid fluid;
         if (blockstate1.is(Blocks.MUD) && !level.dimensionType().ultraWarm()) {
            fluid = Fluids.WATER;
         } else {
            fluid = level.getFluidState(blockpos2).getType();
         }

         return new PointedDripstoneBlock.FluidInfo(blockpos2, fluid, blockstate1);
      });
   }

   private static boolean canFillCauldron(Fluid fluid) {
      return fluid == Fluids.LAVA || fluid == Fluids.WATER;
   }

   private static boolean canGrow(BlockState blockstate, BlockState blockstate1) {
      return blockstate.is(Blocks.DRIPSTONE_BLOCK) && blockstate1.is(Blocks.WATER) && blockstate1.getFluidState().isSource();
   }

   private static Fluid getDripFluid(Level level, Fluid fluid) {
      if (fluid.isSame(Fluids.EMPTY)) {
         return level.dimensionType().ultraWarm() ? Fluids.LAVA : Fluids.WATER;
      } else {
         return fluid;
      }
   }

   private static Optional<BlockPos> findBlockVertical(LevelAccessor levelaccessor, BlockPos blockpos, Direction.AxisDirection direction_axisdirection, BiPredicate<BlockPos, BlockState> bipredicate, Predicate<BlockState> predicate, int i) {
      Direction direction = Direction.get(direction_axisdirection, Direction.Axis.Y);
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int j = 1; j < i; ++j) {
         blockpos_mutableblockpos.move(direction);
         BlockState blockstate = levelaccessor.getBlockState(blockpos_mutableblockpos);
         if (predicate.test(blockstate)) {
            return Optional.of(blockpos_mutableblockpos.immutable());
         }

         if (levelaccessor.isOutsideBuildHeight(blockpos_mutableblockpos.getY()) || !bipredicate.test(blockpos_mutableblockpos, blockstate)) {
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static boolean canDripThrough(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.isAir()) {
         return true;
      } else if (blockstate.isSolidRender(blockgetter, blockpos)) {
         return false;
      } else if (!blockstate.getFluidState().isEmpty()) {
         return false;
      } else {
         VoxelShape voxelshape = blockstate.getCollisionShape(blockgetter, blockpos);
         return !Shapes.joinIsNotEmpty(REQUIRED_SPACE_TO_DRIP_THROUGH_NON_SOLID_BLOCK, voxelshape, BooleanOp.AND);
      }
   }

   static record FluidInfo(BlockPos pos, Fluid fluid, BlockState sourceState) {
      final BlockPos pos;
      final Fluid fluid;
      final BlockState sourceState;
   }
}
