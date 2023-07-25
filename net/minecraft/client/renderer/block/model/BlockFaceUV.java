package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;

public class BlockFaceUV {
   public float[] uvs;
   public final int rotation;

   public BlockFaceUV(@Nullable float[] afloat, int i) {
      this.uvs = afloat;
      this.rotation = i;
   }

   public float getU(int i) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int j = this.getShiftedIndex(i);
         return this.uvs[j != 0 && j != 1 ? 2 : 0];
      }
   }

   public float getV(int i) {
      if (this.uvs == null) {
         throw new NullPointerException("uvs");
      } else {
         int j = this.getShiftedIndex(i);
         return this.uvs[j != 0 && j != 3 ? 3 : 1];
      }
   }

   private int getShiftedIndex(int i) {
      return (i + this.rotation / 90) % 4;
   }

   public int getReverseIndex(int i) {
      return (i + 4 - this.rotation / 90) % 4;
   }

   public void setMissingUv(float[] afloat) {
      if (this.uvs == null) {
         this.uvs = afloat;
      }

   }

   protected static class Deserializer implements JsonDeserializer<BlockFaceUV> {
      private static final int DEFAULT_ROTATION = 0;

      public BlockFaceUV deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         float[] afloat = this.getUVs(jsonobject);
         int i = this.getRotation(jsonobject);
         return new BlockFaceUV(afloat, i);
      }

      protected int getRotation(JsonObject jsonobject) {
         int i = GsonHelper.getAsInt(jsonobject, "rotation", 0);
         if (i >= 0 && i % 90 == 0 && i / 90 <= 3) {
            return i;
         } else {
            throw new JsonParseException("Invalid rotation " + i + " found, only 0/90/180/270 allowed");
         }
      }

      @Nullable
      private float[] getUVs(JsonObject jsonobject) {
         if (!jsonobject.has("uv")) {
            return null;
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "uv");
            if (jsonarray.size() != 4) {
               throw new JsonParseException("Expected 4 uv values, found: " + jsonarray.size());
            } else {
               float[] afloat = new float[4];

               for(int i = 0; i < afloat.length; ++i) {
                  afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), "uv[" + i + "]");
               }

               return afloat;
            }
         }
      }
   }
}
