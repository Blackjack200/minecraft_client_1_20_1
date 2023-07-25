package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CryingObsidianBlock extends Block {
   public CryingObsidianBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (randomsource.nextInt(5) == 0) {
         Direction direction = Direction.getRandom(randomsource);
         if (direction != Direction.UP) {
            BlockPos blockpos1 = blockpos.relative(direction);
            BlockState blockstate1 = level.getBlockState(blockpos1);
            if (!blockstate.canOcclude() || !blockstate1.isFaceSturdy(level, blockpos1, direction.getOpposite())) {
               double d0 = direction.getStepX() == 0 ? randomsource.nextDouble() : 0.5D + (double)direction.getStepX() * 0.6D;
               double d1 = direction.getStepY() == 0 ? randomsource.nextDouble() : 0.5D + (double)direction.getStepY() * 0.6D;
               double d2 = direction.getStepZ() == 0 ? randomsource.nextDouble() : 0.5D + (double)direction.getStepZ() * 0.6D;
               level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double)blockpos.getX() + d0, (double)blockpos.getY() + d1, (double)blockpos.getZ() + d2, 0.0D, 0.0D, 0.0D);
            }
         }
      }
   }
}
