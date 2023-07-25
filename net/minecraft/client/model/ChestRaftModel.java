package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ChestRaftModel extends RaftModel {
   private static final String CHEST_BOTTOM = "chest_bottom";
   private static final String CHEST_LID = "chest_lid";
   private static final String CHEST_LOCK = "chest_lock";

   public ChestRaftModel(ModelPart modelpart) {
      super(modelpart);
   }

   protected ImmutableList.Builder<ModelPart> createPartsBuilder(ModelPart modelpart) {
      ImmutableList.Builder<ModelPart> immutablelist_builder = super.createPartsBuilder(modelpart);
      immutablelist_builder.add(modelpart.getChild("chest_bottom"));
      immutablelist_builder.add(modelpart.getChild("chest_lid"));
      immutablelist_builder.add(modelpart.getChild("chest_lock"));
      return immutablelist_builder;
   }

   public static LayerDefinition createBodyModel() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      RaftModel.createChildren(partdefinition);
      partdefinition.addOrReplaceChild("chest_bottom", CubeListBuilder.create().texOffs(0, 76).addBox(0.0F, 0.0F, 0.0F, 12.0F, 8.0F, 12.0F), PartPose.offsetAndRotation(-2.0F, -10.0F, -6.0F, 0.0F, (-(float)Math.PI / 2F), 0.0F));
      partdefinition.addOrReplaceChild("chest_lid", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 12.0F, 4.0F, 12.0F), PartPose.offsetAndRotation(-2.0F, -14.0F, -6.0F, 0.0F, (-(float)Math.PI / 2F), 0.0F));
      partdefinition.addOrReplaceChild("chest_lock", CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-1.0F, -11.0F, -1.0F, 0.0F, (-(float)Math.PI / 2F), 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }
}
