package net.minecraft.client.gui.screens.telemetry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleConsumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TelemetryEventWidget extends AbstractScrollWidget {
   private static final int HEADER_HORIZONTAL_PADDING = 32;
   private static final String TELEMETRY_REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
   private static final String TELEMETRY_OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
   private static final Component PROPERTY_TITLE = Component.translatable("telemetry_info.property_title").withStyle(ChatFormatting.UNDERLINE);
   private final Font font;
   private TelemetryEventWidget.Content content;
   @Nullable
   private DoubleConsumer onScrolledListener;

   public TelemetryEventWidget(int i, int j, int k, int l, Font font) {
      super(i, j, k, l, Component.empty());
      this.font = font;
      this.content = this.buildContent(Minecraft.getInstance().telemetryOptInExtra());
   }

   public void onOptInChanged(boolean flag) {
      this.content = this.buildContent(flag);
      this.setScrollAmount(this.scrollAmount());
   }

   private TelemetryEventWidget.Content buildContent(boolean flag) {
      TelemetryEventWidget.ContentBuilder telemetryeventwidget_contentbuilder = new TelemetryEventWidget.ContentBuilder(this.containerWidth());
      List<TelemetryEventType> list = new ArrayList<>(TelemetryEventType.values());
      list.sort(Comparator.comparing(TelemetryEventType::isOptIn));
      if (!flag) {
         list.removeIf(TelemetryEventType::isOptIn);
      }

      for(int i = 0; i < list.size(); ++i) {
         TelemetryEventType telemetryeventtype = list.get(i);
         this.addEventType(telemetryeventwidget_contentbuilder, telemetryeventtype);
         if (i < list.size() - 1) {
            telemetryeventwidget_contentbuilder.addSpacer(9);
         }
      }

      return telemetryeventwidget_contentbuilder.build();
   }

   public void setOnScrolledListener(@Nullable DoubleConsumer doubleconsumer) {
      this.onScrolledListener = doubleconsumer;
   }

   protected void setScrollAmount(double d0) {
      super.setScrollAmount(d0);
      if (this.onScrolledListener != null) {
         this.onScrolledListener.accept(this.scrollAmount());
      }

   }

   protected int getInnerHeight() {
      return this.content.container().getHeight();
   }

   protected double scrollRate() {
      return 9.0D;
   }

   protected void renderContents(GuiGraphics guigraphics, int i, int j, float f) {
      int k = this.getY() + this.innerPadding();
      int l = this.getX() + this.innerPadding();
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((double)l, (double)k, 0.0D);
      this.content.container().visitWidgets((abstractwidget) -> abstractwidget.render(guigraphics, i, j, f));
      guigraphics.pose().popPose();
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, this.content.narration());
   }

   private void addEventType(TelemetryEventWidget.ContentBuilder telemetryeventwidget_contentbuilder, TelemetryEventType telemetryeventtype) {
      String s = telemetryeventtype.isOptIn() ? "telemetry.event.optional" : "telemetry.event.required";
      telemetryeventwidget_contentbuilder.addHeader(this.font, Component.translatable(s, telemetryeventtype.title()));
      telemetryeventwidget_contentbuilder.addHeader(this.font, telemetryeventtype.description().withStyle(ChatFormatting.GRAY));
      telemetryeventwidget_contentbuilder.addSpacer(9 / 2);
      telemetryeventwidget_contentbuilder.addLine(this.font, PROPERTY_TITLE, 2);
      this.addEventTypeProperties(telemetryeventtype, telemetryeventwidget_contentbuilder);
   }

   private void addEventTypeProperties(TelemetryEventType telemetryeventtype, TelemetryEventWidget.ContentBuilder telemetryeventwidget_contentbuilder) {
      for(TelemetryProperty<?> telemetryproperty : telemetryeventtype.properties()) {
         telemetryeventwidget_contentbuilder.addLine(this.font, telemetryproperty.title());
      }

   }

   private int containerWidth() {
      return this.width - this.totalInnerPadding();
   }

   static record Content(GridLayout container, Component narration) {
   }

   static class ContentBuilder {
      private final int width;
      private final GridLayout grid;
      private final GridLayout.RowHelper helper;
      private final LayoutSettings alignHeader;
      private final MutableComponent narration = Component.empty();

      public ContentBuilder(int i) {
         this.width = i;
         this.grid = new GridLayout();
         this.grid.defaultCellSetting().alignHorizontallyLeft();
         this.helper = this.grid.createRowHelper(1);
         this.helper.addChild(SpacerElement.width(i));
         this.alignHeader = this.helper.newCellSettings().alignHorizontallyCenter().paddingHorizontal(32);
      }

      public void addLine(Font font, Component component) {
         this.addLine(font, component, 0);
      }

      public void addLine(Font font, Component component, int i) {
         this.helper.addChild((new MultiLineTextWidget(component, font)).setMaxWidth(this.width), this.helper.newCellSettings().paddingBottom(i));
         this.narration.append(component).append("\n");
      }

      public void addHeader(Font font, Component component) {
         this.helper.addChild((new MultiLineTextWidget(component, font)).setMaxWidth(this.width - 64).setCentered(true), this.alignHeader);
         this.narration.append(component).append("\n");
      }

      public void addSpacer(int i) {
         this.helper.addChild(SpacerElement.height(i));
      }

      public TelemetryEventWidget.Content build() {
         this.grid.arrangeElements();
         return new TelemetryEventWidget.Content(this.grid, this.narration);
      }
   }
}
