package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.POTENTIAL_JOB_SITE), behaviorbuilder_instance.registered(MemoryModuleType.JOB_SITE)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, villager, i) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor);
               if (!globalpos.pos().closerToCenterThan(villager.position(), 2.0D) && !villager.assignProfessionWhenSpawned()) {
                  return false;
               } else {
                  memoryaccessor.erase();
                  memoryaccessor1.set(globalpos);
                  serverlevel.broadcastEntityEvent(villager, (byte)14);
                  if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
                     return true;
                  } else {
                     MinecraftServer minecraftserver = serverlevel.getServer();
                     Optional.ofNullable(minecraftserver.getLevel(globalpos.dimension())).flatMap((serverlevel2) -> serverlevel2.getPoiManager().getType(globalpos.pos())).flatMap((holder) -> BuiltInRegistries.VILLAGER_PROFESSION.stream().filter((villagerprofession1) -> villagerprofession1.heldJobSite().test(holder)).findFirst()).ifPresent((villagerprofession) -> {
                        villager.setVillagerData(villager.getVillagerData().setProfession(villagerprofession));
                        villager.refreshBrain(serverlevel);
                     });
                     return true;
                  }
               }
            }));
   }
}
