package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
   public static final int LEASH_RENDER_STEPS = 24;

   public MobRenderer(EntityRendererProvider.Context entityrendererprovider_context, M entitymodel, float f) {
      super(entityrendererprovider_context, entitymodel, f);
   }

   protected boolean shouldShowName(T mob) {
      return super.shouldShowName(mob) && (mob.shouldShowName() || mob.hasCustomName() && mob == this.entityRenderDispatcher.crosshairPickEntity);
   }

   public boolean shouldRender(T mob, Frustum frustum, double d0, double d1, double d2) {
      if (super.shouldRender(mob, frustum, d0, d1, d2)) {
         return true;
      } else {
         Entity entity = mob.getLeashHolder();
         return entity != null ? frustum.isVisible(entity.getBoundingBoxForCulling()) : false;
      }
   }

   public void render(T mob, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      super.render(mob, f, f1, posestack, multibuffersource, i);
      Entity entity = mob.getLeashHolder();
      if (entity != null) {
         this.renderLeash(mob, f1, posestack, multibuffersource, entity);
      }
   }

   private <E extends Entity> void renderLeash(T mob, float f, PoseStack posestack, MultiBufferSource multibuffersource, E entity) {
      posestack.pushPose();
      Vec3 vec3 = entity.getRopeHoldPosition(f);
      double d0 = (double)(Mth.lerp(f, mob.yBodyRotO, mob.yBodyRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
      Vec3 vec31 = mob.getLeashOffset(f);
      double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
      double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
      double d3 = Mth.lerp((double)f, mob.xo, mob.getX()) + d1;
      double d4 = Mth.lerp((double)f, mob.yo, mob.getY()) + vec31.y;
      double d5 = Mth.lerp((double)f, mob.zo, mob.getZ()) + d2;
      posestack.translate(d1, vec31.y, d2);
      float f1 = (float)(vec3.x - d3);
      float f2 = (float)(vec3.y - d4);
      float f3 = (float)(vec3.z - d5);
      float f4 = 0.025F;
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.leash());
      Matrix4f matrix4f = posestack.last().pose();
      float f5 = Mth.invSqrt(f1 * f1 + f3 * f3) * 0.025F / 2.0F;
      float f6 = f3 * f5;
      float f7 = f1 * f5;
      BlockPos blockpos = BlockPos.containing(mob.getEyePosition(f));
      BlockPos blockpos1 = BlockPos.containing(entity.getEyePosition(f));
      int i = this.getBlockLightLevel(mob, blockpos);
      int j = this.entityRenderDispatcher.getRenderer(entity).getBlockLightLevel(entity, blockpos1);
      int k = mob.level().getBrightness(LightLayer.SKY, blockpos);
      int l = mob.level().getBrightness(LightLayer.SKY, blockpos1);

      for(int i1 = 0; i1 <= 24; ++i1) {
         addVertexPair(vertexconsumer, matrix4f, f1, f2, f3, i, j, k, l, 0.025F, 0.025F, f6, f7, i1, false);
      }

      for(int j1 = 24; j1 >= 0; --j1) {
         addVertexPair(vertexconsumer, matrix4f, f1, f2, f3, i, j, k, l, 0.025F, 0.0F, f6, f7, j1, true);
      }

      posestack.popPose();
   }

   private static void addVertexPair(VertexConsumer vertexconsumer, Matrix4f matrix4f, float f, float f1, float f2, int i, int j, int k, int l, float f3, float f4, float f5, float f6, int i1, boolean flag) {
      float f7 = (float)i1 / 24.0F;
      int j1 = (int)Mth.lerp(f7, (float)i, (float)j);
      int k1 = (int)Mth.lerp(f7, (float)k, (float)l);
      int l1 = LightTexture.pack(j1, k1);
      float f8 = i1 % 2 == (flag ? 1 : 0) ? 0.7F : 1.0F;
      float f9 = 0.5F * f8;
      float f10 = 0.4F * f8;
      float f11 = 0.3F * f8;
      float f12 = f * f7;
      float f13 = f1 > 0.0F ? f1 * f7 * f7 : f1 - f1 * (1.0F - f7) * (1.0F - f7);
      float f14 = f2 * f7;
      vertexconsumer.vertex(matrix4f, f12 - f5, f13 + f4, f14 + f6).color(f9, f10, f11, 1.0F).uv2(l1).endVertex();
      vertexconsumer.vertex(matrix4f, f12 + f5, f13 + f3 - f4, f14 - f6).color(f9, f10, f11, 1.0F).uv2(l1).endVertex();
   }
}
