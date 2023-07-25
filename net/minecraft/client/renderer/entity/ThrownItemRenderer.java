package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;

public class ThrownItemRenderer<T extends Entity & ItemSupplier> extends EntityRenderer<T> {
   private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25F;
   private final ItemRenderer itemRenderer;
   private final float scale;
   private final boolean fullBright;

   public ThrownItemRenderer(EntityRendererProvider.Context entityrendererprovider_context, float f, boolean flag) {
      super(entityrendererprovider_context);
      this.itemRenderer = entityrendererprovider_context.getItemRenderer();
      this.scale = f;
      this.fullBright = flag;
   }

   public ThrownItemRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      this(entityrendererprovider_context, 1.0F, false);
   }

   protected int getBlockLightLevel(T entity, BlockPos blockpos) {
      return this.fullBright ? 15 : super.getBlockLightLevel(entity, blockpos);
   }

   public void render(T entity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25D)) {
         posestack.pushPose();
         posestack.scale(this.scale, this.scale, this.scale);
         posestack.mulPose(this.entityRenderDispatcher.cameraOrientation());
         posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
         this.itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND, i, OverlayTexture.NO_OVERLAY, posestack, multibuffersource, entity.level(), entity.getId());
         posestack.popPose();
         super.render(entity, f, f1, posestack, multibuffersource, i);
      }
   }

   public ResourceLocation getTextureLocation(Entity entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
