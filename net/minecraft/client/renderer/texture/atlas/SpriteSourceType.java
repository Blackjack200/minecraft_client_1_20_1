package net.minecraft.client.renderer.texture.atlas;

import com.mojang.serialization.Codec;

public record SpriteSourceType(Codec<? extends SpriteSource> codec) {
}
