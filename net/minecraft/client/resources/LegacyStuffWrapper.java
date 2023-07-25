package net.minecraft.client.resources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class LegacyStuffWrapper {
   /** @deprecated */
   @Deprecated
   public static int[] getPixels(ResourceManager resourcemanager, ResourceLocation resourcelocation) throws IOException {
      InputStream inputstream = resourcemanager.open(resourcelocation);

      int[] var4;
      try {
         NativeImage nativeimage = NativeImage.read(inputstream);

         try {
            var4 = nativeimage.makePixelArray();
         } catch (Throwable var8) {
            if (nativeimage != null) {
               try {
                  nativeimage.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (nativeimage != null) {
            nativeimage.close();
         }
      } catch (Throwable var9) {
         if (inputstream != null) {
            try {
               inputstream.close();
            } catch (Throwable var6) {
               var9.addSuppressed(var6);
            }
         }

         throw var9;
      }

      if (inputstream != null) {
         inputstream.close();
      }

      return var4;
   }
}
