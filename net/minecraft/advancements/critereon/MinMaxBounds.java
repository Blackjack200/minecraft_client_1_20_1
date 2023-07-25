package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public abstract class MinMaxBounds<T extends Number> {
   public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
   public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));
   @Nullable
   protected final T min;
   @Nullable
   protected final T max;

   protected MinMaxBounds(@Nullable T number, @Nullable T number1) {
      this.min = number;
      this.max = number1;
   }

   @Nullable
   public T getMin() {
      return this.min;
   }

   @Nullable
   public T getMax() {
      return this.max;
   }

   public boolean isAny() {
      return this.min == null && this.max == null;
   }

   public JsonElement serializeToJson() {
      if (this.isAny()) {
         return JsonNull.INSTANCE;
      } else if (this.min != null && this.min.equals(this.max)) {
         return new JsonPrimitive(this.min);
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.min != null) {
            jsonobject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonobject.addProperty("max", this.max);
         }

         return jsonobject;
      }
   }

   protected static <T extends Number, R extends MinMaxBounds<T>> R fromJson(@Nullable JsonElement jsonelement, R minmaxbounds, BiFunction<JsonElement, String, T> bifunction, MinMaxBounds.BoundsFactory<T, R> minmaxbounds_boundsfactory) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         if (GsonHelper.isNumberValue(jsonelement)) {
            T number = bifunction.apply(jsonelement, "value");
            return minmaxbounds_boundsfactory.create(number, number);
         } else {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "value");
            T number1 = jsonobject.has("min") ? bifunction.apply(jsonobject.get("min"), "min") : null;
            T number2 = jsonobject.has("max") ? bifunction.apply(jsonobject.get("max"), "max") : null;
            return minmaxbounds_boundsfactory.create(number1, number2);
         }
      } else {
         return minmaxbounds;
      }
   }

   protected static <T extends Number, R extends MinMaxBounds<T>> R fromReader(StringReader stringreader, MinMaxBounds.BoundsFromReaderFactory<T, R> minmaxbounds_boundsfromreaderfactory, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier, Function<T, T> function1) throws CommandSyntaxException {
      if (!stringreader.canRead()) {
         throw ERROR_EMPTY.createWithContext(stringreader);
      } else {
         int i = stringreader.getCursor();

         try {
            T number = optionallyFormat(readNumber(stringreader, function, supplier), function1);
            T number1;
            if (stringreader.canRead(2) && stringreader.peek() == '.' && stringreader.peek(1) == '.') {
               stringreader.skip();
               stringreader.skip();
               number1 = optionallyFormat(readNumber(stringreader, function, supplier), function1);
               if (number == null && number1 == null) {
                  throw ERROR_EMPTY.createWithContext(stringreader);
               }
            } else {
               number1 = number;
            }

            if (number == null && number1 == null) {
               throw ERROR_EMPTY.createWithContext(stringreader);
            } else {
               return minmaxbounds_boundsfromreaderfactory.create(stringreader, number, number1);
            }
         } catch (CommandSyntaxException var8) {
            stringreader.setCursor(i);
            throw new CommandSyntaxException(var8.getType(), var8.getRawMessage(), var8.getInput(), i);
         }
      }
   }

   @Nullable
   private static <T extends Number> T readNumber(StringReader stringreader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
      int i = stringreader.getCursor();

      while(stringreader.canRead() && isAllowedInputChat(stringreader)) {
         stringreader.skip();
      }

      String s = stringreader.getString().substring(i, stringreader.getCursor());
      if (s.isEmpty()) {
         return (T)null;
      } else {
         try {
            return function.apply(s);
         } catch (NumberFormatException var6) {
            throw supplier.get().createWithContext(stringreader, s);
         }
      }
   }

   private static boolean isAllowedInputChat(StringReader stringreader) {
      char c0 = stringreader.peek();
      if ((c0 < '0' || c0 > '9') && c0 != '-') {
         if (c0 != '.') {
            return false;
         } else {
            return !stringreader.canRead(2) || stringreader.peek(1) != '.';
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static <T> T optionallyFormat(@Nullable T object, Function<T, T> function) {
      return (T)(object == null ? null : function.apply(object));
   }

   @FunctionalInterface
   protected interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
      R create(@Nullable T number, @Nullable T number1);
   }

   @FunctionalInterface
   protected interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
      R create(StringReader stringreader, @Nullable T number, @Nullable T number1) throws CommandSyntaxException;
   }

   public static class Doubles extends MinMaxBounds<Double> {
      public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles((Double)null, (Double)null);
      @Nullable
      private final Double minSq;
      @Nullable
      private final Double maxSq;

      private static MinMaxBounds.Doubles create(StringReader stringreader, @Nullable Double odouble, @Nullable Double odouble1) throws CommandSyntaxException {
         if (odouble != null && odouble1 != null && odouble > odouble1) {
            throw ERROR_SWAPPED.createWithContext(stringreader);
         } else {
            return new MinMaxBounds.Doubles(odouble, odouble1);
         }
      }

      @Nullable
      private static Double squareOpt(@Nullable Double odouble) {
         return odouble == null ? null : odouble * odouble;
      }

      private Doubles(@Nullable Double odouble, @Nullable Double odouble1) {
         super(odouble, odouble1);
         this.minSq = squareOpt(odouble);
         this.maxSq = squareOpt(odouble1);
      }

      public static MinMaxBounds.Doubles exactly(double d0) {
         return new MinMaxBounds.Doubles(d0, d0);
      }

      public static MinMaxBounds.Doubles between(double d0, double d1) {
         return new MinMaxBounds.Doubles(d0, d1);
      }

      public static MinMaxBounds.Doubles atLeast(double d0) {
         return new MinMaxBounds.Doubles(d0, (Double)null);
      }

      public static MinMaxBounds.Doubles atMost(double d0) {
         return new MinMaxBounds.Doubles((Double)null, d0);
      }

      public boolean matches(double d0) {
         if (this.min != null && this.min > d0) {
            return false;
         } else {
            return this.max == null || !(this.max < d0);
         }
      }

      public boolean matchesSqr(double d0) {
         if (this.minSq != null && this.minSq > d0) {
            return false;
         } else {
            return this.maxSq == null || !(this.maxSq < d0);
         }
      }

      public static MinMaxBounds.Doubles fromJson(@Nullable JsonElement jsonelement) {
         return fromJson(jsonelement, ANY, GsonHelper::convertToDouble, MinMaxBounds.Doubles::new);
      }

      public static MinMaxBounds.Doubles fromReader(StringReader stringreader) throws CommandSyntaxException {
         return fromReader(stringreader, (odouble) -> odouble);
      }

      public static MinMaxBounds.Doubles fromReader(StringReader stringreader, Function<Double, Double> function) throws CommandSyntaxException {
         return fromReader(stringreader, MinMaxBounds.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, function);
      }
   }

   public static class Ints extends MinMaxBounds<Integer> {
      public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints((Integer)null, (Integer)null);
      @Nullable
      private final Long minSq;
      @Nullable
      private final Long maxSq;

      private static MinMaxBounds.Ints create(StringReader stringreader, @Nullable Integer integer, @Nullable Integer integer1) throws CommandSyntaxException {
         if (integer != null && integer1 != null && integer > integer1) {
            throw ERROR_SWAPPED.createWithContext(stringreader);
         } else {
            return new MinMaxBounds.Ints(integer, integer1);
         }
      }

      @Nullable
      private static Long squareOpt(@Nullable Integer integer) {
         return integer == null ? null : integer.longValue() * integer.longValue();
      }

      private Ints(@Nullable Integer integer, @Nullable Integer integer1) {
         super(integer, integer1);
         this.minSq = squareOpt(integer);
         this.maxSq = squareOpt(integer1);
      }

      public static MinMaxBounds.Ints exactly(int i) {
         return new MinMaxBounds.Ints(i, i);
      }

      public static MinMaxBounds.Ints between(int i, int j) {
         return new MinMaxBounds.Ints(i, j);
      }

      public static MinMaxBounds.Ints atLeast(int i) {
         return new MinMaxBounds.Ints(i, (Integer)null);
      }

      public static MinMaxBounds.Ints atMost(int i) {
         return new MinMaxBounds.Ints((Integer)null, i);
      }

      public boolean matches(int i) {
         if (this.min != null && this.min > i) {
            return false;
         } else {
            return this.max == null || this.max >= i;
         }
      }

      public boolean matchesSqr(long i) {
         if (this.minSq != null && this.minSq > i) {
            return false;
         } else {
            return this.maxSq == null || this.maxSq >= i;
         }
      }

      public static MinMaxBounds.Ints fromJson(@Nullable JsonElement jsonelement) {
         return fromJson(jsonelement, ANY, GsonHelper::convertToInt, MinMaxBounds.Ints::new);
      }

      public static MinMaxBounds.Ints fromReader(StringReader stringreader) throws CommandSyntaxException {
         return fromReader(stringreader, (integer) -> integer);
      }

      public static MinMaxBounds.Ints fromReader(StringReader stringreader, Function<Integer, Integer> function) throws CommandSyntaxException {
         return fromReader(stringreader, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, function);
      }
   }
}
