package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class EnderDragonRenderer extends EntityRenderer<EnderDragon> {
   public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
   private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
   private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");
   private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(DRAGON_LOCATION);
   private static final RenderType DECAL = RenderType.entityDecal(DRAGON_LOCATION);
   private static final RenderType EYES = RenderType.eyes(DRAGON_EYES_LOCATION);
   private static final RenderType BEAM = RenderType.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
   private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0D) / 2.0D);
   private final EnderDragonRenderer.DragonModel model;

   public EnderDragonRenderer(EntityRendererProvider.Context entityrendererprovider_context) {
      super(entityrendererprovider_context);
      this.shadowRadius = 0.5F;
      this.model = new EnderDragonRenderer.DragonModel(entityrendererprovider_context.bakeLayer(ModelLayers.ENDER_DRAGON));
   }

   public void render(EnderDragon enderdragon, float f, float f1, PoseStack posestack, MultiBufferSource multibuffersource, int i) {
      posestack.pushPose();
      float f2 = (float)enderdragon.getLatencyPos(7, f1)[0];
      float f3 = (float)(enderdragon.getLatencyPos(5, f1)[1] - enderdragon.getLatencyPos(10, f1)[1]);
      posestack.mulPose(Axis.YP.rotationDegrees(-f2));
      posestack.mulPose(Axis.XP.rotationDegrees(f3 * 10.0F));
      posestack.translate(0.0F, 0.0F, 1.0F);
      posestack.scale(-1.0F, -1.0F, 1.0F);
      posestack.translate(0.0F, -1.501F, 0.0F);
      boolean flag = enderdragon.hurtTime > 0;
      this.model.prepareMobModel(enderdragon, 0.0F, 0.0F, f1);
      if (enderdragon.dragonDeathTime > 0) {
         float f4 = (float)enderdragon.dragonDeathTime / 200.0F;
         VertexConsumer vertexconsumer = multibuffersource.getBuffer(RenderType.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION));
         this.model.renderToBuffer(posestack, vertexconsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, f4);
         VertexConsumer vertexconsumer1 = multibuffersource.getBuffer(DECAL);
         this.model.renderToBuffer(posestack, vertexconsumer1, i, OverlayTexture.pack(0.0F, flag), 1.0F, 1.0F, 1.0F, 1.0F);
      } else {
         VertexConsumer vertexconsumer2 = multibuffersource.getBuffer(RENDER_TYPE);
         this.model.renderToBuffer(posestack, vertexconsumer2, i, OverlayTexture.pack(0.0F, flag), 1.0F, 1.0F, 1.0F, 1.0F);
      }

      VertexConsumer vertexconsumer3 = multibuffersource.getBuffer(EYES);
      this.model.renderToBuffer(posestack, vertexconsumer3, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      if (enderdragon.dragonDeathTime > 0) {
         float f5 = ((float)enderdragon.dragonDeathTime + f1) / 200.0F;
         float f6 = Math.min(f5 > 0.8F ? (f5 - 0.8F) / 0.2F : 0.0F, 1.0F);
         RandomSource randomsource = RandomSource.create(432L);
         VertexConsumer vertexconsumer4 = multibuffersource.getBuffer(RenderType.lightning());
         posestack.pushPose();
         posestack.translate(0.0F, -1.0F, -2.0F);

         for(int j = 0; (float)j < (f5 + f5 * f5) / 2.0F * 60.0F; ++j) {
            posestack.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            posestack.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            posestack.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            posestack.mulPose(Axis.XP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            posestack.mulPose(Axis.YP.rotationDegrees(randomsource.nextFloat() * 360.0F));
            posestack.mulPose(Axis.ZP.rotationDegrees(randomsource.nextFloat() * 360.0F + f5 * 90.0F));
            float f7 = randomsource.nextFloat() * 20.0F + 5.0F + f6 * 10.0F;
            float f8 = randomsource.nextFloat() * 2.0F + 1.0F + f6 * 2.0F;
            Matrix4f matrix4f = posestack.last().pose();
            int k = (int)(255.0F * (1.0F - f6));
            vertex01(vertexconsumer4, matrix4f, k);
            vertex2(vertexconsumer4, matrix4f, f7, f8);
            vertex3(vertexconsumer4, matrix4f, f7, f8);
            vertex01(vertexconsumer4, matrix4f, k);
            vertex3(vertexconsumer4, matrix4f, f7, f8);
            vertex4(vertexconsumer4, matrix4f, f7, f8);
            vertex01(vertexconsumer4, matrix4f, k);
            vertex4(vertexconsumer4, matrix4f, f7, f8);
            vertex2(vertexconsumer4, matrix4f, f7, f8);
         }

         posestack.popPose();
      }

      posestack.popPose();
      if (enderdragon.nearestCrystal != null) {
         posestack.pushPose();
         float f9 = (float)(enderdragon.nearestCrystal.getX() - Mth.lerp((double)f1, enderdragon.xo, enderdragon.getX()));
         float f10 = (float)(enderdragon.nearestCrystal.getY() - Mth.lerp((double)f1, enderdragon.yo, enderdragon.getY()));
         float f11 = (float)(enderdragon.nearestCrystal.getZ() - Mth.lerp((double)f1, enderdragon.zo, enderdragon.getZ()));
         renderCrystalBeams(f9, f10 + EndCrystalRenderer.getY(enderdragon.nearestCrystal, f1), f11, f1, enderdragon.tickCount, posestack, multibuffersource, i);
         posestack.popPose();
      }

      super.render(enderdragon, f, f1, posestack, multibuffersource, i);
   }

   private static void vertex01(VertexConsumer vertexconsumer, Matrix4f matrix4f, int i) {
      vertexconsumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(255, 255, 255, i).endVertex();
   }

   private static void vertex2(VertexConsumer vertexconsumer, Matrix4f matrix4f, float f, float f1) {
      vertexconsumer.vertex(matrix4f, -HALF_SQRT_3 * f1, f, -0.5F * f1).color(255, 0, 255, 0).endVertex();
   }

   private static void vertex3(VertexConsumer vertexconsumer, Matrix4f matrix4f, float f, float f1) {
      vertexconsumer.vertex(matrix4f, HALF_SQRT_3 * f1, f, -0.5F * f1).color(255, 0, 255, 0).endVertex();
   }

   private static void vertex4(VertexConsumer vertexconsumer, Matrix4f matrix4f, float f, float f1) {
      vertexconsumer.vertex(matrix4f, 0.0F, f, 1.0F * f1).color(255, 0, 255, 0).endVertex();
   }

   public static void renderCrystalBeams(float f, float f1, float f2, float f3, int i, PoseStack posestack, MultiBufferSource multibuffersource, int j) {
      float f4 = Mth.sqrt(f * f + f2 * f2);
      float f5 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
      posestack.pushPose();
      posestack.translate(0.0F, 2.0F, 0.0F);
      posestack.mulPose(Axis.YP.rotation((float)(-Math.atan2((double)f2, (double)f)) - ((float)Math.PI / 2F)));
      posestack.mulPose(Axis.XP.rotation((float)(-Math.atan2((double)f4, (double)f1)) - ((float)Math.PI / 2F)));
      VertexConsumer vertexconsumer = multibuffersource.getBuffer(BEAM);
      float f6 = 0.0F - ((float)i + f3) * 0.01F;
      float f7 = Mth.sqrt(f * f + f1 * f1 + f2 * f2) / 32.0F - ((float)i + f3) * 0.01F;
      int k = 8;
      float f8 = 0.0F;
      float f9 = 0.75F;
      float f10 = 0.0F;
      PoseStack.Pose posestack_pose = posestack.last();
      Matrix4f matrix4f = posestack_pose.pose();
      Matrix3f matrix3f = posestack_pose.normal();

      for(int l = 1; l <= 8; ++l) {
         float f11 = Mth.sin((float)l * ((float)Math.PI * 2F) / 8.0F) * 0.75F;
         float f12 = Mth.cos((float)l * ((float)Math.PI * 2F) / 8.0F) * 0.75F;
         float f13 = (float)l / 8.0F;
         vertexconsumer.vertex(matrix4f, f8 * 0.2F, f9 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f10, f6).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(j).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f8, f9, f5).color(255, 255, 255, 255).uv(f10, f7).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(j).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f11, f12, f5).color(255, 255, 255, 255).uv(f13, f7).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(j).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
         vertexconsumer.vertex(matrix4f, f11 * 0.2F, f12 * 0.2F, 0.0F).color(0, 0, 0, 255).uv(f13, f6).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(j).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
         f8 = f11;
         f9 = f12;
         f10 = f13;
      }

      posestack.popPose();
   }

   public ResourceLocation getTextureLocation(EnderDragon enderdragon) {
      return DRAGON_LOCATION;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      float f = -16.0F;
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("upperlip", -6.0F, -1.0F, -24.0F, 12, 5, 16, 176, 44).addBox("upperhead", -8.0F, -8.0F, -10.0F, 16, 16, 16, 112, 30).mirror().addBox("scale", -5.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).addBox("nostril", -5.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0).mirror().addBox("scale", 3.0F, -12.0F, -4.0F, 2, 4, 6, 0, 0).addBox("nostril", 3.0F, -3.0F, -22.0F, 2, 2, 4, 112, 0), PartPose.ZERO);
      partdefinition1.addOrReplaceChild("jaw", CubeListBuilder.create().addBox("jaw", -6.0F, 0.0F, -16.0F, 12, 4, 16, 176, 65), PartPose.offset(0.0F, 4.0F, -8.0F));
      partdefinition.addOrReplaceChild("neck", CubeListBuilder.create().addBox("box", -5.0F, -5.0F, -5.0F, 10, 10, 10, 192, 104).addBox("scale", -1.0F, -9.0F, -3.0F, 2, 4, 6, 48, 0), PartPose.ZERO);
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().addBox("body", -12.0F, 0.0F, -16.0F, 24, 24, 64, 0, 0).addBox("scale", -1.0F, -6.0F, -10.0F, 2, 6, 12, 220, 53).addBox("scale", -1.0F, -6.0F, 10.0F, 2, 6, 12, 220, 53).addBox("scale", -1.0F, -6.0F, 30.0F, 2, 6, 12, 220, 53), PartPose.offset(0.0F, 4.0F, 8.0F));
      PartDefinition partdefinition2 = partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().addBox("bone", 0.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88), PartPose.offset(12.0F, 5.0F, 2.0F));
      partdefinition2.addOrReplaceChild("left_wing_tip", CubeListBuilder.create().mirror().addBox("bone", 0.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", 0.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144), PartPose.offset(56.0F, 0.0F, 0.0F));
      PartDefinition partdefinition3 = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), PartPose.offset(12.0F, 20.0F, 2.0F));
      PartDefinition partdefinition4 = partdefinition3.addOrReplaceChild("left_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), PartPose.offset(0.0F, 20.0F, -1.0F));
      partdefinition4.addOrReplaceChild("left_front_foot", CubeListBuilder.create().addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), PartPose.offset(0.0F, 23.0F, 0.0F));
      PartDefinition partdefinition5 = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), PartPose.offset(16.0F, 16.0F, 42.0F));
      PartDefinition partdefinition6 = partdefinition5.addOrReplaceChild("left_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), PartPose.offset(0.0F, 32.0F, -4.0F));
      partdefinition6.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), PartPose.offset(0.0F, 31.0F, 4.0F));
      PartDefinition partdefinition7 = partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8, 112, 88).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 88), PartPose.offset(-12.0F, 5.0F, 2.0F));
      partdefinition7.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4, 112, 136).addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56, -56, 144), PartPose.offset(-56.0F, 0.0F, 0.0F));
      PartDefinition partdefinition8 = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().addBox("main", -4.0F, -4.0F, -4.0F, 8, 24, 8, 112, 104), PartPose.offset(-12.0F, 20.0F, 2.0F));
      PartDefinition partdefinition9 = partdefinition8.addOrReplaceChild("right_front_leg_tip", CubeListBuilder.create().addBox("main", -3.0F, -1.0F, -3.0F, 6, 24, 6, 226, 138), PartPose.offset(0.0F, 20.0F, -1.0F));
      partdefinition9.addOrReplaceChild("right_front_foot", CubeListBuilder.create().addBox("main", -4.0F, 0.0F, -12.0F, 8, 4, 16, 144, 104), PartPose.offset(0.0F, 23.0F, 0.0F));
      PartDefinition partdefinition10 = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().addBox("main", -8.0F, -4.0F, -8.0F, 16, 32, 16, 0, 0), PartPose.offset(-16.0F, 16.0F, 42.0F));
      PartDefinition partdefinition11 = partdefinition10.addOrReplaceChild("right_hind_leg_tip", CubeListBuilder.create().addBox("main", -6.0F, -2.0F, 0.0F, 12, 32, 12, 196, 0), PartPose.offset(0.0F, 32.0F, -4.0F));
      partdefinition11.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().addBox("main", -9.0F, 0.0F, -20.0F, 18, 6, 24, 112, 0), PartPose.offset(0.0F, 31.0F, 4.0F));
      return LayerDefinition.create(meshdefinition, 256, 256);
   }

   public static class DragonModel extends EntityModel<EnderDragon> {
      private final ModelPart head;
      private final ModelPart neck;
      private final ModelPart jaw;
      private final ModelPart body;
      private final ModelPart leftWing;
      private final ModelPart leftWingTip;
      private final ModelPart leftFrontLeg;
      private final ModelPart leftFrontLegTip;
      private final ModelPart leftFrontFoot;
      private final ModelPart leftRearLeg;
      private final ModelPart leftRearLegTip;
      private final ModelPart leftRearFoot;
      private final ModelPart rightWing;
      private final ModelPart rightWingTip;
      private final ModelPart rightFrontLeg;
      private final ModelPart rightFrontLegTip;
      private final ModelPart rightFrontFoot;
      private final ModelPart rightRearLeg;
      private final ModelPart rightRearLegTip;
      private final ModelPart rightRearFoot;
      @Nullable
      private EnderDragon entity;
      private float a;

      public DragonModel(ModelPart modelpart) {
         this.head = modelpart.getChild("head");
         this.jaw = this.head.getChild("jaw");
         this.neck = modelpart.getChild("neck");
         this.body = modelpart.getChild("body");
         this.leftWing = modelpart.getChild("left_wing");
         this.leftWingTip = this.leftWing.getChild("left_wing_tip");
         this.leftFrontLeg = modelpart.getChild("left_front_leg");
         this.leftFrontLegTip = this.leftFrontLeg.getChild("left_front_leg_tip");
         this.leftFrontFoot = this.leftFrontLegTip.getChild("left_front_foot");
         this.leftRearLeg = modelpart.getChild("left_hind_leg");
         this.leftRearLegTip = this.leftRearLeg.getChild("left_hind_leg_tip");
         this.leftRearFoot = this.leftRearLegTip.getChild("left_hind_foot");
         this.rightWing = modelpart.getChild("right_wing");
         this.rightWingTip = this.rightWing.getChild("right_wing_tip");
         this.rightFrontLeg = modelpart.getChild("right_front_leg");
         this.rightFrontLegTip = this.rightFrontLeg.getChild("right_front_leg_tip");
         this.rightFrontFoot = this.rightFrontLegTip.getChild("right_front_foot");
         this.rightRearLeg = modelpart.getChild("right_hind_leg");
         this.rightRearLegTip = this.rightRearLeg.getChild("right_hind_leg_tip");
         this.rightRearFoot = this.rightRearLegTip.getChild("right_hind_foot");
      }

      public void prepareMobModel(EnderDragon enderdragon, float f, float f1, float f2) {
         this.entity = enderdragon;
         this.a = f2;
      }

      public void setupAnim(EnderDragon enderdragon, float f, float f1, float f2, float f3, float f4) {
      }

      public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
         posestack.pushPose();
         float f4 = Mth.lerp(this.a, this.entity.oFlapTime, this.entity.flapTime);
         this.jaw.xRot = (float)(Math.sin((double)(f4 * ((float)Math.PI * 2F))) + 1.0D) * 0.2F;
         float f5 = (float)(Math.sin((double)(f4 * ((float)Math.PI * 2F) - 1.0F)) + 1.0D);
         f5 = (f5 * f5 + f5 * 2.0F) * 0.05F;
         posestack.translate(0.0F, f5 - 2.0F, -3.0F);
         posestack.mulPose(Axis.XP.rotationDegrees(f5 * 2.0F));
         float f6 = 0.0F;
         float f7 = 20.0F;
         float f8 = -12.0F;
         float f9 = 1.5F;
         double[] adouble = this.entity.getLatencyPos(6, this.a);
         float f10 = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] - this.entity.getLatencyPos(10, this.a)[0]));
         float f11 = Mth.wrapDegrees((float)(this.entity.getLatencyPos(5, this.a)[0] + (double)(f10 / 2.0F)));
         float f12 = f4 * ((float)Math.PI * 2F);

         for(int k = 0; k < 5; ++k) {
            double[] adouble1 = this.entity.getLatencyPos(5 - k, this.a);
            float f13 = (float)Math.cos((double)((float)k * 0.45F + f12)) * 0.15F;
            this.neck.yRot = Mth.wrapDegrees((float)(adouble1[0] - adouble[0])) * ((float)Math.PI / 180F) * 1.5F;
            this.neck.xRot = f13 + this.entity.getHeadPartYOffset(k, adouble, adouble1) * ((float)Math.PI / 180F) * 1.5F * 5.0F;
            this.neck.zRot = -Mth.wrapDegrees((float)(adouble1[0] - (double)f11)) * ((float)Math.PI / 180F) * 1.5F;
            this.neck.y = f7;
            this.neck.z = f8;
            this.neck.x = f6;
            f7 += Mth.sin(this.neck.xRot) * 10.0F;
            f8 -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
            f6 -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
            this.neck.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f3);
         }

         this.head.y = f7;
         this.head.z = f8;
         this.head.x = f6;
         double[] adouble2 = this.entity.getLatencyPos(0, this.a);
         this.head.yRot = Mth.wrapDegrees((float)(adouble2[0] - adouble[0])) * ((float)Math.PI / 180F);
         this.head.xRot = Mth.wrapDegrees(this.entity.getHeadPartYOffset(6, adouble, adouble2)) * ((float)Math.PI / 180F) * 1.5F * 5.0F;
         this.head.zRot = -Mth.wrapDegrees((float)(adouble2[0] - (double)f11)) * ((float)Math.PI / 180F);
         this.head.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f3);
         posestack.pushPose();
         posestack.translate(0.0F, 1.0F, 0.0F);
         posestack.mulPose(Axis.ZP.rotationDegrees(-f10 * 1.5F));
         posestack.translate(0.0F, -1.0F, 0.0F);
         this.body.zRot = 0.0F;
         this.body.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f3);
         float f14 = f4 * ((float)Math.PI * 2F);
         this.leftWing.xRot = 0.125F - (float)Math.cos((double)f14) * 0.2F;
         this.leftWing.yRot = -0.25F;
         this.leftWing.zRot = -((float)(Math.sin((double)f14) + 0.125D)) * 0.8F;
         this.leftWingTip.zRot = (float)(Math.sin((double)(f14 + 2.0F)) + 0.5D) * 0.75F;
         this.rightWing.xRot = this.leftWing.xRot;
         this.rightWing.yRot = -this.leftWing.yRot;
         this.rightWing.zRot = -this.leftWing.zRot;
         this.rightWingTip.zRot = -this.leftWingTip.zRot;
         this.renderSide(posestack, vertexconsumer, i, j, f5, this.leftWing, this.leftFrontLeg, this.leftFrontLegTip, this.leftFrontFoot, this.leftRearLeg, this.leftRearLegTip, this.leftRearFoot, f3);
         this.renderSide(posestack, vertexconsumer, i, j, f5, this.rightWing, this.rightFrontLeg, this.rightFrontLegTip, this.rightFrontFoot, this.rightRearLeg, this.rightRearLegTip, this.rightRearFoot, f3);
         posestack.popPose();
         float f15 = -Mth.sin(f4 * ((float)Math.PI * 2F)) * 0.0F;
         f12 = f4 * ((float)Math.PI * 2F);
         f7 = 10.0F;
         f8 = 60.0F;
         f6 = 0.0F;
         adouble = this.entity.getLatencyPos(11, this.a);

         for(int l = 0; l < 12; ++l) {
            adouble2 = this.entity.getLatencyPos(12 + l, this.a);
            f15 += Mth.sin((float)l * 0.45F + f12) * 0.05F;
            this.neck.yRot = (Mth.wrapDegrees((float)(adouble2[0] - adouble[0])) * 1.5F + 180.0F) * ((float)Math.PI / 180F);
            this.neck.xRot = f15 + (float)(adouble2[1] - adouble[1]) * ((float)Math.PI / 180F) * 1.5F * 5.0F;
            this.neck.zRot = Mth.wrapDegrees((float)(adouble2[0] - (double)f11)) * ((float)Math.PI / 180F) * 1.5F;
            this.neck.y = f7;
            this.neck.z = f8;
            this.neck.x = f6;
            f7 += Mth.sin(this.neck.xRot) * 10.0F;
            f8 -= Mth.cos(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
            f6 -= Mth.sin(this.neck.yRot) * Mth.cos(this.neck.xRot) * 10.0F;
            this.neck.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f3);
         }

         posestack.popPose();
      }

      private void renderSide(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, ModelPart modelpart, ModelPart modelpart1, ModelPart modelpart2, ModelPart modelpart3, ModelPart modelpart4, ModelPart modelpart5, ModelPart modelpart6, float f1) {
         modelpart4.xRot = 1.0F + f * 0.1F;
         modelpart5.xRot = 0.5F + f * 0.1F;
         modelpart6.xRot = 0.75F + f * 0.1F;
         modelpart1.xRot = 1.3F + f * 0.1F;
         modelpart2.xRot = -0.5F - f * 0.1F;
         modelpart3.xRot = 0.75F + f * 0.1F;
         modelpart.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f1);
         modelpart1.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f1);
         modelpart4.render(posestack, vertexconsumer, i, j, 1.0F, 1.0F, 1.0F, f1);
      }
   }
}
