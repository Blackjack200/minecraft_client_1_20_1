package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public abstract class ListModel<E extends Entity> extends EntityModel<E> {
   public ListModel() {
      this(RenderType::entityCutoutNoCull);
   }

   public ListModel(Function<ResourceLocation, RenderType> function) {
      super(function);
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      this.parts().forEach((modelpart) -> modelpart.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
   }

   public abstract Iterable<ModelPart> parts();
}
