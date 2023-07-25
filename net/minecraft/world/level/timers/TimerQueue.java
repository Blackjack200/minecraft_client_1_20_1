package net.minecraft.world.level.timers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;

public class TimerQueue<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String CALLBACK_DATA_TAG = "Callback";
   private static final String TIMER_NAME_TAG = "Name";
   private static final String TIMER_TRIGGER_TIME_TAG = "TriggerTime";
   private final TimerCallbacks<T> callbacksRegistry;
   private final Queue<TimerQueue.Event<T>> queue = new PriorityQueue<>(createComparator());
   private UnsignedLong sequentialId = UnsignedLong.ZERO;
   private final Table<String, Long, TimerQueue.Event<T>> events = HashBasedTable.create();

   private static <T> Comparator<TimerQueue.Event<T>> createComparator() {
      return Comparator.comparingLong((timerqueue_event1) -> timerqueue_event1.triggerTime).thenComparing((timerqueue_event) -> timerqueue_event.sequentialId);
   }

   public TimerQueue(TimerCallbacks<T> timercallbacks, Stream<? extends Dynamic<?>> stream) {
      this(timercallbacks);
      this.queue.clear();
      this.events.clear();
      this.sequentialId = UnsignedLong.ZERO;
      stream.forEach((dynamic) -> {
         Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
         if (tag instanceof CompoundTag compoundtag) {
            this.loadEvent(compoundtag);
         } else {
            LOGGER.warn("Invalid format of events: {}", (Object)tag);
         }

      });
   }

   public TimerQueue(TimerCallbacks<T> timercallbacks) {
      this.callbacksRegistry = timercallbacks;
   }

   public void tick(T object, long i) {
      while(true) {
         TimerQueue.Event<T> timerqueue_event = this.queue.peek();
         if (timerqueue_event == null || timerqueue_event.triggerTime > i) {
            return;
         }

         this.queue.remove();
         this.events.remove(timerqueue_event.id, i);
         timerqueue_event.callback.handle(object, this, i);
      }
   }

   public void schedule(String s, long i, TimerCallback<T> timercallback) {
      if (!this.events.contains(s, i)) {
         this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
         TimerQueue.Event<T> timerqueue_event = new TimerQueue.Event<>(i, this.sequentialId, s, timercallback);
         this.events.put(s, i, timerqueue_event);
         this.queue.add(timerqueue_event);
      }
   }

   public int remove(String s) {
      Collection<TimerQueue.Event<T>> collection = this.events.row(s).values();
      collection.forEach(this.queue::remove);
      int i = collection.size();
      collection.clear();
      return i;
   }

   public Set<String> getEventsIds() {
      return Collections.unmodifiableSet(this.events.rowKeySet());
   }

   private void loadEvent(CompoundTag compoundtag) {
      CompoundTag compoundtag1 = compoundtag.getCompound("Callback");
      TimerCallback<T> timercallback = this.callbacksRegistry.deserialize(compoundtag1);
      if (timercallback != null) {
         String s = compoundtag.getString("Name");
         long i = compoundtag.getLong("TriggerTime");
         this.schedule(s, i, timercallback);
      }

   }

   private CompoundTag storeEvent(TimerQueue.Event<T> timerqueue_event) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("Name", timerqueue_event.id);
      compoundtag.putLong("TriggerTime", timerqueue_event.triggerTime);
      compoundtag.put("Callback", this.callbacksRegistry.serialize(timerqueue_event.callback));
      return compoundtag;
   }

   public ListTag store() {
      ListTag listtag = new ListTag();
      this.queue.stream().sorted(createComparator()).map(this::storeEvent).forEach(listtag::add);
      return listtag;
   }

   public static class Event<T> {
      public final long triggerTime;
      public final UnsignedLong sequentialId;
      public final String id;
      public final TimerCallback<T> callback;

      Event(long i, UnsignedLong unsignedlong, String s, TimerCallback<T> timercallback) {
         this.triggerTime = i;
         this.sequentialId = unsignedlong;
         this.id = s;
         this.callback = timercallback;
      }
   }
}
