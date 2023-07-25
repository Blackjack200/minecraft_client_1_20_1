package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public class SheepFurLayer extends RenderLayer<Sheep, SheepModel<Sheep>> {
   private static final ResourceLocation SHEEP_FUR_LOCATION = new ResourceLocation("textures/entity/sheep/sheep_fur.png");
   private final SheepFurModel<Sheep> model;

   public SheepFurLayer(RenderLayerParent<Sheep, SheepModel<Sheep>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new SheepFurModel<>(entitymodelset.bakeLayer(ModelLayers.SHEEP_FUR));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Sheep sheep, float f, float f1, float f2, float f3, float f4, float f5) {
      if (!sheep.isSheared()) {
         if (sheep.isInvisible()) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = minecraft.shouldEntityAppearGlowing(sheep);
            if (flag) {
               this.getParentModel().copyPropertiesTo(this.model);
               this.model.prepareMobModel(sheep, f, f1, f2);
               this.model.setupAnim(sheep, f, f1, f3, f4, f5);
               VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.outline(SHEEP_FUR_LOCATION));
               this.model.renderToBuffer(posestack, vertexconsumer, i, LivingEntityRenderer.getOverlayCoords(sheep, 0.0F), 0.0F, 0.0F, 0.0F, 1.0F);
            }

         } else {
            float f7;
            float f8;
            float f9;
            if (sheep.hasCustomName() && "jeb_".equals(sheep.getName().getString())) {
               int j = 25;
               int k = sheep.tickCount / 25 + sheep.getId();
               int l = DyeColor.values().length;
               int i1 = k % l;
               int j1 = (k + 1) % l;
               float f6 = ((float)(sheep.tickCount % 25) + f2) / 25.0F;
               float[] afloat = Sheep.getColorArray(DyeColor.byId(i1));
               float[] afloat1 = Sheep.getColorArray(DyeColor.byId(j1));
               f7 = afloat[0] * (1.0F - f6) + afloat1[0] * f6;
               f8 = afloat[1] * (1.0F - f6) + afloat1[1] * f6;
               f9 = afloat[2] * (1.0F - f6) + afloat1[2] * f6;
            } else {
               float[] afloat2 = Sheep.getColorArray(sheep.getColor());
               f7 = afloat2[0];
               f8 = afloat2[1];
               f9 = afloat2[2];
            }

            coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, SHEEP_FUR_LOCATION, posestack, multibuffersource, i, sheep, f, f1, f3, f4, f5, f2, f7, f8, f9);
         }
      }
   }
}
