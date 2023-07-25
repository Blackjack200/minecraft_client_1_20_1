package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.phys.Vec3;

public class SonicBoom extends Behavior<Warden> {
   private static final int DISTANCE_XZ = 15;
   private static final int DISTANCE_Y = 20;
   private static final double KNOCKBACK_VERTICAL = 0.5D;
   private static final double KNOCKBACK_HORIZONTAL = 2.5D;
   public static final int COOLDOWN = 40;
   private static final int TICKS_BEFORE_PLAYING_SOUND = Mth.ceil(34.0D);
   private static final int DURATION = Mth.ceil(60.0F);

   public SonicBoom() {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryStatus.REGISTERED, MemoryModuleType.SONIC_BOOM_SOUND_DELAY, MemoryStatus.REGISTERED), DURATION);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Warden warden) {
      return warden.closerThan(warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get(), 15.0D, 20.0D);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Warden warden, long i) {
      return true;
   }

   protected void start(ServerLevel serverlevel, Warden warden, long i) {
      warden.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, (long)DURATION);
      warden.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_DELAY, Unit.INSTANCE, (long)TICKS_BEFORE_PLAYING_SOUND);
      serverlevel.broadcastEntityEvent(warden, (byte)62);
      warden.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 3.0F, 1.0F);
   }

   protected void tick(ServerLevel serverlevel, Warden warden, long i) {
      warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((livingentity2) -> warden.getLookControl().setLookAt(livingentity2.position()));
      if (!warden.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_DELAY) && !warden.getBrain().hasMemoryValue(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN)) {
         warden.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, Unit.INSTANCE, (long)(DURATION - TICKS_BEFORE_PLAYING_SOUND));
         warden.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).filter(warden::canTargetEntity).filter((livingentity1) -> warden.closerThan(livingentity1, 15.0D, 20.0D)).ifPresent((livingentity) -> {
            Vec3 vec3 = warden.position().add(0.0D, (double)1.6F, 0.0D);
            Vec3 vec31 = livingentity.getEyePosition().subtract(vec3);
            Vec3 vec32 = vec31.normalize();

            for(int j = 1; j < Mth.floor(vec31.length()) + 7; ++j) {
               Vec3 vec33 = vec3.add(vec32.scale((double)j));
               serverlevel.sendParticles(ParticleTypes.SONIC_BOOM, vec33.x, vec33.y, vec33.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            warden.playSound(SoundEvents.WARDEN_SONIC_BOOM, 3.0F, 1.0F);
            livingentity.hurt(serverlevel.damageSources().sonicBoom(warden), 10.0F);
            double d0 = 0.5D * (1.0D - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            double d1 = 2.5D * (1.0D - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            livingentity.push(vec32.x() * d1, vec32.y() * d0, vec32.z() * d1);
         });
      }
   }

   protected void stop(ServerLevel serverlevel, Warden warden, long i) {
      setCooldown(warden, 40);
   }

   public static void setCooldown(LivingEntity livingentity, int i) {
      livingentity.getBrain().setMemoryWithExpiry(MemoryModuleType.SONIC_BOOM_COOLDOWN, Unit.INSTANCE, (long)i);
   }
}
