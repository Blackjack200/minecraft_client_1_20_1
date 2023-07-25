package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import org.slf4j.Logger;

public final class OptionInstance<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final OptionInstance.Enum<Boolean> BOOLEAN_VALUES = new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
   public static final OptionInstance.CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (component, obool) -> obool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
   private final OptionInstance.TooltipSupplier<T> tooltip;
   final Function<T, Component> toString;
   private final OptionInstance.ValueSet<T> values;
   private final Codec<T> codec;
   private final T initialValue;
   private final Consumer<T> onValueUpdate;
   final Component caption;
   T value;

   public static OptionInstance<Boolean> createBoolean(String s, boolean flag, Consumer<Boolean> consumer) {
      return createBoolean(s, noTooltip(), flag, consumer);
   }

   public static OptionInstance<Boolean> createBoolean(String s, boolean flag) {
      return createBoolean(s, noTooltip(), flag, (obool) -> {
      });
   }

   public static OptionInstance<Boolean> createBoolean(String s, OptionInstance.TooltipSupplier<Boolean> optioninstance_tooltipsupplier, boolean flag) {
      return createBoolean(s, optioninstance_tooltipsupplier, flag, (obool) -> {
      });
   }

   public static OptionInstance<Boolean> createBoolean(String s, OptionInstance.TooltipSupplier<Boolean> optioninstance_tooltipsupplier, boolean flag, Consumer<Boolean> consumer) {
      return createBoolean(s, optioninstance_tooltipsupplier, BOOLEAN_TO_STRING, flag, consumer);
   }

   public static OptionInstance<Boolean> createBoolean(String s, OptionInstance.TooltipSupplier<Boolean> optioninstance_tooltipsupplier, OptionInstance.CaptionBasedToString<Boolean> optioninstance_captionbasedtostring, boolean flag, Consumer<Boolean> consumer) {
      return new OptionInstance<>(s, optioninstance_tooltipsupplier, optioninstance_captionbasedtostring, BOOLEAN_VALUES, flag, consumer);
   }

   public OptionInstance(String s, OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, OptionInstance.CaptionBasedToString<T> optioninstance_captionbasedtostring, OptionInstance.ValueSet<T> optioninstance_valueset, T object, Consumer<T> consumer) {
      this(s, optioninstance_tooltipsupplier, optioninstance_captionbasedtostring, optioninstance_valueset, optioninstance_valueset.codec(), object, consumer);
   }

   public OptionInstance(String s, OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, OptionInstance.CaptionBasedToString<T> optioninstance_captionbasedtostring, OptionInstance.ValueSet<T> optioninstance_valueset, Codec<T> codec, T object, Consumer<T> consumer) {
      this.caption = Component.translatable(s);
      this.tooltip = optioninstance_tooltipsupplier;
      this.toString = (object1) -> optioninstance_captionbasedtostring.toString(this.caption, object1);
      this.values = optioninstance_valueset;
      this.codec = codec;
      this.initialValue = object;
      this.onValueUpdate = consumer;
      this.value = this.initialValue;
   }

   public static <T> OptionInstance.TooltipSupplier<T> noTooltip() {
      return (object) -> null;
   }

   public static <T> OptionInstance.TooltipSupplier<T> cachedConstantTooltip(Component component) {
      return (object) -> Tooltip.create(component);
   }

   public static <T extends OptionEnum> OptionInstance.CaptionBasedToString<T> forOptionEnum() {
      return (component, optionenum) -> optionenum.getCaption();
   }

   public AbstractWidget createButton(Options options, int i, int j, int k) {
      return this.createButton(options, i, j, k, (object) -> {
      });
   }

   public AbstractWidget createButton(Options options, int i, int j, int k, Consumer<T> consumer) {
      return this.values.createButton(this.tooltip, options, i, j, k, consumer).apply(this);
   }

   public T get() {
      return this.value;
   }

   public Codec<T> codec() {
      return this.codec;
   }

   public String toString() {
      return this.caption.getString();
   }

   public void set(T object) {
      T object1 = this.values.validateValue(object).orElseGet(() -> {
         LOGGER.error("Illegal option value " + object + " for " + this.caption);
         return this.initialValue;
      });
      if (!Minecraft.getInstance().isRunning()) {
         this.value = object1;
      } else {
         if (!Objects.equals(this.value, object1)) {
            this.value = object1;
            this.onValueUpdate.accept(this.value);
         }

      }
   }

   public OptionInstance.ValueSet<T> values() {
      return this.values;
   }

   public static record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {
      public CycleButton.ValueListSupplier<T> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
      }

      public Optional<T> validateValue(T object) {
         return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(object) ? Optional.of(object) : Optional.empty();
      }
   }

   public interface CaptionBasedToString<T> {
      Component toString(Component component, T object);
   }

   public static record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier, int encodableMaxInclusive) implements OptionInstance.IntRangeBase, OptionInstance.SliderableOrCyclableValueSet<Integer> {
      public Optional<Integer> validateValue(Integer integer) {
         return Optional.of(Mth.clamp(integer, this.minInclusive(), this.maxInclusive()));
      }

      public int maxInclusive() {
         return this.maxSupplier.getAsInt();
      }

      public Codec<Integer> codec() {
         return ExtraCodecs.validate(Codec.INT, (integer) -> {
            int i = this.encodableMaxInclusive + 1;
            return integer.compareTo(this.minInclusive) >= 0 && integer.compareTo(i) <= 0 ? DataResult.success(integer) : DataResult.error(() -> "Value " + integer + " outside of range [" + this.minInclusive + ":" + i + "]", integer);
         });
      }

      public boolean createCycleButton() {
         return true;
      }

      public CycleButton.ValueListSupplier<Integer> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
      }
   }

   interface CycleableValueSet<T> extends OptionInstance.ValueSet<T> {
      CycleButton.ValueListSupplier<T> valueListSupplier();

      default OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter() {
         return OptionInstance::set;
      }

      default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, Options options, int i, int j, int k, Consumer<T> consumer) {
         return (optioninstance) -> CycleButton.builder(optioninstance.toString).withValues(this.valueListSupplier()).withTooltip(optioninstance_tooltipsupplier).withInitialValue(optioninstance.value).create(i, j, k, 20, optioninstance.caption, (cyclebutton, object) -> {
               this.valueSetter().set(optioninstance, object);
               options.save();
               consumer.accept(object);
            });
      }

      public interface ValueSetter<T> {
         void set(OptionInstance<T> optioninstance, T object);
      }
   }

   public static record Enum<T>(List<T> values, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {
      public Optional<T> validateValue(T object) {
         return this.values.contains(object) ? Optional.of(object) : Optional.empty();
      }

      public CycleButton.ValueListSupplier<T> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(this.values);
      }
   }

   public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.IntRangeBase {
      public Optional<Integer> validateValue(Integer integer) {
         return integer.compareTo(this.minInclusive()) >= 0 && integer.compareTo(this.maxInclusive()) <= 0 ? Optional.of(integer) : Optional.empty();
      }

      public Codec<Integer> codec() {
         return Codec.intRange(this.minInclusive, this.maxInclusive + 1);
      }
   }

   interface IntRangeBase extends OptionInstance.SliderableValueSet<Integer> {
      int minInclusive();

      int maxInclusive();

      default double toSliderValue(Integer integer) {
         return (double)Mth.map((float)integer.intValue(), (float)this.minInclusive(), (float)this.maxInclusive(), 0.0F, 1.0F);
      }

      default Integer fromSliderValue(double d0) {
         return Mth.floor(Mth.map(d0, 0.0D, 1.0D, (double)this.minInclusive(), (double)this.maxInclusive()));
      }

      default <R> OptionInstance.SliderableValueSet<R> xmap(final IntFunction<? extends R> intfunction, final ToIntFunction<? super R> tointfunction) {
         return new OptionInstance.SliderableValueSet<R>() {
            public Optional<R> validateValue(R object) {
               return IntRangeBase.this.validateValue((T)Integer.valueOf(tointfunction.applyAsInt(object))).map(intfunction::apply);
            }

            public double toSliderValue(R object) {
               return IntRangeBase.this.toSliderValue(tointfunction.applyAsInt(object));
            }

            public R fromSliderValue(double d0) {
               return intfunction.apply(IntRangeBase.this.fromSliderValue(d0));
            }

            public Codec<R> codec() {
               return IntRangeBase.this.codec().xmap(intfunction::apply, tointfunction::applyAsInt);
            }
         };
      }
   }

   public static record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {
      public Optional<T> validateValue(T object) {
         return this.validateValue.apply(object);
      }

      public CycleButton.ValueListSupplier<T> valueListSupplier() {
         return CycleButton.ValueListSupplier.create(this.values.get());
      }
   }

   static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton {
      private final OptionInstance<N> instance;
      private final OptionInstance.SliderableValueSet<N> values;
      private final OptionInstance.TooltipSupplier<N> tooltipSupplier;
      private final Consumer<N> onValueChanged;

      OptionInstanceSliderButton(Options options, int i, int j, int k, int l, OptionInstance<N> optioninstance, OptionInstance.SliderableValueSet<N> optioninstance_sliderablevalueset, OptionInstance.TooltipSupplier<N> optioninstance_tooltipsupplier, Consumer<N> consumer) {
         super(options, i, j, k, l, optioninstance_sliderablevalueset.toSliderValue(optioninstance.get()));
         this.instance = optioninstance;
         this.values = optioninstance_sliderablevalueset;
         this.tooltipSupplier = optioninstance_tooltipsupplier;
         this.onValueChanged = consumer;
         this.updateMessage();
      }

      protected void updateMessage() {
         this.setMessage(this.instance.toString.apply(this.instance.get()));
         this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
      }

      protected void applyValue() {
         this.instance.set(this.values.fromSliderValue(this.value));
         this.options.save();
         this.onValueChanged.accept(this.instance.get());
      }
   }

   interface SliderableOrCyclableValueSet<T> extends OptionInstance.CycleableValueSet<T>, OptionInstance.SliderableValueSet<T> {
      boolean createCycleButton();

      default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, Options options, int i, int j, int k, Consumer<T> consumer) {
         return this.createCycleButton() ? OptionInstance.CycleableValueSet.super.createButton(optioninstance_tooltipsupplier, options, i, j, k, consumer) : OptionInstance.SliderableValueSet.super.createButton(optioninstance_tooltipsupplier, options, i, j, k, consumer);
      }
   }

   interface SliderableValueSet<T> extends OptionInstance.ValueSet<T> {
      double toSliderValue(T object);

      T fromSliderValue(double d0);

      default Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, Options options, int i, int j, int k, Consumer<T> consumer) {
         return (optioninstance) -> new OptionInstance.OptionInstanceSliderButton<>(options, i, j, k, 20, optioninstance, this, optioninstance_tooltipsupplier, consumer);
      }
   }

   @FunctionalInterface
   public interface TooltipSupplier<T> {
      @Nullable
      Tooltip apply(T object);
   }

   public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double> {
      INSTANCE;

      public Optional<Double> validateValue(Double odouble) {
         return odouble >= 0.0D && odouble <= 1.0D ? Optional.of(odouble) : Optional.empty();
      }

      public double toSliderValue(Double odouble) {
         return odouble;
      }

      public Double fromSliderValue(double d0) {
         return d0;
      }

      public <R> OptionInstance.SliderableValueSet<R> xmap(final DoubleFunction<? extends R> doublefunction, final ToDoubleFunction<? super R> todoublefunction) {
         return new OptionInstance.SliderableValueSet<R>() {
            public Optional<R> validateValue(R object) {
               return UnitDouble.this.validateValue(todoublefunction.applyAsDouble(object)).map(doublefunction::apply);
            }

            public double toSliderValue(R object) {
               return UnitDouble.this.toSliderValue(todoublefunction.applyAsDouble(object));
            }

            public R fromSliderValue(double d0) {
               return doublefunction.apply(UnitDouble.this.fromSliderValue(d0));
            }

            public Codec<R> codec() {
               return UnitDouble.this.codec().xmap(doublefunction::apply, todoublefunction::applyAsDouble);
            }
         };
      }

      public Codec<Double> codec() {
         return Codec.either(Codec.doubleRange(0.0D, 1.0D), Codec.BOOL).xmap((either) -> either.map((odouble) -> odouble, (obool) -> obool ? 1.0D : 0.0D), Either::left);
      }
   }

   interface ValueSet<T> {
      Function<OptionInstance<T>, AbstractWidget> createButton(OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, Options options, int i, int j, int k, Consumer<T> consumer);

      Optional<T> validateValue(T object);

      Codec<T> codec();
   }
}
