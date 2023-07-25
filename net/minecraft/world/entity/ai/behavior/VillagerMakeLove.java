package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class VillagerMakeLove extends Behavior<Villager> {
   private static final int INTERACT_DIST_SQR = 5;
   private static final float SPEED_MODIFIER = 0.5F;
   private long birthTimestamp;

   public VillagerMakeLove() {
      super(ImmutableMap.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT), 350, 350);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      return this.isBreedingPossible(villager);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return i <= this.birthTimestamp && this.isBreedingPossible(villager);
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      AgeableMob ageablemob = villager.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      BehaviorUtils.lockGazeAndWalkToEachOther(villager, ageablemob, 0.5F);
      serverlevel.broadcastEntityEvent(ageablemob, (byte)18);
      serverlevel.broadcastEntityEvent(villager, (byte)18);
      int j = 275 + villager.getRandom().nextInt(50);
      this.birthTimestamp = i + (long)j;
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      Villager villager1 = (Villager)villager.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      if (!(villager.distanceToSqr(villager1) > 5.0D)) {
         BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager1, 0.5F);
         if (i >= this.birthTimestamp) {
            villager.eatAndDigestFood();
            villager1.eatAndDigestFood();
            this.tryToGiveBirth(serverlevel, villager, villager1);
         } else if (villager.getRandom().nextInt(35) == 0) {
            serverlevel.broadcastEntityEvent(villager1, (byte)12);
            serverlevel.broadcastEntityEvent(villager, (byte)12);
         }

      }
   }

   private void tryToGiveBirth(ServerLevel serverlevel, Villager villager, Villager villager1) {
      Optional<BlockPos> optional = this.takeVacantBed(serverlevel, villager);
      if (!optional.isPresent()) {
         serverlevel.broadcastEntityEvent(villager1, (byte)13);
         serverlevel.broadcastEntityEvent(villager, (byte)13);
      } else {
         Optional<Villager> optional1 = this.breed(serverlevel, villager, villager1);
         if (optional1.isPresent()) {
            this.giveBedToChild(serverlevel, optional1.get(), optional.get());
         } else {
            serverlevel.getPoiManager().release(optional.get());
            DebugPackets.sendPoiTicketCountPacket(serverlevel, optional.get());
         }
      }

   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      villager.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
   }

   private boolean isBreedingPossible(Villager villager) {
      Brain<Villager> brain = villager.getBrain();
      Optional<AgeableMob> optional = brain.getMemory(MemoryModuleType.BREED_TARGET).filter((ageablemob) -> ageablemob.getType() == EntityType.VILLAGER);
      if (!optional.isPresent()) {
         return false;
      } else {
         return BehaviorUtils.targetIsValid(brain, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && villager.canBreed() && optional.get().canBreed();
      }
   }

   private Optional<BlockPos> takeVacantBed(ServerLevel serverlevel, Villager villager) {
      return serverlevel.getPoiManager().take((holder1) -> holder1.is(PoiTypes.HOME), (holder, blockpos) -> this.canReach(villager, blockpos, holder), villager.blockPosition(), 48);
   }

   private boolean canReach(Villager villager, BlockPos blockpos, Holder<PoiType> holder) {
      Path path = villager.getNavigation().createPath(blockpos, holder.value().validRange());
      return path != null && path.canReach();
   }

   private Optional<Villager> breed(ServerLevel serverlevel, Villager villager, Villager villager1) {
      Villager villager2 = villager.getBreedOffspring(serverlevel, villager1);
      if (villager2 == null) {
         return Optional.empty();
      } else {
         villager.setAge(6000);
         villager1.setAge(6000);
         villager2.setAge(-24000);
         villager2.moveTo(villager.getX(), villager.getY(), villager.getZ(), 0.0F, 0.0F);
         serverlevel.addFreshEntityWithPassengers(villager2);
         serverlevel.broadcastEntityEvent(villager2, (byte)12);
         return Optional.of(villager2);
      }
   }

   private void giveBedToChild(ServerLevel serverlevel, Villager villager, BlockPos blockpos) {
      GlobalPos globalpos = GlobalPos.of(serverlevel.dimension(), blockpos);
      villager.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
   }
}
