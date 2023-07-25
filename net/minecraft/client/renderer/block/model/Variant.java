package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Variant implements ModelState {
   private final ResourceLocation modelLocation;
   private final Transformation rotation;
   private final boolean uvLock;
   private final int weight;

   public Variant(ResourceLocation resourcelocation, Transformation transformation, boolean flag, int i) {
      this.modelLocation = resourcelocation;
      this.rotation = transformation;
      this.uvLock = flag;
      this.weight = i;
   }

   public ResourceLocation getModelLocation() {
      return this.modelLocation;
   }

   public Transformation getRotation() {
      return this.rotation;
   }

   public boolean isUvLocked() {
      return this.uvLock;
   }

   public int getWeight() {
      return this.weight;
   }

   public String toString() {
      return "Variant{modelLocation=" + this.modelLocation + ", rotation=" + this.rotation + ", uvLock=" + this.uvLock + ", weight=" + this.weight + "}";
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Variant)) {
         return false;
      } else {
         Variant variant = (Variant)object;
         return this.modelLocation.equals(variant.modelLocation) && Objects.equals(this.rotation, variant.rotation) && this.uvLock == variant.uvLock && this.weight == variant.weight;
      }
   }

   public int hashCode() {
      int i = this.modelLocation.hashCode();
      i = 31 * i + this.rotation.hashCode();
      i = 31 * i + Boolean.valueOf(this.uvLock).hashCode();
      return 31 * i + this.weight;
   }

   public static class Deserializer implements JsonDeserializer<Variant> {
      @VisibleForTesting
      static final boolean DEFAULT_UVLOCK = false;
      @VisibleForTesting
      static final int DEFAULT_WEIGHT = 1;
      @VisibleForTesting
      static final int DEFAULT_X_ROTATION = 0;
      @VisibleForTesting
      static final int DEFAULT_Y_ROTATION = 0;

      public Variant deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         ResourceLocation resourcelocation = this.getModel(jsonobject);
         BlockModelRotation blockmodelrotation = this.getBlockRotation(jsonobject);
         boolean flag = this.getUvLock(jsonobject);
         int i = this.getWeight(jsonobject);
         return new Variant(resourcelocation, blockmodelrotation.getRotation(), flag, i);
      }

      private boolean getUvLock(JsonObject jsonobject) {
         return GsonHelper.getAsBoolean(jsonobject, "uvlock", false);
      }

      protected BlockModelRotation getBlockRotation(JsonObject jsonobject) {
         int i = GsonHelper.getAsInt(jsonobject, "x", 0);
         int j = GsonHelper.getAsInt(jsonobject, "y", 0);
         BlockModelRotation blockmodelrotation = BlockModelRotation.by(i, j);
         if (blockmodelrotation == null) {
            throw new JsonParseException("Invalid BlockModelRotation x: " + i + ", y: " + j);
         } else {
            return blockmodelrotation;
         }
      }

      protected ResourceLocation getModel(JsonObject jsonobject) {
         return new ResourceLocation(GsonHelper.getAsString(jsonobject, "model"));
      }

      protected int getWeight(JsonObject jsonobject) {
         int i = GsonHelper.getAsInt(jsonobject, "weight", 1);
         if (i < 1) {
            throw new JsonParseException("Invalid weight " + i + " found, expected integer >= 1");
         } else {
            return i;
         }
      }
   }
}
