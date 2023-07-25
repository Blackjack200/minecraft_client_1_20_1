package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class TabNavigationBar extends AbstractContainerEventHandler implements Renderable, GuiEventListener, NarratableEntry {
   private static final int NO_TAB = -1;
   private static final int MAX_WIDTH = 400;
   private static final int HEIGHT = 24;
   private static final int MARGIN = 14;
   private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
   private final GridLayout layout;
   private int width;
   private final TabManager tabManager;
   private final ImmutableList<Tab> tabs;
   private final ImmutableList<TabButton> tabButtons;

   TabNavigationBar(int i, TabManager tabmanager, Iterable<Tab> iterable) {
      this.width = i;
      this.tabManager = tabmanager;
      this.tabs = ImmutableList.copyOf(iterable);
      this.layout = new GridLayout(0, 0);
      this.layout.defaultCellSetting().alignHorizontallyCenter();
      ImmutableList.Builder<TabButton> immutablelist_builder = ImmutableList.builder();
      int j = 0;

      for(Tab tab : iterable) {
         immutablelist_builder.add(this.layout.addChild(new TabButton(tabmanager, tab, 0, 24), 0, j++));
      }

      this.tabButtons = immutablelist_builder.build();
   }

   public static TabNavigationBar.Builder builder(TabManager tabmanager, int i) {
      return new TabNavigationBar.Builder(tabmanager, i);
   }

   public void setWidth(int i) {
      this.width = i;
   }

   public void setFocused(boolean flag) {
      super.setFocused(flag);
      if (this.getFocused() != null) {
         this.getFocused().setFocused(flag);
      }

   }

   public void setFocused(@Nullable GuiEventListener guieventlistener) {
      super.setFocused(guieventlistener);
      if (guieventlistener instanceof TabButton tabbutton) {
         this.tabManager.setCurrentTab(tabbutton.tab(), true);
      }

   }

   public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      if (!this.isFocused()) {
         TabButton tabbutton = this.currentTabButton();
         if (tabbutton != null) {
            return ComponentPath.path(this, ComponentPath.leaf(tabbutton));
         }
      }

      return focusnavigationevent instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(focusnavigationevent);
   }

   public List<? extends GuiEventListener> children() {
      return this.tabButtons;
   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      return this.tabButtons.stream().map(AbstractWidget::narrationPriority).max(Comparator.naturalOrder()).orElse(NarratableEntry.NarrationPriority.NONE);
   }

   public void updateNarration(NarrationElementOutput narrationelementoutput) {
      Optional<TabButton> optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(this.currentTabButton()));
      optional.ifPresent((tabbutton) -> {
         this.narrateListElementPosition(narrationelementoutput.nest(), tabbutton);
         tabbutton.updateNarration(narrationelementoutput);
      });
      if (this.isFocused()) {
         narrationelementoutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
      }

   }

   protected void narrateListElementPosition(NarrationElementOutput narrationelementoutput, TabButton tabbutton) {
      if (this.tabs.size() > 1) {
         int i = this.tabButtons.indexOf(tabbutton);
         if (i != -1) {
            narrationelementoutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.tab", i + 1, this.tabs.size()));
         }
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.fill(0, 0, this.width, 24, -16777216);
      guigraphics.blit(CreateWorldScreen.HEADER_SEPERATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);

      for(TabButton tabbutton : this.tabButtons) {
         tabbutton.render(guigraphics, i, j, f);
      }

   }

   public ScreenRectangle getRectangle() {
      return this.layout.getRectangle();
   }

   public void arrangeElements() {
      int i = Math.min(400, this.width) - 28;
      int j = Mth.roundToward(i / this.tabs.size(), 2);

      for(TabButton tabbutton : this.tabButtons) {
         tabbutton.setWidth(j);
      }

      this.layout.arrangeElements();
      this.layout.setX(Mth.roundToward((this.width - i) / 2, 2));
      this.layout.setY(0);
   }

   public void selectTab(int i, boolean flag) {
      if (this.isFocused()) {
         this.setFocused(this.tabButtons.get(i));
      } else {
         this.tabManager.setCurrentTab(this.tabs.get(i), flag);
      }

   }

   public boolean keyPressed(int i) {
      if (Screen.hasControlDown()) {
         int j = this.getNextTabIndex(i);
         if (j != -1) {
            this.selectTab(Mth.clamp(j, 0, this.tabs.size() - 1), true);
            return true;
         }
      }

      return false;
   }

   private int getNextTabIndex(int i) {
      if (i >= 49 && i <= 57) {
         return i - 49;
      } else {
         if (i == 258) {
            int j = this.currentTabIndex();
            if (j != -1) {
               int k = Screen.hasShiftDown() ? j - 1 : j + 1;
               return Math.floorMod(k, this.tabs.size());
            }
         }

         return -1;
      }
   }

   private int currentTabIndex() {
      Tab tab = this.tabManager.getCurrentTab();
      int i = this.tabs.indexOf(tab);
      return i != -1 ? i : -1;
   }

   private @Nullable TabButton currentTabButton() {
      int i = this.currentTabIndex();
      return i != -1 ? this.tabButtons.get(i) : null;
   }

   public static class Builder {
      private final int width;
      private final TabManager tabManager;
      private final List<Tab> tabs = new ArrayList<>();

      Builder(TabManager tabmanager, int i) {
         this.tabManager = tabmanager;
         this.width = i;
      }

      public TabNavigationBar.Builder addTabs(Tab... atab) {
         Collections.addAll(this.tabs, atab);
         return this;
      }

      public TabNavigationBar build() {
         return new TabNavigationBar(this.width, this.tabManager, this.tabs);
      }
   }
}
