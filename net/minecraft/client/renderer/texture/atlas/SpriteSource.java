package net.minecraft.client.renderer.texture.atlas;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public interface SpriteSource {
   FileToIdConverter TEXTURE_ID_CONVERTER = new FileToIdConverter("textures", ".png");

   void run(ResourceManager resourcemanager, SpriteSource.Output spritesource_output);

   SpriteSourceType type();

   public interface Output {
      default void add(ResourceLocation resourcelocation, Resource resource) {
         this.add(resourcelocation, () -> SpriteLoader.loadSprite(resourcelocation, resource));
      }

      void add(ResourceLocation resourcelocation, SpriteSource.SpriteSupplier spritesource_spritesupplier);

      void removeAll(Predicate<ResourceLocation> predicate);
   }

   public interface SpriteSupplier extends Supplier<SpriteContents> {
      default void discard() {
      }
   }
}
