package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BrushableBlockRenderer implements BlockEntityRenderer<BrushableBlockEntity> {
   private final ItemRenderer itemRenderer;

   public BrushableBlockRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.itemRenderer = blockentityrendererprovider_context.getItemRenderer();
   }

   public void render(BrushableBlockEntity brushableblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      if (brushableblockentity.getLevel() != null) {
         int k = brushableblockentity.getBlockState().getValue(BlockStateProperties.DUSTED);
         if (k > 0) {
            Direction direction = brushableblockentity.getHitDirection();
            if (direction != null) {
               ItemStack itemstack = brushableblockentity.getItem();
               if (!itemstack.isEmpty()) {
                  posestack.pushPose();
                  posestack.translate(0.0F, 0.5F, 0.0F);
                  float[] afloat = this.translations(direction, k);
                  posestack.translate(afloat[0], afloat[1], afloat[2]);
                  posestack.mulPose(Axis.YP.rotationDegrees(75.0F));
                  boolean flag = direction == Direction.EAST || direction == Direction.WEST;
                  posestack.mulPose(Axis.YP.rotationDegrees((float)((flag ? 90 : 0) + 11)));
                  posestack.scale(0.5F, 0.5F, 0.5F);
                  int l = LevelRenderer.getLightColor(brushableblockentity.getLevel(), brushableblockentity.getBlockState(), brushableblockentity.getBlockPos().relative(direction));
                  this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, l, OverlayTexture.NO_OVERLAY, posestack, multibuffersource, brushableblockentity.getLevel(), 0);
                  posestack.popPose();
               }
            }
         }
      }
   }

   private float[] translations(Direction direction, int i) {
      float[] afloat = new float[]{0.5F, 0.0F, 0.5F};
      float f = (float)i / 10.0F * 0.75F;
      switch (direction) {
         case EAST:
            afloat[0] = 0.73F + f;
            break;
         case WEST:
            afloat[0] = 0.25F - f;
            break;
         case UP:
            afloat[1] = 0.25F + f;
            break;
         case DOWN:
            afloat[1] = -0.23F - f;
            break;
         case NORTH:
            afloat[2] = 0.25F - f;
            break;
         case SOUTH:
            afloat[2] = 0.73F + f;
      }

      return afloat;
   }
}
