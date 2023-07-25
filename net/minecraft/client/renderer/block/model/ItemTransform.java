package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Type;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ItemTransform {
   public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
   public final Vector3f rotation;
   public final Vector3f translation;
   public final Vector3f scale;

   public ItemTransform(Vector3f vector3f, Vector3f vector3f1, Vector3f vector3f2) {
      this.rotation = new Vector3f((Vector3fc)vector3f);
      this.translation = new Vector3f((Vector3fc)vector3f1);
      this.scale = new Vector3f((Vector3fc)vector3f2);
   }

   public void apply(boolean flag, PoseStack posestack) {
      if (this != NO_TRANSFORM) {
         float f = this.rotation.x();
         float f1 = this.rotation.y();
         float f2 = this.rotation.z();
         if (flag) {
            f1 = -f1;
            f2 = -f2;
         }

         int i = flag ? -1 : 1;
         posestack.translate((float)i * this.translation.x(), this.translation.y(), this.translation.z());
         posestack.mulPose((new Quaternionf()).rotationXYZ(f * ((float)Math.PI / 180F), f1 * ((float)Math.PI / 180F), f2 * ((float)Math.PI / 180F)));
         posestack.scale(this.scale.x(), this.scale.y(), this.scale.z());
      }
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (this.getClass() != object.getClass()) {
         return false;
      } else {
         ItemTransform itemtransform = (ItemTransform)object;
         return this.rotation.equals(itemtransform.rotation) && this.scale.equals(itemtransform.scale) && this.translation.equals(itemtransform.translation);
      }
   }

   public int hashCode() {
      int i = this.rotation.hashCode();
      i = 31 * i + this.translation.hashCode();
      return 31 * i + this.scale.hashCode();
   }

   protected static class Deserializer implements JsonDeserializer<ItemTransform> {
      private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
      private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
      private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
      public static final float MAX_TRANSLATION = 5.0F;
      public static final float MAX_SCALE = 4.0F;

      public ItemTransform deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         Vector3f vector3f = this.getVector3f(jsonobject, "rotation", DEFAULT_ROTATION);
         Vector3f vector3f1 = this.getVector3f(jsonobject, "translation", DEFAULT_TRANSLATION);
         vector3f1.mul(0.0625F);
         vector3f1.set(Mth.clamp(vector3f1.x, -5.0F, 5.0F), Mth.clamp(vector3f1.y, -5.0F, 5.0F), Mth.clamp(vector3f1.z, -5.0F, 5.0F));
         Vector3f vector3f2 = this.getVector3f(jsonobject, "scale", DEFAULT_SCALE);
         vector3f2.set(Mth.clamp(vector3f2.x, -4.0F, 4.0F), Mth.clamp(vector3f2.y, -4.0F, 4.0F), Mth.clamp(vector3f2.z, -4.0F, 4.0F));
         return new ItemTransform(vector3f, vector3f1, vector3f2);
      }

      private Vector3f getVector3f(JsonObject jsonobject, String s, Vector3f vector3f) {
         if (!jsonobject.has(s)) {
            return vector3f;
         } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, s);
            if (jsonarray.size() != 3) {
               throw new JsonParseException("Expected 3 " + s + " values, found: " + jsonarray.size());
            } else {
               float[] afloat = new float[3];

               for(int i = 0; i < afloat.length; ++i) {
                  afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), s + "[" + i + "]");
               }

               return new Vector3f(afloat[0], afloat[1], afloat[2]);
            }
         }
      }
   }
}
