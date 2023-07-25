package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class SpiderModel<T extends Entity> extends HierarchicalModel<T> {
   private static final String BODY_0 = "body0";
   private static final String BODY_1 = "body1";
   private static final String RIGHT_MIDDLE_FRONT_LEG = "right_middle_front_leg";
   private static final String LEFT_MIDDLE_FRONT_LEG = "left_middle_front_leg";
   private static final String RIGHT_MIDDLE_HIND_LEG = "right_middle_hind_leg";
   private static final String LEFT_MIDDLE_HIND_LEG = "left_middle_hind_leg";
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightMiddleHindLeg;
   private final ModelPart leftMiddleHindLeg;
   private final ModelPart rightMiddleFrontLeg;
   private final ModelPart leftMiddleFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;

   public SpiderModel(ModelPart modelpart) {
      this.root = modelpart;
      this.head = modelpart.getChild("head");
      this.rightHindLeg = modelpart.getChild("right_hind_leg");
      this.leftHindLeg = modelpart.getChild("left_hind_leg");
      this.rightMiddleHindLeg = modelpart.getChild("right_middle_hind_leg");
      this.leftMiddleHindLeg = modelpart.getChild("left_middle_hind_leg");
      this.rightMiddleFrontLeg = modelpart.getChild("right_middle_front_leg");
      this.leftMiddleFrontLeg = modelpart.getChild("left_middle_front_leg");
      this.rightFrontLeg = modelpart.getChild("right_front_leg");
      this.leftFrontLeg = modelpart.getChild("left_front_leg");
   }

   public static LayerDefinition createSpiderBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      int i = 15;
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 4).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 15.0F, -3.0F));
      partdefinition.addOrReplaceChild("body0", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
      partdefinition.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 12).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 8.0F, 12.0F), PartPose.offset(0.0F, 15.0F, 9.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(18, 0).addBox(-15.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
      CubeListBuilder cubelistbuilder1 = CubeListBuilder.create().texOffs(18, 0).mirror().addBox(-1.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-4.0F, 15.0F, 2.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder1, PartPose.offset(4.0F, 15.0F, 2.0F));
      partdefinition.addOrReplaceChild("right_middle_hind_leg", cubelistbuilder, PartPose.offset(-4.0F, 15.0F, 1.0F));
      partdefinition.addOrReplaceChild("left_middle_hind_leg", cubelistbuilder1, PartPose.offset(4.0F, 15.0F, 1.0F));
      partdefinition.addOrReplaceChild("right_middle_front_leg", cubelistbuilder, PartPose.offset(-4.0F, 15.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_middle_front_leg", cubelistbuilder1, PartPose.offset(4.0F, 15.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-4.0F, 15.0F, -1.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder1, PartPose.offset(4.0F, 15.0F, -1.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(T entity, float f, float f1, float f2, float f3, float f4) {
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      float f5 = ((float)Math.PI / 4F);
      this.rightHindLeg.zRot = (-(float)Math.PI / 4F);
      this.leftHindLeg.zRot = ((float)Math.PI / 4F);
      this.rightMiddleHindLeg.zRot = -0.58119464F;
      this.leftMiddleHindLeg.zRot = 0.58119464F;
      this.rightMiddleFrontLeg.zRot = -0.58119464F;
      this.leftMiddleFrontLeg.zRot = 0.58119464F;
      this.rightFrontLeg.zRot = (-(float)Math.PI / 4F);
      this.leftFrontLeg.zRot = ((float)Math.PI / 4F);
      float f6 = -0.0F;
      float f7 = ((float)Math.PI / 8F);
      this.rightHindLeg.yRot = ((float)Math.PI / 4F);
      this.leftHindLeg.yRot = (-(float)Math.PI / 4F);
      this.rightMiddleHindLeg.yRot = ((float)Math.PI / 8F);
      this.leftMiddleHindLeg.yRot = (-(float)Math.PI / 8F);
      this.rightMiddleFrontLeg.yRot = (-(float)Math.PI / 8F);
      this.leftMiddleFrontLeg.yRot = ((float)Math.PI / 8F);
      this.rightFrontLeg.yRot = (-(float)Math.PI / 4F);
      this.leftFrontLeg.yRot = ((float)Math.PI / 4F);
      float f8 = -(Mth.cos(f * 0.6662F * 2.0F + 0.0F) * 0.4F) * f1;
      float f9 = -(Mth.cos(f * 0.6662F * 2.0F + (float)Math.PI) * 0.4F) * f1;
      float f10 = -(Mth.cos(f * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * f1;
      float f11 = -(Mth.cos(f * 0.6662F * 2.0F + ((float)Math.PI * 1.5F)) * 0.4F) * f1;
      float f12 = Math.abs(Mth.sin(f * 0.6662F + 0.0F) * 0.4F) * f1;
      float f13 = Math.abs(Mth.sin(f * 0.6662F + (float)Math.PI) * 0.4F) * f1;
      float f14 = Math.abs(Mth.sin(f * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * f1;
      float f15 = Math.abs(Mth.sin(f * 0.6662F + ((float)Math.PI * 1.5F)) * 0.4F) * f1;
      this.rightHindLeg.yRot += f8;
      this.leftHindLeg.yRot += -f8;
      this.rightMiddleHindLeg.yRot += f9;
      this.leftMiddleHindLeg.yRot += -f9;
      this.rightMiddleFrontLeg.yRot += f10;
      this.leftMiddleFrontLeg.yRot += -f10;
      this.rightFrontLeg.yRot += f11;
      this.leftFrontLeg.yRot += -f11;
      this.rightHindLeg.zRot += f12;
      this.leftHindLeg.zRot += -f12;
      this.rightMiddleHindLeg.zRot += f13;
      this.leftMiddleHindLeg.zRot += -f13;
      this.rightMiddleFrontLeg.zRot += f14;
      this.leftMiddleFrontLeg.zRot += -f14;
      this.rightFrontLeg.zRot += f15;
      this.leftFrontLeg.zRot += -f15;
   }
}
