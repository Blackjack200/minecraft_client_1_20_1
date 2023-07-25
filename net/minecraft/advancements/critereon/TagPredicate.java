package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;

public class TagPredicate<T> {
   private final TagKey<T> tag;
   private final boolean expected;

   public TagPredicate(TagKey<T> tagkey, boolean flag) {
      this.tag = tagkey;
      this.expected = flag;
   }

   public static <T> TagPredicate<T> is(TagKey<T> tagkey) {
      return new TagPredicate<>(tagkey, true);
   }

   public static <T> TagPredicate<T> isNot(TagKey<T> tagkey) {
      return new TagPredicate<>(tagkey, false);
   }

   public boolean matches(Holder<T> holder) {
      return holder.is(this.tag) == this.expected;
   }

   public JsonElement serializeToJson() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("id", this.tag.location().toString());
      jsonobject.addProperty("expected", this.expected);
      return jsonobject;
   }

   public static <T> TagPredicate<T> fromJson(@Nullable JsonElement jsonelement, ResourceKey<? extends Registry<T>> resourcekey) {
      if (jsonelement == null) {
         throw new JsonParseException("Expected a tag predicate");
      } else {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "Tag Predicate");
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "id"));
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "expected");
         return new TagPredicate<>(TagKey.create(resourcekey, resourcelocation), flag);
      }
   }
}
