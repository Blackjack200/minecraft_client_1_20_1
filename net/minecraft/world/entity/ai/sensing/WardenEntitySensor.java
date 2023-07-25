package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor extends NearestLivingEntitySensor<Warden> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
   }

   protected void doTick(ServerLevel serverlevel, Warden warden) {
      super.doTick(serverlevel, warden);
      getClosest(warden, (livingentity2) -> livingentity2.getType() == EntityType.PLAYER).or(() -> getClosest(warden, (livingentity1) -> livingentity1.getType() != EntityType.PLAYER)).ifPresentOrElse((livingentity) -> warden.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, livingentity), () -> warden.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE));
   }

   private static Optional<LivingEntity> getClosest(Warden warden, Predicate<LivingEntity> predicate) {
      return warden.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).stream().flatMap(Collection::stream).filter(warden::canTargetEntity).filter(predicate).findFirst();
   }

   protected int radiusXZ() {
      return 24;
   }

   protected int radiusY() {
      return 24;
   }
}
