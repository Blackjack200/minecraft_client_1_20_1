package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class AbstractSliderButton extends AbstractWidget {
   private static final ResourceLocation SLIDER_LOCATION = new ResourceLocation("textures/gui/slider.png");
   protected static final int TEXTURE_WIDTH = 200;
   protected static final int TEXTURE_HEIGHT = 20;
   protected static final int TEXTURE_BORDER_X = 20;
   protected static final int TEXTURE_BORDER_Y = 4;
   protected static final int TEXT_MARGIN = 2;
   private static final int HEIGHT = 20;
   private static final int HANDLE_HALF_WIDTH = 4;
   private static final int HANDLE_WIDTH = 8;
   private static final int BACKGROUND = 0;
   private static final int BACKGROUND_FOCUSED = 1;
   private static final int HANDLE = 2;
   private static final int HANDLE_FOCUSED = 3;
   protected double value;
   private boolean canChangeValue;

   public AbstractSliderButton(int i, int j, int k, int l, Component component, double d0) {
      super(i, j, k, l, component);
      this.value = d0;
   }

   private int getTextureY() {
      int i = this.isFocused() && !this.canChangeValue ? 1 : 0;
      return i * 20;
   }

   private int getHandleTextureY() {
      int i = !this.isHovered && !this.canChangeValue ? 2 : 3;
      return i * 20;
   }

   protected MutableComponent createNarrationMessage() {
      return Component.translatable("gui.narrate.slider", this.getMessage());
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.slider.usage.focused"));
         } else {
            narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.slider.usage.hovered"));
         }
      }

   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      Minecraft minecraft = Minecraft.getInstance();
      guigraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      guigraphics.blitNineSliced(SLIDER_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
      guigraphics.blitNineSliced(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, 20, 20, 4, 200, 20, 0, this.getHandleTextureY());
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      int k = this.active ? 16777215 : 10526880;
      this.renderScrollingString(guigraphics, minecraft.font, 2, k | Mth.ceil(this.alpha * 255.0F) << 24);
   }

   public void onClick(double d0, double d1) {
      this.setValueFromMouse(d0);
   }

   public void setFocused(boolean flag) {
      super.setFocused(flag);
      if (!flag) {
         this.canChangeValue = false;
      } else {
         InputType inputtype = Minecraft.getInstance().getLastInputType();
         if (inputtype == InputType.MOUSE || inputtype == InputType.KEYBOARD_TAB) {
            this.canChangeValue = true;
         }

      }
   }

   public boolean keyPressed(int i, int j, int k) {
      if (CommonInputs.selected(i)) {
         this.canChangeValue = !this.canChangeValue;
         return true;
      } else {
         if (this.canChangeValue) {
            boolean flag = i == 263;
            if (flag || i == 262) {
               float f = flag ? -1.0F : 1.0F;
               this.setValue(this.value + (double)(f / (float)(this.width - 8)));
               return true;
            }
         }

         return false;
      }
   }

   private void setValueFromMouse(double d0) {
      this.setValue((d0 - (double)(this.getX() + 4)) / (double)(this.width - 8));
   }

   private void setValue(double d0) {
      double d1 = this.value;
      this.value = Mth.clamp(d0, 0.0D, 1.0D);
      if (d1 != this.value) {
         this.applyValue();
      }

      this.updateMessage();
   }

   protected void onDrag(double d0, double d1, double d2, double d3) {
      this.setValueFromMouse(d0);
      super.onDrag(d0, d1, d2, d3);
   }

   public void playDownSound(SoundManager soundmanager) {
   }

   public void onRelease(double d0, double d1) {
      super.playDownSound(Minecraft.getInstance().getSoundManager());
   }

   protected abstract void updateMessage();

   protected abstract void applyValue();
}
