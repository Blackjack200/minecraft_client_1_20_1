package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SkullModel extends SkullModelBase {
   private final ModelPart root;
   protected final ModelPart head;

   public SkullModel(ModelPart modelpart) {
      this.root = modelpart;
      this.head = modelpart.getChild("head");
   }

   public static MeshDefinition createHeadModel() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      return meshdefinition;
   }

   public static LayerDefinition createHumanoidHeadLayer() {
      MeshDefinition meshdefinition = createHeadModel();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.getChild("head").addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public static LayerDefinition createMobHeadLayer() {
      MeshDefinition meshdefinition = createHeadModel();
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(float f, float f1, float f2) {
      this.head.yRot = f1 * ((float)Math.PI / 180F);
      this.head.xRot = f2 * ((float)Math.PI / 180F);
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      this.root.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
   }
}
