package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import org.lwjgl.glfw.GLFWDropCallback;

public class MouseHandler {
   private final Minecraft minecraft;
   private boolean isLeftPressed;
   private boolean isMiddlePressed;
   private boolean isRightPressed;
   private double xpos;
   private double ypos;
   private int fakeRightMouse;
   private int activeButton = -1;
   private boolean ignoreFirstMove = true;
   private int clickDepth;
   private double mousePressedTime;
   private final SmoothDouble smoothTurnX = new SmoothDouble();
   private final SmoothDouble smoothTurnY = new SmoothDouble();
   private double accumulatedDX;
   private double accumulatedDY;
   private double accumulatedScroll;
   private double lastMouseEventTime = Double.MIN_VALUE;
   private boolean mouseGrabbed;

   public MouseHandler(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   private void onPress(long i, int j, int k, int l) {
      if (i == this.minecraft.getWindow().getWindow()) {
         if (this.minecraft.screen != null) {
            this.minecraft.setLastInputType(InputType.MOUSE);
         }

         boolean flag = k == 1;
         if (Minecraft.ON_OSX && j == 0) {
            if (flag) {
               if ((l & 2) == 2) {
                  j = 1;
                  ++this.fakeRightMouse;
               }
            } else if (this.fakeRightMouse > 0) {
               j = 1;
               --this.fakeRightMouse;
            }
         }

         int i1 = j;
         if (flag) {
            if (this.minecraft.options.touchscreen().get() && this.clickDepth++ > 0) {
               return;
            }

            this.activeButton = i1;
            this.mousePressedTime = Blaze3D.getTime();
         } else if (this.activeButton != -1) {
            if (this.minecraft.options.touchscreen().get() && --this.clickDepth > 0) {
               return;
            }

            this.activeButton = -1;
         }

         boolean[] aboolean = new boolean[]{false};
         if (this.minecraft.getOverlay() == null) {
            if (this.minecraft.screen == null) {
               if (!this.mouseGrabbed && flag) {
                  this.grabMouse();
               }
            } else {
               double d0 = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
               double d1 = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
               Screen screen = this.minecraft.screen;
               if (flag) {
                  screen.afterMouseAction();
                  Screen.wrapScreenError(() -> aboolean[0] = screen.mouseClicked(d0, d1, i1), "mouseClicked event handler", screen.getClass().getCanonicalName());
               } else {
                  Screen.wrapScreenError(() -> aboolean[0] = screen.mouseReleased(d0, d1, i1), "mouseReleased event handler", screen.getClass().getCanonicalName());
               }
            }
         }

         if (!aboolean[0] && this.minecraft.screen == null && this.minecraft.getOverlay() == null) {
            if (i1 == 0) {
               this.isLeftPressed = flag;
            } else if (i1 == 2) {
               this.isMiddlePressed = flag;
            } else if (i1 == 1) {
               this.isRightPressed = flag;
            }

            KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(i1), flag);
            if (flag) {
               if (this.minecraft.player.isSpectator() && i1 == 2) {
                  this.minecraft.gui.getSpectatorGui().onMouseMiddleClick();
               } else {
                  KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(i1));
               }
            }
         }

      }
   }

   private void onScroll(long i, double d0, double d1) {
      if (i == Minecraft.getInstance().getWindow().getWindow()) {
         double d2 = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(d1) : d1) * this.minecraft.options.mouseWheelSensitivity().get();
         if (this.minecraft.getOverlay() == null) {
            if (this.minecraft.screen != null) {
               double d3 = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
               double d4 = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
               this.minecraft.screen.mouseScrolled(d3, d4, d2);
               this.minecraft.screen.afterMouseAction();
            } else if (this.minecraft.player != null) {
               if (this.accumulatedScroll != 0.0D && Math.signum(d2) != Math.signum(this.accumulatedScroll)) {
                  this.accumulatedScroll = 0.0D;
               }

               this.accumulatedScroll += d2;
               int j = (int)this.accumulatedScroll;
               if (j == 0) {
                  return;
               }

               this.accumulatedScroll -= (double)j;
               if (this.minecraft.player.isSpectator()) {
                  if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
                     this.minecraft.gui.getSpectatorGui().onMouseScrolled(-j);
                  } else {
                     float f = Mth.clamp(this.minecraft.player.getAbilities().getFlyingSpeed() + (float)j * 0.005F, 0.0F, 0.2F);
                     this.minecraft.player.getAbilities().setFlyingSpeed(f);
                  }
               } else {
                  this.minecraft.player.getInventory().swapPaint((double)j);
               }
            }
         }
      }

   }

   private void onDrop(long i, List<Path> list) {
      if (this.minecraft.screen != null) {
         this.minecraft.screen.onFilesDrop(list);
      }

   }

   public void setup(long i) {
      InputConstants.setupMouseCallbacks(i, (i4, d4, d5) -> this.minecraft.execute(() -> this.onMove(i4, d4, d5)), (i2, j2, k2, l2) -> this.minecraft.execute(() -> this.onPress(i2, j2, k2, l2)), (k1, d0, d1) -> this.minecraft.execute(() -> this.onScroll(k1, d0, d1)), (j, k, l) -> {
         Path[] apath = new Path[k];

         for(int i1 = 0; i1 < k; ++i1) {
            apath[i1] = Paths.get(GLFWDropCallback.getName(l, i1));
         }

         this.minecraft.execute(() -> this.onDrop(j, Arrays.asList(apath)));
      });
   }

   private void onMove(long i, double d0, double d1) {
      if (i == Minecraft.getInstance().getWindow().getWindow()) {
         if (this.ignoreFirstMove) {
            this.xpos = d0;
            this.ypos = d1;
            this.ignoreFirstMove = false;
         }

         Screen screen = this.minecraft.screen;
         if (screen != null && this.minecraft.getOverlay() == null) {
            double d2 = d0 * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
            double d3 = d1 * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
            Screen.wrapScreenError(() -> screen.mouseMoved(d2, d3), "mouseMoved event handler", screen.getClass().getCanonicalName());
            if (this.activeButton != -1 && this.mousePressedTime > 0.0D) {
               double d4 = (d0 - this.xpos) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
               double d5 = (d1 - this.ypos) * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
               Screen.wrapScreenError(() -> screen.mouseDragged(d2, d3, this.activeButton, d4, d5), "mouseDragged event handler", screen.getClass().getCanonicalName());
            }

            screen.afterMouseMove();
         }

         this.minecraft.getProfiler().push("mouse");
         if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
            this.accumulatedDX += d0 - this.xpos;
            this.accumulatedDY += d1 - this.ypos;
         }

         this.turnPlayer();
         this.xpos = d0;
         this.ypos = d1;
         this.minecraft.getProfiler().pop();
      }
   }

   public void turnPlayer() {
      double d0 = Blaze3D.getTime();
      double d1 = d0 - this.lastMouseEventTime;
      this.lastMouseEventTime = d0;
      if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
         double d2 = this.minecraft.options.sensitivity().get() * (double)0.6F + (double)0.2F;
         double d3 = d2 * d2 * d2;
         double d4 = d3 * 8.0D;
         double d7;
         double d8;
         if (this.minecraft.options.smoothCamera) {
            double d5 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d4, d1 * d4);
            double d6 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d4, d1 * d4);
            d7 = d5;
            d8 = d6;
         } else if (this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()) {
            this.smoothTurnX.reset();
            this.smoothTurnY.reset();
            d7 = this.accumulatedDX * d3;
            d8 = this.accumulatedDY * d3;
         } else {
            this.smoothTurnX.reset();
            this.smoothTurnY.reset();
            d7 = this.accumulatedDX * d4;
            d8 = this.accumulatedDY * d4;
         }

         this.accumulatedDX = 0.0D;
         this.accumulatedDY = 0.0D;
         int i = 1;
         if (this.minecraft.options.invertYMouse().get()) {
            i = -1;
         }

         this.minecraft.getTutorial().onMouse(d7, d8);
         if (this.minecraft.player != null) {
            this.minecraft.player.turn(d7, d8 * (double)i);
         }

      } else {
         this.accumulatedDX = 0.0D;
         this.accumulatedDY = 0.0D;
      }
   }

   public boolean isLeftPressed() {
      return this.isLeftPressed;
   }

   public boolean isMiddlePressed() {
      return this.isMiddlePressed;
   }

   public boolean isRightPressed() {
      return this.isRightPressed;
   }

   public double xpos() {
      return this.xpos;
   }

   public double ypos() {
      return this.ypos;
   }

   public void setIgnoreFirstMove() {
      this.ignoreFirstMove = true;
   }

   public boolean isMouseGrabbed() {
      return this.mouseGrabbed;
   }

   public void grabMouse() {
      if (this.minecraft.isWindowActive()) {
         if (!this.mouseGrabbed) {
            if (!Minecraft.ON_OSX) {
               KeyMapping.setAll();
            }

            this.mouseGrabbed = true;
            this.xpos = (double)(this.minecraft.getWindow().getScreenWidth() / 2);
            this.ypos = (double)(this.minecraft.getWindow().getScreenHeight() / 2);
            InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
            this.minecraft.setScreen((Screen)null);
            this.minecraft.missTime = 10000;
            this.ignoreFirstMove = true;
         }
      }
   }

   public void releaseMouse() {
      if (this.mouseGrabbed) {
         this.mouseGrabbed = false;
         this.xpos = (double)(this.minecraft.getWindow().getScreenWidth() / 2);
         this.ypos = (double)(this.minecraft.getWindow().getScreenHeight() / 2);
         InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212993, this.xpos, this.ypos);
      }
   }

   public void cursorEntered() {
      this.ignoreFirstMove = true;
   }
}
