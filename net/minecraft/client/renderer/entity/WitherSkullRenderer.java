package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.WitherSkull;

public class WitherSkullRenderer extends EntityRenderer<WitherSkull> {
   private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = new ResourceLocation("textures/entity/wither/wither_invulnerable.png");
   private static final ResourceLocation WITHER_LOCATION = new ResourceLocation("textures/entity/wither/wither.png");
   private final SkullModel model;

   public WitherSkullRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.model = new SkullModel(entityrendererprovider_context.bakeLayer(ModelLayers.WITHER_SKULL));
   }

   public static LayerDefinition createSkullLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   protected int getBlockLightLevel(WitherSkull witherskull, BlockPos blockpos) {
      return 15;
   }

   public void render(WitherSkull witherskull, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      posestack.scale(-1.0F, -1.0F, 1.0F);
      float f2 = Mth.rotLerp(f1, witherskull.yRotO, witherskull.getYRot());
      float f3 = Mth.lerp(f1, witherskull.xRotO, witherskull.getXRot());
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(this.model.renderType(this.getTextureLocation(witherskull)));
      this.model.setupAnim(0.0F, f2, f3);
      this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      posestack.popPose();
      super.render(witherskull, f, f1, posestack, multibuffersource, i);
   }

   public ResourceLocation getTextureLocation(WitherSkull witherskull) {
      return witherskull.isDangerous() ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
   }
}
