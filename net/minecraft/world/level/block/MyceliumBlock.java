package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MyceliumBlock extends SpreadingSnowyDirtBlock {
   public MyceliumBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      super.animateTick(blockstate, level, blockpos, randomsource);
      if (randomsource.nextInt(10) == 0) {
         level.addParticle(ParticleTypes.MYCELIUM, (double)blockpos.getX() + randomsource.nextDouble(), (double)blockpos.getY() + 1.1D, (double)blockpos.getZ() + randomsource.nextDouble(), 0.0D, 0.0D, 0.0D);
      }

   }
}
