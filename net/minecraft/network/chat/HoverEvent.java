package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public class HoverEvent {
   static final Logger LOGGER = LogUtils.getLogger();
   private final HoverEvent.Action<?> action;
   private final Object value;

   public <T> HoverEvent(HoverEvent.Action<T> hoverevent_action, T object) {
      this.action = hoverevent_action;
      this.value = object;
   }

   public HoverEvent.Action<?> getAction() {
      return this.action;
   }

   @Nullable
   public <T> T getValue(HoverEvent.Action<T> hoverevent_action) {
      return (T)(this.action == hoverevent_action ? hoverevent_action.cast(this.value) : null);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         HoverEvent hoverevent = (HoverEvent)object;
         return this.action == hoverevent.action && Objects.equals(this.value, hoverevent.value);
      } else {
         return false;
      }
   }

   public String toString() {
      return "HoverEvent{action=" + this.action + ", value='" + this.value + "'}";
   }

   public int hashCode() {
      int i = this.action.hashCode();
      return 31 * i + (this.value != null ? this.value.hashCode() : 0);
   }

   @Nullable
   public static HoverEvent deserialize(JsonObject jsonobject) {
      String s = GsonHelper.getAsString(jsonobject, "action", (String)null);
      if (s == null) {
         return null;
      } else {
         HoverEvent.Action<?> hoverevent_action = HoverEvent.Action.getByName(s);
         if (hoverevent_action == null) {
            return null;
         } else {
            JsonElement jsonelement = jsonobject.get("contents");
            if (jsonelement != null) {
               return hoverevent_action.deserialize(jsonelement);
            } else {
               Component component = Component.Serializer.fromJson(jsonobject.get("value"));
               return component != null ? hoverevent_action.deserializeFromLegacy(component) : null;
            }
         }
      }
   }

   public JsonObject serialize() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("action", this.action.getName());
      jsonobject.add("contents", this.action.serializeArg(this.value));
      return jsonobject;
   }

   public static class Action<T> {
      public static final HoverEvent.Action<Component> SHOW_TEXT = new HoverEvent.Action<>("show_text", true, Component.Serializer::fromJson, Component.Serializer::toJsonTree, Function.identity());
      public static final HoverEvent.Action<HoverEvent.ItemStackInfo> SHOW_ITEM = new HoverEvent.Action<>("show_item", true, HoverEvent.ItemStackInfo::create, HoverEvent.ItemStackInfo::serialize, HoverEvent.ItemStackInfo::create);
      public static final HoverEvent.Action<HoverEvent.EntityTooltipInfo> SHOW_ENTITY = new HoverEvent.Action<>("show_entity", true, HoverEvent.EntityTooltipInfo::create, HoverEvent.EntityTooltipInfo::serialize, HoverEvent.EntityTooltipInfo::create);
      private static final Map<String, HoverEvent.Action<?>> LOOKUP = Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY).collect(ImmutableMap.toImmutableMap(HoverEvent.Action::getName, (hoverevent_action) -> hoverevent_action));
      private final String name;
      private final boolean allowFromServer;
      private final Function<JsonElement, T> argDeserializer;
      private final Function<T, JsonElement> argSerializer;
      private final Function<Component, T> legacyArgDeserializer;

      public Action(String s, boolean flag, Function<JsonElement, T> function, Function<T, JsonElement> function1, Function<Component, T> function2) {
         this.name = s;
         this.allowFromServer = flag;
         this.argDeserializer = function;
         this.argSerializer = function1;
         this.legacyArgDeserializer = function2;
      }

      public boolean isAllowedFromServer() {
         return this.allowFromServer;
      }

      public String getName() {
         return this.name;
      }

      @Nullable
      public static HoverEvent.Action<?> getByName(String s) {
         return LOOKUP.get(s);
      }

      T cast(Object object) {
         return (T)object;
      }

      @Nullable
      public HoverEvent deserialize(JsonElement jsonelement) {
         T object = this.argDeserializer.apply(jsonelement);
         return object == null ? null : new HoverEvent(this, object);
      }

      @Nullable
      public HoverEvent deserializeFromLegacy(Component component) {
         T object = this.legacyArgDeserializer.apply(component);
         return object == null ? null : new HoverEvent(this, object);
      }

      public JsonElement serializeArg(Object object) {
         return this.argSerializer.apply(this.cast(object));
      }

      public String toString() {
         return "<action " + this.name + ">";
      }
   }

   public static class EntityTooltipInfo {
      public final EntityType<?> type;
      public final UUID id;
      @Nullable
      public final Component name;
      @Nullable
      private List<Component> linesCache;

      public EntityTooltipInfo(EntityType<?> entitytype, UUID uuid, @Nullable Component component) {
         this.type = entitytype;
         this.id = uuid;
         this.name = component;
      }

      @Nullable
      public static HoverEvent.EntityTooltipInfo create(JsonElement jsonelement) {
         if (!jsonelement.isJsonObject()) {
            return null;
         } else {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(GsonHelper.getAsString(jsonobject, "type")));
            UUID uuid = UUID.fromString(GsonHelper.getAsString(jsonobject, "id"));
            Component component = Component.Serializer.fromJson(jsonobject.get("name"));
            return new HoverEvent.EntityTooltipInfo(entitytype, uuid, component);
         }
      }

      @Nullable
      public static HoverEvent.EntityTooltipInfo create(Component component) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(component.getString());
            Component component1 = Component.Serializer.fromJson(compoundtag.getString("name"));
            EntityType<?> entitytype = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(compoundtag.getString("type")));
            UUID uuid = UUID.fromString(compoundtag.getString("id"));
            return new HoverEvent.EntityTooltipInfo(entitytype, uuid, component1);
         } catch (Exception var5) {
            return null;
         }
      }

      public JsonElement serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(this.type).toString());
         jsonobject.addProperty("id", this.id.toString());
         if (this.name != null) {
            jsonobject.add("name", Component.Serializer.toJsonTree(this.name));
         }

         return jsonobject;
      }

      public List<Component> getTooltipLines() {
         if (this.linesCache == null) {
            this.linesCache = Lists.newArrayList();
            if (this.name != null) {
               this.linesCache.add(this.name);
            }

            this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
            this.linesCache.add(Component.literal(this.id.toString()));
         }

         return this.linesCache;
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            HoverEvent.EntityTooltipInfo hoverevent_entitytooltipinfo = (HoverEvent.EntityTooltipInfo)object;
            return this.type.equals(hoverevent_entitytooltipinfo.type) && this.id.equals(hoverevent_entitytooltipinfo.id) && Objects.equals(this.name, hoverevent_entitytooltipinfo.name);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.type.hashCode();
         i = 31 * i + this.id.hashCode();
         return 31 * i + (this.name != null ? this.name.hashCode() : 0);
      }
   }

   public static class ItemStackInfo {
      private final Item item;
      private final int count;
      @Nullable
      private final CompoundTag tag;
      @Nullable
      private ItemStack itemStack;

      ItemStackInfo(Item item, int i, @Nullable CompoundTag compoundtag) {
         this.item = item;
         this.count = i;
         this.tag = compoundtag;
      }

      public ItemStackInfo(ItemStack itemstack) {
         this(itemstack.getItem(), itemstack.getCount(), itemstack.getTag() != null ? itemstack.getTag().copy() : null);
      }

      public boolean equals(Object object) {
         if (this == object) {
            return true;
         } else if (object != null && this.getClass() == object.getClass()) {
            HoverEvent.ItemStackInfo hoverevent_itemstackinfo = (HoverEvent.ItemStackInfo)object;
            return this.count == hoverevent_itemstackinfo.count && this.item.equals(hoverevent_itemstackinfo.item) && Objects.equals(this.tag, hoverevent_itemstackinfo.tag);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.item.hashCode();
         i = 31 * i + this.count;
         return 31 * i + (this.tag != null ? this.tag.hashCode() : 0);
      }

      public ItemStack getItemStack() {
         if (this.itemStack == null) {
            this.itemStack = new ItemStack(this.item, this.count);
            if (this.tag != null) {
               this.itemStack.setTag(this.tag);
            }
         }

         return this.itemStack;
      }

      private static HoverEvent.ItemStackInfo create(JsonElement jsonelement) {
         if (jsonelement.isJsonPrimitive()) {
            return new HoverEvent.ItemStackInfo(BuiltInRegistries.ITEM.get(new ResourceLocation(jsonelement.getAsString())), 1, (CompoundTag)null);
         } else {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(jsonelement, "item");
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonobject, "id")));
            int i = GsonHelper.getAsInt(jsonobject, "count", 1);
            if (jsonobject.has("tag")) {
               String s = GsonHelper.getAsString(jsonobject, "tag");

               try {
                  CompoundTag compoundtag = TagParser.parseTag(s);
                  return new HoverEvent.ItemStackInfo(item, i, compoundtag);
               } catch (CommandSyntaxException var6) {
                  HoverEvent.LOGGER.warn("Failed to parse tag: {}", s, var6);
               }
            }

            return new HoverEvent.ItemStackInfo(item, i, (CompoundTag)null);
         }
      }

      @Nullable
      private static HoverEvent.ItemStackInfo create(Component component) {
         try {
            CompoundTag compoundtag = TagParser.parseTag(component.getString());
            return new HoverEvent.ItemStackInfo(ItemStack.of(compoundtag));
         } catch (CommandSyntaxException var2) {
            HoverEvent.LOGGER.warn("Failed to parse item tag: {}", component, var2);
            return null;
         }
      }

      private JsonElement serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("id", BuiltInRegistries.ITEM.getKey(this.item).toString());
         if (this.count != 1) {
            jsonobject.addProperty("count", this.count);
         }

         if (this.tag != null) {
            jsonobject.addProperty("tag", this.tag.toString());
         }

         return jsonobject;
      }
   }
}
