package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;

public class HorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {
   private final HorseModel<Horse> model;

   public HorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new HorseModel<>(entitymodelset.bakeLayer(ModelLayers.HORSE_ARMOR));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, Horse horse, float f, float f1, float f2, float f3, float f4, float f5) {
      ItemStack itemstack = horse.getArmor();
      if (itemstack.getItem() instanceof HorseArmorItem) {
         HorseArmorItem horsearmoritem = (HorseArmorItem)itemstack.getItem();
         this.getParentModel().copyPropertiesTo(this.model);
         this.model.prepareMobModel(horse, f, f1, f2);
         this.model.setupAnim(horse, f, f1, f3, f4, f5);
         float f6;
         float f7;
         float f8;
         if (horsearmoritem instanceof DyeableHorseArmorItem) {
            int j = ((DyeableHorseArmorItem)horsearmoritem).getColor(itemstack);
            f6 = (float)(j >> 16 & 255) / 255.0F;
            f7 = (float)(j >> 8 & 255) / 255.0F;
            f8 = (float)(j & 255) / 255.0F;
         } else {
            f6 = 1.0F;
            f7 = 1.0F;
            f8 = 1.0F;
         }

         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutoutNoCull(horsearmoritem.getTexture()));
         this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, f6, f7, f8, 1.0F);
      }
   }
}
