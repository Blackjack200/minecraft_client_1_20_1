package net.minecraft.client.resources.model;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class AtlasSet implements AutoCloseable {
   private final Map<ResourceLocation, AtlasSet.AtlasEntry> atlases;

   public AtlasSet(Map<ResourceLocation, ResourceLocation> map, TextureManager texturemanager) {
      this.atlases = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (map_entry) -> {
         TextureAtlas textureatlas = new TextureAtlas(map_entry.getKey());
         texturemanager.register(map_entry.getKey(), textureatlas);
         return new AtlasSet.AtlasEntry(textureatlas, map_entry.getValue());
      }));
   }

   public TextureAtlas getAtlas(ResourceLocation resourcelocation) {
      return this.atlases.get(resourcelocation).atlas();
   }

   public void close() {
      this.atlases.values().forEach(AtlasSet.AtlasEntry::close);
      this.atlases.clear();
   }

   public Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> scheduleLoad(ResourceManager resourcemanager, int i, Executor executor) {
      return this.atlases.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (map_entry) -> {
         AtlasSet.AtlasEntry atlasset_atlasentry = map_entry.getValue();
         return SpriteLoader.create(atlasset_atlasentry.atlas).loadAndStitch(resourcemanager, atlasset_atlasentry.atlasInfoLocation, i, executor).thenApply((spriteloader_preparations) -> new AtlasSet.StitchResult(atlasset_atlasentry.atlas, spriteloader_preparations));
      }));
   }

   static record AtlasEntry(TextureAtlas atlas, ResourceLocation atlasInfoLocation) implements AutoCloseable {
      final TextureAtlas atlas;
      final ResourceLocation atlasInfoLocation;

      public void close() {
         this.atlas.clearTextureData();
      }
   }

   public static class StitchResult {
      private final TextureAtlas atlas;
      private final SpriteLoader.Preparations preparations;

      public StitchResult(TextureAtlas textureatlas, SpriteLoader.Preparations spriteloader_preparations) {
         this.atlas = textureatlas;
         this.preparations = spriteloader_preparations;
      }

      @Nullable
      public TextureAtlasSprite getSprite(ResourceLocation resourcelocation) {
         return this.preparations.regions().get(resourcelocation);
      }

      public TextureAtlasSprite missing() {
         return this.preparations.missing();
      }

      public CompletableFuture<Void> readyForUpload() {
         return this.preparations.readyForUpload();
      }

      public void upload() {
         this.atlas.upload(this.preparations);
      }
   }
}
