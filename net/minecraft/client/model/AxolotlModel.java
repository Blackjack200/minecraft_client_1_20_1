package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LerpingModel;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.joml.Vector3f;

public class AxolotlModel<T extends Axolotl & LerpingModel> extends AgeableListModel<T> {
   public static final float SWIMMING_LEG_XROT = 1.8849558F;
   private final ModelPart tail;
   private final ModelPart leftHindLeg;
   private final ModelPart rightHindLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart body;
   private final ModelPart head;
   private final ModelPart topGills;
   private final ModelPart leftGills;
   private final ModelPart rightGills;

   public AxolotlModel(ModelPart modelpart) {
      super(true, 8.0F, 3.35F);
      this.body = modelpart.getChild("body");
      this.head = this.body.getChild("head");
      this.rightHindLeg = this.body.getChild("right_hind_leg");
      this.leftHindLeg = this.body.getChild("left_hind_leg");
      this.rightFrontLeg = this.body.getChild("right_front_leg");
      this.leftFrontLeg = this.body.getChild("left_front_leg");
      this.tail = this.body.getChild("tail");
      this.topGills = this.head.getChild("top_gills");
      this.leftGills = this.head.getChild("left_gills");
      this.rightGills = this.head.getChild("right_gills");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 11).addBox(-4.0F, -2.0F, -9.0F, 8.0F, 4.0F, 10.0F).texOffs(2, 17).addBox(0.0F, -3.0F, -8.0F, 0.0F, 5.0F, 9.0F), PartPose.offset(0.0F, 20.0F, 5.0F));
      CubeDeformation cubedeformation = new CubeDeformation(0.001F);
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 1).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 5.0F, 5.0F, cubedeformation), PartPose.offset(0.0F, 0.0F, -9.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(3, 37).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 3.0F, 0.0F, cubedeformation);
      CubeListBuilder cubelistbuilder1 = CubeListBuilder.create().texOffs(0, 40).addBox(-3.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubedeformation);
      CubeListBuilder cubelistbuilder2 = CubeListBuilder.create().texOffs(11, 40).addBox(0.0F, -5.0F, 0.0F, 3.0F, 7.0F, 0.0F, cubedeformation);
      partdefinition2.addOrReplaceChild("top_gills", cubelistbuilder, PartPose.offset(0.0F, -3.0F, -1.0F));
      partdefinition2.addOrReplaceChild("left_gills", cubelistbuilder1, PartPose.offset(-4.0F, 0.0F, -1.0F));
      partdefinition2.addOrReplaceChild("right_gills", cubelistbuilder2, PartPose.offset(4.0F, 0.0F, -1.0F));
      CubeListBuilder cubelistbuilder3 = CubeListBuilder.create().texOffs(2, 13).addBox(-1.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubedeformation);
      CubeListBuilder cubelistbuilder4 = CubeListBuilder.create().texOffs(2, 13).addBox(-2.0F, 0.0F, 0.0F, 3.0F, 5.0F, 0.0F, cubedeformation);
      partdefinition1.addOrReplaceChild("right_hind_leg", cubelistbuilder4, PartPose.offset(-3.5F, 1.0F, -1.0F));
      partdefinition1.addOrReplaceChild("left_hind_leg", cubelistbuilder3, PartPose.offset(3.5F, 1.0F, -1.0F));
      partdefinition1.addOrReplaceChild("right_front_leg", cubelistbuilder4, PartPose.offset(-3.5F, 1.0F, -8.0F));
      partdefinition1.addOrReplaceChild("left_front_leg", cubelistbuilder3, PartPose.offset(3.5F, 1.0F, -8.0F));
      partdefinition1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(2, 19).addBox(0.0F, -3.0F, 0.0F, 0.0F, 5.0F, 12.0F), PartPose.offset(0.0F, 0.0F, 1.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of();
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body);
   }

   public void setupAnim(T axolotl, float f, float f1, float f2, float f3, float f4) {
      this.setupInitialAnimationValues(axolotl, f3, f4);
      if (axolotl.isPlayingDead()) {
         this.setupPlayDeadAnimation(f3);
         this.saveAnimationValues(axolotl);
      } else {
         boolean flag = f1 > 1.0E-5F || axolotl.getXRot() != axolotl.xRotO || axolotl.getYRot() != axolotl.yRotO;
         if (axolotl.isInWaterOrBubble()) {
            if (flag) {
               this.setupSwimmingAnimation(f2, f4);
            } else {
               this.setupWaterHoveringAnimation(f2);
            }

            this.saveAnimationValues(axolotl);
         } else {
            if (axolotl.onGround()) {
               if (flag) {
                  this.setupGroundCrawlingAnimation(f2, f3);
               } else {
                  this.setupLayStillOnGroundAnimation(f2, f3);
               }
            }

            this.saveAnimationValues(axolotl);
         }
      }
   }

   private void saveAnimationValues(T axolotl) {
      Map<String, Vector3f> map = axolotl.getModelRotationValues();
      map.put("body", this.getRotationVector(this.body));
      map.put("head", this.getRotationVector(this.head));
      map.put("right_hind_leg", this.getRotationVector(this.rightHindLeg));
      map.put("left_hind_leg", this.getRotationVector(this.leftHindLeg));
      map.put("right_front_leg", this.getRotationVector(this.rightFrontLeg));
      map.put("left_front_leg", this.getRotationVector(this.leftFrontLeg));
      map.put("tail", this.getRotationVector(this.tail));
      map.put("top_gills", this.getRotationVector(this.topGills));
      map.put("left_gills", this.getRotationVector(this.leftGills));
      map.put("right_gills", this.getRotationVector(this.rightGills));
   }

   private Vector3f getRotationVector(ModelPart modelpart) {
      return new Vector3f(modelpart.xRot, modelpart.yRot, modelpart.zRot);
   }

   private void setRotationFromVector(ModelPart modelpart, Vector3f vector3f) {
      modelpart.setRotation(vector3f.x(), vector3f.y(), vector3f.z());
   }

   private void setupInitialAnimationValues(T axolotl, float f, float f1) {
      this.body.x = 0.0F;
      this.head.y = 0.0F;
      this.body.y = 20.0F;
      Map<String, Vector3f> map = axolotl.getModelRotationValues();
      if (map.isEmpty()) {
         this.body.setRotation(f1 * ((float)Math.PI / 180F), f * ((float)Math.PI / 180F), 0.0F);
         this.head.setRotation(0.0F, 0.0F, 0.0F);
         this.leftHindLeg.setRotation(0.0F, 0.0F, 0.0F);
         this.rightHindLeg.setRotation(0.0F, 0.0F, 0.0F);
         this.leftFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
         this.rightFrontLeg.setRotation(0.0F, 0.0F, 0.0F);
         this.leftGills.setRotation(0.0F, 0.0F, 0.0F);
         this.rightGills.setRotation(0.0F, 0.0F, 0.0F);
         this.topGills.setRotation(0.0F, 0.0F, 0.0F);
         this.tail.setRotation(0.0F, 0.0F, 0.0F);
      } else {
         this.setRotationFromVector(this.body, map.get("body"));
         this.setRotationFromVector(this.head, map.get("head"));
         this.setRotationFromVector(this.leftHindLeg, map.get("left_hind_leg"));
         this.setRotationFromVector(this.rightHindLeg, map.get("right_hind_leg"));
         this.setRotationFromVector(this.leftFrontLeg, map.get("left_front_leg"));
         this.setRotationFromVector(this.rightFrontLeg, map.get("right_front_leg"));
         this.setRotationFromVector(this.leftGills, map.get("left_gills"));
         this.setRotationFromVector(this.rightGills, map.get("right_gills"));
         this.setRotationFromVector(this.topGills, map.get("top_gills"));
         this.setRotationFromVector(this.tail, map.get("tail"));
      }

   }

   private float lerpTo(float f, float f1) {
      return this.lerpTo(0.05F, f, f1);
   }

   private float lerpTo(float f, float f1, float f2) {
      return Mth.rotLerp(f, f1, f2);
   }

   private void lerpPart(ModelPart modelpart, float f, float f1, float f2) {
      modelpart.setRotation(this.lerpTo(modelpart.xRot, f), this.lerpTo(modelpart.yRot, f1), this.lerpTo(modelpart.zRot, f2));
   }

   private void setupLayStillOnGroundAnimation(float f, float f1) {
      float f2 = f * 0.09F;
      float f3 = Mth.sin(f2);
      float f4 = Mth.cos(f2);
      float f5 = f3 * f3 - 2.0F * f3;
      float f6 = f4 * f4 - 3.0F * f3;
      this.head.xRot = this.lerpTo(this.head.xRot, -0.09F * f5);
      this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
      this.head.zRot = this.lerpTo(this.head.zRot, -0.2F);
      this.tail.yRot = this.lerpTo(this.tail.yRot, -0.1F + 0.1F * f5);
      this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F + 0.05F * f6);
      this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
      this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
      this.lerpPart(this.leftHindLeg, 1.1F, 1.0F, 0.0F);
      this.lerpPart(this.leftFrontLeg, 0.8F, 2.3F, -0.5F);
      this.applyMirrorLegRotations();
      this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
      this.body.yRot = this.lerpTo(this.body.yRot, f1 * ((float)Math.PI / 180F));
      this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
   }

   private void setupGroundCrawlingAnimation(float f, float f1) {
      float f2 = f * 0.11F;
      float f3 = Mth.cos(f2);
      float f4 = (f3 * f3 - 2.0F * f3) / 5.0F;
      float f5 = 0.7F * f3;
      this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
      this.head.yRot = this.lerpTo(this.head.yRot, 0.09F * f3);
      this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
      this.tail.yRot = this.lerpTo(this.tail.yRot, this.head.yRot);
      this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.6F - 0.08F * (f3 * f3 + 2.0F * Mth.sin(f2)));
      this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -this.topGills.xRot);
      this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
      this.lerpPart(this.leftHindLeg, 0.9424779F, 1.5F - f4, -0.1F);
      this.lerpPart(this.leftFrontLeg, 1.0995574F, ((float)Math.PI / 2F) - f5, 0.0F);
      this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -1.0F - f4, 0.0F);
      this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, (-(float)Math.PI / 2F) - f5, 0.0F);
      this.body.xRot = this.lerpTo(0.2F, this.body.xRot, 0.0F);
      this.body.yRot = this.lerpTo(this.body.yRot, f1 * ((float)Math.PI / 180F));
      this.body.zRot = this.lerpTo(this.body.zRot, 0.0F);
   }

   private void setupWaterHoveringAnimation(float f) {
      float f1 = f * 0.075F;
      float f2 = Mth.cos(f1);
      float f3 = Mth.sin(f1) * 0.15F;
      this.body.xRot = this.lerpTo(this.body.xRot, -0.15F + 0.075F * f2);
      this.body.y -= f3;
      this.head.xRot = this.lerpTo(this.head.xRot, -this.body.xRot);
      this.topGills.xRot = this.lerpTo(this.topGills.xRot, 0.2F * f2);
      this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, -0.3F * f2 - 0.19F);
      this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
      this.lerpPart(this.leftHindLeg, 2.3561945F - f2 * 0.11F, 0.47123894F, 1.7278761F);
      this.lerpPart(this.leftFrontLeg, ((float)Math.PI / 4F) - f2 * 0.2F, 2.042035F, 0.0F);
      this.applyMirrorLegRotations();
      this.tail.yRot = this.lerpTo(this.tail.yRot, 0.5F * f2);
      this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
      this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
   }

   private void setupSwimmingAnimation(float f, float f1) {
      float f2 = f * 0.33F;
      float f3 = Mth.sin(f2);
      float f4 = Mth.cos(f2);
      float f5 = 0.13F * f3;
      this.body.xRot = this.lerpTo(0.1F, this.body.xRot, f1 * ((float)Math.PI / 180F) + f5);
      this.head.xRot = -f5 * 1.8F;
      this.body.y -= 0.45F * f4;
      this.topGills.xRot = this.lerpTo(this.topGills.xRot, -0.5F * f3 - 0.8F);
      this.leftGills.yRot = this.lerpTo(this.leftGills.yRot, 0.3F * f3 + 0.9F);
      this.rightGills.yRot = this.lerpTo(this.rightGills.yRot, -this.leftGills.yRot);
      this.tail.yRot = this.lerpTo(this.tail.yRot, 0.3F * Mth.cos(f2 * 0.9F));
      this.lerpPart(this.leftHindLeg, 1.8849558F, -0.4F * f3, ((float)Math.PI / 2F));
      this.lerpPart(this.leftFrontLeg, 1.8849558F, -0.2F * f4 - 0.1F, ((float)Math.PI / 2F));
      this.applyMirrorLegRotations();
      this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
      this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
   }

   private void setupPlayDeadAnimation(float f) {
      this.lerpPart(this.leftHindLeg, 1.4137167F, 1.0995574F, ((float)Math.PI / 4F));
      this.lerpPart(this.leftFrontLeg, ((float)Math.PI / 4F), 2.042035F, 0.0F);
      this.body.xRot = this.lerpTo(this.body.xRot, -0.15F);
      this.body.zRot = this.lerpTo(this.body.zRot, 0.35F);
      this.applyMirrorLegRotations();
      this.body.yRot = this.lerpTo(this.body.yRot, f * ((float)Math.PI / 180F));
      this.head.xRot = this.lerpTo(this.head.xRot, 0.0F);
      this.head.yRot = this.lerpTo(this.head.yRot, 0.0F);
      this.head.zRot = this.lerpTo(this.head.zRot, 0.0F);
      this.tail.yRot = this.lerpTo(this.tail.yRot, 0.0F);
      this.lerpPart(this.topGills, 0.0F, 0.0F, 0.0F);
      this.lerpPart(this.leftGills, 0.0F, 0.0F, 0.0F);
      this.lerpPart(this.rightGills, 0.0F, 0.0F, 0.0F);
   }

   private void applyMirrorLegRotations() {
      this.lerpPart(this.rightHindLeg, this.leftHindLeg.xRot, -this.leftHindLeg.yRot, -this.leftHindLeg.zRot);
      this.lerpPart(this.rightFrontLeg, this.leftFrontLeg.xRot, -this.leftFrontLeg.yRot, -this.leftFrontLeg.zRot);
   }
}
