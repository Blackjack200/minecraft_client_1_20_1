package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden> extends Behavior<E> {
   public Digging(int i) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), i);
   }

   protected boolean canStillUse(ServerLevel serverlevel, E warden, long i) {
      return warden.getRemovalReason() == null;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, E warden) {
      return warden.onGround() || warden.isInWater() || warden.isInLava();
   }

   protected void start(ServerLevel serverlevel, E warden, long i) {
      if (warden.onGround()) {
         warden.setPose(Pose.DIGGING);
         warden.playSound(SoundEvents.WARDEN_DIG, 5.0F, 1.0F);
      } else {
         warden.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
         this.stop(serverlevel, warden, i);
      }

   }

   protected void stop(ServerLevel serverlevel, E warden, long i) {
      if (warden.getRemovalReason() == null) {
         warden.remove(Entity.RemovalReason.DISCARDED);
      }

   }
}
