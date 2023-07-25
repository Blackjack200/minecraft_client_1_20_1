package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractContainerEventHandler implements Renderable, NarratableEntry {
   protected final Minecraft minecraft;
   protected final int itemHeight;
   private final List<E> children = new AbstractSelectionList.TrackedList();
   protected int width;
   protected int height;
   protected int y0;
   protected int y1;
   protected int x1;
   protected int x0;
   protected boolean centerListVertically = true;
   private double scrollAmount;
   private boolean renderSelection = true;
   private boolean renderHeader;
   protected int headerHeight;
   private boolean scrolling;
   @Nullable
   private E selected;
   private boolean renderBackground = true;
   private boolean renderTopAndBottom = true;
   @Nullable
   private E hovered;

   public AbstractSelectionList(Minecraft minecraft, int i, int j, int k, int l, int i1) {
      this.minecraft = minecraft;
      this.width = i;
      this.height = j;
      this.y0 = k;
      this.y1 = l;
      this.itemHeight = i1;
      this.x0 = 0;
      this.x1 = i;
   }

   public void setRenderSelection(boolean flag) {
      this.renderSelection = flag;
   }

   protected void setRenderHeader(boolean flag, int i) {
      this.renderHeader = flag;
      this.headerHeight = i;
      if (!flag) {
         this.headerHeight = 0;
      }

   }

   public int getRowWidth() {
      return 220;
   }

   @Nullable
   public E getSelected() {
      return this.selected;
   }

   public void setSelected(@Nullable E abstractselectionlist_entry) {
      this.selected = abstractselectionlist_entry;
   }

   public E getFirstElement() {
      return this.children.get(0);
   }

   public void setRenderBackground(boolean flag) {
      this.renderBackground = flag;
   }

   public void setRenderTopAndBottom(boolean flag) {
      this.renderTopAndBottom = flag;
   }

   @Nullable
   public E getFocused() {
      return (E)(super.getFocused());
   }

   public final List<E> children() {
      return this.children;
   }

   protected void clearEntries() {
      this.children.clear();
      this.selected = null;
   }

   protected void replaceEntries(Collection<E> collection) {
      this.clearEntries();
      this.children.addAll(collection);
   }

   protected E getEntry(int i) {
      return this.children().get(i);
   }

   protected int addEntry(E abstractselectionlist_entry) {
      this.children.add(abstractselectionlist_entry);
      return this.children.size() - 1;
   }

   protected void addEntryToTop(E abstractselectionlist_entry) {
      double d0 = (double)this.getMaxScroll() - this.getScrollAmount();
      this.children.add(0, abstractselectionlist_entry);
      this.setScrollAmount((double)this.getMaxScroll() - d0);
   }

   protected boolean removeEntryFromTop(E abstractselectionlist_entry) {
      double d0 = (double)this.getMaxScroll() - this.getScrollAmount();
      boolean flag = this.removeEntry(abstractselectionlist_entry);
      this.setScrollAmount((double)this.getMaxScroll() - d0);
      return flag;
   }

   protected int getItemCount() {
      return this.children().size();
   }

   protected boolean isSelectedItem(int i) {
      return Objects.equals(this.getSelected(), this.children().get(i));
   }

   @Nullable
   protected final E getEntryAtPosition(double d0, double d1) {
      int i = this.getRowWidth() / 2;
      int j = this.x0 + this.width / 2;
      int k = j - i;
      int l = j + i;
      int i1 = Mth.floor(d1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
      int j1 = i1 / this.itemHeight;
      return (E)(d0 < (double)this.getScrollbarPosition() && d0 >= (double)k && d0 <= (double)l && j1 >= 0 && i1 >= 0 && j1 < this.getItemCount() ? this.children().get(j1) : null);
   }

   public void updateSize(int i, int j, int k, int l) {
      this.width = i;
      this.height = j;
      this.y0 = k;
      this.y1 = l;
      this.x0 = 0;
      this.x1 = i;
   }

   public void setLeftPos(int i) {
      this.x0 = i;
      this.x1 = i + this.width;
   }

   protected int getMaxPosition() {
      return this.getItemCount() * this.itemHeight + this.headerHeight;
   }

   protected void clickedHeader(int i, int j) {
   }

   protected void renderHeader(GuiGraphics guigraphics, int i, int j) {
   }

   protected void renderBackground(GuiGraphics guigraphics) {
   }

   protected void renderDecorations(GuiGraphics guigraphics, int i, int j) {
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      int k = this.getScrollbarPosition();
      int l = k + 6;
      this.hovered = this.isMouseOver((double)i, (double)j) ? this.getEntryAtPosition((double)i, (double)j) : null;
      if (this.renderBackground) {
         guigraphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
         int i1 = 32;
         guigraphics.blit(Screen.BACKGROUND_LOCATION, this.x0, this.y0, (float)this.x1, (float)(this.y1 + (int)this.getScrollAmount()), this.x1 - this.x0, this.y1 - this.y0, 32, 32);
         guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      int j1 = this.getRowLeft();
      int k1 = this.y0 + 4 - (int)this.getScrollAmount();
      this.enableScissor(guigraphics);
      if (this.renderHeader) {
         this.renderHeader(guigraphics, j1, k1);
      }

      this.renderList(guigraphics, i, j, f);
      guigraphics.disableScissor();
      if (this.renderTopAndBottom) {
         int l1 = 32;
         guigraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
         guigraphics.blit(Screen.BACKGROUND_LOCATION, this.x0, 0, 0.0F, 0.0F, this.width, this.y0, 32, 32);
         guigraphics.blit(Screen.BACKGROUND_LOCATION, this.x0, this.y1, 0.0F, (float)this.y1, this.width, this.height - this.y1, 32, 32);
         guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
         int i2 = 4;
         guigraphics.fillGradient(RenderType.guiOverlay(), this.x0, this.y0, this.x1, this.y0 + 4, -16777216, 0, 0);
         guigraphics.fillGradient(RenderType.guiOverlay(), this.x0, this.y1 - 4, this.x1, this.y1, 0, -16777216, 0);
      }

      int j2 = this.getMaxScroll();
      if (j2 > 0) {
         int k2 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
         k2 = Mth.clamp(k2, 32, this.y1 - this.y0 - 8);
         int l2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k2) / j2 + this.y0;
         if (l2 < this.y0) {
            l2 = this.y0;
         }

         guigraphics.fill(k, this.y0, l, this.y1, -16777216);
         guigraphics.fill(k, l2, l, l2 + k2, -8355712);
         guigraphics.fill(k, l2, l - 1, l2 + k2 - 1, -4144960);
      }

      this.renderDecorations(guigraphics, i, j);
      RenderSystem.disableBlend();
   }

   protected void enableScissor(GuiGraphics guigraphics) {
      guigraphics.enableScissor(this.x0, this.y0, this.x1, this.y1);
   }

   protected void centerScrollOn(E abstractselectionlist_entry) {
      this.setScrollAmount((double)(this.children().indexOf(abstractselectionlist_entry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2));
   }

   protected void ensureVisible(E abstractselectionlist_entry) {
      int i = this.getRowTop(this.children().indexOf(abstractselectionlist_entry));
      int j = i - this.y0 - 4 - this.itemHeight;
      if (j < 0) {
         this.scroll(j);
      }

      int k = this.y1 - i - this.itemHeight - this.itemHeight;
      if (k < 0) {
         this.scroll(-k);
      }

   }

   private void scroll(int i) {
      this.setScrollAmount(this.getScrollAmount() + (double)i);
   }

   public double getScrollAmount() {
      return this.scrollAmount;
   }

   public void setScrollAmount(double d0) {
      this.scrollAmount = Mth.clamp(d0, 0.0D, (double)this.getMaxScroll());
   }

   public int getMaxScroll() {
      return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
   }

   public int getScrollBottom() {
      return (int)this.getScrollAmount() - this.height - this.headerHeight;
   }

   protected void updateScrollingState(double d0, double d1, int i) {
      this.scrolling = i == 0 && d0 >= (double)this.getScrollbarPosition() && d0 < (double)(this.getScrollbarPosition() + 6);
   }

   protected int getScrollbarPosition() {
      return this.width / 2 + 124;
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      this.updateScrollingState(d0, d1, i);
      if (!this.isMouseOver(d0, d1)) {
         return false;
      } else {
         E abstractselectionlist_entry = this.getEntryAtPosition(d0, d1);
         if (abstractselectionlist_entry != null) {
            if (abstractselectionlist_entry.mouseClicked(d0, d1, i)) {
               E abstractselectionlist_entry1 = this.getFocused();
               if (abstractselectionlist_entry1 != abstractselectionlist_entry && abstractselectionlist_entry1 instanceof ContainerEventHandler) {
                  ContainerEventHandler containereventhandler = (ContainerEventHandler)abstractselectionlist_entry1;
                  containereventhandler.setFocused((GuiEventListener)null);
               }

               this.setFocused(abstractselectionlist_entry);
               this.setDragging(true);
               return true;
            }
         } else if (i == 0) {
            this.clickedHeader((int)(d0 - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(d1 - (double)this.y0) + (int)this.getScrollAmount() - 4);
            return true;
         }

         return this.scrolling;
      }
   }

   public boolean mouseReleased(double d0, double d1, int i) {
      if (this.getFocused() != null) {
         this.getFocused().mouseReleased(d0, d1, i);
      }

      return false;
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (super.mouseDragged(d0, d1, i, d2, d3)) {
         return true;
      } else if (i == 0 && this.scrolling) {
         if (d1 < (double)this.y0) {
            this.setScrollAmount(0.0D);
         } else if (d1 > (double)this.y1) {
            this.setScrollAmount((double)this.getMaxScroll());
         } else {
            double d4 = (double)Math.max(1, this.getMaxScroll());
            int j = this.y1 - this.y0;
            int k = Mth.clamp((int)((float)(j * j) / (float)this.getMaxPosition()), 32, j - 8);
            double d5 = Math.max(1.0D, d4 / (double)(j - k));
            this.setScrollAmount(this.getScrollAmount() + d3 * d5);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      this.setScrollAmount(this.getScrollAmount() - d2 * (double)this.itemHeight / 2.0D);
      return true;
   }

   public void setFocused(@Nullable GuiEventListener guieventlistener) {
      super.setFocused(guieventlistener);
      int i = this.children.indexOf(guieventlistener);
      if (i >= 0) {
         E abstractselectionlist_entry = this.children.get(i);
         this.setSelected(abstractselectionlist_entry);
         if (this.minecraft.getLastInputType().isKeyboard()) {
            this.ensureVisible(abstractselectionlist_entry);
         }
      }

   }

   @Nullable
   protected E nextEntry(ScreenDirection screendirection) {
      return this.nextEntry(screendirection, (abstractselectionlist_entry) -> true);
   }

   @Nullable
   protected E nextEntry(ScreenDirection screendirection, Predicate<E> predicate) {
      return this.nextEntry(screendirection, predicate, this.getSelected());
   }

   @Nullable
   protected E nextEntry(ScreenDirection screendirection, Predicate<E> predicate, @Nullable E abstractselectionlist_entry) {
      byte var10000;
      switch (screendirection) {
         case RIGHT:
         case LEFT:
            var10000 = 0;
            break;
         case UP:
            var10000 = -1;
            break;
         case DOWN:
            var10000 = 1;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      int i = var10000;
      if (!this.children().isEmpty() && i != 0) {
         int j;
         if (abstractselectionlist_entry == null) {
            j = i > 0 ? 0 : this.children().size() - 1;
         } else {
            j = this.children().indexOf(abstractselectionlist_entry) + i;
         }

         for(int l = j; l >= 0 && l < this.children.size(); l += i) {
            E abstractselectionlist_entry1 = this.children().get(l);
            if (predicate.test(abstractselectionlist_entry1)) {
               return abstractselectionlist_entry1;
            }
         }
      }

      return (E)null;
   }

   public boolean isMouseOver(double d0, double d1) {
      return d1 >= (double)this.y0 && d1 <= (double)this.y1 && d0 >= (double)this.x0 && d0 <= (double)this.x1;
   }

   protected void renderList(GuiGraphics guigraphics, int i, int j, float f) {
      int k = this.getRowLeft();
      int l = this.getRowWidth();
      int i1 = this.itemHeight - 4;
      int j1 = this.getItemCount();

      for(int k1 = 0; k1 < j1; ++k1) {
         int l1 = this.getRowTop(k1);
         int i2 = this.getRowBottom(k1);
         if (i2 >= this.y0 && l1 <= this.y1) {
            this.renderItem(guigraphics, i, j, f, k1, k, l1, l, i1);
         }
      }

   }

   protected void renderItem(GuiGraphics guigraphics, int i, int j, float f, int k, int l, int i1, int j1, int k1) {
      E abstractselectionlist_entry = this.getEntry(k);
      abstractselectionlist_entry.renderBack(guigraphics, k, i1, l, j1, k1, i, j, Objects.equals(this.hovered, abstractselectionlist_entry), f);
      if (this.renderSelection && this.isSelectedItem(k)) {
         int l1 = this.isFocused() ? -1 : -8355712;
         this.renderSelection(guigraphics, i1, j1, k1, l1, -16777216);
      }

      abstractselectionlist_entry.render(guigraphics, k, i1, l, j1, k1, i, j, Objects.equals(this.hovered, abstractselectionlist_entry), f);
   }

   protected void renderSelection(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      int j1 = this.x0 + (this.width - j) / 2;
      int k1 = this.x0 + (this.width + j) / 2;
      guigraphics.fill(j1, i - 2, k1, i + k + 2, l);
      guigraphics.fill(j1 + 1, i - 1, k1 - 1, i + k + 1, i1);
   }

   public int getRowLeft() {
      return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
   }

   public int getRowRight() {
      return this.getRowLeft() + this.getRowWidth();
   }

   protected int getRowTop(int i) {
      return this.y0 + 4 - (int)this.getScrollAmount() + i * this.itemHeight + this.headerHeight;
   }

   protected int getRowBottom(int i) {
      return this.getRowTop(i) + this.itemHeight;
   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      if (this.isFocused()) {
         return NarratableEntry.NarrationPriority.FOCUSED;
      } else {
         return this.hovered != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
      }
   }

   @Nullable
   protected E remove(int i) {
      E abstractselectionlist_entry = this.children.get(i);
      return (E)(this.removeEntry(this.children.get(i)) ? abstractselectionlist_entry : null);
   }

   protected boolean removeEntry(E abstractselectionlist_entry) {
      boolean flag = this.children.remove(abstractselectionlist_entry);
      if (flag && abstractselectionlist_entry == this.getSelected()) {
         this.setSelected((E)null);
      }

      return flag;
   }

   @Nullable
   protected E getHovered() {
      return this.hovered;
   }

   void bindEntryToSelf(AbstractSelectionList.Entry<E> abstractselectionlist_entry) {
      abstractselectionlist_entry.list = this;
   }

   protected void narrateListElementPosition(NarrationElementOutput narrationelementoutput, E abstractselectionlist_entry) {
      List<E> list = this.children();
      if (list.size() > 1) {
         int i = list.indexOf(abstractselectionlist_entry);
         if (i != -1) {
            narrationelementoutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.list", i + 1, list.size()));
         }
      }

   }

   public ScreenRectangle getRectangle() {
      return new ScreenRectangle(this.x0, this.y0, this.x1 - this.x0, this.y1 - this.y0);
   }

   protected abstract static class Entry<E extends AbstractSelectionList.Entry<E>> implements GuiEventListener {
      /** @deprecated */
      @Deprecated
      AbstractSelectionList<E> list;

      public void setFocused(boolean flag) {
      }

      public boolean isFocused() {
         return this.list.getFocused() == this;
      }

      public abstract void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f);

      public void renderBack(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
      }

      public boolean isMouseOver(double d0, double d1) {
         return Objects.equals(this.list.getEntryAtPosition(d0, d1), this);
      }
   }

   class TrackedList extends AbstractList<E> {
      private final List<E> delegate = Lists.newArrayList();

      public E get(int i) {
         return this.delegate.get(i);
      }

      public int size() {
         return this.delegate.size();
      }

      public E set(int i, E abstractselectionlist_entry) {
         E abstractselectionlist_entry1 = this.delegate.set(i, abstractselectionlist_entry);
         AbstractSelectionList.this.bindEntryToSelf(abstractselectionlist_entry);
         return abstractselectionlist_entry1;
      }

      public void add(int i, E abstractselectionlist_entry) {
         this.delegate.add(i, abstractselectionlist_entry);
         AbstractSelectionList.this.bindEntryToSelf(abstractselectionlist_entry);
      }

      public E remove(int i) {
         return this.delegate.remove(i);
      }
   }
}
