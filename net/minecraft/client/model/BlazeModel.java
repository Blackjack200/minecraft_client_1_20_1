package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class BlazeModel<T extends Entity> extends HierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart[] upperBodyParts;
   private final ModelPart head;

   public BlazeModel(ModelPart modelpart) {
      this.root = modelpart;
      this.head = modelpart.getChild("head");
      this.upperBodyParts = new ModelPart[12];
      Arrays.setAll(this.upperBodyParts, (i) -> modelpart.getChild(getPartName(i)));
   }

   private static String getPartName(int i) {
      return "part" + i;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      float f = 0.0F;
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 8.0F, 2.0F);

      for(int i = 0; i < 4; ++i) {
         float f1 = Mth.cos(f) * 9.0F;
         float f2 = -2.0F + Mth.cos((float)(i * 2) * 0.25F);
         float f3 = Mth.sin(f) * 9.0F;
         partdefinition.addOrReplaceChild(getPartName(i), cubelistbuilder, PartPose.offset(f1, f2, f3));
         ++f;
      }

      f = ((float)Math.PI / 4F);

      for(int j = 4; j < 8; ++j) {
         float f4 = Mth.cos(f) * 7.0F;
         float f5 = 2.0F + Mth.cos((float)(j * 2) * 0.25F);
         float f6 = Mth.sin(f) * 7.0F;
         partdefinition.addOrReplaceChild(getPartName(j), cubelistbuilder, PartPose.offset(f4, f5, f6));
         ++f;
      }

      f = 0.47123894F;

      for(int k = 8; k < 12; ++k) {
         float f7 = Mth.cos(f) * 5.0F;
         float f8 = 11.0F + Mth.cos((float)k * 1.5F * 0.5F);
         float f9 = Mth.sin(f) * 5.0F;
         partdefinition.addOrReplaceChild(getPartName(k), cubelistbuilder, PartPose.offset(f7, f8, f9));
         ++f;
      }

      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   public void setupAnim(T entity, float f, float f1, float f2, float f3, float f4) {
      float f5 = f2 * (float)Math.PI * -0.1F;

      for(int i = 0; i < 4; ++i) {
         this.upperBodyParts[i].y = -2.0F + Mth.cos(((float)(i * 2) + f2) * 0.25F);
         this.upperBodyParts[i].x = Mth.cos(f5) * 9.0F;
         this.upperBodyParts[i].z = Mth.sin(f5) * 9.0F;
         ++f5;
      }

      f5 = ((float)Math.PI / 4F) + f2 * (float)Math.PI * 0.03F;

      for(int j = 4; j < 8; ++j) {
         this.upperBodyParts[j].y = 2.0F + Mth.cos(((float)(j * 2) + f2) * 0.25F);
         this.upperBodyParts[j].x = Mth.cos(f5) * 7.0F;
         this.upperBodyParts[j].z = Mth.sin(f5) * 7.0F;
         ++f5;
      }

      f5 = 0.47123894F + f2 * (float)Math.PI * -0.05F;

      for(int k = 8; k < 12; ++k) {
         this.upperBodyParts[k].y = 11.0F + Mth.cos(((float)k * 1.5F + f2) * 0.5F);
         this.upperBodyParts[k].x = Mth.cos(f5) * 5.0F;
         this.upperBodyParts[k].z = Mth.sin(f5) * 5.0F;
         ++f5;
      }

      this.head.yRot = f3 * ((float)Math.PI / 180F);
      this.head.xRot = f4 * ((float)Math.PI / 180F);
   }
}
