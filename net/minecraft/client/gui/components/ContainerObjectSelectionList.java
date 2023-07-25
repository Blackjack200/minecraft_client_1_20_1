package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class ContainerObjectSelectionList<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
   public ContainerObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int i1) {
      super(minecraft, i, j, k, l, i1);
   }

   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      if (this.getItemCount() == 0) {
         return null;
      } else if (!(focusnavigationevent instanceof FocusNavigationEvent.ArrowNavigation)) {
         return super.nextFocusPath(focusnavigationevent);
      } else {
         FocusNavigationEvent.ArrowNavigation focusnavigationevent_arrownavigation = (FocusNavigationEvent.ArrowNavigation)focusnavigationevent;
         E containerobjectselectionlist_entry = this.getFocused();
         if (focusnavigationevent_arrownavigation.direction().getAxis() == ScreenAxis.HORIZONTAL && containerobjectselectionlist_entry != null) {
            return ComponentPath.path(this, containerobjectselectionlist_entry.nextFocusPath(focusnavigationevent));
         } else {
            int i = -1;
            ScreenDirection screendirection = focusnavigationevent_arrownavigation.direction();
            if (containerobjectselectionlist_entry != null) {
               i = containerobjectselectionlist_entry.children().indexOf(containerobjectselectionlist_entry.getFocused());
            }

            if (i == -1) {
               switch (screendirection) {
                  case LEFT:
                     i = Integer.MAX_VALUE;
                     screendirection = ScreenDirection.DOWN;
                     break;
                  case RIGHT:
                     i = 0;
                     screendirection = ScreenDirection.DOWN;
                     break;
                  default:
                     i = 0;
               }
            }

            E containerobjectselectionlist_entry1 = containerobjectselectionlist_entry;

            ComponentPath componentpath;
            do {
               containerobjectselectionlist_entry1 = this.nextEntry(screendirection, (containerobjectselectionlist_entry2) -> !containerobjectselectionlist_entry2.children().isEmpty(), containerobjectselectionlist_entry1);
               if (containerobjectselectionlist_entry1 == null) {
                  return null;
               }

               componentpath = containerobjectselectionlist_entry1.focusPathAtIndex(focusnavigationevent_arrownavigation, i);
            } while(componentpath == null);

            return ComponentPath.path(this, componentpath);
         }
      }
   }

   public void setFocused(@Nullable GuiEventListener guieventlistener) {
      super.setFocused(guieventlistener);
      if (guieventlistener == null) {
         this.setSelected((E)null);
      }

   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      return this.isFocused() ? NarratableEntry.NarrationPriority.FOCUSED : super.narrationPriority();
   }

   protected boolean isSelectedItem(int i) {
      return false;
   }

   public void updateNarration(NarrationElementOutput narrationelementoutput) {
      E containerobjectselectionlist_entry = this.getHovered();
      if (containerobjectselectionlist_entry != null) {
         containerobjectselectionlist_entry.updateNarration(narrationelementoutput.nest());
         this.narrateListElementPosition(narrationelementoutput, containerobjectselectionlist_entry);
      } else {
         E containerobjectselectionlist_entry1 = this.getFocused();
         if (containerobjectselectionlist_entry1 != null) {
            containerobjectselectionlist_entry1.updateNarration(narrationelementoutput.nest());
            this.narrateListElementPosition(narrationelementoutput, containerobjectselectionlist_entry1);
         }
      }

      narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
   }

   public abstract static class Entry<E extends ContainerObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements ContainerEventHandler {
      @Nullable
      private GuiEventListener focused;
      @Nullable
      private NarratableEntry lastNarratable;
      private boolean dragging;

      public boolean isDragging() {
         return this.dragging;
      }

      public void setDragging(boolean flag) {
         this.dragging = flag;
      }

      public boolean mouseClicked(double d0, double d1, int i) {
         return ContainerEventHandler.super.mouseClicked(d0, d1, i);
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

      @Nullable
      public GuiEventListener getFocused() {
         return this.focused;
      }

      @Nullable
      public ComponentPath focusPathAtIndex(FocusNavigationEvent focusnavigationevent, int i) {
         if (this.children().isEmpty()) {
            return null;
         } else {
            ComponentPath componentpath = this.children().get(Math.min(i, this.children().size() - 1)).nextFocusPath(focusnavigationevent);
            return ComponentPath.path(this, componentpath);
         }
      }

      @Nullable
      public ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
         if (focusnavigationevent instanceof FocusNavigationEvent.ArrowNavigation) {
            FocusNavigationEvent.ArrowNavigation focusnavigationevent_arrownavigation = (FocusNavigationEvent.ArrowNavigation)focusnavigationevent;
            byte var10000;
            switch (focusnavigationevent_arrownavigation.direction()) {
               case LEFT:
                  var10000 = -1;
                  break;
               case RIGHT:
                  var10000 = 1;
                  break;
               case UP:
               case DOWN:
                  var10000 = 0;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            int i = var10000;
            if (i == 0) {
               return null;
            }

            int j = Mth.clamp(i + this.children().indexOf(this.getFocused()), 0, this.children().size() - 1);

            for(int k = j; k >= 0 && k < this.children().size(); k += i) {
               GuiEventListener guieventlistener = this.children().get(k);
               ComponentPath componentpath = guieventlistener.nextFocusPath(focusnavigationevent);
               if (componentpath != null) {
                  return ComponentPath.path(this, componentpath);
               }
            }
         }

         return ContainerEventHandler.super.nextFocusPath(focusnavigationevent);
      }

      public abstract List<? extends NarratableEntry> narratables();

      void updateNarration(NarrationElementOutput narrationelementoutput) {
         List<? extends NarratableEntry> list = this.narratables();
         Screen.NarratableSearchResult screen_narratablesearchresult = Screen.findNarratableWidget(list, this.lastNarratable);
         if (screen_narratablesearchresult != null) {
            if (screen_narratablesearchresult.priority.isTerminal()) {
               this.lastNarratable = screen_narratablesearchresult.entry;
            }

            if (list.size() > 1) {
               narrationelementoutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.object_list", screen_narratablesearchresult.index + 1, list.size()));
               if (screen_narratablesearchresult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                  narrationelementoutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.component_list.usage"));
               }
            }

            screen_narratablesearchresult.entry.updateNarration(narrationelementoutput.nest());
         }

      }
   }
}
