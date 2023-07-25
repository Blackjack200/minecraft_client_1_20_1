package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CherryLeavesBlock extends LeavesBlock {
   public CherryLeavesBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      super.animateTick(blockstate, level, blockpos, randomsource);
      if (randomsource.nextInt(10) == 0) {
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = level.getBlockState(blockpos1);
         if (!isFaceFull(blockstate1.getCollisionShape(level, blockpos1), Direction.UP)) {
            ParticleUtils.spawnParticleBelow(level, blockpos, randomsource, ParticleTypes.CHERRY_LEAVES);
         }
      }
   }
}
