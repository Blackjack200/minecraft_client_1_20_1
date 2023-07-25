package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
   public static BehaviorControl<Villager> create(float f) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.present(MemoryModuleType.POTENTIAL_JOB_SITE), behaviorbuilder_instance.absent(MemoryModuleType.JOB_SITE), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES), behaviorbuilder_instance.registered(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3, memoryaccessor4) -> (serverlevel, villager, i) -> {
               if (villager.isBaby()) {
                  return false;
               } else if (villager.getVillagerData().getProfession() != VillagerProfession.NONE) {
                  return false;
               } else {
                  BlockPos blockpos = behaviorbuilder_instance.<GlobalPos>get(memoryaccessor).pos();
                  Optional<Holder<PoiType>> optional = serverlevel.getPoiManager().getType(blockpos);
                  if (optional.isEmpty()) {
                     return true;
                  } else {
                     behaviorbuilder_instance.<List<LivingEntity>>get(memoryaccessor2).stream().filter((livingentity1) -> livingentity1 instanceof Villager && livingentity1 != villager).map((livingentity) -> (Villager)livingentity).filter(LivingEntity::isAlive).filter((villager2) -> nearbyWantsJobsite(optional.get(), villager2, blockpos)).findFirst().ifPresent((villager1) -> {
                        memoryaccessor3.erase();
                        memoryaccessor4.erase();
                        memoryaccessor.erase();
                        if (villager1.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                           BehaviorUtils.setWalkAndLookTargetMemories(villager1, blockpos, f, 1);
                           villager1.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(serverlevel.dimension(), blockpos));
                           DebugPackets.sendPoiTicketCountPacket(serverlevel, blockpos);
                        }

                     });
                     return true;
                  }
               }
            }));
   }

   private static boolean nearbyWantsJobsite(Holder<PoiType> holder, Villager villager, BlockPos blockpos) {
      boolean flag = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
      if (flag) {
         return false;
      } else {
         Optional<GlobalPos> optional = villager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
         VillagerProfession villagerprofession = villager.getVillagerData().getProfession();
         if (villagerprofession.heldJobSite().test(holder)) {
            return optional.isEmpty() ? canReachPos(villager, blockpos, holder.value()) : optional.get().pos().equals(blockpos);
         } else {
            return false;
         }
      }
   }

   private static boolean canReachPos(PathfinderMob pathfindermob, BlockPos blockpos, PoiType poitype) {
      Path path = pathfindermob.getNavigation().createPath(blockpos, poitype.validRange());
      return path != null && path.canReach();
   }
}
