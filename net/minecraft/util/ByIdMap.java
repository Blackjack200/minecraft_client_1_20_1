package net.minecraft.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class ByIdMap {
   private static <T> IntFunction<T> createMap(ToIntFunction<T> tointfunction, T[] aobject) {
      if (aobject.length == 0) {
         throw new IllegalArgumentException("Empty value list");
      } else {
         Int2ObjectMap<T> int2objectmap = new Int2ObjectOpenHashMap<>();

         for(T object : aobject) {
            int i = tointfunction.applyAsInt(object);
            T object1 = int2objectmap.put(i, object);
            if (object1 != null) {
               throw new IllegalArgumentException("Duplicate entry on id " + i + ": current=" + object + ", previous=" + object1);
            }
         }

         return int2objectmap;
      }
   }

   public static <T> IntFunction<T> sparse(ToIntFunction<T> tointfunction, T[] aobject, T object) {
      IntFunction<T> intfunction = createMap(tointfunction, aobject);
      return (i) -> Objects.requireNonNullElse(intfunction.apply(i), object);
   }

   private static <T> T[] createSortedArray(ToIntFunction<T> tointfunction, T[] aobject) {
      int i = aobject.length;
      if (i == 0) {
         throw new IllegalArgumentException("Empty value list");
      } else {
         T[] aobject1 = (T[])((Object[])aobject.clone());
         Arrays.fill(aobject1, (Object)null);

         for(T object : aobject) {
            int j = tointfunction.applyAsInt(object);
            if (j < 0 || j >= i) {
               throw new IllegalArgumentException("Values are not continous, found index " + j + " for value " + object);
            }

            T object1 = aobject1[j];
            if (object1 != null) {
               throw new IllegalArgumentException("Duplicate entry on id " + j + ": current=" + object + ", previous=" + object1);
            }

            aobject1[j] = object;
         }

         for(int k = 0; k < i; ++k) {
            if (aobject1[k] == null) {
               throw new IllegalArgumentException("Missing value at index: " + k);
            }
         }

         return aobject1;
      }
   }

   public static <T> IntFunction<T> continuous(ToIntFunction<T> tointfunction, T[] aobject, ByIdMap.OutOfBoundsStrategy byidmap_outofboundsstrategy) {
      T[] aobject1 = createSortedArray(tointfunction, aobject);
      int i = aobject1.length;
      IntFunction var10000;
      switch (byidmap_outofboundsstrategy) {
         case ZERO:
            T object = aobject1[0];
            var10000 = (k1) -> k1 >= 0 && k1 < i ? aobject1[k1] : object;
            break;
         case WRAP:
            var10000 = (i1) -> aobject1[Mth.positiveModulo(i1, i)];
            break;
         case CLAMP:
            var10000 = (k) -> aobject1[Mth.clamp(k, 0, i - 1)];
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static enum OutOfBoundsStrategy {
      ZERO,
      WRAP,
      CLAMP;
   }
}
