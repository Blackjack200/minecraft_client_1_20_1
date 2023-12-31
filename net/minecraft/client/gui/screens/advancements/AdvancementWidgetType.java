package net.minecraft.client.gui.screens.advancements;

public enum AdvancementWidgetType {
   OBTAINED(0),
   UNOBTAINED(1);

   private final int y;

   private AdvancementWidgetType(int i) {
      this.y = i;
   }

   public int getIndex() {
      return this.y;
   }
}
