package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.pathfinder.Path;

public class NearestBedSensor extends Sensor<Mob> {
   private static final int CACHE_TIMEOUT = 40;
   private static final int BATCH_SIZE = 5;
   private static final int RATE = 20;
   private final Long2LongMap batchCache = new Long2LongOpenHashMap();
   private int triedCount;
   private long lastUpdate;

   public NearestBedSensor() {
      super(20);
   }

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_BED);
   }

   protected void doTick(ServerLevel serverlevel, Mob mob) {
      if (mob.isBaby()) {
         this.triedCount = 0;
         this.lastUpdate = serverlevel.getGameTime() + (long)serverlevel.getRandom().nextInt(20);
         PoiManager poimanager = serverlevel.getPoiManager();
         Predicate<BlockPos> predicate = (blockpos1) -> {
            long i = blockpos1.asLong();
            if (this.batchCache.containsKey(i)) {
               return false;
            } else if (++this.triedCount >= 5) {
               return false;
            } else {
               this.batchCache.put(i, this.lastUpdate + 40L);
               return true;
            }
         };
         Set<Pair<Holder<PoiType>, BlockPos>> set = poimanager.findAllWithType((holder) -> holder.is(PoiTypes.HOME), predicate, mob.blockPosition(), 48, PoiManager.Occupancy.ANY).collect(Collectors.toSet());
         Path path = AcquirePoi.findPathToPois(mob, set);
         if (path != null && path.canReach()) {
            BlockPos blockpos = path.getTarget();
            Optional<Holder<PoiType>> optional = poimanager.getType(blockpos);
            if (optional.isPresent()) {
               mob.getBrain().setMemory(MemoryModuleType.NEAREST_BED, blockpos);
            }
         } else if (this.triedCount < 5) {
            this.batchCache.long2LongEntrySet().removeIf((long2longmap_entry) -> long2longmap_entry.getLongValue() < this.lastUpdate);
         }

      }
   }
}
