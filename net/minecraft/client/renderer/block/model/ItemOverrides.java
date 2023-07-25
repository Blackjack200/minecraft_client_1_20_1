package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemOverrides {
   public static final ItemOverrides EMPTY = new ItemOverrides();
   public static final float NO_OVERRIDE = Float.NEGATIVE_INFINITY;
   private final ItemOverrides.BakedOverride[] overrides;
   private final ResourceLocation[] properties;

   private ItemOverrides() {
      this.overrides = new ItemOverrides.BakedOverride[0];
      this.properties = new ResourceLocation[0];
   }

   public ItemOverrides(ModelBaker modelbaker, BlockModel blockmodel, List<ItemOverride> list) {
      this.properties = list.stream().flatMap(ItemOverride::getPredicates).map(ItemOverride.Predicate::getProperty).distinct().toArray((i1) -> new ResourceLocation[i1]);
      Object2IntMap<ResourceLocation> object2intmap = new Object2IntOpenHashMap<>();

      for(int i = 0; i < this.properties.length; ++i) {
         object2intmap.put(this.properties[i], i);
      }

      List<ItemOverrides.BakedOverride> list1 = Lists.newArrayList();

      for(int j = list.size() - 1; j >= 0; --j) {
         ItemOverride itemoverride = list.get(j);
         BakedModel bakedmodel = this.bakeModel(modelbaker, blockmodel, itemoverride);
         ItemOverrides.PropertyMatcher[] aitemoverrides_propertymatcher = itemoverride.getPredicates().map((itemoverride_predicate) -> {
            int l = object2intmap.getInt(itemoverride_predicate.getProperty());
            return new ItemOverrides.PropertyMatcher(l, itemoverride_predicate.getValue());
         }).toArray((k) -> new ItemOverrides.PropertyMatcher[k]);
         list1.add(new ItemOverrides.BakedOverride(aitemoverrides_propertymatcher, bakedmodel));
      }

      this.overrides = list1.toArray(new ItemOverrides.BakedOverride[0]);
   }

   @Nullable
   private BakedModel bakeModel(ModelBaker modelbaker, BlockModel blockmodel, ItemOverride itemoverride) {
      UnbakedModel unbakedmodel = modelbaker.getModel(itemoverride.getModel());
      return Objects.equals(unbakedmodel, blockmodel) ? null : modelbaker.bake(itemoverride.getModel(), BlockModelRotation.X0_Y0);
   }

   @Nullable
   public BakedModel resolve(BakedModel bakedmodel, ItemStack itemstack, @Nullable ClientLevel clientlevel, @Nullable LivingEntity livingentity, int i) {
      if (this.overrides.length != 0) {
         Item item = itemstack.getItem();
         int j = this.properties.length;
         float[] afloat = new float[j];

         for(int k = 0; k < j; ++k) {
            ResourceLocation resourcelocation = this.properties[k];
            ItemPropertyFunction itempropertyfunction = ItemProperties.getProperty(item, resourcelocation);
            if (itempropertyfunction != null) {
               afloat[k] = itempropertyfunction.call(itemstack, clientlevel, livingentity, i);
            } else {
               afloat[k] = Float.NEGATIVE_INFINITY;
            }
         }

         for(ItemOverrides.BakedOverride itemoverrides_bakedoverride : this.overrides) {
            if (itemoverrides_bakedoverride.test(afloat)) {
               BakedModel bakedmodel1 = itemoverrides_bakedoverride.model;
               if (bakedmodel1 == null) {
                  return bakedmodel;
               }

               return bakedmodel1;
            }
         }
      }

      return bakedmodel;
   }

   static class BakedOverride {
      private final ItemOverrides.PropertyMatcher[] matchers;
      @Nullable
      final BakedModel model;

      BakedOverride(ItemOverrides.PropertyMatcher[] aitemoverrides_propertymatcher, @Nullable BakedModel bakedmodel) {
         this.matchers = aitemoverrides_propertymatcher;
         this.model = bakedmodel;
      }

      boolean test(float[] afloat) {
         for(ItemOverrides.PropertyMatcher itemoverrides_propertymatcher : this.matchers) {
            float f = afloat[itemoverrides_propertymatcher.index];
            if (f < itemoverrides_propertymatcher.value) {
               return false;
            }
         }

         return true;
      }
   }

   static class PropertyMatcher {
      public final int index;
      public final float value;

      PropertyMatcher(int i, float f) {
         this.index = i;
         this.value = f;
      }
   }
}
