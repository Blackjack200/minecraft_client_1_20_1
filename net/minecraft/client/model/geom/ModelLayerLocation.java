package net.minecraft.client.model.geom;

import net.minecraft.resources.ResourceLocation;

public final class ModelLayerLocation {
   private final ResourceLocation model;
   private final String layer;

   public ModelLayerLocation(ResourceLocation resourcelocation, String s) {
      this.model = resourcelocation;
      this.layer = s;
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   public String getLayer() {
      return this.layer;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof ModelLayerLocation)) {
         return false;
      } else {
         ModelLayerLocation modellayerlocation = (ModelLayerLocation)object;
         return this.model.equals(modellayerlocation.model) && this.layer.equals(modellayerlocation.layer);
      }
   }

   public int hashCode() {
      int i = this.model.hashCode();
      return 31 * i + this.layer.hashCode();
   }

   public String toString() {
      return this.model + "#" + this.layer;
   }
}
