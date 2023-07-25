package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedstoneWallTorchBlock extends RedstoneTorchBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   protected RedstoneWallTorchBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(true)));
   }

   public String getDescriptionId() {
      return this.asItem().getDescriptionId();
   }

   public VoxelShape getShape(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, CollisionContext collisioncontext) {
      return WallTorchBlock.getShape(blockstate);
   }

   public boolean canSurvive(BlockState blockstate, LevelReader levelreader, BlockPos blockpos) {
      return Blocks.WALL_TORCH.canSurvive(blockstate, levelreader, blockpos);
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return Blocks.WALL_TORCH.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(blockplacecontext);
      return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         Direction direction = blockstate.getValue(FACING).getOpposite();
         double d0 = 0.27D;
         double d1 = (double)blockpos.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D + 0.27D * (double)direction.getStepX();
         double d2 = (double)blockpos.getY() + 0.7D + (randomsource.nextDouble() - 0.5D) * 0.2D + 0.22D;
         double d3 = (double)blockpos.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D + 0.27D * (double)direction.getStepZ();
         level.addParticle(this.flameParticle, d1, d2, d3, 0.0D, 0.0D, 0.0D);
      }
   }

   protected boolean hasNeighborSignal(Level level, BlockPos blockpos, BlockState blockstate) {
      Direction direction = blockstate.getValue(FACING).getOpposite();
      return level.hasSignal(blockpos.relative(direction), direction);
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(LIT) && blockstate.getValue(FACING) != direction ? 15 : 0;
   }

   public BlockState rotate(BlockState blockstate, Rotation rotation) {
      return Blocks.WALL_TORCH.rotate(blockstate, rotation);
   }

   public BlockState mirror(BlockState blockstate, Mirror mirror) {
      return Blocks.WALL_TORCH.mirror(blockstate, mirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, LIT);
   }
}
