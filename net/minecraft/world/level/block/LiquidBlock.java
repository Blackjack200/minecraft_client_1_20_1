package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
   protected final FlowingFluid fluid;
   private final List<FluidState> stateCache;
   public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST);

   protected LiquidBlock(FlowingFluid flowingfluid, BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.fluid = flowingfluid;
      this.stateCache = Lists.newArrayList();
      this.stateCache.add(flowingfluid.getSource(false));

      for(int i = 1; i < 8; ++i) {
         this.stateCache.add(flowingfluid.getFlowing(8 - i, false));
      }

      this.stateCache.add(flowingfluid.getFlowing(8, true));
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
   }

   public VoxelShape getCollisionShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return collisioncontext.isAbove(STABLE_SHAPE, blockpos, true) && blockstate.getValue(LEVEL) == 0 && collisioncontext.canStandOnFluid(blockgetter.getFluidState(blockpos.above()), blockstate.getFluidState()) ? STABLE_SHAPE : Shapes.empty();
   }

   public boolean isRandomlyTicking(BlockState blockstate) {
      return blockstate.getFluidState().isRandomlyTicking();
   }

   public void randomTick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      blockstate.getFluidState().randomTick(serverlevel, blockpos, randomsource);
   }

   public boolean propagatesSkylightDown(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos) {
      return false;
   }

   public boolean isPathfindable(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, PathComputationType pathcomputationtype) {
      return !this.fluid.is(FluidTags.LAVA);
   }

   public FluidState getFluidState(BlockState blockstate) {
      int i = blockstate.getValue(LEVEL);
      return this.stateCache.get(Math.min(i, 8));
   }

   public boolean skipRendering(BlockState blockstate, BlockState blockstate1, Direction direction) {
      return blockstate1.getFluidState().getType().isSame(this.fluid);
   }

   public RenderShape getRenderShape(BlockState blockstate) {
      return RenderShape.INVISIBLE;
   }

   public List<ItemStack> getDrops(BlockState blockstate, LootParams.Builder lootparams_builder) {
      return Collections.emptyList();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return Shapes.empty();
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (this.shouldSpreadLiquid(level, blockpos, blockstate)) {
         level.scheduleTick(blockpos, blockstate.getFluidState().getType(), this.fluid.getTickDelay(level));
      }

   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      if (blockstate.getFluidState().isSource() || blockstate1.getFluidState().isSource()) {
         levelaccessor.scheduleTick(blockpos, blockstate.getFluidState().getType(), this.fluid.getTickDelay(levelaccessor));
      }

      return super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (this.shouldSpreadLiquid(level, blockpos, blockstate)) {
         level.scheduleTick(blockpos, blockstate.getFluidState().getType(), this.fluid.getTickDelay(level));
      }

   }

   private boolean shouldSpreadLiquid(Level level, BlockPos blockpos, BlockState blockstate) {
      if (this.fluid.is(FluidTags.LAVA)) {
         boolean flag = level.getBlockState(blockpos.below()).is(Blocks.SOUL_SOIL);

         for(Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
            BlockPos blockpos1 = blockpos.relative(direction.getOpposite());
            if (level.getFluidState(blockpos1).is(FluidTags.WATER)) {
               Block block = level.getFluidState(blockpos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
               level.setBlockAndUpdate(blockpos, block.defaultBlockState());
               this.fizz(level, blockpos);
               return false;
            }

            if (flag && level.getBlockState(blockpos1).is(Blocks.BLUE_ICE)) {
               level.setBlockAndUpdate(blockpos, Blocks.BASALT.defaultBlockState());
               this.fizz(level, blockpos);
               return false;
            }
         }
      }

      return true;
   }

   private void fizz(LevelAccessor levelaccessor, BlockPos blockpos) {
      levelaccessor.levelEvent(1501, blockpos, 0);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LEVEL);
   }

   public ItemStack pickupBlock(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate) {
      if (blockstate.getValue(LEVEL) == 0) {
         levelaccessor.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 11);
         return new ItemStack(this.fluid.getBucket());
      } else {
         return ItemStack.EMPTY;
      }
   }

   public Optional<SoundEvent> getPickupSound() {
      return this.fluid.getPickupSound();
   }
}
