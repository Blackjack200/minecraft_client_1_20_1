package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class SpriteLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation location;
   private final int maxSupportedTextureSize;
   private final int minWidth;
   private final int minHeight;

   public SpriteLoader(ResourceLocation resourcelocation, int i, int j, int k) {
      this.location = resourcelocation;
      this.maxSupportedTextureSize = i;
      this.minWidth = j;
      this.minHeight = k;
   }

   public static SpriteLoader create(TextureAtlas textureatlas) {
      return new SpriteLoader(textureatlas.location(), textureatlas.maxSupportedTextureSize(), textureatlas.getWidth(), textureatlas.getHeight());
   }

   public SpriteLoader.Preparations stitch(List<SpriteContents> list, int i, Executor executor) {
      int j = this.maxSupportedTextureSize;
      Stitcher<SpriteContents> stitcher = new Stitcher<>(j, j, i);
      int k = Integer.MAX_VALUE;
      int l = 1 << i;

      for(SpriteContents spritecontents : list) {
         k = Math.min(k, Math.min(spritecontents.width(), spritecontents.height()));
         int i1 = Math.min(Integer.lowestOneBit(spritecontents.width()), Integer.lowestOneBit(spritecontents.height()));
         if (i1 < l) {
            LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", spritecontents.name(), spritecontents.width(), spritecontents.height(), Mth.log2(l), Mth.log2(i1));
            l = i1;
         }

         stitcher.registerSprite(spritecontents);
      }

      int j1 = Math.min(k, l);
      int k1 = Mth.log2(j1);
      int l1;
      if (k1 < i) {
         LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.location, i, k1, j1);
         l1 = k1;
      } else {
         l1 = i;
      }

      try {
         stitcher.stitch();
      } catch (StitcherException var16) {
         CrashReport crashreport = CrashReport.forThrowable(var16, "Stitching");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Stitcher");
         crashreportcategory.setDetail("Sprites", var16.getAllSprites().stream().map((stitcher_entry) -> String.format(Locale.ROOT, "%s[%dx%d]", stitcher_entry.name(), stitcher_entry.width(), stitcher_entry.height())).collect(Collectors.joining(",")));
         crashreportcategory.setDetail("Max Texture Size", j);
         throw new ReportedException(crashreport);
      }

      int j2 = Math.max(stitcher.getWidth(), this.minWidth);
      int k2 = Math.max(stitcher.getHeight(), this.minHeight);
      Map<ResourceLocation, TextureAtlasSprite> map = this.getStitchedSprites(stitcher, j2, k2);
      TextureAtlasSprite textureatlassprite = map.get(MissingTextureAtlasSprite.getLocation());
      CompletableFuture<Void> completablefuture;
      if (l1 > 0) {
         completablefuture = CompletableFuture.runAsync(() -> map.values().forEach((textureatlassprite1) -> textureatlassprite1.contents().increaseMipLevel(l1)), executor);
      } else {
         completablefuture = CompletableFuture.completedFuture((Void)null);
      }

      return new SpriteLoader.Preparations(j2, k2, l1, textureatlassprite, map, completablefuture);
   }

   public static CompletableFuture<List<SpriteContents>> runSpriteSuppliers(List<Supplier<SpriteContents>> list, Executor executor) {
      List<CompletableFuture<SpriteContents>> list1 = list.stream().map((supplier) -> CompletableFuture.supplyAsync(supplier, executor)).toList();
      return Util.sequence(list1).thenApply((list2) -> list2.stream().filter(Objects::nonNull).toList());
   }

   public CompletableFuture<SpriteLoader.Preparations> loadAndStitch(ResourceManager resourcemanager, ResourceLocation resourcelocation, int i, Executor executor) {
      return CompletableFuture.supplyAsync(() -> SpriteResourceLoader.load(resourcemanager, resourcelocation).list(resourcemanager), executor).thenCompose((list1) -> runSpriteSuppliers(list1, executor)).thenApply((list) -> this.stitch(list, i, executor));
   }

   @Nullable
   public static SpriteContents loadSprite(ResourceLocation resourcelocation, Resource resource) {
      AnimationMetadataSection animationmetadatasection;
      try {
         animationmetadatasection = resource.metadata().getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
      } catch (Exception var8) {
         LOGGER.error("Unable to parse metadata from {}", resourcelocation, var8);
         return null;
      }

      NativeImage nativeimage;
      try {
         InputStream inputstream = resource.open();

         try {
            nativeimage = NativeImage.read(inputstream);
         } catch (Throwable var9) {
            if (inputstream != null) {
               try {
                  inputstream.close();
               } catch (Throwable var7) {
                  var9.addSuppressed(var7);
               }
            }

            throw var9;
         }

         if (inputstream != null) {
            inputstream.close();
         }
      } catch (IOException var10) {
         LOGGER.error("Using missing texture, unable to load {}", resourcelocation, var10);
         return null;
      }

      FrameSize framesize = animationmetadatasection.calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight());
      if (Mth.isMultipleOf(nativeimage.getWidth(), framesize.width()) && Mth.isMultipleOf(nativeimage.getHeight(), framesize.height())) {
         return new SpriteContents(resourcelocation, framesize, nativeimage, animationmetadatasection);
      } else {
         LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", resourcelocation, nativeimage.getWidth(), nativeimage.getHeight(), framesize.width(), framesize.height());
         nativeimage.close();
         return null;
      }
   }

   private Map<ResourceLocation, TextureAtlasSprite> getStitchedSprites(Stitcher<SpriteContents> stitcher, int i, int j) {
      Map<ResourceLocation, TextureAtlasSprite> map = new HashMap<>();
      stitcher.gatherSprites((spritecontents, i1, j1) -> map.put(spritecontents.name(), new TextureAtlasSprite(this.location, spritecontents, i, j, i1, j1)));
      return map;
   }

   public static record Preparations(int width, int height, int mipLevel, TextureAtlasSprite missing, Map<ResourceLocation, TextureAtlasSprite> regions, CompletableFuture<Void> readyForUpload) {
      public CompletableFuture<SpriteLoader.Preparations> waitForUpload() {
         return this.readyForUpload.thenApply((ovoid) -> this);
      }
   }
}
