package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;

public class LlamaRenderer extends MobRenderer<Llama, LlamaModel<Llama>> {
   private static final ResourceLocation CREAMY = new ResourceLocation("textures/entity/llama/creamy.png");
   private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/llama/white.png");
   private static final ResourceLocation BROWN = new ResourceLocation("textures/entity/llama/brown.png");
   private static final ResourceLocation GRAY = new ResourceLocation("textures/entity/llama/gray.png");

   public LlamaRenderer(EntityRendererProvider.Context entityrendererprovider_context, ModelLayerLocation modellayerlocation) {
      super(entityrendererprovider_context, new LlamaModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation)), 0.7F);
      this.addLayer(new LlamaDecorLayer(this, entityrendererprovider_context.getModelSet()));
   }

   public ResourceLocation getTextureLocation(Llama llama) {
      ResourceLocation var10000;
      switch (llama.getVariant()) {
         case CREAMY:
            var10000 = CREAMY;
            break;
         case WHITE:
            var10000 = WHITE;
            break;
         case BROWN:
            var10000 = BROWN;
            break;
         case GRAY:
            var10000 = GRAY;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }
}
