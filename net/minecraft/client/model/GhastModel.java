package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public class GhastModel<T extends Entity> extends HierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart[] tentacles = new ModelPart[9];

   public GhastModel(ModelPart modelpart) {
      this.root = modelpart;

      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i] = modelpart.getChild(createTentacleName(i));
      }

   }

   private static String createTentacleName(int i) {
      return "tentacle" + i;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F), PartPose.offset(0.0F, 17.6F, 0.0F));
      RandomSource randomsource = RandomSource.create(1660L);

      for(int i = 0; i < 9; ++i) {
         float f = (((float)(i % 3) - (float)(i / 3 % 2) * 0.5F + 0.25F) / 2.0F * 2.0F - 1.0F) * 5.0F;
         float f1 = ((float)(i / 3) / 2.0F * 2.0F - 1.0F) * 5.0F;
         int j = randomsource.nextInt(7) + 8;
         partdefinition.addOrReplaceChild(createTentacleName(i), CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, (float)j, 2.0F), PartPose.offset(f, 24.6F, f1));
      }

      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(T entity, float f, float f1, float f2, float f3, float f4) {
      for(int i = 0; i < this.tentacles.length; ++i) {
         this.tentacles[i].xRot = 0.2F * Mth.sin(f2 * 0.3F + (float)i) + 0.4F;
      }

   }

   public ModelPart root() {
      return this.root;
   }
}
