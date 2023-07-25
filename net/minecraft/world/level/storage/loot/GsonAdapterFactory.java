package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class GsonAdapterFactory {
   public static <E, T extends SerializerType<E>> GsonAdapterFactory.Builder<E, T> builder(Registry<T> registry, String s, String s1, Function<E, T> function) {
      return new GsonAdapterFactory.Builder<>(registry, s, s1, function);
   }

   public static class Builder<E, T extends SerializerType<E>> {
      private final Registry<T> registry;
      private final String elementName;
      private final String typeKey;
      private final Function<E, T> typeGetter;
      @Nullable
      private Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> inlineType;
      @Nullable
      private T defaultType;

      Builder(Registry<T> registry, String s, String s1, Function<E, T> function) {
         this.registry = registry;
         this.elementName = s;
         this.typeKey = s1;
         this.typeGetter = function;
      }

      public GsonAdapterFactory.Builder<E, T> withInlineSerializer(T serializertype, GsonAdapterFactory.InlineSerializer<? extends E> gsonadapterfactory_inlineserializer) {
         this.inlineType = Pair.of(serializertype, gsonadapterfactory_inlineserializer);
         return this;
      }

      public GsonAdapterFactory.Builder<E, T> withDefaultType(T serializertype) {
         this.defaultType = serializertype;
         return this;
      }

      public Object build() {
         return new GsonAdapterFactory.JsonAdapter<>(this.registry, this.elementName, this.typeKey, this.typeGetter, this.defaultType, this.inlineType);
      }
   }

   public interface InlineSerializer<T> {
      JsonElement serialize(T object, JsonSerializationContext jsonserializationcontext);

      T deserialize(JsonElement jsonelement, JsonDeserializationContext jsondeserializationcontext);
   }

   static class JsonAdapter<E, T extends SerializerType<E>> implements JsonDeserializer<E>, JsonSerializer<E> {
      private final Registry<T> registry;
      private final String elementName;
      private final String typeKey;
      private final Function<E, T> typeGetter;
      @Nullable
      private final T defaultType;
      @Nullable
      private final Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> inlineType;

      JsonAdapter(Registry<T> registry, String s, String s1, Function<E, T> function, @Nullable T serializertype, @Nullable Pair<T, GsonAdapterFactory.InlineSerializer<? extends E>> pair) {
         this.registry = registry;
         this.elementName = s;
         this.typeKey = s1;
         this.typeGetter = function;
         this.defaultType = serializertype;
         this.inlineType = pair;
      }

      public E deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         if (jsonelement.isJsonObject()) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, this.elementName);
            String s = GsonHelper.getAsString(jsonobject, this.typeKey, "");
            T serializertype;
            if (s.isEmpty()) {
               serializertype = this.defaultType;
            } else {
               ResourceLocation resourcelocation = new ResourceLocation(s);
               serializertype = this.registry.get(resourcelocation);
            }

            if (serializertype == null) {
               throw new JsonSyntaxException("Unknown type '" + s + "'");
            } else {
               return serializertype.getSerializer().deserialize(jsonobject, jsondeserializationcontext);
            }
         } else if (this.inlineType == null) {
            throw new UnsupportedOperationException("Object " + jsonelement + " can't be deserialized");
         } else {
            return this.inlineType.getSecond().deserialize(jsonelement, jsondeserializationcontext);
         }
      }

      public JsonElement serialize(E object, Type type, JsonSerializationContext jsonserializationcontext) {
         T serializertype = this.typeGetter.apply(object);
         if (this.inlineType != null && this.inlineType.getFirst() == serializertype) {
            return this.inlineType.getSecond().serialize(object, jsonserializationcontext);
         } else if (serializertype == null) {
            throw new JsonSyntaxException("Unknown type: " + object);
         } else {
            JsonObject jsonobject = new JsonObject();
            jsonobject.addProperty(this.typeKey, this.registry.getKey(serializertype).toString());
            serializertype.getSerializer().serialize(jsonobject, object, jsonserializationcontext);
            return jsonobject;
         }
      }
   }
}
