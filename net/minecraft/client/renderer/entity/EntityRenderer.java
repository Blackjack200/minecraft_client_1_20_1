package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class EntityRenderer<T extends Entity> {
   protected static final float NAMETAG_SCALE = 0.025F;
   protected final EntityRenderDispatcher entityRenderDispatcher;
   private final Font font;
   protected float shadowRadius;
   protected float shadowStrength = 1.0F;

   protected EntityRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      this.entityRenderDispatcher = entityrendererprovider_context.getEntityRenderDispatcher();
      this.font = entityrendererprovider_context.getFont();
   }

   public final int getPackedLightCoords(T entity, float f) {
      BlockPos blockpos = BlockPos.containing(entity.getLightProbePosition(f));
      return LightTexture.pack(this.getBlockLightLevel(entity, blockpos), this.getSkyLightLevel(entity, blockpos));
   }

   protected int getSkyLightLevel(T entity, BlockPos blockpos) {
      return entity.level().getBrightness(LightLayer.SKY, blockpos);
   }

   protected int getBlockLightLevel(T entity, BlockPos blockpos) {
      return entity.isOnFire() ? 15 : entity.level().getBrightness(LightLayer.BLOCK, blockpos);
   }

   public boolean shouldRender(T entity, Frustum frustum, double d0, double d1, double d2) {
      if (!entity.shouldRender(d0, d1, d2)) {
         return false;
      } else if (entity.noCulling) {
         return true;
      } else {
         AABB aabb = entity.getBoundingBoxForCulling().inflate(0.5D);
         if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
            aabb = new AABB(entity.getX() - 2.0D, entity.getY() - 2.0D, entity.getZ() - 2.0D, entity.getX() + 2.0D, entity.getY() + 2.0D, entity.getZ() + 2.0D);
         }

         return frustum.isVisible(aabb);
      }
   }

   public Vec3 getRenderOffset(T entity, float f) {
      return Vec3.ZERO;
   }

   public void render(T entity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      if (this.shouldShowName(entity)) {
         this.renderNameTag(entity, entity.getDisplayName(), posestack, multibuffersource, i);
      }
   }

   protected boolean shouldShowName(T entity) {
      return entity.shouldShowName() && entity.hasCustomName();
   }

   public abstract ResourceLocation getTextureLocation(T entity);

   public Font getFont() {
      return this.font;
   }

   protected void renderNameTag(T entity, Component component, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
      if (!(d0 > 4096.0D)) {
         boolean flag = !entity.isDiscrete();
         float f = entity.getNameTagOffsetY();
         int j = "deadmau5".equals(component.getString()) ? -10 : 0;
         posestack.pushPose();
         posestack.translate(0.0F, f, 0.0F);
         posestack.mulPose(this.entityRenderDispatcher.cameraOrientation());
         posestack.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f matrix4f = posestack.last().pose();
         float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
         int k = (int)(f1 * 255.0F) << 24;
         Font font = this.getFont();
         float f2 = (float)(-font.width(component) / 2);
         font.drawInBatch(component, f2, (float)j, 553648127, false, matrix4f, multibuffersource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, k, i);
         if (flag) {
            font.drawInBatch(component, f2, (float)j, -1, false, matrix4f, multibuffersource, Font.DisplayMode.NORMAL, 0, i);
         }

         posestack.popPose();
      }
   }
}
