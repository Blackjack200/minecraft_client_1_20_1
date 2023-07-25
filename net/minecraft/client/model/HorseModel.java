package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class HorseModel<T extends AbstractHorse> extends AgeableListModel<T> {
   private static final float DEG_125 = 2.1816616F;
   private static final float DEG_60 = ((float)Math.PI / 3F);
   private static final float DEG_45 = ((float)Math.PI / 4F);
   private static final float DEG_30 = ((float)Math.PI / 6F);
   private static final float DEG_15 = 0.2617994F;
   protected static final String HEAD_PARTS = "head_parts";
   private static final String LEFT_HIND_BABY_LEG = "left_hind_baby_leg";
   private static final String RIGHT_HIND_BABY_LEG = "right_hind_baby_leg";
   private static final String LEFT_FRONT_BABY_LEG = "left_front_baby_leg";
   private static final String RIGHT_FRONT_BABY_LEG = "right_front_baby_leg";
   private static final String SADDLE = "saddle";
   private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
   private static final String LEFT_SADDLE_LINE = "left_saddle_line";
   private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
   private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
   private static final String HEAD_SADDLE = "head_saddle";
   private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
   protected final ModelPart body;
   protected final ModelPart headParts;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightHindBabyLeg;
   private final ModelPart leftHindBabyLeg;
   private final ModelPart rightFrontBabyLeg;
   private final ModelPart leftFrontBabyLeg;
   private final ModelPart tail;
   private final ModelPart[] saddleParts;
   private final ModelPart[] ridingParts;

   public HorseModel(ModelPart modelpart) {
      super(true, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F);
      this.body = modelpart.getChild("body");
      this.headParts = modelpart.getChild("head_parts");
      this.rightHindLeg = modelpart.getChild("right_hind_leg");
      this.leftHindLeg = modelpart.getChild("left_hind_leg");
      this.rightFrontLeg = modelpart.getChild("right_front_leg");
      this.leftFrontLeg = modelpart.getChild("left_front_leg");
      this.rightHindBabyLeg = modelpart.getChild("right_hind_baby_leg");
      this.leftHindBabyLeg = modelpart.getChild("left_hind_baby_leg");
      this.rightFrontBabyLeg = modelpart.getChild("right_front_baby_leg");
      this.leftFrontBabyLeg = modelpart.getChild("left_front_baby_leg");
      this.tail = this.body.getChild("tail");
      ModelPart modelpart1 = this.body.getChild("saddle");
      ModelPart modelpart2 = this.headParts.getChild("left_saddle_mouth");
      ModelPart modelpart3 = this.headParts.getChild("right_saddle_mouth");
      ModelPart modelpart4 = this.headParts.getChild("left_saddle_line");
      ModelPart modelpart5 = this.headParts.getChild("right_saddle_line");
      ModelPart modelpart6 = this.headParts.getChild("head_saddle");
      ModelPart modelpart7 = this.headParts.getChild("mouth_saddle_wrap");
      this.saddleParts = new ModelPart[]{modelpart1, modelpart2, modelpart3, modelpart6, modelpart7};
      this.ridingParts = new ModelPart[]{modelpart4, modelpart5};
   }

   public static MeshDefinition createBodyMesh(CubeDeformation cubedeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new CubeDeformation(0.05F)), PartPose.offset(0.0F, 11.0F, 5.0F));
      PartDefinition partdefinition2 = partdefinition.addOrReplaceChild("head_parts", CubeListBuilder.create().texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, ((float)Math.PI / 6F), 0.0F, 0.0F));
      PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, cubedeformation), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F, cubedeformation), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, cubedeformation), PartPose.ZERO);
      partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation), PartPose.offset(4.0F, 14.0F, 7.0F));
      partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation), PartPose.offset(-4.0F, 14.0F, 7.0F));
      partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation), PartPose.offset(4.0F, 14.0F, -12.0F));
      partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation), PartPose.offset(-4.0F, 14.0F, -12.0F));
      CubeDeformation cubedeformation1 = cubedeformation.extend(0.0F, 5.5F, 0.0F);
      partdefinition.addOrReplaceChild("left_hind_baby_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation1), PartPose.offset(4.0F, 14.0F, 7.0F));
      partdefinition.addOrReplaceChild("right_hind_baby_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, cubedeformation1), PartPose.offset(-4.0F, 14.0F, 7.0F));
      partdefinition.addOrReplaceChild("left_front_baby_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation1), PartPose.offset(4.0F, 14.0F, -12.0F));
      partdefinition.addOrReplaceChild("right_front_baby_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, cubedeformation1), PartPose.offset(-4.0F, 14.0F, -12.0F));
      partdefinition1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, cubedeformation), PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, ((float)Math.PI / 6F), 0.0F, 0.0F));
      partdefinition1.addOrReplaceChild("saddle", CubeListBuilder.create().texOffs(26, 0).addBox(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.5F)), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("left_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, cubedeformation), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("right_saddle_mouth", CubeListBuilder.create().texOffs(29, 5).addBox(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, cubedeformation), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("left_saddle_line", CubeListBuilder.create().texOffs(32, 2).addBox(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F), PartPose.rotation((-(float)Math.PI / 6F), 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("right_saddle_line", CubeListBuilder.create().texOffs(32, 2).addBox(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F), PartPose.rotation((-(float)Math.PI / 6F), 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("head_saddle", CubeListBuilder.create().texOffs(1, 1).addBox(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.22F)), PartPose.ZERO);
      partdefinition2.addOrReplaceChild("mouth_saddle_wrap", CubeListBuilder.create().texOffs(19, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.2F)), PartPose.ZERO);
      partdefinition3.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO);
      partdefinition3.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.001F)), PartPose.ZERO);
      return meshdefinition;
   }

   public void setupAnim(T abstracthorse, float f, float f1, float f2, float f3, float f4) {
      boolean flag = abstracthorse.isSaddled();
      boolean flag1 = abstracthorse.isVehicle();

      for(ModelPart modelpart : this.saddleParts) {
         modelpart.visible = flag;
      }

      for(ModelPart modelpart1 : this.ridingParts) {
         modelpart1.visible = flag1 && flag;
      }

      this.body.y = 11.0F;
   }

   public Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.headParts);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightHindBabyLeg, this.leftHindBabyLeg, this.rightFrontBabyLeg, this.leftFrontBabyLeg);
   }

   public void prepareMobModel(T abstracthorse, float f, float f1, float f2) {
      super.prepareMobModel(abstracthorse, f, f1, f2);
      float f3 = Mth.rotLerp(f2, abstracthorse.yBodyRotO, abstracthorse.yBodyRot);
      float f4 = Mth.rotLerp(f2, abstracthorse.yHeadRotO, abstracthorse.yHeadRot);
      float f5 = Mth.lerp(f2, abstracthorse.xRotO, abstracthorse.getXRot());
      float f6 = f4 - f3;
      float f7 = f5 * ((float)Math.PI / 180F);
      if (f6 > 20.0F) {
         f6 = 20.0F;
      }

      if (f6 < -20.0F) {
         f6 = -20.0F;
      }

      if (f1 > 0.2F) {
         f7 += Mth.cos(f * 0.8F) * 0.15F * f1;
      }

      float f8 = abstracthorse.getEatAnim(f2);
      float f9 = abstracthorse.getStandAnim(f2);
      float f10 = 1.0F - f9;
      float f11 = abstracthorse.getMouthAnim(f2);
      boolean flag = abstracthorse.tailCounter != 0;
      float f12 = (float)abstracthorse.tickCount + f2;
      this.headParts.y = 4.0F;
      this.headParts.z = -12.0F;
      this.body.xRot = 0.0F;
      this.headParts.xRot = ((float)Math.PI / 6F) + f7;
      this.headParts.yRot = f6 * ((float)Math.PI / 180F);
      float f13 = abstracthorse.isInWater() ? 0.2F : 1.0F;
      float f14 = Mth.cos(f13 * f * 0.6662F + (float)Math.PI);
      float f15 = f14 * 0.8F * f1;
      float f16 = (1.0F - Math.max(f9, f8)) * (((float)Math.PI / 6F) + f7 + f11 * Mth.sin(f12) * 0.05F);
      this.headParts.xRot = f9 * (0.2617994F + f7) + f8 * (2.1816616F + Mth.sin(f12) * 0.05F) + f16;
      this.headParts.yRot = f9 * f6 * ((float)Math.PI / 180F) + (1.0F - Math.max(f9, f8)) * this.headParts.yRot;
      this.headParts.y = f9 * -4.0F + f8 * 11.0F + (1.0F - Math.max(f9, f8)) * this.headParts.y;
      this.headParts.z = f9 * -4.0F + f8 * -12.0F + (1.0F - Math.max(f9, f8)) * this.headParts.z;
      this.body.xRot = f9 * (-(float)Math.PI / 4F) + f10 * this.body.xRot;
      float f17 = 0.2617994F * f9;
      float f18 = Mth.cos(f12 * 0.6F + (float)Math.PI);
      this.leftFrontLeg.y = 2.0F * f9 + 14.0F * f10;
      this.leftFrontLeg.z = -6.0F * f9 - 10.0F * f10;
      this.rightFrontLeg.y = this.leftFrontLeg.y;
      this.rightFrontLeg.z = this.leftFrontLeg.z;
      float f19 = ((-(float)Math.PI / 3F) + f18) * f9 + f15 * f10;
      float f20 = ((-(float)Math.PI / 3F) - f18) * f9 - f15 * f10;
      this.leftHindLeg.xRot = f17 - f14 * 0.5F * f1 * f10;
      this.rightHindLeg.xRot = f17 + f14 * 0.5F * f1 * f10;
      this.leftFrontLeg.xRot = f19;
      this.rightFrontLeg.xRot = f20;
      this.tail.xRot = ((float)Math.PI / 6F) + f1 * 0.75F;
      this.tail.y = -5.0F + f1;
      this.tail.z = 2.0F + f1 * 2.0F;
      if (flag) {
         this.tail.yRot = Mth.cos(f12 * 0.7F);
      } else {
         this.tail.yRot = 0.0F;
      }

      this.rightHindBabyLeg.y = this.rightHindLeg.y;
      this.rightHindBabyLeg.z = this.rightHindLeg.z;
      this.rightHindBabyLeg.xRot = this.rightHindLeg.xRot;
      this.leftHindBabyLeg.y = this.leftHindLeg.y;
      this.leftHindBabyLeg.z = this.leftHindLeg.z;
      this.leftHindBabyLeg.xRot = this.leftHindLeg.xRot;
      this.rightFrontBabyLeg.y = this.rightFrontLeg.y;
      this.rightFrontBabyLeg.z = this.rightFrontLeg.z;
      this.rightFrontBabyLeg.xRot = this.rightFrontLeg.xRot;
      this.leftFrontBabyLeg.y = this.leftFrontLeg.y;
      this.leftFrontBabyLeg.z = this.leftFrontLeg.z;
      this.leftFrontBabyLeg.xRot = this.leftFrontLeg.xRot;
      boolean flag1 = abstracthorse.isBaby();
      this.rightHindLeg.visible = !flag1;
      this.leftHindLeg.visible = !flag1;
      this.rightFrontLeg.visible = !flag1;
      this.leftFrontLeg.visible = !flag1;
      this.rightHindBabyLeg.visible = flag1;
      this.leftHindBabyLeg.visible = flag1;
      this.rightFrontBabyLeg.visible = flag1;
      this.leftFrontBabyLeg.visible = flag1;
      this.body.y = flag1 ? 10.8F : 0.0F;
   }
}
