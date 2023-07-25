package net.minecraft.world.entity.monster.piglin;

import java.util.List;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class StartHuntingHoglin {
   public static OneShot<Piglin> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN), behaviorbuilder_instance.absent(MemoryModuleType.ANGRY_AT), behaviorbuilder_instance.absent(MemoryModuleType.HUNTED_RECENTLY), behaviorbuilder_instance.registered(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, piglin, i) -> {
               if (!piglin.isBaby() && !behaviorbuilder_instance.<List>tryGet(memoryaccessor3).map((list1) -> list1.stream().anyMatch(StartHuntingHoglin::hasHuntedRecently)).isPresent()) {
                  Hoglin hoglin = behaviorbuilder_instance.get(memoryaccessor);
                  PiglinAi.setAngerTarget(piglin, hoglin);
                  PiglinAi.dontKillAnyMoreHoglinsForAWhile(piglin);
                  PiglinAi.broadcastAngerTarget(piglin, hoglin);
                  behaviorbuilder_instance.<List>tryGet(memoryaccessor3).ifPresent((list) -> list.forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile));
                  return true;
               } else {
                  return false;
               }
            }));
   }

   private static boolean hasHuntedRecently(AbstractPiglin abstractpiglin) {
      return abstractpiglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
   }
}
