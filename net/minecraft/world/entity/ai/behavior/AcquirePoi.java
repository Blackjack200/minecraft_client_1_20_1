package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class AcquirePoi {
   public static final int SCAN_RANGE = 48;

   public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memorymoduletype, boolean flag, Optional<Byte> optional) {
      return create(predicate, memorymoduletype, memorymoduletype, flag, optional);
   }

   public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> predicate, MemoryModuleType<GlobalPos> memorymoduletype, MemoryModuleType<GlobalPos> memorymoduletype1, boolean flag, Optional<Byte> optional) {
      int i = 5;
      int j = 20;
      MutableLong mutablelong = new MutableLong(0L);
      Long2ObjectMap<AcquirePoi.JitteredLinearRetry> long2objectmap = new Long2ObjectOpenHashMap<>();
      OneShot<PathfinderMob> oneshot = BehaviorBuilder.create((behaviorbuilder_instance1) -> behaviorbuilder_instance1.<MemoryAccessor>group(behaviorbuilder_instance1.absent(memorymoduletype1)).apply(behaviorbuilder_instance1, (memoryaccessor1) -> (serverlevel, pathfindermob, k) -> {
               if (flag && pathfindermob.isBaby()) {
                  return false;
               } else if (mutablelong.getValue() == 0L) {
                  mutablelong.setValue(serverlevel.getGameTime() + (long)serverlevel.random.nextInt(20));
                  return false;
               } else if (serverlevel.getGameTime() < mutablelong.getValue()) {
                  return false;
               } else {
                  mutablelong.setValue(k + 20L + (long)serverlevel.getRandom().nextInt(20));
                  PoiManager poimanager = serverlevel.getPoiManager();
                  long2objectmap.long2ObjectEntrySet().removeIf((long2objectmap_entry) -> !long2objectmap_entry.getValue().isStillValid(k));
                  Predicate<BlockPos> predicate4 = (blockpos4) -> {
                     AcquirePoi.JitteredLinearRetry acquirepoi_jitteredlinearretry = long2objectmap.get(blockpos4.asLong());
                     if (acquirepoi_jitteredlinearretry == null) {
                        return true;
                     } else if (!acquirepoi_jitteredlinearretry.shouldRetry(k)) {
                        return false;
                     } else {
                        acquirepoi_jitteredlinearretry.markAttempt(k);
                        return true;
                     }
                  };
                  Set<Pair<Holder<PoiType>, BlockPos>> set = poimanager.findAllClosestFirstWithType(predicate, predicate4, pathfindermob.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE).limit(5L).collect(Collectors.toSet());
                  Path path = findPathToPois(pathfindermob, set);
                  if (path != null && path.canReach()) {
                     BlockPos blockpos = path.getTarget();
                     poimanager.getType(blockpos).ifPresent((holder) -> {
                        poimanager.take(predicate, (holder1, blockpos3) -> blockpos3.equals(blockpos), blockpos, 1);
                        memoryaccessor1.set(GlobalPos.of(serverlevel.dimension(), blockpos));
                        optional.ifPresent((obyte) -> serverlevel.broadcastEntityEvent(pathfindermob, obyte));
                        long2objectmap.clear();
                        DebugPackets.sendPoiTicketCountPacket(serverlevel, blockpos);
                     });
                  } else {
                     for(Pair<Holder<PoiType>, BlockPos> pair : set) {
                        long2objectmap.computeIfAbsent(pair.getSecond().asLong(), (i1) -> new AcquirePoi.JitteredLinearRetry(serverlevel.random, k));
                     }
                  }

                  return true;
               }
            }));
      return memorymoduletype1 == memorymoduletype ? oneshot : BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor>group(behaviorbuilder_instance.absent(memorymoduletype)).apply(behaviorbuilder_instance, (memoryaccessor) -> oneshot));
   }

   @Nullable
   public static Path findPathToPois(Mob mob, Set<Pair<Holder<PoiType>, BlockPos>> set) {
      if (set.isEmpty()) {
         return null;
      } else {
         Set<BlockPos> set1 = new HashSet<>();
         int i = 1;

         for(Pair<Holder<PoiType>, BlockPos> pair : set) {
            i = Math.max(i, pair.getFirst().value().validRange());
            set1.add(pair.getSecond());
         }

         return mob.getNavigation().createPath(set1, i);
      }
   }

   static class JitteredLinearRetry {
      private static final int MIN_INTERVAL_INCREASE = 40;
      private static final int MAX_INTERVAL_INCREASE = 80;
      private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
      private final RandomSource random;
      private long previousAttemptTimestamp;
      private long nextScheduledAttemptTimestamp;
      private int currentDelay;

      JitteredLinearRetry(RandomSource randomsource, long i) {
         this.random = randomsource;
         this.markAttempt(i);
      }

      public void markAttempt(long i) {
         this.previousAttemptTimestamp = i;
         int j = this.currentDelay + this.random.nextInt(40) + 40;
         this.currentDelay = Math.min(j, 400);
         this.nextScheduledAttemptTimestamp = i + (long)this.currentDelay;
      }

      public boolean isStillValid(long i) {
         return i - this.previousAttemptTimestamp < 400L;
      }

      public boolean shouldRetry(long i) {
         return i >= this.nextScheduledAttemptTimestamp;
      }

      public String toString() {
         return "RetryMarker{, previousAttemptAt=" + this.previousAttemptTimestamp + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptTimestamp + ", currentDelay=" + this.currentDelay + "}";
      }
   }
}
