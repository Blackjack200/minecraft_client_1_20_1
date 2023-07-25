package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;

public class AnimationUtils {
   public static void animateCrossbowHold(ModelPart modelpart, ModelPart modelpart1, ModelPart modelpart2, boolean flag) {
      ModelPart modelpart3 = flag ? modelpart : modelpart1;
      ModelPart modelpart4 = flag ? modelpart1 : modelpart;
      modelpart3.yRot = (flag ? -0.3F : 0.3F) + modelpart2.yRot;
      modelpart4.yRot = (flag ? 0.6F : -0.6F) + modelpart2.yRot;
      modelpart3.xRot = (-(float)Math.PI / 2F) + modelpart2.xRot + 0.1F;
      modelpart4.xRot = -1.5F + modelpart2.xRot;
   }

   public static void animateCrossbowCharge(ModelPart modelpart, ModelPart modelpart1, LivingEntity livingentity, boolean flag) {
      ModelPart modelpart2 = flag ? modelpart : modelpart1;
      ModelPart modelpart3 = flag ? modelpart1 : modelpart;
      modelpart2.yRot = flag ? -0.8F : 0.8F;
      modelpart2.xRot = -0.97079635F;
      modelpart3.xRot = modelpart2.xRot;
      float f = (float)CrossbowItem.getChargeDuration(livingentity.getUseItem());
      float f1 = Mth.clamp((float)livingentity.getTicksUsingItem(), 0.0F, f);
      float f2 = f1 / f;
      modelpart3.yRot = Mth.lerp(f2, 0.4F, 0.85F) * (float)(flag ? 1 : -1);
      modelpart3.xRot = Mth.lerp(f2, modelpart3.xRot, (-(float)Math.PI / 2F));
   }

   public static <T extends Mob> void swingWeaponDown(ModelPart modelpart, ModelPart modelpart1, T mob, float f, float f1) {
      float f2 = Mth.sin(f * (float)Math.PI);
      float f3 = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * (float)Math.PI);
      modelpart.zRot = 0.0F;
      modelpart1.zRot = 0.0F;
      modelpart.yRot = 0.15707964F;
      modelpart1.yRot = -0.15707964F;
      if (mob.getMainArm() == HumanoidArm.RIGHT) {
         modelpart.xRot = -1.8849558F + Mth.cos(f1 * 0.09F) * 0.15F;
         modelpart1.xRot = -0.0F + Mth.cos(f1 * 0.19F) * 0.5F;
         modelpart.xRot += f2 * 2.2F - f3 * 0.4F;
         modelpart1.xRot += f2 * 1.2F - f3 * 0.4F;
      } else {
         modelpart.xRot = -0.0F + Mth.cos(f1 * 0.19F) * 0.5F;
         modelpart1.xRot = -1.8849558F + Mth.cos(f1 * 0.09F) * 0.15F;
         modelpart.xRot += f2 * 1.2F - f3 * 0.4F;
         modelpart1.xRot += f2 * 2.2F - f3 * 0.4F;
      }

      bobArms(modelpart, modelpart1, f1);
   }

   public static void bobModelPart(ModelPart modelpart, float f, float f1) {
      modelpart.zRot += f1 * (Mth.cos(f * 0.09F) * 0.05F + 0.05F);
      modelpart.xRot += f1 * Mth.sin(f * 0.067F) * 0.05F;
   }

   public static void bobArms(ModelPart modelpart, ModelPart modelpart1, float f) {
      bobModelPart(modelpart, f, 1.0F);
      bobModelPart(modelpart1, f, -1.0F);
   }

   public static void animateZombieArms(ModelPart modelpart, ModelPart modelpart1, boolean flag, float f, float f1) {
      float f2 = Mth.sin(f * (float)Math.PI);
      float f3 = Mth.sin((1.0F - (1.0F - f) * (1.0F - f)) * (float)Math.PI);
      modelpart1.zRot = 0.0F;
      modelpart.zRot = 0.0F;
      modelpart1.yRot = -(0.1F - f2 * 0.6F);
      modelpart.yRot = 0.1F - f2 * 0.6F;
      float f4 = -(float)Math.PI / (flag ? 1.5F : 2.25F);
      modelpart1.xRot = f4;
      modelpart.xRot = f4;
      modelpart1.xRot += f2 * 1.2F - f3 * 0.4F;
      modelpart.xRot += f2 * 1.2F - f3 * 0.4F;
      bobArms(modelpart1, modelpart, f1);
   }
}
