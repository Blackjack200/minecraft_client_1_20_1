package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface StringRepresentable {
   int PRE_BUILT_MAP_THRESHOLD = 16;

   String getSerializedName();

   static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnum(Supplier<E[]> supplier) {
      return fromEnumWithMapping(supplier, (s) -> s);
   }

   static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnumWithMapping(Supplier<E[]> supplier, Function<String, String> function) {
      E[] aenum = (Enum[])supplier.get();
      if (aenum.length > 16) {
         Map<String, E> map = Arrays.stream(aenum).collect(Collectors.toMap((oenum2) -> function.apply(oenum2.getSerializedName()), (oenum1) -> oenum1));
         return new StringRepresentable.EnumCodec<>(aenum, (s1) -> (E)(s1 == null ? null : map.get(s1)));
      } else {
         return new StringRepresentable.EnumCodec<>(aenum, (s) -> {
            for(E oenum : aenum) {
               if (function.apply(oenum.getSerializedName()).equals(s)) {
                  return oenum;
               }
            }

            return (E)null;
         });
      }
   }

   static Keyable keys(final StringRepresentable[] astringrepresentable) {
      return new Keyable() {
         public <T> Stream<T> keys(DynamicOps<T> dynamicops) {
            return Arrays.stream(astringrepresentable).map(StringRepresentable::getSerializedName).map(dynamicops::createString);
         }
      };
   }

   /** @deprecated */
   @Deprecated
   public static class EnumCodec<E extends Enum<E> & StringRepresentable> implements Codec<E> {
      private final Codec<E> codec;
      private final Function<String, E> resolver;

      public EnumCodec(E[] aenum, Function<String, E> function) {
         this.codec = ExtraCodecs.orCompressed(ExtraCodecs.stringResolverCodec((object1) -> object1.getSerializedName(), function), ExtraCodecs.idResolverCodec((object) -> object.ordinal(), (i) -> (E)(i >= 0 && i < aenum.length ? aenum[i] : null), -1));
         this.resolver = function;
      }

      public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicops, T object) {
         return this.codec.decode(dynamicops, object);
      }

      public <T> DataResult<T> encode(E oenum, DynamicOps<T> dynamicops, T object) {
         return this.codec.encode(oenum, dynamicops, object);
      }

      @Nullable
      public E byName(@Nullable String s) {
         return this.resolver.apply(s);
      }

      public E byName(@Nullable String s, E oenum) {
         return Objects.requireNonNullElse(this.byName(s), oenum);
      }
   }
}
