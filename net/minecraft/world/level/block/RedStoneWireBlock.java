package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock extends Block {
   public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
   public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
   protected static final int H = 1;
   protected static final int W = 3;
   protected static final int E = 13;
   protected static final int N = 3;
   protected static final int S = 13;
   private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
   private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
   private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
   private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
   private static final Vec3[] COLORS = Util.make(new Vec3[16], (avec3) -> {
      for(int i = 0; i <= 15; ++i) {
         float f = (float)i / 15.0F;
         float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
         float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
         float f3 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
         avec3[i] = new Vec3((double)f1, (double)f2, (double)f3);
      }

   });
   private static final float PARTICLE_DENSITY = 0.2F;
   private final BlockState crossState;
   private boolean shouldSignal = true;

   public RedStoneWireBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
      this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

      for(BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
         if (blockstate.getValue(POWER) == 0) {
            SHAPES_CACHE.put(blockstate, this.calculateShape(blockstate));
         }
      }

   }

   private VoxelShape calculateShape(BlockState blockstate) {
      VoxelShape voxelshape = SHAPE_DOT;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = blockstate.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside == RedstoneSide.SIDE) {
            voxelshape = Shapes.or(voxelshape, SHAPES_FLOOR.get(direction));
         } else if (redstoneside == RedstoneSide.UP) {
            voxelshape = Shapes.or(voxelshape, SHAPES_UP.get(direction));
         }
      }

      return voxelshape;
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return SHAPES_CACHE.get(blockstate.setValue(POWER, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      return this.getConnectionState(blockplacecontext.getLevel(), this.crossState, blockplacecontext.getClickedPos());
   }

   private BlockState getConnectionState(BlockGetter blockgetter, BlockState blockstate, BlockPos blockpos) {
      boolean flag = isDot(blockstate);
      blockstate = this.getMissingConnections(blockgetter, this.defaultBlockState().setValue(POWER, blockstate.getValue(POWER)), blockpos);
      if (flag && isDot(blockstate)) {
         return blockstate;
      } else {
         boolean flag1 = blockstate.getValue(NORTH).isConnected();
         boolean flag2 = blockstate.getValue(SOUTH).isConnected();
         boolean flag3 = blockstate.getValue(EAST).isConnected();
         boolean flag4 = blockstate.getValue(WEST).isConnected();
         boolean flag5 = !flag1 && !flag2;
         boolean flag6 = !flag3 && !flag4;
         if (!flag4 && flag5) {
            blockstate = blockstate.setValue(WEST, RedstoneSide.SIDE);
         }

         if (!flag3 && flag5) {
            blockstate = blockstate.setValue(EAST, RedstoneSide.SIDE);
         }

         if (!flag1 && flag6) {
            blockstate = blockstate.setValue(NORTH, RedstoneSide.SIDE);
         }

         if (!flag2 && flag6) {
            blockstate = blockstate.setValue(SOUTH, RedstoneSide.SIDE);
         }

         return blockstate;
      }
   }

   private BlockState getMissingConnections(BlockGetter blockgetter, BlockState blockstate, BlockPos blockpos) {
      boolean flag = !blockgetter.getBlockState(blockpos.above()).isRedstoneConductor(blockgetter, blockpos);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!blockstate.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
            RedstoneSide redstoneside = this.getConnectingSide(blockgetter, blockpos, direction, flag);
            blockstate = blockstate.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
         }
      }

      return blockstate;
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (direction == Direction.DOWN) {
         return blockstate;
      } else if (direction == Direction.UP) {
         return this.getConnectionState(levelaccessor, blockstate, blockpos);
      } else {
         RedstoneSide redstoneside = this.getConnectingSide(levelaccessor, blockpos, direction);
         return redstoneside.isConnected() == blockstate.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && !isCross(blockstate) ? blockstate.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside) : this.getConnectionState(levelaccessor, this.crossState.setValue(POWER, blockstate.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside), blockpos);
      }
   }

   private static boolean isCross(BlockState blockstate) {
      return blockstate.getValue(NORTH).isConnected() && blockstate.getValue(SOUTH).isConnected() && blockstate.getValue(EAST).isConnected() && blockstate.getValue(WEST).isConnected();
   }

   private static boolean isDot(BlockState blockstate) {
      return !blockstate.getValue(NORTH).isConnected() && !blockstate.getValue(SOUTH).isConnected() && !blockstate.getValue(EAST).isConnected() && !blockstate.getValue(WEST).isConnected();
   }

   public void updateIndirectNeighbourShapes(BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos, int i, int j) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = blockstate.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside != RedstoneSide.NONE && !levelaccessor.getBlockState(blockpos_mutableblockpos.setWithOffset(blockpos, direction)).is(this)) {
            blockpos_mutableblockpos.move(Direction.DOWN);
            BlockState blockstate1 = levelaccessor.getBlockState(blockpos_mutableblockpos);
            if (blockstate1.is(this)) {
               BlockPos blockpos1 = blockpos_mutableblockpos.relative(direction.getOpposite());
               levelaccessor.neighborShapeChanged(direction.getOpposite(), levelaccessor.getBlockState(blockpos1), blockpos_mutableblockpos, blockpos1, i, j);
            }

            blockpos_mutableblockpos.setWithOffset(blockpos, direction).move(Direction.UP);
            BlockState blockstate2 = levelaccessor.getBlockState(blockpos_mutableblockpos);
            if (blockstate2.is(this)) {
               BlockPos blockpos2 = blockpos_mutableblockpos.relative(direction.getOpposite());
               levelaccessor.neighborShapeChanged(direction.getOpposite(), levelaccessor.getBlockState(blockpos2), blockpos_mutableblockpos, blockpos2, i, j);
            }
         }
      }

   }

   private RedstoneSide getConnectingSide(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return this.getConnectingSide(blockgetter, blockpos, direction, !blockgetter.getBlockState(blockpos.above()).isRedstoneConductor(blockgetter, blockpos));
   }

   private RedstoneSide getConnectingSide(BlockGetter blockgetter, BlockPos blockpos, Direction direction, boolean flag) {
      BlockPos blockpos1 = blockpos.relative(direction);
      BlockState blockstate = blockgetter.getBlockState(blockpos1);
      if (flag) {
         boolean flag1 = blockstate.getBlock() instanceof TrapDoorBlock || this.canSurviveOn(blockgetter, blockpos1, blockstate);
         if (flag1 && shouldConnectTo(blockgetter.getBlockState(blockpos1.above()))) {
            if (blockstate.isFaceSturdy(blockgetter, blockpos1, direction.getOpposite())) {
               return RedstoneSide.UP;
            }

            return RedstoneSide.SIDE;
         }
      }

      return !shouldConnectTo(blockstate, direction) && (blockstate.isRedstoneConductor(blockgetter, blockpos1) || !shouldConnectTo(blockgetter.getBlockState(blockpos1.below()))) ? RedstoneSide.NONE : RedstoneSide.SIDE;
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      BlockPos blockpos1 = blockpos.below();
      BlockState blockstate1 = levelreader.getBlockState(blockpos1);
      return this.canSurviveOn(levelreader, blockpos1, blockstate1);
   }

   private boolean canSurviveOn(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate) {
      return blockstate.isFaceSturdy(blockgetter, blockpos, Direction.UP) || blockstate.is(Blocks.HOPPER);
   }

   private void updatePowerStrength(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.calculateTargetStrength(level, blockpos);
      if (blockstate.getValue(POWER) != i) {
         if (level.getBlockState(blockpos) == blockstate) {
            level.setBlock(blockpos, blockstate.setValue(POWER, Integer.valueOf(i)), 2);
         }

         Set<BlockPos> set = Sets.newHashSet();
         set.add(blockpos);

         for(Direction direction : Direction.values()) {
            set.add(blockpos.relative(direction));
         }

         for(BlockPos blockpos1 : set) {
            level.updateNeighborsAt(blockpos1, this);
         }
      }

   }

   private int calculateTargetStrength(Level level, BlockPos blockpos) {
      this.shouldSignal = false;
      int i = level.getBestNeighborSignal(blockpos);
      this.shouldSignal = true;
      int j = 0;
      if (i < 15) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos1 = blockpos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos1);
            j = Math.max(j, this.getWireSignal(blockstate));
            BlockPos blockpos2 = blockpos.above();
            if (blockstate.isRedstoneConductor(level, blockpos1) && !level.getBlockState(blockpos2).isRedstoneConductor(level, blockpos2)) {
               j = Math.max(j, this.getWireSignal(level.getBlockState(blockpos1.above())));
            } else if (!blockstate.isRedstoneConductor(level, blockpos1)) {
               j = Math.max(j, this.getWireSignal(level.getBlockState(blockpos1.below())));
            }
         }
      }

      return Math.max(i, j - 1);
   }

   private int getWireSignal(BlockState blockstate) {
      return blockstate.is(this) ? blockstate.getValue(POWER) : 0;
   }

   private void checkCornerChangeAt(Level level, BlockPos blockpos) {
      if (level.getBlockState(blockpos).is(this)) {
         level.updateNeighborsAt(blockpos, this);

         for(Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockpos.relative(direction), this);
         }

      }
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!blockstate1.is(blockstate.getBlock()) && !level.isClientSide) {
         this.updatePowerStrength(level, blockpos, blockstate);

         for(Direction direction : Direction.Plane.VERTICAL) {
            level.updateNeighborsAt(blockpos.relative(direction), this);
         }

         this.updateNeighborsOfNeighboringWires(level, blockpos);
      }
   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag && !blockstate.is(blockstate1.getBlock())) {
         super.onRemove(blockstate, level, blockpos, blockstate1, flag);
         if (!level.isClientSide) {
            for(Direction direction : Direction.values()) {
               level.updateNeighborsAt(blockpos.relative(direction), this);
            }

            this.updatePowerStrength(level, blockpos, blockstate);
            this.updateNeighborsOfNeighboringWires(level, blockpos);
         }
      }
   }

   private void updateNeighborsOfNeighboringWires(Level level, BlockPos blockpos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         this.checkCornerChangeAt(level, blockpos.relative(direction));
      }

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction1);
         if (level.getBlockState(blockpos1).isRedstoneConductor(level, blockpos1)) {
            this.checkCornerChangeAt(level, blockpos1.above());
         } else {
            this.checkCornerChangeAt(level, blockpos1.below());
         }
      }

   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (!level.isClientSide) {
         if (blockstate.canSurvive(level, blockpos)) {
            this.updatePowerStrength(level, blockpos, blockstate);
         } else {
            dropResources(blockstate, level, blockpos);
            level.removeBlock(blockpos, false);
         }

      }
   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return !this.shouldSignal ? 0 : blockstate.getSignal(blockgetter, blockpos, direction);
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      if (this.shouldSignal && direction != Direction.DOWN) {
         int i = blockstate.getValue(POWER);
         if (i == 0) {
            return 0;
         } else {
            return direction != Direction.UP && !this.getConnectionState(blockgetter, blockstate, blockpos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite())).isConnected() ? 0 : i;
         }
      } else {
         return 0;
      }
   }

   protected static boolean shouldConnectTo(BlockState blockstate) {
      return shouldConnectTo(blockstate, (Direction)null);
   }

   protected static boolean shouldConnectTo(BlockState blockstate, @Nullable Direction direction) {
      if (blockstate.is(Blocks.REDSTONE_WIRE)) {
         return true;
      } else if (blockstate.is(Blocks.REPEATER)) {
         Direction direction1 = blockstate.getValue(RepeaterBlock.FACING);
         return direction1 == direction || direction1.getOpposite() == direction;
      } else if (blockstate.is(Blocks.OBSERVER)) {
         return direction == blockstate.getValue(ObserverBlock.FACING);
      } else {
         return blockstate.isSignalSource() && direction != null;
      }
   }

   public boolean isSignalSource(BlockState blockstate) {
      return this.shouldSignal;
   }

   public static int getColorForPower(int i) {
      Vec3 vec3 = COLORS[i];
      return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
   }

   private void spawnParticlesAlongLine(Level level, RandomSource randomsource, BlockPos blockpos, Vec3 vec3, Direction direction, Direction direction1, float f, float f1) {
      float f2 = f1 - f;
      if (!(randomsource.nextFloat() >= 0.2F * f2)) {
         float f3 = 0.4375F;
         float f4 = f + f2 * randomsource.nextFloat();
         double d0 = 0.5D + (double)(0.4375F * (float)direction.getStepX()) + (double)(f4 * (float)direction1.getStepX());
         double d1 = 0.5D + (double)(0.4375F * (float)direction.getStepY()) + (double)(f4 * (float)direction1.getStepY());
         double d2 = 0.5D + (double)(0.4375F * (float)direction.getStepZ()) + (double)(f4 * (float)direction1.getStepZ());
         level.addParticle(new DustParticleOptions(vec3.toVector3f(), 1.0F), (double)blockpos.getX() + d0, (double)blockpos.getY() + d1, (double)blockpos.getZ() + d2, 0.0D, 0.0D, 0.0D);
      }
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      int i = blockstate.getValue(POWER);
      if (i != 0) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = blockstate.getValue(PROPERTY_BY_DIRECTION.get(direction));
            switch (redstoneside) {
               case UP:
                  this.spawnParticlesAlongLine(level, randomsource, blockpos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
               case SIDE:
                  this.spawnParticlesAlongLine(level, randomsource, blockpos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
                  break;
               case NONE:
               default:
                  this.spawnParticlesAlongLine(level, randomsource, blockpos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
            }
         }

      }
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      switch (rotation) {
         case CLOCKWISE_180:
            return blockstate.setValue(NORTH, blockstate.getValue(SOUTH)).setValue(EAST, blockstate.getValue(WEST)).setValue(SOUTH, blockstate.getValue(NORTH)).setValue(WEST, blockstate.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return blockstate.setValue(NORTH, blockstate.getValue(EAST)).setValue(EAST, blockstate.getValue(SOUTH)).setValue(SOUTH, blockstate.getValue(WEST)).setValue(WEST, blockstate.getValue(NORTH));
         case CLOCKWISE_90:
            return blockstate.setValue(NORTH, blockstate.getValue(WEST)).setValue(EAST, blockstate.getValue(NORTH)).setValue(SOUTH, blockstate.getValue(EAST)).setValue(WEST, blockstate.getValue(SOUTH));
         default:
            return blockstate;
      }
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      switch (mirror) {
         case LEFT_RIGHT:
            return blockstate.setValue(NORTH, blockstate.getValue(SOUTH)).setValue(SOUTH, blockstate.getValue(NORTH));
         case FRONT_BACK:
            return blockstate.setValue(EAST, blockstate.getValue(WEST)).setValue(WEST, blockstate.getValue(EAST));
         default:
            return super.mirror(blockstate, mirror);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(NORTH, EAST, SOUTH, WEST, POWER);
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (!player.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         if (isCross(blockstate) || isDot(blockstate)) {
            BlockState blockstate1 = isCross(blockstate) ? this.defaultBlockState() : this.crossState;
            blockstate1 = blockstate1.setValue(POWER, blockstate.getValue(POWER));
            blockstate1 = this.getConnectionState(level, blockstate1, blockpos);
            if (blockstate1 != blockstate) {
               level.setBlock(blockpos, blockstate1, 3);
               this.updatesOnShapeChange(level, blockpos, blockstate, blockstate1);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      }
   }

   private void updatesOnShapeChange(Level level, BlockPos blockpos, BlockState blockstate, BlockState blockstate1) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         if (blockstate.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != blockstate1.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && level.getBlockState(blockpos1).isRedstoneConductor(level, blockpos1)) {
            level.updateNeighborsAtExceptFromFacing(blockpos1, blockstate1.getBlock(), direction.getOpposite());
         }
      }

   }
}
