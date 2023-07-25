package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class BlockModelDefinition {
   private final Map<String, MultiVariant> variants = Maps.newLinkedHashMap();
   private MultiPart multiPart;

   public static BlockModelDefinition fromStream(BlockModelDefinition.Context blockmodeldefinition_context, Reader reader) {
      return GsonHelper.fromJson(blockmodeldefinition_context.gson, reader, BlockModelDefinition.class);
   }

   public static BlockModelDefinition fromJsonElement(BlockModelDefinition.Context blockmodeldefinition_context, JsonElement jsonelement) {
      return blockmodeldefinition_context.gson.fromJson(jsonelement, BlockModelDefinition.class);
   }

   public BlockModelDefinition(Map<String, MultiVariant> map, MultiPart multipart) {
      this.multiPart = multipart;
      this.variants.putAll(map);
   }

   public BlockModelDefinition(List<BlockModelDefinition> list) {
      BlockModelDefinition blockmodeldefinition = null;

      for(BlockModelDefinition blockmodeldefinition1 : list) {
         if (blockmodeldefinition1.isMultiPart()) {
            this.variants.clear();
            blockmodeldefinition = blockmodeldefinition1;
         }

         this.variants.putAll(blockmodeldefinition1.variants);
      }

      if (blockmodeldefinition != null) {
         this.multiPart = blockmodeldefinition.multiPart;
      }

   }

   @VisibleForTesting
   public boolean hasVariant(String s) {
      return this.variants.get(s) != null;
   }

   @VisibleForTesting
   public MultiVariant getVariant(String s) {
      MultiVariant multivariant = this.variants.get(s);
      if (multivariant == null) {
         throw new BlockModelDefinition.MissingVariantException();
      } else {
         return multivariant;
      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else {
         if (object instanceof BlockModelDefinition) {
            BlockModelDefinition blockmodeldefinition = (BlockModelDefinition)object;
            if (this.variants.equals(blockmodeldefinition.variants)) {
               return this.isMultiPart() ? this.multiPart.equals(blockmodeldefinition.multiPart) : !blockmodeldefinition.isMultiPart();
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return 31 * this.variants.hashCode() + (this.isMultiPart() ? this.multiPart.hashCode() : 0);
   }

   public Map<String, MultiVariant> getVariants() {
      return this.variants;
   }

   @VisibleForTesting
   public Set<MultiVariant> getMultiVariants() {
      Set<MultiVariant> set = Sets.newHashSet(this.variants.values());
      if (this.isMultiPart()) {
         set.addAll(this.multiPart.getMultiVariants());
      }

      return set;
   }

   public boolean isMultiPart() {
      return this.multiPart != null;
   }

   public MultiPart getMultiPart() {
      return this.multiPart;
   }

   public static final class Context {
      protected final Gson gson = (new GsonBuilder()).registerTypeAdapter(BlockModelDefinition.class, new BlockModelDefinition.Deserializer()).registerTypeAdapter(Variant.class, new Variant.Deserializer()).registerTypeAdapter(MultiVariant.class, new MultiVariant.Deserializer()).registerTypeAdapter(MultiPart.class, new MultiPart.Deserializer(this)).registerTypeAdapter(Selector.class, new Selector.Deserializer()).create();
      private StateDefinition<Block, BlockState> definition;

      public StateDefinition<Block, BlockState> getDefinition() {
         return this.definition;
      }

      public void setDefinition(StateDefinition<Block, BlockState> statedefinition) {
         this.definition = statedefinition;
      }
   }

   public static class Deserializer implements JsonDeserializer<BlockModelDefinition> {
      public BlockModelDefinition deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         Map<String, MultiVariant> map = this.getVariants(jsondeserializationcontext, jsonobject);
         MultiPart multipart = this.getMultiPart(jsondeserializationcontext, jsonobject);
         if (!map.isEmpty() || multipart != null && !multipart.getMultiVariants().isEmpty()) {
            return new BlockModelDefinition(map, multipart);
         } else {
            throw new JsonParseException("Neither 'variants' nor 'multipart' found");
         }
      }

      protected Map<String, MultiVariant> getVariants(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         Map<String, MultiVariant> map = Maps.newHashMap();
         if (jsonobject.has("variants")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "variants");

            for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
               map.put(map_entry.getKey(), jsondeserializationcontext.deserialize(map_entry.getValue(), MultiVariant.class));
            }
         }

         return map;
      }

      @Nullable
      protected MultiPart getMultiPart(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         if (!jsonobject.has("multipart")) {
            return null;
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "multipart");
            return jsondeserializationcontext.deserialize(jsonarray, MultiPart.class);
         }
      }
   }

   protected class MissingVariantException extends RuntimeException {
   }
}
