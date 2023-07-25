package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

public class CampfireRenderer implements BlockEntityRenderer<CampfireBlockEntity> {
   private static final float SIZE = 0.375F;
   private final ItemRenderer itemRenderer;

   public CampfireRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.itemRenderer = blockentityrendererprovider_context.getItemRenderer();
   }

   public void render(CampfireBlockEntity campfireblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      Direction direction = campfireblockentity.getBlockState().getValue(CampfireBlock.FACING);
      NonNullList<ItemStack> nonnulllist = campfireblockentity.getItems();
      int k = (int)campfireblockentity.getBlockPos().asLong();

      for(int l = 0; l < nonnulllist.size(); ++l) {
         ItemStack itemstack = nonnulllist.get(l);
         if (itemstack != ItemStack.EMPTY) {
            posestack.pushPose();
            posestack.translate(0.5F, 0.44921875F, 0.5F);
            Direction direction1 = Direction.from2DDataValue((l + direction.get2DDataValue()) % 4);
            float f1 = -direction1.toYRot();
            posestack.mulPose(Axis.YP.rotationDegrees(f1));
            posestack.mulPose(Axis.XP.rotationDegrees(90.0F));
            posestack.translate(-0.3125F, -0.3125F, 0.0F);
            posestack.scale(0.375F, 0.375F, 0.375F);
            this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, i, j, posestack, multibuffersource, campfireblockentity.getLevel(), k + l);
            posestack.popPose();
         }
      }

   }
}
