package net.minecraft.world.entity.ai.behavior;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class BackUpIfTooClose {
   public static OneShot<Mob> create(int i, float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, mob, i1) -> {
               LivingEntity livingentity = behaviorbuilder_instance.get(memoryaccessor2);
               if (livingentity.closerThan(mob, (double)i) && behaviorbuilder_instance.<NearestVisibleLivingEntities>get(memoryaccessor3).contains(livingentity)) {
                  memoryaccessor1.set(new EntityTracker(livingentity, true));
                  mob.getMoveControl().strafe(-f, 0.0F);
                  mob.setYRot(Mth.rotateIfNecessary(mob.getYRot(), mob.yHeadRot, 0.0F));
                  return true;
               } else {
                  return false;
               }
            }));
   }
}
