package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCandleBlock extends Block {
   public static final int LIGHT_PER_CANDLE = 3;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   protected AbstractCandleBlock(BlockBehaviour.Properties blockbehaviour_properties) {
      super(blockbehaviour_properties);
   }

   protected abstract Iterable<Vec3> getParticleOffsets(BlockState blockstate);

   public static boolean isLit(BlockState blockstate) {
      return blockstate.hasProperty(LIT) && (blockstate.is(BlockTags.CANDLES) || blockstate.is(BlockTags.CANDLE_CAKES)) && blockstate.getValue(LIT);
   }

   public void onProjectileHit(Level level, BlockState blockstate, BlockHitResult blockhitresult, Projectile projectile) {
      if (!level.isClientSide && projectile.isOnFire() && this.canBeLit(blockstate)) {
         setLit(level, blockstate, blockhitresult.getBlockPos(), true);
      }

   }

   protected boolean canBeLit(BlockState blockstate) {
      return !blockstate.getValue(LIT);
   }

   public void animateTick(BlockState blockstate, Level level, BlockPos blockpos, RandomSource randomsource) {
      if (blockstate.getValue(LIT)) {
         this.getParticleOffsets(blockstate).forEach((vec3) -> addParticlesAndSound(level, vec3.add((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()), randomsource));
      }
   }

   private static void addParticlesAndSound(Level level, Vec3 vec3, RandomSource randomsource) {
      float f = randomsource.nextFloat();
      if (f < 0.3F) {
         level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0.0D, 0.0D, 0.0D);
         if (f < 0.17F) {
            level.playLocalSound(vec3.x + 0.5D, vec3.y + 0.5D, vec3.z + 0.5D, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + randomsource.nextFloat(), randomsource.nextFloat() * 0.7F + 0.3F, false);
         }
      }

      level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x, vec3.y, vec3.z, 0.0D, 0.0D, 0.0D);
   }

   public static void extinguish(@Nullable Player player, BlockState blockstate, LevelAccessor levelaccessor, BlockPos blockpos) {
      setLit(levelaccessor, blockstate, blockpos, false);
      if (blockstate.getBlock() instanceof AbstractCandleBlock) {
         ((AbstractCandleBlock)blockstate.getBlock()).getParticleOffsets(blockstate).forEach((vec3) -> levelaccessor.addParticle(ParticleTypes.SMOKE, (double)blockpos.getX() + vec3.x(), (double)blockpos.getY() + vec3.y(), (double)blockpos.getZ() + vec3.z(), 0.0D, (double)0.1F, 0.0D));
      }

      levelaccessor.playSound((Player)null, blockpos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
      levelaccessor.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
   }

   private static void setLit(LevelAccessor levelaccessor, BlockState blockstate, BlockPos blockpos, boolean flag) {
      levelaccessor.setBlock(blockpos, blockstate.setValue(LIT, Boolean.valueOf(flag)), 11);
   }
}
