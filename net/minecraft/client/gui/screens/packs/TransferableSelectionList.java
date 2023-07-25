package net.minecraft.client.gui.screens.packs;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;

public class TransferableSelectionList extends ObjectSelectionList<TransferableSelectionList.PackEntry> {
   static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
   static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
   static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
   private final Component title;
   final PackSelectionScreen screen;

   public TransferableSelectionList(Minecraft minecraft, PackSelectionScreen packselectionscreen, int i, int j, Component component) {
      super(minecraft, i, j, 32, j - 55 + 4, 36);
      this.screen = packselectionscreen;
      this.title = component;
      this.centerListVertically = false;
      this.setRenderHeader(true, (int)(9.0F * 1.5F));
   }

   protected void renderHeader(GuiGraphics guigraphics, int i, int j) {
      Component component = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
      guigraphics.drawString(this.minecraft.font, component, i + this.width / 2 - this.minecraft.font.width(component) / 2, Math.min(this.y0 + 3, j), 16777215, false);
   }

   public int getRowWidth() {
      return this.width;
   }

   protected int getScrollbarPosition() {
      return this.x1 - 6;
   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.getSelected() != null) {
         switch (i) {
            case 32:
            case 257:
               this.getSelected().keyboardSelection();
               return true;
            default:
               if (Screen.hasShiftDown()) {
                  switch (i) {
                     case 264:
                        this.getSelected().keyboardMoveDown();
                        return true;
                     case 265:
                        this.getSelected().keyboardMoveUp();
                        return true;
                  }
               }
         }
      }

      return super.keyPressed(i, j, k);
   }

   public static class PackEntry extends ObjectSelectionList.Entry<TransferableSelectionList.PackEntry> {
      private static final int ICON_OVERLAY_X_MOVE_RIGHT = 0;
      private static final int ICON_OVERLAY_X_MOVE_LEFT = 32;
      private static final int ICON_OVERLAY_X_MOVE_DOWN = 64;
      private static final int ICON_OVERLAY_X_MOVE_UP = 96;
      private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
      private static final int ICON_OVERLAY_Y_SELECTED = 32;
      private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
      private static final int MAX_NAME_WIDTH_PIXELS = 157;
      private static final String TOO_LONG_NAME_SUFFIX = "...";
      private final TransferableSelectionList parent;
      protected final Minecraft minecraft;
      private final PackSelectionModel.Entry pack;
      private final FormattedCharSequence nameDisplayCache;
      private final MultiLineLabel descriptionDisplayCache;
      private final FormattedCharSequence incompatibleNameDisplayCache;
      private final MultiLineLabel incompatibleDescriptionDisplayCache;

      public PackEntry(Minecraft minecraft, TransferableSelectionList transferableselectionlist, PackSelectionModel.Entry packselectionmodel_entry) {
         this.minecraft = minecraft;
         this.pack = packselectionmodel_entry;
         this.parent = transferableselectionlist;
         this.nameDisplayCache = cacheName(minecraft, packselectionmodel_entry.getTitle());
         this.descriptionDisplayCache = cacheDescription(minecraft, packselectionmodel_entry.getExtendedDescription());
         this.incompatibleNameDisplayCache = cacheName(minecraft, TransferableSelectionList.INCOMPATIBLE_TITLE);
         this.incompatibleDescriptionDisplayCache = cacheDescription(minecraft, packselectionmodel_entry.getCompatibility().getDescription());
      }

      private static FormattedCharSequence cacheName(Minecraft minecraft, Component component) {
         int i = minecraft.font.width(component);
         if (i > 157) {
            FormattedText formattedtext = FormattedText.composite(minecraft.font.substrByWidth(component, 157 - minecraft.font.width("...")), FormattedText.of("..."));
            return Language.getInstance().getVisualOrder(formattedtext);
         } else {
            return component.getVisualOrderText();
         }
      }

      private static MultiLineLabel cacheDescription(Minecraft minecraft, Component component) {
         return MultiLineLabel.create(minecraft.font, component, 157, 2);
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.pack.getTitle());
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         PackCompatibility packcompatibility = this.pack.getCompatibility();
         if (!packcompatibility.isCompatible()) {
            guigraphics.fill(k - 1, j - 1, k + l - 9, j + i1 + 1, -8978432);
         }

         guigraphics.blit(this.pack.getIconTexture(), k, j, 0.0F, 0.0F, 32, 32, 32, 32);
         FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
         MultiLineLabel multilinelabel = this.descriptionDisplayCache;
         if (this.showHoverOverlay() && (this.minecraft.options.touchscreen().get() || flag || this.parent.getSelected() == this && this.parent.isFocused())) {
            guigraphics.fill(k, j, k + 32, j + 32, -1601138544);
            int l1 = j1 - k;
            int i2 = k1 - j;
            if (!this.pack.getCompatibility().isCompatible()) {
               formattedcharsequence = this.incompatibleNameDisplayCache;
               multilinelabel = this.incompatibleDescriptionDisplayCache;
            }

            if (this.pack.canSelect()) {
               if (l1 < 32) {
                  guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, 32.0F, 32, 32, 256, 256);
               } else {
                  guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 0.0F, 0.0F, 32, 32, 256, 256);
               }
            } else {
               if (this.pack.canUnselect()) {
                  if (l1 < 16) {
                     guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 32.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 32.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveUp()) {
                  if (l1 < 32 && l1 > 16 && i2 < 16) {
                     guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 96.0F, 0.0F, 32, 32, 256, 256);
                  }
               }

               if (this.pack.canMoveDown()) {
                  if (l1 < 32 && l1 > 16 && i2 > 16) {
                     guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, 32.0F, 32, 32, 256, 256);
                  } else {
                     guigraphics.blit(TransferableSelectionList.ICON_OVERLAY_LOCATION, k, j, 64.0F, 0.0F, 32, 32, 256, 256);
                  }
               }
            }
         }

         guigraphics.drawString(this.minecraft.font, formattedcharsequence, k + 32 + 2, j + 1, 16777215);
         multilinelabel.renderLeftAligned(guigraphics, k + 32 + 2, j + 12, 10, 8421504);
      }

      public String getPackId() {
         return this.pack.getId();
      }

      private boolean showHoverOverlay() {
         return !this.pack.isFixedPosition() || !this.pack.isRequired();
      }

      public void keyboardSelection() {
         if (this.pack.canSelect() && this.handlePackSelection()) {
            this.parent.screen.updateFocus(this.parent);
         } else if (this.pack.canUnselect()) {
            this.pack.unselect();
            this.parent.screen.updateFocus(this.parent);
         }

      }

      void keyboardMoveUp() {
         if (this.pack.canMoveUp()) {
            this.pack.moveUp();
         }

      }

      void keyboardMoveDown() {
         if (this.pack.canMoveDown()) {
            this.pack.moveDown();
         }

      }

      private boolean handlePackSelection() {
         if (this.pack.getCompatibility().isCompatible()) {
            this.pack.select();
            return true;
         } else {
            Component component = this.pack.getCompatibility().getConfirmation();
            this.minecraft.setScreen(new ConfirmScreen((flag) -> {
               this.minecraft.setScreen(this.parent.screen);
               if (flag) {
                  this.pack.select();
               }

            }, TransferableSelectionList.INCOMPATIBLE_CONFIRM_TITLE, component));
            return false;
         }
      }

      public boolean mouseClicked(double d0, double d1, int i) {
         if (i != 0) {
            return false;
         } else {
            double d2 = d0 - (double)this.parent.getRowLeft();
            double d3 = d1 - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && d2 <= 32.0D) {
               this.parent.screen.clearSelected();
               if (this.pack.canSelect()) {
                  this.handlePackSelection();
                  return true;
               }

               if (d2 < 16.0D && this.pack.canUnselect()) {
                  this.pack.unselect();
                  return true;
               }

               if (d2 > 16.0D && d3 < 16.0D && this.pack.canMoveUp()) {
                  this.pack.moveUp();
                  return true;
               }

               if (d2 > 16.0D && d3 > 16.0D && this.pack.canMoveDown()) {
                  this.pack.moveDown();
                  return true;
               }
            }

            return false;
         }
      }
   }
}
