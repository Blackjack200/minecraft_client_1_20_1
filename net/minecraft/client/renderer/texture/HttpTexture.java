package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class HttpTexture extends SimpleTexture {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int SKIN_WIDTH = 64;
   private static final int SKIN_HEIGHT = 64;
   private static final int LEGACY_SKIN_HEIGHT = 32;
   @Nullable
   private final File file;
   private final String urlString;
   private final boolean processLegacySkin;
   @Nullable
   private final Runnable onDownloaded;
   @Nullable
   private CompletableFuture<?> future;
   private boolean uploaded;

   public HttpTexture(@Nullable File file, String s, ResourceLocation resourcelocation, boolean flag, @Nullable Runnable runnable) {
      super(resourcelocation);
      this.file = file;
      this.urlString = s;
      this.processLegacySkin = flag;
      this.onDownloaded = runnable;
   }

   private void loadCallback(NativeImage nativeimage) {
      if (this.onDownloaded != null) {
         this.onDownloaded.run();
      }

      Minecraft.getInstance().execute(() -> {
         this.uploaded = true;
         if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> this.upload(nativeimage));
         } else {
            this.upload(nativeimage);
         }

      });
   }

   private void upload(NativeImage nativeimage) {
      TextureUtil.prepareImage(this.getId(), nativeimage.getWidth(), nativeimage.getHeight());
      nativeimage.upload(0, 0, 0, true);
   }

   public void load(ResourceManager resourcemanager) throws IOException {
      Minecraft.getInstance().execute(() -> {
         if (!this.uploaded) {
            try {
               super.load(resourcemanager);
            } catch (IOException var3) {
               LOGGER.warn("Failed to load texture: {}", this.location, var3);
            }

            this.uploaded = true;
         }

      });
      if (this.future == null) {
         NativeImage nativeimage;
         if (this.file != null && this.file.isFile()) {
            LOGGER.debug("Loading http texture from local cache ({})", (Object)this.file);
            FileInputStream fileinputstream = new FileInputStream(this.file);
            nativeimage = this.load(fileinputstream);
         } else {
            nativeimage = null;
         }

         if (nativeimage != null) {
            this.loadCallback(nativeimage);
         } else {
            this.future = CompletableFuture.runAsync(() -> {
               HttpURLConnection httpurlconnection = null;
               LOGGER.debug("Downloading http texture from {} to {}", this.urlString, this.file);

               try {
                  httpurlconnection = (HttpURLConnection)(new URL(this.urlString)).openConnection(Minecraft.getInstance().getProxy());
                  httpurlconnection.setDoInput(true);
                  httpurlconnection.setDoOutput(false);
                  httpurlconnection.connect();
                  if (httpurlconnection.getResponseCode() / 100 == 2) {
                     InputStream inputstream;
                     if (this.file != null) {
                        FileUtils.copyInputStreamToFile(httpurlconnection.getInputStream(), this.file);
                        inputstream = new FileInputStream(this.file);
                     } else {
                        inputstream = httpurlconnection.getInputStream();
                     }

                     Minecraft.getInstance().execute(() -> {
                        NativeImage nativeimage2 = this.load(inputstream);
                        if (nativeimage2 != null) {
                           this.loadCallback(nativeimage2);
                        }

                     });
                     return;
                  }
               } catch (Exception var6) {
                  LOGGER.error("Couldn't download http texture", (Throwable)var6);
                  return;
               } finally {
                  if (httpurlconnection != null) {
                     httpurlconnection.disconnect();
                  }

               }

            }, Util.backgroundExecutor());
         }
      }
   }

   @Nullable
   private NativeImage load(InputStream inputstream) {
      NativeImage nativeimage = null;

      try {
         nativeimage = NativeImage.read(inputstream);
         if (this.processLegacySkin) {
            nativeimage = this.processLegacySkin(nativeimage);
         }
      } catch (Exception var4) {
         LOGGER.warn("Error while loading the skin texture", (Throwable)var4);
      }

      return nativeimage;
   }

   @Nullable
   private NativeImage processLegacySkin(NativeImage nativeimage) {
      int i = nativeimage.getHeight();
      int j = nativeimage.getWidth();
      if (j == 64 && (i == 32 || i == 64)) {
         boolean flag = i == 32;
         if (flag) {
            NativeImage nativeimage1 = new NativeImage(64, 64, true);
            nativeimage1.copyFrom(nativeimage);
            nativeimage.close();
            nativeimage = nativeimage1;
            nativeimage1.fillRect(0, 32, 64, 32, 0);
            nativeimage1.copyRect(4, 16, 16, 32, 4, 4, true, false);
            nativeimage1.copyRect(8, 16, 16, 32, 4, 4, true, false);
            nativeimage1.copyRect(0, 20, 24, 32, 4, 12, true, false);
            nativeimage1.copyRect(4, 20, 16, 32, 4, 12, true, false);
            nativeimage1.copyRect(8, 20, 8, 32, 4, 12, true, false);
            nativeimage1.copyRect(12, 20, 16, 32, 4, 12, true, false);
            nativeimage1.copyRect(44, 16, -8, 32, 4, 4, true, false);
            nativeimage1.copyRect(48, 16, -8, 32, 4, 4, true, false);
            nativeimage1.copyRect(40, 20, 0, 32, 4, 12, true, false);
            nativeimage1.copyRect(44, 20, -8, 32, 4, 12, true, false);
            nativeimage1.copyRect(48, 20, -16, 32, 4, 12, true, false);
            nativeimage1.copyRect(52, 20, -8, 32, 4, 12, true, false);
         }

         setNoAlpha(nativeimage, 0, 0, 32, 16);
         if (flag) {
            doNotchTransparencyHack(nativeimage, 32, 0, 64, 32);
         }

         setNoAlpha(nativeimage, 0, 16, 64, 32);
         setNoAlpha(nativeimage, 16, 48, 48, 64);
         return nativeimage;
      } else {
         nativeimage.close();
         LOGGER.warn("Discarding incorrectly sized ({}x{}) skin texture from {}", j, i, this.urlString);
         return null;
      }
   }

   private static void doNotchTransparencyHack(NativeImage nativeimage, int i, int j, int k, int l) {
      for(int i1 = i; i1 < k; ++i1) {
         for(int j1 = j; j1 < l; ++j1) {
            int k1 = nativeimage.getPixelRGBA(i1, j1);
            if ((k1 >> 24 & 255) < 128) {
               return;
            }
         }
      }

      for(int l1 = i; l1 < k; ++l1) {
         for(int i2 = j; i2 < l; ++i2) {
            nativeimage.setPixelRGBA(l1, i2, nativeimage.getPixelRGBA(l1, i2) & 16777215);
         }
      }

   }

   private static void setNoAlpha(NativeImage nativeimage, int i, int j, int k, int l) {
      for(int i1 = i; i1 < k; ++i1) {
         for(int j1 = j; j1 < l; ++j1) {
            nativeimage.setPixelRGBA(i1, j1, nativeimage.getPixelRGBA(i1, j1) | -16777216);
         }
      }

   }
}
