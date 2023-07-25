package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class LockIconButton extends Button {
   private boolean locked;

   public LockIconButton(int i, int j, Button.OnPress button_onpress) {
      super(i, j, 20, 20, Component.translatable("narrator.button.difficulty_lock"), button_onpress, DEFAULT_NARRATION);
   }

   protected MutableComponent createNarrationMessage() {
      return CommonComponents.joinForNarration(super.createNarrationMessage(), this.isLocked() ? Component.translatable("narrator.button.difficulty_lock.locked") : Component.translatable("narrator.button.difficulty_lock.unlocked"));
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean flag) {
      this.locked = flag;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      LockIconButton.Icon lockiconbutton_icon;
      if (!this.active) {
         lockiconbutton_icon = this.locked ? LockIconButton.Icon.LOCKED_DISABLED : LockIconButton.Icon.UNLOCKED_DISABLED;
      } else if (this.isHoveredOrFocused()) {
         lockiconbutton_icon = this.locked ? LockIconButton.Icon.LOCKED_HOVER : LockIconButton.Icon.UNLOCKED_HOVER;
      } else {
         lockiconbutton_icon = this.locked ? LockIconButton.Icon.LOCKED : LockIconButton.Icon.UNLOCKED;
      }

      guigraphics.blit(Button.WIDGETS_LOCATION, this.getX(), this.getY(), lockiconbutton_icon.getX(), lockiconbutton_icon.getY(), this.width, this.height);
   }

   static enum Icon {
      LOCKED(0, 146),
      LOCKED_HOVER(0, 166),
      LOCKED_DISABLED(0, 186),
      UNLOCKED(20, 146),
      UNLOCKED_HOVER(20, 166),
      UNLOCKED_DISABLED(20, 186);

      private final int x;
      private final int y;

      private Icon(int i, int j) {
         this.x = i;
         this.y = j;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }
   }
}
