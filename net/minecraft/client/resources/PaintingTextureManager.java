package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.PaintingVariant;

public class PaintingTextureManager extends TextureAtlasHolder {
   private static final ResourceLocation BACK_SPRITE_LOCATION = new ResourceLocation("back");

   public PaintingTextureManager(TextureManager texturemanager) {
      super(texturemanager, new ResourceLocation("textures/atlas/paintings.png"), new ResourceLocation("paintings"));
   }

   public TextureAtlasSprite get(PaintingVariant paintingvariant) {
      return this.getSprite(BuiltInRegistries.PAINTING_VARIANT.getKey(paintingvariant));
   }

   public TextureAtlasSprite getBackSprite() {
      return this.getSprite(BACK_SPRITE_LOCATION);
   }
}
