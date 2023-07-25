package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
   private static final RandomSource RANDOM = RandomSource.createThreadSafe();
   private static final int DEFAULT_SCAN_RATE = 20;
   protected static final int TARGETING_RANGE = 16;
   private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0D);
   private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat().range(16.0D).ignoreInvisibilityTesting();
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0D);
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().range(16.0D).ignoreInvisibilityTesting();
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight();
   private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0D).ignoreLineOfSight().ignoreInvisibilityTesting();
   private final int scanRate;
   private long timeToTick;

   public Sensor(int i) {
      this.scanRate = i;
      this.timeToTick = (long)RANDOM.nextInt(i);
   }

   public Sensor() {
      this(20);
   }

   public final void tick(ServerLevel serverlevel, E livingentity) {
      if (--this.timeToTick <= 0L) {
         this.timeToTick = (long)this.scanRate;
         this.doTick(serverlevel, livingentity);
      }

   }

   protected abstract void doTick(ServerLevel serverlevel, E livingentity);

   public abstract Set<MemoryModuleType<?>> requires();

   public static boolean isEntityTargetable(LivingEntity livingentity, LivingEntity livingentity1) {
      return livingentity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingentity1) ? TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(livingentity, livingentity1) : TARGET_CONDITIONS.test(livingentity, livingentity1);
   }

   public static boolean isEntityAttackable(LivingEntity livingentity, LivingEntity livingentity1) {
      return livingentity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingentity1) ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(livingentity, livingentity1) : ATTACK_TARGET_CONDITIONS.test(livingentity, livingentity1);
   }

   public static boolean isEntityAttackableIgnoringLineOfSight(LivingEntity livingentity, LivingEntity livingentity1) {
      return livingentity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingentity1) ? ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(livingentity, livingentity1) : ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(livingentity, livingentity1);
   }
}
