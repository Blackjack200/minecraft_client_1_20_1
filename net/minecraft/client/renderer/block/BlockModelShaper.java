package net.minecraft.client.renderer.block;

import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockModelShaper {
   private Map<BlockState, BakedModel> modelByStateCache = Map.of();
   private final ModelManager modelManager;

   public BlockModelShaper(ModelManager modelmanager) {
      this.modelManager = modelmanager;
   }

   public TextureAtlasSprite getParticleIcon(BlockState blockstate) {
      return this.getBlockModel(blockstate).getParticleIcon();
   }

   public BakedModel getBlockModel(BlockState blockstate) {
      BakedModel bakedmodel = this.modelByStateCache.get(blockstate);
      if (bakedmodel == null) {
         bakedmodel = this.modelManager.getMissingModel();
      }

      return bakedmodel;
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void replaceCache(Map<BlockState, BakedModel> map) {
      this.modelByStateCache = map;
   }

   public static ModelResourceLocation stateToModelLocation(BlockState blockstate) {
      return stateToModelLocation(BuiltInRegistries.BLOCK.getKey(blockstate.getBlock()), blockstate);
   }

   public static ModelResourceLocation stateToModelLocation(ResourceLocation resourcelocation, BlockState blockstate) {
      return new ModelResourceLocation(resourcelocation, statePropertiesToString(blockstate.getValues()));
   }

   public static String statePropertiesToString(Map<Property<?>, Comparable<?>> map) {
      StringBuilder stringbuilder = new StringBuilder();

      for(Map.Entry<Property<?>, Comparable<?>> map_entry : map.entrySet()) {
         if (stringbuilder.length() != 0) {
            stringbuilder.append(',');
         }

         Property<?> property = map_entry.getKey();
         stringbuilder.append(property.getName());
         stringbuilder.append('=');
         stringbuilder.append(getValue(property, map_entry.getValue()));
      }

      return stringbuilder.toString();
   }

   private static <T extends Comparable<T>> String getValue(Property<T> property, Comparable<?> comparable) {
      return property.getName((T)comparable);
   }
}
