package net.minecraft.advancements.critereon;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;

public abstract class EntityTypePredicate {
   public static final EntityTypePredicate ANY = new EntityTypePredicate() {
      public boolean matches(EntityType<?> entitytype) {
         return true;
      }

      public JsonElement serializeToJson() {
         return JsonNull.INSTANCE;
      }
   };
   private static final Joiner COMMA_JOINER = Joiner.on(", ");

   public abstract boolean matches(EntityType<?> entitytype);

   public abstract JsonElement serializeToJson();

   public static EntityTypePredicate fromJson(@Nullable JsonElement jsonelement) {
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         String s = GsonHelper.convertToString(jsonelement, "type");
         if (s.startsWith("#")) {
            ResourceLocation resourcelocation = new ResourceLocation(s.substring(1));
            return new EntityTypePredicate.TagPredicate(TagKey.create(Registries.ENTITY_TYPE, resourcelocation));
         } else {
            ResourceLocation resourcelocation1 = new ResourceLocation(s);
            EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.getOptional(resourcelocation1).orElseThrow(() -> new JsonSyntaxException("Unknown entity type '" + resourcelocation1 + "', valid types are: " + COMMA_JOINER.join(BuiltInRegistries.ENTITY_TYPE.keySet())));
            return new EntityTypePredicate.TypePredicate(entitytype);
         }
      } else {
         return ANY;
      }
   }

   public static EntityTypePredicate of(EntityType<?> entitytype) {
      return new EntityTypePredicate.TypePredicate(entitytype);
   }

   public static EntityTypePredicate of(TagKey<EntityType<?>> tagkey) {
      return new EntityTypePredicate.TagPredicate(tagkey);
   }

   static class TagPredicate extends EntityTypePredicate {
      private final TagKey<EntityType<?>> tag;

      public TagPredicate(TagKey<EntityType<?>> tagkey) {
         this.tag = tagkey;
      }

      public boolean matches(EntityType<?> entitytype) {
         return entitytype.is(this.tag);
      }

      public JsonElement serializeToJson() {
         return new JsonPrimitive("#" + this.tag.location());
      }
   }

   static class TypePredicate extends EntityTypePredicate {
      private final EntityType<?> type;

      public TypePredicate(EntityType<?> entitytype) {
         this.type = entitytype;
      }

      public boolean matches(EntityType<?> entitytype) {
         return this.type == entitytype;
      }

      public JsonElement serializeToJson() {
         return new JsonPrimitive(BuiltInRegistries.ENTITY_TYPE.getKey(this.type).toString());
      }
   }
}
