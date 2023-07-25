package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;

public class PiglinModel<T extends Mob> extends PlayerModel<T> {
   public final ModelPart rightEar = this.head.getChild("right_ear");
   private final ModelPart leftEar = this.head.getChild("left_ear");
   private final PartPose bodyDefault = this.body.storePose();
   private final PartPose headDefault = this.head.storePose();
   private final PartPose leftArmDefault = this.leftArm.storePose();
   private final PartPose rightArmDefault = this.rightArm.storePose();

   public PiglinModel(ModelPart modelpart) {
      super(modelpart, false);
   }

   public static MeshDefinition createMesh(CubeDeformation cubedeformation) {
      MeshDefinition meshdefinition = PlayerModel.createMesh(cubedeformation, false);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubedeformation), PartPose.ZERO);
      addHead(cubedeformation, meshdefinition);
      partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
      return meshdefinition;
   }

   public static void addHead(CubeDeformation cubedeformation, MeshDefinition meshdefinition) {
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, cubedeformation).texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, cubedeformation).texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubedeformation).texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubedeformation), PartPose.ZERO);
      partdefinition1.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubedeformation), PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, (-(float)Math.PI / 6F)));
      partdefinition1.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubedeformation), PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, ((float)Math.PI / 6F)));
   }

   public void setupAnim(T mob, float f, float f1, float f2, float f3, float f4) {
      this.body.loadPose(this.bodyDefault);
      this.head.loadPose(this.headDefault);
      this.leftArm.loadPose(this.leftArmDefault);
      this.rightArm.loadPose(this.rightArmDefault);
      super.setupAnim(mob, f, f1, f2, f3, f4);
      float f5 = ((float)Math.PI / 6F);
      float f6 = f2 * 0.1F + f * 0.5F;
      float f7 = 0.08F + f1 * 0.4F;
      this.leftEar.zRot = (-(float)Math.PI / 6F) - Mth.cos(f6 * 1.2F) * f7;
      this.rightEar.zRot = ((float)Math.PI / 6F) + Mth.cos(f6) * f7;
      if (mob instanceof AbstractPiglin abstractpiglin) {
         PiglinArmPose piglinarmpose = abstractpiglin.getArmPose();
         if (piglinarmpose == PiglinArmPose.DANCING) {
            float f8 = f2 / 60.0F;
            this.rightEar.zRot = ((float)Math.PI / 6F) + ((float)Math.PI / 180F) * Mth.sin(f8 * 30.0F) * 10.0F;
            this.leftEar.zRot = (-(float)Math.PI / 6F) - ((float)Math.PI / 180F) * Mth.cos(f8 * 30.0F) * 10.0F;
            this.head.x = Mth.sin(f8 * 10.0F);
            this.head.y = Mth.sin(f8 * 40.0F) + 0.4F;
            this.rightArm.zRot = ((float)Math.PI / 180F) * (70.0F + Mth.cos(f8 * 40.0F) * 10.0F);
            this.leftArm.zRot = this.rightArm.zRot * -1.0F;
            this.rightArm.y = Mth.sin(f8 * 40.0F) * 0.5F + 1.5F;
            this.leftArm.y = Mth.sin(f8 * 40.0F) * 0.5F + 1.5F;
            this.body.y = Mth.sin(f8 * 40.0F) * 0.35F;
         } else if (piglinarmpose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0F) {
            this.holdWeaponHigh(mob);
         } else if (piglinarmpose == PiglinArmPose.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !mob.isLeftHanded());
         } else if (piglinarmpose == PiglinArmPose.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, mob, !mob.isLeftHanded());
         } else if (piglinarmpose == PiglinArmPose.ADMIRING_ITEM) {
            this.head.xRot = 0.5F;
            this.head.yRot = 0.0F;
            if (mob.isLeftHanded()) {
               this.rightArm.yRot = -0.5F;
               this.rightArm.xRot = -0.9F;
            } else {
               this.leftArm.yRot = 0.5F;
               this.leftArm.xRot = -0.9F;
            }
         }
      } else if (mob.getType() == EntityType.ZOMBIFIED_PIGLIN) {
         AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, mob.isAggressive(), this.attackTime, f2);
      }

      this.leftPants.copyFrom(this.leftLeg);
      this.rightPants.copyFrom(this.rightLeg);
      this.leftSleeve.copyFrom(this.leftArm);
      this.rightSleeve.copyFrom(this.rightArm);
      this.jacket.copyFrom(this.body);
      this.hat.copyFrom(this.head);
   }

   protected void setupAttackAnimation(T mob, float f) {
      if (this.attackTime > 0.0F && mob instanceof Piglin && ((Piglin)mob).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
         AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, mob, this.attackTime, f);
      } else {
         super.setupAttackAnimation(mob, f);
      }
   }

   private void holdWeaponHigh(T mob) {
      if (mob.isLeftHanded()) {
         this.leftArm.xRot = -1.8F;
      } else {
         this.rightArm.xRot = -1.8F;
      }

   }
}
