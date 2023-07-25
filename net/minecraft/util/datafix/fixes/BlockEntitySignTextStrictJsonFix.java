package net.minecraft.util.datafix.fixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.lang.reflect.Type;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.StringUtils;

public class BlockEntitySignTextStrictJsonFix extends NamedEntityFix {
   public static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(Component.class, new JsonDeserializer<Component>() {
      public MutableComponent deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         if (jsonelement.isJsonPrimitive()) {
            return Component.literal(jsonelement.getAsString());
         } else if (jsonelement.isJsonArray()) {
            JsonArray jsonarray = jsonelement.getAsJsonArray();
            MutableComponent mutablecomponent = null;

            for(JsonElement jsonelement1 : jsonarray) {
               MutableComponent mutablecomponent1 = this.deserialize(jsonelement1, jsonelement1.getClass(), jsondeserializationcontext);
               if (mutablecomponent == null) {
                  mutablecomponent = mutablecomponent1;
               } else {
                  mutablecomponent.append(mutablecomponent1);
               }
            }

            return mutablecomponent;
         } else {
            throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
         }
      }
   }).create();

   public BlockEntitySignTextStrictJsonFix(Schema schema, boolean flag) {
      super(schema, flag, "BlockEntitySignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
   }

   private Dynamic<?> updateLine(Dynamic<?> dynamic, String s) {
      String s1 = dynamic.get(s).asString("");
      Component component = null;
      if (!"null".equals(s1) && !StringUtils.isEmpty(s1)) {
         if (s1.charAt(0) == '"' && s1.charAt(s1.length() - 1) == '"' || s1.charAt(0) == '{' && s1.charAt(s1.length() - 1) == '}') {
            try {
               component = GsonHelper.fromNullableJson(GSON, s1, Component.class, true);
               if (component == null) {
                  component = CommonComponents.EMPTY;
               }
            } catch (Exception var8) {
            }

            if (component == null) {
               try {
                  component = Component.Serializer.fromJson(s1);
               } catch (Exception var7) {
               }
            }

            if (component == null) {
               try {
                  component = Component.Serializer.fromJsonLenient(s1);
               } catch (Exception var6) {
               }
            }

            if (component == null) {
               component = Component.literal(s1);
            }
         } else {
            component = Component.literal(s1);
         }
      } else {
         component = CommonComponents.EMPTY;
      }

      return dynamic.set(s, dynamic.createString(Component.Serializer.toJson(component)));
   }

   protected Typed<?> fix(Typed<?> typed) {
      return typed.update(DSL.remainderFinder(), (dynamic) -> {
         dynamic = this.updateLine(dynamic, "Text1");
         dynamic = this.updateLine(dynamic, "Text2");
         dynamic = this.updateLine(dynamic, "Text3");
         return this.updateLine(dynamic, "Text4");
      });
   }
}
