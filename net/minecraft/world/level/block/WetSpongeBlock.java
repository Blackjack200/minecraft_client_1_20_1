package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock extends Block {
   protected WetSpongeBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void onPlace(BlockState blockstate, Level level, BlockPos blockpos, BlockState blockstate1, boolean flag) {
      if (level.dimensionType().ultraWarm()) {
         level.setBlock(blockpos, Blocks.SPONGE.defaultBlockState(), 3);
         level.levelEvent(2009, blockpos, 0);
         level.playSound((Player)null, blockpos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (1.0F + level.getRandom().nextFloat() * 0.2F) * 0.7F);
      }

   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      Direction direction = Direction.getRandom(randomsource);
      if (direction != Direction.UP) {
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate1 = level.getBlockState(blockpos1);
         if (!blockstate.canOcclude() || !blockstate1.isFaceSturdy(level, blockpos1, direction.getOpposite())) {
            double d0 = (double)blockpos.getX();
            double d1 = (double)blockpos.getY();
            double d2 = (double)blockpos.getZ();
            if (direction == Direction.DOWN) {
               d1 -= 0.05D;
               d0 += randomsource.nextDouble();
               d2 += randomsource.nextDouble();
            } else {
               d1 += randomsource.nextDouble() * 0.8D;
               if (direction.getAxis() == Direction.Axis.X) {
                  d2 += randomsource.nextDouble();
                  if (direction == Direction.EAST) {
                     ++d0;
                  } else {
                     d0 += 0.05D;
                  }
               } else {
                  d0 += randomsource.nextDouble();
                  if (direction == Direction.SOUTH) {
                     ++d2;
                  } else {
                     d2 += 0.05D;
                  }
               }
            }

            level.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }
   }
}
