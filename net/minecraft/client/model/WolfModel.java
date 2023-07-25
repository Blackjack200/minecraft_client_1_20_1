package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;

public class WolfModel<T extends Wolf> extends ColorableAgeableListModel<T> {
   private static final String REAL_HEAD = "real_head";
   private static final String UPPER_BODY = "upper_body";
   private static final String REAL_TAIL = "real_tail";
   private final ModelPart head;
   private final ModelPart realHead;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart tail;
   private final ModelPart realTail;
   private final ModelPart upperBody;
   private static final int LEG_SIZE = 8;

   public WolfModel(ModelPart modelpart) {
      this.head = modelpart.getChild("head");
      this.realHead = this.head.getChild("real_head");
      this.body = modelpart.getChild("body");
      this.upperBody = modelpart.getChild("upper_body");
      this.rightHindLeg = modelpart.getChild("right_hind_leg");
      this.leftHindLeg = modelpart.getChild("left_hind_leg");
      this.rightFrontLeg = modelpart.getChild("right_front_leg");
      this.leftFrontLeg = modelpart.getChild("left_front_leg");
      this.tail = modelpart.getChild("tail");
      this.realTail = this.tail.getChild("real_tail");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      float f = 13.5F;
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(-1.0F, 13.5F, -7.0F));
      partdefinition1.addOrReplaceChild("real_head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -2.0F, 6.0F, 6.0F, 4.0F).texOffs(16, 14).addBox(-2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(16, 14).addBox(2.0F, -5.0F, 0.0F, 2.0F, 2.0F, 1.0F).texOffs(0, 10).addBox(-0.5F, -0.001F, -5.0F, 3.0F, 3.0F, 4.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 14).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 9.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 14.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(21, 0).addBox(-3.0F, -3.0F, -3.0F, 8.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(-1.0F, 14.0F, -3.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, 7.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, 7.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.5F, 16.0F, -4.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(0.5F, 16.0F, -4.0F));
      PartDefinition partdefinition2 = partdefinition.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, 12.0F, 8.0F, ((float)Math.PI / 5F), 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("real_tail", CubeListBuilder.create().texOffs(9, 18).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.tail, this.upperBody);
   }

   public void prepareMobModel(T wolf, float f, float f1, float f2) {
      if (wolf.isAngry()) {
         this.tail.yRot = 0.0F;
      } else {
         this.tail.yRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
      }

      if (wolf.isInSittingPose()) {
         this.upperBody.setPos(-1.0F, 16.0F, -3.0F);
         this.upperBody.xRot = 1.2566371F;
         this.upperBody.yRot = 0.0F;
         this.body.setPos(0.0F, 18.0F, 0.0F);
         this.body.xRot = ((float)Math.PI / 4F);
         this.tail.setPos(-1.0F, 21.0F, 6.0F);
         this.rightHindLeg.setPos(-2.5F, 22.7F, 2.0F);
         this.rightHindLeg.xRot = ((float)Math.PI * 1.5F);
         this.leftHindLeg.setPos(0.5F, 22.7F, 2.0F);
         this.leftHindLeg.xRot = ((float)Math.PI * 1.5F);
         this.rightFrontLeg.xRot = 5.811947F;
         this.rightFrontLeg.setPos(-2.49F, 17.0F, -4.0F);
         this.leftFrontLeg.xRot = 5.811947F;
         this.leftFrontLeg.setPos(0.51F, 17.0F, -4.0F);
      } else {
         this.body.setPos(0.0F, 14.0F, 2.0F);
         this.body.xRot = ((float)Math.PI / 2F);
         this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
         this.upperBody.xRot = this.body.xRot;
         this.tail.setPos(-1.0F, 12.0F, 8.0F);
         this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
         this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
         this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
         this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
         this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
         this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
         this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
         this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
      }

      this.realHead.zRot = wolf.getHeadRollAngle(f2) + wolf.getBodyRollAngle(f2, 0.0F);
      this.upperBody.zRot = wolf.getBodyRollAngle(f2, -0.08F);
      this.body.zRot = wolf.getBodyRollAngle(f2, -0.16F);
      this.realTail.zRot = wolf.getBodyRollAngle(f2, -0.2F);
   }

   public void setupAnim(T wolf, float f, float f1, float f2, float f3, float f4) {
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.tail.xRot = f2;
   }
}
