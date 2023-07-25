package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.world.entity.animal.allay.Allay;

public class AllayModel extends HierarchicalModel<Allay> implements ArmedModel {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart right_arm;
   private final ModelPart left_arm;
   private final ModelPart right_wing;
   private final ModelPart left_wing;
   private static final float FLYING_ANIMATION_X_ROT = ((float)Math.PI / 4F);
   private static final float MAX_HAND_HOLDING_ITEM_X_ROT_RAD = -1.134464F;
   private static final float MIN_HAND_HOLDING_ITEM_X_ROT_RAD = (-(float)Math.PI / 3F);

   public AllayModel(ModelPart modelpart) {
      super(RenderType::entityTranslucent);
      this.root = modelpart.getChild("root");
      this.head = this.root.getChild("head");
      this.body = this.root.getChild("body");
      this.right_arm = this.body.getChild("right_arm");
      this.left_arm = this.body.getChild("left_arm");
      this.right_wing = this.body.getChild("right_wing");
      this.left_wing = this.body.getChild("left_wing");
   }

   public ModelPart root() {
      return this.root;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 23.5F, 0.0F));
      partdefinition1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.99F, 0.0F));
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 10).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 5.0F, 2.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, -4.0F, 0.0F));
      partdefinition2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 0).addBox(-0.75F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(-1.75F, 0.5F, 0.0F));
      partdefinition2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(23, 6).addBox(-0.25F, -0.5F, -1.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(1.75F, 0.5F, 0.0F));
      partdefinition2.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, 0.6F));
      partdefinition2.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(16, 14).addBox(0.0F, 1.0F, 0.0F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, 0.6F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public void setupAnim(Allay allay, float f, float f1, float f2, float f3, float f4) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      float f5 = f2 * 20.0F * ((float)Math.PI / 180F) + f;
      float f6 = Mth.cos(f5) * (float)Math.PI * 0.15F + f1;
      float f7 = f2 - (float)allay.tickCount;
      float f8 = f2 * 9.0F * ((float)Math.PI / 180F);
      float f9 = Math.min(f1 / 0.3F, 1.0F);
      float f10 = 1.0F - f9;
      float f11 = allay.getHoldingItemAnimationProgress(f7);
      if (allay.isDancing()) {
         float f12 = f2 * 8.0F * ((float)Math.PI / 180F) + f1;
         float f13 = Mth.cos(f12) * 16.0F * ((float)Math.PI / 180F);
         float f14 = allay.getSpinningProgress(f7);
         float f15 = Mth.cos(f12) * 14.0F * ((float)Math.PI / 180F);
         float f16 = Mth.cos(f12) * 30.0F * ((float)Math.PI / 180F);
         this.root.yRot = allay.isSpinning() ? 12.566371F * f14 : this.root.yRot;
         this.root.zRot = f13 * (1.0F - f14);
         this.head.yRot = f16 * (1.0F - f14);
         this.head.zRot = f15 * (1.0F - f14);
      } else {
         this.head.xRot = f4 * ((float)Math.PI / 180F);
         this.head.yRot = f3 * ((float)Math.PI / 180F);
      }

      this.right_wing.xRot = 0.43633232F * (1.0F - f9);
      this.right_wing.yRot = (-(float)Math.PI / 4F) + f6;
      this.left_wing.xRot = 0.43633232F * (1.0F - f9);
      this.left_wing.yRot = ((float)Math.PI / 4F) - f6;
      this.body.xRot = f9 * ((float)Math.PI / 4F);
      float f17 = f11 * Mth.lerp(f9, (-(float)Math.PI / 3F), -1.134464F);
      this.root.y += (float)Math.cos((double)f8) * 0.25F * f10;
      this.right_arm.xRot = f17;
      this.left_arm.xRot = f17;
      float f18 = f10 * (1.0F - f11);
      float f19 = 0.43633232F - Mth.cos(f8 + ((float)Math.PI * 1.5F)) * (float)Math.PI * 0.075F * f18;
      this.left_arm.zRot = -f19;
      this.right_arm.zRot = f19;
      this.right_arm.yRot = 0.27925268F * f11;
      this.left_arm.yRot = -0.27925268F * f11;
   }

   public void translateToHand(HumanoidArm humanoidarm, PoseStack posestack) {
      float f = 1.0F;
      float f1 = 3.0F;
      this.root.translateAndRotate(posestack);
      this.body.translateAndRotate(posestack);
      posestack.translate(0.0F, 0.0625F, 0.1875F);
      posestack.mulPose(Axis.XP.rotation(this.right_arm.xRot));
      posestack.scale(0.7F, 0.7F, 0.7F);
      posestack.translate(0.0625F, 0.0F, 0.0F);
   }
}
