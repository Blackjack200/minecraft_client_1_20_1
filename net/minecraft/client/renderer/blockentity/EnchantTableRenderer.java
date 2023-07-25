package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;

public class EnchantTableRenderer implements BlockEntityRenderer<EnchantmentTableBlockEntity> {
   public static final Material BOOK_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("entity/enchanting_table_book"));
   private final BookModel bookModel;

   public EnchantTableRenderer(BlockEntityRendererProvider.Context blockentityrendererprovider_context) {
      this.bookModel = new BookModel(blockentityrendererprovider_context.bakeLayer(ModelLayers.BOOK));
   }

   public void render(EnchantmentTableBlockEntity enchantmenttableblockentity, float f, PoseStack posestack, MultiBufferSource multibuffersource, int i, int j) {
      posestack.pushPose();
      posestack.translate(0.5F, 0.75F, 0.5F);
      float f1 = (float)enchantmenttableblockentity.time + f;
      posestack.translate(0.0F, 0.1F + Mth.sin(f1 * 0.1F) * 0.01F, 0.0F);

      float f2;
      for(f2 = enchantmenttableblockentity.rot - enchantmenttableblockentity.oRot; f2 >= (float)Math.PI; f2 -= ((float)Math.PI * 2F)) {
      }

      while(f2 < -(float)Math.PI) {
         f2 += ((float)Math.PI * 2F);
      }

      float f3 = enchantmenttableblockentity.oRot + f2 * f;
      posestack.mulPose(Axis.YP.rotation(-f3));
      posestack.mulPose(Axis.ZP.rotationDegrees(80.0F));
      float f4 = Mth.lerp(f, enchantmenttableblockentity.oFlip, enchantmenttableblockentity.flip);
      float f5 = Mth.frac(f4 + 0.25F) * 1.6F - 0.3F;
      float f6 = Mth.frac(f4 + 0.75F) * 1.6F - 0.3F;
      float f7 = Mth.lerp(f, enchantmenttableblockentity.oOpen, enchantmenttableblockentity.open);
      this.bookModel.setupAnim(f1, Mth.clamp(f5, 0.0F, 1.0F), Mth.clamp(f6, 0.0F, 1.0F), f7);
      VertexConsumer vertexconsumer = BOOK_LOCATION.buffer(multibuffersource, RenderType::entitySolid);
      this.bookModel.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
   }
}
