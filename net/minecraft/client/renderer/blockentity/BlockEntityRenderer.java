package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public interface BlockEntityRenderer<T extends BlockEntity> {
   void render(T blockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j);

   default boolean shouldRenderOffScreen(T blockentity) {
      return false;
   }

   default int getViewDistance() {
      return 64;
   }

   default boolean shouldRender(T blockentity, Vec3 vec3) {
      return Vec3.atCenterOf(blockentity.getBlockPos()).closerThan(vec3, (double)this.getViewDistance());
   }
}
