package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidModel<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
   public static final float OVERLAY_SCALE = 0.25F;
   public static final float HAT_OVERLAY_SCALE = 0.5F;
   public static final float LEGGINGS_OVERLAY_SCALE = -0.1F;
   private static final float DUCK_WALK_ROTATION = 0.005F;
   private static final float SPYGLASS_ARM_ROT_Y = 0.2617994F;
   private static final float SPYGLASS_ARM_ROT_X = 1.9198622F;
   private static final float SPYGLASS_ARM_CROUCH_ROT_X = 0.2617994F;
   public static final float TOOT_HORN_XROT_BASE = 1.4835298F;
   public static final float TOOT_HORN_YROT_BASE = ((float)Math.PI / 6F);
   public final ModelPart head;
   public final ModelPart hat;
   public final ModelPart body;
   public final ModelPart rightArm;
   public final ModelPart leftArm;
   public final ModelPart rightLeg;
   public final ModelPart leftLeg;
   public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
   public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
   public boolean crouching;
   public float swimAmount;

   public HumanoidModel(ModelPart modelpart) {
      this(modelpart, RenderType::entityCutoutNoCull);
   }

   public HumanoidModel(ModelPart modelpart, Function<ResourceLocation, RenderType> function) {
      super(function, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.head = modelpart.getChild("head");
      this.hat = modelpart.getChild("hat");
      this.body = modelpart.getChild("body");
      this.rightArm = modelpart.getChild("right_arm");
      this.leftArm = modelpart.getChild("left_arm");
      this.rightLeg = modelpart.getChild("right_leg");
      this.leftLeg = modelpart.getChild("left_leg");
   }

   public static MeshDefinition createMesh(CubeDeformation cubedeformation, float f) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubedeformation), PartPose.offset(0.0F, 0.0F + f, 0.0F));
      partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubedeformation.extend(0.5F)), PartPose.offset(0.0F, 0.0F + f, 0.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(0.0F, 0.0F + f, 0.0F));
      partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(-5.0F, 2.0F + f, 0.0F));
      partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(5.0F, 2.0F + f, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(-1.9F, 12.0F + f, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(1.9F, 12.0F + f, 0.0F));
      return meshdefinition;
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
   }

   public void prepareMobModel(T livingentity, float f, float f1, float f2) {
      this.swimAmount = livingentity.getSwimAmount(f2);
      super.prepareMobModel(livingentity, f, f1, f2);
   }

   public void setupAnim(T livingentity, float f, float f1, float f2, float f3, float f4) {
      boolean flag = livingentity.getFallFlyingTicks() > 4;
      boolean flag1 = livingentity.isVisuallySwimming();
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      if (flag) {
         this.head.xRot = (-(float)Math.PI / 4F);
      } else if (this.swimAmount > 0.0F) {
         if (flag1) {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
         } else {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, f4 * ((float)Math.PI / 180F));
         }
      } else {
         this.head.xRot = f4 * ((float)Math.PI / 180F);
      }

      this.body.yRot = 0.0F;
      this.rightArm.z = 0.0F;
      this.rightArm.x = -5.0F;
      this.leftArm.z = 0.0F;
      this.leftArm.x = 5.0F;
      float f5 = 1.0F;
      if (flag) {
         f5 = (float)livingentity.getDeltaMovement().lengthSqr();
         f5 /= 0.2F;
         f5 *= f5 * f5;
      }

      if (f5 < 1.0F) {
         f5 = 1.0F;
      }

      this.rightArm.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * 2.0F * f1 * 0.5F / f5;
      this.leftArm.xRot = Mth.cos(f * 0.6662F) * 2.0F * f1 * 0.5F / f5;
      this.rightArm.zRot = 0.0F;
      this.leftArm.zRot = 0.0F;
      this.rightLeg.xRot = Mth.cos(f * 0.6662F) * 1.4F * f1 / f5;
      this.leftLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1 / f5;
      this.rightLeg.yRot = 0.005F;
      this.leftLeg.yRot = -0.005F;
      this.rightLeg.zRot = 0.005F;
      this.leftLeg.zRot = -0.005F;
      if (this.riding) {
         this.rightArm.xRot += (-(float)Math.PI / 5F);
         this.leftArm.xRot += (-(float)Math.PI / 5F);
         this.rightLeg.xRot = -1.4137167F;
         this.rightLeg.yRot = ((float)Math.PI / 10F);
         this.rightLeg.zRot = 0.07853982F;
         this.leftLeg.xRot = -1.4137167F;
         this.leftLeg.yRot = (-(float)Math.PI / 10F);
         this.leftLeg.zRot = -0.07853982F;
      }

      this.rightArm.yRot = 0.0F;
      this.leftArm.yRot = 0.0F;
      boolean flag2 = livingentity.getMainArm() == HumanoidArm.RIGHT;
      if (livingentity.isUsingItem()) {
         boolean flag3 = livingentity.getUsedItemHand() == InteractionHand.MAIN_HAND;
         if (flag3 == flag2) {
            this.poseRightArm(livingentity);
         } else {
            this.poseLeftArm(livingentity);
         }
      } else {
         boolean flag4 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
         if (flag2 != flag4) {
            this.poseLeftArm(livingentity);
            this.poseRightArm(livingentity);
         } else {
            this.poseRightArm(livingentity);
            this.poseLeftArm(livingentity);
         }
      }

      this.setupAttackAnimation(livingentity, f2);
      if (this.crouching) {
         this.body.xRot = 0.5F;
         this.rightArm.xRot += 0.4F;
         this.leftArm.xRot += 0.4F;
         this.rightLeg.z = 4.0F;
         this.leftLeg.z = 4.0F;
         this.rightLeg.y = 12.2F;
         this.leftLeg.y = 12.2F;
         this.head.y = 4.2F;
         this.body.y = 3.2F;
         this.leftArm.y = 5.2F;
         this.rightArm.y = 5.2F;
      } else {
         this.body.xRot = 0.0F;
         this.rightLeg.z = 0.0F;
         this.leftLeg.z = 0.0F;
         this.rightLeg.y = 12.0F;
         this.leftLeg.y = 12.0F;
         this.head.y = 0.0F;
         this.body.y = 0.0F;
         this.leftArm.y = 2.0F;
         this.rightArm.y = 2.0F;
      }

      if (this.rightArmPose != HumanoidModel.ArmPose.SPYGLASS) {
         AnimationUtils.bobModelPart(this.rightArm, f2, 1.0F);
      }

      if (this.leftArmPose != HumanoidModel.ArmPose.SPYGLASS) {
         AnimationUtils.bobModelPart(this.leftArm, f2, -1.0F);
      }

      if (this.swimAmount > 0.0F) {
         float f6 = f % 26.0F;
         HumanoidArm humanoidarm = this.getAttackArm(livingentity);
         float f7 = humanoidarm == HumanoidArm.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         float f8 = humanoidarm == HumanoidArm.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         if (!livingentity.isUsingItem()) {
            if (f6 < 14.0F) {
               this.leftArm.xRot = this.rotlerpRad(f8, this.leftArm.xRot, 0.0F);
               this.rightArm.xRot = Mth.lerp(f7, this.rightArm.xRot, 0.0F);
               this.leftArm.yRot = this.rotlerpRad(f8, this.leftArm.yRot, (float)Math.PI);
               this.rightArm.yRot = Mth.lerp(f7, this.rightArm.yRot, (float)Math.PI);
               this.leftArm.zRot = this.rotlerpRad(f8, this.leftArm.zRot, (float)Math.PI + 1.8707964F * this.quadraticArmUpdate(f6) / this.quadraticArmUpdate(14.0F));
               this.rightArm.zRot = Mth.lerp(f7, this.rightArm.zRot, (float)Math.PI - 1.8707964F * this.quadraticArmUpdate(f6) / this.quadraticArmUpdate(14.0F));
            } else if (f6 >= 14.0F && f6 < 22.0F) {
               float f9 = (f6 - 14.0F) / 8.0F;
               this.leftArm.xRot = this.rotlerpRad(f8, this.leftArm.xRot, ((float)Math.PI / 2F) * f9);
               this.rightArm.xRot = Mth.lerp(f7, this.rightArm.xRot, ((float)Math.PI / 2F) * f9);
               this.leftArm.yRot = this.rotlerpRad(f8, this.leftArm.yRot, (float)Math.PI);
               this.rightArm.yRot = Mth.lerp(f7, this.rightArm.yRot, (float)Math.PI);
               this.leftArm.zRot = this.rotlerpRad(f8, this.leftArm.zRot, 5.012389F - 1.8707964F * f9);
               this.rightArm.zRot = Mth.lerp(f7, this.rightArm.zRot, 1.2707963F + 1.8707964F * f9);
            } else if (f6 >= 22.0F && f6 < 26.0F) {
               float f10 = (f6 - 22.0F) / 4.0F;
               this.leftArm.xRot = this.rotlerpRad(f8, this.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f10);
               this.rightArm.xRot = Mth.lerp(f7, this.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f10);
               this.leftArm.yRot = this.rotlerpRad(f8, this.leftArm.yRot, (float)Math.PI);
               this.rightArm.yRot = Mth.lerp(f7, this.rightArm.yRot, (float)Math.PI);
               this.leftArm.zRot = this.rotlerpRad(f8, this.leftArm.zRot, (float)Math.PI);
               this.rightArm.zRot = Mth.lerp(f7, this.rightArm.zRot, (float)Math.PI);
            }
         }

         float f11 = 0.3F;
         float f12 = 0.33333334F;
         this.leftLeg.xRot = Mth.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F + (float)Math.PI));
         this.rightLeg.xRot = Mth.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * Mth.cos(f * 0.33333334F));
      }

      this.hat.copyFrom(this.head);
   }

   private void poseRightArm(T livingentity) {
      switch (this.rightArmPose) {
         case EMPTY:
            this.rightArm.yRot = 0.0F;
            break;
         case BLOCK:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
            this.rightArm.yRot = (-(float)Math.PI / 6F);
            break;
         case ITEM:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
            this.rightArm.yRot = 0.0F;
            break;
         case THROW_SPEAR:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
            this.rightArm.yRot = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            break;
         case CROSSBOW_CHARGE:
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingentity, true);
            break;
         case CROSSBOW_HOLD:
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
            break;
         case BRUSH:
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 5F);
            this.rightArm.yRot = 0.0F;
            break;
         case SPYGLASS:
            this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (livingentity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.rightArm.yRot = this.head.yRot - 0.2617994F;
            break;
         case TOOT_HORN:
            this.rightArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
            this.rightArm.yRot = this.head.yRot - ((float)Math.PI / 6F);
      }

   }

   private void poseLeftArm(T livingentity) {
      switch (this.leftArmPose) {
         case EMPTY:
            this.leftArm.yRot = 0.0F;
            break;
         case BLOCK:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
            this.leftArm.yRot = ((float)Math.PI / 6F);
            break;
         case ITEM:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
            this.leftArm.yRot = 0.0F;
            break;
         case THROW_SPEAR:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
            this.leftArm.yRot = 0.0F;
            break;
         case BOW_AND_ARROW:
            this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
            this.leftArm.yRot = 0.1F + this.head.yRot;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
            break;
         case CROSSBOW_CHARGE:
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, livingentity, false);
            break;
         case CROSSBOW_HOLD:
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
            break;
         case BRUSH:
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 5F);
            this.leftArm.yRot = 0.0F;
            break;
         case SPYGLASS:
            this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (livingentity.isCrouching() ? 0.2617994F : 0.0F), -2.4F, 3.3F);
            this.leftArm.yRot = this.head.yRot + 0.2617994F;
            break;
         case TOOT_HORN:
            this.leftArm.xRot = Mth.clamp(this.head.xRot, -1.2F, 1.2F) - 1.4835298F;
            this.leftArm.yRot = this.head.yRot + ((float)Math.PI / 6F);
      }

   }

   protected void setupAttackAnimation(T livingentity, float f) {
      if (!(this.attackTime <= 0.0F)) {
         HumanoidArm humanoidarm = this.getAttackArm(livingentity);
         ModelPart modelpart = this.getArm(humanoidarm);
         float f1 = this.attackTime;
         this.body.yRot = Mth.sin(Mth.sqrt(f1) * ((float)Math.PI * 2F)) * 0.2F;
         if (humanoidarm == HumanoidArm.LEFT) {
            this.body.yRot *= -1.0F;
         }

         this.rightArm.z = Mth.sin(this.body.yRot) * 5.0F;
         this.rightArm.x = -Mth.cos(this.body.yRot) * 5.0F;
         this.leftArm.z = -Mth.sin(this.body.yRot) * 5.0F;
         this.leftArm.x = Mth.cos(this.body.yRot) * 5.0F;
         this.rightArm.yRot += this.body.yRot;
         this.leftArm.yRot += this.body.yRot;
         this.leftArm.xRot += this.body.yRot;
         f1 = 1.0F - this.attackTime;
         f1 *= f1;
         f1 *= f1;
         f1 = 1.0F - f1;
         float f2 = Mth.sin(f1 * (float)Math.PI);
         float f3 = Mth.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
         modelpart.xRot -= f2 * 1.2F + f3;
         modelpart.yRot += this.body.yRot * 2.0F;
         modelpart.zRot += Mth.sin(this.attackTime * (float)Math.PI) * -0.4F;
      }
   }

   protected float rotlerpRad(float f, float f1, float f2) {
      float f3 = (f2 - f1) % ((float)Math.PI * 2F);
      if (f3 < -(float)Math.PI) {
         f3 += ((float)Math.PI * 2F);
      }

      if (f3 >= (float)Math.PI) {
         f3 -= ((float)Math.PI * 2F);
      }

      return f1 + f * f3;
   }

   private float quadraticArmUpdate(float f) {
      return -65.0F * f + f * f;
   }

   public void copyPropertiesTo(HumanoidModel<T> humanoidmodel) {
      super.copyPropertiesTo(humanoidmodel);
      humanoidmodel.leftArmPose = this.leftArmPose;
      humanoidmodel.rightArmPose = this.rightArmPose;
      humanoidmodel.crouching = this.crouching;
      humanoidmodel.head.copyFrom(this.head);
      humanoidmodel.hat.copyFrom(this.hat);
      humanoidmodel.body.copyFrom(this.body);
      humanoidmodel.rightArm.copyFrom(this.rightArm);
      humanoidmodel.leftArm.copyFrom(this.leftArm);
      humanoidmodel.rightLeg.copyFrom(this.rightLeg);
      humanoidmodel.leftLeg.copyFrom(this.leftLeg);
   }

   public void setAllVisible(boolean flag) {
      this.head.visible = flag;
      this.hat.visible = flag;
      this.body.visible = flag;
      this.rightArm.visible = flag;
      this.leftArm.visible = flag;
      this.rightLeg.visible = flag;
      this.leftLeg.visible = flag;
   }

   public void translateToHand(HumanoidArm humanoidarm, PoseStack posestack) {
      this.getArm(humanoidarm).translateAndRotate(posestack);
   }

   protected ModelPart getArm(HumanoidArm humanoidarm) {
      return humanoidarm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelPart getHead() {
      return this.head;
   }

   private HumanoidArm getAttackArm(T livingentity) {
      HumanoidArm humanoidarm = livingentity.getMainArm();
      return livingentity.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
   }

   public static enum ArmPose {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true),
      SPYGLASS(false),
      TOOT_HORN(false),
      BRUSH(false);

      private final boolean twoHanded;

      private ArmPose(boolean flag) {
         this.twoHanded = flag;
      }

      public boolean isTwoHanded() {
         return this.twoHanded;
      }
   }
}
