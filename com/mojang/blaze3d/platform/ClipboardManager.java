package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import java.nio.ByteBuffer;
import net.minecraft.util.StringDecomposer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

public class ClipboardManager {
   public static final int FORMAT_UNAVAILABLE = 65545;
   private final ByteBuffer clipboardScratchBuffer = BufferUtils.createByteBuffer(8192);

   public String getClipboard(long i, GLFWErrorCallbackI glfwerrorcallbacki) {
      GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(glfwerrorcallbacki);
      String s = GLFW.glfwGetClipboardString(i);
      s = s != null ? StringDecomposer.filterBrokenSurrogates(s) : "";
      GLFWErrorCallback glfwerrorcallback1 = GLFW.glfwSetErrorCallback(glfwerrorcallback);
      if (glfwerrorcallback1 != null) {
         glfwerrorcallback1.free();
      }

      return s;
   }

   private static void pushClipboard(long i, ByteBuffer bytebuffer, byte[] abyte) {
      bytebuffer.clear();
      bytebuffer.put(abyte);
      bytebuffer.put((byte)0);
      bytebuffer.flip();
      GLFW.glfwSetClipboardString(i, bytebuffer);
   }

   public void setClipboard(long i, String s) {
      byte[] abyte = s.getBytes(Charsets.UTF_8);
      int j = abyte.length + 1;
      if (j < this.clipboardScratchBuffer.capacity()) {
         pushClipboard(i, this.clipboardScratchBuffer, abyte);
      } else {
         ByteBuffer bytebuffer = MemoryUtil.memAlloc(j);

         try {
            pushClipboard(i, bytebuffer, abyte);
         } finally {
            MemoryUtil.memFree(bytebuffer);
         }
      }

   }
}
