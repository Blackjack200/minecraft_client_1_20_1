package net.minecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.BooleanSupplier;

public class ToggleKeyMapping extends KeyMapping {
   private final BooleanSupplier needsToggle;

   public ToggleKeyMapping(String s, int i, String s1, BooleanSupplier booleansupplier) {
      super(s, InputConstants.Type.KEYSYM, i, s1);
      this.needsToggle = booleansupplier;
   }

   public void setDown(boolean flag) {
      if (this.needsToggle.getAsBoolean()) {
         if (flag) {
            super.setDown(!this.isDown());
         }
      } else {
         super.setDown(flag);
      }

   }

   protected void reset() {
      super.setDown(false);
   }
}
