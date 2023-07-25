package net.minecraft.world.inventory;

public abstract class DataSlot {
   private int prevValue;

   public static DataSlot forContainer(final ContainerData containerdata, final int i) {
      return new DataSlot() {
         public int get() {
            return containerdata.get(i);
         }

         public void set(int ix) {
            containerdata.set(i, i);
         }
      };
   }

   public static DataSlot shared(final int[] aint, final int i) {
      return new DataSlot() {
         public int get() {
            return aint[i];
         }

         public void set(int ix) {
            aint[i] = i;
         }
      };
   }

   public static DataSlot standalone() {
      return new DataSlot() {
         private int value;

         public int get() {
            return this.value;
         }

         public void set(int i) {
            this.value = i;
         }
      };
   }

   public abstract int get();

   public abstract void set(int i);

   public boolean checkAndClearUpdateFlag() {
      int i = this.get();
      boolean flag = i != this.prevValue;
      this.prevValue = i;
      return flag;
   }
}
