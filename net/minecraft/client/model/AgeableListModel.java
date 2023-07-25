package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class AgeableListModel<E extends Entity> extends EntityModel<E> {
   private final boolean scaleHead;
   private final float babyYHeadOffset;
   private final float babyZHeadOffset;
   private final float babyHeadScale;
   private final float babyBodyScale;
   private final float bodyYOffset;

   protected AgeableListModel(boolean flag, float f, float f1) {
      this(flag, f, f1, 2.0F, 2.0F, 24.0F);
   }

   protected AgeableListModel(boolean flag, float f, float f1, float f2, float f3, float f4) {
      this(RenderType::entityCutoutNoCull, flag, f, f1, f2, f3, f4);
   }

   protected AgeableListModel(Function<ResourceLocation, RenderType> function, boolean flag, float f, float f1, float f2, float f3, float f4) {
      super(function);
      this.scaleHead = flag;
      this.babyYHeadOffset = f;
      this.babyZHeadOffset = f1;
      this.babyHeadScale = f2;
      this.babyBodyScale = f3;
      this.bodyYOffset = f4;
   }

   protected AgeableListModel() {
      this(false, 5.0F, 2.0F);
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      if (this.young) {
         posestack.pushPose();
         if (this.scaleHead) {
            float f4 = 1.5F / this.babyHeadScale;
            posestack.scale(f4, f4, f4);
         }

         posestack.translate(0.0F, this.babyYHeadOffset / 16.0F, this.babyZHeadOffset / 16.0F);
         this.headParts().forEach((modelpart3) -> modelpart3.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         posestack.popPose();
         posestack.pushPose();
         float f5 = 1.0F / this.babyBodyScale;
         posestack.scale(f5, f5, f5);
         posestack.translate(0.0F, this.bodyYOffset / 16.0F, 0.0F);
         this.bodyParts().forEach((modelpart2) -> modelpart2.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         posestack.popPose();
      } else {
         this.headParts().forEach((modelpart1) -> modelpart1.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         this.bodyParts().forEach((modelpart) -> modelpart.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
      }

   }

   protected abstract Iterable<ModelPart> headParts();

   protected abstract Iterable<ModelPart> bodyParts();
}
