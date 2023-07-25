package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;

public class PiglinHeadModel extends SkullModelBase {
   private final ModelPart head;
   private final ModelPart leftEar;
   private final ModelPart rightEar;

   public PiglinHeadModel(ModelPart modelpart) {
      this.head = modelpart.getChild("head");
      this.leftEar = this.head.getChild("left_ear");
      this.rightEar = this.head.getChild("right_ear");
   }

   public static MeshDefinition createHeadModel() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PiglinModel.addHead(CubeDeformation.NONE, meshdefinition);
      return meshdefinition;
   }

   public void setupAnim(float f, float f1, float f2) {
      this.head.yRot = f1 * ((float)Math.PI / 180F);
      this.head.xRot = f2 * ((float)Math.PI / 180F);
      float f3 = 1.2F;
      this.leftEar.zRot = (float)(-(Math.cos((double)(f * (float)Math.PI * 0.2F * 1.2F)) + 2.5D)) * 0.2F;
      this.rightEar.zRot = (float)(Math.cos((double)(f * (float)Math.PI * 0.2F)) + 2.5D) * 0.2F;
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      this.head.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
   }
}
