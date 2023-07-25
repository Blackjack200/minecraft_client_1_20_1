package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class RedstoneTorchBlock extends TorchBlock {
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   private static final Map<BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES = new WeakHashMap<>();
   public static final int RECENT_TOGGLE_TIMER = 60;
   public static final int MAX_RECENT_TOGGLES = 8;
   public static final int RESTART_DELAY = 160;
   private static final int TOGGLE_DELAY = 2;

   protected RedstoneTorchBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties, DustParticleOptions.REDSTONE);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)));
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      for(Direction direction : Direction.values()) {
         level.updateNeighborsAt(blockpos.relative(direction), this);
      }

   }

   public void onRemove(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!flag) {
         for(Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockpos.relative(direction), this);
         }

      }
   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(LIT) && Direction.UP != direction ? 15 : 0;
   }

   protected boolean hasNeighborSignal(Level level, BlockPos blockpos, BlockState blockstate) {
      return level.hasSignal(blockpos.below(), Direction.DOWN);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      boolean flag = this.hasNeighborSignal(serverlevel, blockpos, blockstate);
      List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.get(serverlevel);

      while(list != null && !list.isEmpty() && serverlevel.getGameTime() - (list.get(0)).when > 60L) {
         list.remove(0);
      }

      if (blockstate.getValue(LIT)) {
         if (flag) {
            serverlevel.setBlock(blockpos, blockstate.setValue(LIT, Boolean.valueOf(false)), 3);
            if (isToggledTooFrequently(serverlevel, blockpos, true)) {
               serverlevel.levelEvent(1502, blockpos, 0);
               serverlevel.scheduleTick(blockpos, serverlevel.getBlockState(blockpos).getBlock(), 160);
            }
         }
      } else if (!flag && !isToggledTooFrequently(serverlevel, blockpos, false)) {
         serverlevel.setBlock(blockpos, blockstate.setValue(LIT, Boolean.valueOf(true)), 3);
      }

   }

   public void neighborChanged(BlockState blockstate, Level level, BlockPos blockpos, Block block, BlockPos blockpos1, boolean flag) {
      if (blockstate.getValue(LIT) == this.hasNeighborSignal(level, blockpos, blockstate) && !level.getBlockTicks().willTickThisTick(blockpos, this)) {
         level.scheduleTick(blockpos, this, 2);
      }

   }

   public int getDirectSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return direction == Direction.DOWN ? blockstate.getSignal(blockgetter, blockpos, direction) : 0;
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         double d0 = (double)blockpos.getX() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
         double d1 = (double)blockpos.getY() + 0.7D + (randomsource.nextDouble() - 0.5D) * 0.2D;
         double d2 = (double)blockpos.getZ() + 0.5D + (randomsource.nextDouble() - 0.5D) * 0.2D;
         level.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(LIT);
   }

   private static boolean isToggledTooFrequently(Level level, BlockPos blockpos, boolean flag) {
      List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.computeIfAbsent(level, (blockgetter) -> Lists.newArrayList());
      if (flag) {
         list.add(new RedstoneTorchBlock.Toggle(blockpos.immutable(), level.getGameTime()));
      }

      int i = 0;

      for(int j = 0; j < list.size(); ++j) {
         RedstoneTorchBlock.Toggle redstonetorchblock_toggle = list.get(j);
         if (redstonetorchblock_toggle.pos.equals(blockpos)) {
            ++i;
            if (i >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   public static class Toggle {
      final BlockPos pos;
      final long when;

      public Toggle(BlockPos blockpos, long i) {
         this.pos = blockpos;
         this.when = i;
      }
   }
}
