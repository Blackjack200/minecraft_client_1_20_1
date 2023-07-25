package net.minecraft.client.renderer.texture;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class SpriteContents implements Stitcher.Entry, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation name;
   final int width;
   final int height;
   private final NativeImage originalImage;
   NativeImage[] byMipLevel;
   @Nullable
   private final SpriteContents.AnimatedTexture animatedTexture;

   public SpriteContents(ResourceLocation resourcelocation, FrameSize framesize, NativeImage nativeimage, AnimationMetadataSection animationmetadatasection) {
      this.name = resourcelocation;
      this.width = framesize.width();
      this.height = framesize.height();
      this.animatedTexture = this.createAnimatedTexture(framesize, nativeimage.getWidth(), nativeimage.getHeight(), animationmetadatasection);
      this.originalImage = nativeimage;
      this.byMipLevel = new NativeImage[]{this.originalImage};
   }

   public void increaseMipLevel(int i) {
      try {
         this.byMipLevel = MipmapGenerator.generateMipLevels(this.byMipLevel, i);
      } catch (Throwable var6) {
         CrashReport crashreport = CrashReport.forThrowable(var6, "Generating mipmaps for frame");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Sprite being mipmapped");
         crashreportcategory.setDetail("First frame", () -> {
            StringBuilder stringbuilder = new StringBuilder();
            if (stringbuilder.length() > 0) {
               stringbuilder.append(", ");
            }

            stringbuilder.append(this.originalImage.getWidth()).append("x").append(this.originalImage.getHeight());
            return stringbuilder.toString();
         });
         CrashReportCategory crashreportcategory1 = crashreport.addCategory("Frame being iterated");
         crashreportcategory1.setDetail("Sprite name", this.name);
         crashreportcategory1.setDetail("Sprite size", () -> this.width + " x " + this.height);
         crashreportcategory1.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
         crashreportcategory1.setDetail("Mipmap levels", i);
         throw new ReportedException(crashreport);
      }
   }

   private int getFrameCount() {
      return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
   }

   @Nullable
   private SpriteContents.AnimatedTexture createAnimatedTexture(FrameSize framesize, int i, int j, AnimationMetadataSection animationmetadatasection) {
      int k = i / framesize.width();
      int l = j / framesize.height();
      int i1 = k * l;
      List<SpriteContents.FrameInfo> list = new ArrayList<>();
      animationmetadatasection.forEachFrame((i2, j2) -> list.add(new SpriteContents.FrameInfo(i2, j2)));
      if (list.isEmpty()) {
         for(int j1 = 0; j1 < i1; ++j1) {
            list.add(new SpriteContents.FrameInfo(j1, animationmetadatasection.getDefaultFrameTime()));
         }
      } else {
         int k1 = 0;
         IntSet intset = new IntOpenHashSet();

         for(Iterator<SpriteContents.FrameInfo> iterator = list.iterator(); iterator.hasNext(); ++k1) {
            SpriteContents.FrameInfo spritecontents_frameinfo = iterator.next();
            boolean flag = true;
            if (spritecontents_frameinfo.time <= 0) {
               LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, k1, spritecontents_frameinfo.time);
               flag = false;
            }

            if (spritecontents_frameinfo.index < 0 || spritecontents_frameinfo.index >= i1) {
               LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, k1, spritecontents_frameinfo.index);
               flag = false;
            }

            if (flag) {
               intset.add(spritecontents_frameinfo.index);
            } else {
               iterator.remove();
            }
         }

         int[] aint = IntStream.range(0, i1).filter((l1) -> !intset.contains(l1)).toArray();
         if (aint.length > 0) {
            LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(aint));
         }
      }

      return list.size() <= 1 ? null : new SpriteContents.AnimatedTexture(ImmutableList.copyOf(list), k, animationmetadatasection.isInterpolatedFrames());
   }

   void upload(int i, int j, int k, int l, NativeImage[] anativeimage) {
      for(int i1 = 0; i1 < this.byMipLevel.length; ++i1) {
         anativeimage[i1].upload(i1, i >> i1, j >> i1, k >> i1, l >> i1, this.width >> i1, this.height >> i1, this.byMipLevel.length > 1, false);
      }

   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   public ResourceLocation name() {
      return this.name;
   }

   public IntStream getUniqueFrames() {
      return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
   }

   @Nullable
   public SpriteTicker createTicker() {
      return this.animatedTexture != null ? this.animatedTexture.createTicker() : null;
   }

   public void close() {
      for(NativeImage nativeimage : this.byMipLevel) {
         nativeimage.close();
      }

   }

   public String toString() {
      return "SpriteContents{name=" + this.name + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
   }

   public boolean isTransparent(int i, int j, int k) {
      int l = j;
      int i1 = k;
      if (this.animatedTexture != null) {
         l = j + this.animatedTexture.getFrameX(i) * this.width;
         i1 = k + this.animatedTexture.getFrameY(i) * this.height;
      }

      return (this.originalImage.getPixelRGBA(l, i1) >> 24 & 255) == 0;
   }

   public void uploadFirstFrame(int i, int j) {
      if (this.animatedTexture != null) {
         this.animatedTexture.uploadFirstFrame(i, j);
      } else {
         this.upload(i, j, 0, 0, this.byMipLevel);
      }

   }

   class AnimatedTexture {
      final List<SpriteContents.FrameInfo> frames;
      private final int frameRowSize;
      private final boolean interpolateFrames;

      AnimatedTexture(List<SpriteContents.FrameInfo> list, int i, boolean flag) {
         this.frames = list;
         this.frameRowSize = i;
         this.interpolateFrames = flag;
      }

      int getFrameX(int i) {
         return i % this.frameRowSize;
      }

      int getFrameY(int i) {
         return i / this.frameRowSize;
      }

      void uploadFrame(int i, int j, int k) {
         int l = this.getFrameX(k) * SpriteContents.this.width;
         int i1 = this.getFrameY(k) * SpriteContents.this.height;
         SpriteContents.this.upload(i, j, l, i1, SpriteContents.this.byMipLevel);
      }

      public SpriteTicker createTicker() {
         return SpriteContents.this.new Ticker(this, this.interpolateFrames ? SpriteContents.this.new InterpolationData() : null);
      }

      public void uploadFirstFrame(int i, int j) {
         this.uploadFrame(i, j, (this.frames.get(0)).index);
      }

      public IntStream getUniqueFrames() {
         return this.frames.stream().mapToInt((spritecontents_frameinfo) -> spritecontents_frameinfo.index).distinct();
      }
   }

   static class FrameInfo {
      final int index;
      final int time;

      FrameInfo(int i, int j) {
         this.index = i;
         this.time = j;
      }
   }

   final class InterpolationData implements AutoCloseable {
      private final NativeImage[] activeFrame = new NativeImage[SpriteContents.this.byMipLevel.length];

      InterpolationData() {
         for(int i = 0; i < this.activeFrame.length; ++i) {
            int j = SpriteContents.this.width >> i;
            int k = SpriteContents.this.height >> i;
            this.activeFrame[i] = new NativeImage(j, k, false);
         }

      }

      void uploadInterpolatedFrame(int i, int j, SpriteContents.Ticker spritecontents_ticker) {
         SpriteContents.AnimatedTexture spritecontents_animatedtexture = spritecontents_ticker.animationInfo;
         List<SpriteContents.FrameInfo> list = spritecontents_animatedtexture.frames;
         SpriteContents.FrameInfo spritecontents_frameinfo = list.get(spritecontents_ticker.frame);
         double d0 = 1.0D - (double)spritecontents_ticker.subFrame / (double)spritecontents_frameinfo.time;
         int k = spritecontents_frameinfo.index;
         int l = (list.get((spritecontents_ticker.frame + 1) % list.size())).index;
         if (k != l) {
            for(int i1 = 0; i1 < this.activeFrame.length; ++i1) {
               int j1 = SpriteContents.this.width >> i1;
               int k1 = SpriteContents.this.height >> i1;

               for(int l1 = 0; l1 < k1; ++l1) {
                  for(int i2 = 0; i2 < j1; ++i2) {
                     int j2 = this.getPixel(spritecontents_animatedtexture, k, i1, i2, l1);
                     int k2 = this.getPixel(spritecontents_animatedtexture, l, i1, i2, l1);
                     int l2 = this.mix(d0, j2 >> 16 & 255, k2 >> 16 & 255);
                     int i3 = this.mix(d0, j2 >> 8 & 255, k2 >> 8 & 255);
                     int j3 = this.mix(d0, j2 & 255, k2 & 255);
                     this.activeFrame[i1].setPixelRGBA(i2, l1, j2 & -16777216 | l2 << 16 | i3 << 8 | j3);
                  }
               }
            }

            SpriteContents.this.upload(i, j, 0, 0, this.activeFrame);
         }

      }

      private int getPixel(SpriteContents.AnimatedTexture spritecontents_animatedtexture, int i, int j, int k, int l) {
         return SpriteContents.this.byMipLevel[j].getPixelRGBA(k + (spritecontents_animatedtexture.getFrameX(i) * SpriteContents.this.width >> j), l + (spritecontents_animatedtexture.getFrameY(i) * SpriteContents.this.height >> j));
      }

      private int mix(double d0, int i, int j) {
         return (int)(d0 * (double)i + (1.0D - d0) * (double)j);
      }

      public void close() {
         for(NativeImage nativeimage : this.activeFrame) {
            nativeimage.close();
         }

      }
   }

   class Ticker implements SpriteTicker {
      int frame;
      int subFrame;
      final SpriteContents.AnimatedTexture animationInfo;
      @Nullable
      private final SpriteContents.InterpolationData interpolationData;

      Ticker(SpriteContents.AnimatedTexture spritecontents_animatedtexture, @Nullable SpriteContents.InterpolationData spritecontents_interpolationdata) {
         this.animationInfo = spritecontents_animatedtexture;
         this.interpolationData = spritecontents_interpolationdata;
      }

      public void tickAndUpload(int i, int j) {
         ++this.subFrame;
         SpriteContents.FrameInfo spritecontents_frameinfo = this.animationInfo.frames.get(this.frame);
         if (this.subFrame >= spritecontents_frameinfo.time) {
            int k = spritecontents_frameinfo.index;
            this.frame = (this.frame + 1) % this.animationInfo.frames.size();
            this.subFrame = 0;
            int l = (this.animationInfo.frames.get(this.frame)).index;
            if (k != l) {
               this.animationInfo.uploadFrame(i, j, l);
            }
         } else if (this.interpolationData != null) {
            if (!RenderSystem.isOnRenderThread()) {
               RenderSystem.recordRenderCall(() -> this.interpolationData.uploadInterpolatedFrame(i, j, this));
            } else {
               this.interpolationData.uploadInterpolatedFrame(i, j, this);
            }
         }

      }

      public void close() {
         if (this.interpolationData != null) {
            this.interpolationData.close();
         }

      }
   }
}
