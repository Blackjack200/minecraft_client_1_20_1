package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;

public class ElderGuardianRenderer extends GuardianRenderer {
   public static final ResourceLocation GUARDIAN_ELDER_LOCATION = new ResourceLocation("textures/entity/guardian_elder.png");

   public ElderGuardianRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context, 1.2F, ModelLayers.ELDER_GUARDIAN);
   }

   protected void scale(Guardian guardian, PoseStack posestack, float f) {
      posestack.scale(ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE, ElderGuardian.ELDER_SIZE_SCALE);
   }

   public ResourceLocation getTextureLocation(Guardian guardian) {
      return GUARDIAN_ELDER_LOCATION;
   }
}
