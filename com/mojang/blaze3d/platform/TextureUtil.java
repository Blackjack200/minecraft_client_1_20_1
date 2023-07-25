package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@DontObfuscate
public class TextureUtil {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final int MIN_MIPMAP_LEVEL = 0;
   private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

   public static int generateTextureId() {
      RenderSystem.assertOnRenderThreadOrInit();
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         int[] aint = new int[ThreadLocalRandom.current().nextInt(15) + 1];
         GlStateManager._genTextures(aint);
         int i = GlStateManager._genTexture();
         GlStateManager._deleteTextures(aint);
         return i;
      } else {
         return GlStateManager._genTexture();
      }
   }

   public static void releaseTextureId(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._deleteTexture(i);
   }

   public static void prepareImage(int i, int j, int k) {
      prepareImage(NativeImage.InternalGlFormat.RGBA, i, 0, j, k);
   }

   public static void prepareImage(NativeImage.InternalGlFormat nativeimage_internalglformat, int i, int j, int k) {
      prepareImage(nativeimage_internalglformat, i, 0, j, k);
   }

   public static void prepareImage(int i, int j, int k, int l) {
      prepareImage(NativeImage.InternalGlFormat.RGBA, i, j, k, l);
   }

   public static void prepareImage(NativeImage.InternalGlFormat nativeimage_internalglformat, int i, int j, int k, int l) {
      RenderSystem.assertOnRenderThreadOrInit();
      bind(i);
      if (j >= 0) {
         GlStateManager._texParameter(3553, 33085, j);
         GlStateManager._texParameter(3553, 33082, 0);
         GlStateManager._texParameter(3553, 33083, j);
         GlStateManager._texParameter(3553, 34049, 0.0F);
      }

      for(int i1 = 0; i1 <= j; ++i1) {
         GlStateManager._texImage2D(3553, i1, nativeimage_internalglformat.glFormat(), k >> i1, l >> i1, 0, 6408, 5121, (IntBuffer)null);
      }

   }

   private static void bind(int i) {
      RenderSystem.assertOnRenderThreadOrInit();
      GlStateManager._bindTexture(i);
   }

   public static ByteBuffer readResource(InputStream inputstream) throws IOException {
      ReadableByteChannel readablebytechannel = Channels.newChannel(inputstream);
      if (readablebytechannel instanceof SeekableByteChannel seekablebytechannel) {
         return readResource(readablebytechannel, (int)seekablebytechannel.size() + 1);
      } else {
         return readResource(readablebytechannel, 8192);
      }
   }

   private static ByteBuffer readResource(ReadableByteChannel readablebytechannel, int i) throws IOException {
      ByteBuffer bytebuffer = MemoryUtil.memAlloc(i);

      try {
         while(readablebytechannel.read(bytebuffer) != -1) {
            if (!bytebuffer.hasRemaining()) {
               bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
            }
         }

         return bytebuffer;
      } catch (IOException var4) {
         MemoryUtil.memFree(bytebuffer);
         throw var4;
      }
   }

   public static void writeAsPNG(Path path, String s, int i, int j, int k, int l) {
      writeAsPNG(path, s, i, j, k, l, (IntUnaryOperator)null);
   }

   public static void writeAsPNG(Path path, String s, int i, int j, int k, int l, @Nullable IntUnaryOperator intunaryoperator) {
      RenderSystem.assertOnRenderThread();
      bind(i);

      for(int i1 = 0; i1 <= j; ++i1) {
         int j1 = k >> i1;
         int k1 = l >> i1;

         try {
            NativeImage nativeimage = new NativeImage(j1, k1, false);

            try {
               nativeimage.downloadTexture(i1, false);
               if (intunaryoperator != null) {
                  nativeimage.applyToAllPixels(intunaryoperator);
               }

               Path path1 = path.resolve(s + "_" + i1 + ".png");
               nativeimage.writeToFile(path1);
               LOGGER.debug("Exported png to: {}", (Object)path1.toAbsolutePath());
            } catch (Throwable var14) {
               try {
                  nativeimage.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }

               throw var14;
            }

            nativeimage.close();
         } catch (IOException var15) {
            LOGGER.debug("Unable to write: ", (Throwable)var15);
         }
      }

   }

   public static Path getDebugTexturePath(Path path) {
      return path.resolve("screenshots").resolve("debug");
   }

   public static Path getDebugTexturePath() {
      return getDebugTexturePath(Path.of("."));
   }
}
