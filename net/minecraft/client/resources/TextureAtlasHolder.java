package net.minecraft.client.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class TextureAtlasHolder implements PreparableReloadListener, AutoCloseable {
   private final TextureAtlas textureAtlas;
   private final ResourceLocation atlasInfoLocation;

   public TextureAtlasHolder(TextureManager texturemanager, ResourceLocation resourcelocation, ResourceLocation resourcelocation1) {
      this.atlasInfoLocation = resourcelocation1;
      this.textureAtlas = new TextureAtlas(resourcelocation);
      texturemanager.register(this.textureAtlas.location(), this.textureAtlas);
   }

   protected TextureAtlasSprite getSprite(ResourceLocation resourcelocation) {
      return this.textureAtlas.getSprite(resourcelocation);
   }

   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparablereloadlistener_preparationbarrier, ResourceManager resourcemanager, ProfilerFiller profilerfiller, ProfilerFiller profilerfiller1, Executor executor, Executor executor1) {
      return SpriteLoader.create(this.textureAtlas).loadAndStitch(resourcemanager, this.atlasInfoLocation, 0, executor).thenCompose(SpriteLoader.Preparations::waitForUpload).thenCompose(preparablereloadlistener_preparationbarrier::wait).thenAcceptAsync((spriteloader_preparations) -> this.apply(spriteloader_preparations, profilerfiller1), executor1);
   }

   private void apply(SpriteLoader.Preparations spriteloader_preparations, ProfilerFiller profilerfiller) {
      profilerfiller.startTick();
      profilerfiller.push("upload");
      this.textureAtlas.upload(spriteloader_preparations);
      profilerfiller.pop();
      profilerfiller.endTick();
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }
}
