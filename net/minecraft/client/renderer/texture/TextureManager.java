package net.minecraft.client.renderer.texture;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class TextureManager implements PreparableReloadListener, Tickable, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = new ResourceLocation("");
   private final Map<ResourceLocation, AbstractTexture> byPath = Maps.newHashMap();
   private final Set<Tickable> tickableTextures = Sets.newHashSet();
   private final Map<String, Integer> prefixRegister = Maps.newHashMap();
   private final ResourceManager resourceManager;

   public TextureManager(ResourceManager resourcemanager) {
      this.resourceManager = resourcemanager;
   }

   public void bindForSetup(ResourceLocation resourcelocation) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> this._bind(resourcelocation));
      } else {
         this._bind(resourcelocation);
      }

   }

   private void _bind(ResourceLocation resourcelocation) {
      AbstractTexture abstracttexture = this.byPath.get(resourcelocation);
      if (abstracttexture == null) {
         abstracttexture = new SimpleTexture(resourcelocation);
         this.register(resourcelocation, abstracttexture);
      }

      abstracttexture.bind();
   }

   public void register(ResourceLocation resourcelocation, AbstractTexture abstracttexture) {
      abstracttexture = this.loadTexture(resourcelocation, abstracttexture);
      AbstractTexture abstracttexture1 = this.byPath.put(resourcelocation, abstracttexture);
      if (abstracttexture1 != abstracttexture) {
         if (abstracttexture1 != null && abstracttexture1 != MissingTextureAtlasSprite.getTexture()) {
            this.safeClose(resourcelocation, abstracttexture1);
         }

         if (abstracttexture instanceof Tickable) {
            this.tickableTextures.add((Tickable)abstracttexture);
         }
      }

   }

   private void safeClose(ResourceLocation resourcelocation, AbstractTexture abstracttexture) {
      if (abstracttexture != MissingTextureAtlasSprite.getTexture()) {
         this.tickableTextures.remove(abstracttexture);

         try {
            abstracttexture.close();
         } catch (Exception var4) {
            LOGGER.warn("Failed to close texture {}", resourcelocation, var4);
         }
      }

      abstracttexture.releaseId();
   }

   private AbstractTexture loadTexture(ResourceLocation resourcelocation, AbstractTexture abstracttexture) {
      try {
         abstracttexture.load(this.resourceManager);
         return abstracttexture;
      } catch (IOException var6) {
         if (resourcelocation != INTENTIONAL_MISSING_TEXTURE) {
            LOGGER.warn("Failed to load texture: {}", resourcelocation, var6);
         }

         return MissingTextureAtlasSprite.getTexture();
      } catch (Throwable var7) {
         CrashReport crashreport = CrashReport.forThrowable(var7, "Registering texture");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Resource location being registered");
         crashreportcategory.setDetail("Resource location", resourcelocation);
         crashreportcategory.setDetail("Texture object class", () -> abstracttexture.getClass().getName());
         throw new ReportedException(crashreport);
      }
   }

   public AbstractTexture getTexture(ResourceLocation resourcelocation) {
      AbstractTexture abstracttexture = this.byPath.get(resourcelocation);
      if (abstracttexture == null) {
         abstracttexture = new SimpleTexture(resourcelocation);
         this.register(resourcelocation, abstracttexture);
      }

      return abstracttexture;
   }

   public AbstractTexture getTexture(ResourceLocation resourcelocation, AbstractTexture abstracttexture) {
      return this.byPath.getOrDefault(resourcelocation, abstracttexture);
   }

   public ResourceLocation register(String s, DynamicTexture dynamictexture) {
      Integer integer = this.prefixRegister.get(s);
      if (integer == null) {
         integer = 1;
      } else {
         integer = integer + 1;
      }

      this.prefixRegister.put(s, integer);
      ResourceLocation resourcelocation = new ResourceLocation(String.format(Locale.ROOT, "dynamic/%s_%d", s, integer));
      this.register(resourcelocation, dynamictexture);
      return resourcelocation;
   }

   public CompletableFuture<Void> preload(ResourceLocation resourcelocation, Executor executor) {
      if (!this.byPath.containsKey(resourcelocation)) {
         PreloadedTexture preloadedtexture = new PreloadedTexture(this.resourceManager, resourcelocation, executor);
         this.byPath.put(resourcelocation, preloadedtexture);
         return preloadedtexture.getFuture().thenRunAsync(() -> this.register(resourcelocation, preloadedtexture), TextureManager::execute);
      } else {
         return CompletableFuture.completedFuture((Void)null);
      }
   }

   private static void execute(Runnable runnable) {
      Minecraft.getInstance().execute(() -> RenderSystem.recordRenderCall(runnable::run));
   }

   public void tick() {
      for(Tickable tickable : this.tickableTextures) {
         tickable.tick();
      }

   }

   public void release(ResourceLocation resourcelocation) {
      AbstractTexture abstracttexture = this.byPath.remove(resourcelocation);
      if (abstracttexture != null) {
         this.safeClose(resourcelocation, abstracttexture);
      }

   }

   public void close() {
      this.byPath.forEach(this::safeClose);
      this.byPath.clear();
      this.tickableTextures.clear();
      this.prefixRegister.clear();
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      CompletableFuture<Void> completablefuture = new CompletableFuture<>();
      CompletableFuture.allOf(TitleScreen.preloadResources(this, executor), this.preload(AbstractWidget.WIDGETS_LOCATION, executor)).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((ovoid) -> {
         MissingTextureAtlasSprite.getTexture();
         RealmsMainScreen.updateTeaserImages(this.resourceManager);
         Iterator<Map.Entry<ResourceLocation, AbstractTexture>> iterator = this.byPath.entrySet().iterator();

         while(iterator.hasNext()) {
            Map.Entry<ResourceLocation, AbstractTexture> map_entry = iterator.next();
            ResourceLocation resourcelocation = map_entry.getKey();
            AbstractTexture abstracttexture = map_entry.getValue();
            if (abstracttexture == MissingTextureAtlasSprite.getTexture() && !resourcelocation.equals(MissingTextureAtlasSprite.getLocation())) {
               iterator.remove();
            } else {
               abstracttexture.reset(this, resourcemanager, resourcelocation, executor1);
            }
         }

         Minecraft.getInstance().tell(() -> completablefuture.complete((Void)null));
      }, (runnable) -> RenderSystem.recordRenderCall(runnable::run));
      return completablefuture;
   }

   public void dumpAllSheets(Path path) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> this._dumpAllSheets(path));
      } else {
         this._dumpAllSheets(path);
      }

   }

   private void _dumpAllSheets(Path path) {
      try {
         Files.createDirectories(path);
      } catch (IOException var3) {
         LOGGER.error("Failed to create directory {}", path, var3);
         return;
      }

      this.byPath.forEach((resourcelocation, abstracttexture) -> {
         if (abstracttexture instanceof Dumpable dumpable) {
            try {
               dumpable.dumpContents(resourcelocation, path);
            } catch (IOException var5) {
               LOGGER.error("Failed to dump texture {}", resourcelocation, var5);
            }
         }

      });
   }
}
