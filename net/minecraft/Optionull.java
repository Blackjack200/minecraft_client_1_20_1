package net.minecraft;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class Optionull {
   @Nullable
   public static <T, R> R map(@Nullable T object, Function<T, R> function) {
      return (R)(object == null ? null : function.apply(object));
   }

   public static <T, R> R mapOrDefault(@Nullable T object, Function<T, R> function, R object1) {
      return (R)(object == null ? object1 : function.apply(object));
   }

   public static <T, R> R mapOrElse(@Nullable T object, Function<T, R> function, Supplier<R> supplier) {
      return (R)(object == null ? supplier.get() : function.apply(object));
   }

   @Nullable
   public static <T> T first(Collection<T> collection) {
      Iterator<T> iterator = collection.iterator();
      return (T)(iterator.hasNext() ? iterator.next() : null);
   }

   public static <T> T firstOrDefault(Collection<T> collection, T object) {
      Iterator<T> iterator = collection.iterator();
      return (T)(iterator.hasNext() ? iterator.next() : object);
   }

   public static <T> T firstOrElse(Collection<T> collection, Supplier<T> supplier) {
      Iterator<T> iterator = collection.iterator();
      return (T)(iterator.hasNext() ? iterator.next() : supplier.get());
   }

   public static <T> boolean isNullOrEmpty(@Nullable T[] aobject) {
      return aobject == null || aobject.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable boolean[] aboolean) {
      return aboolean == null || aboolean.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable byte[] abyte) {
      return abyte == null || abyte.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable char[] achar) {
      return achar == null || achar.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable short[] ashort) {
      return ashort == null || ashort.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable int[] aint) {
      return aint == null || aint.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable long[] along) {
      return along == null || along.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable float[] afloat) {
      return afloat == null || afloat.length == 0;
   }

   public static boolean isNullOrEmpty(@Nullable double[] adouble) {
      return adouble == null || adouble.length == 0;
   }
}
