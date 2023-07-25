package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;

public class ParrotModel extends HierarchicalModel<Parrot> {
   private static final String FEATHER = "feather";
   private final ModelPart root;
   private final ModelPart body;
   private final ModelPart tail;
   private final ModelPart leftWing;
   private final ModelPart rightWing;
   private final ModelPart head;
   private final ModelPart feather;
   private final ModelPart leftLeg;
   private final ModelPart rightLeg;

   public ParrotModel(ModelPart modelpart) {
      this.root = modelpart;
      this.body = modelpart.getChild("body");
      this.tail = modelpart.getChild("tail");
      this.leftWing = modelpart.getChild("left_wing");
      this.rightWing = modelpart.getChild("right_wing");
      this.head = modelpart.getChild("head");
      this.feather = this.head.getChild("feather");
      this.leftLeg = modelpart.getChild("left_leg");
      this.rightLeg = modelpart.getChild("right_leg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(2, 8).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 16.5F, -3.0F));
      partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, 1).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 21.07F, 1.16F));
      partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(1.5F, 16.94F, -2.76F));
      partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(19, 8).addBox(-0.5F, 0.0F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(-1.5F, 16.94F, -2.76F));
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(2, 2).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 15.69F, -2.76F));
      partdefinition1.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(10, 0).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(0.0F, -2.0F, -1.0F));
      partdefinition1.addOrReplaceChild("beak1", CubeListBuilder.create().texOffs(11, 7).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -0.5F, -1.5F));
      partdefinition1.addOrReplaceChild("beak2", CubeListBuilder.create().texOffs(16, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -1.75F, -2.45F));
      partdefinition1.addOrReplaceChild("feather", CubeListBuilder.create().texOffs(2, 18).addBox(0.0F, -4.0F, -2.0F, 0.0F, 5.0F, 4.0F), PartPose.offset(0.0F, -2.15F, 0.15F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(14, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F);
      partdefinition.addOrReplaceChild("left_leg", cubelistbuilder, PartPose.offset(1.0F, 22.0F, -1.05F));
      partdefinition.addOrReplaceChild("right_leg", cubelistbuilder, PartPose.offset(-1.0F, 22.0F, -1.05F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Parrot parrot, float f, float f1, float f2, float f3, float f4) {
      this.setupAnim(getState(parrot), parrot.tickCount, f, f1, f2, f3, f4);
   }

   public void prepareMobModel(Parrot parrot, float f, float f1, float f2) {
      this.prepare(getState(parrot));
   }

   public void renderOnShoulder(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3, int k) {
      this.prepare(ParrotModel.State.ON_SHOULDER);
      this.setupAnim(ParrotModel.State.ON_SHOULDER, k, f, f1, 0.0F, f2, f3);
      this.root.render(posestack, vertexconsumer, i, j);
   }

   private void setupAnim(ParrotModel.State parrotmodel_state, int i, float f, float f1, float f2, float f3, float f4) {
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.head.zRot = 0.0F;
      this.head.x = 0.0F;
      this.body.x = 0.0F;
      this.tail.x = 0.0F;
      this.rightWing.x = -1.5F;
      this.leftWing.x = 1.5F;
      switch (parrotmodel_state) {
         case SITTING:
            break;
         case PARTY:
            float f5 = Mth.cos((float)i);
            float f6 = Mth.sin((float)i);
            this.head.x = f5;
            this.head.y = 15.69F + f6;
            this.head.xRot = 0.0F;
            this.head.yRot = 0.0F;
            this.head.zRot = Mth.sin((float)i) * 0.4F;
            this.body.x = f5;
            this.body.y = 16.5F + f6;
            this.leftWing.zRot = -0.0873F - f2;
            this.leftWing.x = 1.5F + f5;
            this.leftWing.y = 16.94F + f6;
            this.rightWing.zRot = 0.0873F + f2;
            this.rightWing.x = -1.5F + f5;
            this.rightWing.y = 16.94F + f6;
            this.tail.x = f5;
            this.tail.y = 21.07F + f6;
            break;
         case STANDING:
            this.leftLeg.xRot += Mth.cos(f * 0.6662F) * 1.4F * f1;
            this.rightLeg.xRot += Mth.cos(f * 0.6662F + (float)Math.PI) * 1.4F * f1;
         case FLYING:
         case ON_SHOULDER:
         default:
            float f7 = f2 * 0.3F;
            this.head.y = 15.69F + f7;
            this.tail.xRot = 1.015F + Mth.cos(f * 0.6662F) * 0.3F * f1;
            this.tail.y = 21.07F + f7;
            this.body.y = 16.5F + f7;
            this.leftWing.zRot = -0.0873F - f2;
            this.leftWing.y = 16.94F + f7;
            this.rightWing.zRot = 0.0873F + f2;
            this.rightWing.y = 16.94F + f7;
            this.leftLeg.y = 22.0F + f7;
            this.rightLeg.y = 22.0F + f7;
      }

   }

   private void prepare(ParrotModel.State parrotmodel_state) {
      this.feather.xRot = -0.2214F;
      this.body.xRot = 0.4937F;
      this.leftWing.xRot = -0.6981F;
      this.leftWing.yRot = -(float)Math.PI;
      this.rightWing.xRot = -0.6981F;
      this.rightWing.yRot = -(float)Math.PI;
      this.leftLeg.xRot = -0.0299F;
      this.rightLeg.xRot = -0.0299F;
      this.leftLeg.y = 22.0F;
      this.rightLeg.y = 22.0F;
      this.leftLeg.zRot = 0.0F;
      this.rightLeg.zRot = 0.0F;
      switch (parrotmodel_state) {
         case SITTING:
            float f = 1.9F;
            this.head.y = 17.59F;
            this.tail.xRot = 1.5388988F;
            this.tail.y = 22.97F;
            this.body.y = 18.4F;
            this.leftWing.zRot = -0.0873F;
            this.leftWing.y = 18.84F;
            this.rightWing.zRot = 0.0873F;
            this.rightWing.y = 18.84F;
            ++this.leftLeg.y;
            ++this.rightLeg.y;
            ++this.leftLeg.xRot;
            ++this.rightLeg.xRot;
            break;
         case PARTY:
            this.leftLeg.zRot = -0.34906584F;
            this.rightLeg.zRot = 0.34906584F;
         case STANDING:
         case ON_SHOULDER:
         default:
            break;
         case FLYING:
            this.leftLeg.xRot += 0.6981317F;
            this.rightLeg.xRot += 0.6981317F;
      }

   }

   private static ParrotModel.State getState(Parrot parrot) {
      if (parrot.isPartyParrot()) {
         return ParrotModel.State.PARTY;
      } else if (parrot.isInSittingPose()) {
         return ParrotModel.State.SITTING;
      } else {
         return parrot.isFlying() ? ParrotModel.State.FLYING : ParrotModel.State.STANDING;
      }
   }

   public static enum State {
      FLYING,
      STANDING,
      SITTING,
      PARTY,
      ON_SHOULDER;
   }
}
