package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class Brain<E extends LivingEntity> {
   static final Logger LOGGER = LogUtils.getLogger();
   private final Supplier<Codec<Brain<E>>> codec;
   private static final int SCHEDULE_UPDATE_DELAY = 20;
   private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.newHashMap();
   private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
   private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
   private Schedule schedule = Schedule.EMPTY;
   private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
   private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
   private Set<Activity> coreActivities = Sets.newHashSet();
   private final Set<Activity> activeActivities = Sets.newHashSet();
   private Activity defaultActivity = Activity.IDLE;
   private long lastScheduleUpdate = -9999L;

   public static <E extends LivingEntity> Brain.Provider<E> provider(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection1) {
      return new Brain.Provider<>(collection, collection1);
   }

   public static <E extends LivingEntity> Codec<Brain<E>> codec(final Collection<? extends MemoryModuleType<?>> collection, final Collection<? extends SensorType<? extends Sensor<? super E>>> collection1) {
      final MutableObject<Codec<Brain<E>>> mutableobject = new MutableObject<>();
      mutableobject.setValue((new MapCodec<Brain<E>>() {
         public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
            return collection.stream().flatMap((memorymoduletype) -> memorymoduletype.getCodec().map((codec) -> BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memorymoduletype)).stream()).map((resourcelocation) -> dynamicops.createString(resourcelocation.toString()));
         }

         public <T> DataResult<Brain<E>> decode(DynamicOps<T> dynamicops, MapLike<T> maplike) {
            MutableObject<DataResult<ImmutableList.Builder<Brain.MemoryValue<?>>>> mutableobject = new MutableObject<>(DataResult.success(ImmutableList.builder()));
            maplike.entries().forEach((pair) -> {
               DataResult<MemoryModuleType<?>> dataresult = BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().parse(dynamicops, pair.getFirst());
               DataResult<? extends Brain.MemoryValue<?>> dataresult1 = dataresult.flatMap((memorymoduletype) -> this.captureRead(memorymoduletype, dynamicops, (T)pair.getSecond()));
               mutableobject.setValue(mutableobject.getValue().apply2(ImmutableList.Builder::add, dataresult1));
            });
            ImmutableList<Brain.MemoryValue<?>> immutablelist = mutableobject.getValue().resultOrPartial(Brain.LOGGER::error).map(ImmutableList.Builder::build).orElseGet(ImmutableList::of);
            return DataResult.success(new Brain<>(collection, collection1, immutablelist, mutableobject::getValue));
         }

         private <T, U> DataResult<Brain.MemoryValue<U>> captureRead(MemoryModuleType<U> memorymoduletype, DynamicOps<T> dynamicops, T object) {
            return memorymoduletype.getCodec().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No codec for memory: " + memorymoduletype)).flatMap((codec) -> codec.parse(dynamicops, object)).map((expirablevalue) -> new Brain.MemoryValue<>(memorymoduletype, Optional.of(expirablevalue)));
         }

         public <T> RecordBuilder<T> encode(Brain<E> brain, DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
            brain.memories().forEach((brain_memoryvalue) -> brain_memoryvalue.serialize(dynamicops, recordbuilder));
            return recordbuilder;
         }
      }).fieldOf("memories").codec());
      return mutableobject.getValue();
   }

   public Brain(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection1, ImmutableList<Brain.MemoryValue<?>> immutablelist, Supplier<Codec<Brain<E>>> supplier) {
      this.codec = supplier;

      for(MemoryModuleType<?> memorymoduletype : collection) {
         this.memories.put(memorymoduletype, Optional.empty());
      }

      for(SensorType<? extends Sensor<? super E>> sensortype : collection1) {
         this.sensors.put(sensortype, sensortype.create());
      }

      for(Sensor<? super E> sensor : this.sensors.values()) {
         for(MemoryModuleType<?> memorymoduletype1 : sensor.requires()) {
            this.memories.put(memorymoduletype1, Optional.empty());
         }
      }

      for(Brain.MemoryValue<?> brain_memoryvalue : immutablelist) {
         brain_memoryvalue.setMemoryInternal(this);
      }

   }

   public <T> DataResult<T> serializeStart(DynamicOps<T> dynamicops) {
      return this.codec.get().encodeStart(dynamicops, this);
   }

   Stream<Brain.MemoryValue<?>> memories() {
      return this.memories.entrySet().stream().map((map_entry) -> Brain.MemoryValue.createUnchecked(map_entry.getKey(), map_entry.getValue()));
   }

   public boolean hasMemoryValue(MemoryModuleType<?> memorymoduletype) {
      return this.checkMemory(memorymoduletype, MemoryStatus.VALUE_PRESENT);
   }

   public void clearMemories() {
      this.memories.keySet().forEach((memorymoduletype) -> this.memories.put(memorymoduletype, Optional.empty()));
   }

   public <U> void eraseMemory(MemoryModuleType<U> memorymoduletype) {
      this.setMemory(memorymoduletype, Optional.empty());
   }

   public <U> void setMemory(MemoryModuleType<U> memorymoduletype, @Nullable U object) {
      this.setMemory(memorymoduletype, Optional.ofNullable(object));
   }

   public <U> void setMemoryWithExpiry(MemoryModuleType<U> memorymoduletype, U object, long i) {
      this.setMemoryInternal(memorymoduletype, Optional.of(ExpirableValue.of(object, i)));
   }

   public <U> void setMemory(MemoryModuleType<U> memorymoduletype, Optional<? extends U> optional) {
      this.setMemoryInternal(memorymoduletype, optional.map(ExpirableValue::of));
   }

   <U> void setMemoryInternal(MemoryModuleType<U> memorymoduletype, Optional<? extends ExpirableValue<?>> optional) {
      if (this.memories.containsKey(memorymoduletype)) {
         if (optional.isPresent() && this.isEmptyCollection(optional.get().getValue())) {
            this.eraseMemory(memorymoduletype);
         } else {
            this.memories.put(memorymoduletype, optional);
         }
      }

   }

   public <U> Optional<U> getMemory(MemoryModuleType<U> memorymoduletype) {
      Optional<? extends ExpirableValue<?>> optional = this.memories.get(memorymoduletype);
      if (optional == null) {
         throw new IllegalStateException("Unregistered memory fetched: " + memorymoduletype);
      } else {
         return optional.map(ExpirableValue::getValue);
      }
   }

   @Nullable
   public <U> Optional<U> getMemoryInternal(MemoryModuleType<U> memorymoduletype) {
      Optional<? extends ExpirableValue<?>> optional = this.memories.get(memorymoduletype);
      return optional == null ? null : optional.map(ExpirableValue::getValue);
   }

   public <U> long getTimeUntilExpiry(MemoryModuleType<U> memorymoduletype) {
      Optional<? extends ExpirableValue<?>> optional = this.memories.get(memorymoduletype);
      return optional.map(ExpirableValue::getTimeToLive).orElse(0L);
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
      return this.memories;
   }

   public <U> boolean isMemoryValue(MemoryModuleType<U> memorymoduletype, U object) {
      return !this.hasMemoryValue(memorymoduletype) ? false : this.getMemory(memorymoduletype).filter((object2) -> object2.equals(object)).isPresent();
   }

   public boolean checkMemory(MemoryModuleType<?> memorymoduletype, MemoryStatus memorystatus) {
      Optional<? extends ExpirableValue<?>> optional = this.memories.get(memorymoduletype);
      if (optional == null) {
         return false;
      } else {
         return memorystatus == MemoryStatus.REGISTERED || memorystatus == MemoryStatus.VALUE_PRESENT && optional.isPresent() || memorystatus == MemoryStatus.VALUE_ABSENT && !optional.isPresent();
      }
   }

   public Schedule getSchedule() {
      return this.schedule;
   }

   public void setSchedule(Schedule schedule) {
      this.schedule = schedule;
   }

   public void setCoreActivities(Set<Activity> set) {
      this.coreActivities = set;
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public Set<Activity> getActiveActivities() {
      return this.activeActivities;
   }

   /** @deprecated */
   @Deprecated
   @VisibleForDebug
   public List<BehaviorControl<? super E>> getRunningBehaviors() {
      List<BehaviorControl<? super E>> list = new ObjectArrayList<>();

      for(Map<Activity, Set<BehaviorControl<? super E>>> map : this.availableBehaviorsByPriority.values()) {
         for(Set<BehaviorControl<? super E>> set : map.values()) {
            for(BehaviorControl<? super E> behaviorcontrol : set) {
               if (behaviorcontrol.getStatus() == Behavior.Status.RUNNING) {
                  list.add(behaviorcontrol);
               }
            }
         }
      }

      return list;
   }

   public void useDefaultActivity() {
      this.setActiveActivity(this.defaultActivity);
   }

   public Optional<Activity> getActiveNonCoreActivity() {
      for(Activity activity : this.activeActivities) {
         if (!this.coreActivities.contains(activity)) {
            return Optional.of(activity);
         }
      }

      return Optional.empty();
   }

   public void setActiveActivityIfPossible(Activity activity) {
      if (this.activityRequirementsAreMet(activity)) {
         this.setActiveActivity(activity);
      } else {
         this.useDefaultActivity();
      }

   }

   private void setActiveActivity(Activity activity) {
      if (!this.isActive(activity)) {
         this.eraseMemoriesForOtherActivitesThan(activity);
         this.activeActivities.clear();
         this.activeActivities.addAll(this.coreActivities);
         this.activeActivities.add(activity);
      }
   }

   private void eraseMemoriesForOtherActivitesThan(Activity activity) {
      for(Activity activity1 : this.activeActivities) {
         if (activity1 != activity) {
            Set<MemoryModuleType<?>> set = this.activityMemoriesToEraseWhenStopped.get(activity1);
            if (set != null) {
               for(MemoryModuleType<?> memorymoduletype : set) {
                  this.eraseMemory(memorymoduletype);
               }
            }
         }
      }

   }

   public void updateActivityFromSchedule(long i, long j) {
      if (j - this.lastScheduleUpdate > 20L) {
         this.lastScheduleUpdate = j;
         Activity activity = this.getSchedule().getActivityAt((int)(i % 24000L));
         if (!this.activeActivities.contains(activity)) {
            this.setActiveActivityIfPossible(activity);
         }
      }

   }

   public void setActiveActivityToFirstValid(List<Activity> list) {
      for(Activity activity : list) {
         if (this.activityRequirementsAreMet(activity)) {
            this.setActiveActivity(activity);
            break;
         }
      }

   }

   public void setDefaultActivity(Activity activity) {
      this.defaultActivity = activity;
   }

   public void addActivity(Activity activity, int i, ImmutableList<? extends BehaviorControl<? super E>> immutablelist) {
      this.addActivity(activity, this.createPriorityPairs(i, immutablelist));
   }

   public void addActivityAndRemoveMemoryWhenStopped(Activity activity, int i, ImmutableList<? extends BehaviorControl<? super E>> immutablelist, MemoryModuleType<?> memorymoduletype) {
      Set<Pair<MemoryModuleType<?>, MemoryStatus>> set = ImmutableSet.of(Pair.of(memorymoduletype, MemoryStatus.VALUE_PRESENT));
      Set<MemoryModuleType<?>> set1 = ImmutableSet.of(memorymoduletype);
      this.addActivityAndRemoveMemoriesWhenStopped(activity, this.createPriorityPairs(i, immutablelist), set, set1);
   }

   public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutablelist) {
      this.addActivityAndRemoveMemoriesWhenStopped(activity, immutablelist, ImmutableSet.of(), Sets.newHashSet());
   }

   public void addActivityWithConditions(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutablelist, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set) {
      this.addActivityAndRemoveMemoriesWhenStopped(activity, immutablelist, set, Sets.newHashSet());
   }

   public void addActivityAndRemoveMemoriesWhenStopped(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutablelist, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set, Set<MemoryModuleType<?>> set1) {
      this.activityRequirements.put(activity, set);
      if (!set1.isEmpty()) {
         this.activityMemoriesToEraseWhenStopped.put(activity, set1);
      }

      for(Pair<Integer, ? extends BehaviorControl<? super E>> pair : immutablelist) {
         this.availableBehaviorsByPriority.computeIfAbsent(pair.getFirst(), (integer) -> Maps.newHashMap()).computeIfAbsent(activity, (activity1) -> Sets.newLinkedHashSet()).add(pair.getSecond());
      }

   }

   @VisibleForTesting
   public void removeAllBehaviors() {
      this.availableBehaviorsByPriority.clear();
   }

   public boolean isActive(Activity activity) {
      return this.activeActivities.contains(activity);
   }

   public Brain<E> copyWithoutBehaviors() {
      Brain<E> brain = new Brain<>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);

      for(Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map_entry : this.memories.entrySet()) {
         MemoryModuleType<?> memorymoduletype = map_entry.getKey();
         if (map_entry.getValue().isPresent()) {
            brain.memories.put(memorymoduletype, map_entry.getValue());
         }
      }

      return brain;
   }

   public void tick(ServerLevel serverlevel, E livingentity) {
      this.forgetOutdatedMemories();
      this.tickSensors(serverlevel, livingentity);
      this.startEachNonRunningBehavior(serverlevel, livingentity);
      this.tickEachRunningBehavior(serverlevel, livingentity);
   }

   private void tickSensors(ServerLevel serverlevel, E livingentity) {
      for(Sensor<? super E> sensor : this.sensors.values()) {
         sensor.tick(serverlevel, livingentity);
      }

   }

   private void forgetOutdatedMemories() {
      for(Map.Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> map_entry : this.memories.entrySet()) {
         if (map_entry.getValue().isPresent()) {
            ExpirableValue<?> expirablevalue = map_entry.getValue().get();
            if (expirablevalue.hasExpired()) {
               this.eraseMemory(map_entry.getKey());
            }

            expirablevalue.tick();
         }
      }

   }

   public void stopAll(ServerLevel serverlevel, E livingentity) {
      long i = livingentity.level().getGameTime();

      for(BehaviorControl<? super E> behaviorcontrol : this.getRunningBehaviors()) {
         behaviorcontrol.doStop(serverlevel, livingentity, i);
      }

   }

   private void startEachNonRunningBehavior(ServerLevel serverlevel, E livingentity) {
      long i = serverlevel.getGameTime();

      for(Map<Activity, Set<BehaviorControl<? super E>>> map : this.availableBehaviorsByPriority.values()) {
         for(Map.Entry<Activity, Set<BehaviorControl<? super E>>> map_entry : map.entrySet()) {
            Activity activity = map_entry.getKey();
            if (this.activeActivities.contains(activity)) {
               for(BehaviorControl<? super E> behaviorcontrol : map_entry.getValue()) {
                  if (behaviorcontrol.getStatus() == Behavior.Status.STOPPED) {
                     behaviorcontrol.tryStart(serverlevel, livingentity, i);
                  }
               }
            }
         }
      }

   }

   private void tickEachRunningBehavior(ServerLevel serverlevel, E livingentity) {
      long i = serverlevel.getGameTime();

      for(BehaviorControl<? super E> behaviorcontrol : this.getRunningBehaviors()) {
         behaviorcontrol.tickOrStop(serverlevel, livingentity, i);
      }

   }

   private boolean activityRequirementsAreMet(Activity activity) {
      if (!this.activityRequirements.containsKey(activity)) {
         return false;
      } else {
         for(Pair<MemoryModuleType<?>, MemoryStatus> pair : this.activityRequirements.get(activity)) {
            MemoryModuleType<?> memorymoduletype = pair.getFirst();
            MemoryStatus memorystatus = pair.getSecond();
            if (!this.checkMemory(memorymoduletype, memorystatus)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isEmptyCollection(Object object) {
      return object instanceof Collection && ((Collection)object).isEmpty();
   }

   ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(int i, ImmutableList<? extends BehaviorControl<? super E>> immutablelist) {
      int j = i;
      ImmutableList.Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> immutablelist_builder = ImmutableList.builder();

      for(BehaviorControl<? super E> behaviorcontrol : immutablelist) {
         immutablelist_builder.add(Pair.of(j++, behaviorcontrol));
      }

      return immutablelist_builder.build();
   }

   static final class MemoryValue<U> {
      private final MemoryModuleType<U> type;
      private final Optional<? extends ExpirableValue<U>> value;

      static <U> Brain.MemoryValue<U> createUnchecked(MemoryModuleType<U> memorymoduletype, Optional<? extends ExpirableValue<?>> optional) {
         return new Brain.MemoryValue<>(memorymoduletype, optional);
      }

      MemoryValue(MemoryModuleType<U> memorymoduletype, Optional<? extends ExpirableValue<U>> optional) {
         this.type = memorymoduletype;
         this.value = optional;
      }

      void setMemoryInternal(Brain<?> brain) {
         brain.setMemoryInternal(this.type, this.value);
      }

      public <T> void serialize(DynamicOps<T> dynamicops, RecordBuilder<T> recordbuilder) {
         this.type.getCodec().ifPresent((codec) -> this.value.ifPresent((expirablevalue) -> recordbuilder.add(BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().encodeStart(dynamicops, this.type), codec.encodeStart(dynamicops, expirablevalue))));
      }
   }

   public static final class Provider<E extends LivingEntity> {
      private final Collection<? extends MemoryModuleType<?>> memoryTypes;
      private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes;
      private final Codec<Brain<E>> codec;

      Provider(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection1) {
         this.memoryTypes = collection;
         this.sensorTypes = collection1;
         this.codec = Brain.codec(collection, collection1);
      }

      public Brain<E> makeBrain(Dynamic<?> dynamic) {
         return this.codec.parse(dynamic).resultOrPartial(Brain.LOGGER::error).orElseGet(() -> new Brain<>(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> this.codec));
      }
   }
}
