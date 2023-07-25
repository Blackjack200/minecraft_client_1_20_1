package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class ToastComponent {
   private static final int SLOT_COUNT = 5;
   private static final int NO_SPACE = -1;
   final Minecraft minecraft;
   private final List<ToastComponent.ToastInstance<?>> visible = new ArrayList<>();
   private final BitSet occupiedSlots = new BitSet(5);
   private final Deque<Toast> queued = Queues.newArrayDeque();

   public ToastComponent(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(GuiGraphics guigraphics) {
      if (!this.minecraft.options.hideGui) {
         int i = guigraphics.guiWidth();
         this.visible.removeIf((toastcomponent_toastinstance) -> {
            if (toastcomponent_toastinstance != null && toastcomponent_toastinstance.render(i, guigraphics)) {
               this.occupiedSlots.clear(toastcomponent_toastinstance.index, toastcomponent_toastinstance.index + toastcomponent_toastinstance.slotCount);
               return true;
            } else {
               return false;
            }
         });
         if (!this.queued.isEmpty() && this.freeSlots() > 0) {
            this.queued.removeIf((toast) -> {
               int j = toast.slotCount();
               int k = this.findFreeIndex(j);
               if (k != -1) {
                  this.visible.add(new ToastComponent.ToastInstance<>(toast, k, j));
                  this.occupiedSlots.set(k, k + j);
                  return true;
               } else {
                  return false;
               }
            });
         }

      }
   }

   private int findFreeIndex(int i) {
      if (this.freeSlots() >= i) {
         int j = 0;

         for(int k = 0; k < 5; ++k) {
            if (this.occupiedSlots.get(k)) {
               j = 0;
            } else {
               ++j;
               if (j == i) {
                  return k + 1 - j;
               }
            }
         }
      }

      return -1;
   }

   private int freeSlots() {
      return 5 - this.occupiedSlots.cardinality();
   }

   @Nullable
   public <T extends Toast> T getToast(Class<? extends T> oclass, Object object) {
      for(ToastComponent.ToastInstance<?> toastcomponent_toastinstance : this.visible) {
         if (toastcomponent_toastinstance != null && oclass.isAssignableFrom(toastcomponent_toastinstance.getToast().getClass()) && toastcomponent_toastinstance.getToast().getToken().equals(object)) {
            return (T)toastcomponent_toastinstance.getToast();
         }
      }

      for(Toast toast : this.queued) {
         if (oclass.isAssignableFrom(toast.getClass()) && toast.getToken().equals(object)) {
            return (T)toast;
         }
      }

      return (T)null;
   }

   public void clear() {
      this.occupiedSlots.clear();
      this.visible.clear();
      this.queued.clear();
   }

   public void addToast(Toast toast) {
      this.queued.add(toast);
   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public double getNotificationDisplayTimeMultiplier() {
      return this.minecraft.options.notificationDisplayTime().get();
   }

   class ToastInstance<T extends Toast> {
      private static final long ANIMATION_TIME = 600L;
      private final T toast;
      final int index;
      final int slotCount;
      private long animationTime = -1L;
      private long visibleTime = -1L;
      private Toast.Visibility visibility = Toast.Visibility.SHOW;

      ToastInstance(T toast, int i, int j) {
         this.toast = toast;
         this.index = i;
         this.slotCount = j;
      }

      public T getToast() {
         return this.toast;
      }

      private float getVisibility(long i) {
         float f = Mth.clamp((float)(i - this.animationTime) / 600.0F, 0.0F, 1.0F);
         f *= f;
         return this.visibility == Toast.Visibility.HIDE ? 1.0F - f : f;
      }

      public boolean render(int i, GuiGraphics guigraphics) {
         long j = Util.getMillis();
         if (this.animationTime == -1L) {
            this.animationTime = j;
            this.visibility.playSound(ToastComponent.this.minecraft.getSoundManager());
         }

         if (this.visibility == Toast.Visibility.SHOW && j - this.animationTime <= 600L) {
            this.visibleTime = j;
         }

         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)i - (float)this.toast.width() * this.getVisibility(j), (float)(this.index * 32), 800.0F);
         Toast.Visibility toast_visibility = this.toast.render(guigraphics, ToastComponent.this, j - this.visibleTime);
         guigraphics.pose().popPose();
         if (toast_visibility != this.visibility) {
            this.animationTime = j - (long)((int)((1.0F - this.getVisibility(j)) * 600.0F));
            this.visibility = toast_visibility;
            this.visibility.playSound(ToastComponent.this.minecraft.getSoundManager());
         }

         return this.visibility == Toast.Visibility.HIDE && j - this.animationTime > 600L;
      }
   }
}
