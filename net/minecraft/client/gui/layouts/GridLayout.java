package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.Mth;

public class GridLayout extends AbstractLayout {
   private final List<LayoutElement> children = new ArrayList<>();
   private final List<GridLayout.CellInhabitant> cellInhabitants = new ArrayList<>();
   private final LayoutSettings defaultCellSettings = LayoutSettings.defaults();
   private int rowSpacing = 0;
   private int columnSpacing = 0;

   public GridLayout() {
      this(0, 0);
   }

   public GridLayout(int i, int j) {
      super(i, j, 0, 0);
   }

   public void arrangeElements() {
      super.arrangeElements();
      int i = 0;
      int j = 0;

      for(GridLayout.CellInhabitant gridlayout_cellinhabitant : this.cellInhabitants) {
         i = Math.max(gridlayout_cellinhabitant.getLastOccupiedRow(), i);
         j = Math.max(gridlayout_cellinhabitant.getLastOccupiedColumn(), j);
      }

      int[] aint = new int[j + 1];
      int[] aint1 = new int[i + 1];

      for(GridLayout.CellInhabitant gridlayout_cellinhabitant1 : this.cellInhabitants) {
         int k = gridlayout_cellinhabitant1.getHeight() - (gridlayout_cellinhabitant1.occupiedRows - 1) * this.rowSpacing;
         Divisor divisor = new Divisor(k, gridlayout_cellinhabitant1.occupiedRows);

         for(int l = gridlayout_cellinhabitant1.row; l <= gridlayout_cellinhabitant1.getLastOccupiedRow(); ++l) {
            aint1[l] = Math.max(aint1[l], divisor.nextInt());
         }

         int i1 = gridlayout_cellinhabitant1.getWidth() - (gridlayout_cellinhabitant1.occupiedColumns - 1) * this.columnSpacing;
         Divisor divisor1 = new Divisor(i1, gridlayout_cellinhabitant1.occupiedColumns);

         for(int j1 = gridlayout_cellinhabitant1.column; j1 <= gridlayout_cellinhabitant1.getLastOccupiedColumn(); ++j1) {
            aint[j1] = Math.max(aint[j1], divisor1.nextInt());
         }
      }

      int[] aint2 = new int[j + 1];
      int[] aint3 = new int[i + 1];
      aint2[0] = 0;

      for(int k1 = 1; k1 <= j; ++k1) {
         aint2[k1] = aint2[k1 - 1] + aint[k1 - 1] + this.columnSpacing;
      }

      aint3[0] = 0;

      for(int l1 = 1; l1 <= i; ++l1) {
         aint3[l1] = aint3[l1 - 1] + aint1[l1 - 1] + this.rowSpacing;
      }

      for(GridLayout.CellInhabitant gridlayout_cellinhabitant2 : this.cellInhabitants) {
         int i2 = 0;

         for(int j2 = gridlayout_cellinhabitant2.column; j2 <= gridlayout_cellinhabitant2.getLastOccupiedColumn(); ++j2) {
            i2 += aint[j2];
         }

         i2 += this.columnSpacing * (gridlayout_cellinhabitant2.occupiedColumns - 1);
         gridlayout_cellinhabitant2.setX(this.getX() + aint2[gridlayout_cellinhabitant2.column], i2);
         int k2 = 0;

         for(int l2 = gridlayout_cellinhabitant2.row; l2 <= gridlayout_cellinhabitant2.getLastOccupiedRow(); ++l2) {
            k2 += aint1[l2];
         }

         k2 += this.rowSpacing * (gridlayout_cellinhabitant2.occupiedRows - 1);
         gridlayout_cellinhabitant2.setY(this.getY() + aint3[gridlayout_cellinhabitant2.row], k2);
      }

      this.width = aint2[j] + aint[j];
      this.height = aint3[i] + aint1[i];
   }

   public <T extends LayoutElement> T addChild(T layoutelement, int i, int j) {
      return this.addChild(layoutelement, i, j, this.newCellSettings());
   }

   public <T extends LayoutElement> T addChild(T layoutelement, int i, int j, LayoutSettings layoutsettings) {
      return this.addChild(layoutelement, i, j, 1, 1, layoutsettings);
   }

   public <T extends LayoutElement> T addChild(T layoutelement, int i, int j, int k, int l) {
      return this.addChild(layoutelement, i, j, k, l, this.newCellSettings());
   }

   public <T extends LayoutElement> T addChild(T layoutelement, int i, int j, int k, int l, LayoutSettings layoutsettings) {
      if (k < 1) {
         throw new IllegalArgumentException("Occupied rows must be at least 1");
      } else if (l < 1) {
         throw new IllegalArgumentException("Occupied columns must be at least 1");
      } else {
         this.cellInhabitants.add(new GridLayout.CellInhabitant(layoutelement, i, j, k, l, layoutsettings));
         this.children.add(layoutelement);
         return layoutelement;
      }
   }

   public GridLayout columnSpacing(int i) {
      this.columnSpacing = i;
      return this;
   }

   public GridLayout rowSpacing(int i) {
      this.rowSpacing = i;
      return this;
   }

   public GridLayout spacing(int i) {
      return this.columnSpacing(i).rowSpacing(i);
   }

   public void visitChildren(Consumer<LayoutElement> consumer) {
      this.children.forEach(consumer);
   }

   public LayoutSettings newCellSettings() {
      return this.defaultCellSettings.copy();
   }

   public LayoutSettings defaultCellSetting() {
      return this.defaultCellSettings;
   }

   public GridLayout.RowHelper createRowHelper(int i) {
      return new GridLayout.RowHelper(i);
   }

   static class CellInhabitant extends AbstractLayout.AbstractChildWrapper {
      final int row;
      final int column;
      final int occupiedRows;
      final int occupiedColumns;

      CellInhabitant(LayoutElement layoutelement, int i, int j, int k, int l, LayoutSettings layoutsettings) {
         super(layoutelement, layoutsettings.getExposed());
         this.row = i;
         this.column = j;
         this.occupiedRows = k;
         this.occupiedColumns = l;
      }

      public int getLastOccupiedRow() {
         return this.row + this.occupiedRows - 1;
      }

      public int getLastOccupiedColumn() {
         return this.column + this.occupiedColumns - 1;
      }
   }

   public final class RowHelper {
      private final int columns;
      private int index;

      RowHelper(int i) {
         this.columns = i;
      }

      public <T extends LayoutElement> T addChild(T layoutelement) {
         return this.addChild(layoutelement, 1);
      }

      public <T extends LayoutElement> T addChild(T layoutelement, int i) {
         return this.addChild(layoutelement, i, this.defaultCellSetting());
      }

      public <T extends LayoutElement> T addChild(T layoutelement, LayoutSettings layoutsettings) {
         return this.addChild(layoutelement, 1, layoutsettings);
      }

      public <T extends LayoutElement> T addChild(T layoutelement, int i, LayoutSettings layoutsettings) {
         int j = this.index / this.columns;
         int k = this.index % this.columns;
         if (k + i > this.columns) {
            ++j;
            k = 0;
            this.index = Mth.roundToward(this.index, this.columns);
         }

         this.index += i;
         return GridLayout.this.addChild(layoutelement, j, k, 1, i, layoutsettings);
      }

      public GridLayout getGrid() {
         return GridLayout.this;
      }

      public LayoutSettings newCellSettings() {
         return GridLayout.this.newCellSettings();
      }

      public LayoutSettings defaultCellSetting() {
         return GridLayout.this.defaultCellSetting();
      }
   }
}
