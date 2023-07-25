package net.minecraft.world.ticks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

public class ProtoChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
   private final List<SavedTick<T>> ticks = Lists.newArrayList();
   private final Set<SavedTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(SavedTick.UNIQUE_TICK_HASH);

   public void schedule(ScheduledTick<T> scheduledtick) {
      SavedTick<T> savedtick = new SavedTick<>(scheduledtick.type(), scheduledtick.pos(), 0, scheduledtick.priority());
      this.schedule(savedtick);
   }

   private void schedule(SavedTick<T> savedtick) {
      if (this.ticksPerPosition.add(savedtick)) {
         this.ticks.add(savedtick);
      }

   }

   public boolean hasScheduledTick(BlockPos blockpos, T object) {
      return this.ticksPerPosition.contains(SavedTick.probe(object, blockpos));
   }

   public int count() {
      return this.ticks.size();
   }

   public Tag save(long i, Function<T, String> function) {
      ListTag listtag = new ListTag();

      for(SavedTick<T> savedtick : this.ticks) {
         listtag.add(savedtick.save(function));
      }

      return listtag;
   }

   public List<SavedTick<T>> scheduledTicks() {
      return List.copyOf(this.ticks);
   }

   public static <T> ProtoChunkTicks<T> load(ListTag listtag, Function<String, Optional<T>> function, ChunkPos chunkpos) {
      ProtoChunkTicks<T> protochunkticks = new ProtoChunkTicks<>();
      SavedTick.loadTickList(listtag, function, chunkpos, protochunkticks::schedule);
      return protochunkticks;
   }
}
