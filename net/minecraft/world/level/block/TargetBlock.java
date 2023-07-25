package net.minecraft.world.level.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock extends Block {
   private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;
   private static final int ACTIVATION_TICKS_ARROWS = 20;
   private static final int ACTIVATION_TICKS_OTHER = 8;

   public TargetBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(OUTPUT_POWER, Integer.valueOf(0)));
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      int i = updateRedstoneOutput(level, blockstate, blockhitresult, projectile);
      Entity entity = projectile.getOwner();
      if (entity instanceof ServerPlayer serverplayer) {
         serverplayer.awardStat(Stats.TARGET_HIT);
         CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverplayer, projectile, blockhitresult.getLocation(), i);
      }

   }

   private static int updateRedstoneOutput(LevelAccessor levelaccessor, BlockState blockstate, BlockHitResult blockhitresult, Entity entity) {
      int i = getRedstoneStrength(blockhitresult, blockhitresult.getLocation());
      int j = entity instanceof AbstractArrow ? 20 : 8;
      if (!levelaccessor.getBlockTicks().hasScheduledTick(blockhitresult.getBlockPos(), blockstate.getBlock())) {
         setOutputPower(levelaccessor, blockstate, i, blockhitresult.getBlockPos(), j);
      }

      return i;
   }

   private static int getRedstoneStrength(BlockHitResult blockhitresult, Vec3 vec3) {
      Direction direction = blockhitresult.getDirection();
      double d0 = Math.abs(Mth.frac(vec3.x) - 0.5D);
      double d1 = Math.abs(Mth.frac(vec3.y) - 0.5D);
      double d2 = Math.abs(Mth.frac(vec3.z) - 0.5D);
      Direction.Axis direction_axis = direction.getAxis();
      double d3;
      if (direction_axis == Direction.Axis.Y) {
         d3 = Math.max(d0, d2);
      } else if (direction_axis == Direction.Axis.Z) {
         d3 = Math.max(d0, d1);
      } else {
         d3 = Math.max(d1, d2);
      }

      return Math.max(1, Mth.ceil(15.0D * Mth.clamp((0.5D - d3) / 0.5D, 0.0D, 1.0D)));
   }

   private static void setOutputPower(LevelAccessor levelaccessor, BlockState blockstate, int i, BlockPos blockpos, int j) {
      levelaccessor.setBlock(blockpos, blockstate.setValue(OUTPUT_POWER, Integer.valueOf(i)), 3);
      levelaccessor.scheduleTick(blockpos, blockstate.getBlock(), j);
   }

   public void tick(BlockState blockstate, ServerLevel serverlevel, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(OUTPUT_POWER) != 0) {
         serverlevel.setBlock(blockpos, blockstate.setValue(OUTPUT_POWER, Integer.valueOf(0)), 3);
      }

   }

   public int getSignal(BlockState blockstate, BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      return blockstate.getValue(OUTPUT_POWER);
   }

   public boolean isSignalSource(BlockState blockstate) {
      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> statedefinition_builder) {
      statedefinition_builder.add(OUTPUT_POWER);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (!level.isClientSide() && !blockstate.is(blockstate1.getBlock())) {
         if (blockstate.getValue(OUTPUT_POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockpos, this)) {
            level.setBlock(blockpos, blockstate.setValue(OUTPUT_POWER, Integer.valueOf(0)), 18);
         }

      }
   }
}
