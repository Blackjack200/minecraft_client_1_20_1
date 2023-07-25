package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class BookModel extends Model {
   private static final String LEFT_PAGES = "left_pages";
   private static final String RIGHT_PAGES = "right_pages";
   private static final String FLIP_PAGE_1 = "flip_page1";
   private static final String FLIP_PAGE_2 = "flip_page2";
   private final ModelPart root;
   private final ModelPart leftLid;
   private final ModelPart rightLid;
   private final ModelPart leftPages;
   private final ModelPart rightPages;
   private final ModelPart flipPage1;
   private final ModelPart flipPage2;

   public BookModel(ModelPart modelpart) {
      super(RenderType::entitySolid);
      this.root = modelpart;
      this.leftLid = modelpart.getChild("left_lid");
      this.rightLid = modelpart.getChild("right_lid");
      this.leftPages = modelpart.getChild("left_pages");
      this.rightPages = modelpart.getChild("right_pages");
      this.flipPage1 = modelpart.getChild("flip_page1");
      this.flipPage2 = modelpart.getChild("flip_page2");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, -1.0F));
      partdefinition.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, 1.0F));
      partdefinition.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F), PartPose.rotation(0.0F, ((float)Math.PI / 2F), 0.0F));
      partdefinition.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
      partdefinition.addOrReplaceChild("flip_page1", cubelistbuilder, PartPose.ZERO);
      partdefinition.addOrReplaceChild("flip_page2", cubelistbuilder, PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void renderToBuffer(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      this.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
   }

   public void render(PoseStack posestack, VertexConsumer vertexconsumer, int i, int j, float f, float f1, float f2, float f3) {
      this.root.render(posestack, vertexconsumer, i, j, f, f1, f2, f3);
   }

   public void setupAnim(float f, float f1, float f2, float f3) {
      float f4 = (Mth.sin(f * 0.02F) * 0.1F + 1.25F) * f3;
      this.leftLid.yRot = (float)Math.PI + f4;
      this.rightLid.yRot = -f4;
      this.leftPages.yRot = f4;
      this.rightPages.yRot = -f4;
      this.flipPage1.yRot = f4 - f4 * 2.0F * f1;
      this.flipPage2.yRot = f4 - f4 * 2.0F * f2;
      this.leftPages.x = Mth.sin(f4);
      this.rightPages.x = Mth.sin(f4);
      this.flipPage1.x = Mth.sin(f4);
      this.flipPage2.x = Mth.sin(f4);
   }
}
