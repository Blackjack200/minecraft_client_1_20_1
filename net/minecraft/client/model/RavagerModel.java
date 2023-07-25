package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Ravager;

public class RavagerModel extends HierarchicalModel<Ravager> {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart mouth;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart neck;

   public RavagerModel(ModelPart modelpart) {
      this.root = modelpart;
      this.neck = modelpart.getChild("neck");
      this.head = this.neck.getChild("head");
      this.mouth = this.head.getChild("mouth");
      this.rightHindLeg = modelpart.getChild("right_hind_leg");
      this.leftHindLeg = modelpart.getChild("left_hind_leg");
      this.rightFrontLeg = modelpart.getChild("right_front_leg");
      this.leftFrontLeg = modelpart.getChild("left_front_leg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      int i = 16;
      PartDefinition partdefinition1 = partdefinition.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(68, 73).addBox(-5.0F, -1.0F, -18.0F, 10.0F, 10.0F, 18.0F), PartPose.offset(0.0F, -7.0F, 5.5F));
      PartDefinition partdefinition2 = partdefinition1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -20.0F, -14.0F, 16.0F, 20.0F, 16.0F).texOffs(0, 0).addBox(-2.0F, -6.0F, -18.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(0.0F, 16.0F, -17.0F));
      partdefinition2.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(74, 55).addBox(0.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F), PartPose.offsetAndRotation(-10.0F, -14.0F, -8.0F, 1.0995574F, 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(74, 55).mirror().addBox(0.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F), PartPose.offsetAndRotation(8.0F, -14.0F, -8.0F, 1.0995574F, 0.0F, 0.0F));
      partdefinition2.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 36).addBox(-8.0F, 0.0F, -16.0F, 16.0F, 3.0F, 16.0F), PartPose.offset(0.0F, -2.0F, 2.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 55).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 16.0F, 20.0F).texOffs(0, 91).addBox(-6.0F, 6.0F, -7.0F, 12.0F, 13.0F, 18.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F), PartPose.offset(-8.0F, -13.0F, 18.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(96, 0).mirror().addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F), PartPose.offset(8.0F, -13.0F, 18.0F));
      partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F), PartPose.offset(-8.0F, -13.0F, -5.0F));
      partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(64, 0).mirror().addBox(-4.0F, 0.0F, -4.0F, 8.0F, 37.0F, 8.0F), PartPose.offset(8.0F, -13.0F, -5.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(Ravager ravager, float f, float f1, float f2, float f3, float f4) {
      this.head.xRot = f4 * ((float)Math.PI / 180F);
      this.head.yRot = f3 * ((float)Math.PI / 180F);
      float f5 = 0.4F * f1;
      this.rightHindLeg.xRot = Mth.cos(f * 0.6662F) * f5;
      this.leftHindLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * f5;
      this.rightFrontLeg.xRot = Mth.cos(f * 0.6662F + (float)Math.PI) * f5;
      this.leftFrontLeg.xRot = Mth.cos(f * 0.6662F) * f5;
   }

   public void prepareMobModel(Ravager ravager, float f, float f1, float f2) {
      super.prepareMobModel(ravager, f, f1, f2);
      int i = ravager.getStunnedTick();
      int j = ravager.getRoarTick();
      int k = 20;
      int l = ravager.getAttackTick();
      int i1 = 10;
      if (l > 0) {
         float f3 = Mth.triangleWave((float)l - f2, 10.0F);
         float f4 = (1.0F + f3) * 0.5F;
         float f5 = f4 * f4 * f4 * 12.0F;
         float f6 = f5 * Mth.sin(this.neck.xRot);
         this.neck.z = -6.5F + f5;
         this.neck.y = -7.0F - f6;
         float f7 = Mth.sin(((float)l - f2) / 10.0F * (float)Math.PI * 0.25F);
         this.mouth.xRot = ((float)Math.PI / 2F) * f7;
         if (l > 5) {
            this.mouth.xRot = Mth.sin(((float)(-4 + l) - f2) / 4.0F) * (float)Math.PI * 0.4F;
         } else {
            this.mouth.xRot = 0.15707964F * Mth.sin((float)Math.PI * ((float)l - f2) / 10.0F);
         }
      } else {
         float f8 = -1.0F;
         float f9 = -1.0F * Mth.sin(this.neck.xRot);
         this.neck.x = 0.0F;
         this.neck.y = -7.0F - f9;
         this.neck.z = 5.5F;
         boolean flag = i > 0;
         this.neck.xRot = flag ? 0.21991149F : 0.0F;
         this.mouth.xRot = (float)Math.PI * (flag ? 0.05F : 0.01F);
         if (flag) {
            double d0 = (double)i / 40.0D;
            this.neck.x = (float)Math.sin(d0 * 10.0D) * 3.0F;
         } else if (j > 0) {
            float f10 = Mth.sin(((float)(20 - j) - f2) / 20.0F * (float)Math.PI * 0.25F);
            this.mouth.xRot = ((float)Math.PI / 2F) * f10;
         }
      }

   }
}
