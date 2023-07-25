package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class AgeableHierarchicalModel<E extends Entity> extends HierarchicalModel<E> {
   private final float youngScaleFactor;
   private final float bodyYOffset;

   public AgeableHierarchicalModel(float f, float f1) {
      this(f, f1, RenderType::entityCutoutNoCull);
   }

   public AgeableHierarchicalModel(float f, float f1, Function<ResourceLocation, RenderType> function) {
      super(function);
      this.bodyYOffset = f1;
      this.youngScaleFactor = f;
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      if (this.young) {
         posestack.pushPose();
         posestack.scale(this.youngScaleFactor, this.youngScaleFactor, this.youngScaleFactor);
         posestack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
         this.root().render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
         posestack.popPose();
      } else {
         this.root().render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
      }

   }
}
