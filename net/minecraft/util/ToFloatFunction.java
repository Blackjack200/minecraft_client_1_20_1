package net.minecraft.util;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.function.Function;

public interface ToFloatFunction<C> {
   ToFloatFunction<Float> IDENTITY = createUnlimited((f) -> f);

   float apply(C object);

   float minValue();

   float maxValue();

   static ToFloatFunction<Float> createUnlimited(final Float2FloatFunction float2floatfunction) {
      return new ToFloatFunction<Float>() {
         public float apply(Float ofloat) {
            return float2floatfunction.apply(ofloat);
         }

         public float minValue() {
            return Float.NEGATIVE_INFINITY;
         }

         public float maxValue() {
            return Float.POSITIVE_INFINITY;
         }
      };
   }

   default <C2> ToFloatFunction<C2> comap(final Function<C2, C> function) {
      final ToFloatFunction<C> tofloatfunction = this;
      return new ToFloatFunction<C2>() {
         public float apply(C2 object) {
            return tofloatfunction.apply(function.apply(object));
         }

         public float minValue() {
            return tofloatfunction.minValue();
         }

         public float maxValue() {
            return tofloatfunction.maxValue();
         }
      };
   }
}
