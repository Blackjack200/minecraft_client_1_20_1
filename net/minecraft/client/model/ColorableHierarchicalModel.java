package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;

public abstract class ColorableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
   private float r = 1.0F;
   private float g = 1.0F;
   private float b = 1.0F;

   public void setColor(float f, float f1, float f2) {
      this.r = f;
      this.g = f1;
      this.b = f2;
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      super.renderToBuffer(posestack, vertexconsumer, i, j, this.r * f, this.g * f1, this.b * f2, f3);
   }
}
