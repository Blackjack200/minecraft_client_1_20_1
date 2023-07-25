package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class TemptingSensor extends Sensor<PathfinderMob> {
   public static final int TEMPTATION_RANGE = 10;
   private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().range(10.0D).ignoreLineOfSight();
   private final Ingredient temptations;

   public TemptingSensor(Ingredient ingredient) {
      this.temptations = ingredient;
   }

   protected void doTick(ServerLevel serverlevel, PathfinderMob pathfindermob) {
      Brain<?> brain = pathfindermob.getBrain();
      List<Player> list = serverlevel.players().stream().filter(EntitySelector.NO_SPECTATORS).filter((serverplayer2) -> TEMPT_TARGETING.test(pathfindermob, serverplayer2)).filter((serverplayer1) -> pathfindermob.closerThan(serverplayer1, 10.0D)).filter(this::playerHoldingTemptation).filter((serverplayer) -> !pathfindermob.hasPassenger(serverplayer)).sorted(Comparator.comparingDouble(pathfindermob::distanceToSqr)).collect(Collectors.toList());
      if (!list.isEmpty()) {
         Player player = list.get(0);
         brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
      } else {
         brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
      }

   }

   private boolean playerHoldingTemptation(Player player1) {
      return this.isTemptation(player1.getMainHandItem()) || this.isTemptation(player1.getOffhandItem());
   }

   private boolean isTemptation(ItemStack itemstack) {
      return this.temptations.test(itemstack);
   }

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
   }
}
