package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.mutable.MutableInt;

public class BellBlockEntity extends BlockEntity {
   private static final int DURATION = 50;
   private static final int GLOW_DURATION = 60;
   private static final int MIN_TICKS_BETWEEN_SEARCHES = 60;
   private static final int MAX_RESONATION_TICKS = 40;
   private static final int TICKS_BEFORE_RESONATION = 5;
   private static final int SEARCH_RADIUS = 48;
   private static final int HEAR_BELL_RADIUS = 32;
   private static final int HIGHLIGHT_RAIDERS_RADIUS = 48;
   private long lastRingTimestamp;
   public int ticks;
   public boolean shaking;
   public Direction clickDirection;
   private List<LivingEntity> nearbyEntities;
   private boolean resonating;
   private int resonationTicks;

   public BellBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.BELL, blockpos, blockstate);
   }

   public boolean triggerEvent(int i, int j) {
      if (i == 1) {
         this.updateEntities();
         this.resonationTicks = 0;
         this.clickDirection = Direction.from3DDataValue(j);
         this.ticks = 0;
         this.shaking = true;
         return true;
      } else {
         return super.triggerEvent(i, j);
      }
   }

   private static void tick(Level level, BlockPos blockpos, BlockState blockstate, BellBlockEntity bellblockentity, BellBlockEntity.ResonationEndAction bellblockentity_resonationendaction) {
      if (bellblockentity.shaking) {
         ++bellblockentity.ticks;
      }

      if (bellblockentity.ticks >= 50) {
         bellblockentity.shaking = false;
         bellblockentity.ticks = 0;
      }

      if (bellblockentity.ticks >= 5 && bellblockentity.resonationTicks == 0 && areRaidersNearby(blockpos, bellblockentity.nearbyEntities)) {
         bellblockentity.resonating = true;
         level.playSound((Player)null, blockpos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
      }

      if (bellblockentity.resonating) {
         if (bellblockentity.resonationTicks < 40) {
            ++bellblockentity.resonationTicks;
         } else {
            bellblockentity_resonationendaction.run(level, blockpos, bellblockentity.nearbyEntities);
            bellblockentity.resonating = false;
         }
      }

   }

   public static void clientTick(Level level, BlockPos blockpos, BlockState blockstate, BellBlockEntity bellblockentity) {
      tick(level, blockpos, blockstate, bellblockentity, BellBlockEntity::showBellParticles);
   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, BellBlockEntity bellblockentity) {
      tick(level, blockpos, blockstate, bellblockentity, BellBlockEntity::makeRaidersGlow);
   }

   public void onHit(Direction direction) {
      BlockPos blockpos = this.getBlockPos();
      this.clickDirection = direction;
      if (this.shaking) {
         this.ticks = 0;
      } else {
         this.shaking = true;
      }

      this.level.blockEvent(blockpos, this.getBlockState().getBlock(), 1, direction.get3DDataValue());
   }

   private void updateEntities() {
      BlockPos blockpos = this.getBlockPos();
      if (this.level.getGameTime() > this.lastRingTimestamp + 60L || this.nearbyEntities == null) {
         this.lastRingTimestamp = this.level.getGameTime();
         AABB aabb = (new AABB(blockpos)).inflate(48.0D);
         this.nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, aabb);
      }

      if (!this.level.isClientSide) {
         for(LivingEntity livingentity : this.nearbyEntities) {
            if (livingentity.isAlive() && !livingentity.isRemoved() && blockpos.closerToCenterThan(livingentity.position(), 32.0D)) {
               livingentity.getBrain().setMemory(MemoryModuleType.HEARD_BELL_TIME, this.level.getGameTime());
            }
         }
      }

   }

   private static boolean areRaidersNearby(BlockPos blockpos, List<LivingEntity> list) {
      for(LivingEntity livingentity : list) {
         if (livingentity.isAlive() && !livingentity.isRemoved() && blockpos.closerToCenterThan(livingentity.position(), 32.0D) && livingentity.getType().is(EntityTypeTags.RAIDERS)) {
            return true;
         }
      }

      return false;
   }

   private static void makeRaidersGlow(Level level1, BlockPos blockpos1, List<LivingEntity> list) {
      list.stream().filter((livingentity1) -> isRaiderWithinRange(blockpos1, livingentity1)).forEach(BellBlockEntity::glow);
   }

   private static void showBellParticles(Level level1, BlockPos blockpos1, List<LivingEntity> list) {
      MutableInt mutableint = new MutableInt(16700985);
      int i = (int)list.stream().filter((livingentity2) -> blockpos1.closerToCenterThan(livingentity2.position(), 48.0D)).count();
      list.stream().filter((livingentity1) -> isRaiderWithinRange(blockpos1, livingentity1)).forEach((livingentity) -> {
         float f = 1.0F;
         double d0 = Math.sqrt((livingentity.getX() - (double)blockpos1.getX()) * (livingentity.getX() - (double)blockpos1.getX()) + (livingentity.getZ() - (double)blockpos1.getZ()) * (livingentity.getZ() - (double)blockpos1.getZ()));
         double d1 = (double)((float)blockpos1.getX() + 0.5F) + 1.0D / d0 * (livingentity.getX() - (double)blockpos1.getX());
         double d2 = (double)((float)blockpos1.getZ() + 0.5F) + 1.0D / d0 * (livingentity.getZ() - (double)blockpos1.getZ());
         int k = Mth.clamp((i - 21) / -2, 3, 15);

         for(int l = 0; l < k; ++l) {
            int i1 = mutableint.addAndGet(5);
            double d3 = (double)FastColor.ARGB32.red(i1) / 255.0D;
            double d4 = (double)FastColor.ARGB32.green(i1) / 255.0D;
            double d5 = (double)FastColor.ARGB32.blue(i1) / 255.0D;
            level1.addParticle(ParticleTypes.ENTITY_EFFECT, d1, (double)((float)blockpos1.getY() + 0.5F), d2, d3, d4, d5);
         }

      });
   }

   private static boolean isRaiderWithinRange(BlockPos blockpos, LivingEntity livingentity) {
      return livingentity.isAlive() && !livingentity.isRemoved() && blockpos.closerToCenterThan(livingentity.position(), 48.0D) && livingentity.getType().is(EntityTypeTags.RAIDERS);
   }

   private static void glow(LivingEntity livingentity) {
      livingentity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
   }

   @FunctionalInterface
   interface ResonationEndAction {
      void run(Level level, BlockPos blockpos, List<LivingEntity> list);
   }
}
