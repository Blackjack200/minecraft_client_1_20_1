package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.JOB_SITE), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, villager, i) -> {
               GlobalPos globalpos = behaviorbuilder_instance.get(memoryaccessor);
               serverlevel.getPoiManager().getType(globalpos.pos()).ifPresent((holder) -> behaviorbuilder_instance.<List<LivingEntity>>get(memoryaccessor1).stream().filter((livingentity1) -> livingentity1 instanceof Villager && livingentity1 != villager).map((livingentity) -> (Villager)livingentity).filter(LivingEntity::isAlive).filter((villager2) -> competesForSameJobsite(globalpos, holder, villager2)).reduce(villager, PoiCompetitorScan::selectWinner));
               return true;
            }));
   }

   private static Villager selectWinner(Villager villager, Villager villager1) {
      Villager villager2;
      Villager villager3;
      if (villager.getVillagerXp() > villager1.getVillagerXp()) {
         villager2 = villager;
         villager3 = villager1;
      } else {
         villager2 = villager1;
         villager3 = villager;
      }

      villager3.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
      return villager2;
   }

   private static boolean competesForSameJobsite(GlobalPos globalpos, Holder<PoiType> holder, Villager villager) {
      Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      return optional.isPresent() && globalpos.equals(optional.get()) && hasMatchingProfession(holder, villager.getVillagerData().getProfession());
   }

   private static boolean hasMatchingProfession(Holder<PoiType> holder, VillagerProfession villagerprofession) {
      return villagerprofession.heldJobSite().test(holder);
   }
}
