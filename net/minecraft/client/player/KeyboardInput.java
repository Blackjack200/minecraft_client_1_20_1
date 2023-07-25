package net.minecraft.client.player;

import net.minecraft.client.Options;

public class KeyboardInput extends Input {
   private final Options options;

   public KeyboardInput(Options options) {
      this.options = options;
   }

   private static float calculateImpulse(boolean flag, boolean flag1) {
      if (flag == flag1) {
         return 0.0F;
      } else {
         return flag ? 1.0F : -1.0F;
      }
   }

   public void tick(boolean flag, float f) {
      this.up = this.options.keyUp.isDown();
      this.down = this.options.keyDown.isDown();
      this.left = this.options.keyLeft.isDown();
      this.right = this.options.keyRight.isDown();
      this.forwardImpulse = calculateImpulse(this.up, this.down);
      this.leftImpulse = calculateImpulse(this.left, this.right);
      this.jumping = this.options.keyJump.isDown();
      this.shiftKeyDown = this.options.keyShift.isDown();
      if (flag) {
         this.leftImpulse *= f;
         this.forwardImpulse *= f;
      }

   }
}
