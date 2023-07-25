package com.mojang.realmsclient.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.realms.RealmsObjectSelectionList;

public abstract class RowButton {
   public final int width;
   public final int height;
   public final int xOffset;
   public final int yOffset;

   public RowButton(int i, int j, int k, int l) {
      this.width = i;
      this.height = j;
      this.xOffset = k;
      this.yOffset = l;
   }

   public void drawForRowAt(GuiGraphics guigraphics, int i, int j, int k, int l) {
      int i1 = i + this.xOffset;
      int j1 = j + this.yOffset;
      boolean flag = k >= i1 && k <= i1 + this.width && l >= j1 && l <= j1 + this.height;
      this.draw(guigraphics, i1, j1, flag);
   }

   protected abstract void draw(GuiGraphics guigraphics, int i, int j, boolean flag);

   public int getRight() {
      return this.xOffset + this.width;
   }

   public int getBottom() {
      return this.yOffset + this.height;
   }

   public abstract void onClick(int i);

   public static void drawButtonsInRow(GuiGraphics guigraphics, List<RowButton> list, RealmsObjectSelectionList<?> realmsobjectselectionlist, int i, int j, int k, int l) {
      for(RowButton rowbutton : list) {
         if (realmsobjectselectionlist.getRowWidth() > rowbutton.getRight()) {
            rowbutton.drawForRowAt(guigraphics, i, j, k, l);
         }
      }

   }

   public static void rowButtonMouseClicked(RealmsObjectSelectionList<?> realmsobjectselectionlist, ObjectSelectionList.Entry<?> objectselectionlist_entry, List<RowButton> list, int i, double d0, double d1) {
      if (i == 0) {
         int j = realmsobjectselectionlist.children().indexOf(objectselectionlist_entry);
         if (j > -1) {
            realmsobjectselectionlist.selectItem(j);
            int k = realmsobjectselectionlist.getRowLeft();
            int l = realmsobjectselectionlist.getRowTop(j);
            int i1 = (int)(d0 - (double)k);
            int j1 = (int)(d1 - (double)l);

            for(RowButton rowbutton : list) {
               if (i1 >= rowbutton.xOffset && i1 <= rowbutton.getRight() && j1 >= rowbutton.yOffset && j1 <= rowbutton.getBottom()) {
                  rowbutton.onClick(j);
               }
            }
         }
      }

   }
}
