package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DisplayInfo {
   private final Component title;
   private final Component description;
   private final ItemStack icon;
   @Nullable
   private final ResourceLocation background;
   private final FrameType frame;
   private final boolean showToast;
   private final boolean announceChat;
   private final boolean hidden;
   private float x;
   private float y;

   public DisplayInfo(ItemStack itemstack, Component component, Component component1, @Nullable ResourceLocation resourcelocation, FrameType frametype, boolean flag, boolean flag1, boolean flag2) {
      this.title = component;
      this.description = component1;
      this.icon = itemstack;
      this.background = resourcelocation;
      this.frame = frametype;
      this.showToast = flag;
      this.announceChat = flag1;
      this.hidden = flag2;
   }

   public void setLocation(float f, float f1) {
      this.x = f;
      this.y = f1;
   }

   public Component getTitle() {
      return this.title;
   }

   public Component getDescription() {
      return this.description;
   }

   public ItemStack getIcon() {
      return this.icon;
   }

   @Nullable
   public ResourceLocation getBackground() {
      return this.background;
   }

   public FrameType getFrame() {
      return this.frame;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public boolean shouldShowToast() {
      return this.showToast;
   }

   public boolean shouldAnnounceChat() {
      return this.announceChat;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public static DisplayInfo fromJson(JsonObject jsonobject) {
      Component component = Component.Serializer.fromJson(jsonobject.get("title"));
      Component component1 = Component.Serializer.fromJson(jsonobject.get("description"));
      if (component != null && component1 != null) {
         ItemStack itemstack = getIcon(GsonHelper.getAsJsonObject(jsonobject, "icon"));
         ResourceLocation resourcelocation = jsonobject.has("background") ? new ResourceLocation(GsonHelper.getAsString(jsonobject, "background")) : null;
         FrameType frametype = jsonobject.has("frame") ? FrameType.byName(GsonHelper.getAsString(jsonobject, "frame")) : FrameType.TASK;
         boolean flag = GsonHelper.getAsBoolean(jsonobject, "show_toast", true);
         boolean flag1 = GsonHelper.getAsBoolean(jsonobject, "announce_to_chat", true);
         boolean flag2 = GsonHelper.getAsBoolean(jsonobject, "hidden", false);
         return new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, flag1, flag2);
      } else {
         throw new JsonSyntaxException("Both title and description must be set");
      }
   }

   private static ItemStack getIcon(JsonObject jsonobject) {
      if (!jsonobject.has("item")) {
         throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
      } else {
         Item item = GsonHelper.getAsItem(jsonobject, "item");
         if (jsonobject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
         } else {
            ItemStack itemstack = new ItemStack(item);
            if (jsonobject.has("nbt")) {
               try {
                  CompoundTag compoundtag = TagParser.parseTag(GsonHelper.convertToString(jsonobject.get("nbt"), "nbt"));
                  itemstack.setTag(compoundtag);
               } catch (CommandSyntaxException var4) {
                  throw new JsonSyntaxException("Invalid nbt tag: " + var4.getMessage());
               }
            }

            return itemstack;
         }
      }
   }

   public void serializeToNetwork(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeComponent(this.title);
      friendlybytebuf.writeComponent(this.description);
      friendlybytebuf.writeItem(this.icon);
      friendlybytebuf.writeEnum(this.frame);
      int i = 0;
      if (this.background != null) {
         i |= 1;
      }

      if (this.showToast) {
         i |= 2;
      }

      if (this.hidden) {
         i |= 4;
      }

      friendlybytebuf.writeInt(i);
      if (this.background != null) {
         friendlybytebuf.writeResourceLocation(this.background);
      }

      friendlybytebuf.writeFloat(this.x);
      friendlybytebuf.writeFloat(this.y);
   }

   public static DisplayInfo fromNetwork(FriendlyByteBuf friendlybytebuf) {
      Component component = friendlybytebuf.readComponent();
      Component component1 = friendlybytebuf.readComponent();
      ItemStack itemstack = friendlybytebuf.readItem();
      FrameType frametype = friendlybytebuf.readEnum(FrameType.class);
      int i = friendlybytebuf.readInt();
      ResourceLocation resourcelocation = (i & 1) != 0 ? friendlybytebuf.readResourceLocation() : null;
      boolean flag = (i & 2) != 0;
      boolean flag1 = (i & 4) != 0;
      DisplayInfo displayinfo = new DisplayInfo(itemstack, component, component1, resourcelocation, frametype, flag, false, flag1);
      displayinfo.setLocation(friendlybytebuf.readFloat(), friendlybytebuf.readFloat());
      return displayinfo;
   }

   public JsonElement serializeToJson() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("icon", this.serializeIcon());
      jsonobject.add("title", Component.Serializer.toJsonTree(this.title));
      jsonobject.add("description", Component.Serializer.toJsonTree(this.description));
      jsonobject.addProperty("frame", this.frame.getName());
      jsonobject.addProperty("show_toast", this.showToast);
      jsonobject.addProperty("announce_to_chat", this.announceChat);
      jsonobject.addProperty("hidden", this.hidden);
      if (this.background != null) {
         jsonobject.addProperty("background", this.background.toString());
      }

      return jsonobject;
   }

   private JsonObject serializeIcon() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.icon.getItem()).toString());
      if (this.icon.hasTag()) {
         jsonobject.addProperty("nbt", this.icon.getTag().toString());
      }

      return jsonobject;
   }
}
