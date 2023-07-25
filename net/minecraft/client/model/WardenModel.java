package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.animation.definitions.WardenAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenModel<T extends Warden> extends HierarchicalModel<T> {
   private static final float DEFAULT_ARM_X_Y = 13.0F;
   private static final float DEFAULT_ARM_Z = 1.0F;
   private final ModelPart root;
   protected final ModelPart bone;
   protected final ModelPart body;
   protected final ModelPart head;
   protected final ModelPart rightTendril;
   protected final ModelPart leftTendril;
   protected final ModelPart leftLeg;
   protected final ModelPart leftArm;
   protected final ModelPart leftRibcage;
   protected final ModelPart rightArm;
   protected final ModelPart rightLeg;
   protected final ModelPart rightRibcage;
   private final List<ModelPart> tendrilsLayerModelParts;
   private final List<ModelPart> heartLayerModelParts;
   private final List<ModelPart> bioluminescentLayerModelParts;
   private final List<ModelPart> pulsatingSpotsLayerModelParts;

   public WardenModel(ModelPart modelpart) {
      super(RenderType::entityCutoutNoCull);
      this.root = modelpart;
      this.bone = modelpart.getChild("bone");
      this.body = this.bone.getChild("body");
      this.head = this.body.getChild("head");
      this.rightLeg = this.bone.getChild("right_leg");
      this.leftLeg = this.bone.getChild("left_leg");
      this.rightArm = this.body.getChild("right_arm");
      this.leftArm = this.body.getChild("left_arm");
      this.rightTendril = this.head.getChild("right_tendril");
      this.leftTendril = this.head.getChild("left_tendril");
      this.rightRibcage = this.body.getChild("right_ribcage");
      this.leftRibcage = this.body.getChild("left_ribcage");
      this.tendrilsLayerModelParts = ImmutableList.of(this.leftTendril, this.rightTendril);
      this.heartLayerModelParts = ImmutableList.of(this.body);
      this.bioluminescentLayerModelParts = ImmutableList.of(this.head, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
      this.pulsatingSpotsLayerModelParts = ImmutableList.of(this.body, this.head, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -13.0F, -4.0F, 18.0F, 21.0F, 11.0F), PartPose.offset(0.0F, -21.0F, 0.0F));
      partdefinition2.addOrReplaceChild("right_ribcage", CubeListBuilder.create().texOffs(90, 11).addBox(-2.0F, -11.0F, -0.1F, 9.0F, 21.0F, 0.0F), PartPose.offset(-7.0F, -2.0F, -4.0F));
      partdefinition2.addOrReplaceChild("left_ribcage", CubeListBuilder.create().texOffs(90, 11).mirror().addBox(-7.0F, -11.0F, -0.1F, 9.0F, 21.0F, 0.0F).mirror(false), PartPose.offset(7.0F, -2.0F, -4.0F));
      PartDefinition partdefinition3 = partdefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -16.0F, -5.0F, 16.0F, 16.0F, 10.0F), PartPose.offset(0.0F, -13.0F, 0.0F));
      partdefinition3.addOrReplaceChild("right_tendril", CubeListBuilder.create().texOffs(52, 32).addBox(-16.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), PartPose.offset(-8.0F, -12.0F, 0.0F));
      partdefinition3.addOrReplaceChild("left_tendril", CubeListBuilder.create().texOffs(58, 0).addBox(0.0F, -13.0F, 0.0F, 16.0F, 16.0F, 0.0F), PartPose.offset(8.0F, -12.0F, 0.0F));
      partdefinition2.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(44, 50).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), PartPose.offset(-13.0F, -13.0F, 1.0F));
      partdefinition2.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 58).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 28.0F, 8.0F), PartPose.offset(13.0F, -13.0F, 1.0F));
      partdefinition1.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(76, 48).addBox(-3.1F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), PartPose.offset(-5.9F, -13.0F, 0.0F));
      partdefinition1.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(76, 76).addBox(-2.9F, 0.0F, -3.0F, 6.0F, 13.0F, 6.0F), PartPose.offset(5.9F, -13.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(T warden, float f, float f1, float f2, float f3, float f4) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      float f5 = f2 - (float)warden.tickCount;
      this.animateHeadLookTarget(f3, f4);
      this.animateWalk(f, f1);
      this.animateIdlePose(f2);
      this.animateTendrils(warden, f2, f5);
      this.animate(warden.attackAnimationState, WardenAnimation.WARDEN_ATTACK, f2);
      this.animate(warden.sonicBoomAnimationState, WardenAnimation.WARDEN_SONIC_BOOM, f2);
      this.animate(warden.diggingAnimationState, WardenAnimation.WARDEN_DIG, f2);
      this.animate(warden.emergeAnimationState, WardenAnimation.WARDEN_EMERGE, f2);
      this.animate(warden.roarAnimationState, WardenAnimation.WARDEN_ROAR, f2);
      this.animate(warden.sniffAnimationState, WardenAnimation.WARDEN_SNIFF, f2);
   }

   private void animateHeadLookTarget(float f, float f1) {
      this.head.xRot = f1 * ((float)Math.PI / 180F);
      this.head.yRot = f * ((float)Math.PI / 180F);
   }

   private void animateIdlePose(float f) {
      float f1 = f * 0.1F;
      float f2 = Mth.cos(f1);
      float f3 = Mth.sin(f1);
      this.head.zRot += 0.06F * f2;
      this.head.xRot += 0.06F * f3;
      this.body.zRot += 0.025F * f3;
      this.body.xRot += 0.025F * f2;
   }

   private void animateWalk(float f, float f1) {
      float f2 = Math.min(0.5F, 3.0F * f1);
      float f3 = f * 0.8662F;
      float f4 = Mth.cos(f3);
      float f5 = Mth.sin(f3);
      float f6 = Math.min(0.35F, f2);
      this.head.zRot += 0.3F * f5 * f2;
      this.head.xRot += 1.2F * Mth.cos(f3 + ((float)Math.PI / 2F)) * f6;
      this.body.zRot = 0.1F * f5 * f2;
      this.body.xRot = 1.0F * f4 * f6;
      this.leftLeg.xRot = 1.0F * f4 * f2;
      this.rightLeg.xRot = 1.0F * Mth.cos(f3 + (float)Math.PI) * f2;
      this.leftArm.xRot = -(0.8F * f4 * f2);
      this.leftArm.zRot = 0.0F;
      this.rightArm.xRot = -(0.8F * f5 * f2);
      this.rightArm.zRot = 0.0F;
      this.resetArmPoses();
   }

   private void resetArmPoses() {
      this.leftArm.yRot = 0.0F;
      this.leftArm.z = 1.0F;
      this.leftArm.x = 13.0F;
      this.leftArm.y = -13.0F;
      this.rightArm.yRot = 0.0F;
      this.rightArm.z = 1.0F;
      this.rightArm.x = -13.0F;
      this.rightArm.y = -13.0F;
   }

   private void animateTendrils(T warden, float f, float f1) {
      float f2 = warden.getTendrilAnimation(f1) * (float)(Math.cos((double)f * 2.25D) * Math.PI * (double)0.1F);
      this.leftTendril.xRot = f2;
      this.rightTendril.xRot = -f2;
   }

   public ModelPart root() {
      return this.root;
   }

   public List<ModelPart> getTendrilsLayerModelParts() {
      return this.tendrilsLayerModelParts;
   }

   public List<ModelPart> getHeartLayerModelParts() {
      return this.heartLayerModelParts;
   }

   public List<ModelPart> getBioluminescentLayerModelParts() {
      return this.bioluminescentLayerModelParts;
   }

   public List<ModelPart> getPulsatingSpotsLayerModelParts() {
      return this.pulsatingSpotsLayerModelParts;
   }
}
