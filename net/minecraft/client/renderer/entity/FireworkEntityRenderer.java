package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;

public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
   private final ItemRenderer itemRenderer;

   public FireworkEntityRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.itemRenderer = entityrendererprovider_context.getItemRenderer();
   }

   public void render(FireworkRocketEntity fireworkrocketentity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
      if (fireworkrocketentity.isShotAtAngle()) {
         posestack.mulPose(Axis.ZP.rotationDegrees(180.0F));
         posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
         posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
      }

      this.itemRenderer.renderStatic(fireworkrocketentity.getItem(), ItemDisplayContext.GROUND, i, OverlayTexture.NO_OVERLAY, posestack, multibuffersource, fireworkrocketentity.level(), fireworkrocketentity.getId());
      posestack.popPose();
      super.render(fireworkrocketentity, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkrocketentity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
