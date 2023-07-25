package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.sounds.SoundEvents;

public class PageButton extends Button {
   private final boolean isForward;
   private final boolean playTurnSound;

   public PageButton(int i, int j, boolean flag, Button.OnPress button_onpress, boolean flag1) {
      super(i, j, 23, 13, CommonComponents.EMPTY, button_onpress, DEFAULT_NARRATION);
      this.isForward = flag;
      this.playTurnSound = flag1;
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      int k = 0;
      int l = 192;
      if (this.isHoveredOrFocused()) {
         k += 23;
      }

      if (!this.isForward) {
         l += 13;
      }

      guigraphics.blit(BookViewScreen.BOOK_LOCATION, this.getX(), this.getY(), k, l, 23, 13);
   }

   public void playDownSound(SoundManager soundmanager) {
      if (this.playTurnSound) {
         soundmanager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
      }

   }
}
