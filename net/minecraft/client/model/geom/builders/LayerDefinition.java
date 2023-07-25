package net.minecraft.client.model.geom.builders;

import net.minecraft.client.model.geom.ModelPart;

public class LayerDefinition {
   private final MeshDefinition mesh;
   private final MaterialDefinition material;

   private LayerDefinition(MeshDefinition meshdefinition, MaterialDefinition materialdefinition) {
      this.mesh = meshdefinition;
      this.material = materialdefinition;
   }

   public ModelPart bakeRoot() {
      return this.mesh.getRoot().bake(this.material.xTexSize, this.material.yTexSize);
   }

   public static LayerDefinition create(MeshDefinition meshdefinition, int i, int j) {
      return new LayerDefinition(meshdefinition, new MaterialDefinition(i, j));
   }
}
