package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public abstract class HierarchicalModel<E extends Entity> extends EntityModel<E> {
   private static final Vector3f ANIMATION_VECTOR_CACHE = new Vector3f();

   public HierarchicalModel() {
      this(RenderType::entityCutoutNoCull);
   }

   public HierarchicalModel(Function<ResourceLocation, RenderType> function) {
      super(function);
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      this.root().render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
   }

   public abstract ModelPart root();

   public Optional<ModelPart> getAnyDescendantWithName(String s) {
      return s.equals("root") ? Optional.of(this.root()) : this.root().getAllParts().filter((modelpart1) -> modelpart1.hasChild(s)).findFirst().map((modelpart) -> modelpart.getChild(s));
   }

   protected void animate(AnimationState animationstate, AnimationDefinition animationdefinition, float f) {
      this.animate(animationstate, animationdefinition, f, 1.0F);
   }

   protected void animateWalk(AnimationDefinition animationdefinition, float f, float f1, float f2, float f3) {
      long i = (long)(f * 50.0F * f2);
      float f4 = Math.min(f1 * f3, 1.0F);
      KeyframeAnimations.animate(this, animationdefinition, i, f4, ANIMATION_VECTOR_CACHE);
   }

   protected void animate(AnimationState animationstate, AnimationDefinition animationdefinition, float f, float f1) {
      animationstate.updateTime(f, f1);
      animationstate.ifStarted((animationstate1) -> KeyframeAnimations.animate(this, animationdefinition, animationstate1.getAccumulatedTime(), 1.0F, ANIMATION_VECTOR_CACHE));
   }

   protected void applyStatic(AnimationDefinition animationdefinition) {
      KeyframeAnimations.animate(this, animationdefinition, 0L, 1.0F, ANIMATION_VECTOR_CACHE);
   }
}
