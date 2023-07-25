package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LecternRenderer implements BlockEntityRenderer<LecternBlockEntity> {
   private final BookModel bookModel;

   public LecternRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.bookModel = new BookModel(blockentityrendererprovider_context.bakeLayer(ModelLayers.BOOK));
   }

   public void render(LecternBlockEntity lecternblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      BlockState blockstate = lecternblockentity.getBlockState();
      if (blockstate.getValue(LecternBlock.HAS_BOOK)) {
         posestack.pushPose();
         posestack.translate(0.5F, 1.0625F, 0.5F);
         float f1 = blockstate.getValue(LecternBlock.FACING).getClockWise().toYRot();
         posestack.mulPose(Axis.YP.rotationDegrees(-f1));
         posestack.mulPose(Axis.ZP.rotationDegrees(67.5F));
         posestack.translate(0.0F, -0.125F, 0.0F);
         this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
         VertexConsumer vertexconsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(multibuffersource, RenderType::entitySolid);
         this.bookModel.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
         posestack.popPose();
      }
   }
}
