package net.minecraft.client.gui.components.toasts;

import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class AdvancementToast implements Toast {
   public static final int DISPLAY_TIME = 5000;
   private final Advancement advancement;
   private boolean playedSound;

   public AdvancementToast(Advancement advancement) {
      this.advancement = advancement;
   }

   public Toast.Visibility render(GuiGraphics guigraphics, ToastComponent toastcomponent, long i) {
      DisplayInfo displayinfo = this.advancement.getDisplay();
      guigraphics.blit(TEXTURE, 0, 0, 0, 0, this.width(), this.height());
      if (displayinfo != null) {
         List<FormattedCharSequence> list = toastcomponent.getMinecraft().font.split(displayinfo.getTitle(), 125);
         int j = displayinfo.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
         if (list.size() == 1) {
            guigraphics.drawString(toastcomponent.getMinecraft().font, displayinfo.getFrame().getDisplayName(), 30, 7, j | -16777216, false);
            guigraphics.drawString(toastcomponent.getMinecraft().font, list.get(0), 30, 18, -1, false);
         } else {
            int k = 1500;
            float f = 300.0F;
            if (i < 1500L) {
               int l = Mth.floor(Mth.clamp((float)(1500L - i) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
               guigraphics.drawString(toastcomponent.getMinecraft().font, displayinfo.getFrame().getDisplayName(), 30, 11, j | l, false);
            } else {
               int i1 = Mth.floor(Mth.clamp((float)(i - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
               int j1 = this.height() / 2 - list.size() * 9 / 2;

               for(FormattedCharSequence formattedcharsequence : list) {
                  guigraphics.drawString(toastcomponent.getMinecraft().font, formattedcharsequence, 30, j1, 16777215 | i1, false);
                  j1 += 9;
               }
            }
         }

         if (!this.playedSound && i > 0L) {
            this.playedSound = true;
            if (displayinfo.getFrame() == FrameType.CHALLENGE) {
               toastcomponent.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
         }

         guigraphics.renderFakeItem(displayinfo.getIcon(), 8, 8);
         return (double)i >= 5000.0D * toastcomponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
      } else {
         return Toast.Visibility.HIDE;
      }
   }
}
