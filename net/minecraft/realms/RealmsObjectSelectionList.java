package net.minecraft.realms;

import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

public abstract class RealmsObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
   protected RealmsObjectSelectionList(int i, int j, int k, int l, int i1) {
      super(Minecraft.getInstance(), i, j, k, l, i1);
   }

   public void setSelectedItem(int i) {
      if (i == -1) {
         this.setSelected((E)null);
      } else if (super.getItemCount() != 0) {
         this.setSelected(this.getEntry(i));
      }

   }

   public void selectItem(int i) {
      this.setSelectedItem(i);
   }

   public void itemClicked(int i, int j, double d0, double d1, int k, int l) {
   }

   public int getMaxPosition() {
      return 0;
   }

   public int getScrollbarPosition() {
      return this.getRowLeft() + this.getRowWidth();
   }

   public int getRowWidth() {
      return (int)((double)this.width * 0.6D);
   }

   public void replaceEntries(Collection<E> collection) {
      super.replaceEntries(collection);
   }

   public int getItemCount() {
      return super.getItemCount();
   }

   public int getRowTop(int i) {
      return super.getRowTop(i);
   }

   public int getRowLeft() {
      return super.getRowLeft();
   }

   public int addEntry(E objectselectionlist_entry) {
      return super.addEntry(objectselectionlist_entry);
   }

   public void clear() {
      this.clearEntries();
   }
}
