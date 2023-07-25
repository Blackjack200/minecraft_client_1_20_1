package net.minecraft.client.resources.metadata.animation;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nullable;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.Validate;

public class AnimationMetadataSectionSerializer implements MetadataSectionSerializer<AnimationMetadataSection> {
   public AnimationMetadataSection fromJson(JsonObject jsonobject) {
      ImmutableList.Builder<AnimationFrame> immutablelist_builder = ImmutableList.builder();
      int i = GsonHelper.getAsInt(jsonobject, "frametime", 1);
      if (i != 1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid default frame time");
      }

      if (jsonobject.has("frames")) {
         try {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "frames");

            for(int j = 0; j < jsonarray.size(); ++j) {
               JsonElement jsonelement = jsonarray.get(j);
               AnimationFrame animationframe = this.getFrame(j, jsonelement);
               if (animationframe != null) {
                  immutablelist_builder.add(animationframe);
               }
            }
         } catch (ClassCastException var8) {
            throw new JsonParseException("Invalid animation->frames: expected array, was " + jsonobject.get("frames"), var8);
         }
      }

      int k = GsonHelper.getAsInt(jsonobject, "width", -1);
      int l = GsonHelper.getAsInt(jsonobject, "height", -1);
      if (k != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)k, "Invalid width");
      }

      if (l != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)l, "Invalid height");
      }

      boolean flag = GsonHelper.getAsBoolean(jsonobject, "interpolate", false);
      return new AnimationMetadataSection(immutablelist_builder.build(), k, l, i, flag);
   }

   @Nullable
   private AnimationFrame getFrame(int i, JsonElement jsonelement) {
      if (jsonelement.isJsonPrimitive()) {
         return new AnimationFrame(GsonHelper.convertToInt(jsonelement, "frames[" + i + "]"));
      } else if (jsonelement.isJsonObject()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "frames[" + i + "]");
         int j = GsonHelper.getAsInt(jsonobject, "time", -1);
         if (jsonobject.has("time")) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)j, "Invalid frame time");
         }

         int k = GsonHelper.getAsInt(jsonobject, "index");
         Validate.inclusiveBetween(0L, 2147483647L, (long)k, "Invalid frame index");
         return new AnimationFrame(k, j);
      } else {
         return null;
      }
   }

   public String getMetadataSectionName() {
      return "animation";
   }
}
