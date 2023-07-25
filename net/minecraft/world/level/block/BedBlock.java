package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ArrayUtils;

public class BedBlock extends HorizontalDirectionalBlock implements EntityBlock {
   public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
   public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;
   protected static final int HEIGHT = 9;
   protected static final VoxelShape BASE = Block.box(0.0D, 3.0D, 0.0D, 16.0D, 9.0D, 16.0D);
   private static final int LEG_WIDTH = 3;
   protected static final VoxelShape LEG_NORTH_WEST = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 3.0D, 3.0D);
   protected static final VoxelShape LEG_SOUTH_WEST = Block.box(0.0D, 0.0D, 13.0D, 3.0D, 3.0D, 16.0D);
   protected static final VoxelShape LEG_NORTH_EAST = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 3.0D, 3.0D);
   protected static final VoxelShape LEG_SOUTH_EAST = Block.box(13.0D, 0.0D, 13.0D, 16.0D, 3.0D, 16.0D);
   protected static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_NORTH_EAST);
   protected static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, LEG_SOUTH_WEST, LEG_SOUTH_EAST);
   protected static final VoxelShape WEST_SHAPE = Shapes.or(BASE, LEG_NORTH_WEST, LEG_SOUTH_WEST);
   protected static final VoxelShape EAST_SHAPE = Shapes.or(BASE, LEG_NORTH_EAST, LEG_SOUTH_EAST);
   private final DyeColor color;

   public BedBlock(DyeColor dyecolor, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.color = dyecolor;
      this.registerDefaultState(this.stateDefinition.any().setValue(PART, BedPart.FOOT).setValue(OCCUPIED, Boolean.valueOf(false)));
   }

   @Nullable
   public static Direction getBedOrientation(BlockGetter blockgetter, BlockPos blockpos) {
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      return blockstate.getBlock() instanceof BedBlock ? blockstate.getValue(FACING) : null;
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (level.isClientSide) {
         return InteractionResult.CONSUME;
      } else {
         if (blockstate.getValue(PART) != BedPart.HEAD) {
            blockpos = blockpos.relative(blockstate.getValue(FACING));
            blockstate = level.getBlockState(blockpos);
            if (!blockstate.is(this)) {
               return InteractionResult.CONSUME;
            }
         }

         if (!canSetSpawn(level)) {
            level.removeBlock(blockpos, false);
            BlockPos blockpos1 = blockpos.relative(blockstate.getValue(FACING).getOpposite());
            if (level.getBlockState(blockpos1).is(this)) {
               level.removeBlock(blockpos1, false);
            }

            Vec3 vec3 = blockpos.getCenter();
            level.explode((Entity)null, level.damageSources().badRespawnPointExplosion(vec3), (ExplosionDamageCalculator)null, vec3, 5.0F, true, Level.ExplosionInteraction.BLOCK);
            return InteractionResult.SUCCESS;
         } else if (blockstate.getValue(OCCUPIED)) {
            if (!this.kickVillagerOutOfBed(level, blockpos)) {
               player.displayClientMessage(Component.translatable("block.minecraft.bed.occupied"), true);
            }

            return InteractionResult.SUCCESS;
         } else {
            player.startSleepInBed(blockpos).ifLeft((player_bedsleepingproblem) -> {
               if (player_bedsleepingproblem.getMessage() != null) {
                  player.displayClientMessage(player_bedsleepingproblem.getMessage(), true);
               }

            });
            return InteractionResult.SUCCESS;
         }
      }
   }

   public static boolean canSetSpawn(Level level) {
      return level.dimensionType().bedWorks();
   }

   private boolean kickVillagerOutOfBed(Level level, BlockPos blockpos) {
      List<Villager> list = level.getEntitiesOfClass(Villager.class, new AABB(blockpos), LivingEntity::isSleeping);
      if (list.isEmpty()) {
         return false;
      } else {
         list.get(0).stopSleeping();
         return true;
      }
   }

   public void fallOn(Level level, BlockState blockstate, BlockPos blockpos, Entity entity, float f) {
      super.fallOn(level, blockstate, blockpos, entity, f * 0.5F);
   }

   public void updateEntityAfterFallOn(BlockGetter blockgetter, Entity entity) {
      if (entity.isSuppressingBounce()) {
         super.updateEntityAfterFallOn(blockgetter, entity);
      } else {
         this.bounceUp(entity);
      }

   }

   private void bounceUp(Entity entity) {
      Vec3 vec3 = entity.getDeltaMovement();
      if (vec3.y < 0.0D) {
         double d0 = entity instanceof LivingEntity ? 1.0D : 0.8D;
         entity.setDeltaMovement(vec3.x, -vec3.y * (double)0.66F * d0, vec3.z);
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == getNeighbourDirection(blockstate.getValue(PART), blockstate.getValue(FACING))) {
         return blockstate1.is(this) && blockstate1.getValue(PART) != blockstate.getValue(PART) ? blockstate.setValue(OCCUPIED, blockstate1.getValue(OCCUPIED)) : Blocks.AIR.defaultBlockState();
      } else {
         return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
      }
   }

   private static Direction getNeighbourDirection(BedPart bedpart, Direction direction) {
      return bedpart == BedPart.FOOT ? direction : direction.getOpposite();
   }

   public void playerWillDestroy(Level level, BlockPos blockpos, BlockState blockstate, Player player) {
      if (!level.isClientSide && player.isCreative()) {
         BedPart bedpart = blockstate.getValue(PART);
         if (bedpart == BedPart.FOOT) {
            BlockPos blockpos1 = blockpos.relative(getNeighbourDirection(bedpart, blockstate.getValue(FACING)));
            BlockState blockstate1 = level.getBlockState(blockpos1);
            if (blockstate1.is(this) && blockstate1.getValue(PART) == BedPart.HEAD) {
               level.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 35);
               level.levelEvent(player, 2001, blockpos1, Block.getId(blockstate1));
            }
         }
      }

      super.playerWillDestroy(level, blockpos, blockstate, player);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Direction direction = blockplacecontext.getHorizontalDirection();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      BlockPos blockpos1 = blockpos.relative(direction);
      Level level = blockplacecontext.getLevel();
      return level.getBlockState(blockpos1).canBeReplaced(blockplacecontext) && level.getWorldBorder().isWithinBounds(blockpos1) ? this.defaultBlockState().setValue(FACING, direction) : null;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      Direction direction = getConnectedDirection(blockstate).getOpposite();
      switch (direction) {
         case NORTH:
            return NORTH_SHAPE;
         case SOUTH:
            return SOUTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         default:
            return EAST_SHAPE;
      }
   }

   public static Direction getConnectedDirection(BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING);
      return blockstate.getValue(PART) == BedPart.HEAD ? direction.getOpposite() : direction;
   }

   public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockstate) {
      BedPart bedpart = blockstate.getValue(PART);
      return bedpart == BedPart.HEAD ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
   }

   private static boolean isBunkBed(BlockGetter blockgetter, BlockPos blockpos) {
      return blockgetter.getBlockState(blockpos.below()).getBlock() instanceof BedBlock;
   }

   public static Optional<Vec3> findStandUpPosition(EntityType<?> entitytype, CollisionGetter collisiongetter, BlockPos blockpos, Direction direction, float f) {
      Direction direction1 = direction.getClockWise();
      Direction direction2 = direction1.isFacingAngle(f) ? direction1.getOpposite() : direction1;
      if (isBunkBed(collisiongetter, blockpos)) {
         return findBunkBedStandUpPosition(entitytype, collisiongetter, blockpos, direction, direction2);
      } else {
         int[][] aint = bedStandUpOffsets(direction, direction2);
         Optional<Vec3> optional = findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos, aint, true);
         return optional.isPresent() ? optional : findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos, aint, false);
      }
   }

   private static Optional<Vec3> findBunkBedStandUpPosition(EntityType<?> entitytype, CollisionGetter collisiongetter, BlockPos blockpos, Direction direction, Direction direction1) {
      int[][] aint = bedSurroundStandUpOffsets(direction, direction1);
      Optional<Vec3> optional = findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos, aint, true);
      if (optional.isPresent()) {
         return optional;
      } else {
         BlockPos blockpos1 = blockpos.below();
         Optional<Vec3> optional1 = findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos1, aint, true);
         if (optional1.isPresent()) {
            return optional1;
         } else {
            int[][] aint1 = bedAboveStandUpOffsets(direction);
            Optional<Vec3> optional2 = findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos, aint1, true);
            if (optional2.isPresent()) {
               return optional2;
            } else {
               Optional<Vec3> optional3 = findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos, aint, false);
               if (optional3.isPresent()) {
                  return optional3;
               } else {
                  Optional<Vec3> optional4 = findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos1, aint, false);
                  return optional4.isPresent() ? optional4 : findStandUpPositionAtOffset(entitytype, collisiongetter, blockpos, aint1, false);
               }
            }
         }
      }
   }

   private static Optional<Vec3> findStandUpPositionAtOffset(EntityType<?> entitytype, CollisionGetter collisiongetter, BlockPos blockpos, int[][] aint, boolean flag) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int[] aint1 : aint) {
         blockpos_mutableblockpos.set(blockpos.getX() + aint1[0], blockpos.getY(), blockpos.getZ() + aint1[1]);
         Vec3 vec3 = DismountHelper.findSafeDismountLocation(entitytype, collisiongetter, blockpos_mutableblockpos, flag);
         if (vec3 != null) {
            return Optional.of(vec3);
         }
      }

      return Optional.empty();
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, PART, OCCUPIED);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new BedBlockEntity(blockpos, blockstate, this.color);
   }

   public void setPlacedBy(Level level, BlockPos blockpos, BlockState blockstate, @Nullable LivingEntity livingentity, ItemStack itemstack) {
      super.setPlacedBy(level, blockpos, blockstate, livingentity, itemstack);
      if (!level.isClientSide) {
         BlockPos blockpos1 = blockpos.relative(blockstate.getValue(FACING));
         level.setBlock(blockpos1, blockstate.setValue(PART, BedPart.HEAD), 3);
         level.blockUpdated(blockpos, Blocks.AIR);
         blockstate.updateNeighbourShapes(level, blockpos, 3);
      }

   }

   public DyeColor getColor() {
      return this.color;
   }

   public long getSeed(BlockState blockstate, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.relative(blockstate.getValue(FACING), blockstate.getValue(PART) == BedPart.HEAD ? 0 : 1);
      return Mth.getSeed(blockpos1.getX(), blockpos.getY(), blockpos1.getZ());
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return false;
   }

   private static int[][] bedStandUpOffsets(Direction direction, Direction direction1) {
      return ArrayUtils.addAll((int[][])bedSurroundStandUpOffsets(direction, direction1), (int[][])bedAboveStandUpOffsets(direction));
   }

   private static int[][] bedSurroundStandUpOffsets(Direction direction, Direction direction1) {
      return new int[][]{{direction1.getStepX(), direction1.getStepZ()}, {direction1.getStepX() - direction.getStepX(), direction1.getStepZ() - direction.getStepZ()}, {direction1.getStepX() - direction.getStepX() * 2, direction1.getStepZ() - direction.getStepZ() * 2}, {-direction.getStepX() * 2, -direction.getStepZ() * 2}, {-direction1.getStepX() - direction.getStepX() * 2, -direction1.getStepZ() - direction.getStepZ() * 2}, {-direction1.getStepX() - direction.getStepX(), -direction1.getStepZ() - direction.getStepZ()}, {-direction1.getStepX(), -direction1.getStepZ()}, {-direction1.getStepX() + direction.getStepX(), -direction1.getStepZ() + direction.getStepZ()}, {direction.getStepX(), direction.getStepZ()}, {direction1.getStepX() + direction.getStepX(), direction1.getStepZ() + direction.getStepZ()}};
   }

   private static int[][] bedAboveStandUpOffsets(Direction direction) {
      return new int[][]{{0, 0}, {-direction.getStepX(), -direction.getStepZ()}};
   }
}
