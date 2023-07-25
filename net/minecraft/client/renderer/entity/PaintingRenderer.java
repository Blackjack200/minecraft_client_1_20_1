package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PaintingRenderer extends EntityRenderer<Painting> {
   public PaintingRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
   }

   public void render(Painting painting, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
      PaintingVariant paintingvariant = painting.getVariant().value();
      float f2 = 0.0625F;
      posestack.scale(0.0625F, 0.0625F, 0.0625F);
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entitySolid(this.getTextureLocation(painting)));
      PaintingTextureManager paintingtexturemanager = Minecraft.getInstance().getPaintingTextures();
      this.renderPainting(posestack, vertexconsumer, painting, paintingvariant.getWidth(), paintingvariant.getHeight(), paintingtexturemanager.get(paintingvariant), paintingtexturemanager.getBackSprite());
      posestack.popPose();
      super.render(painting, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(Painting painting) {
      return Minecraft.getInstance().getPaintingTextures().getBackSprite().atlasLocation();
   }

   private void renderPainting(PoseStack posestack, VertexConsumer vertexconsumer, Painting painting, int i, int j, TextureAtlasSprite textureatlassprite, TextureAtlasSprite textureatlassprite1) {
      PoseStack.Pose posestack_pose = posestack.last();
      Matrix4f matrix4f = posestack_pose.pose();
      Matrix3f matrix3f = posestack_pose.normal();
      float f = (float)(-i) / 2.0F;
      float f1 = (float)(-j) / 2.0F;
      float f2 = 0.5F;
      float f3 = textureatlassprite1.getU0();
      float f4 = textureatlassprite1.getU1();
      float f5 = textureatlassprite1.getV0();
      float f6 = textureatlassprite1.getV1();
      float f7 = textureatlassprite1.getU0();
      float f8 = textureatlassprite1.getU1();
      float f9 = textureatlassprite1.getV0();
      float f10 = textureatlassprite1.getV(1.0D);
      float f11 = textureatlassprite1.getU0();
      float f12 = textureatlassprite1.getU(1.0D);
      float f13 = textureatlassprite1.getV0();
      float f14 = textureatlassprite1.getV1();
      int k = i / 16;
      int l = j / 16;
      double d0 = 16.0D / (double)k;
      double d1 = 16.0D / (double)l;

      for(int i1 = 0; i1 < k; ++i1) {
         for(int j1 = 0; j1 < l; ++j1) {
            float f15 = f + (float)((i1 + 1) * 16);
            float f16 = f + (float)(i1 * 16);
            float f17 = f1 + (float)((j1 + 1) * 16);
            float f18 = f1 + (float)(j1 * 16);
            int k1 = painting.getBlockX();
            int l1 = Mth.floor(painting.getY() + (double)((f17 + f18) / 2.0F / 16.0F));
            int i2 = painting.getBlockZ();
            Direction direction = painting.getDirection();
            if (direction == Direction.NORTH) {
               k1 = Mth.floor(painting.getX() + (double)((f15 + f16) / 2.0F / 16.0F));
            }

            if (direction == Direction.WEST) {
               i2 = Mth.floor(painting.getZ() - (double)((f15 + f16) / 2.0F / 16.0F));
            }

            if (direction == Direction.SOUTH) {
               k1 = Mth.floor(painting.getX() - (double)((f15 + f16) / 2.0F / 16.0F));
            }

            if (direction == Direction.EAST) {
               i2 = Mth.floor(painting.getZ() + (double)((f15 + f16) / 2.0F / 16.0F));
            }

            int j2 = LevelRenderer.getLightColor(painting.level(), new BlockPos(k1, l1, i2));
            float f19 = textureatlassprite.getU(d0 * (double)(k - i1));
            float f20 = textureatlassprite.getU(d0 * (double)(k - (i1 + 1)));
            float f21 = textureatlassprite.getV(d1 * (double)(l - j1));
            float f22 = textureatlassprite.getV(d1 * (double)(l - (j1 + 1)));
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f18, f20, f21, -0.5F, 0, 0, -1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f18, f19, f21, -0.5F, 0, 0, -1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f17, f19, f22, -0.5F, 0, 0, -1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f17, f20, f22, -0.5F, 0, 0, -1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f17, f4, f5, 0.5F, 0, 0, 1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f17, f3, f5, 0.5F, 0, 0, 1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f18, f3, f6, 0.5F, 0, 0, 1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f18, f4, f6, 0.5F, 0, 0, 1, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f17, f7, f9, -0.5F, 0, 1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f17, f8, f9, -0.5F, 0, 1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f17, f8, f10, 0.5F, 0, 1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f17, f7, f10, 0.5F, 0, 1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f18, f7, f9, 0.5F, 0, -1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f18, f8, f9, 0.5F, 0, -1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f18, f8, f10, -0.5F, 0, -1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f18, f7, f10, -0.5F, 0, -1, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f17, f12, f13, 0.5F, -1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f18, f12, f14, 0.5F, -1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f18, f11, f14, -0.5F, -1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f15, f17, f11, f13, -0.5F, -1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f17, f12, f13, -0.5F, 1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f18, f12, f14, -0.5F, 1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f18, f11, f14, 0.5F, 1, 0, 0, j2);
            this.vertex(matrix4f, matrix3f, vertexconsumer, f16, f17, f11, f13, 0.5F, 1, 0, 0, j2);
         }
      }

   }

   private void vertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, float f4, int i, int j, int k, int l) {
      vertexconsumer.vertex(matrix4f, f, f1, f4).color(255, 255, 255, 255).uv(f2, f3).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(l).normal(matrix3f, (float)i, (float)j, (float)k).endVertex();
   }
}
