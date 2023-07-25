package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

public class SetClosestHomeAsWalkTarget {
   private static final int CACHE_TIMEOUT = 40;
   private static final int BATCH_SIZE = 5;
   private static final int RATE = 20;
   private static final int OK_DISTANCE_SQR = 4;

   public static BehaviorControl<PathfinderMob> create(float f) {
      Long2LongMap long2longmap = new Long2LongOpenHashMap();
      MutableLong mutablelong = new MutableLong(0L);
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.absent(MemoryModuleType.WALK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.HOME)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1) -> (serverlevel, pathfindermob, i) -> {
               if (serverlevel.getGameTime() - mutablelong.getValue() < 20L) {
                  return false;
               } else {
                  PoiManager poimanager = serverlevel.getPoiManager();
                  Optional<BlockPos> optional = poimanager.findClosest((holder1) -> holder1.is(PoiTypes.HOME), pathfindermob.blockPosition(), 48, PoiManager.Occupancy.ANY);
                  if (!optional.isEmpty() && !(optional.get().distSqr(pathfindermob.blockPosition()) <= 4.0D)) {
                     MutableInt mutableint = new MutableInt(0);
                     mutablelong.setValue(serverlevel.getGameTime() + (long)serverlevel.getRandom().nextInt(20));
                     Predicate<BlockPos> predicate = (blockpos1) -> {
                        long j = blockpos1.asLong();
                        if (long2longmap.containsKey(j)) {
                           return false;
                        } else if (mutableint.incrementAndGet() >= 5) {
                           return false;
                        } else {
                           long2longmap.put(j, mutablelong.getValue() + 40L);
                           return true;
                        }
                     };
                     Set<Pair<Holder<PoiType>, BlockPos>> set = poimanager.findAllWithType((holder) -> holder.is(PoiTypes.HOME), predicate, pathfindermob.blockPosition(), 48, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
                     Path path = AcquirePoi.findPathToPois(pathfindermob, set);
                     if (path != null && path.canReach()) {
                        BlockPos blockpos = path.getTarget();
                        Optional<Holder<PoiType>> optional1 = poimanager.getType(blockpos);
                        if (optional1.isPresent()) {
                           memoryaccessor.set(new WalkTarget(blockpos, f, 1));
                           DebugPackets.sendPoiTicketCountPacket(serverlevel, blockpos);
                        }
                     } else if (mutableint.getValue() < 5) {
                        long2longmap.long2LongEntrySet().removeIf((long2longmap_entry) -> long2longmap_entry.getLongValue() < mutablelong.getValue());
                     }

                     return true;
                  } else {
                     return false;
                  }
               }
            }));
   }
}
