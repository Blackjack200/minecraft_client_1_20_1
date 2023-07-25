package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record InclusiveRange<T extends Comparable<T>>(T minInclusive, T maxInclusive) {
   public static final Codec<InclusiveRange<Integer>> INT = codec(Codec.INT);

   public InclusiveRange {
      if (comparable.compareTo(comparable1) > 0) {
         throw new IllegalArgumentException("min_inclusive must be less than or equal to max_inclusive");
      }
   }

   public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec) {
      return ExtraCodecs.intervalCodec(codec, "min_inclusive", "max_inclusive", InclusiveRange::create, InclusiveRange::minInclusive, InclusiveRange::maxInclusive);
   }

   public static <T extends Comparable<T>> Codec<InclusiveRange<T>> codec(Codec<T> codec, T comparable, T comparable1) {
      return ExtraCodecs.validate(codec(codec), (inclusiverange) -> {
         if (inclusiverange.minInclusive().compareTo(comparable) < 0) {
            return DataResult.error(() -> "Range limit too low, expected at least " + comparable + " [" + inclusiverange.minInclusive() + "-" + inclusiverange.maxInclusive() + "]");
         } else {
            return inclusiverange.maxInclusive().compareTo(comparable1) > 0 ? DataResult.error(() -> "Range limit too high, expected at most " + comparable1 + " [" + inclusiverange.minInclusive() + "-" + inclusiverange.maxInclusive() + "]") : DataResult.success(inclusiverange);
         }
      });
   }

   public static <T extends Comparable<T>> DataResult<InclusiveRange<T>> create(T comparable, T comparable1) {
      return comparable.compareTo(comparable1) <= 0 ? DataResult.success(new InclusiveRange<>(comparable, comparable1)) : DataResult.error(() -> "min_inclusive must be less than or equal to max_inclusive");
   }

   public boolean isValueInRange(T comparable) {
      return comparable.compareTo(this.minInclusive) >= 0 && comparable.compareTo(this.maxInclusive) <= 0;
   }

   public boolean contains(InclusiveRange<T> inclusiverange) {
      return inclusiverange.minInclusive().compareTo(this.minInclusive) >= 0 && inclusiverange.maxInclusive.compareTo(this.maxInclusive) <= 0;
   }

   public String toString() {
      return "[" + this.minInclusive + ", " + this.maxInclusive + "]";
   }
}
