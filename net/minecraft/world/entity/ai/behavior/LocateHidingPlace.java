package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class LocateHidingPlace {
   public static OneShot<LivingEntity> create(int i, float f, int j) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.HOME), behaviorbuilder_instance.registered(MemoryModuleType.HIDING_PLACE), behaviorbuilder_instance.registered(MemoryModuleType.PATH), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.BREED_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.INTERACTION_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3, memoryaccessor4, memoryaccessor5, memoryaccessor6) -> (serverlevel, livingentity, i2) -> {
               serverlevel.getPoiManager().find((holder1) -> holder1.is(PoiTypes.HOME), (blockpos3) -> true, livingentity.blockPosition(), j + 1, PoiManager.Occupancy.ANY).filter((blockpos2) -> blockpos2.closerToCenterThan(livingentity.position(), (double)j)).or(() -> serverlevel.getPoiManager().getRandom((holder) -> holder.is(PoiTypes.HOME), (blockpos1) -> true, PoiManager.Occupancy.ANY, livingentity.blockPosition(), i, livingentity.getRandom())).or(() -> behaviorbuilder_instance.<GlobalPos>tryGet(memoryaccessor1).map(GlobalPos::pos)).ifPresent((blockpos) -> {
                  memoryaccessor3.erase();
                  memoryaccessor4.erase();
                  memoryaccessor5.erase();
                  memoryaccessor6.erase();
                  memoryaccessor2.set(GlobalPos.of(serverlevel.dimension(), blockpos));
                  if (!blockpos.closerToCenterThan(livingentity.position(), (double)j)) {
                     memoryaccessor.set(new WalkTarget(blockpos, f, j));
                  }

               });
               return true;
            }));
   }
}
