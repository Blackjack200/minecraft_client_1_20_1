package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class NonOverlappingMerger extends AbstractDoubleList implements IndexMerger {
   private final DoubleList lower;
   private final DoubleList upper;
   private final boolean swap;

   protected NonOverlappingMerger(DoubleList doublelist, DoubleList doublelist1, boolean flag) {
      this.lower = doublelist;
      this.upper = doublelist1;
      this.swap = flag;
   }

   public int size() {
      return this.lower.size() + this.upper.size();
   }

   public boolean forMergedIndexes(IndexMerger.IndexConsumer indexmerger_indexconsumer) {
      return this.swap ? this.forNonSwappedIndexes((i, j, k) -> indexmerger_indexconsumer.merge(j, i, k)) : this.forNonSwappedIndexes(indexmerger_indexconsumer);
   }

   private boolean forNonSwappedIndexes(IndexMerger.IndexConsumer indexmerger_indexconsumer) {
      int i = this.lower.size();

      for(int j = 0; j < i; ++j) {
         if (!indexmerger_indexconsumer.merge(j, -1, j)) {
            return false;
         }
      }

      int k = this.upper.size() - 1;

      for(int l = 0; l < k; ++l) {
         if (!indexmerger_indexconsumer.merge(i - 1, l, i + l)) {
            return false;
         }
      }

      return true;
   }

   public double getDouble(int i) {
      return i < this.lower.size() ? this.lower.getDouble(i) : this.upper.getDouble(i - this.lower.size());
   }

   public DoubleList getList() {
      return this;
   }
}
