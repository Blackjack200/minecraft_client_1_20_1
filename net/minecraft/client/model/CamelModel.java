package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.definitions.CamelAnimation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.camel.Camel;

public class CamelModel<T extends Camel> extends HierarchicalModel<T> {
   private static final float MAX_WALK_ANIMATION_SPEED = 2.0F;
   private static final float WALK_ANIMATION_SCALE_FACTOR = 2.5F;
   private static final float BABY_SCALE = 0.45F;
   private static final float BABY_Y_OFFSET = 29.35F;
   private static final String SADDLE = "saddle";
   private static final String BRIDLE = "bridle";
   private static final String REINS = "reins";
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart[] saddleParts;
   private final ModelPart[] ridingParts;

   public CamelModel(ModelPart modelpart) {
      this.root = modelpart;
      ModelPart modelpart1 = modelpart.getChild("body");
      this.head = modelpart1.getChild("head");
      this.saddleParts = new ModelPart[]{modelpart1.getChild("saddle"), this.head.getChild("bridle")};
      this.ridingParts = new ModelPart[]{this.head.getChild("reins")};
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      CubeDeformation cubedeformation = new CubeDeformation(0.1F);
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F), PartPose.offset(0.0F, 4.0F, 9.5F));
      partdefinition1.addOrReplaceChild("hump", CubeListBuilder.create().texOffs(74, 0).addBox(-4.5F, -5.0F, -5.5F, 9.0F, 5.0F, 11.0F), PartPose.offset(0.0F, -12.0F, -10.0F));
      partdefinition1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(122, 0).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 0.0F), PartPose.offset(0.0F, -9.0F, 3.5F));
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(60, 24).addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F).texOffs(21, 0).addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F).texOffs(50, 0).addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F), PartPose.offset(0.0F, -3.0F, -19.5F));
      partdefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(3.0F, -21.0F, -9.5F));
      partdefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(67, 0).addBox(-2.5F, 0.5F, -1.0F, 3.0F, 1.0F, 2.0F), PartPose.offset(-3.0F, -21.0F, -9.5F));
      partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(58, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(4.9F, 1.0F, 9.5F));
      partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(94, 16).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(-4.9F, 1.0F, 9.5F));
      partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(4.9F, 1.0F, -10.5F));
      partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 26).addBox(-2.5F, 2.0F, -2.5F, 5.0F, 21.0F, 5.0F), PartPose.offset(-4.9F, 1.0F, -10.5F));
      partdefinition1.addOrReplaceChild("saddle", CubeListBuilder.create().texOffs(74, 64).addBox(-4.5F, -17.0F, -15.5F, 9.0F, 5.0F, 11.0F, cubedeformation).texOffs(92, 114).addBox(-3.5F, -20.0F, -15.5F, 7.0F, 3.0F, 11.0F, cubedeformation).texOffs(0, 89).addBox(-7.5F, -12.0F, -23.5F, 15.0F, 12.0F, 27.0F, cubedeformation), PartPose.offset(0.0F, 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("reins", CubeListBuilder.create().texOffs(98, 42).addBox(3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F).texOffs(84, 57).addBox(-3.5F, -18.0F, -2.0F, 7.0F, 7.0F, 0.0F).texOffs(98, 42).addBox(-3.51F, -18.0F, -17.0F, 0.0F, 7.0F, 15.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("bridle", CubeListBuilder.create().texOffs(60, 87).addBox(-3.5F, -7.0F, -15.0F, 7.0F, 8.0F, 19.0F, cubedeformation).texOffs(21, 64).addBox(-3.5F, -21.0F, -15.0F, 7.0F, 14.0F, 7.0F, cubedeformation).texOffs(50, 64).addBox(-2.5F, -21.0F, -21.0F, 5.0F, 5.0F, 6.0F, cubedeformation).texOffs(74, 70).addBox(2.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F).texOffs(74, 70).mirror().addBox(-3.5F, -19.0F, -18.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public void setupAnim(T camel, float f, float f1, float f2, float f3, float f4) {
      this.root().getAllParts().forEach(ModelPart::resetPose);
      this.applyHeadRotation(camel, f3, f4, f2);
      this.toggleInvisibleParts(camel);
      this.animateWalk(CamelAnimation.CAMEL_WALK, f, f1, 2.0F, 2.5F);
      this.animate(camel.sitAnimationState, CamelAnimation.CAMEL_SIT, f2, 1.0F);
      this.animate(camel.sitPoseAnimationState, CamelAnimation.CAMEL_SIT_POSE, f2, 1.0F);
      this.animate(camel.sitUpAnimationState, CamelAnimation.CAMEL_STANDUP, f2, 1.0F);
      this.animate(camel.idleAnimationState, CamelAnimation.CAMEL_IDLE, f2, 1.0F);
      this.animate(camel.dashAnimationState, CamelAnimation.CAMEL_DASH, f2, 1.0F);
   }

   private void applyHeadRotation(T camel, float f, float f1, float f2) {
      f = Mth.clamp(f, -30.0F, 30.0F);
      f1 = Mth.clamp(f1, -25.0F, 45.0F);
      if (camel.getJumpCooldown() > 0) {
         float f3 = f2 - (float)camel.tickCount;
         float f4 = 45.0F * ((float)camel.getJumpCooldown() - f3) / 55.0F;
         f1 = Mth.clamp(f1 + f4, -25.0F, 70.0F);
      }

      this.head.yRot = f * ((float)Math.PI / 180F);
      this.head.xRot = f1 * ((float)Math.PI / 180F);
   }

   private void toggleInvisibleParts(T camel) {
      boolean flag = camel.isSaddled();
      boolean flag1 = camel.isVehicle();

      for(ModelPart modelpart : this.saddleParts) {
         modelpart.visible = flag;
      }

      for(ModelPart modelpart1 : this.ridingParts) {
         modelpart1.visible = flag1 && flag;
      }

   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      if (this.young) {
         posestack.pushPose();
         posestack.scale(0.45F, 0.45F, 0.45F);
         posestack.translate(0.0F, 1.834375F, 0.0F);
         this.root().render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
         posestack.popPose();
      } else {
         this.root().render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
      }

   }

   public ModelPart root() {
      return this.root;
   }
}
