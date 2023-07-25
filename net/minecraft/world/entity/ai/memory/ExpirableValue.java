package net.minecraft.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.VisibleForDebug;

public class ExpirableValue<T> {
   private final T value;
   private long timeToLive;

   public ExpirableValue(T object, long i) {
      this.value = object;
      this.timeToLive = i;
   }

   public void tick() {
      if (this.canExpire()) {
         --this.timeToLive;
      }

   }

   public static <T> ExpirableValue<T> of(T object) {
      return new ExpirableValue<>(object, Long.MAX_VALUE);
   }

   public static <T> ExpirableValue<T> of(T object, long i) {
      return new ExpirableValue<>(object, i);
   }

   public long getTimeToLive() {
      return this.timeToLive;
   }

   public T getValue() {
      return this.value;
   }

   public boolean hasExpired() {
      return this.timeToLive <= 0L;
   }

   public String toString() {
      return this.value + (this.canExpire() ? " (ttl: " + this.timeToLive + ")" : "");
   }

   @VisibleForDebug
   public boolean canExpire() {
      return this.timeToLive != Long.MAX_VALUE;
   }

   public static <T> Codec<ExpirableValue<T>> codec(Codec<T> codec) {
      return RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(codec.fieldOf("value").forGetter((expirablevalue1) -> expirablevalue1.value), Codec.LONG.optionalFieldOf("ttl").forGetter((expirablevalue) -> expirablevalue.canExpire() ? Optional.of(expirablevalue.timeToLive) : Optional.empty())).apply(recordcodecbuilder_instance, (object, optional) -> new ExpirableValue<>(object, optional.orElse(Long.MAX_VALUE))));
   }
}
