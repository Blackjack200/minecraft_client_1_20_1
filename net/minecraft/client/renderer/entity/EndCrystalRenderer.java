package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.joml.Quaternionf;

public class EndCrystalRenderer extends EntityRenderer<EndCrystal> {
   private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
   private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
   private static final String GLASS = "glass";
   private static final String BASE = "base";
   private final ModelPart cube;
   private final ModelPart glass;
   private final ModelPart base;

   public EndCrystalRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.5F;
      ModelPart modelpart = entityrendererprovider_context.bakeLayer(ModelLayers.END_CRYSTAL);
      this.glass = modelpart.getChild("glass");
      this.cube = modelpart.getChild("cube");
      this.base = modelpart.getChild("base");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("glass", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void render(EndCrystal endcrystal, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      float f2 = getY(endcrystal, f1);
      float f3 = ((float)endcrystal.time + f1) * 3.0F;
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(RENDER_TYPE);
      posestack.pushPose();
      posestack.scale(2.0F, 2.0F, 2.0F);
      posestack.translate(0.0F, -0.5F, 0.0F);
      int j = OverlayTexture.NO_OVERLAY;
      if (endcrystal.showsBottom()) {
         this.base.render(posestack, vertexconsumer, i, j);
      }

      posestack.mulPose(Axis.YP.rotationDegrees(f3));
      posestack.translate(0.0F, 1.5F + f2 / 2.0F, 0.0F);
      posestack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
      this.glass.render(posestack, vertexconsumer, i, j);
      float f4 = 0.875F;
      posestack.scale(0.875F, 0.875F, 0.875F);
      posestack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
      posestack.mulPose(Axis.YP.rotationDegrees(f3));
      this.glass.render(posestack, vertexconsumer, i, j);
      posestack.scale(0.875F, 0.875F, 0.875F);
      posestack.mulPose((new Quaternionf()).setAngleAxis(((float)Math.PI / 3F), SIN_45, 0.0F, SIN_45));
      posestack.mulPose(Axis.YP.rotationDegrees(f3));
      this.cube.render(posestack, vertexconsumer, i, j);
      posestack.popPose();
      posestack.popPose();
      BlockPos blockpos = endcrystal.getBeamTarget();
      if (blockpos != null) {
         float f5 = (float)blockpos.getX() + 0.5F;
         float f6 = (float)blockpos.getY() + 0.5F;
         float f7 = (float)blockpos.getZ() + 0.5F;
         float f8 = (float)((double)f5 - endcrystal.getX());
         float f9 = (float)((double)f6 - endcrystal.getY());
         float f10 = (float)((double)f7 - endcrystal.getZ());
         posestack.translate(f8, f9, f10);
         EnderDragonRenderer.renderCrystalBeams(-f8, -f9 + f2, -f10, f1, endcrystal.time, posestack, multibuffersource, i);
      }

      super.render(endcrystal, f, f1, posestack, multibuffersource, i);
   }

   public static float getY(EndCrystal endcrystal, float f) {
      float f1 = (float)endcrystal.time + f;
      float f2 = Mth.sin(f1 * 0.2F) / 2.0F + 0.5F;
      f2 = (f2 * f2 + f2) * 0.4F;
      return f2 - 1.4F;
   }

   public ResourceLocation getTextureLocation(EndCrystal endcrystal) {
      return END_CRYSTAL_LOCATION;
   }

   public boolean shouldRender(EndCrystal endcrystal, Frustum frustum, double d0, double d1, double d2) {
      return super.shouldRender(endcrystal, frustum, d0, d1, d2) || endcrystal.getBeamTarget() != null;
   }
}
