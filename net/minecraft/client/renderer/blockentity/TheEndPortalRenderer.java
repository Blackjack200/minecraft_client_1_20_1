package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import org.joml.Matrix4f;

public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> implements BlockEntityRenderer<T> {
   public static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
   public static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");

   public TheEndPortalRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
   }

   public void render(T theendportalblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      Matrix4f matrix4f = posestack.last().pose();
      this.renderCube(theendportalblockentity, matrix4f, multibuffersource.getBuffer(this.renderType()));
   }

   private void renderCube(T theendportalblockentity, Matrix4f matrix4f, VertexConsumer vertexconsumer) {
      float f = this.getOffsetDown();
      float f1 = this.getOffsetUp();
      this.renderFace(theendportalblockentity, matrix4f, vertexconsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, Direction.SOUTH);
      this.renderFace(theendportalblockentity, matrix4f, vertexconsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, Direction.NORTH);
      this.renderFace(theendportalblockentity, matrix4f, vertexconsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.EAST);
      this.renderFace(theendportalblockentity, matrix4f, vertexconsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F, Direction.WEST);
      this.renderFace(theendportalblockentity, matrix4f, vertexconsumer, 0.0F, 1.0F, f, f, 0.0F, 0.0F, 1.0F, 1.0F, Direction.DOWN);
      this.renderFace(theendportalblockentity, matrix4f, vertexconsumer, 0.0F, 1.0F, f1, f1, 1.0F, 1.0F, 0.0F, 0.0F, Direction.UP);
   }

   private void renderFace(T theendportalblockentity, Matrix4f matrix4f, VertexConsumer vertexconsumer, float f, float f1, float f2, float f3, float f4, float f5, float f6, float f7, Direction direction) {
      if (theendportalblockentity.shouldRenderFace(direction)) {
         vertexconsumer.vertex(matrix4f, f, f2, f4).endVertex();
         vertexconsumer.vertex(matrix4f, f1, f2, f5).endVertex();
         vertexconsumer.vertex(matrix4f, f1, f3, f6).endVertex();
         vertexconsumer.vertex(matrix4f, f, f3, f7).endVertex();
      }

   }

   protected float getOffsetUp() {
      return 0.75F;
   }

   protected float getOffsetDown() {
      return 0.375F;
   }

   protected RenderType renderType() {
      return RenderType.endPortal();
   }
}
