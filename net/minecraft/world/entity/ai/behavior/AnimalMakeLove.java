package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove extends Behavior<Animal> {
   private static final int BREED_RANGE = 3;
   private static final int MIN_DURATION = 60;
   private static final int MAX_DURATION = 110;
   private final EntityType<? extends Animal> partnerType;
   private final float speedModifier;
   private long spawnChildAtTime;

   public AnimalMakeLove(EntityType<? extends Animal> entitytype, float f) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 110);
      this.partnerType = entitytype;
      this.speedModifier = f;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Animal animal) {
      return animal.isInLove() && this.findValidBreedPartner(animal).isPresent();
   }

   protected void start(ServerLevel serverlevel, Animal animal, long i) {
      Animal animal1 = this.findValidBreedPartner(animal).get();
      animal.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal1);
      animal1.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal);
      BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal1, this.speedModifier);
      int j = 60 + animal.getRandom().nextInt(50);
      this.spawnChildAtTime = i + (long)j;
   }

   protected boolean canStillUse(ServerLevel serverlevel, Animal animal, long i) {
      if (!this.hasBreedTargetOfRightType(animal)) {
         return false;
      } else {
         Animal animal1 = this.getBreedTarget(animal);
         return animal1.isAlive() && animal.canMate(animal1) && BehaviorUtils.entityIsVisible(animal.getBrain(), animal1) && i <= this.spawnChildAtTime;
      }
   }

   protected void tick(ServerLevel serverlevel, Animal animal, long i) {
      Animal animal1 = this.getBreedTarget(animal);
      BehaviorUtils.lockGazeAndWalkToEachOther(animal, animal1, this.speedModifier);
      if (animal.closerThan(animal1, 3.0D)) {
         if (i >= this.spawnChildAtTime) {
            animal.spawnChildFromBreeding(serverlevel, animal1);
            animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            animal1.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
         }

      }
   }

   protected void stop(ServerLevel serverlevel, Animal animal, long i) {
      animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
      animal.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      animal.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      this.spawnChildAtTime = 0L;
   }

   private Animal getBreedTarget(Animal animal) {
      return (Animal)animal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
   }

   private boolean hasBreedTargetOfRightType(Animal animal) {
      Brain<?> brain = animal.getBrain();
      return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) && brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
   }

   private Optional<? extends Animal> findValidBreedPartner(Animal animal) {
      return animal.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest((livingentity) -> {
         if (livingentity.getType() == this.partnerType && livingentity instanceof Animal animal2) {
            if (animal.canMate(animal2)) {
               return true;
            }
         }

         return false;
      }).map(Animal.class::cast);
   }
}
