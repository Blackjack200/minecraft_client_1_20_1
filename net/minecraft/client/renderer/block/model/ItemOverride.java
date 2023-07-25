package net.minecraft.client.renderer.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ItemOverride {
   private final ResourceLocation model;
   private final List<ItemOverride.Predicate> predicates;

   public ItemOverride(ResourceLocation resourcelocation, List<ItemOverride.Predicate> list) {
      this.model = resourcelocation;
      this.predicates = ImmutableList.copyOf(list);
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   public Stream<ItemOverride.Predicate> getPredicates() {
      return this.predicates.stream();
   }

   protected static class Deserializer implements JsonDeserializer<ItemOverride> {
      public ItemOverride deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "model"));
         List<ItemOverride.Predicate> list = this.getPredicates(jsonobject);
         return new ItemOverride(resourcelocation, list);
      }

      protected List<ItemOverride.Predicate> getPredicates(JsonObject jsonobject) {
         Map<ResourceLocation, Float> map = Maps.newLinkedHashMap();
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "predicate");

         for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
            map.put(new ResourceLocation(map_entry.getKey()), GsonHelper.convertToFloat(map_entry.getValue(), map_entry.getKey()));
         }

         return map.entrySet().stream().map((map_entry1) -> new ItemOverride.Predicate(map_entry1.getKey(), map_entry1.getValue())).collect(ImmutableList.toImmutableList());
      }
   }

   public static class Predicate {
      private final ResourceLocation property;
      private final float value;

      public Predicate(ResourceLocation resourcelocation, float f) {
         this.property = resourcelocation;
         this.value = f;
      }

      public ResourceLocation getProperty() {
         return this.property;
      }

      public float getValue() {
         return this.value;
      }
   }
}
