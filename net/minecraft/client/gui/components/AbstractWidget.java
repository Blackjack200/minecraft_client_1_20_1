package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public abstract class AbstractWidget implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
   public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
   public static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
   private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5D;
   private static final double MIN_SCROLL_PERIOD = 3.0D;
   protected int width;
   protected int height;
   private int x;
   private int y;
   private Component message;
   protected boolean isHovered;
   public boolean active = true;
   public boolean visible = true;
   protected float alpha = 1.0F;
   private int tabOrderGroup;
   private boolean focused;
   @Nullable
   private Tooltip tooltip;
   private int tooltipMsDelay;
   private long hoverOrFocusedStartTime;
   private boolean wasHoveredOrFocused;

   public AbstractWidget(int i, int j, int k, int l, Component component) {
      this.x = i;
      this.y = j;
      this.width = k;
      this.height = l;
      this.message = component;
   }

   public int getHeight() {
      return this.height;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.visible) {
         this.isHovered = i >= this.getX() && j >= this.getY() && i < this.getX() + this.width && j < this.getY() + this.height;
         this.renderWidget(guigraphics, i, j, f);
         this.updateTooltip();
      }
   }

   private void updateTooltip() {
      if (this.tooltip != null) {
         boolean flag = this.isHovered || this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard();
         if (flag != this.wasHoveredOrFocused) {
            if (flag) {
               this.hoverOrFocusedStartTime = Util.getMillis();
            }

            this.wasHoveredOrFocused = flag;
         }

         if (flag && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.tooltipMsDelay) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
               screen.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(), this.isFocused());
            }
         }

      }
   }

   protected ClientTooltipPositioner createTooltipPositioner() {
      return (ClientTooltipPositioner)(!this.isHovered && this.isFocused() && Minecraft.getInstance().getLastInputType().isKeyboard() ? new BelowOrAboveWidgetTooltipPositioner(this) : new MenuTooltipPositioner(this));
   }

   public void setTooltip(@Nullable Tooltip tooltip) {
      this.tooltip = tooltip;
   }

   @Nullable
   public Tooltip getTooltip() {
      return this.tooltip;
   }

   public void setTooltipDelay(int i) {
      this.tooltipMsDelay = i;
   }

   protected MutableComponent createNarrationMessage() {
      return wrapDefaultNarrationMessage(this.getMessage());
   }

   public static MutableComponent wrapDefaultNarrationMessage(Component component) {
      return Component.translatable("gui.narrate.button", component);
   }

   protected abstract void renderWidget(GuiGraphics guigraphics, int i, int j, float f);

   protected static void renderScrollingString(GuiGraphics guigraphics, Font font, Component component, int i, int j, int k, int l, int i1) {
      int j1 = font.width(component);
      int k1 = (j + l - 9) / 2 + 1;
      int l1 = k - i;
      if (j1 > l1) {
         int i2 = j1 - l1;
         double d0 = (double)Util.getMillis() / 1000.0D;
         double d1 = Math.max((double)i2 * 0.5D, 3.0D);
         double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / 2.0D + 0.5D;
         double d3 = Mth.lerp(d2, 0.0D, (double)i2);
         guigraphics.enableScissor(i, j, k, l);
         guigraphics.drawString(font, component, i - (int)d3, k1, i1);
         guigraphics.disableScissor();
      } else {
         guigraphics.drawCenteredString(font, component, (i + k) / 2, k1, i1);
      }

   }

   protected void renderScrollingString(GuiGraphics guigraphics, Font font, int i, int j) {
      int k = this.getX() + i;
      int l = this.getX() + this.getWidth() - i;
      renderScrollingString(guigraphics, font, this.getMessage(), k, this.getY(), l, this.getY() + this.getHeight(), j);
   }

   public void renderTexture(GuiGraphics guigraphics, ResourceLocation resourcelocation, int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
      int j2 = l;
      if (!this.isActive()) {
         j2 = l + i1 * 2;
      } else if (this.isHoveredOrFocused()) {
         j2 = l + i1;
      }

      RenderSystem.enableDepthTest();
      guigraphics.blit(resourcelocation, i, j, (float)k, (float)j2, j1, k1, l1, i2);
   }

   public void onClick(double d0, double d1) {
   }

   public void onRelease(double d0, double d1) {
   }

   protected void onDrag(double d0, double d1, double d2, double d3) {
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.active && this.visible) {
         if (this.isValidClickButton(i)) {
            boolean flag = this.clicked(d0, d1);
            if (flag) {
               this.playDownSound(Minecraft.getInstance().getSoundManager());
               this.onClick(d0, d1);
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean mouseReleased(double d0, double d1, int i) {
      if (this.isValidClickButton(i)) {
         this.onRelease(d0, d1);
         return true;
      } else {
         return false;
      }
   }

   protected boolean isValidClickButton(int i) {
      return i == 0;
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (this.isValidClickButton(i)) {
         this.onDrag(d0, d1, d2, d3);
         return true;
      } else {
         return false;
      }
   }

   protected boolean clicked(double d0, double d1) {
      return this.active && this.visible && d0 >= (double)this.getX() && d1 >= (double)this.getY() && d0 < (double)(this.getX() + this.width) && d1 < (double)(this.getY() + this.height);
   }

   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      if (this.active && this.visible) {
         return !this.isFocused() ? ComponentPath.leaf(this) : null;
      } else {
         return null;
      }
   }

   public boolean isMouseOver(double d0, double d1) {
      return this.active && this.visible && d0 >= (double)this.getX() && d1 >= (double)this.getY() && d0 < (double)(this.getX() + this.width) && d1 < (double)(this.getY() + this.height);
   }

   public void playDownSound(SoundManager soundmanager) {
      soundmanager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
   }

   public int getWidth() {
      return this.width;
   }

   public void setWidth(int i) {
      this.width = i;
   }

   public void setAlpha(float f) {
      this.alpha = f;
   }

   public void setMessage(Component component) {
      this.message = component;
   }

   public Component getMessage() {
      return this.message;
   }

   public boolean isFocused() {
      return this.focused;
   }

   public boolean isHovered() {
      return this.isHovered;
   }

   public boolean isHoveredOrFocused() {
      return this.isHovered() || this.isFocused();
   }

   public boolean isActive() {
      return this.visible && this.active;
   }

   public void setFocused(boolean flag) {
      this.focused = flag;
   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      if (this.isFocused()) {
         return NarratableEntry.NarrationPriority.FOCUSED;
      } else {
         return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
      }
   }

   public final void updateNarration(NarrationElementOutput narrationelementoutput) {
      this.updateWidgetNarration(narrationelementoutput);
      if (this.tooltip != null) {
         this.tooltip.updateNarration(narrationelementoutput);
      }

   }

   protected abstract void updateWidgetNarration(NarrationElementOutput narrationelementoutput);

   protected void defaultButtonNarrationText(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.focused"));
         } else {
            narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
         }
      }

   }

   public int getX() {
      return this.x;
   }

   public void setX(int i) {
      this.x = i;
   }

   public int getY() {
      return this.y;
   }

   public void setY(int i) {
      this.y = i;
   }

   public void visitWidgets(Consumer<AbstractWidget> consumer) {
      consumer.accept(this);
   }

   public ScreenRectangle getRectangle() {
      return LayoutElement.super.getRectangle();
   }

   public int getTabOrderGroup() {
      return this.tabOrderGroup;
   }

   public void setTabOrderGroup(int i) {
      this.tabOrderGroup = i;
   }
}
