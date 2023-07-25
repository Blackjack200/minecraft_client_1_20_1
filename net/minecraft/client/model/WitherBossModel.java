package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

public class WitherBossModel<T extends WitherBoss> extends HierarchicalModel<T> {
   private static final String RIBCAGE = "ribcage";
   private static final String CENTER_HEAD = "center_head";
   private static final String RIGHT_HEAD = "right_head";
   private static final String LEFT_HEAD = "left_head";
   private static final float RIBCAGE_X_ROT_OFFSET = 0.065F;
   private static final float TAIL_X_ROT_OFFSET = 0.265F;
   private final ModelPart root;
   private final ModelPart centerHead;
   private final ModelPart rightHead;
   private final ModelPart leftHead;
   private final ModelPart ribcage;
   private final ModelPart tail;

   public WitherBossModel(ModelPart modelpart) {
      this.root = modelpart;
      this.ribcage = modelpart.getChild("ribcage");
      this.tail = modelpart.getChild("tail");
      this.centerHead = modelpart.getChild("center_head");
      this.rightHead = modelpart.getChild("right_head");
      this.leftHead = modelpart.getChild("left_head");
   }

   public static LayerDefinition createBodyLayer(CubeDeformation cubedeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(0, 16).addBox(-10.0F, 3.9F, -0.5F, 20.0F, 3.0F, 3.0F, cubedeformation), PartPose.ZERO);
      float f = 0.20420352F;
      partdefinition.addOrReplaceChild("ribcage", CubeListBuilder.create().texOffs(0, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 3.0F, cubedeformation).texOffs(24, 22).addBox(-4.0F, 1.5F, 0.5F, 11.0F, 2.0F, 2.0F, cubedeformation).texOffs(24, 22).addBox(-4.0F, 4.0F, 0.5F, 11.0F, 2.0F, 2.0F, cubedeformation).texOffs(24, 22).addBox(-4.0F, 6.5F, 0.5F, 11.0F, 2.0F, 2.0F, cubedeformation), PartPose.offsetAndRotation(-2.0F, 6.9F, -0.5F, 0.20420352F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 22).addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, cubedeformation), PartPose.offsetAndRotation(-2.0F, 6.9F + Mth.cos(0.20420352F) * 10.0F, -0.5F + Mth.sin(0.20420352F) * 10.0F, 0.83252203F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("center_head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubedeformation), PartPose.ZERO);
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, cubedeformation);
      partdefinition.addOrReplaceChild("right_head", cubelistbuilder, PartPose.offset(-8.0F, 4.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_head", cubelistbuilder, PartPose.offset(10.0F, 4.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(T witherboss, float f, float f1, float f2, float f3, float f4) {
      float f5 = Mth.cos(f2 * 0.1F);
      this.ribcage.xRot = (0.065F + 0.05F * f5) * (float)Math.PI;
      this.tail.setPos(-2.0F, 6.9F + Mth.cos(this.ribcage.xRot) * 10.0F, -0.5F + Mth.sin(this.ribcage.xRot) * 10.0F);
      this.tail.xRot = (0.265F + 0.1F * f5) * (float)Math.PI;
      this.centerHead.yRot = f3 * ((float)Math.PI / 180F);
      this.centerHead.xRot = f4 * ((float)Math.PI / 180F);
   }

   public void prepareMobModel(T witherboss, float f, float f1, float f2) {
      setupHeadRotation(witherboss, this.rightHead, 0);
      setupHeadRotation(witherboss, this.leftHead, 1);
   }

   private static <T extends WitherBoss> void setupHeadRotation(T witherboss, ModelPart modelpart, int i) {
      modelpart.yRot = (witherboss.getHeadYRot(i) - witherboss.yBodyRot) * ((float)Math.PI / 180F);
      modelpart.xRot = witherboss.getHeadXRot(i) * ((float)Math.PI / 180F);
   }
}
