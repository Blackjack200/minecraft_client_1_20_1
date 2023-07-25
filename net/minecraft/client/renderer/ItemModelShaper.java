package net.minecraft.client.renderer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemModelShaper {
   public final Int2ObjectMap<ModelResourceLocation> shapes = new Int2ObjectOpenHashMap<>(256);
   private final Int2ObjectMap<BakedModel> shapesCache = new Int2ObjectOpenHashMap<>(256);
   private final ModelManager modelManager;

   public ItemModelShaper(ModelManager modelmanager) {
      this.modelManager = modelmanager;
   }

   public BakedModel getItemModel(ItemStack itemstack) {
      BakedModel bakedmodel = this.getItemModel(itemstack.getItem());
      return bakedmodel == null ? this.modelManager.getMissingModel() : bakedmodel;
   }

   @Nullable
   public BakedModel getItemModel(Item item) {
      return this.shapesCache.get(getIndex(item));
   }

   private static int getIndex(Item item) {
      return Item.getId(item);
   }

   public void register(Item item, ModelResourceLocation modelresourcelocation) {
      this.shapes.put(getIndex(item), modelresourcelocation);
   }

   public ModelManager getModelManager() {
      return this.modelManager;
   }

   public void rebuildCache() {
      this.shapesCache.clear();

      for(Map.Entry<Integer, ModelResourceLocation> map_entry : this.shapes.entrySet()) {
         this.shapesCache.put(map_entry.getKey(), this.modelManager.getModel(map_entry.getValue()));
      }

   }
}
