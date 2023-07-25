package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class MemoryTracker {
   private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

   public static ByteBuffer create(int i) {
      long j = ALLOCATOR.malloc((long)i);
      if (j == 0L) {
         throw new OutOfMemoryError("Failed to allocate " + i + " bytes");
      } else {
         return MemoryUtil.memByteBuffer(j, i);
      }
   }

   public static ByteBuffer resize(ByteBuffer bytebuffer, int i) {
      long j = ALLOCATOR.realloc(MemoryUtil.memAddress0(bytebuffer), (long)i);
      if (j == 0L) {
         throw new OutOfMemoryError("Failed to resize buffer from " + bytebuffer.capacity() + " bytes to " + i + " bytes");
      } else {
         return MemoryUtil.memByteBuffer(j, i);
      }
   }
}
