package com.mojang.blaze3d.platform;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public final class Monitor {
   private final long monitor;
   private final List<VideoMode> videoModes;
   private VideoMode currentMode;
   private int x;
   private int y;

   public Monitor(long i) {
      this.monitor = i;
      this.videoModes = Lists.newArrayList();
      this.refreshVideoModes();
   }

   public void refreshVideoModes() {
      RenderSystem.assertInInitPhase();
      this.videoModes.clear();
      GLFWVidMode.Buffer glfwvidmode_buffer = GLFW.glfwGetVideoModes(this.monitor);

      for(int i = glfwvidmode_buffer.limit() - 1; i >= 0; --i) {
         glfwvidmode_buffer.position(i);
         VideoMode videomode = new VideoMode(glfwvidmode_buffer);
         if (videomode.getRedBits() >= 8 && videomode.getGreenBits() >= 8 && videomode.getBlueBits() >= 8) {
            this.videoModes.add(videomode);
         }
      }

      int[] aint = new int[1];
      int[] aint1 = new int[1];
      GLFW.glfwGetMonitorPos(this.monitor, aint, aint1);
      this.x = aint[0];
      this.y = aint1[0];
      GLFWVidMode glfwvidmode = GLFW.glfwGetVideoMode(this.monitor);
      this.currentMode = new VideoMode(glfwvidmode);
   }

   public VideoMode getPreferredVidMode(Optional<VideoMode> optional) {
      RenderSystem.assertInInitPhase();
      if (optional.isPresent()) {
         VideoMode videomode = optional.get();

         for(VideoMode videomode1 : this.videoModes) {
            if (videomode1.equals(videomode)) {
               return videomode1;
            }
         }
      }

      return this.getCurrentMode();
   }

   public int getVideoModeIndex(VideoMode videomode) {
      RenderSystem.assertInInitPhase();
      return this.videoModes.indexOf(videomode);
   }

   public VideoMode getCurrentMode() {
      return this.currentMode;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public VideoMode getMode(int i) {
      return this.videoModes.get(i);
   }

   public int getModeCount() {
      return this.videoModes.size();
   }

   public long getMonitor() {
      return this.monitor;
   }

   public String toString() {
      return String.format(Locale.ROOT, "Monitor[%s %sx%s %s]", this.monitor, this.x, this.y, this.currentMode);
   }
}
