package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class BlockElement {
   private static final boolean DEFAULT_RESCALE = false;
   private static final float MIN_EXTENT = -16.0F;
   private static final float MAX_EXTENT = 32.0F;
   public final Vector3f from;
   public final Vector3f to;
   public final Map<Direction, BlockElementFace> faces;
   public final BlockElementRotation rotation;
   public final boolean shade;

   public BlockElement(Vector3f vector3f, Vector3f vector3f1, Map<Direction, BlockElementFace> map, @Nullable BlockElementRotation blockelementrotation, boolean flag) {
      this.from = vector3f;
      this.to = vector3f1;
      this.faces = map;
      this.rotation = blockelementrotation;
      this.shade = flag;
      this.fillUvs();
   }

   private void fillUvs() {
      for(Map.Entry<Direction, BlockElementFace> map_entry : this.faces.entrySet()) {
         float[] afloat = this.uvsByFace(map_entry.getKey());
         (map_entry.getValue()).uv.setMissingUv(afloat);
      }

   }

   private float[] uvsByFace(Direction direction) {
      switch (direction) {
         case DOWN:
            return new float[]{this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
         case UP:
            return new float[]{this.from.x(), this.from.z(), this.to.x(), this.to.z()};
         case NORTH:
         default:
            return new float[]{16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
         case SOUTH:
            return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
         case WEST:
            return new float[]{this.from.z(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
         case EAST:
            return new float[]{16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
      }
   }

   protected static class Deserializer implements JsonDeserializer<BlockElement> {
      private static final boolean DEFAULT_SHADE = true;

      public BlockElement deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         Vector3f vector3f = this.getFrom(jsonobject);
         Vector3f vector3f1 = this.getTo(jsonobject);
         BlockElementRotation blockelementrotation = this.getRotation(jsonobject);
         Map<Direction, BlockElementFace> map = this.getFaces(jsondeserializationcontext, jsonobject);
         if (jsonobject.has("shade") && !GsonHelper.isBooleanValue(jsonobject, "shade")) {
            throw new JsonParseException("Expected shade to be a Boolean");
         } else {
            boolean flag = GsonHelper.getAsBoolean(jsonobject, "shade", true);
            return new BlockElement(vector3f, vector3f1, map, blockelementrotation, flag);
         }
      }

      @Nullable
      private BlockElementRotation getRotation(JsonObject jsonobject) {
         BlockElementRotation blockelementrotation = null;
         if (jsonobject.has("rotation")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "rotation");
            Vector3f vector3f = this.getVector3f(jsonobject1, "origin");
            vector3f.mul(0.0625F);
            Direction.Axis direction_axis = this.getAxis(jsonobject1);
            float f = this.getAngle(jsonobject1);
            boolean flag = GsonHelper.getAsBoolean(jsonobject1, "rescale", false);
            blockelementrotation = new BlockElementRotation(vector3f, direction_axis, f, flag);
         }

         return blockelementrotation;
      }

      private float getAngle(JsonObject jsonobject) {
         float f = GsonHelper.getAsFloat(jsonobject, "angle");
         if (f != 0.0F && Mth.abs(f) != 22.5F && Mth.abs(f) != 45.0F) {
            throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
         } else {
            return f;
         }
      }

      private Direction.Axis getAxis(JsonObject jsonobject) {
         String s = GsonHelper.getAsString(jsonobject, "axis");
         Direction.Axis direction_axis = Direction.Axis.byName(s.toLowerCase(Locale.ROOT));
         if (direction_axis == null) {
            throw new JsonParseException("Invalid rotation axis: " + s);
         } else {
            return direction_axis;
         }
      }

      private Map<Direction, BlockElementFace> getFaces(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         Map<Direction, BlockElementFace> map = this.filterNullFromFaces(jsondeserializationcontext, jsonobject);
         if (map.isEmpty()) {
            throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
         } else {
            return map;
         }
      }

      private Map<Direction, BlockElementFace> filterNullFromFaces(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         Map<Direction, BlockElementFace> map = Maps.newEnumMap(Direction.class);
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "faces");

         for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
            Direction direction = this.getFacing(map_entry.getKey());
            map.put(direction, jsondeserializationcontext.deserialize(map_entry.getValue(), BlockElementFace.class));
         }

         return map;
      }

      private Direction getFacing(String s) {
         Direction direction = Direction.byName(s);
         if (direction == null) {
            throw new JsonParseException("Unknown facing: " + s);
         } else {
            return direction;
         }
      }

      private Vector3f getTo(JsonObject jsonobject) {
         Vector3f vector3f = this.getVector3f(jsonobject, "to");
         if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F)) {
            return vector3f;
         } else {
            throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vector3f);
         }
      }

      private Vector3f getFrom(JsonObject jsonobject) {
         Vector3f vector3f = this.getVector3f(jsonobject, "from");
         if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F)) {
            return vector3f;
         } else {
            throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vector3f);
         }
      }

      private Vector3f getVector3f(JsonObject jsonobject, String s) {
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
