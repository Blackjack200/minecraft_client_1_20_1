package com.mojang.blaze3d.platform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import org.lwjgl.system.Pointer;

public class DebugMemoryUntracker {
   @Nullable
   private static final MethodHandle UNTRACK = GLX.make(() -> {
      try {
         MethodHandles.Lookup methodhandles_lookup = MethodHandles.lookup();
         Class<?> oclass = Class.forName("org.lwjgl.system.MemoryManage$DebugAllocator");
         Method method = oclass.getDeclaredMethod("untrack", Long.TYPE);
         method.setAccessible(true);
         Field field = Class.forName("org.lwjgl.system.MemoryUtil$LazyInit").getDeclaredField("ALLOCATOR");
         field.setAccessible(true);
         Object object = field.get((Object)null);
         return oclass.isInstance(object) ? methodhandles_lookup.unreflect(method) : null;
      } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException var5) {
         throw new RuntimeException(var5);
      }
   });

   public static void untrack(long i) {
      if (UNTRACK != null) {
         try {
            UNTRACK.invoke(i);
         } catch (Throwable var3) {
            throw new RuntimeException(var3);
         }
      }
   }

   public static void untrack(Pointer pointer) {
      untrack(pointer.address());
   }
}
