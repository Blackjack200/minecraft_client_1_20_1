package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;

public class ArrowLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
   private final EntityRenderDispatcher dispatcher;

   public ArrowLayer(EntityRendererProvider.Context entityrendererprovider_context, LivingEntityRenderer<T, M> livingentityrenderer) {
      super(livingentityrenderer);
      this.dispatcher = entityrendererprovider_context.getEntityRenderDispatcher();
   }

   protected int numStuck(T livingentity) {
      return livingentity.getArrowCount();
   }

   protected void renderStuckItem(PoseStack posestack, MultiBufferSource multibuffersource, int i, Entity entity, float f, float f1, float f2, float f3) {
      float f4 = Mth.sqrt(f * f + f2 * f2);
      Arrow arrow = new Arrow(entity.level(), entity.getX(), entity.getY(), entity.getZ());
      arrow.setYRot((float)(Math.atan2((double)f, (double)f2) * (double)(180F / (float)Math.PI)));
      arrow.setXRot((float)(Math.atan2((double)f1, (double)f4) * (double)(180F / (float)Math.PI)));
      arrow.yRotO = arrow.getYRot();
      arrow.xRotO = arrow.getXRot();
      this.dispatcher.render(arrow, 0.0D, 0.0D, 0.0D, 0.0F, f3, posestack, multibuffersource, i);
   }
}
