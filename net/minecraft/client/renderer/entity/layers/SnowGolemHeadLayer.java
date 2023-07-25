package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowGolemHeadLayer extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
   private final BlockRenderDispatcher blockRenderer;
   private final ItemRenderer itemRenderer;

   public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> renderlayerparent, BlockRenderDispatcher blockrenderdispatcher, ItemRenderer itemrenderer) {
      super(renderlayerparent);
      this.blockRenderer = blockrenderdispatcher;
      this.itemRenderer = itemrenderer;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, SnowGolem snowgolem, float f, float f1, float f2, float f3, float f4, float f5) {
      if (snowgolem.hasPumpkin()) {
         boolean flag = Minecraft.getInstance().shouldEntityAppearGlowing(snowgolem) && snowgolem.isInvisible();
         if (!snowgolem.isInvisible() || flag) {
            posestack.pushPose();
            this.getParentModel().getHead().translateAndRotate(posestack);
            float f6 = 0.625F;
            posestack.translate(0.0F, -0.34375F, 0.0F);
            posestack.mulPose(Axis.YP.rotationDegrees(180.0F));
            posestack.scale(0.625F, -0.625F, -0.625F);
            ItemStack itemstack = new ItemStack(Blocks.CARVED_PUMPKIN);
            if (flag) {
               BlockState blockstate = Blocks.CARVED_PUMPKIN.defaultBlockState();
               BakedModel bakedmodel = this.blockRenderer.getBlockModel(blockstate);
               int j = LivingEntityRenderer.getOverlayCoords(snowgolem, 0.0F);
               posestack.translate(-0.5F, -0.5F, -0.5F);
               this.blockRenderer.getModelRenderer().renderModel(posestack.last(), multibuffersource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), blockstate, bakedmodel, 0.0F, 0.0F, 0.0F, i, j);
            } else {
               this.itemRenderer.renderStatic(snowgolem, itemstack, ItemDisplayContext.HEAD, false, posestack, multibuffersource, snowgolem.level(), i, LivingEntityRenderer.getOverlayCoords(snowgolem, 0.0F), snowgolem.getId());
            }

            posestack.popPose();
         }
      }
   }
}
