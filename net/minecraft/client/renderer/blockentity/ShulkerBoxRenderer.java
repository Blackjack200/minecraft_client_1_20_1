package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ShulkerBoxRenderer implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
   private final ShulkerModel<?> model;

   public ShulkerBoxRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.model = new ShulkerModel(blockentityrendererprovider_context.bakeLayer(ModelLayers.SHULKER));
   }

   public void render(ShulkerBoxBlockEntity shulkerboxblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      Direction direction = Direction.UP;
      if (shulkerboxblockentity.hasLevel()) {
         BlockState blockstate = shulkerboxblockentity.getLevel().getBlockState(shulkerboxblockentity.getBlockPos());
         if (blockstate.getBlock() instanceof ShulkerBoxBlock) {
            direction = blockstate.getValue(ShulkerBoxBlock.FACING);
         }
      }

      DyeColor dyecolor = shulkerboxblockentity.getColor();
      Material material;
      if (dyecolor == null) {
         material = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
      } else {
         material = Sheets.SHULKER_TEXTURE_LOCATION.get(dyecolor.getId());
      }

      posestack.pushPose();
      posestack.translate(0.5F, 0.5F, 0.5F);
      float f1 = 0.9995F;
      posestack.scale(0.9995F, 0.9995F, 0.9995F);
      posestack.mulPose(direction.getRotation());
      posestack.scale(1.0F, -1.0F, -1.0F);
      posestack.translate(0.0F, -1.0F, 0.0F);
      ModelPart modelpart = this.model.getLid();
      modelpart.setPos(0.0F, 24.0F - shulkerboxblockentity.getProgress(f) * 0.5F * 16.0F, 0.0F);
      modelpart.yRot = 270.0F * shulkerboxblockentity.getProgress(f) * ((float)Math.PI / 180F);
      VertexConsumer vertexconsumer = material.buffer(multibuffersource, RenderType::entityCutoutNoCull);
      this.model.renderToBuffer(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
   }
}
