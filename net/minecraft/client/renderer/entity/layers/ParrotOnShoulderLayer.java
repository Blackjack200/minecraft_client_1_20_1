package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;

public class ParrotOnShoulderLayer<T extends Player> extends RenderLayer<T, PlayerModel<T>> {
   private final ParrotModel model;

   public ParrotOnShoulderLayer(RenderLayerParent<T, PlayerModel<T>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      this.model = new ParrotModel(entitymodelset.bakeLayer(ModelLayers.PARROT));
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T player, float f, float f1, float f2, float f3, float f4, float f5) {
      this.render(posestack, multibuffersource, i, player, f, f1, f4, f5, true);
      this.render(posestack, multibuffersource, i, player, f, f1, f4, f5, false);
   }

   private void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T player, float f, float f1, float f2, float f3, boolean flag) {
      CompoundTag compoundtag = flag ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
      EntityType.byString(compoundtag.getString("id")).filter((entitytype1) -> entitytype1 == EntityType.PARROT).ifPresent((entitytype) -> {
         posestack.pushPose();
         posestack.translate(flag ? 0.4F : -0.4F, player.isCrouching() ? -1.3F : -1.5F, 0.0F);
         Parrot.Variant parrot_variant = Parrot.Variant.byId(compoundtag.getInt("Variant"));
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(parrot_variant)));
         this.model.renderOnShoulder(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, f, f1, f2, f3, player.tickCount);
         posestack.popPose();
      });
   }
}
