package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;

public class LlamaDecorLayer extends RenderLayer<Llama, LlamaModel<Llama>> {
   private static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{new ResourceLocation("textures/entity/llama/decor/white.png"), new ResourceLocation("textures/entity/llama/decor/orange.png"), new ResourceLocation("textures/entity/llama/decor/magenta.png"), new ResourceLocation("textures/entity/llama/decor/light_blue.png"), new ResourceLocation("textures/entity/llama/decor/yellow.png"), new ResourceLocation("textures/entity/llama/decor/lime.png"), new ResourceLocation("textures/entity/llama/decor/pink.png"), new ResourceLocation("textures/entity/llama/decor/gray.png"), new ResourceLocation("textures/entity/llama/decor/light_gray.png"), new ResourceLocation("textures/entity/llama/decor/cyan.png"), new ResourceLocation("textures/entity/llama/decor/purple.png"), new ResourceLocation("textures/entity/llama/decor/blue.png"), new ResourceLocation("textures/entity/llama/decor/brown.png"), new ResourceLocation("textures/entity/llama/decor/green.png"), new ResourceLocation("textures/entity/llama/decor/red.png"), new ResourceLocation("textures/entity/llama/decor/black.png")};
   private static final ResourceLocation TRADER_LLAMA = new ResourceLocation("textures/entity/llama/decor/trader_llama.png");
   private final LlamaModel<Llama> model;

   public LlamaDecorLayer(RenderLayerParent<Llama, LlamaModel<Llama>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new LlamaModel<>(entitymodelset.bakeLayer(ModelLayers.LLAMA_DECOR));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Llama llama, float f, float f1, float f2, float f3, float f4, float f5) {
      DyeColor dyecolor = llama.getSwag();
      ResourceLocation resourcelocation;
      if (dyecolor != null) {
         resourcelocation = TEXTURE_LOCATION[dyecolor.getId()];
      } else {
         if (!llama.isTraderLlama()) {
            return;
         }

         resourcelocation = TRADER_LLAMA;
      }

      this.getParentModel().copyPropertiesTo(this.model);
      this.model.setupAnim(llama, f, f1, f3, f4, f5);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutoutNoCull(resourcelocation));
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }
}
