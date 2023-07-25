package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;

public class MultiVariant implements UnbakedModel {
   private final List<Variant> variants;

   public MultiVariant(List<Variant> list) {
      this.variants = list;
   }

   public List<Variant> getVariants() {
      return this.variants;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object instanceof MultiVariant) {
         MultiVariant multivariant = (MultiVariant)object;
         return this.variants.equals(multivariant.variants);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.variants.hashCode();
   }

   public Collection<ResourceLocation> getDependencies() {
      return this.getVariants().stream().map(Variant::getModelLocation).collect(Collectors.toSet());
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
      this.getVariants().stream().map(Variant::getModelLocation).distinct().forEach((resourcelocation) -> function.apply(resourcelocation).resolveParents(function));
   }

   @Nullable
   public BakedModel bake(ModelBaker modelbaker, Function<Material, TextureAtlasSprite> function, ModelState modelstate, ResourceLocation resourcelocation) {
      if (this.getVariants().isEmpty()) {
         return null;
      } else {
         WeightedBakedModel.Builder weightedbakedmodel_builder = new WeightedBakedModel.Builder();

         for(Variant variant : this.getVariants()) {
            BakedModel bakedmodel = modelbaker.bake(variant.getModelLocation(), variant);
            weightedbakedmodel_builder.add(bakedmodel, variant.getWeight());
         }

         return weightedbakedmodel_builder.build();
      }
   }

   public static class Deserializer implements JsonDeserializer<MultiVariant> {
      public MultiVariant deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         List<Variant> list = Lists.newArrayList();
         if (jsonelement.isJsonArray()) {
            JsonArray jsonarray = jsonelement.getAsJsonArray();
            if (jsonarray.size() == 0) {
               throw new JsonParseException("Empty variant array");
            }

            for(JsonElement jsonelement1 : jsonarray) {
               list.add(jsondeserializationcontext.deserialize(jsonelement1, Variant.class));
            }
         } else {
            list.add(jsondeserializationcontext.deserialize(jsonelement, Variant.class));
         }

         return new MultiVariant(list);
      }
   }
}
