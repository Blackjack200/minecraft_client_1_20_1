package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.contents.BlockDataSource;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.network.chat.contents.EntityDataSource;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.StorageDataSource;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, FormattedText {
   Style getStyle();

   ComponentContents getContents();

   default String getString() {
      return FormattedText.super.getString();
   }

   default String getString(int i) {
      StringBuilder stringbuilder = new StringBuilder();
      this.visit((s) -> {
         int k = i - stringbuilder.length();
         if (k <= 0) {
            return STOP_ITERATION;
         } else {
            stringbuilder.append(s.length() <= k ? s : s.substring(0, k));
            return Optional.empty();
         }
      });
      return stringbuilder.toString();
   }

   List<Component> getSiblings();

   default MutableComponent plainCopy() {
      return MutableComponent.create(this.getContents());
   }

   default MutableComponent copy() {
      return new MutableComponent(this.getContents(), new ArrayList<>(this.getSiblings()), this.getStyle());
   }

   FormattedCharSequence getVisualOrderText();

   default <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
      Style style1 = this.getStyle().applyTo(style);
      Optional<T> optional = this.getContents().visit(formattedtext_styledcontentconsumer, style1);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(Component component : this.getSiblings()) {
            Optional<T> optional1 = component.visit(formattedtext_styledcontentconsumer, style1);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
      Optional<T> optional = this.getContents().visit(formattedtext_contentconsumer);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(Component component : this.getSiblings()) {
            Optional<T> optional1 = component.visit(formattedtext_contentconsumer);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default List<Component> toFlatList() {
      return this.toFlatList(Style.EMPTY);
   }

   default List<Component> toFlatList(Style style) {
      List<Component> list = Lists.newArrayList();
      this.visit((style1, s) -> {
         if (!s.isEmpty()) {
            list.add(literal(s).withStyle(style1));
         }

         return Optional.empty();
      }, style);
      return list;
   }

   default boolean contains(Component component) {
      if (this.equals(component)) {
         return true;
      } else {
         List<Component> list = this.toFlatList();
         List<Component> list1 = component.toFlatList(this.getStyle());
         return Collections.indexOfSubList(list, list1) != -1;
      }
   }

   static Component nullToEmpty(@Nullable String s) {
      return (Component)(s != null ? literal(s) : CommonComponents.EMPTY);
   }

   static MutableComponent literal(String s) {
      return MutableComponent.create(new LiteralContents(s));
   }

   static MutableComponent translatable(String s) {
      return MutableComponent.create(new TranslatableContents(s, (String)null, TranslatableContents.NO_ARGS));
   }

   static MutableComponent translatable(String s, Object... aobject) {
      return MutableComponent.create(new TranslatableContents(s, (String)null, aobject));
   }

   static MutableComponent translatableWithFallback(String s, @Nullable String s1) {
      return MutableComponent.create(new TranslatableContents(s, s1, TranslatableContents.NO_ARGS));
   }

   static MutableComponent translatableWithFallback(String s, @Nullable String s1, Object... aobject) {
      return MutableComponent.create(new TranslatableContents(s, s1, aobject));
   }

   static MutableComponent empty() {
      return MutableComponent.create(ComponentContents.EMPTY);
   }

   static MutableComponent keybind(String s) {
      return MutableComponent.create(new KeybindContents(s));
   }

   static MutableComponent nbt(String s, boolean flag, Optional<Component> optional, DataSource datasource) {
      return MutableComponent.create(new NbtContents(s, flag, optional, datasource));
   }

   static MutableComponent score(String s, String s1) {
      return MutableComponent.create(new ScoreContents(s, s1));
   }

   static MutableComponent selector(String s, Optional<Component> optional) {
      return MutableComponent.create(new SelectorContents(s, optional));
   }

   public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
      private static final Gson GSON = Util.make(() -> {
         GsonBuilder gsonbuilder = new GsonBuilder();
         gsonbuilder.disableHtmlEscaping();
         gsonbuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
         gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         gsonbuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         return gsonbuilder.create();
      });
      private static final Field JSON_READER_POS = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
         }
      });
      private static final Field JSON_READER_LINESTART = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
         }
      });

      public MutableComponent deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         if (jsonelement.isJsonPrimitive()) {
            return Component.literal(jsonelement.getAsString());
         } else if (!jsonelement.isJsonObject()) {
            if (jsonelement.isJsonArray()) {
               JsonArray jsonarray2 = jsonelement.getAsJsonArray();
               MutableComponent mutablecomponent9 = null;

               for(JsonElement jsonelement1 : jsonarray2) {
                  MutableComponent mutablecomponent10 = this.deserialize(jsonelement1, jsonelement1.getClass(), jsondeserializationcontext);
                  if (mutablecomponent9 == null) {
                     mutablecomponent9 = mutablecomponent10;
                  } else {
                     mutablecomponent9.append(mutablecomponent10);
                  }
               }

               return mutablecomponent9;
            } else {
               throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
            }
         } else {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            MutableComponent mutablecomponent;
            if (jsonobject.has("text")) {
               String s = GsonHelper.getAsString(jsonobject, "text");
               mutablecomponent = s.isEmpty() ? Component.empty() : Component.literal(s);
            } else if (jsonobject.has("translate")) {
               String s1 = GsonHelper.getAsString(jsonobject, "translate");
               String s2 = GsonHelper.getAsString(jsonobject, "fallback", (String)null);
               if (jsonobject.has("with")) {
                  JsonArray jsonarray = GsonHelper.getAsJsonArray(jsonobject, "with");
                  Object[] aobject = new Object[jsonarray.size()];

                  for(int i = 0; i < aobject.length; ++i) {
                     aobject[i] = unwrapTextArgument(this.deserialize(jsonarray.get(i), type, jsondeserializationcontext));
                  }

                  mutablecomponent = Component.translatableWithFallback(s1, s2, aobject);
               } else {
                  mutablecomponent = Component.translatableWithFallback(s1, s2);
               }
            } else if (jsonobject.has("score")) {
               JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "score");
               if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                  throw new JsonParseException("A score component needs a least a name and an objective");
               }

               mutablecomponent = Component.score(GsonHelper.getAsString(jsonobject1, "name"), GsonHelper.getAsString(jsonobject1, "objective"));
            } else if (jsonobject.has("selector")) {
               Optional<Component> optional = this.parseSeparator(type, jsondeserializationcontext, jsonobject);
               mutablecomponent = Component.selector(GsonHelper.getAsString(jsonobject, "selector"), optional);
            } else if (jsonobject.has("keybind")) {
               mutablecomponent = Component.keybind(GsonHelper.getAsString(jsonobject, "keybind"));
            } else {
               if (!jsonobject.has("nbt")) {
                  throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
               }

               String s3 = GsonHelper.getAsString(jsonobject, "nbt");
               Optional<Component> optional1 = this.parseSeparator(type, jsondeserializationcontext, jsonobject);
               boolean flag = GsonHelper.getAsBoolean(jsonobject, "interpret", false);
               DataSource datasource;
               if (jsonobject.has("block")) {
                  datasource = new BlockDataSource(GsonHelper.getAsString(jsonobject, "block"));
               } else if (jsonobject.has("entity")) {
                  datasource = new EntityDataSource(GsonHelper.getAsString(jsonobject, "entity"));
               } else {
                  if (!jsonobject.has("storage")) {
                     throw new JsonParseException("Don't know how to turn " + jsonelement + " into a Component");
                  }

                  datasource = new StorageDataSource(new ResourceLocation(GsonHelper.getAsString(jsonobject, "storage")));
               }

               mutablecomponent = Component.nbt(s3, flag, optional1, datasource);
            }

            if (jsonobject.has("extra")) {
               JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "extra");
               if (jsonarray1.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int j = 0; j < jsonarray1.size(); ++j) {
                  mutablecomponent.append(this.deserialize(jsonarray1.get(j), type, jsondeserializationcontext));
               }
            }

            mutablecomponent.setStyle(jsondeserializationcontext.deserialize(jsonelement, Style.class));
            return mutablecomponent;
         }
      }

      private static Object unwrapTextArgument(Object object) {
         if (object instanceof Component component) {
            if (component.getStyle().isEmpty() && component.getSiblings().isEmpty()) {
               ComponentContents componentcontents = component.getContents();
               if (componentcontents instanceof LiteralContents) {
                  LiteralContents literalcontents = (LiteralContents)componentcontents;
                  return literalcontents.text();
               }
            }
         }

         return object;
      }

      private Optional<Component> parseSeparator(Type type, JsonDeserializationContext jsondeserializationcontext, JsonObject jsonobject) {
         return jsonobject.has("separator") ? Optional.of(this.deserialize(jsonobject.get("separator"), type, jsondeserializationcontext)) : Optional.empty();
      }

      private void serializeStyle(Style style, JsonObject jsonobject, JsonSerializationContext jsonserializationcontext) {
         JsonElement jsonelement = jsonserializationcontext.serialize(style);
         if (jsonelement.isJsonObject()) {
            JsonObject jsonobject1 = (JsonObject)jsonelement;

            for(Map.Entry<String, JsonElement> map_entry : jsonobject1.entrySet()) {
               jsonobject.add(map_entry.getKey(), map_entry.getValue());
            }
         }

      }

      public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonserializationcontext) {
         JsonObject jsonobject = new JsonObject();
         if (!component.getStyle().isEmpty()) {
            this.serializeStyle(component.getStyle(), jsonobject, jsonserializationcontext);
         }

         if (!component.getSiblings().isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Component component1 : component.getSiblings()) {
               jsonarray.add(this.serialize(component1, Component.class, jsonserializationcontext));
            }

            jsonobject.add("extra", jsonarray);
         }

         ComponentContents componentcontents = component.getContents();
         if (componentcontents == ComponentContents.EMPTY) {
            jsonobject.addProperty("text", "");
         } else if (componentcontents instanceof LiteralContents) {
            LiteralContents literalcontents = (LiteralContents)componentcontents;
            jsonobject.addProperty("text", literalcontents.text());
         } else if (componentcontents instanceof TranslatableContents) {
            TranslatableContents translatablecontents = (TranslatableContents)componentcontents;
            jsonobject.addProperty("translate", translatablecontents.getKey());
            String s = translatablecontents.getFallback();
            if (s != null) {
               jsonobject.addProperty("fallback", s);
            }

            if (translatablecontents.getArgs().length > 0) {
               JsonArray jsonarray1 = new JsonArray();

               for(Object object : translatablecontents.getArgs()) {
                  if (object instanceof Component) {
                     jsonarray1.add(this.serialize((Component)object, object.getClass(), jsonserializationcontext));
                  } else {
                     jsonarray1.add(new JsonPrimitive(String.valueOf(object)));
                  }
               }

               jsonobject.add("with", jsonarray1);
            }
         } else if (componentcontents instanceof ScoreContents) {
            ScoreContents scorecontents = (ScoreContents)componentcontents;
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("name", scorecontents.getName());
            jsonobject1.addProperty("objective", scorecontents.getObjective());
            jsonobject.add("score", jsonobject1);
         } else if (componentcontents instanceof SelectorContents) {
            SelectorContents selectorcontents = (SelectorContents)componentcontents;
            jsonobject.addProperty("selector", selectorcontents.getPattern());
            this.serializeSeparator(jsonserializationcontext, jsonobject, selectorcontents.getSeparator());
         } else if (componentcontents instanceof KeybindContents) {
            KeybindContents keybindcontents = (KeybindContents)componentcontents;
            jsonobject.addProperty("keybind", keybindcontents.getName());
         } else {
            if (!(componentcontents instanceof NbtContents)) {
               throw new IllegalArgumentException("Don't know how to serialize " + componentcontents + " as a Component");
            }

            NbtContents nbtcontents = (NbtContents)componentcontents;
            jsonobject.addProperty("nbt", nbtcontents.getNbtPath());
            jsonobject.addProperty("interpret", nbtcontents.isInterpreting());
            this.serializeSeparator(jsonserializationcontext, jsonobject, nbtcontents.getSeparator());
            DataSource datasource = nbtcontents.getDataSource();
            if (datasource instanceof BlockDataSource) {
               BlockDataSource blockdatasource = (BlockDataSource)datasource;
               jsonobject.addProperty("block", blockdatasource.posPattern());
            } else if (datasource instanceof EntityDataSource) {
               EntityDataSource entitydatasource = (EntityDataSource)datasource;
               jsonobject.addProperty("entity", entitydatasource.selectorPattern());
            } else {
               if (!(datasource instanceof StorageDataSource)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + componentcontents + " as a Component");
               }

               StorageDataSource storagedatasource = (StorageDataSource)datasource;
               jsonobject.addProperty("storage", storagedatasource.id().toString());
            }
         }

         return jsonobject;
      }

      private void serializeSeparator(JsonSerializationContext jsonserializationcontext, JsonObject jsonobject, Optional<Component> optional) {
         optional.ifPresent((component) -> jsonobject.add("separator", this.serialize(component, component.getClass(), jsonserializationcontext)));
      }

      public static String toJson(Component component) {
         return GSON.toJson(component);
      }

      public static String toStableJson(Component component) {
         return GsonHelper.toStableString(toJsonTree(component));
      }

      public static JsonElement toJsonTree(Component component) {
         return GSON.toJsonTree(component);
      }

      @Nullable
      public static MutableComponent fromJson(String s) {
         return GsonHelper.fromNullableJson(GSON, s, MutableComponent.class, false);
      }

      @Nullable
      public static MutableComponent fromJson(JsonElement jsonelement) {
         return GSON.fromJson(jsonelement, MutableComponent.class);
      }

      @Nullable
      public static MutableComponent fromJsonLenient(String s) {
         return GsonHelper.fromNullableJson(GSON, s, MutableComponent.class, true);
      }

      public static MutableComponent fromJson(com.mojang.brigadier.StringReader stringreader) {
         try {
            JsonReader jsonreader = new JsonReader(new StringReader(stringreader.getRemaining()));
            jsonreader.setLenient(false);
            MutableComponent mutablecomponent = GSON.getAdapter(MutableComponent.class).read(jsonreader);
            stringreader.setCursor(stringreader.getCursor() + getPos(jsonreader));
            return mutablecomponent;
         } catch (StackOverflowError | IOException var3) {
            throw new JsonParseException(var3);
         }
      }

      private static int getPos(JsonReader jsonreader) {
         try {
            return JSON_READER_POS.getInt(jsonreader) - JSON_READER_LINESTART.getInt(jsonreader) + 1;
         } catch (IllegalAccessException var2) {
            throw new IllegalStateException("Couldn't read position of JsonReader", var2);
         }
      }
   }
}
