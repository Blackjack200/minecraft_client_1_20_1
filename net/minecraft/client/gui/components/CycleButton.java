package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

public class CycleButton<T> extends AbstractButton {
   public static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
   private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
   private final Component name;
   private int index;
   private T value;
   private final CycleButton.ValueListSupplier<T> values;
   private final Function<T, Component> valueStringifier;
   private final Function<CycleButton<T>, MutableComponent> narrationProvider;
   private final CycleButton.OnValueChange<T> onValueChange;
   private final boolean displayOnlyValue;
   private final OptionInstance.TooltipSupplier<T> tooltipSupplier;

   CycleButton(int i, int j, int k, int l, Component component, Component component1, int i1, T object, CycleButton.ValueListSupplier<T> cyclebutton_valuelistsupplier, Function<T, Component> function, Function<CycleButton<T>, MutableComponent> function1, CycleButton.OnValueChange<T> cyclebutton_onvaluechange, OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier, boolean flag) {
      super(i, j, k, l, component);
      this.name = component1;
      this.index = i1;
      this.value = object;
      this.values = cyclebutton_valuelistsupplier;
      this.valueStringifier = function;
      this.narrationProvider = function1;
      this.onValueChange = cyclebutton_onvaluechange;
      this.displayOnlyValue = flag;
      this.tooltipSupplier = optioninstance_tooltipsupplier;
      this.updateTooltip();
   }

   private void updateTooltip() {
      this.setTooltip(this.tooltipSupplier.apply(this.value));
   }

   public void onPress() {
      if (Screen.hasShiftDown()) {
         this.cycleValue(-1);
      } else {
         this.cycleValue(1);
      }

   }

   private void cycleValue(int i) {
      List<T> list = this.values.getSelectedList();
      this.index = Mth.positiveModulo(this.index + i, list.size());
      T object = list.get(this.index);
      this.updateValue(object);
      this.onValueChange.onValueChange(this, object);
   }

   private T getCycledValue(int i) {
      List<T> list = this.values.getSelectedList();
      return list.get(Mth.positiveModulo(this.index + i, list.size()));
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      if (d2 > 0.0D) {
         this.cycleValue(-1);
      } else if (d2 < 0.0D) {
         this.cycleValue(1);
      }

      return true;
   }

   public void setValue(T object) {
      List<T> list = this.values.getSelectedList();
      int i = list.indexOf(object);
      if (i != -1) {
         this.index = i;
      }

      this.updateValue(object);
   }

   private void updateValue(T object) {
      Component component = this.createLabelForValue(object);
      this.setMessage(component);
      this.value = object;
      this.updateTooltip();
   }

   private Component createLabelForValue(T object) {
      return (Component)(this.displayOnlyValue ? this.valueStringifier.apply(object) : this.createFullName(object));
   }

   private MutableComponent createFullName(T object) {
      return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(object));
   }

   public T getValue() {
      return this.value;
   }

   protected MutableComponent createNarrationMessage() {
      return this.narrationProvider.apply(this);
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
      if (this.active) {
         T object = this.getCycledValue(1);
         Component component = this.createLabelForValue(object);
         if (this.isFocused()) {
            narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.focused", component));
         } else {
            narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.hovered", component));
         }
      }

   }

   public MutableComponent createDefaultNarrationMessage() {
      return wrapDefaultNarrationMessage((Component)(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
   }

   public static <T> CycleButton.Builder<T> builder(Function<T, Component> function) {
      return new CycleButton.Builder<>(function);
   }

   public static CycleButton.Builder<Boolean> booleanBuilder(Component component, Component component1) {
      return (new CycleButton.Builder<>((obool) -> obool ? component : component1)).withValues(BOOLEAN_OPTIONS);
   }

   public static CycleButton.Builder<Boolean> onOffBuilder() {
      return (new CycleButton.Builder<>((obool) -> obool ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)).withValues(BOOLEAN_OPTIONS);
   }

   public static CycleButton.Builder<Boolean> onOffBuilder(boolean flag) {
      return onOffBuilder().withInitialValue(flag);
   }

   public static class Builder<T> {
      private int initialIndex;
      @Nullable
      private T initialValue;
      private final Function<T, Component> valueStringifier;
      private OptionInstance.TooltipSupplier<T> tooltipSupplier = (object) -> null;
      private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
      private CycleButton.ValueListSupplier<T> values = CycleButton.ValueListSupplier.create(ImmutableList.of());
      private boolean displayOnlyValue;

      public Builder(Function<T, Component> function) {
         this.valueStringifier = function;
      }

      public CycleButton.Builder<T> withValues(Collection<T> collection) {
         return this.withValues(CycleButton.ValueListSupplier.create(collection));
      }

      @SafeVarargs
      public final CycleButton.Builder<T> withValues(T... aobject) {
         return this.withValues(ImmutableList.copyOf(aobject));
      }

      public CycleButton.Builder<T> withValues(List<T> list, List<T> list1) {
         return this.withValues(CycleButton.ValueListSupplier.create(CycleButton.DEFAULT_ALT_LIST_SELECTOR, list, list1));
      }

      public CycleButton.Builder<T> withValues(BooleanSupplier booleansupplier, List<T> list, List<T> list1) {
         return this.withValues(CycleButton.ValueListSupplier.create(booleansupplier, list, list1));
      }

      public CycleButton.Builder<T> withValues(CycleButton.ValueListSupplier<T> cyclebutton_valuelistsupplier) {
         this.values = cyclebutton_valuelistsupplier;
         return this;
      }

      public CycleButton.Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> optioninstance_tooltipsupplier) {
         this.tooltipSupplier = optioninstance_tooltipsupplier;
         return this;
      }

      public CycleButton.Builder<T> withInitialValue(T object) {
         this.initialValue = object;
         int i = this.values.getDefaultList().indexOf(object);
         if (i != -1) {
            this.initialIndex = i;
         }

         return this;
      }

      public CycleButton.Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> function) {
         this.narrationProvider = function;
         return this;
      }

      public CycleButton.Builder<T> displayOnlyValue() {
         this.displayOnlyValue = true;
         return this;
      }

      public CycleButton<T> create(int i, int j, int k, int l, Component component) {
         return this.create(i, j, k, l, component, (cyclebutton, object) -> {
         });
      }

      public CycleButton<T> create(int i, int j, int k, int l, Component component, CycleButton.OnValueChange<T> cyclebutton_onvaluechange) {
         List<T> list = this.values.getDefaultList();
         if (list.isEmpty()) {
            throw new IllegalStateException("No values for cycle button");
         } else {
            T object = (T)(this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
            Component component1 = this.valueStringifier.apply(object);
            Component component2 = (Component)(this.displayOnlyValue ? component1 : CommonComponents.optionNameValue(component, component1));
            return new CycleButton<>(i, j, k, l, component2, component, this.initialIndex, object, this.values, this.valueStringifier, this.narrationProvider, cyclebutton_onvaluechange, this.tooltipSupplier, this.displayOnlyValue);
         }
      }
   }

   public interface OnValueChange<T> {
      void onValueChange(CycleButton<T> cyclebutton, T object);
   }

   public interface ValueListSupplier<T> {
      List<T> getSelectedList();

      List<T> getDefaultList();

      static <T> CycleButton.ValueListSupplier<T> create(Collection<T> collection) {
         final List<T> list = ImmutableList.copyOf(collection);
         return new CycleButton.ValueListSupplier<T>() {
            public List<T> getSelectedList() {
               return list;
            }

            public List<T> getDefaultList() {
               return list;
            }
         };
      }

      static <T> CycleButton.ValueListSupplier<T> create(final BooleanSupplier booleansupplier, List<T> list, List<T> list1) {
         final List<T> list2 = ImmutableList.copyOf(list);
         final List<T> list3 = ImmutableList.copyOf(list1);
         return new CycleButton.ValueListSupplier<T>() {
            public List<T> getSelectedList() {
               return booleansupplier.getAsBoolean() ? list3 : list2;
            }

            public List<T> getDefaultList() {
               return list2;
            }
         };
      }
   }
}
