package com.mojang.blaze3d.platform;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import net.minecraft.server.packs.resources.IoSupplier;
import org.lwjgl.glfw.GLFWNativeCocoa;

public class MacosUtil {
   private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

   public static void toggleFullscreen(long i) {
      getNsWindow(i).filter(MacosUtil::isInKioskMode).ifPresent(MacosUtil::toggleFullscreen);
   }

   private static Optional<NSObject> getNsWindow(long i) {
      long j = GLFWNativeCocoa.glfwGetCocoaWindow(i);
      return j != 0L ? Optional.of(new NSObject(new Pointer(j))) : Optional.empty();
   }

   private static boolean isInKioskMode(NSObject nsobject) {
      return (nsobject.sendRaw("styleMask", new Object[0]) & 16384L) == 16384L;
   }

   private static void toggleFullscreen(NSObject nsobject1) {
      nsobject1.send("toggleFullScreen:", new Object[]{Pointer.NULL});
   }

   public static void loadIcon(IoSupplier<InputStream> iosupplier) throws IOException {
      InputStream inputstream = iosupplier.get();

      try {
         String s = Base64.getEncoder().encodeToString(inputstream.readAllBytes());
         Client client = Client.getInstance();
         Object object = client.sendProxy("NSData", "alloc").send("initWithBase64Encoding:", s);
         Object object1 = client.sendProxy("NSImage", "alloc").send("initWithData:", object);
         client.sendProxy("NSApplication", "sharedApplication").send("setApplicationIconImage:", object1);
      } catch (Throwable var7) {
         if (inputstream != null) {
            try {
               inputstream.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (inputstream != null) {
         inputstream.close();
      }

   }
}
