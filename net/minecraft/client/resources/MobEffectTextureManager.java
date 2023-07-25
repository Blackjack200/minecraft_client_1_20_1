package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class MobEffectTextureManager extends TextureAtlasHolder {
   public MobEffectTextureManager(TextureManager texturemanager) {
      super(texturemanager, new ResourceLocation("textures/atlas/mob_effects.png"), new ResourceLocation("mob_effects"));
   }

   public TextureAtlasSprite get(MobEffect mobeffect) {
      return this.getSprite(BuiltInRegistries.MOB_EFFECT.getKey(mobeffect));
   }
}
