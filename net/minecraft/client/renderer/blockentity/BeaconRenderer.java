package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BeaconRenderer implements BlockEntityRenderer<BeaconBlockEntity> {
   public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");
   public static final int MAX_RENDER_Y = 1024;

   public BeaconRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
   }

   public void render(BeaconBlockEntity beaconblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      long k = beaconblockentity.getLevel().getGameTime();
      List<BeaconBlockEntity.BeaconBeamSection> list = beaconblockentity.getBeamSections();
      int l = 0;

      for(int i1 = 0; i1 < list.size(); ++i1) {
         BeaconBlockEntity.BeaconBeamSection beaconblockentity_beaconbeamsection = list.get(i1);
         renderBeaconBeam(posestack, multibuffersource, f, k, l, i1 == list.size() - 1 ? 1024 : beaconblockentity_beaconbeamsection.getHeight(), beaconblockentity_beaconbeamsection.getColor());
         l += beaconblockentity_beaconbeamsection.getHeight();
      }

   }

   private static void renderBeaconBeam(PoseStack posestack, MultiBufferSource multibuffersource, float f, long i, int j, int k, float[] afloat) {
      renderBeaconBeam(posestack, multibuffersource, BEAM_LOCATION, f, 1.0F, i, j, k, afloat, 0.2F, 0.25F);
   }

   public static void renderBeaconBeam(PoseStack posestack, MultiBufferSource multibuffersource, ResourceLocation resourcelocation, float f, float f1, long i, int j, int k, float[] afloat, float f2, float f3) {
      int l = j + k;
      posestack.pushPose();
      posestack.translate(0.5D, 0.0D, 0.5D);
      float f4 = (float)Math.floorMod(i, 40) + f;
      float f5 = k < 0 ? f4 : -f4;
      float f6 = Mth.frac(f5 * 0.2F - (float)Mth.floor(f5 * 0.1F));
      float f7 = afloat[0];
      float f8 = afloat[1];
      float f9 = afloat[2];
      posestack.pushPose();
      posestack.mulPose(Axis.YP.rotationDegrees(f4 * 2.25F - 45.0F));
      float f10 = 0.0F;
      float f13 = 0.0F;
      float f14 = -f2;
      float f15 = 0.0F;
      float f16 = 0.0F;
      float f17 = -f2;
      float f18 = 0.0F;
      float f19 = 1.0F;
      float f20 = -1.0F + f6;
      float f21 = (float)k * f1 * (0.5F / f2) + f20;
      renderPart(posestack, multibuffersource.getBuffer(RenderType.beaconBeam(resourcelocation, false)), f7, f8, f9, 1.0F, j, l, 0.0F, f2, f2, 0.0F, f14, 0.0F, 0.0F, f17, 0.0F, 1.0F, f21, f20);
      posestack.popPose();
      f10 = -f3;
      float f23 = -f3;
      f13 = -f3;
      f14 = -f3;
      f18 = 0.0F;
      f19 = 1.0F;
      f20 = -1.0F + f6;
      f21 = (float)k * f1 + f20;
      renderPart(posestack, multibuffersource.getBuffer(RenderType.beaconBeam(resourcelocation, true)), f7, f8, f9, 0.125F, j, l, f10, f23, f3, f13, f14, f3, f3, f3, 0.0F, 1.0F, f21, f20);
      posestack.popPose();
   }

   private static void renderPart(PoseStack posestack, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, int i, int j, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15) {
      PoseStack.Pose posestack_pose = posestack.last();
      Matrix4f matrix4f = posestack_pose.pose();
      Matrix3f matrix3f = posestack_pose.normal();
      renderQuad(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, i, j, f4, f5, f6, f7, f12, f13, f14, f15);
      renderQuad(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, i, j, f10, f11, f8, f9, f12, f13, f14, f15);
      renderQuad(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, i, j, f6, f7, f10, f11, f12, f13, f14, f15);
      renderQuad(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, i, j, f8, f9, f4, f5, f12, f13, f14, f15);
   }

   private static void renderQuad(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, int i, int j, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
      addVertex(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, j, f4, f5, f9, f10);
      addVertex(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, i, f4, f5, f9, f11);
      addVertex(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, i, f6, f7, f8, f11);
      addVertex(matrix4f, matrix3f, vertexconsumer, f, f1, f2, f3, j, f6, f7, f8, f10);
   }

   private static void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, int i, float f4, float f5, float f6, float f7) {
      vertexconsumer.vertex(matrix4f, f4, (float)i, f5).color(f, f1, f2, f3).uv(f6, f7).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
   }

   public boolean shouldRenderOffScreen(BeaconBlockEntity beaconblockentity) {
      return true;
   }

   public int getViewDistance() {
      return 256;
   }

   public boolean shouldRender(BeaconBlockEntity beaconblockentity, Vec3 vec3) {
      return Vec3.atCenterOf(beaconblockentity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D).closerThan(vec3.multiply(1.0D, 0.0D, 1.0D), (double)this.getViewDistance());
   }
}
