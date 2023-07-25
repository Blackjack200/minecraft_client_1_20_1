package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class PreloadedTexture extends SimpleTexture {
   @Nullable
   private CompletableFuture<SimpleTexture.TextureImage> future;

   public PreloadedTexture(ResourceManager resourcemanager, ResourceLocation resourcelocation, Executor executor) {
      super(resourcelocation);
      this.future = CompletableFuture.supplyAsync(() -> SimpleTexture.TextureImage.load(resourcemanager, resourcelocation), executor);
   }

   protected SimpleTexture.TextureImage getTextureImage(ResourceManager resourcemanager) {
      if (this.future != null) {
         SimpleTexture.TextureImage simpletexture_textureimage = this.future.join();
         this.future = null;
         return simpletexture_textureimage;
      } else {
         return SimpleTexture.TextureImage.load(resourcemanager, this.location);
      }
   }

   public CompletableFuture<Void> getFuture() {
      return this.future == null ? CompletableFuture.completedFuture((Void)null) : this.future.thenApply((simpletexture_textureimage) -> null);
   }

   public void reset(TextureManager texturemanager, ResourceManager resourcemanager, ResourceLocation resourcelocation, Executor executor) {
      this.future = CompletableFuture.supplyAsync(() -> SimpleTexture.TextureImage.load(resourcemanager, this.location), Util.backgroundExecutor());
      this.future.thenRunAsync(() -> texturemanager.register(this.location, this), executor(executor));
   }

   private static Executor executor(Executor executor) {
      return (runnable) -> executor.execute(() -> RenderSystem.recordRenderCall(runnable::run));
   }
}
