package net.minecraft.client.model.geom.builders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public class PartDefinition {
   private final List<CubeDefinition> cubes;
   private final PartPose partPose;
   private final Map<String, PartDefinition> children = Maps.newHashMap();

   PartDefinition(List<CubeDefinition> list, PartPose partpose) {
      this.cubes = list;
      this.partPose = partpose;
   }

   public PartDefinition addOrReplaceChild(String s, CubeListBuilder cubelistbuilder, PartPose partpose) {
      PartDefinition partdefinition = new PartDefinition(cubelistbuilder.getCubes(), partpose);
      PartDefinition partdefinition1 = this.children.put(s, partdefinition);
      if (partdefinition1 != null) {
         partdefinition.children.putAll(partdefinition1.children);
      }

      return partdefinition;
   }

   public ModelPart bake(int i, int j) {
      Object2ObjectArrayMap<String, ModelPart> object2objectarraymap = this.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (map_entry) -> map_entry.getValue().bake(i, j), (modelpart1, modelpart2) -> modelpart1, Object2ObjectArrayMap::new));
      List<ModelPart.Cube> list = this.cubes.stream().map((cubedefinition) -> cubedefinition.bake(i, j)).collect(ImmutableList.toImmutableList());
      ModelPart modelpart = new ModelPart(list, object2objectarraymap);
      modelpart.setInitialPose(this.partPose);
      modelpart.loadPose(this.partPose);
      return modelpart;
   }

   public PartDefinition getChild(String s) {
      return this.children.get(s);
   }
}
