package net.minecraft.client.model.geom;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class EntityModelSet implements ResourceManagerReloadListener {
   private Map<ModelLayerLocation, LayerDefinition> roots = ImmutableMap.of();

   public ModelPart bakeLayer(ModelLayerLocation modellayerlocation) {
      LayerDefinition layerdefinition = this.roots.get(modellayerlocation);
      if (layerdefinition == null) {
         throw new IllegalArgumentException("No model for layer " + modellayerlocation);
      } else {
         return layerdefinition.bakeRoot();
      }
   }

   public void onResourceManagerReload(ResourceManager resourcemanager) {
      this.roots = ImmutableMap.copyOf(LayerDefinitions.createRoots());
   }
}
