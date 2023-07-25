package net.minecraft.client.gui.components.events;

import javax.annotation.Nullable;

public abstract class AbstractContainerEventHandler implements ContainerEventHandler {
   @Nullable
   private GuiEventListener focused;
   private boolean isDragging;

   public final boolean isDragging() {
      return this.isDragging;
   }

   public final void setDragging(boolean flag) {
      this.isDragging = flag;
   }

   @Nullable
   public GuiEventListener getFocused() {
      return this.focused;
   }

   public void setFocused(@Nullable GuiEventListener guieventlistener) {
      if (this.focused != null) {
         this.focused.setFocused(false);
      }

      if (guieventlistener != null) {
         guieventlistener.setFocused(true);
      }

      this.focused = guieventlistener;
   }
}
