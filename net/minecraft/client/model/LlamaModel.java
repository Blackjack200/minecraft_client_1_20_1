package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

public class LlamaModel<T extends AbstractChestedHorse> extends EntityModel<T> {
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightChest;
   private final ModelPart leftChest;

   public LlamaModel(ModelPart modelpart) {
      this.head = modelpart.getChild("head");
      this.body = modelpart.getChild("body");
      this.rightChest = modelpart.getChild("right_chest");
      this.leftChest = modelpart.getChild("left_chest");
      this.rightHindLeg = modelpart.getChild("right_hind_leg");
      this.leftHindLeg = modelpart.getChild("left_hind_leg");
      this.rightFrontLeg = modelpart.getChild("right_front_leg");
      this.leftFrontLeg = modelpart.getChild("left_front_leg");
   }

   public static LayerDefinition createBodyLayer(CubeDeformation cubedeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, cubedeformation).texOffs(0, 14).addBox("neck", -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, cubedeformation).texOffs(17, 0).addBox("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, cubedeformation).texOffs(17, 0).addBox("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, cubedeformation), PartPose.offset(0.0F, 7.0F, -6.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, cubedeformation), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, cubedeformation), PartPose.offsetAndRotation(-8.5F, 3.0F, 3.0F, 0.0F, ((float)Math.PI / 2F), 0.0F));
      partdefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, cubedeformation), PartPose.offsetAndRotation(5.5F, 3.0F, 3.0F, 0.0F, ((float)Math.PI / 2F), 0.0F));
      int i = 4;
      int j = 14;
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, cubedeformation);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-3.5F, 10.0F, 6.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(3.5F, 10.0F, 6.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-3.5F, 10.0F, -5.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(3.5F, 10.0F, -5.0F));
      return LayerDefinition.create(meshdefinition, 128, 64);
   }

   public void setupAnim(T abstractchestedhorse, float f, float f1, float f2, float f3, float f4) {
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
      this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
      this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
      this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1;
      boolean flag = !abstractchestedhorse.isBaby() && abstractchestedhorse.hasChest();
      this.rightChest.visible = flag;
      this.leftChest.visible = flag;
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      if (this.young) {
         float f4 = 2.0F;
         posestack.pushPose();
         float f5 = 0.7F;
         posestack.scale(0.71428573F, 0.64935064F, 0.7936508F);
         posestack.translate(0.0F, 1.3125F, 0.22F);
         this.head.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
         posestack.popPose();
         posestack.pushPose();
         float f6 = 1.1F;
         posestack.scale(0.625F, 0.45454544F, 0.45454544F);
         posestack.translate(0.0F, 2.0625F, 0.0F);
         this.body.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
         posestack.popPose();
         posestack.pushPose();
         posestack.scale(0.45454544F, 0.41322312F, 0.45454544F);
         posestack.translate(0.0F, 2.0625F, 0.0F);
         ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((modelpart1) -> modelpart1.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
         posestack.popPose();
      } else {
         ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((modelpart) -> modelpart.render(posestack, vertexconsumer, i, j, f, f1, f2, f3));
      }

   }
}
