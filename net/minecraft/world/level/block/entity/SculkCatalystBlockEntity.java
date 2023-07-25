package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener.Holder<SculkCatalystBlockEntity.CatalystListener> {
   private final SculkCatalystBlockEntity.CatalystListener catalystListener;

   public SculkCatalystBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.SCULK_CATALYST, blockpos, blockstate);
      this.catalystListener = new SculkCatalystBlockEntity.CatalystListener(blockstate, new BlockPositionSource(blockpos));
   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, SculkCatalystBlockEntity sculkcatalystblockentity) {
      sculkcatalystblockentity.catalystListener.getSculkSpreader().updateCursors(level, blockpos, level.getRandom(), true);
   }

   public void load(CompoundTag compoundtag) {
      this.catalystListener.sculkSpreader.load(compoundtag);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      this.catalystListener.sculkSpreader.save(compoundtag);
      super.saveAdditional(compoundtag);
   }

   public SculkCatalystBlockEntity.CatalystListener getListener() {
      return this.catalystListener;
   }

   public static class CatalystListener implements GameEventListener {
      public static final int PULSE_TICKS = 8;
      final SculkSpreader sculkSpreader;
      private final BlockState blockState;
      private final PositionSource positionSource;

      public CatalystListener(BlockState blockstate, PositionSource positionsource) {
         this.blockState = blockstate;
         this.positionSource = positionsource;
         this.sculkSpreader = SculkSpreader.createLevelSpreader();
      }

      public PositionSource getListenerSource() {
         return this.positionSource;
      }

      public int getListenerRadius() {
         return 8;
      }

      public GameEventListener.DeliveryMode getDeliveryMode() {
         return GameEventListener.DeliveryMode.BY_DISTANCE;
      }

      public boolean handleGameEvent(ServerLevel serverlevel, GameEvent gameevent, GameEvent.Context gameevent_context, Vec3 vec3) {
         if (gameevent == GameEvent.ENTITY_DIE) {
            Entity i = gameevent_context.sourceEntity();
            if (i instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)i;
               if (!livingentity.wasExperienceConsumed()) {
                  int i = livingentity.getExperienceReward();
                  if (livingentity.shouldDropExperience() && i > 0) {
                     this.sculkSpreader.addCursors(BlockPos.containing(vec3.relative(Direction.UP, 0.5D)), i);
                     this.tryAwardItSpreadsAdvancement(serverlevel, livingentity);
                  }

                  livingentity.skipDropExperience();
                  this.positionSource.getPosition(serverlevel).ifPresent((vec31) -> this.bloom(serverlevel, BlockPos.containing(vec31), this.blockState, serverlevel.getRandom()));
               }

               return true;
            }
         }

         return false;
      }

      @VisibleForTesting
      public SculkSpreader getSculkSpreader() {
         return this.sculkSpreader;
      }

      private void bloom(ServerLevel serverlevel, BlockPos blockpos, BlockState blockstate, RandomSource randomsource) {
         serverlevel.setBlock(blockpos, blockstate.setValue(SculkCatalystBlock.PULSE, Boolean.valueOf(true)), 3);
         serverlevel.scheduleTick(blockpos, blockstate.getBlock(), 8);
         serverlevel.sendParticles(ParticleTypes.SCULK_SOUL, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 1.15D, (double)blockpos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
         serverlevel.playSound((Player)null, blockpos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + randomsource.nextFloat() * 0.4F);
      }

      private void tryAwardItSpreadsAdvancement(Level level, LivingEntity livingentity) {
         LivingEntity livingentity1 = livingentity.getLastHurtByMob();
         if (livingentity1 instanceof ServerPlayer serverplayer) {
            DamageSource damagesource = livingentity.getLastDamageSource() == null ? level.damageSources().playerAttack(serverplayer) : livingentity.getLastDamageSource();
            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverplayer, livingentity, damagesource);
         }

      }
   }
}
