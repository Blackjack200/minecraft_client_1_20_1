package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.level.block.state.BlockState;

public class MushroomCowMushroomLayer<T extends MushroomCow> extends RenderLayer<T, CowModel<T>> {
   private final BlockRenderDispatcher blockRenderer;

   public MushroomCowMushroomLayer(RenderLayerParent<T, CowModel<T>> renderlayerparent, BlockRenderDispatcher blockrenderdispatcher) {
      super(renderlayerparent);
      this.blockRenderer = blockrenderdispatcher;
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T mushroomcow, float f, float f1, float f2, float f3, float f4, float f5) {
      if (!mushroomcow.isBaby()) {
         Minecraft minecraft = Minecraft.getInstance();
         boolean flag = minecraft.shouldEntityAppearGlowing(mushroomcow) && mushroomcow.isInvisible();
         if (!mushroomcow.isInvisible() || flag) {
            BlockState blockstate = mushroomcow.getVariant().getBlockState();
            int j = LivingEntityRenderer.getOverlayCoords(mushroomcow, 0.0F);
            BakedModel bakedmodel = this.blockRenderer.getBlockModel(blockstate);
            posestack.pushPose();
            posestack.translate(0.2F, -0.35F, 0.5F);
            posestack.mulPose(Axis.YP.rotationDegrees(-48.0F));
            posestack.scale(-1.0F, -1.0F, 1.0F);
            posestack.translate(-0.5F, -0.5F, -0.5F);
            this.renderMushroomBlock(posestack, multibuffersource, i, flag, blockstate, j, bakedmodel);
            posestack.popPose();
            posestack.pushPose();
            posestack.translate(0.2F, -0.35F, 0.5F);
            posestack.mulPose(Axis.YP.rotationDegrees(42.0F));
            posestack.translate(0.1F, 0.0F, -0.6F);
            posestack.mulPose(Axis.YP.rotationDegrees(-48.0F));
            posestack.scale(-1.0F, -1.0F, 1.0F);
            posestack.translate(-0.5F, -0.5F, -0.5F);
            this.renderMushroomBlock(posestack, multibuffersource, i, flag, blockstate, j, bakedmodel);
            posestack.popPose();
            posestack.pushPose();
            this.getParentModel().getHead().translateAndRotate(posestack);
            posestack.translate(0.0F, -0.7F, -0.2F);
            posestack.mulPose(Axis.YP.rotationDegrees(-78.0F));
            posestack.scale(-1.0F, -1.0F, 1.0F);
            posestack.translate(-0.5F, -0.5F, -0.5F);
            this.renderMushroomBlock(posestack, multibuffersource, i, flag, blockstate, j, bakedmodel);
            posestack.popPose();
         }
      }
   }

   private void renderMushroomBlock(PoseStack posestack, MultiBufferSource multibuffersource, int i, boolean flag, BlockState blockstate, int j, BakedModel bakedmodel) {
      if (flag) {
         this.blockRenderer.getModelRenderer().renderModel(posestack.last(), multibuffersource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), blockstate, bakedmodel, 0.0F, 0.0F, 0.0F, i, j);
      } else {
         this.blockRenderer.renderSingleBlock(blockstate, posestack, multibuffersource, i, j);
      }

   }
}
