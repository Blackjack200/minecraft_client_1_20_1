package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
   private static final String TAG_ID = "i";
   private static final String TAG_X = "x";
   private static final String TAG_Y = "y";
   private static final String TAG_Z = "z";
   private static final String TAG_DELAY = "t";
   private static final String TAG_PRIORITY = "p";
   public static final Hash.Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Hash.Strategy<SavedTick<?>>() {
      public int hashCode(SavedTick<?> savedtick) {
         return 31 * savedtick.pos().hashCode() + savedtick.type().hashCode();
      }

      public boolean equals(@Nullable SavedTick<?> savedtick, @Nullable SavedTick<?> savedtick1) {
         if (savedtick == savedtick1) {
            return true;
         } else if (savedtick != null && savedtick1 != null) {
            return savedtick.type() == savedtick1.type() && savedtick.pos().equals(savedtick1.pos());
         } else {
            return false;
         }
      }
   };

   public static <T> void loadTickList(ListTag listtag, Function<String, Optional<T>> function, ChunkPos chunkpos, Consumer<SavedTick<T>> consumer) {
      long i = chunkpos.toLong();

      for(int j = 0; j < listtag.size(); ++j) {
         CompoundTag compoundtag = listtag.getCompound(j);
         loadTick(compoundtag, function).ifPresent((savedtick) -> {
            if (ChunkPos.asLong(savedtick.pos()) == i) {
               consumer.accept(savedtick);
            }

         });
      }

   }

   public static <T> Optional<SavedTick<T>> loadTick(CompoundTag compoundtag, Function<String, Optional<T>> function) {
      return function.apply(compoundtag.getString("i")).map((object) -> {
         BlockPos blockpos = new BlockPos(compoundtag.getInt("x"), compoundtag.getInt("y"), compoundtag.getInt("z"));
         return new SavedTick<>(object, blockpos, compoundtag.getInt("t"), TickPriority.byValue(compoundtag.getInt("p")));
      });
   }

   private static CompoundTag saveTick(String s, BlockPos blockpos, int i, TickPriority tickpriority) {
      CompoundTag compoundtag = new CompoundTag();
      compoundtag.putString("i", s);
      compoundtag.putInt("x", blockpos.getX());
      compoundtag.putInt("y", blockpos.getY());
      compoundtag.putInt("z", blockpos.getZ());
      compoundtag.putInt("t", i);
      compoundtag.putInt("p", tickpriority.getValue());
      return compoundtag;
   }

   public static <T> CompoundTag saveTick(ScheduledTick<T> scheduledtick, Function<T, String> function, long i) {
      return saveTick(function.apply(scheduledtick.type()), scheduledtick.pos(), (int)(scheduledtick.triggerTick() - i), scheduledtick.priority());
   }

   public CompoundTag save(Function<T, String> function) {
      return saveTick(function.apply(this.type), this.pos, this.delay, this.priority);
   }

   public ScheduledTick<T> unpack(long i, long j) {
      return new ScheduledTick<>(this.type, this.pos, i + (long)this.delay, this.priority, j);
   }

   public static <T> SavedTick<T> probe(T object, BlockPos blockpos) {
      return new SavedTick<>(object, blockpos, 0, TickPriority.NORMAL);
   }
}
