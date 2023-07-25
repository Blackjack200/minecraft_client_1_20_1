package net.minecraft.client.resources.model;

import java.util.Collection;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public interface UnbakedModel {
   Collection<ResourceLocation> getDependencies();

   void resolveParents(Function<ResourceLocation, UnbakedModel> function);

   @Nullable
   BakedModel bake(ModelBaker modelbaker, Function<Material, TextureAtlasSprite> function, ModelState modelstate, ResourceLocation resourcelocation);
}
