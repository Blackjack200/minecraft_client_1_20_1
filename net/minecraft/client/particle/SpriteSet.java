package net.minecraft.client.particle;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

public interface SpriteSet {
   TextureAtlasSprite get(int i, int j);

   TextureAtlasSprite get(RandomSource randomsource);
}
