package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class LoadingOverlay extends Overlay {
   static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = new ResourceLocation("textures/gui/title/mojangstudios.png");
   private static final int LOGO_BACKGROUND_COLOR = FastColor.ARGB32.color(255, 239, 50, 61);
   private static final int LOGO_BACKGROUND_COLOR_DARK = FastColor.ARGB32.color(255, 0, 0, 0);
   private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get() ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
   private static final int LOGO_SCALE = 240;
   private static final float LOGO_QUARTER_FLOAT = 60.0F;
   private static final int LOGO_QUARTER = 60;
   private static final int LOGO_HALF = 120;
   private static final float LOGO_OVERLAP = 0.0625F;
   private static final float SMOOTHING = 0.95F;
   public static final long FADE_OUT_TIME = 1000L;
   public static final long FADE_IN_TIME = 500L;
   private final Minecraft minecraft;
   private final ReloadInstance reload;
   private final Consumer<Optional<Throwable>> onFinish;
   private final boolean fadeIn;
   private float currentProgress;
   private long fadeOutStart = -1L;
   private long fadeInStart = -1L;

   public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadinstance, Consumer<Optional<Throwable>> consumer, boolean flag) {
      this.minecraft = minecraft;
      this.reload = reloadinstance;
      this.onFinish = consumer;
      this.fadeIn = flag;
   }

   public static void registerTextures(Minecraft minecraft) {
      minecraft.getTextureManager().register(MOJANG_STUDIOS_LOGO_LOCATION, new LoadingOverlay.LogoTexture());
   }

   private static int replaceAlpha(int i, int j) {
      return i & 16777215 | j << 24;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      int k = guigraphics.guiWidth();
      int l = guigraphics.guiHeight();
      long i1 = Util.getMillis();
      if (this.fadeIn && this.fadeInStart == -1L) {
         this.fadeInStart = i1;
      }

      float f1 = this.fadeOutStart > -1L ? (float)(i1 - this.fadeOutStart) / 1000.0F : -1.0F;
      float f2 = this.fadeInStart > -1L ? (float)(i1 - this.fadeInStart) / 500.0F : -1.0F;
      float f3;
      if (f1 >= 1.0F) {
         if (this.minecraft.screen != null) {
            this.minecraft.screen.render(guigraphics, 0, 0, f);
         }

         int j1 = Mth.ceil((1.0F - Mth.clamp(f1 - 1.0F, 0.0F, 1.0F)) * 255.0F);
         guigraphics.fill(RenderType.guiOverlay(), 0, 0, k, l, replaceAlpha(BRAND_BACKGROUND.getAsInt(), j1));
         f3 = 1.0F - Mth.clamp(f1 - 1.0F, 0.0F, 1.0F);
      } else if (this.fadeIn) {
         if (this.minecraft.screen != null && f2 < 1.0F) {
            this.minecraft.screen.render(guigraphics, i, j, f);
         }

         int k1 = Mth.ceil(Mth.clamp((double)f2, 0.15D, 1.0D) * 255.0D);
         guigraphics.fill(RenderType.guiOverlay(), 0, 0, k, l, replaceAlpha(BRAND_BACKGROUND.getAsInt(), k1));
         f3 = Mth.clamp(f2, 0.0F, 1.0F);
      } else {
         int l1 = BRAND_BACKGROUND.getAsInt();
         float f5 = (float)(l1 >> 16 & 255) / 255.0F;
         float f6 = (float)(l1 >> 8 & 255) / 255.0F;
         float f7 = (float)(l1 & 255) / 255.0F;
         GlStateManager._clearColor(f5, f6, f7, 1.0F);
         GlStateManager._clear(16384, Minecraft.ON_OSX);
         f3 = 1.0F;
      }

      int i2 = (int)((double)guigraphics.guiWidth() * 0.5D);
      int j2 = (int)((double)guigraphics.guiHeight() * 0.5D);
      double d0 = Math.min((double)guigraphics.guiWidth() * 0.75D, (double)guigraphics.guiHeight()) * 0.25D;
      int k2 = (int)(d0 * 0.5D);
      double d1 = d0 * 4.0D;
      int l2 = (int)(d1 * 0.5D);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, f3);
      guigraphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, i2 - l2, j2 - k2, l2, (int)d0, -0.0625F, 0.0F, 120, 60, 120, 120);
      guigraphics.blit(MOJANG_STUDIOS_LOGO_LOCATION, i2, j2 - k2, l2, (int)d0, 0.0625F, 60.0F, 120, 60, 120, 120);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      int i3 = (int)((double)guigraphics.guiHeight() * 0.8325D);
      float f9 = this.reload.getActualProgress();
      this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + f9 * 0.050000012F, 0.0F, 1.0F);
      if (f1 < 1.0F) {
         this.drawProgressBar(guigraphics, k / 2 - l2, i3 - 5, k / 2 + l2, i3 + 5, 1.0F - Mth.clamp(f1, 0.0F, 1.0F));
      }

      if (f1 >= 2.0F) {
         this.minecraft.setOverlay((Overlay)null);
      }

      if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f2 >= 2.0F)) {
         try {
            this.reload.checkExceptions();
            this.onFinish.accept(Optional.empty());
         } catch (Throwable var23) {
            this.onFinish.accept(Optional.of(var23));
         }

         this.fadeOutStart = Util.getMillis();
         if (this.minecraft.screen != null) {
            this.minecraft.screen.init(this.minecraft, guigraphics.guiWidth(), guigraphics.guiHeight());
         }
      }

   }

   private void drawProgressBar(GuiGraphics guigraphics, int i, int j, int k, int l, float f) {
      int i1 = Mth.ceil((float)(k - i - 2) * this.currentProgress);
      int j1 = Math.round(f * 255.0F);
      int k1 = FastColor.ARGB32.color(j1, 255, 255, 255);
      guigraphics.fill(i + 2, j + 2, i + i1, l - 2, k1);
      guigraphics.fill(i + 1, j, k - 1, j + 1, k1);
      guigraphics.fill(i + 1, l, k - 1, l - 1, k1);
      guigraphics.fill(i, j, i + 1, l, k1);
      guigraphics.fill(k, j, k - 1, l, k1);
   }

   public boolean isPauseScreen() {
      return true;
   }

   static class LogoTexture extends SimpleTexture {
      public LogoTexture() {
         super(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
      }

      protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourcemanager) {
         VanillaPackResources vanillapackresources = Minecraft.getInstance().getVanillaPackResources();
         IoSupplier<InputStream> iosupplier = vanillapackresources.getResource(PackType.CLIENT_RESOURCES, LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION);
         if (iosupplier == null) {
            return new SimpleTexture.TextureImage(new FileNotFoundException(LoadingOverlay.MOJANG_STUDIOS_LOGO_LOCATION.toString()));
         } else {
            try {
               InputStream inputstream = iosupplier.get();

               SimpleTexture.TextureImage var5;
               try {
                  var5 = new SimpleTexture.TextureImage(new TextureMetadataSection(true, true), NativeImage.read(inputstream));
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

               return var5;
            } catch (IOException var9) {
               return new SimpleTexture.TextureImage(var9);
            }
         }
      }
   }
}
