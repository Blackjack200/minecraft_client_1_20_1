package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
   private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
   protected final EntityModel<T> model;
   private final BlockRenderDispatcher blockRenderer;

   public MinecartRenderer(EntityRendererProvider.Context entityrendererprovider_context, ModelLayerLocation modellayerlocation) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.7F;
      this.model = new MinecartModel<>(entityrendererprovider_context.bakeLayer(modellayerlocation));
      this.blockRenderer = entityrendererprovider_context.getBlockRenderDispatcher();
   }

   public void render(T abstractminecart, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      super.render(abstractminecart, f, f1, posestack, multibuffersource, i);
      posestack.pushPose();
      long j = (long)abstractminecart.getId() * 493286711L;
      j = j * j * 4392167121L + j * 98761L;
      float f2 = (((float)(j >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float f3 = (((float)(j >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float f4 = (((float)(j >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      posestack.translate(f2, f3, f4);
      double d0 = Mth.lerp((double)f1, abstractminecart.xOld, abstractminecart.getX());
      double d1 = Mth.lerp((double)f1, abstractminecart.yOld, abstractminecart.getY());
      double d2 = Mth.lerp((double)f1, abstractminecart.zOld, abstractminecart.getZ());
      double d3 = (double)0.3F;
      Vec3 vec3 = abstractminecart.getPos(d0, d1, d2);
      float f5 = Mth.lerp(f1, abstractminecart.xRotO, abstractminecart.getXRot());
      if (vec3 != null) {
         Vec3 vec31 = abstractminecart.getPosOffs(d0, d1, d2, (double)0.3F);
         Vec3 vec32 = abstractminecart.getPosOffs(d0, d1, d2, (double)-0.3F);
         if (vec31 == null) {
            vec31 = vec3;
         }

         if (vec32 == null) {
            vec32 = vec3;
         }

         posestack.translate(vec3.x - d0, (vec31.y + vec32.y) / 2.0D - d1, vec3.z - d2);
         Vec3 vec33 = vec32.add(-vec31.x, -vec31.y, -vec31.z);
         if (vec33.length() != 0.0D) {
            vec33 = vec33.normalize();
            f = (float)(Math.atan2(vec33.z, vec33.x) * 180.0D / Math.PI);
            f5 = (float)(Math.atan(vec33.y) * 73.0D);
         }
      }

      posestack.translate(0.0F, 0.375F, 0.0F);
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
      posestack.mulPose(Axis.ZP.rotationDegrees(-f5));
      float f6 = (float)abstractminecart.getHurtTime() - f1;
      float f7 = abstractminecart.getDamage() - f1;
      if (f7 < 0.0F) {
         f7 = 0.0F;
      }

      if (f6 > 0.0F) {
         posestack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f6) * f6 * f7 / 10.0F * (float)abstractminecart.getHurtDir()));
      }

      int k = abstractminecart.getDisplayOffset();
      BlockState blockstate = abstractminecart.getDisplayBlockState();
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         posestack.pushPose();
         float f8 = 0.75F;
         posestack.scale(0.75F, 0.75F, 0.75F);
         posestack.translate(-0.5F, (float)(k - 8) / 16.0F, 0.5F);
         posestack.mulPose(Axis.YP.rotationDegrees(90.0F));
         this.renderMinecartContents(abstractminecart, f1, blockstate, posestack, multibuffersource, i);
         posestack.popPose();
      }

      posestack.scale(-1.0F, -1.0F, 1.0F);
      this.model.setupAnim(abstractminecart, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(this.getTextureLocation(abstractminecart)));
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
   }

   public ResourceLocation getTextureLocation(T abstractminecart) {
      return MINECART_LOCATION;
   }

   protected void renderMinecartContents(T abstractminecart, float f, BlockState blockstate, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      this.blockRenderer.renderSingleBlock(blockstate, posestack, multibuffersource, i, OverlayTexture.NO_OVERLAY);
   }
}
