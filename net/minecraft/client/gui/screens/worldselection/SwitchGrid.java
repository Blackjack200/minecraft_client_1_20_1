package net.minecraft.client.gui.screens.worldselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

class SwitchGrid {
   private static final int DEFAULT_SWITCH_BUTTON_WIDTH = 44;
   private final List<SwitchGrid.LabeledSwitch> switches;

   SwitchGrid(List<SwitchGrid.LabeledSwitch> list) {
      this.switches = list;
   }

   public void refreshStates() {
      this.switches.forEach(SwitchGrid.LabeledSwitch::refreshState);
   }

   public static SwitchGrid.Builder builder(int i) {
      return new SwitchGrid.Builder(i);
   }

   public static class Builder {
      final int width;
      private final List<SwitchGrid.SwitchBuilder> switchBuilders = new ArrayList<>();
      int paddingLeft;
      int rowSpacing = 4;
      int rowCount;
      Optional<SwitchGrid.InfoUnderneathSettings> infoUnderneath = Optional.empty();

      public Builder(int i) {
         this.width = i;
      }

      void increaseRow() {
         ++this.rowCount;
      }

      public SwitchGrid.SwitchBuilder addSwitch(Component component, BooleanSupplier booleansupplier, Consumer<Boolean> consumer) {
         SwitchGrid.SwitchBuilder switchgrid_switchbuilder = new SwitchGrid.SwitchBuilder(component, booleansupplier, consumer, 44);
         this.switchBuilders.add(switchgrid_switchbuilder);
         return switchgrid_switchbuilder;
      }

      public SwitchGrid.Builder withPaddingLeft(int i) {
         this.paddingLeft = i;
         return this;
      }

      public SwitchGrid.Builder withRowSpacing(int i) {
         this.rowSpacing = i;
         return this;
      }

      public SwitchGrid build(Consumer<LayoutElement> consumer) {
         GridLayout gridlayout = (new GridLayout()).rowSpacing(this.rowSpacing);
         gridlayout.addChild(SpacerElement.width(this.width - 44), 0, 0);
         gridlayout.addChild(SpacerElement.width(44), 0, 1);
         List<SwitchGrid.LabeledSwitch> list = new ArrayList<>();
         this.rowCount = 0;

         for(SwitchGrid.SwitchBuilder switchgrid_switchbuilder : this.switchBuilders) {
            list.add(switchgrid_switchbuilder.build(this, gridlayout, 0));
         }

         gridlayout.arrangeElements();
         consumer.accept(gridlayout);
         SwitchGrid switchgrid = new SwitchGrid(list);
         switchgrid.refreshStates();
         return switchgrid;
      }

      public SwitchGrid.Builder withInfoUnderneath(int i, boolean flag) {
         this.infoUnderneath = Optional.of(new SwitchGrid.InfoUnderneathSettings(i, flag));
         return this;
      }
   }

   static record InfoUnderneathSettings(int maxInfoRows, boolean alwaysMaxHeight) {
      final int maxInfoRows;
      final boolean alwaysMaxHeight;
   }

   static record LabeledSwitch(CycleButton<Boolean> button, BooleanSupplier stateSupplier, @Nullable BooleanSupplier isActiveCondition) {
      public void refreshState() {
         this.button.setValue(this.stateSupplier.getAsBoolean());
         if (this.isActiveCondition != null) {
            this.button.active = this.isActiveCondition.getAsBoolean();
         }

      }
   }

   public static class SwitchBuilder {
      private final Component label;
      private final BooleanSupplier stateSupplier;
      private final Consumer<Boolean> onClicked;
      @Nullable
      private Component info;
      @Nullable
      private BooleanSupplier isActiveCondition;
      private final int buttonWidth;

      SwitchBuilder(Component component, BooleanSupplier booleansupplier, Consumer<Boolean> consumer, int i) {
         this.label = component;
         this.stateSupplier = booleansupplier;
         this.onClicked = consumer;
         this.buttonWidth = i;
      }

      public SwitchGrid.SwitchBuilder withIsActiveCondition(BooleanSupplier booleansupplier) {
         this.isActiveCondition = booleansupplier;
         return this;
      }

      public SwitchGrid.SwitchBuilder withInfo(Component component) {
         this.info = component;
         return this;
      }

      SwitchGrid.LabeledSwitch build(SwitchGrid.Builder switchgrid_builder, GridLayout gridlayout, int i) {
         switchgrid_builder.increaseRow();
         StringWidget stringwidget = (new StringWidget(this.label, Minecraft.getInstance().font)).alignLeft();
         gridlayout.addChild(stringwidget, switchgrid_builder.rowCount, i, gridlayout.newCellSettings().align(0.0F, 0.5F).paddingLeft(switchgrid_builder.paddingLeft));
         Optional<SwitchGrid.InfoUnderneathSettings> optional = switchgrid_builder.infoUnderneath;
         CycleButton.Builder<Boolean> cyclebutton_builder = CycleButton.onOffBuilder(this.stateSupplier.getAsBoolean());
         cyclebutton_builder.displayOnlyValue();
         boolean flag = this.info != null && !optional.isPresent();
         if (flag) {
            Tooltip tooltip = Tooltip.create(this.info);
            cyclebutton_builder.withTooltip((obool1) -> tooltip);
         }

         if (this.info != null && !flag) {
            cyclebutton_builder.withCustomNarration((cyclebutton3) -> CommonComponents.joinForNarration(this.label, cyclebutton3.createDefaultNarrationMessage(), this.info));
         } else {
            cyclebutton_builder.withCustomNarration((cyclebutton2) -> CommonComponents.joinForNarration(this.label, cyclebutton2.createDefaultNarrationMessage()));
         }

         CycleButton<Boolean> cyclebutton = cyclebutton_builder.create(0, 0, this.buttonWidth, 20, Component.empty(), (cyclebutton1, obool) -> this.onClicked.accept(obool));
         if (this.isActiveCondition != null) {
            cyclebutton.active = this.isActiveCondition.getAsBoolean();
         }

         gridlayout.addChild(cyclebutton, switchgrid_builder.rowCount, i + 1, gridlayout.newCellSettings().alignHorizontallyRight());
         if (this.info != null) {
            optional.ifPresent((switchgrid_infounderneathsettings) -> {
               Component component = this.info.copy().withStyle(ChatFormatting.GRAY);
               Font font = Minecraft.getInstance().font;
               MultiLineTextWidget multilinetextwidget = new MultiLineTextWidget(component, font);
               multilinetextwidget.setMaxWidth(switchgrid_builder.width - switchgrid_builder.paddingLeft - this.buttonWidth);
               multilinetextwidget.setMaxRows(switchgrid_infounderneathsettings.maxInfoRows());
               switchgrid_builder.increaseRow();
               int k = switchgrid_infounderneathsettings.alwaysMaxHeight ? 9 * switchgrid_infounderneathsettings.maxInfoRows - multilinetextwidget.getHeight() : 0;
               gridlayout.addChild(multilinetextwidget, switchgrid_builder.rowCount, i, gridlayout.newCellSettings().paddingTop(-switchgrid_builder.rowSpacing).paddingBottom(k));
            });
         }

         return new SwitchGrid.LabeledSwitch(cyclebutton, this.stateSupplier, this.isActiveCondition);
      }
   }
}
