package net.minecraft.client.gui.spectator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SpectatorMenu {
   private static final SpectatorMenuItem CLOSE_ITEM = new SpectatorMenu.CloseSpectatorItem();
   private static final SpectatorMenuItem SCROLL_LEFT = new SpectatorMenu.ScrollMenuItem(-1, true);
   private static final SpectatorMenuItem SCROLL_RIGHT_ENABLED = new SpectatorMenu.ScrollMenuItem(1, true);
   private static final SpectatorMenuItem SCROLL_RIGHT_DISABLED = new SpectatorMenu.ScrollMenuItem(1, false);
   private static final int MAX_PER_PAGE = 8;
   static final Component CLOSE_MENU_TEXT = Component.translatable("spectatorMenu.close");
   static final Component PREVIOUS_PAGE_TEXT = Component.translatable("spectatorMenu.previous_page");
   static final Component NEXT_PAGE_TEXT = Component.translatable("spectatorMenu.next_page");
   public static final SpectatorMenuItem EMPTY_SLOT = new SpectatorMenuItem() {
      public void selectItem(SpectatorMenu spectatormenu) {
      }

      public Component getName() {
         return CommonComponents.EMPTY;
      }

      public void renderIcon(GuiGraphics guigraphics, float f, int i) {
      }

      public boolean isEnabled() {
         return false;
      }
   };
   private final SpectatorMenuListener listener;
   private SpectatorMenuCategory category;
   private int selectedSlot = -1;
   int page;

   public SpectatorMenu(SpectatorMenuListener spectatormenulistener) {
      this.category = new RootSpectatorMenuCategory();
      this.listener = spectatormenulistener;
   }

   public SpectatorMenuItem getItem(int i) {
      int j = i + this.page * 6;
      if (this.page > 0 && i == 0) {
         return SCROLL_LEFT;
      } else if (i == 7) {
         return j < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED;
      } else if (i == 8) {
         return CLOSE_ITEM;
      } else {
         return j >= 0 && j < this.category.getItems().size() ? MoreObjects.firstNonNull(this.category.getItems().get(j), EMPTY_SLOT) : EMPTY_SLOT;
      }
   }

   public List<SpectatorMenuItem> getItems() {
      List<SpectatorMenuItem> list = Lists.newArrayList();

      for(int i = 0; i <= 8; ++i) {
         list.add(this.getItem(i));
      }

      return list;
   }

   public SpectatorMenuItem getSelectedItem() {
      return this.getItem(this.selectedSlot);
   }

   public SpectatorMenuCategory getSelectedCategory() {
      return this.category;
   }

   public void selectSlot(int i) {
      SpectatorMenuItem spectatormenuitem = this.getItem(i);
      if (spectatormenuitem != EMPTY_SLOT) {
         if (this.selectedSlot == i && spectatormenuitem.isEnabled()) {
            spectatormenuitem.selectItem(this);
         } else {
            this.selectedSlot = i;
         }
      }

   }

   public void exit() {
      this.listener.onSpectatorMenuClosed(this);
   }

   public int getSelectedSlot() {
      return this.selectedSlot;
   }

   public void selectCategory(SpectatorMenuCategory spectatormenucategory) {
      this.category = spectatormenucategory;
      this.selectedSlot = -1;
      this.page = 0;
   }

   public SpectatorPage getCurrentPage() {
      return new SpectatorPage(this.getItems(), this.selectedSlot);
   }

   static class CloseSpectatorItem implements SpectatorMenuItem {
      public void selectItem(SpectatorMenu spectatormenu) {
         spectatormenu.exit();
      }

      public Component getName() {
         return SpectatorMenu.CLOSE_MENU_TEXT;
      }

      public void renderIcon(GuiGraphics guigraphics, float f, int i) {
         guigraphics.blit(SpectatorGui.SPECTATOR_LOCATION, 0, 0, 128.0F, 0.0F, 16, 16, 256, 256);
      }

      public boolean isEnabled() {
         return true;
      }
   }

   static class ScrollMenuItem implements SpectatorMenuItem {
      private final int direction;
      private final boolean enabled;

      public ScrollMenuItem(int i, boolean flag) {
         this.direction = i;
         this.enabled = flag;
      }

      public void selectItem(SpectatorMenu spectatormenu) {
         spectatormenu.page += this.direction;
      }

      public Component getName() {
         return this.direction < 0 ? SpectatorMenu.PREVIOUS_PAGE_TEXT : SpectatorMenu.NEXT_PAGE_TEXT;
      }

      public void renderIcon(GuiGraphics guigraphics, float f, int i) {
         if (this.direction < 0) {
            guigraphics.blit(SpectatorGui.SPECTATOR_LOCATION, 0, 0, 144.0F, 0.0F, 16, 16, 256, 256);
         } else {
            guigraphics.blit(SpectatorGui.SPECTATOR_LOCATION, 0, 0, 160.0F, 0.0F, 16, 16, 256, 256);
         }

      }

      public boolean isEnabled() {
         return this.enabled;
      }
   }
}
