package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.ItemStack;

public class VexModel extends HierarchicalModel<Vex> implements ArmedModel {
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart head;

   public VexModel(ModelPart modelpart) {
      super(RenderType::entityTranslucent);
      this.root = modelpart.getChild("root");
      this.body = this.root.getChild("body");
      this.rightArm = this.body.getChild("right_arm");
      this.leftArm = this.body.getChild("left_arm");
      this.rightWing = this.body.getChild("right_wing");
      this.leftWing = this.body.getChild("left_wing");
      this.head = this.root.getChild("head");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 0.0F));
      partdefinition1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16).addBox(-1.5F, 1.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 20.0F, 0.0F));
      partdefinition2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-1.25F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(-1.75F, 0.25F, 0.0F));
      partdefinition2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.75F, -0.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.1F)), PartPose.offset(1.75F, 0.25F, 0.0F));
      partdefinition2.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).mirror().addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.5F, 1.0F, 1.0F));
      partdefinition2.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 1.0F, 1.0F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public void setupAnim(Vex vex, float f, float f1, float f2, float f3, float f4) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      float f5 = Mth.cos(f2 * 5.5F * ((float)Math.PI / 180F)) * 0.1F;
      this.rightArm.zRot = ((float)Math.PI / 5F) + f5;
      this.leftArm.zRot = -(((float)Math.PI / 5F) + f5);
      if (vex.isCharging()) {
         this.body.xRot = 0.0F;
         this.setArmsCharging(vex.getMainHandItem(), vex.getOffhandItem(), f5);
      } else {
         this.body.xRot = 0.15707964F;
      }

      this.leftWing.yRot = 1.0995574F + Mth.cos(f2 * 45.836624F * ((float)Math.PI / 180F)) * ((float)Math.PI / 180F) * 16.2F;
      this.rightWing.yRot = -this.leftWing.yRot;
      this.leftWing.xRot = 0.47123888F;
      this.leftWing.zRot = -0.47123888F;
      this.rightWing.xRot = 0.47123888F;
      this.rightWing.zRot = 0.47123888F;
   }

   private void setArmsCharging(ItemStack itemstack, ItemStack itemstack1, float f) {
      if (itemstack.isEmpty() && itemstack1.isEmpty()) {
         this.rightArm.xRot = -1.2217305F;
         this.rightArm.yRot = 0.2617994F;
         this.rightArm.zRot = -0.47123888F - f;
         this.leftArm.xRot = -1.2217305F;
         this.leftArm.yRot = -0.2617994F;
         this.leftArm.zRot = 0.47123888F + f;
      } else {
         if (!itemstack.isEmpty()) {
            this.rightArm.xRot = 3.6651914F;
            this.rightArm.yRot = 0.2617994F;
            this.rightArm.zRot = -0.47123888F - f;
         }

         if (!itemstack1.isEmpty()) {
            this.leftArm.xRot = 3.6651914F;
            this.leftArm.yRot = -0.2617994F;
            this.leftArm.zRot = 0.47123888F + f;
         }

      }
   }

   public ModelPart root() {
      return this.root;
   }

   public void translateToHand(HumanoidArm humanoidarm, PoseStack posestack) {
      boolean flag = humanoidarm == HumanoidArm.RIGHT;
      ModelPart modelpart = flag ? this.rightArm : this.leftArm;
      this.root.translateAndRotate(posestack);
      this.body.translateAndRotate(posestack);
      modelpart.translateAndRotate(posestack);
      posestack.scale(0.55F, 0.55F, 0.55F);
      this.offsetStackPosition(posestack, flag);
   }

   private void offsetStackPosition(PoseStack posestack, boolean flag) {
      if (flag) {
         posestack.translate(0.046875D, -0.15625D, 0.078125D);
      } else {
         posestack.translate(-0.046875D, -0.15625D, 0.078125D);
      }

   }
}
