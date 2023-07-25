package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

public class IllusionerRenderer extends IllagerRenderer<Illusioner> {
   private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

   public IllusionerRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new IllagerModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
      this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>(this, entityrendererprovider_context.getItemInHandRenderer()) {
         public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Illusioner illusioner, float f, float f1, float f2, float f3, float f4, float f5) {
            if (illusioner.isCastingSpell() || illusioner.isAggressive()) {
               super.render(posestack, multibuffersource, i, illusioner, f, f1, f2, f3, f4, f5);
            }

         }
      });
      this.model.getHat().visible = true;
   }

   public ResourceLocation getTextureLocation(Illusioner illusioner) {
      return ILLUSIONER;
   }

   public void render(Illusioner illusioner, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (illusioner.isInvisible()) {
         Vec3[] avec3 = illusioner.getIllusionOffsets(f1);
         float f2 = this.getBob(illusioner, f1);

         for(int j = 0; j < avec3.length; ++j) {
            posestack.pushPose();
            posestack.translate(avec3[j].x + (double)Mth.cos((float)j + f2 * 0.5F) * 0.025D, avec3[j].y + (double)Mth.cos((float)j + f2 * 0.75F) * 0.0125D, avec3[j].z + (double)Mth.cos((float)j + f2 * 0.7F) * 0.025D);
            super.render(illusioner, f, f1, posestack, multibuffersource, i);
            posestack.popPose();
         }
      } else {
         super.render(illusioner, f, f1, posestack, multibuffersource, i);
      }

   }

   protected boolean isBodyVisible(Illusioner illusioner) {
      return true;
   }
}
