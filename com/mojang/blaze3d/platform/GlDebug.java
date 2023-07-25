package com.mojang.blaze3d.platform;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

public class GlDebug {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CIRCULAR_LOG_SIZE = 10;
   private static final Queue<GlDebug.LogEntry> MESSAGE_BUFFER = EvictingQueue.create(10);
   @Nullable
   private static volatile GlDebug.LogEntry lastEntry;
   private static final List<Integer> DEBUG_LEVELS = ImmutableList.of(37190, 37191, 37192, 33387);
   private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of(37190, 37191, 37192);
   private static boolean debugEnabled;

   private static String printUnknownToken(int i) {
      return "Unknown (0x" + Integer.toHexString(i).toUpperCase() + ")";
   }

   public static String sourceToString(int i) {
      switch (i) {
         case 33350:
            return "API";
         case 33351:
            return "WINDOW SYSTEM";
         case 33352:
            return "SHADER COMPILER";
         case 33353:
            return "THIRD PARTY";
         case 33354:
            return "APPLICATION";
         case 33355:
            return "OTHER";
         default:
            return printUnknownToken(i);
      }
   }

   public static String typeToString(int i) {
      switch (i) {
         case 33356:
            return "ERROR";
         case 33357:
            return "DEPRECATED BEHAVIOR";
         case 33358:
            return "UNDEFINED BEHAVIOR";
         case 33359:
            return "PORTABILITY";
         case 33360:
            return "PERFORMANCE";
         case 33361:
            return "OTHER";
         case 33384:
            return "MARKER";
         default:
            return printUnknownToken(i);
      }
   }

   public static String severityToString(int i) {
      switch (i) {
         case 33387:
            return "NOTIFICATION";
         case 37190:
            return "HIGH";
         case 37191:
            return "MEDIUM";
         case 37192:
            return "LOW";
         default:
            return printUnknownToken(i);
      }
   }

   private static void printDebugLog(int i, int j, int k, int l, int i1, long j1, long k1) {
      String s = GLDebugMessageCallback.getMessage(i1, j1);
      GlDebug.LogEntry gldebug_logentry;
      synchronized(MESSAGE_BUFFER) {
         gldebug_logentry = lastEntry;
         if (gldebug_logentry != null && gldebug_logentry.isSame(i, j, k, l, s)) {
            ++gldebug_logentry.count;
         } else {
            gldebug_logentry = new GlDebug.LogEntry(i, j, k, l, s);
            MESSAGE_BUFFER.add(gldebug_logentry);
            lastEntry = gldebug_logentry;
         }
      }

      LOGGER.info("OpenGL debug message: {}", (Object)gldebug_logentry);
   }

   public static List<String> getLastOpenGlDebugMessages() {
      synchronized(MESSAGE_BUFFER) {
         List<String> list = Lists.newArrayListWithCapacity(MESSAGE_BUFFER.size());

         for(GlDebug.LogEntry gldebug_logentry : MESSAGE_BUFFER) {
            list.add(gldebug_logentry + " x " + gldebug_logentry.count);
         }

         return list;
      }
   }

   public static boolean isDebugEnabled() {
      return debugEnabled;
   }

   public static void enableDebugCallback(int i, boolean flag) {
      RenderSystem.assertInInitPhase();
      if (i > 0) {
         GLCapabilities glcapabilities = GL.getCapabilities();
         if (glcapabilities.GL_KHR_debug) {
            debugEnabled = true;
            GL11.glEnable(37600);
            if (flag) {
               GL11.glEnable(33346);
            }

            for(int j = 0; j < DEBUG_LEVELS.size(); ++j) {
               boolean flag1 = j < i;
               KHRDebug.glDebugMessageControl(4352, 4352, DEBUG_LEVELS.get(j), (int[])null, flag1);
            }

            KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
         } else if (glcapabilities.GL_ARB_debug_output) {
            debugEnabled = true;
            if (flag) {
               GL11.glEnable(33346);
            }

            for(int k = 0; k < DEBUG_LEVELS_ARB.size(); ++k) {
               boolean flag2 = k < i;
               ARBDebugOutput.glDebugMessageControlARB(4352, 4352, DEBUG_LEVELS_ARB.get(k), (int[])null, flag2);
            }

            ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
         }

      }
   }

   static class LogEntry {
      private final int id;
      private final int source;
      private final int type;
      private final int severity;
      private final String message;
      int count = 1;

      LogEntry(int i, int j, int k, int l, String s) {
         this.id = k;
         this.source = i;
         this.type = j;
         this.severity = l;
         this.message = s;
      }

      boolean isSame(int i, int j, int k, int l, String s) {
         return j == this.type && i == this.source && k == this.id && l == this.severity && s.equals(this.message);
      }

      public String toString() {
         return "id=" + this.id + ", source=" + GlDebug.sourceToString(this.source) + ", type=" + GlDebug.typeToString(this.type) + ", severity=" + GlDebug.severityToString(this.severity) + ", message='" + this.message + "'";
      }
   }
}
