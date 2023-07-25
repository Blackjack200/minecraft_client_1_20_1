package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class FollowTemptation extends Behavior<PathfinderMob> {
   public static final int TEMPTATION_COOLDOWN = 100;
   public static final double CLOSE_ENOUGH_DIST = 2.5D;
   private final Function<LivingEntity, Float> speedModifier;
   private final Function<LivingEntity, Double> closeEnoughDistance;

   public FollowTemptation(Function<LivingEntity, Float> function) {
      this(function, (livingentity) -> 2.5D);
   }

   public FollowTemptation(Function<LivingEntity, Float> function, Function<LivingEntity, Double> function1) {
      super(Util.make(() -> {
         ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> immutablemap_builder = ImmutableMap.builder();
         immutablemap_builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
         immutablemap_builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
         immutablemap_builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT);
         immutablemap_builder.put(MemoryModuleType.IS_TEMPTED, MemoryStatus.REGISTERED);
         immutablemap_builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_PRESENT);
         immutablemap_builder.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
         immutablemap_builder.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
         return immutablemap_builder.build();
      }));
      this.speedModifier = function;
      this.closeEnoughDistance = function1;
   }

   protected float getSpeedModifier(PathfinderMob pathfindermob) {
      return this.speedModifier.apply(pathfindermob);
   }

   private Optional<Player> getTemptingPlayer(PathfinderMob pathfindermob) {
      return pathfindermob.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
   }

   protected boolean timedOut(long i) {
      return false;
   }

   protected boolean canStillUse(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      return this.getTemptingPlayer(pathfindermob).isPresent() && !pathfindermob.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET) && !pathfindermob.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
   }

   protected void start(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      pathfindermob.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
   }

   protected void stop(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      Brain<?> brain = pathfindermob.getBrain();
      brain.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
      brain.setMemory(MemoryModuleType.IS_TEMPTED, false);
      brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel serverlevel, PathfinderMob pathfindermob, long i) {
      Player player = this.getTemptingPlayer(pathfindermob).get();
      Brain<?> brain = pathfindermob.getBrain();
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
      double d0 = this.closeEnoughDistance.apply(pathfindermob);
      if (pathfindermob.distanceToSqr(player) < Mth.square(d0)) {
         brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      } else {
         brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(player, false), this.getSpeedModifier(pathfindermob), 2));
      }

   }
}
