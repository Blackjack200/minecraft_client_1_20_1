package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.FastColor;
import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public final class NativeImage implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
   private final NativeImage.Format format;
   private final int width;
   private final int height;
   private final boolean useStbFree;
   private long pixels;
   private final long size;

   public NativeImage(int i, int j, boolean flag) {
      this(NativeImage.Format.RGBA, i, j, flag);
   }

   public NativeImage(NativeImage.Format nativeimage_format, int i, int j, boolean flag) {
      if (i > 0 && j > 0) {
         this.format = nativeimage_format;
         this.width = i;
         this.height = j;
         this.size = (long)i * (long)j * (long)nativeimage_format.components();
         this.useStbFree = false;
         if (flag) {
            this.pixels = MemoryUtil.nmemCalloc(1L, this.size);
         } else {
            this.pixels = MemoryUtil.nmemAlloc(this.size);
         }

      } else {
         throw new IllegalArgumentException("Invalid texture size: " + i + "x" + j);
      }
   }

   private NativeImage(NativeImage.Format nativeimage_format, int i, int j, boolean flag, long k) {
      if (i > 0 && j > 0) {
         this.format = nativeimage_format;
         this.width = i;
         this.height = j;
         this.useStbFree = flag;
         this.pixels = k;
         this.size = (long)i * (long)j * (long)nativeimage_format.components();
      } else {
         throw new IllegalArgumentException("Invalid texture size: " + i + "x" + j);
      }
   }

   public String toString() {
      return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
   }

   private boolean isOutsideBounds(int i, int j) {
      return i < 0 || i >= this.width || j < 0 || j >= this.height;
   }

   public static NativeImage read(InputStream inputstream) throws IOException {
      return read(NativeImage.Format.RGBA, inputstream);
   }

   public static NativeImage read(@Nullable NativeImage.Format nativeimage_format, InputStream inputstream) throws IOException {
      ByteBuffer bytebuffer = null;

      NativeImage var3;
      try {
         bytebuffer = TextureUtil.readResource(inputstream);
         bytebuffer.rewind();
         var3 = read(nativeimage_format, bytebuffer);
      } finally {
         MemoryUtil.memFree(bytebuffer);
         IOUtils.closeQuietly(inputstream);
      }

      return var3;
   }

   public static NativeImage read(ByteBuffer bytebuffer) throws IOException {
      return read(NativeImage.Format.RGBA, bytebuffer);
   }

   public static NativeImage read(byte[] abyte) throws IOException {
      MemoryStack memorystack = MemoryStack.stackPush();

      NativeImage var3;
      try {
         ByteBuffer bytebuffer = memorystack.malloc(abyte.length);
         bytebuffer.put(abyte);
         bytebuffer.rewind();
         var3 = read(bytebuffer);
      } catch (Throwable var5) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }
         }

         throw var5;
      }

      if (memorystack != null) {
         memorystack.close();
      }

      return var3;
   }

   public static NativeImage read(@Nullable NativeImage.Format nativeimage_format, ByteBuffer bytebuffer) throws IOException {
      if (nativeimage_format != null && !nativeimage_format.supportedByStb()) {
         throw new UnsupportedOperationException("Don't know how to read format " + nativeimage_format);
      } else if (MemoryUtil.memAddress(bytebuffer) == 0L) {
         throw new IllegalArgumentException("Invalid buffer");
      } else {
         MemoryStack memorystack = MemoryStack.stackPush();

         NativeImage var7;
         try {
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);
            IntBuffer intbuffer2 = memorystack.mallocInt(1);
            ByteBuffer bytebuffer1 = STBImage.stbi_load_from_memory(bytebuffer, intbuffer, intbuffer1, intbuffer2, nativeimage_format == null ? 0 : nativeimage_format.components);
            if (bytebuffer1 == null) {
               throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }

            var7 = new NativeImage(nativeimage_format == null ? NativeImage.Format.getStbFormat(intbuffer2.get(0)) : nativeimage_format, intbuffer.get(0), intbuffer1.get(0), true, MemoryUtil.memAddress(bytebuffer1));
         } catch (Throwable var9) {
            if (memorystack != null) {
               try {
                  memorystack.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }
            }

            throw var9;
         }

         if (memorystack != null) {
            memorystack.close();
         }

         return var7;
      }
   }

   private static void setFilter(boolean flag, boolean flag1) {
      RenderSystem.assertOnRenderThreadOrInit();
      if (flag) {
         GlStateManager._texParameter(3553, 10241, flag1 ? 9987 : 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
      } else {
         GlStateManager._texParameter(3553, 10241, flag1 ? 9986 : 9728);
         GlStateManager._texParameter(3553, 10240, 9728);
      }

   }

   private void checkAllocated() {
      if (this.pixels == 0L) {
         throw new IllegalStateException("Image is not allocated.");
      }
   }

   public void close() {
      if (this.pixels != 0L) {
         if (this.useStbFree) {
            STBImage.nstbi_image_free(this.pixels);
         } else {
            MemoryUtil.nmemFree(this.pixels);
         }
      }

      this.pixels = 0L;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public NativeImage.Format format() {
      return this.format;
   }

   public int getPixelRGBA(int i, int j) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         this.checkAllocated();
         long k = ((long)i + (long)j * (long)this.width) * 4L;
         return MemoryUtil.memGetInt(this.pixels + k);
      }
   }

   public void setPixelRGBA(int i, int j, int k) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         this.checkAllocated();
         long l = ((long)i + (long)j * (long)this.width) * 4L;
         MemoryUtil.memPutInt(this.pixels + l, k);
      }
   }

   public NativeImage mappedCopy(IntUnaryOperator intunaryoperator) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         NativeImage nativeimage = new NativeImage(this.width, this.height, false);
         int i = this.width * this.height;
         IntBuffer intbuffer = MemoryUtil.memIntBuffer(this.pixels, i);
         IntBuffer intbuffer1 = MemoryUtil.memIntBuffer(nativeimage.pixels, i);

         for(int j = 0; j < i; ++j) {
            intbuffer1.put(j, intunaryoperator.applyAsInt(intbuffer.get(j)));
         }

         return nativeimage;
      }
   }

   public void applyToAllPixels(IntUnaryOperator intunaryoperator) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         int i = this.width * this.height;
         IntBuffer intbuffer = MemoryUtil.memIntBuffer(this.pixels, i);

         for(int j = 0; j < i; ++j) {
            intbuffer.put(j, intunaryoperator.applyAsInt(intbuffer.get(j)));
         }

      }
   }

   public int[] getPixelsRGBA() {
      if (this.format != NativeImage.Format.RGBA) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelsRGBA only works on RGBA images; have %s", this.format));
      } else {
         this.checkAllocated();
         int[] aint = new int[this.width * this.height];
         MemoryUtil.memIntBuffer(this.pixels, this.width * this.height).get(aint);
         return aint;
      }
   }

   public void setPixelLuminance(int i, int j, byte b0) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminance()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelLuminance only works on image with luminance; have %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         this.checkAllocated();
         long k = ((long)i + (long)j * (long)this.width) * (long)this.format.components() + (long)(this.format.luminanceOffset() / 8);
         MemoryUtil.memPutByte(this.pixels + k, b0);
      }
   }

   public byte getRedOrLuminance(int i, int j) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminanceOrRed()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no red or luminance in %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrRedOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)k);
      }
   }

   public byte getGreenOrLuminance(int i, int j) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminanceOrGreen()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no green or luminance in %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrGreenOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)k);
      }
   }

   public byte getBlueOrLuminance(int i, int j) {
      RenderSystem.assertOnRenderThread();
      if (!this.format.hasLuminanceOrBlue()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no blue or luminance in %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrBlueOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)k);
      }
   }

   public byte getLuminanceOrAlpha(int i, int j) {
      if (!this.format.hasLuminanceOrAlpha()) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", this.format));
      } else if (this.isOutsideBounds(i, j)) {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", i, j, this.width, this.height));
      } else {
         int k = (i + j * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)k);
      }
   }

   public void blendPixel(int i, int j, int k) {
      if (this.format != NativeImage.Format.RGBA) {
         throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
      } else {
         int l = this.getPixelRGBA(i, j);
         float f = (float)FastColor.ABGR32.alpha(k) / 255.0F;
         float f1 = (float)FastColor.ABGR32.blue(k) / 255.0F;
         float f2 = (float)FastColor.ABGR32.green(k) / 255.0F;
         float f3 = (float)FastColor.ABGR32.red(k) / 255.0F;
         float f4 = (float)FastColor.ABGR32.alpha(l) / 255.0F;
         float f5 = (float)FastColor.ABGR32.blue(l) / 255.0F;
         float f6 = (float)FastColor.ABGR32.green(l) / 255.0F;
         float f7 = (float)FastColor.ABGR32.red(l) / 255.0F;
         float f9 = 1.0F - f;
         float f10 = f * f + f4 * f9;
         float f11 = f1 * f + f5 * f9;
         float f12 = f2 * f + f6 * f9;
         float f13 = f3 * f + f7 * f9;
         if (f10 > 1.0F) {
            f10 = 1.0F;
         }

         if (f11 > 1.0F) {
            f11 = 1.0F;
         }

         if (f12 > 1.0F) {
            f12 = 1.0F;
         }

         if (f13 > 1.0F) {
            f13 = 1.0F;
         }

         int i1 = (int)(f10 * 255.0F);
         int j1 = (int)(f11 * 255.0F);
         int k1 = (int)(f12 * 255.0F);
         int l1 = (int)(f13 * 255.0F);
         this.setPixelRGBA(i, j, FastColor.ABGR32.color(i1, j1, k1, l1));
      }
   }

   /** @deprecated */
   @Deprecated
   public int[] makePixelArray() {
      if (this.format != NativeImage.Format.RGBA) {
         throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
      } else {
         this.checkAllocated();
         int[] aint = new int[this.getWidth() * this.getHeight()];

         for(int i = 0; i < this.getHeight(); ++i) {
            for(int j = 0; j < this.getWidth(); ++j) {
               int k = this.getPixelRGBA(j, i);
               aint[j + i * this.getWidth()] = FastColor.ARGB32.color(FastColor.ABGR32.alpha(k), FastColor.ABGR32.red(k), FastColor.ABGR32.green(k), FastColor.ABGR32.blue(k));
            }
         }

         return aint;
      }
   }

   public void upload(int i, int j, int k, boolean flag) {
      this.upload(i, j, k, 0, 0, this.width, this.height, false, flag);
   }

   public void upload(int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, boolean flag1) {
      this.upload(i, j, k, l, i1, j1, k1, false, false, flag, flag1);
   }

   public void upload(int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> this._upload(i, j, k, l, i1, j1, k1, flag, flag1, flag2, flag3));
      } else {
         this._upload(i, j, k, l, i1, j1, k1, flag, flag1, flag2, flag3);
      }

   }

   private void _upload(int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      try {
         RenderSystem.assertOnRenderThreadOrInit();
         this.checkAllocated();
         setFilter(flag, flag2);
         if (j1 == this.getWidth()) {
            GlStateManager._pixelStore(3314, 0);
         } else {
            GlStateManager._pixelStore(3314, this.getWidth());
         }

         GlStateManager._pixelStore(3316, l);
         GlStateManager._pixelStore(3315, i1);
         this.format.setUnpackPixelStoreState();
         GlStateManager._texSubImage2D(3553, i, j, k, j1, k1, this.format.glFormat(), 5121, this.pixels);
         if (flag1) {
            GlStateManager._texParameter(3553, 10242, 33071);
            GlStateManager._texParameter(3553, 10243, 33071);
         }
      } finally {
         if (flag3) {
            this.close();
         }

      }

   }

   public void downloadTexture(int i, boolean flag) {
      RenderSystem.assertOnRenderThread();
      this.checkAllocated();
      this.format.setPackPixelStoreState();
      GlStateManager._getTexImage(3553, i, this.format.glFormat(), 5121, this.pixels);
      if (flag && this.format.hasAlpha()) {
         for(int j = 0; j < this.getHeight(); ++j) {
            for(int k = 0; k < this.getWidth(); ++k) {
               this.setPixelRGBA(k, j, this.getPixelRGBA(k, j) | 255 << this.format.alphaOffset());
            }
         }
      }

   }

   public void downloadDepthBuffer(float f) {
      RenderSystem.assertOnRenderThread();
      if (this.format.components() != 1) {
         throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
      } else {
         this.checkAllocated();
         this.format.setPackPixelStoreState();
         GlStateManager._readPixels(0, 0, this.width, this.height, 6402, 5121, this.pixels);
      }
   }

   public void drawPixels() {
      RenderSystem.assertOnRenderThread();
      this.format.setUnpackPixelStoreState();
      GlStateManager._glDrawPixels(this.width, this.height, this.format.glFormat(), 5121, this.pixels);
   }

   public void writeToFile(File file) throws IOException {
      this.writeToFile(file.toPath());
   }

   public void copyFromFont(STBTTFontinfo stbttfontinfo, int i, int j, int k, float f, float f1, float f2, float f3, int l, int i1) {
      if (l >= 0 && l + j <= this.getWidth() && i1 >= 0 && i1 + k <= this.getHeight()) {
         if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
         } else {
            STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(stbttfontinfo.address(), this.pixels + (long)l + (long)(i1 * this.getWidth()), j, k, this.getWidth(), f, f1, f2, f3, i);
         }
      } else {
         throw new IllegalArgumentException(String.format(Locale.ROOT, "Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", l, i1, j, k, this.getWidth(), this.getHeight()));
      }
   }

   public void writeToFile(Path path) throws IOException {
      if (!this.format.supportedByStb()) {
         throw new UnsupportedOperationException("Don't know how to write format " + this.format);
      } else {
         this.checkAllocated();
         WritableByteChannel writablebytechannel = Files.newByteChannel(path, OPEN_OPTIONS);

         try {
            if (!this.writeToChannel(writablebytechannel)) {
               throw new IOException("Could not write image to the PNG file \"" + path.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
         } catch (Throwable var6) {
            if (writablebytechannel != null) {
               try {
                  writablebytechannel.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (writablebytechannel != null) {
            writablebytechannel.close();
         }

      }
   }

   public byte[] asByteArray() throws IOException {
      ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();

      byte[] var3;
      try {
         WritableByteChannel writablebytechannel = Channels.newChannel(bytearrayoutputstream);

         try {
            if (!this.writeToChannel(writablebytechannel)) {
               throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
            }

            var3 = bytearrayoutputstream.toByteArray();
         } catch (Throwable var7) {
            if (writablebytechannel != null) {
               try {
                  writablebytechannel.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (writablebytechannel != null) {
            writablebytechannel.close();
         }
      } catch (Throwable var8) {
         try {
            bytearrayoutputstream.close();
         } catch (Throwable var5) {
            var8.addSuppressed(var5);
         }

         throw var8;
      }

      bytearrayoutputstream.close();
      return var3;
   }

   private boolean writeToChannel(WritableByteChannel writablebytechannel) throws IOException {
      NativeImage.WriteCallback nativeimage_writecallback = new NativeImage.WriteCallback(writablebytechannel);

      boolean var4;
      try {
         int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
         if (i < this.getHeight()) {
            LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.getHeight(), i);
         }

         if (STBImageWrite.nstbi_write_png_to_func(nativeimage_writecallback.address(), 0L, this.getWidth(), i, this.format.components(), this.pixels, 0) != 0) {
            nativeimage_writecallback.throwIfException();
            return true;
         }

         var4 = false;
      } finally {
         nativeimage_writecallback.free();
      }

      return var4;
   }

   public void copyFrom(NativeImage nativeimage) {
      if (nativeimage.format() != this.format) {
         throw new UnsupportedOperationException("Image formats don't match.");
      } else {
         int i = this.format.components();
         this.checkAllocated();
         nativeimage.checkAllocated();
         if (this.width == nativeimage.width) {
            MemoryUtil.memCopy(nativeimage.pixels, this.pixels, Math.min(this.size, nativeimage.size));
         } else {
            int j = Math.min(this.getWidth(), nativeimage.getWidth());
            int k = Math.min(this.getHeight(), nativeimage.getHeight());

            for(int l = 0; l < k; ++l) {
               int i1 = l * nativeimage.getWidth() * i;
               int j1 = l * this.getWidth() * i;
               MemoryUtil.memCopy(nativeimage.pixels + (long)i1, this.pixels + (long)j1, (long)j);
            }
         }

      }
   }

   public void fillRect(int i, int j, int k, int l, int i1) {
      for(int j1 = j; j1 < j + l; ++j1) {
         for(int k1 = i; k1 < i + k; ++k1) {
            this.setPixelRGBA(k1, j1, i1);
         }
      }

   }

   public void copyRect(int i, int j, int k, int l, int i1, int j1, boolean flag, boolean flag1) {
      this.copyRect(this, i, j, i + k, j + l, i1, j1, flag, flag1);
   }

   public void copyRect(NativeImage nativeimage, int i, int j, int k, int l, int i1, int j1, boolean flag, boolean flag1) {
      for(int k1 = 0; k1 < j1; ++k1) {
         for(int l1 = 0; l1 < i1; ++l1) {
            int i2 = flag ? i1 - 1 - l1 : l1;
            int j2 = flag1 ? j1 - 1 - k1 : k1;
            int k2 = this.getPixelRGBA(i + l1, j + k1);
            nativeimage.setPixelRGBA(k + i2, l + j2, k2);
         }
      }

   }

   public void flipY() {
      this.checkAllocated();
      MemoryStack memorystack = MemoryStack.stackPush();

      try {
         int i = this.format.components();
         int j = this.getWidth() * i;
         long k = memorystack.nmalloc(j);

         for(int l = 0; l < this.getHeight() / 2; ++l) {
            int i1 = l * this.getWidth() * i;
            int j1 = (this.getHeight() - 1 - l) * this.getWidth() * i;
            MemoryUtil.memCopy(this.pixels + (long)i1, k, (long)j);
            MemoryUtil.memCopy(this.pixels + (long)j1, this.pixels + (long)i1, (long)j);
            MemoryUtil.memCopy(k, this.pixels + (long)j1, (long)j);
         }
      } catch (Throwable var10) {
         if (memorystack != null) {
            try {
               memorystack.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (memorystack != null) {
         memorystack.close();
      }

   }

   public void resizeSubRectTo(int i, int j, int k, int l, NativeImage nativeimage) {
      this.checkAllocated();
      if (nativeimage.format() != this.format) {
         throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
      } else {
         int i1 = this.format.components();
         STBImageResize.nstbir_resize_uint8(this.pixels + (long)((i + j * this.getWidth()) * i1), k, l, this.getWidth() * i1, nativeimage.pixels, nativeimage.getWidth(), nativeimage.getHeight(), 0, i1);
      }
   }

   public void untrack() {
      DebugMemoryUntracker.untrack(this.pixels);
   }

   public static enum Format {
      RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
      RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
      LUMINANCE_ALPHA(2, 33319, false, false, false, true, true, 255, 255, 255, 0, 8, true),
      LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true);

      final int components;
      private final int glFormat;
      private final boolean hasRed;
      private final boolean hasGreen;
      private final boolean hasBlue;
      private final boolean hasLuminance;
      private final boolean hasAlpha;
      private final int redOffset;
      private final int greenOffset;
      private final int blueOffset;
      private final int luminanceOffset;
      private final int alphaOffset;
      private final boolean supportedByStb;

      private Format(int i, int j, boolean flag, boolean flag1, boolean flag2, boolean flag3, boolean flag4, int k, int l, int i1, int j1, int k1, boolean flag5) {
         this.components = i;
         this.glFormat = j;
         this.hasRed = flag;
         this.hasGreen = flag1;
         this.hasBlue = flag2;
         this.hasLuminance = flag3;
         this.hasAlpha = flag4;
         this.redOffset = k;
         this.greenOffset = l;
         this.blueOffset = i1;
         this.luminanceOffset = j1;
         this.alphaOffset = k1;
         this.supportedByStb = flag5;
      }

      public int components() {
         return this.components;
      }

      public void setPackPixelStoreState() {
         RenderSystem.assertOnRenderThread();
         GlStateManager._pixelStore(3333, this.components());
      }

      public void setUnpackPixelStoreState() {
         RenderSystem.assertOnRenderThreadOrInit();
         GlStateManager._pixelStore(3317, this.components());
      }

      public int glFormat() {
         return this.glFormat;
      }

      public boolean hasRed() {
         return this.hasRed;
      }

      public boolean hasGreen() {
         return this.hasGreen;
      }

      public boolean hasBlue() {
         return this.hasBlue;
      }

      public boolean hasLuminance() {
         return this.hasLuminance;
      }

      public boolean hasAlpha() {
         return this.hasAlpha;
      }

      public int redOffset() {
         return this.redOffset;
      }

      public int greenOffset() {
         return this.greenOffset;
      }

      public int blueOffset() {
         return this.blueOffset;
      }

      public int luminanceOffset() {
         return this.luminanceOffset;
      }

      public int alphaOffset() {
         return this.alphaOffset;
      }

      public boolean hasLuminanceOrRed() {
         return this.hasLuminance || this.hasRed;
      }

      public boolean hasLuminanceOrGreen() {
         return this.hasLuminance || this.hasGreen;
      }

      public boolean hasLuminanceOrBlue() {
         return this.hasLuminance || this.hasBlue;
      }

      public boolean hasLuminanceOrAlpha() {
         return this.hasLuminance || this.hasAlpha;
      }

      public int luminanceOrRedOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.redOffset;
      }

      public int luminanceOrGreenOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
      }

      public int luminanceOrBlueOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
      }

      public int luminanceOrAlphaOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
      }

      public boolean supportedByStb() {
         return this.supportedByStb;
      }

      static NativeImage.Format getStbFormat(int i) {
         switch (i) {
            case 1:
               return LUMINANCE;
            case 2:
               return LUMINANCE_ALPHA;
            case 3:
               return RGB;
            case 4:
            default:
               return RGBA;
         }
      }
   }

   public static enum InternalGlFormat {
      RGBA(6408),
      RGB(6407),
      RG(33319),
      RED(6403);

      private final int glFormat;

      private InternalGlFormat(int i) {
         this.glFormat = i;
      }

      public int glFormat() {
         return this.glFormat;
      }
   }

   static class WriteCallback extends STBIWriteCallback {
      private final WritableByteChannel output;
      @Nullable
      private IOException exception;

      WriteCallback(WritableByteChannel writablebytechannel) {
         this.output = writablebytechannel;
      }

      public void invoke(long i, long j, int k) {
         ByteBuffer bytebuffer = getData(j, k);

         try {
            this.output.write(bytebuffer);
         } catch (IOException var8) {
            this.exception = var8;
         }

      }

      public void throwIfException() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         }
      }
   }
}
