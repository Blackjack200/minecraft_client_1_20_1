package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.glfw.GLFWMonitorCallbackI;
import org.slf4j.Logger;

public class ScreenManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<>();
   private final MonitorCreator monitorCreator;

   public ScreenManager(MonitorCreator monitorcreator) {
      RenderSystem.assertInInitPhase();
      this.monitorCreator = monitorcreator;
      GLFW.glfwSetMonitorCallback(this::onMonitorChange);
      PointerBuffer pointerbuffer = GLFW.glfwGetMonitors();
      if (pointerbuffer != null) {
         for(int i = 0; i < pointerbuffer.limit(); ++i) {
            long j = pointerbuffer.get(i);
            this.monitors.put(j, monitorcreator.createMonitor(j));
         }
      }

   }

   private void onMonitorChange(long k, int l) {
      RenderSystem.assertOnRenderThread();
      if (l == 262145) {
         this.monitors.put(k, this.monitorCreator.createMonitor(k));
         LOGGER.debug("Monitor {} connected. Current monitors: {}", k, this.monitors);
      } else if (l == 262146) {
         this.monitors.remove(k);
         LOGGER.debug("Monitor {} disconnected. Current monitors: {}", k, this.monitors);
      }

   }

   @Nullable
   public Monitor getMonitor(long i) {
      RenderSystem.assertInInitPhase();
      return this.monitors.get(i);
   }

   @Nullable
   public Monitor findBestMonitor(Window window) {
      long i = GLFW.glfwGetWindowMonitor(window.getWindow());
      if (i != 0L) {
         return this.getMonitor(i);
      } else {
         int j = window.getX();
         int k = j + window.getScreenWidth();
         int l = window.getY();
         int i1 = l + window.getScreenHeight();
         int j1 = -1;
         Monitor monitor = null;
         long k1 = GLFW.glfwGetPrimaryMonitor();
         LOGGER.debug("Selecting monitor - primary: {}, current monitors: {}", k1, this.monitors);

         for(Monitor monitor1 : this.monitors.values()) {
            int l1 = monitor1.getX();
            int i2 = l1 + monitor1.getCurrentMode().getWidth();
            int j2 = monitor1.getY();
            int k2 = j2 + monitor1.getCurrentMode().getHeight();
            int l2 = clamp(j, l1, i2);
            int i3 = clamp(k, l1, i2);
            int j3 = clamp(l, j2, k2);
            int k3 = clamp(i1, j2, k2);
            int l3 = Math.max(0, i3 - l2);
            int i4 = Math.max(0, k3 - j3);
            int j4 = l3 * i4;
            if (j4 > j1) {
               monitor = monitor1;
               j1 = j4;
            } else if (j4 == j1 && k1 == monitor1.getMonitor()) {
               LOGGER.debug("Primary monitor {} is preferred to monitor {}", monitor1, monitor);
               monitor = monitor1;
            }
         }

         LOGGER.debug("Selected monitor: {}", (Object)monitor);
         return monitor;
      }
   }

   public static int clamp(int i, int j, int k) {
      if (i < j) {
         return j;
      } else {
         return i > k ? k : i;
      }
   }

   public void shutdown() {
      RenderSystem.assertOnRenderThread();
      GLFWMonitorCallback glfwmonitorcallback = GLFW.glfwSetMonitorCallback((GLFWMonitorCallbackI)null);
      if (glfwmonitorcallback != null) {
         glfwmonitorcallback.free();
      }

   }
}
