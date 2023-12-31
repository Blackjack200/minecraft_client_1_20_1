package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AmethystBlock extends Block {
   public AmethystBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      if (!level.isClientSide) {
         BlockPos blockpos = blockhitresult.getBlockPos();
         level.playSound((Player)null, blockpos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 1.2F);
         level.playSound((Player)null, blockpos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 1.2F);
      }

   }
}
