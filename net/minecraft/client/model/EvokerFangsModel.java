package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class EvokerFangsModel<T extends Entity> extends HierarchicalModel<T> {
   private static final String BASE = "base";
   private static final String UPPER_JAW = "upper_jaw";
   private static final String LOWER_JAW = "lower_jaw";
   private final ModelPart root;
   private final ModelPart base;
   private final ModelPart upperJaw;
   private final ModelPart lowerJaw;

   public EvokerFangsModel(ModelPart modelpart) {
      this.root = modelpart;
      this.base = modelpart.getChild("base");
      this.upperJaw = modelpart.getChild("upper_jaw");
      this.lowerJaw = modelpart.getChild("lower_jaw");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 10.0F, 12.0F, 10.0F), PartPose.offset(-5.0F, 24.0F, -5.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(40, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 14.0F, 8.0F);
      partdefinition.addOrReplaceChild("upper_jaw", cubelistbuilder, PartPose.offset(1.5F, 24.0F, -4.0F));
      partdefinition.addOrReplaceChild("lower_jaw", cubelistbuilder, PartPose.offsetAndRotation(-1.5F, 24.0F, 4.0F, 0.0F, (float)Math.PI, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(T entity, float f, float f1, float f2, float f3, float f4) {
      float f5 = f * 2.0F;
      if (f5 > 1.0F) {
         f5 = 1.0F;
      }

      f5 = 1.0F - f5 * f5 * f5;
      this.upperJaw.zRot = (float)Math.PI - f5 * 0.35F * (float)Math.PI;
      this.lowerJaw.zRot = (float)Math.PI + f5 * 0.35F * (float)Math.PI;
      float f6 = (f + Mth.sin(f * 2.7F)) * 0.6F * 12.0F;
      this.upperJaw.y = 24.0F - f6;
      this.lowerJaw.y = this.upperJaw.y;
      this.base.y = this.upperJaw.y;
   }

   public ModelPart root() {
      return this.root;
   }
}
