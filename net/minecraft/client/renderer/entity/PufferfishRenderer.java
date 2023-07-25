package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

public class PufferfishRenderer extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
   private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
   private int puffStateO = 3;
   private final EntityModel<Pufferfish> small;
   private final EntityModel<Pufferfish> mid;
   private final EntityModel<Pufferfish> big = this.getModel();

   public PufferfishRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, new PufferfishBigModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2F);
      this.mid = new PufferfishMidModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
      this.small = new PufferfishSmallModel<>(entityrendererprovider_context.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
   }

   public ResourceLocation getTextureLocation(Pufferfish pufferfish) {
      return PUFFER_LOCATION;
   }

   public void render(Pufferfish pufferfish, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      int j = pufferfish.getPuffState();
      if (j != this.puffStateO) {
         if (j == 0) {
            this.model = this.small;
         } else if (j == 1) {
            this.model = this.mid;
         } else {
            this.model = this.big;
         }
      }

      this.puffStateO = j;
      this.shadowRadius = 0.1F + 0.1F * (float)j;
      super.render(pufferfish, f, f1, posestack, multibuffersource, i);
   }

   protected void setupRotations(Pufferfish pufferfish, PoseStack posestack, float f, float f1, float f2) {
      posestack.translate(0.0F, Mth.cos(f * 0.05F) * 0.08F, 0.0F);
      super.setupRotations(pufferfish, posestack, f, f1, f2);
   }
}
