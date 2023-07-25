package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

public class ResetProfession {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.JOB_SITE)).apply(behaviorbuilder_instance, (memoryaccessor) -> (serverlevel, villager, i) -> {
               VillagerData villagerdata = villager.getVillagerData();
               if (villagerdata.getProfession() != VillagerProfession.NONE && villagerdata.getProfession() != VillagerProfession.NITWIT && villager.getVillagerXp() == 0 && villagerdata.getLevel() <= 1) {
                  villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE));
                  villager.refreshBrain(serverlevel);
                  return true;
               } else {
                  return false;
               }
            }));
   }
}
