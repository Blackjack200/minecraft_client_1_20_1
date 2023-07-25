package net.minecraft.world.inventory;

public class SimpleContainerData implements ContainerData {
   private final int[] ints;

   public SimpleContainerData(int i) {
      this.ints = new int[i];
   }

   public int get(int i) {
      return this.ints[i];
   }

   public void set(int i, int j) {
      this.ints[i] = j;
   }

   public int getCount() {
      return this.ints.length;
   }
}
