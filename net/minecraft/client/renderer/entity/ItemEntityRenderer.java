package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
   private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
   private static final int ITEM_COUNT_FOR_5_BUNDLE = 48;
   private static final int ITEM_COUNT_FOR_4_BUNDLE = 32;
   private static final int ITEM_COUNT_FOR_3_BUNDLE = 16;
   private static final int ITEM_COUNT_FOR_2_BUNDLE = 1;
   private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
   private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
   private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
   private final ItemRenderer itemRenderer;
   private final RandomSource random = RandomSource.create();

   public ItemEntityRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.itemRenderer = entityrendererprovider_context.getItemRenderer();
      this.shadowRadius = 0.15F;
      this.shadowStrength = 0.75F;
   }

   private int getRenderAmount(ItemStack itemstack) {
      int i = 1;
      if (itemstack.getCount() > 48) {
         i = 5;
      } else if (itemstack.getCount() > 32) {
         i = 4;
      } else if (itemstack.getCount() > 16) {
         i = 3;
      } else if (itemstack.getCount() > 1) {
         i = 2;
      }

      return i;
   }

   public void render(ItemEntity itementity, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      ItemStack itemstack = itementity.getItem();
      int j = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
      this.random.setSeed((long)j);
      BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, itementity.level(), (LivingEntity)null, itementity.getId());
      boolean flag = bakedmodel.isGui3d();
      int k = this.getRenderAmount(itemstack);
      float f2 = 0.25F;
      float f3 = Mth.sin(((float)itementity.getAge() + f1) / 10.0F + itementity.bobOffs) * 0.1F + 0.1F;
      float f4 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
      posestack.translate(0.0F, f3 + 0.25F * f4, 0.0F);
      float f5 = itementity.getSpin(f1);
      posestack.mulPose(Axis.YP.rotation(f5));
      float f6 = bakedmodel.getTransforms().ground.scale.x();
      float f7 = bakedmodel.getTransforms().ground.scale.y();
      float f8 = bakedmodel.getTransforms().ground.scale.z();
      if (!flag) {
         float f9 = -0.0F * (float)(k - 1) * 0.5F * f6;
         float f10 = -0.0F * (float)(k - 1) * 0.5F * f7;
         float f11 = -0.09375F * (float)(k - 1) * 0.5F * f8;
         posestack.translate(f9, f10, f11);
      }

      for(int l = 0; l < k; ++l) {
         posestack.pushPose();
         if (l > 0) {
            if (flag) {
               float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               posestack.translate(f12, f13, f14);
            } else {
               float f15 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               float f16 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               posestack.translate(f15, f16, 0.0F);
            }
         }

         this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, posestack, multibuffersource, i, OverlayTexture.NO_OVERLAY, bakedmodel);
         posestack.popPose();
         if (!flag) {
            posestack.translate(0.0F * f6, 0.0F * f7, 0.09375F * f8);
         }
      }

      posestack.popPose();
      super.render(itementity, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(ItemEntity itementity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
