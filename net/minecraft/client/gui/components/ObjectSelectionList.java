package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;

public abstract class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList<E> {
   private static final Component USAGE_NARRATION = Component.translatable("narration.selection.usage");

   public ObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l, int i1) {
      super(minecraft, i, j, k, l, i1);
   }

   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      if (this.getItemCount() == 0) {
         return null;
      } else if (this.isFocused() && focusnavigationevent instanceof FocusNavigationEvent.ArrowNavigation) {
         FocusNavigationEvent.ArrowNavigation focusnavigationevent_arrownavigation = (FocusNavigationEvent.ArrowNavigation)focusnavigationevent;
         E objectselectionlist_entry = this.nextEntry(focusnavigationevent_arrownavigation.direction());
         return objectselectionlist_entry != null ? ComponentPath.path(this, ComponentPath.leaf(objectselectionlist_entry)) : null;
      } else if (!this.isFocused()) {
         E objectselectionlist_entry1 = this.getSelected();
         if (objectselectionlist_entry1 == null) {
            objectselectionlist_entry1 = this.nextEntry(focusnavigationevent.getVerticalDirectionForInitialFocus());
         }

         return objectselectionlist_entry1 == null ? null : ComponentPath.path(this, ComponentPath.leaf(objectselectionlist_entry1));
      } else {
         return null;
      }
   }

   public void updateNarration(NarrationElementOutput narrationelementoutput) {
      E objectselectionlist_entry = this.getHovered();
      if (objectselectionlist_entry != null) {
         this.narrateListElementPosition(narrationelementoutput.nest(), objectselectionlist_entry);
         objectselectionlist_entry.updateNarration(narrationelementoutput);
      } else {
         E objectselectionlist_entry1 = this.getSelected();
         if (objectselectionlist_entry1 != null) {
            this.narrateListElementPosition(narrationelementoutput.nest(), objectselectionlist_entry1);
            objectselectionlist_entry1.updateNarration(narrationelementoutput);
         }
      }

      if (this.isFocused()) {
         narrationelementoutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
      }

   }

   public abstract static class Entry<E extends ObjectSelectionList.Entry<E>> extends AbstractSelectionList.Entry<E> implements NarrationSupplier {
      public abstract Component getNarration();

      public void updateNarration(NarrationElementOutput narrationelementoutput) {
         narrationelementoutput.add(NarratedElementType.TITLE, this.getNarration());
      }
   }
}
