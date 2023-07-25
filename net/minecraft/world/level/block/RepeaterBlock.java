package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RepeaterBlock extends DiodeBlock {
   public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
   public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

   protected RepeaterBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DELAY, Integer.valueOf(1)).setValue(LOCKED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState blockstate, Level level, BlockPos blockpos, Player player, InteractionHand interactionhand, BlockHitResult blockhitresult) {
      if (!player.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         level.setBlock(blockpos, blockstate.cycle(DELAY), 3);
         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   protected int getDelay(BlockState blockstate) {
      return blockstate.getValue(DELAY) * 2;
   }

   public BlockState getStateForPlacement(BlockPlaceContext blockplacecontext) {
      BlockState blockstate = super.getStateForPlacement(blockplacecontext);
      return blockstate.setValue(LOCKED, Boolean.valueOf(this.isLocked(blockplacecontext.getLevel(), blockplacecontext.getClickedPos(), blockstate)));
   }

   public BlockState updateShape(BlockState blockstate, Direction direction, BlockState blockstate1, LevelAccessor levelaccessor, BlockPos blockpos, BlockPos blockpos1) {
      return !levelaccessor.isClientSide() && direction.getAxis() != blockstate.getValue(FACING).getAxis() ? blockstate.setValue(LOCKED, Boolean.valueOf(this.isLocked(levelaccessor, blockpos, blockstate))) : super.updateShape(blockstate, direction, blockstate1, levelaccessor, blockpos, blockpos1);
   }

   public boolean isLocked(LevelReader levelreader, BlockPos blockpos, BlockState blockstate) {
      return this.getAlternateSignal(levelreader, blockpos, blockstate) > 0;
   }

   protected boolean sideInputDiodesOnly() {
      return true;
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(POWERED)) {
         Direction direction = blockstate.getValue(FACING);
         double d0 = (double)blockpos.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
         double d1 = (double)blockpos.getY() + 0.4D + (randomsource.nextDouble() - 0.5D) * 0.2D;
         double d2 = (double)blockpos.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
         float f = -5.0F;
         if (randomsource.nextBoolean()) {
            f = (float)(blockstate.getValue(DELAY) * 2 - 1);
         }

         f /= 16.0F;
         double d3 = (double)(f * (float)direction.getStepX());
         double d4 = (double)(f * (float)direction.getStepZ());
         level.addParticle(DustParticleOptions.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(FACING, DELAY, LOCKED, POWERED);
   }
}
