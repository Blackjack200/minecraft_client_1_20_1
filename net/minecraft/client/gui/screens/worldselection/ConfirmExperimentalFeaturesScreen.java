package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.flag.FeatureFlags;

public class ConfirmExperimentalFeaturesScreen extends Screen {
   private static final Component TITLE = Component.translatable("selectWorld.experimental.title");
   private static final Component MESSAGE = Component.translatable("selectWorld.experimental.message");
   private static final Component DETAILS_BUTTON = Component.translatable("selectWorld.experimental.details");
   private static final int COLUMN_SPACING = 10;
   private static final int DETAILS_BUTTON_WIDTH = 100;
   private final BooleanConsumer callback;
   final Collection<Pack> enabledPacks;
   private final GridLayout layout = (new GridLayout()).columnSpacing(10).rowSpacing(20);

   public ConfirmExperimentalFeaturesScreen(Collection<Pack> collection, BooleanConsumer booleanconsumer) {
      super(TITLE);
      this.enabledPacks = collection;
      this.callback = booleanconsumer;
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
   }

   protected void init() {
      super.init();
      GridLayout.RowHelper gridlayout_rowhelper = this.layout.createRowHelper(2);
      LayoutSettings layoutsettings = gridlayout_rowhelper.newCellSettings().alignHorizontallyCenter();
      gridlayout_rowhelper.addChild(new StringWidget(this.title, this.font), 2, layoutsettings);
      MultiLineTextWidget multilinetextwidget = gridlayout_rowhelper.addChild((new MultiLineTextWidget(MESSAGE, this.font)).setCentered(true), 2, layoutsettings);
      multilinetextwidget.setMaxWidth(310);
      gridlayout_rowhelper.addChild(Button.builder(DETAILS_BUTTON, (button2) -> this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen.DetailsScreen())).width(100).build(), 2, layoutsettings);
      gridlayout_rowhelper.addChild(Button.builder(CommonComponents.GUI_PROCEED, (button1) -> this.callback.accept(true)).build());
      gridlayout_rowhelper.addChild(Button.builder(CommonComponents.GUI_BACK, (button) -> this.callback.accept(false)).build());
      this.layout.visitWidgets((guieventlistener) -> {
         AbstractWidget var10000 = this.addRenderableWidget(guieventlistener);
      });
      this.layout.arrangeElements();
      this.repositionElements();
   }

   protected void repositionElements() {
      FrameLayout.alignInRectangle(this.layout, 0, 0, this.width, this.height, 0.5F, 0.5F);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
   }

   public void onClose() {
      this.callback.accept(false);
   }

   class DetailsScreen extends Screen {
      private ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList packList;

      DetailsScreen() {
         super(Component.translatable("selectWorld.experimental.details.title"));
      }

      public void onClose() {
         this.minecraft.setScreen(ConfirmExperimentalFeaturesScreen.this);
      }

      protected void init() {
         super.init();
         this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.onClose()).bounds(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20).build());
         this.packList = new ConfirmExperimentalFeaturesScreen.DetailsScreen.PackList(this.minecraft, ConfirmExperimentalFeaturesScreen.this.enabledPacks);
         this.addWidget(this.packList);
      }

      public void render(GuiGraphics guigraphics, int i, int j, float f) {
         this.renderBackground(guigraphics);
         this.packList.render(guigraphics, i, j, f);
         guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
         super.render(guigraphics, i, j, f);
      }

      class PackList extends ObjectSelectionList<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
         public PackList(Minecraft minecraft, Collection<Pack> collection) {
            super(minecraft, DetailsScreen.this.width, DetailsScreen.this.height, 32, DetailsScreen.this.height - 64, (9 + 2) * 3);

            for(Pack pack : collection) {
               String s = FeatureFlags.printMissingFlags(FeatureFlags.VANILLA_SET, pack.getRequestedFeatures());
               if (!s.isEmpty()) {
                  Component component = ComponentUtils.mergeStyles(pack.getTitle().copy(), Style.EMPTY.withBold(true));
                  Component component1 = Component.translatable("selectWorld.experimental.details.entry", s);
                  this.addEntry(DetailsScreen.this.new PackListEntry(component, component1, MultiLineLabel.create(DetailsScreen.this.font, component1, this.getRowWidth())));
               }
            }

         }

         public int getRowWidth() {
            return this.width * 3 / 4;
         }
      }

      class PackListEntry extends ObjectSelectionList.Entry<ConfirmExperimentalFeaturesScreen.DetailsScreen.PackListEntry> {
         private final Component packId;
         private final Component message;
         private final MultiLineLabel splitMessage;

         PackListEntry(Component component, Component component1, MultiLineLabel multilinelabel) {
            this.packId = component;
            this.message = component1;
            this.splitMessage = multilinelabel;
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            guigraphics.drawString(DetailsScreen.this.minecraft.font, this.packId, k, j, 16777215);
            this.splitMessage.renderLeftAligned(guigraphics, k, j + 12, 9, 16777215);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.packId, this.message));
         }
      }
   }
}
