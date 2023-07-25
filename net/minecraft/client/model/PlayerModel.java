package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class PlayerModel<T extends LivingEntity> extends HumanoidModel<T> {
   private static final String EAR = "ear";
   private static final String CLOAK = "cloak";
   private static final String LEFT_SLEEVE = "left_sleeve";
   private static final String RIGHT_SLEEVE = "right_sleeve";
   private static final String LEFT_PANTS = "left_pants";
   private static final String RIGHT_PANTS = "right_pants";
   private final List<ModelPart> parts;
   public final ModelPart leftSleeve;
   public final ModelPart rightSleeve;
   public final ModelPart leftPants;
   public final ModelPart rightPants;
   public final ModelPart jacket;
   private final ModelPart cloak;
   private final ModelPart ear;
   private final boolean slim;

   public PlayerModel(ModelPart modelpart, boolean flag) {
      super(modelpart, RenderType::entityTranslucent);
      this.slim = flag;
      this.ear = modelpart.getChild("ear");
      this.cloak = modelpart.getChild("cloak");
      this.leftSleeve = modelpart.getChild("left_sleeve");
      this.rightSleeve = modelpart.getChild("right_sleeve");
      this.leftPants = modelpart.getChild("left_pants");
      this.rightPants = modelpart.getChild("right_pants");
      this.jacket = modelpart.getChild("jacket");
      this.parts = modelpart.getAllParts().filter((modelpart1) -> !modelpart1.isEmpty()).collect(ImmutableList.toImmutableList());
   }

   public static MeshDefinition createMesh(CubeDeformation cubedeformation, boolean flag) {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(cubedeformation, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(24, 0).addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, cubedeformation), PartPose.ZERO);
      partdefinition.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, cubedeformation, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
      float f = 0.25F;
      if (flag) {
         partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(5.0F, 2.5F, 0.0F));
         partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(-5.0F, 2.5F, 0.0F));
         partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
         partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.offset(-5.0F, 2.5F, 0.0F));
      } else {
         partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(5.0F, 2.0F, 0.0F));
         partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
         partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
      }

      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation), PartPose.offset(1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubedeformation.extend(0.25F)), PartPose.ZERO);
      return meshdefinition;
   }

   protected Iterable<ModelPart> bodyParts() {
      return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
   }

   public void renderEars(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j) {
      this.ear.copyFrom(this.head);
      this.ear.x = 0.0F;
      this.ear.y = 0.0F;
      this.ear.render(posestack, vertexconsumer, i, j);
   }

   public void renderCloak(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j) {
      this.cloak.render(posestack, vertexconsumer, i, j);
   }

   public void setupAnim(T livingentity, float f, float f1, float f2, float f3, float f4) {
      super.setupAnim(livingentity, f, f1, f2, f3, f4);
      this.leftPants.copyFrom(this.leftLeg);
      this.rightPants.copyFrom(this.rightLeg);
      this.leftSleeve.copyFrom(this.leftArm);
      this.rightSleeve.copyFrom(this.rightArm);
      this.jacket.copyFrom(this.body);
      if (livingentity.getItemBySlot(EquipmentSlot.CHEST).isEmpty()) {
         if (livingentity.isCrouching()) {
            this.cloak.z = 1.4F;
            this.cloak.y = 1.85F;
         } else {
            this.cloak.z = 0.0F;
            this.cloak.y = 0.0F;
         }
      } else if (livingentity.isCrouching()) {
         this.cloak.z = 0.3F;
         this.cloak.y = 0.8F;
      } else {
         this.cloak.z = -1.1F;
         this.cloak.y = -0.85F;
      }

   }

   public void setAllVisible(boolean flag) {
      super.setAllVisible(flag);
      this.leftSleeve.visible = flag;
      this.rightSleeve.visible = flag;
      this.leftPants.visible = flag;
      this.rightPants.visible = flag;
      this.jacket.visible = flag;
      this.cloak.visible = flag;
      this.ear.visible = flag;
   }

   public void translateToHand(HumanoidArm humanoidarm, PoseStack posestack) {
      ModelPart modelpart = this.getArm(humanoidarm);
      if (this.slim) {
         float f = 0.5F * (float)(humanoidarm == HumanoidArm.RIGHT ? 1 : -1);
         modelpart.x += f;
         modelpart.translateAndRotate(posestack);
         modelpart.x -= f;
      } else {
         modelpart.translateAndRotate(posestack);
      }

   }

   public ModelPart getRandomModelPart(RandomSource randomsource) {
      return this.parts.get(randomsource.nextInt(this.parts.size()));
   }
}
