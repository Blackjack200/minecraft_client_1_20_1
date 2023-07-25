package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;

public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>> {
   private final EntityModel<T> model;

   public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new SlimeModel<>(entitymodelset.bakeLayer(ModelLayers.SLIME_OUTER));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      Minecraft minecraft = Minecraft.getInstance();
      boolean flag = minecraft.shouldEntityAppearGlowing(livingentity) && livingentity.isInvisible();
      if (!livingentity.isInvisible() || flag) {
         VertexConsumer vertexconsumer;
         if (flag) {
            vertexconsumer = multibuffersource.getBuffer(RenderType.outline(this.getTextureLocation(livingentity)));
         } else {
            vertexconsumer = multibuffersource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(livingentity)));
         }

         this.getParentModel().copyPropertiesTo(this.model);
         this.model.prepareMobModel(livingentity, f, f1, f2);
         this.model.setupAnim(livingentity, f, f1, f3, f4, f5);
         this.model.renderToBuffer(posestack, vertexconsumer, i, LivingEntityRenderer.getOverlayCoords(livingentity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
