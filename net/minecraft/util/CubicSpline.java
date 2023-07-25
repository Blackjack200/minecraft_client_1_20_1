package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.mutable.MutableObject;

public interface CubicSpline<C, I extends ToFloatFunction<C>> extends ToFloatFunction<C> {
   @VisibleForDebug
   String parityString();

   CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> cubicspline_coordinatevisitor);

   static <C, I extends ToFloatFunction<C>> Codec<CubicSpline<C, I>> codec(Codec<I> codec) {
      MutableObject<Codec<CubicSpline<C, I>>> mutableobject = new MutableObject<>();
      Codec<Point<C, I>> codec1 = RecordCodecBuilder.create((recordcodecbuilder_instance1) -> recordcodecbuilder_instance1.group(Codec.FLOAT.fieldOf("location").forGetter(Point::location), ExtraCodecs.lazyInitializedCodec(mutableobject::getValue).fieldOf("value").forGetter(Point::value), Codec.FLOAT.fieldOf("derivative").forGetter(Point::derivative)).apply(recordcodecbuilder_instance1, (f, cubicspline1, f1) -> {
            record Point<C, I extends ToFloatFunction<C>>(float location, CubicSpline<C, I> value, float derivative) {
            }

            return new Point<>((float)f, cubicspline1, (float)f1);
         }));
      Codec<CubicSpline.Multipoint<C, I>> codec2 = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(codec.fieldOf("coordinate").forGetter(CubicSpline.Multipoint::coordinate), ExtraCodecs.nonEmptyList(codec1.listOf()).fieldOf("points").forGetter((cubicspline_multipoint1) -> IntStream.range(0, cubicspline_multipoint1.locations.length).mapToObj((j) -> new Point<>(cubicspline_multipoint1.locations()[j], (CubicSpline)cubicspline_multipoint1.values().get(j), cubicspline_multipoint1.derivatives()[j])).toList())).apply(recordcodecbuilder_instance, (tofloatfunction, list) -> {
            float[] afloat = new float[list.size()];
            ImmutableList.Builder<CubicSpline<C, I>> immutablelist_builder = ImmutableList.builder();
            float[] afloat1 = new float[list.size()];

            for(int i = 0; i < list.size(); ++i) {
               Point<C, I> cubicspline_1point = list.get(i);
               afloat[i] = cubicspline_1point.location();
               immutablelist_builder.add(cubicspline_1point.value());
               afloat1[i] = cubicspline_1point.derivative();
            }

            return CubicSpline.Multipoint.create(tofloatfunction, afloat, immutablelist_builder.build(), afloat1);
         }));
      mutableobject.setValue(Codec.either(Codec.FLOAT, codec2).xmap((either) -> either.map(CubicSpline.Constant::new, (cubicspline_multipoint) -> cubicspline_multipoint), (cubicspline) -> {
         Either var10000;
         if (cubicspline instanceof CubicSpline.Constant<C, I> cubicspline_constant) {
            var10000 = Either.left(cubicspline_constant.value());
         } else {
            var10000 = Either.right(cubicspline);
         }

         return var10000;
      }));
      return mutableobject.getValue();
   }

   static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> constant(float f) {
      return new CubicSpline.Constant<>(f);
   }

   static <C, I extends ToFloatFunction<C>> CubicSpline.Builder<C, I> builder(I tofloatfunction) {
      return new CubicSpline.Builder<>(tofloatfunction);
   }

   static <C, I extends ToFloatFunction<C>> CubicSpline.Builder<C, I> builder(I tofloatfunction, ToFloatFunction<Float> tofloatfunction1) {
      return new CubicSpline.Builder<>(tofloatfunction, tofloatfunction1);
   }

   public static final class Builder<C, I extends ToFloatFunction<C>> {
      private final I coordinate;
      private final ToFloatFunction<Float> valueTransformer;
      private final FloatList locations = new FloatArrayList();
      private final List<CubicSpline<C, I>> values = Lists.newArrayList();
      private final FloatList derivatives = new FloatArrayList();

      protected Builder(I tofloatfunction) {
         this(tofloatfunction, ToFloatFunction.IDENTITY);
      }

      protected Builder(I tofloatfunction, ToFloatFunction<Float> tofloatfunction1) {
         this.coordinate = tofloatfunction;
         this.valueTransformer = tofloatfunction1;
      }

      public CubicSpline.Builder<C, I> addPoint(float f, float f1) {
         return this.addPoint(f, new CubicSpline.Constant<>(this.valueTransformer.apply(f1)), 0.0F);
      }

      public CubicSpline.Builder<C, I> addPoint(float f, float f1, float f2) {
         return this.addPoint(f, new CubicSpline.Constant<>(this.valueTransformer.apply(f1)), f2);
      }

      public CubicSpline.Builder<C, I> addPoint(float f, CubicSpline<C, I> cubicspline) {
         return this.addPoint(f, cubicspline, 0.0F);
      }

      private CubicSpline.Builder<C, I> addPoint(float f, CubicSpline<C, I> cubicspline, float f1) {
         if (!this.locations.isEmpty() && f <= this.locations.getFloat(this.locations.size() - 1)) {
            throw new IllegalArgumentException("Please register points in ascending order");
         } else {
            this.locations.add(f);
            this.values.add(cubicspline);
            this.derivatives.add(f1);
            return this;
         }
      }

      public CubicSpline<C, I> build() {
         if (this.locations.isEmpty()) {
            throw new IllegalStateException("No elements added");
         } else {
            return CubicSpline.Multipoint.create(this.coordinate, this.locations.toFloatArray(), ImmutableList.copyOf(this.values), this.derivatives.toFloatArray());
         }
      }
   }

   @VisibleForDebug
   public static record Constant<C, I extends ToFloatFunction<C>>(float value) implements CubicSpline<C, I> {
      public float apply(C object) {
         return this.value;
      }

      public String parityString() {
         return String.format(Locale.ROOT, "k=%.3f", this.value);
      }

      public float minValue() {
         return this.value;
      }

      public float maxValue() {
         return this.value;
      }

      public CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> cubicspline_coordinatevisitor) {
         return this;
      }
   }

   public interface CoordinateVisitor<I> {
      I visit(I object);
   }

   @VisibleForDebug
   public static record Multipoint<C, I extends ToFloatFunction<C>>(I coordinate, float[] locations, List<CubicSpline<C, I>> values, float[] derivatives, float minValue, float maxValue) implements CubicSpline<C, I> {
      final float[] locations;

      public Multipoint {
         validateSizes(afloat, list, afloat1);
      }

      static <C, I extends ToFloatFunction<C>> CubicSpline.Multipoint<C, I> create(I tofloatfunction, float[] afloat, List<CubicSpline<C, I>> list, float[] afloat1) {
         validateSizes(afloat, list, afloat1);
         int i = afloat.length - 1;
         float f = Float.POSITIVE_INFINITY;
         float f1 = Float.NEGATIVE_INFINITY;
         float f2 = tofloatfunction.minValue();
         float f3 = tofloatfunction.maxValue();
         if (f2 < afloat[0]) {
            float f4 = linearExtend(f2, afloat, list.get(0).minValue(), afloat1, 0);
            float f5 = linearExtend(f2, afloat, list.get(0).maxValue(), afloat1, 0);
            f = Math.min(f, Math.min(f4, f5));
            f1 = Math.max(f1, Math.max(f4, f5));
         }

         if (f3 > afloat[i]) {
            float f6 = linearExtend(f3, afloat, list.get(i).minValue(), afloat1, i);
            float f7 = linearExtend(f3, afloat, list.get(i).maxValue(), afloat1, i);
            f = Math.min(f, Math.min(f6, f7));
            f1 = Math.max(f1, Math.max(f6, f7));
         }

         for(CubicSpline<C, I> cubicspline : list) {
            f = Math.min(f, cubicspline.minValue());
            f1 = Math.max(f1, cubicspline.maxValue());
         }

         for(int j = 0; j < i; ++j) {
            float f8 = afloat[j];
            float f9 = afloat[j + 1];
            float f10 = f9 - f8;
            CubicSpline<C, I> cubicspline1 = list.get(j);
            CubicSpline<C, I> cubicspline2 = list.get(j + 1);
            float f11 = cubicspline1.minValue();
            float f12 = cubicspline1.maxValue();
            float f13 = cubicspline2.minValue();
            float f14 = cubicspline2.maxValue();
            float f15 = afloat1[j];
            float f16 = afloat1[j + 1];
            if (f15 != 0.0F || f16 != 0.0F) {
               float f17 = f15 * f10;
               float f18 = f16 * f10;
               float f19 = Math.min(f11, f13);
               float f20 = Math.max(f12, f14);
               float f21 = f17 - f14 + f11;
               float f22 = f17 - f13 + f12;
               float f23 = -f18 + f13 - f12;
               float f24 = -f18 + f14 - f11;
               float f25 = Math.min(f21, f23);
               float f26 = Math.max(f22, f24);
               f = Math.min(f, f19 + 0.25F * f25);
               f1 = Math.max(f1, f20 + 0.25F * f26);
            }
         }

         return new CubicSpline.Multipoint<>(tofloatfunction, afloat, list, afloat1, f, f1);
      }

      private static float linearExtend(float f, float[] afloat, float f1, float[] afloat1, int i) {
         float f2 = afloat1[i];
         return f2 == 0.0F ? f1 : f1 + f2 * (f - afloat[i]);
      }

      private static <C, I extends ToFloatFunction<C>> void validateSizes(float[] afloat, List<CubicSpline<C, I>> list, float[] afloat1) {
         if (afloat.length == list.size() && afloat.length == afloat1.length) {
            if (afloat.length == 0) {
               throw new IllegalArgumentException("Cannot create a multipoint spline with no points");
            }
         } else {
            throw new IllegalArgumentException("All lengths must be equal, got: " + afloat.length + " " + list.size() + " " + afloat1.length);
         }
      }

      public float apply(C object) {
         float f = this.coordinate.apply(object);
         int i = findIntervalStart(this.locations, f);
         int j = this.locations.length - 1;
         if (i < 0) {
            return linearExtend(f, this.locations, this.values.get(0).apply(object), this.derivatives, 0);
         } else if (i == j) {
            return linearExtend(f, this.locations, this.values.get(j).apply(object), this.derivatives, j);
         } else {
            float f1 = this.locations[i];
            float f2 = this.locations[i + 1];
            float f3 = (f - f1) / (f2 - f1);
            ToFloatFunction<C> tofloatfunction = this.values.get(i);
            ToFloatFunction<C> tofloatfunction1 = this.values.get(i + 1);
            float f4 = this.derivatives[i];
            float f5 = this.derivatives[i + 1];
            float f6 = tofloatfunction.apply(object);
            float f7 = tofloatfunction1.apply(object);
            float f8 = f4 * (f2 - f1) - (f7 - f6);
            float f9 = -f5 * (f2 - f1) + (f7 - f6);
            return Mth.lerp(f3, f6, f7) + f3 * (1.0F - f3) * Mth.lerp(f3, f8, f9);
         }
      }

      private static int findIntervalStart(float[] afloat, float f) {
         return Mth.binarySearch(0, afloat.length, (i) -> f < afloat[i]) - 1;
      }

      @VisibleForTesting
      public String parityString() {
         return "Spline{coordinate=" + this.coordinate + ", locations=" + this.toString(this.locations) + ", derivatives=" + this.toString(this.derivatives) + ", values=" + (String)this.values.stream().map(CubicSpline::parityString).collect(Collectors.joining(", ", "[", "]")) + "}";
      }

      private String toString(float[] afloat) {
         return "[" + (String)IntStream.range(0, afloat.length).mapToDouble((i) -> (double)afloat[i]).mapToObj((d0) -> String.format(Locale.ROOT, "%.3f", d0)).collect(Collectors.joining(", ")) + "]";
      }

      public CubicSpline<C, I> mapAll(CubicSpline.CoordinateVisitor<I> cubicspline_coordinatevisitor) {
         return create(cubicspline_coordinatevisitor.visit(this.coordinate), this.locations, this.values().stream().map((cubicspline) -> cubicspline.mapAll(cubicspline_coordinatevisitor)).toList(), this.derivatives);
      }
   }
}
