package net.minecraft.world.phys.shapes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class IdenticalMerger implements IndexMerger {
   private final DoubleList coords;

   public IdenticalMerger(DoubleList doublelist) {
      this.coords = doublelist;
   }

   public boolean forMergedIndexes(IndexMerger.IndexConsumer indexmerger_indexconsumer) {
      int i = this.coords.size() - 1;

      for(int j = 0; j < i; ++j) {
         if (!indexmerger_indexconsumer.merge(j, j, j)) {
            return false;
         }
      }

      return true;
   }

   public int size() {
      return this.coords.size();
   }

   public DoubleList getList() {
      return this.coords;
   }
}
