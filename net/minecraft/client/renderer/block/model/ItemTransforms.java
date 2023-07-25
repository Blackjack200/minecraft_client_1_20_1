package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.world.item.ItemDisplayContext;

public class ItemTransforms {
   public static final ItemTransforms NO_TRANSFORMS = new ItemTransforms();
   public final ItemTransform thirdPersonLeftHand;
   public final ItemTransform thirdPersonRightHand;
   public final ItemTransform firstPersonLeftHand;
   public final ItemTransform firstPersonRightHand;
   public final ItemTransform head;
   public final ItemTransform gui;
   public final ItemTransform ground;
   public final ItemTransform fixed;

   private ItemTransforms() {
      this(ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM, ItemTransform.NO_TRANSFORM);
   }

   public ItemTransforms(ItemTransforms itemtransforms) {
      this.thirdPersonLeftHand = itemtransforms.thirdPersonLeftHand;
      this.thirdPersonRightHand = itemtransforms.thirdPersonRightHand;
      this.firstPersonLeftHand = itemtransforms.firstPersonLeftHand;
      this.firstPersonRightHand = itemtransforms.firstPersonRightHand;
      this.head = itemtransforms.head;
      this.gui = itemtransforms.gui;
      this.ground = itemtransforms.ground;
      this.fixed = itemtransforms.fixed;
   }

   public ItemTransforms(ItemTransform itemtransform, ItemTransform itemtransform1, ItemTransform itemtransform2, ItemTransform itemtransform3, ItemTransform itemtransform4, ItemTransform itemtransform5, ItemTransform itemtransform6, ItemTransform itemtransform7) {
      this.thirdPersonLeftHand = itemtransform;
      this.thirdPersonRightHand = itemtransform1;
      this.firstPersonLeftHand = itemtransform2;
      this.firstPersonRightHand = itemtransform3;
      this.head = itemtransform4;
      this.gui = itemtransform5;
      this.ground = itemtransform6;
      this.fixed = itemtransform7;
   }

   public ItemTransform getTransform(ItemDisplayContext itemdisplaycontext) {
      ItemTransform var10000;
      switch (itemdisplaycontext) {
         case THIRD_PERSON_LEFT_HAND:
            var10000 = this.thirdPersonLeftHand;
            break;
         case THIRD_PERSON_RIGHT_HAND:
            var10000 = this.thirdPersonRightHand;
            break;
         case FIRST_PERSON_LEFT_HAND:
            var10000 = this.firstPersonLeftHand;
            break;
         case FIRST_PERSON_RIGHT_HAND:
            var10000 = this.firstPersonRightHand;
            break;
         case HEAD:
            var10000 = this.head;
            break;
         case GUI:
            var10000 = this.gui;
            break;
         case GROUND:
            var10000 = this.ground;
            break;
         case FIXED:
            var10000 = this.fixed;
            break;
         default:
            var10000 = ItemTransform.NO_TRANSFORM;
      }

      return var10000;
   }

   public boolean hasTransform(ItemDisplayContext itemdisplaycontext) {
      return this.getTransform(itemdisplaycontext) != ItemTransform.NO_TRANSFORM;
   }

   protected static class Deserializer implements JsonDeserializer<ItemTransforms> {
      public ItemTransforms deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         JsonObject jsonobject = jsonelement.getAsJsonObject();
         ItemTransform itemtransform = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
         ItemTransform itemtransform1 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
         if (itemtransform1 == ItemTransform.NO_TRANSFORM) {
            itemtransform1 = itemtransform;
         }

         ItemTransform itemtransform2 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
         ItemTransform itemtransform3 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
         if (itemtransform3 == ItemTransform.NO_TRANSFORM) {
            itemtransform3 = itemtransform2;
         }

         ItemTransform itemtransform4 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.HEAD);
         ItemTransform itemtransform5 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.GUI);
         ItemTransform itemtransform6 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.GROUND);
         ItemTransform itemtransform7 = this.getTransform(jsondeserializationcontext, jsonobject, ItemDisplayContext.FIXED);
         return new ItemTransforms(itemtransform1, itemtransform, itemtransform3, itemtransform2, itemtransform4, itemtransform5, itemtransform6, itemtransform7);
      }

      private ItemTransform getTransform(JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject, ItemDisplayContext itemdisplaycontext) {
         String s = itemdisplaycontext.getSerializedName();
         return jsonobject.has(s) ? jsondeserializationcontext.deserialize(jsonobject.get(s), ItemTransform.class) : ItemTransform.NO_TRANSFORM;
      }
   }
}
