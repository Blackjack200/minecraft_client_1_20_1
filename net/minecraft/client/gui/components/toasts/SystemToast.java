package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class SystemToast implements Toast {
   private static final int MAX_LINE_SIZE = 200;
   private static final int LINE_SPACING = 12;
   private static final int MARGIN = 10;
   private final SystemToast.SystemToastIds id;
   private Component title;
   private List<FormattedCharSequence> messageLines;
   private long lastChanged;
   private boolean changed;
   private final int width;

   public SystemToast(SystemToast.SystemToastIds systemtoast_systemtoastids, Component component, @Nullable Component component1) {
      this(systemtoast_systemtoastids, component, nullToEmpty(component1), Math.max(160, 30 + Math.max(Minecraft.getInstance().font.width(component), component1 == null ? 0 : Minecraft.getInstance().font.width(component1))));
   }

   public static SystemToast multiline(Minecraft minecraft, SystemToast.SystemToastIds systemtoast_systemtoastids, Component component, Component component1) {
      Font font = minecraft.font;
      List<FormattedCharSequence> list = font.split(component1, 200);
      int i = Math.max(200, list.stream().mapToInt(font::width).max().orElse(200));
      return new SystemToast(systemtoast_systemtoastids, component, list, i + 30);
   }

   private SystemToast(SystemToast.SystemToastIds systemtoast_systemtoastids, Component component, List<FormattedCharSequence> list, int i) {
      this.id = systemtoast_systemtoastids;
      this.title = component;
      this.messageLines = list;
      this.width = i;
   }

   private static ImmutableList<FormattedCharSequence> nullToEmpty(@Nullable Component component) {
      return component == null ? ImmutableList.of() : ImmutableList.of(component.getVisualOrderText());
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return 20 + Math.max(this.messageLines.size(), 1) * 12;
   }

   public Toast.Visibility render(GuiGraphics guigraphics, ToastComponent toastcomponent, long i) {
      if (this.changed) {
         this.lastChanged = i;
         this.changed = false;
      }

      int j = this.width();
      if (j == 160 && this.messageLines.size() <= 1) {
         guigraphics.blit(TEXTURE, 0, 0, 0, 64, j, this.height());
      } else {
         int k = this.height();
         int l = 28;
         int i1 = Math.min(4, k - 28);
         this.renderBackgroundRow(guigraphics, toastcomponent, j, 0, 0, 28);

         for(int j1 = 28; j1 < k - i1; j1 += 10) {
            this.renderBackgroundRow(guigraphics, toastcomponent, j, 16, j1, Math.min(16, k - j1 - i1));
         }

         this.renderBackgroundRow(guigraphics, toastcomponent, j, 32 - i1, k - i1, i1);
      }

      if (this.messageLines == null) {
         guigraphics.drawString(toastcomponent.getMinecraft().font, this.title, 18, 12, -256, false);
      } else {
         guigraphics.drawString(toastcomponent.getMinecraft().font, this.title, 18, 7, -256, false);

         for(int k1 = 0; k1 < this.messageLines.size(); ++k1) {
            guigraphics.drawString(toastcomponent.getMinecraft().font, this.messageLines.get(k1), 18, 18 + k1 * 12, -1, false);
         }
      }

      return (double)(i - this.lastChanged) < (double)this.id.displayTime * toastcomponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
   }

   private void renderBackgroundRow(GuiGraphics guigraphics, ToastComponent toastcomponent, int i, int j, int k, int l) {
      int i1 = j == 0 ? 20 : 5;
      int j1 = Math.min(60, i - i1);
      guigraphics.blit(TEXTURE, 0, k, 0, 64 + j, i1, l);

      for(int k1 = i1; k1 < i - j1; k1 += 64) {
         guigraphics.blit(TEXTURE, k1, k, 32, 64 + j, Math.min(64, i - k1 - j1), l);
      }

      guigraphics.blit(TEXTURE, i - j1, k, 160 - j1, 64 + j, j1, l);
   }

   public void reset(Component component, @Nullable Component component1) {
      this.title = component;
      this.messageLines = nullToEmpty(component1);
      this.changed = true;
   }

   public SystemToast.SystemToastIds getToken() {
      return this.id;
   }

   public static void add(ToastComponent toastcomponent, SystemToast.SystemToastIds systemtoast_systemtoastids, Component component, @Nullable Component component1) {
      toastcomponent.addToast(new SystemToast(systemtoast_systemtoastids, component, component1));
   }

   public static void addOrUpdate(ToastComponent toastcomponent, SystemToast.SystemToastIds systemtoast_systemtoastids, Component component, @Nullable Component component1) {
      SystemToast systemtoast = toastcomponent.getToast(SystemToast.class, systemtoast_systemtoastids);
      if (systemtoast == null) {
         add(toastcomponent, systemtoast_systemtoastids, component, component1);
      } else {
         systemtoast.reset(component, component1);
      }

   }

   public static void onWorldAccessFailure(Minecraft minecraft, String s) {
      add(minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.access_failure"), Component.literal(s));
   }

   public static void onWorldDeleteFailure(Minecraft minecraft, String s) {
      add(minecraft.getToasts(), SystemToast.SystemToastIds.WORLD_ACCESS_FAILURE, Component.translatable("selectWorld.delete_failure"), Component.literal(s));
   }

   public static void onPackCopyFailure(Minecraft minecraft, String s) {
      add(minecraft.getToasts(), SystemToast.SystemToastIds.PACK_COPY_FAILURE, Component.translatable("pack.copyFailure"), Component.literal(s));
   }

   public static enum SystemToastIds {
      TUTORIAL_HINT,
      NARRATOR_TOGGLE,
      WORLD_BACKUP,
      PACK_LOAD_FAILURE,
      WORLD_ACCESS_FAILURE,
      PACK_COPY_FAILURE,
      PERIODIC_NOTIFICATION,
      UNSECURE_SERVER_WARNING(10000L);

      final long displayTime;

      private SystemToastIds(long i) {
         this.displayTime = i;
      }

      private SystemToastIds() {
         this(5000L);
      }
   }
}
