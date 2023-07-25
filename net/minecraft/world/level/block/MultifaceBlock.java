package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MultifaceBlock extends Block {
   private static final float AABB_OFFSET = 1.0F;
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
   private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (enummap) -> {
      enummap.put(Direction.NORTH, NORTH_AABB);
      enummap.put(Direction.EAST, EAST_AABB);
      enummap.put(Direction.SOUTH, SOUTH_AABB);
      enummap.put(Direction.WEST, WEST_AABB);
      enummap.put(Direction.UP, UP_AABB);
      enummap.put(Direction.DOWN, DOWN_AABB);
   });
   protected static final Direction[] DIRECTIONS = Direction.values();
   private final ImmutableMap<BlockState, VoxelShape> shapesCache;
   private final boolean canRotate;
   private final boolean canMirrorX;
   private final boolean canMirrorZ;

   public MultifaceBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
      this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
      this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
      this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
      this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
   }

   public static Set<Direction> availableFaces(BlockState blockstate) {
      if (!(blockstate.getBlock() instanceof MultifaceBlock)) {
         return Set.of();
      } else {
         Set<Direction> set = EnumSet.noneOf(Direction.class);

         for(Direction direction : Direction.values()) {
            if (hasFace(blockstate, direction)) {
               set.add(direction);
            }
         }

         return set;
      }
   }

   public static Set<Direction> unpack(byte b0) {
      Set<Direction> set = EnumSet.noneOf(Direction.class);

      for(Direction direction : Direction.values()) {
         if ((b0 & (byte)(1 << direction.ordinal())) > 0) {
            set.add(direction);
         }
      }

      return set;
   }

   public static byte pack(Collection<Direction> collection) {
      byte b0 = 0;

      for(Direction direction : collection) {
         b0 = (byte)(b0 | 1 << direction.ordinal());
      }

      return b0;
   }

   protected boolean isFaceSupported(Direction direction) {
      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      for(Direction direction : DIRECTIONS) {
         if (this.isFaceSupported(direction)) {
            statedefinition_builder.add(getFaceProperty(direction));
         }
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (!hasAnyFace(blockstate)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return hasFace(blockstate, direction) && !canAttachTo(levelaccessor, direction, blockpos1, blockstate1) ? removeFace(blockstate, getFaceProperty(direction)) : blockstate;
      }
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return this.shapesCache.get(blockstate);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      boolean flag = false;

      for(Direction direction : DIRECTIONS) {
         if (hasFace(blockstate, direction)) {
            BlockPos blockpos1 = blockpos.relative(direction);
            if (!canAttachTo(levelreader, direction, blockpos1, levelreader.getBlockState(blockpos1))) {
               return false;
            }

            flag = true;
         }
      }

      return flag;
   }

   public boolean canBeReplaced(BlockState blockstate, BlockPlaceContext blockplacecontext) {
      return hasAnyVacantFace(blockstate);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      Level level = blockplacecontext.getLevel();
      BlockPos blockpos = blockplacecontext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      return Arrays.stream(blockplacecontext.getNearestLookingDirections()).map((direction) -> this.getStateForPlacement(blockstate, level, blockpos, direction)).filter(Objects::nonNull).findFirst().orElse((BlockState)null);
   }

   public boolean isValidStateForPlacement(BlockGetter blockgetter, BlockState blockstate, BlockPos blockpos, Direction direction) {
      if (this.isFaceSupported(direction) && (!blockstate.is(this) || !hasFace(blockstate, direction))) {
         BlockPos blockpos1 = blockpos.relative(direction);
         return canAttachTo(blockgetter, direction, blockpos1, blockgetter.getBlockState(blockpos1));
      } else {
         return false;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      if (!this.isValidStateForPlacement(blockgetter, blockstate, blockpos, direction)) {
         return null;
      } else {
         BlockState blockstate1;
         if (blockstate.is(this)) {
            blockstate1 = blockstate;
         } else if (this.isWaterloggable() && blockstate.getFluidState().isSourceOfType(Fluids.WATER)) {
            blockstate1 = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
         } else {
            blockstate1 = this.defaultBlockState();
         }

         return blockstate1.setValue(getFaceProperty(direction), Boolean.valueOf(true));
      }
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return !this.canRotate ? blockstate : this.mapDirections(blockstate, rotation::rotate);
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      if (mirror == Mirror.FRONT_BACK && !this.canMirrorX) {
         return blockstate;
      } else {
         return mirror == Mirror.LEFT_RIGHT && !this.canMirrorZ ? blockstate : this.mapDirections(blockstate, mirror::mirror);
      }
   }

   private BlockState mapDirections(BlockState blockstate, Function<Direction, Direction> function) {
      BlockState blockstate1 = blockstate;

      for(Direction direction : DIRECTIONS) {
         if (this.isFaceSupported(direction)) {
            blockstate1 = blockstate1.setValue(getFaceProperty(function.apply(direction)), blockstate.getValue(getFaceProperty(direction)));
         }
      }

      return blockstate1;
   }

   public static boolean hasFace(BlockState blockstate, Direction direction) {
      BooleanProperty booleanproperty = getFaceProperty(direction);
      return blockstate.hasProperty(booleanproperty) && blockstate.getValue(booleanproperty);
   }

   public static boolean canAttachTo(BlockGetter blockgetter, Direction direction, BlockPos blockpos, BlockState blockstate) {
      return Block.isFaceFull(blockstate.getBlockSupportShape(blockgetter, blockpos), direction.getOpposite()) || Block.isFaceFull(blockstate.getCollisionShape(blockgetter, blockpos), direction.getOpposite());
   }

   private boolean isWaterloggable() {
      return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
   }

   private static BlockState removeFace(BlockState blockstate, BooleanProperty booleanproperty) {
      BlockState blockstate1 = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
      return hasAnyFace(blockstate1) ? blockstate1 : Blocks.AIR.defaultBlockState();
   }

   public static BooleanProperty getFaceProperty(Direction direction) {
      return PROPERTY_BY_DIRECTION.get(direction);
   }

   private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> statedefinition) {
      BlockState blockstate = statedefinition.any();

      for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
         if (blockstate.hasProperty(booleanproperty)) {
            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
         }
      }

      return blockstate;
   }

   private static VoxelShape calculateMultifaceShape(BlockState blockstate) {
      VoxelShape voxelshape = Shapes.empty();

      for(Direction direction : DIRECTIONS) {
         if (hasFace(blockstate, direction)) {
            voxelshape = Shapes.or(voxelshape, SHAPE_BY_DIRECTION.get(direction));
         }
      }

      return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
   }

   protected static boolean hasAnyFace(BlockState blockstate) {
      return Arrays.stream(DIRECTIONS).anyMatch((direction) -> hasFace(blockstate, direction));
   }

   private static boolean hasAnyVacantFace(BlockState blockstate) {
      return Arrays.stream(DIRECTIONS).anyMatch((direction) -> !hasFace(blockstate, direction));
   }

   public abstract MultifaceSpreader getSpreader();
}
