package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class StuckInBodyLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
   public StuckInBodyLayer(LivingEntityRenderer<T, M> livingentityrenderer) {
      super(livingentityrenderer);
   }

   protected abstract int numStuck(T livingentity);

   protected abstract void renderStuckItem(PoseStack posestack, MultiBufferSource multibuffersource, int i, Entity entity, float f, float f1, float f2, float f3);

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      int j = this.numStuck(livingentity);
      RandomSource randomsource = RandomSource.create((long)livingentity.getId());
      if (j > 0) {
         for(int k = 0; k < j; ++k) {
            posestack.pushPose();
            ModelPart modelpart = this.getParentModel().getRandomModelPart(randomsource);
            ModelPart.Cube modelpart_cube = modelpart.getRandomCube(randomsource);
            modelpart.translateAndRotate(posestack);
            float f6 = randomsource.nextFloat();
            float f7 = randomsource.nextFloat();
            float f8 = randomsource.nextFloat();
            float f9 = Mth.lerp(f6, modelpart_cube.minX, modelpart_cube.maxX) / 16.0F;
            float f10 = Mth.lerp(f7, modelpart_cube.minY, modelpart_cube.maxY) / 16.0F;
            float f11 = Mth.lerp(f8, modelpart_cube.minZ, modelpart_cube.maxZ) / 16.0F;
            posestack.translate(f9, f10, f11);
            f6 = -1.0F * (f6 * 2.0F - 1.0F);
            f7 = -1.0F * (f7 * 2.0F - 1.0F);
            f8 = -1.0F * (f8 * 2.0F - 1.0F);
            this.renderStuckItem(posestack, multibuffersource, i, livingentity, f6, f7, f8, f2);
            posestack.popPose();
         }

      }
   }
}
