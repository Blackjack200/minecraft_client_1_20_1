package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlock extends AbstractFurnaceBlock {
   protected SmokerBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public BlockEntity newBlockEntity(BlockPos blockpos, BlockState blockstate) {
      return new SmokerBlockEntity(blockpos, blockstate);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockstate, BlockEntityType<T> blockentitytype) {
      return createFurnaceTicker(level, blockentitytype, BlockEntityType.SMOKER);
   }

   protected void openContainer(Level level, BlockPos blockpos, Player player) {
      BlockEntity blockentity = level.getBlockEntity(blockpos);
      if (blockentity instanceof SmokerBlockEntity) {
         player.openMenu((MenuProvider)blockentity);
         player.awardStat(Stats.INTERACT_WITH_SMOKER);
      }

   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         double d0 = (double)blockpos.getX() + 0.5D;
         double d1 = (double)blockpos.getY();
         double d2 = (double)blockpos.getZ() + 0.5D;
         if (randomsource.nextDouble() < 0.1D) {
            level.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         level.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);
      }
   }
}
