package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Bee;

public class BeeModel<T extends Bee> extends AgeableListModel<T> {
   private static final float BEE_Y_BASE = 19.0F;
   private static final String BONE = "bone";
   private static final String STINGER = "stinger";
   private static final String LEFT_ANTENNA = "left_antenna";
   private static final String RIGHT_ANTENNA = "right_antenna";
   private static final String FRONT_LEGS = "front_legs";
   private static final String MIDDLE_LEGS = "middle_legs";
   private static final String BACK_LEGS = "back_legs";
   private final ModelPart bone;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart frontLeg;
   private final ModelPart midLeg;
   private final ModelPart backLeg;
   private final ModelPart stinger;
   private final ModelPart leftAntenna;
   private final ModelPart rightAntenna;
   private float rollAmount;

   public BeeModel(ModelPart modelpart) {
      super(false, 24.0F, 0.0F);
      this.bone = modelpart.getChild("bone");
      ModelPart modelpart1 = this.bone.getChild("body");
      this.stinger = modelpart1.getChild("stinger");
      this.leftAntenna = modelpart1.getChild("left_antenna");
      this.rightAntenna = modelpart1.getChild("right_antenna");
      this.rightWing = this.bone.getChild("right_wing");
      this.leftWing = this.bone.getChild("left_wing");
      this.frontLeg = this.bone.getChild("front_legs");
      this.midLeg = this.bone.getChild("middle_legs");
      this.backLeg = this.bone.getChild("back_legs");
   }

   public static LayerDefinition createBodyLayer() {
      float f = 19.0F;
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 19.0F, 0.0F));
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 7.0F, 10.0F), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(26, 7).addBox(0.0F, -1.0F, 5.0F, 0.0F, 1.0F, 2.0F), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("left_antenna", CubeListBuilder.create().texOffs(2, 0).addBox(1.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F));
      partdefinition2.addOrReplaceChild("right_antenna", CubeListBuilder.create().texOffs(2, 3).addBox(-2.5F, -2.0F, -3.0F, 1.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -2.0F, -5.0F));
      CubeDeformation cubedeformation = new CubeDeformation(0.001F);
      partdefinition1.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 18).addBox(-9.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, cubedeformation), PartPose.offsetAndRotation(-1.5F, -4.0F, -3.0F, 0.0F, -0.2618F, 0.0F));
      partdefinition1.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(0.0F, 0.0F, 0.0F, 9.0F, 0.0F, 6.0F, cubedeformation), PartPose.offsetAndRotation(1.5F, -4.0F, -3.0F, 0.0F, 0.2618F, 0.0F));
      partdefinition1.addOrReplaceChild("front_legs", CubeListBuilder.create().addBox("front_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 1), PartPose.offset(1.5F, 3.0F, -2.0F));
      partdefinition1.addOrReplaceChild("middle_legs", CubeListBuilder.create().addBox("middle_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 3), PartPose.offset(1.5F, 3.0F, 0.0F));
      partdefinition1.addOrReplaceChild("back_legs", CubeListBuilder.create().addBox("back_legs", -5.0F, 0.0F, 0.0F, 7, 2, 0, 26, 5), PartPose.offset(1.5F, 3.0F, 2.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void prepareMobModel(T bee, float f, float f1, float f2) {
      super.prepareMobModel(bee, f, f1, f2);
      this.rollAmount = bee.getRollAmount(f2);
      this.stinger.visible = !bee.hasStung();
   }

   public void setupAnim(T bee, float f, float f1, float f2, float f3, float f4) {
      this.rightWing.xRot = 0.0F;
      this.leftAntenna.xRot = 0.0F;
      this.rightAntenna.xRot = 0.0F;
      this.bone.xRot = 0.0F;
      boolean flag = bee.onGround() && bee.getDeltaMovement().lengthSqr() < 1.0E-7D;
      if (flag) {
         this.rightWing.yRot = -0.2618F;
         this.rightWing.zRot = 0.0F;
         this.leftWing.xRot = 0.0F;
         this.leftWing.yRot = 0.2618F;
         this.leftWing.zRot = 0.0F;
         this.frontLeg.xRot = 0.0F;
         this.midLeg.xRot = 0.0F;
         this.backLeg.xRot = 0.0F;
      } else {
         float f5 = f2 * 120.32113F * ((float)Math.PI / 180F);
         this.rightWing.yRot = 0.0F;
         this.rightWing.zRot = Mth.cos(f5) * (float)Math.PI * 0.15F;
         this.leftWing.xRot = this.rightWing.xRot;
         this.leftWing.yRot = this.rightWing.yRot;
         this.leftWing.zRot = -this.rightWing.zRot;
         this.frontLeg.xRot = ((float)Math.PI / 4F);
         this.midLeg.xRot = ((float)Math.PI / 4F);
         this.backLeg.xRot = ((float)Math.PI / 4F);
         this.bone.xRot = 0.0F;
         this.bone.yRot = 0.0F;
         this.bone.zRot = 0.0F;
      }

      if (!bee.isAngry()) {
         this.bone.xRot = 0.0F;
         this.bone.yRot = 0.0F;
         this.bone.zRot = 0.0F;
         if (!flag) {
            float f6 = Mth.cos(f2 * 0.18F);
            this.bone.xRot = 0.1F + f6 * (float)Math.PI * 0.025F;
            this.leftAntenna.xRot = f6 * (float)Math.PI * 0.03F;
            this.rightAntenna.xRot = f6 * (float)Math.PI * 0.03F;
            this.frontLeg.xRot = -f6 * (float)Math.PI * 0.1F + ((float)Math.PI / 8F);
            this.backLeg.xRot = -f6 * (float)Math.PI * 0.05F + ((float)Math.PI / 4F);
            this.bone.y = 19.0F - Mth.cos(f2 * 0.18F) * 0.9F;
         }
      }

      if (this.rollAmount > 0.0F) {
         this.bone.xRot = ModelUtils.rotlerpRad(this.bone.xRot, 3.0915928F, this.rollAmount);
      }

   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of();
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.bone);
   }
}
