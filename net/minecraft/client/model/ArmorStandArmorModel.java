package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandArmorModel extends HumanoidModel<ArmorStand> {
   public ArmorStandArmorModel(ModelPart modelpart) {
      super(modelpart);
   }

   public static LayerDefinition createBodyLayer(CubeDeformation cubedeformation) {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(cubedeformation, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubedeformation), PartPose.offset(0.0F, 1.0F, 0.0F));
      partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubedeformation.extend(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation.extend(-0.1F)), PartPose.offset(-1.9F, 11.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation.extend(-0.1F)), PartPose.offset(1.9F, 11.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(ArmorStand armorstand, float f, float f1, float f2, float f3, float f4) {
      this.head.xRot = ((float)Math.PI / 180F) * armorstand.getHeadPose().getX();
      this.head.yRot = ((float)Math.PI / 180F) * armorstand.getHeadPose().getY();
      this.head.zRot = ((float)Math.PI / 180F) * armorstand.getHeadPose().getZ();
      this.body.xRot = ((float)Math.PI / 180F) * armorstand.getBodyPose().getX();
      this.body.yRot = ((float)Math.PI / 180F) * armorstand.getBodyPose().getY();
      this.body.zRot = ((float)Math.PI / 180F) * armorstand.getBodyPose().getZ();
      this.leftArm.xRot = ((float)Math.PI / 180F) * armorstand.getLeftArmPose().getX();
      this.leftArm.yRot = ((float)Math.PI / 180F) * armorstand.getLeftArmPose().getY();
      this.leftArm.zRot = ((float)Math.PI / 180F) * armorstand.getLeftArmPose().getZ();
      this.rightArm.xRot = ((float)Math.PI / 180F) * armorstand.getRightArmPose().getX();
      this.rightArm.yRot = ((float)Math.PI / 180F) * armorstand.getRightArmPose().getY();
      this.rightArm.zRot = ((float)Math.PI / 180F) * armorstand.getRightArmPose().getZ();
      this.leftLeg.xRot = ((float)Math.PI / 180F) * armorstand.getLeftLegPose().getX();
      this.leftLeg.yRot = ((float)Math.PI / 180F) * armorstand.getLeftLegPose().getY();
      this.leftLeg.zRot = ((float)Math.PI / 180F) * armorstand.getLeftLegPose().getZ();
      this.rightLeg.xRot = ((float)Math.PI / 180F) * armorstand.getRightLegPose().getX();
      this.rightLeg.yRot = ((float)Math.PI / 180F) * armorstand.getRightLegPose().getY();
      this.rightLeg.zRot = ((float)Math.PI / 180F) * armorstand.getRightLegPose().getZ();
      this.hat.copyFrom(this.head);
   }
}
