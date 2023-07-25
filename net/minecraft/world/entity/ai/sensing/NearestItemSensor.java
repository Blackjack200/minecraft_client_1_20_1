package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;

public class NearestItemSensor extends Sensor<Mob> {
   private static final long XZ_RANGE = 32L;
   private static final long Y_RANGE = 16L;
   public static final int MAX_DISTANCE_TO_WANTED_ITEM = 32;

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
   }

   protected void doTick(ServerLevel serverlevel, Mob mob) {
      Brain<?> brain = mob.getBrain();
      List<ItemEntity> list = serverlevel.getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(32.0D, 16.0D, 32.0D), (itementity2) -> true);
      list.sort(Comparator.comparingDouble(mob::distanceToSqr));
      Optional<ItemEntity> optional = list.stream().filter((itementity1) -> mob.wantsToPickUp(itementity1.getItem())).filter((itementity) -> itementity.closerThan(mob, 32.0D)).filter(mob::hasLineOfSight).findFirst();
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, optional);
   }
}
