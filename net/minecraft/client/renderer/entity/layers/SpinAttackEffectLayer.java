package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class SpinAttackEffectLayer<T extends LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
   public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/trident_riptide.png");
   public static final String BOX = "box";
   private final ModelPart box;

   public SpinAttackEffectLayer(RenderLayerParent<T, PlayerModel<T>> renderlayerparent, EntityModelSet entitymodelset) {
      super(renderlayerparent);
      ModelPart modelpart = entitymodelset.bakeLayer(ModelLayers.PLAYER_SPIN_ATTACK);
      this.box = modelpart.getChild("box");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void render(PoseStack posestack, MultiBufferSource multibuffersource, int i, T livingentity, float f, float f1, float f2, float f3, float f4, float f5) {
      if (livingentity.isAutoSpinAttack()) {
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

         for(int j = 0; j < 3; ++j) {
            posestack.pushPose();
            float f6 = f3 * (float)(-(45 + j * 5));
            posestack.mulPose(Axis.YP.rotationDegrees(f6));
            float f7 = 0.75F * (float)j;
            posestack.scale(f7, f7, f7);
            posestack.translate(0.0F, -0.2F + 0.6F * (float)j, 0.0F);
            this.box.render(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY);
            posestack.popPose();
         }

      }
   }
}
