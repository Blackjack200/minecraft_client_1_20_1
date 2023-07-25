package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class ElytraModel<T extends LivingEntity> extends AgeableListModel<T> {
   private final ModelPart rightWing;
   private final ModelPart leftWing;

   public ElytraModel(ModelPart modelpart) {
      this.leftWing = modelpart.getChild("left_wing");
      this.rightWing = modelpart.getChild("right_wing");
   }

   public static LayerDefinition createLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      CubeDeformation cubedeformation = new CubeDeformation(1.0F);
      partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(22, 0).addBox(-10.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, cubedeformation), PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, -0.2617994F));
      partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(22, 0).mirror().addBox(0.0F, 0.0F, 0.0F, 10.0F, 20.0F, 2.0F, cubedeformation), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.2617994F, 0.0F, 0.2617994F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of();
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.leftWing, this.rightWing);
   }

   public void setupAnim(T livingentity, float f, float f1, float f2, float f3, float f4) {
      float f5 = 0.2617994F;
      float f6 = -0.2617994F;
      float f7 = 0.0F;
      float f8 = 0.0F;
      if (livingentity.isFallFlying()) {
         float f9 = 1.0F;
         Vec3 vec3 = livingentity.getDeltaMovement();
         if (vec3.y < 0.0D) {
            Vec3 vec31 = vec3.normalize();
            f9 = 1.0F - (float)Math.pow(-vec31.y, 1.5D);
         }

         f5 = f9 * 0.34906584F + (1.0F - f9) * f5;
         f6 = f9 * (-(float)Math.PI / 2F) + (1.0F - f9) * f6;
      } else if (livingentity.isCrouching()) {
         f5 = 0.6981317F;
         f6 = (-(float)Math.PI / 4F);
         f7 = 3.0F;
         f8 = 0.08726646F;
      }

      this.leftWing.y = f7;
      if (livingentity instanceof AbstractClientPlayer abstractclientplayer) {
         abstractclientplayer.elytraRotX += (f5 - abstractclientplayer.elytraRotX) * 0.1F;
         abstractclientplayer.elytraRotY += (f8 - abstractclientplayer.elytraRotY) * 0.1F;
         abstractclientplayer.elytraRotZ += (f6 - abstractclientplayer.elytraRotZ) * 0.1F;
         this.leftWing.xRot = abstractclientplayer.elytraRotX;
         this.leftWing.yRot = abstractclientplayer.elytraRotY;
         this.leftWing.zRot = abstractclientplayer.elytraRotZ;
      } else {
         this.leftWing.xRot = f5;
         this.leftWing.zRot = f6;
         this.leftWing.yRot = f8;
      }

      this.rightWing.yRot = -this.leftWing.yRot;
      this.rightWing.y = this.leftWing.y;
      this.rightWing.xRot = this.leftWing.xRot;
      this.rightWing.zRot = -this.leftWing.zRot;
   }
}
