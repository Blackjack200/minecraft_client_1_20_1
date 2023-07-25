package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Rabbit;

public class RabbitModel<T extends Rabbit> extends EntityModel<T> {
   private static final float REAR_JUMP_ANGLE = 50.0F;
   private static final float FRONT_JUMP_ANGLE = -40.0F;
   private static final String LEFT_HAUNCH = "left_haunch";
   private static final String RIGHT_HAUNCH = "right_haunch";
   private final ModelPart leftRearFoot;
   private final ModelPart rightRearFoot;
   private final ModelPart leftHaunch;
   private final ModelPart rightHaunch;
   private final ModelPart body;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart head;
   private final ModelPart rightEar;
   private final ModelPart leftEar;
   private final ModelPart tail;
   private final ModelPart nose;
   private float jumpRotation;
   private static final float NEW_SCALE = 0.6F;

   public RabbitModel(ModelPart modelpart) {
      this.leftRearFoot = modelpart.getChild("left_hind_foot");
      this.rightRearFoot = modelpart.getChild("right_hind_foot");
      this.leftHaunch = modelpart.getChild("left_haunch");
      this.rightHaunch = modelpart.getChild("right_haunch");
      this.body = modelpart.getChild("body");
      this.leftFrontLeg = modelpart.getChild("left_front_leg");
      this.rightFrontLeg = modelpart.getChild("right_front_leg");
      this.head = modelpart.getChild("head");
      this.rightEar = modelpart.getChild("right_ear");
      this.leftEar = modelpart.getChild("left_ear");
      this.tail = modelpart.getChild("tail");
      this.nose = modelpart.getChild("nose");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("left_hind_foot", CubeListBuilder.create().texOffs(26, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), PartPose.offset(3.0F, 17.5F, 3.7F));
      partdefinition.addOrReplaceChild("right_hind_foot", CubeListBuilder.create().texOffs(8, 24).addBox(-1.0F, 5.5F, -3.7F, 2.0F, 1.0F, 7.0F), PartPose.offset(-3.0F, 17.5F, 3.7F));
      partdefinition.addOrReplaceChild("left_haunch", CubeListBuilder.create().texOffs(30, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(3.0F, 17.5F, 3.7F, -0.34906584F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_haunch", CubeListBuilder.create().texOffs(16, 15).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(-3.0F, 17.5F, 3.7F, -0.34906584F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -10.0F, 6.0F, 5.0F, 10.0F), PartPose.offsetAndRotation(0.0F, 19.0F, 8.0F, -0.34906584F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(8, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 17.0F, -1.0F, -0.17453292F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 17.0F, -1.0F, -0.17453292F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-2.5F, -4.0F, -5.0F, 5.0F, 4.0F, 5.0F), PartPose.offset(0.0F, 16.0F, -1.0F));
      partdefinition.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(52, 0).addBox(-2.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.0F, -0.2617994F, 0.0F));
      partdefinition.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(58, 0).addBox(0.5F, -9.0F, -1.0F, 2.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F, 0.0F, 0.2617994F, 0.0F));
      partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(52, 6).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 20.0F, 7.0F, -0.3490659F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(32, 9).addBox(-0.5F, -2.5F, -5.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 16.0F, -1.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      if (this.young) {
         float f4 = 1.5F;
         posestack.pushPose();
         posestack.scale(0.56666666F, 0.56666666F, 0.56666666F);
         posestack.translate(0.0F, 1.375F, 0.125F);
         ImmutableList.of(this.head, this.leftEar, this.rightEar, this.nose).forEach((modelpart2) -> modelpart2.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         posestack.popPose();
         posestack.pushPose();
         posestack.scale(0.4F, 0.4F, 0.4F);
         posestack.translate(0.0F, 2.25F, 0.0F);
         ImmutableList.of(this.leftRearFoot, this.rightRearFoot, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.tail).forEach((modelpart1) -> modelpart1.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         posestack.popPose();
      } else {
         posestack.pushPose();
         posestack.scale(0.6F, 0.6F, 0.6F);
         posestack.translate(0.0F, 1.0F, 0.0F);
         ImmutableList.of(this.leftRearFoot, this.rightRearFoot, this.leftHaunch, this.rightHaunch, this.body, this.leftFrontLeg, this.rightFrontLeg, this.head, this.rightEar, this.leftEar, this.tail, this.nose).forEach((modelpart) -> modelpart.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         posestack.popPose();
      }

   }

   public void setupAnim(T rabbit, float f, float f1, float f2, float f3, float f4) {
      float f5 = f2 - (float)rabbit.tickCount;
      this.nose.xRot = f4 * ((float)Math.PI / 180F);
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      this.rightEar.xRot = f4 * ((float)Math.PI / 180F);
      this.leftEar.xRot = f4 * ((float)Math.PI / 180F);
      this.nose.yRot = f3 * ((float)Math.PI / 180F);
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.rightEar.yRot = this.nose.yRot - 0.2617994F;
      this.leftEar.yRot = this.nose.yRot + 0.2617994F;
      this.jumpRotation = Mth.sin(rabbit.getJumpCompletion(f5) * (float)Math.PI);
      this.leftHaunch.xRot = (this.jumpRotation * 50.0F - 21.0F) * ((float)Math.PI / 180F);
      this.rightHaunch.xRot = (this.jumpRotation * 50.0F - 21.0F) * ((float)Math.PI / 180F);
      this.leftRearFoot.xRot = this.jumpRotation * 50.0F * ((float)Math.PI / 180F);
      this.rightRearFoot.xRot = this.jumpRotation * 50.0F * ((float)Math.PI / 180F);
      this.leftFrontLeg.xRot = (this.jumpRotation * -40.0F - 11.0F) * ((float)Math.PI / 180F);
      this.rightFrontLeg.xRot = (this.jumpRotation * -40.0F - 11.0F) * ((float)Math.PI / 180F);
   }

   public void prepareMobModel(T rabbit, float f, float f1, float f2) {
      super.prepareMobModel(rabbit, f, f1, f2);
      this.jumpRotation = Mth.sin(rabbit.getJumpCompletion(f2) * (float)Math.PI);
   }
}
