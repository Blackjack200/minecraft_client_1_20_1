package net.minecraft.client.gui;

import javax.annotation.Nullable;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface ComponentPath {
   static ComponentPath leaf(GuiEventListener guieventlistener) {
      return new ComponentPath.Leaf(guieventlistener);
   }

   @Nullable
   static ComponentPath path(ContainerEventHandler containereventhandler, @Nullable ComponentPath componentpath) {
      return componentpath == null ? null : new ComponentPath.Path(containereventhandler, componentpath);
   }

   static ComponentPath path(GuiEventListener guieventlistener, ContainerEventHandler... acontainereventhandler) {
      ComponentPath componentpath = leaf(guieventlistener);

      for(ContainerEventHandler containereventhandler : acontainereventhandler) {
         componentpath = path(containereventhandler, componentpath);
      }

      return componentpath;
   }

   GuiEventListener component();

   void applyFocus(boolean flag);

   public static record Leaf(GuiEventListener component) implements ComponentPath {
      public void applyFocus(boolean flag) {
         this.component.setFocused(flag);
      }
   }

   public static record Path(ContainerEventHandler component, ComponentPath childPath) implements ComponentPath {
      public void applyFocus(boolean flag) {
         if (!flag) {
            this.component.setFocused((GuiEventListener)null);
         } else {
            this.component.setFocused(this.childPath.component());
         }

         this.childPath.applyFocus(flag);
      }
   }
}
