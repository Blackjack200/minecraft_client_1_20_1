package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CapeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
   public CapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderlayerparent) {
      super(renderlayerparent);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, AbstractClientPlayer abstractclientplayer, float f, float f1, float f2, float f3, float f4, float f5) {
      if (abstractclientplayer.isCapeLoaded() && !abstractclientplayer.isInvisible() && abstractclientplayer.isModelPartShown(PlayerModelPart.CAPE) && abstractclientplayer.getCloakTextureLocation() != null) {
         ItemStack itemstack = abstractclientplayer.getItemBySlot(EquipmentSlot.CHEST);
         if (!itemstack.is(Items.ELYTRA)) {
            posestack.pushPose();
            posestack.translate(0.0F, 0.0F, 0.125F);
            double d0 = Mth.lerp((double)f2, abstractclientplayer.xCloakO, abstractclientplayer.xCloak) - Mth.lerp((double)f2, abstractclientplayer.xo, abstractclientplayer.getX());
            double d1 = Mth.lerp((double)f2, abstractclientplayer.yCloakO, abstractclientplayer.yCloak) - Mth.lerp((double)f2, abstractclientplayer.yo, abstractclientplayer.getY());
            double d2 = Mth.lerp((double)f2, abstractclientplayer.zCloakO, abstractclientplayer.zCloak) - Mth.lerp((double)f2, abstractclientplayer.zo, abstractclientplayer.getZ());
            float f6 = Mth.rotLerp(f2, abstractclientplayer.yBodyRotO, abstractclientplayer.yBodyRot);
            double d3 = (double)Mth.sin(f6 * ((float)Math.PI / 180F));
            double d4 = (double)(-Mth.cos(f6 * ((float)Math.PI / 180F)));
            float f7 = (float)d1 * 10.0F;
            f7 = Mth.clamp(f7, -6.0F, 32.0F);
            float f8 = (float)(d0 * d3 + d2 * d4) * 100.0F;
            f8 = Mth.clamp(f8, 0.0F, 150.0F);
            float f9 = (float)(d0 * d4 - d2 * d3) * 100.0F;
            f9 = Mth.clamp(f9, -20.0F, 20.0F);
            if (f8 < 0.0F) {
               f8 = 0.0F;
            }

            float f10 = Mth.lerp(f2, abstractclientplayer.oBob, abstractclientplayer.bob);
            f7 += Mth.sin(Mth.lerp(f2, abstractclientplayer.walkDistO, abstractclientplayer.walkDist) * 6.0F) * 32.0F * f10;
            if (abstractclientplayer.isCrouching()) {
               f7 += 25.0F;
            }

            posestack.mulPose(Axis.XP.rotationDegrees(6.0F + f8 / 2.0F + f7));
            posestack.mulPose(Axis.ZP.rotationDegrees(f9 / 2.0F));
            posestack.mulPose(Axis.YP.rotationDegrees(180.0F - f9 / 2.0F));
            VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entitySolid(abstractclientplayer.getCloakTextureLocation()));
            this.getParentModel().renderCloak(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY);
            posestack.popPose();
         }
      }
   }
}
