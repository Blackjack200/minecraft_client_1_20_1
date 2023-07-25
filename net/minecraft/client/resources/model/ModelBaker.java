package net.minecraft.client.resources.model;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public interface ModelBaker {
   UnbakedModel getModel(ResourceLocation resourcelocation);

   @Nullable
   BakedModel bake(ResourceLocation resourcelocation, ModelState modelstate);
}
