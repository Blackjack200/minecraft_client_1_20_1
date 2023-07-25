package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public class SpawnerRenderer implements BlockEntityRenderer<SpawnerBlockEntity> {
   private final EntityRenderDispatcher entityRenderer;

   public SpawnerRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.entityRenderer = blockentityrendererprovider_context.getEntityRenderer();
   }

   public void render(SpawnerBlockEntity spawnerblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      posestack.pushPose();
      posestack.translate(0.5F, 0.0F, 0.5F);
      BaseSpawner basespawner = spawnerblockentity.getSpawner();
      Entity entity = basespawner.getOrCreateDisplayEntity(spawnerblockentity.getLevel(), spawnerblockentity.getLevel().getRandom(), spawnerblockentity.getBlockPos());
      if (entity != null) {
         float f1 = 0.53125F;
         float f2 = Math.max(entity.getBbWidth(), entity.getBbHeight());
         if ((double)f2 > 1.0D) {
            f1 /= f2;
         }

         posestack.translate(0.0F, 0.4F, 0.0F);
         posestack.mulPose(Axis.YP.rotationDegrees((float)Mth.lerp((double)f, basespawner.getoSpin(), basespawner.getSpin()) * 10.0F));
         posestack.translate(0.0F, -0.2F, 0.0F);
         posestack.mulPose(Axis.XP.rotationDegrees(-30.0F));
         posestack.scale(f1, f1, f1);
         this.entityRenderer.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, f, posestack, multibuffersource, i);
      }

      posestack.popPose();
   }
}
