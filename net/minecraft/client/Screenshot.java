package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class Screenshot {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String SCREENSHOT_DIR = "screenshots";
   private int rowHeight;
   private final DataOutputStream outputStream;
   private final byte[] bytes;
   private final int width;
   private final int height;
   private File file;

   public static void grab(File file, RenderTarget rendertarget, Consumer<Component> consumer) {
      grab(file, (String)null, rendertarget, consumer);
   }

   public static void grab(File file, @Nullable String s, RenderTarget rendertarget, Consumer<Component> consumer) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> _grab(file, s, rendertarget, consumer));
      } else {
         _grab(file, s, rendertarget, consumer);
      }

   }

   private static void _grab(File file, @Nullable String s, RenderTarget rendertarget, Consumer<Component> consumer) {
      NativeImage nativeimage = takeScreenshot(rendertarget);
      File file1 = new File(file, "screenshots");
      file1.mkdir();
      File file2;
      if (s == null) {
         file2 = getFile(file1);
      } else {
         file2 = new File(file1, s);
      }

      Util.ioPool().execute(() -> {
         try {
            nativeimage.writeToFile(file2);
            Component component = Component.literal(file2.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));
            consumer.accept(Component.translatable("screenshot.success", component));
         } catch (Exception var7) {
            LOGGER.warn("Couldn't save screenshot", (Throwable)var7);
            consumer.accept(Component.translatable("screenshot.failure", var7.getMessage()));
         } finally {
            nativeimage.close();
         }

      });
   }

   public static NativeImage takeScreenshot(RenderTarget rendertarget) {
      int i = rendertarget.width;
      int j = rendertarget.height;
      NativeImage nativeimage = new NativeImage(i, j, false);
      RenderSystem.bindTexture(rendertarget.getColorTextureId());
      nativeimage.downloadTexture(0, true);
      nativeimage.flipY();
      return nativeimage;
   }

   private static File getFile(File file) {
      String s = Util.getFilenameFormattedDateTime();
      int i = 1;

      while(true) {
         File file1 = new File(file, s + (i == 1 ? "" : "_" + i) + ".png");
         if (!file1.exists()) {
            return file1;
         }

         ++i;
      }
   }

   public Screenshot(File file, int i, int j, int k) throws IOException {
      this.width = i;
      this.height = j;
      this.rowHeight = k;
      File file1 = new File(file, "screenshots");
      file1.mkdir();
      String s = "huge_" + Util.getFilenameFormattedDateTime();

      for(int l = 1; (this.file = new File(file1, s + (l == 1 ? "" : "_" + l) + ".tga")).exists(); ++l) {
      }

      byte[] abyte = new byte[18];
      abyte[2] = 2;
      abyte[12] = (byte)(i % 256);
      abyte[13] = (byte)(i / 256);
      abyte[14] = (byte)(j % 256);
      abyte[15] = (byte)(j / 256);
      abyte[16] = 24;
      this.bytes = new byte[i * k * 3];
      this.outputStream = new DataOutputStream(new FileOutputStream(this.file));
      this.outputStream.write(abyte);
   }

   public void addRegion(ByteBuffer bytebuffer, int i, int j, int k, int l) {
      int i1 = k;
      int j1 = l;
      if (k > this.width - i) {
         i1 = this.width - i;
      }

      if (l > this.height - j) {
         j1 = this.height - j;
      }

      this.rowHeight = j1;

      for(int k1 = 0; k1 < j1; ++k1) {
         bytebuffer.position((l - j1) * k * 3 + k1 * k * 3);
         int l1 = (i + k1 * this.width) * 3;
         bytebuffer.get(this.bytes, l1, i1 * 3);
      }

   }

   public void saveRow() throws IOException {
      this.outputStream.write(this.bytes, 0, this.width * 3 * this.rowHeight);
   }

   public File close() throws IOException {
      this.outputStream.close();
      return this.file;
   }
}
