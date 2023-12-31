package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class PlayerSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
   }

   protected void doTick(ServerLevel serverlevel, LivingEntity livingentity) {
      List<Player> list = serverlevel.players().stream().filter(EntitySelector.NO_SPECTATORS).filter((serverplayer) -> livingentity.closerThan(serverplayer, 16.0D)).sorted(Comparator.comparingDouble(livingentity::distanceToSqr)).collect(Collectors.toList());
      Brain<?> brain = livingentity.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_PLAYERS, list);
      List<Player> list1 = list.stream().filter((player1) -> isEntityTargetable(livingentity, player1)).collect(Collectors.toList());
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, list1.isEmpty() ? null : list1.get(0));
      Optional<Player> optional = list1.stream().filter((player) -> isEntityAttackable(livingentity, player)).findFirst();
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, optional);
   }
}
