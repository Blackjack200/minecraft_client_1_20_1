package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public class LazyLoadedImage {
   private final ResourceLocation id;
   private final Resource resource;
   private final AtomicReference<NativeImage> image = new AtomicReference<>();
   private final AtomicInteger referenceCount;

   public LazyLoadedImage(ResourceLocation resourcelocation, Resource resource, int i) {
      this.id = resourcelocation;
      this.resource = resource;
      this.referenceCount = new AtomicInteger(i);
   }

   public NativeImage get() throws IOException {
      NativeImage nativeimage = this.image.get();
      if (nativeimage == null) {
         synchronized(this) {
            nativeimage = this.image.get();
            if (nativeimage == null) {
               try {
                  InputStream inputstream = this.resource.open();

                  try {
                     nativeimage = NativeImage.read(inputstream);
                     this.image.set(nativeimage);
                  } catch (Throwable var8) {
                     if (inputstream != null) {
                        try {
                           inputstream.close();
                        } catch (Throwable var7) {
                           var8.addSuppressed(var7);
                        }
                     }

                     throw var8;
                  }

                  if (inputstream != null) {
                     inputstream.close();
                  }
               } catch (IOException var9) {
                  throw new IOException("Failed to load image " + this.id, var9);
               }
            }
         }
      }

      return nativeimage;
   }

   public void release() {
      int i = this.referenceCount.decrementAndGet();
      if (i <= 0) {
         NativeImage nativeimage = this.image.getAndSet((NativeImage)null);
         if (nativeimage != null) {
            nativeimage.close();
         }
      }

   }
}
