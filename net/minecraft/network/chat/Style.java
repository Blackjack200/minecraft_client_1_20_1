package net.minecraft.network.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class Style {
   public static final Style EMPTY = new Style((TextColor)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (ClickEvent)null, (HoverEvent)null, (String)null, (ResourceLocation)null);
   public static final Codec<Style> FORMATTING_CODEC = RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(TextColor.CODEC.optionalFieldOf("color").forGetter((style7) -> Optional.ofNullable(style7.color)), Codec.BOOL.optionalFieldOf("bold").forGetter((style6) -> Optional.ofNullable(style6.bold)), Codec.BOOL.optionalFieldOf("italic").forGetter((style5) -> Optional.ofNullable(style5.italic)), Codec.BOOL.optionalFieldOf("underlined").forGetter((style4) -> Optional.ofNullable(style4.underlined)), Codec.BOOL.optionalFieldOf("strikethrough").forGetter((style3) -> Optional.ofNullable(style3.strikethrough)), Codec.BOOL.optionalFieldOf("obfuscated").forGetter((style2) -> Optional.ofNullable(style2.obfuscated)), Codec.STRING.optionalFieldOf("insertion").forGetter((style1) -> Optional.ofNullable(style1.insertion)), ResourceLocation.CODEC.optionalFieldOf("font").forGetter((style) -> Optional.ofNullable(style.font))).apply(recordcodecbuilder_instance, Style::create));
   public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft", "default");
   @Nullable
   final TextColor color;
   @Nullable
   final Boolean bold;
   @Nullable
   final Boolean italic;
   @Nullable
   final Boolean underlined;
   @Nullable
   final Boolean strikethrough;
   @Nullable
   final Boolean obfuscated;
   @Nullable
   final ClickEvent clickEvent;
   @Nullable
   final HoverEvent hoverEvent;
   @Nullable
   final String insertion;
   @Nullable
   final ResourceLocation font;

   private static Style create(Optional<TextColor> optional, Optional<Boolean> optional1, Optional<Boolean> optional2, Optional<Boolean> optional3, Optional<Boolean> optional4, Optional<Boolean> optional5, Optional<String> optional6, Optional<ResourceLocation> optional7) {
      return new Style(optional.orElse((TextColor)null), optional1.orElse((Boolean)null), optional2.orElse((Boolean)null), optional3.orElse((Boolean)null), optional4.orElse((Boolean)null), optional5.orElse((Boolean)null), (ClickEvent)null, (HoverEvent)null, optional6.orElse((String)null), optional7.orElse((ResourceLocation)null));
   }

   Style(@Nullable TextColor textcolor, @Nullable Boolean obool, @Nullable Boolean obool1, @Nullable Boolean obool2, @Nullable Boolean obool3, @Nullable Boolean obool4, @Nullable ClickEvent clickevent, @Nullable HoverEvent hoverevent, @Nullable String s, @Nullable ResourceLocation resourcelocation) {
      this.color = textcolor;
      this.bold = obool;
      this.italic = obool1;
      this.underlined = obool2;
      this.strikethrough = obool3;
      this.obfuscated = obool4;
      this.clickEvent = clickevent;
      this.hoverEvent = hoverevent;
      this.insertion = s;
      this.font = resourcelocation;
   }

   @Nullable
   public TextColor getColor() {
      return this.color;
   }

   public boolean isBold() {
      return this.bold == Boolean.TRUE;
   }

   public boolean isItalic() {
      return this.italic == Boolean.TRUE;
   }

   public boolean isStrikethrough() {
      return this.strikethrough == Boolean.TRUE;
   }

   public boolean isUnderlined() {
      return this.underlined == Boolean.TRUE;
   }

   public boolean isObfuscated() {
      return this.obfuscated == Boolean.TRUE;
   }

   public boolean isEmpty() {
      return this == EMPTY;
   }

   @Nullable
   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }

   @Nullable
   public HoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   @Nullable
   public String getInsertion() {
      return this.insertion;
   }

   public ResourceLocation getFont() {
      return this.font != null ? this.font : DEFAULT_FONT;
   }

   public Style withColor(@Nullable TextColor textcolor) {
      return new Style(textcolor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withColor(@Nullable ChatFormatting chatformatting) {
      return this.withColor(chatformatting != null ? TextColor.fromLegacyFormat(chatformatting) : null);
   }

   public Style withColor(int i) {
      return this.withColor(TextColor.fromRgb(i));
   }

   public Style withBold(@Nullable Boolean obool) {
      return new Style(this.color, obool, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withItalic(@Nullable Boolean obool) {
      return new Style(this.color, this.bold, obool, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withUnderlined(@Nullable Boolean obool) {
      return new Style(this.color, this.bold, this.italic, obool, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withStrikethrough(@Nullable Boolean obool) {
      return new Style(this.color, this.bold, this.italic, this.underlined, obool, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withObfuscated(@Nullable Boolean obool) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obool, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withClickEvent(@Nullable ClickEvent clickevent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickevent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withHoverEvent(@Nullable HoverEvent hoverevent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverevent, this.insertion, this.font);
   }

   public Style withInsertion(@Nullable String s) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, s, this.font);
   }

   public Style withFont(@Nullable ResourceLocation resourcelocation) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, resourcelocation);
   }

   public Style applyFormat(ChatFormatting chatformatting) {
      TextColor textcolor = this.color;
      Boolean obool = this.bold;
      Boolean obool1 = this.italic;
      Boolean obool2 = this.strikethrough;
      Boolean obool3 = this.underlined;
      Boolean obool4 = this.obfuscated;
      switch (chatformatting) {
         case OBFUSCATED:
            obool4 = true;
            break;
         case BOLD:
            obool = true;
            break;
         case STRIKETHROUGH:
            obool2 = true;
            break;
         case UNDERLINE:
            obool3 = true;
            break;
         case ITALIC:
            obool1 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            textcolor = TextColor.fromLegacyFormat(chatformatting);
      }

      return new Style(textcolor, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyLegacyFormat(ChatFormatting chatformatting) {
      TextColor textcolor = this.color;
      Boolean obool = this.bold;
      Boolean obool1 = this.italic;
      Boolean obool2 = this.strikethrough;
      Boolean obool3 = this.underlined;
      Boolean obool4 = this.obfuscated;
      switch (chatformatting) {
         case OBFUSCATED:
            obool4 = true;
            break;
         case BOLD:
            obool = true;
            break;
         case STRIKETHROUGH:
            obool2 = true;
            break;
         case UNDERLINE:
            obool3 = true;
            break;
         case ITALIC:
            obool1 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            obool4 = false;
            obool = false;
            obool2 = false;
            obool3 = false;
            obool1 = false;
            textcolor = TextColor.fromLegacyFormat(chatformatting);
      }

      return new Style(textcolor, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyFormats(ChatFormatting... achatformatting) {
      TextColor textcolor = this.color;
      Boolean obool = this.bold;
      Boolean obool1 = this.italic;
      Boolean obool2 = this.strikethrough;
      Boolean obool3 = this.underlined;
      Boolean obool4 = this.obfuscated;

      for(ChatFormatting chatformatting : achatformatting) {
         switch (chatformatting) {
            case OBFUSCATED:
               obool4 = true;
               break;
            case BOLD:
               obool = true;
               break;
            case STRIKETHROUGH:
               obool2 = true;
               break;
            case UNDERLINE:
               obool3 = true;
               break;
            case ITALIC:
               obool1 = true;
               break;
            case RESET:
               return EMPTY;
            default:
               textcolor = TextColor.fromLegacyFormat(chatformatting);
         }
      }

      return new Style(textcolor, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyTo(Style style) {
      if (this == EMPTY) {
         return style;
      } else {
         return style == EMPTY ? this : new Style(this.color != null ? this.color : style.color, this.bold != null ? this.bold : style.bold, this.italic != null ? this.italic : style.italic, this.underlined != null ? this.underlined : style.underlined, this.strikethrough != null ? this.strikethrough : style.strikethrough, this.obfuscated != null ? this.obfuscated : style.obfuscated, this.clickEvent != null ? this.clickEvent : style.clickEvent, this.hoverEvent != null ? this.hoverEvent : style.hoverEvent, this.insertion != null ? this.insertion : style.insertion, this.font != null ? this.font : style.font);
      }
   }

   public String toString() {
      final StringBuilder stringbuilder = new StringBuilder("{");

      class Collector {
         private boolean isNotFirst;

         private void prependSeparator() {
            if (this.isNotFirst) {
               stringbuilder.append(',');
            }

            this.isNotFirst = true;
         }

         void addFlagString(String s, @Nullable Boolean obool) {
            if (obool != null) {
               this.prependSeparator();
               if (!obool) {
                  stringbuilder.append('!');
               }

               stringbuilder.append(s);
            }

         }

         void addValueString(String s, @Nullable Object object) {
            if (object != null) {
               this.prependSeparator();
               stringbuilder.append(s);
               stringbuilder.append('=');
               stringbuilder.append(object);
            }

         }
      }

      Collector style_1collector = new Collector();
      style_1collector.addValueString("color", this.color);
      style_1collector.addFlagString("bold", this.bold);
      style_1collector.addFlagString("italic", this.italic);
      style_1collector.addFlagString("underlined", this.underlined);
      style_1collector.addFlagString("strikethrough", this.strikethrough);
      style_1collector.addFlagString("obfuscated", this.obfuscated);
      style_1collector.addValueString("clickEvent", this.clickEvent);
      style_1collector.addValueString("hoverEvent", this.hoverEvent);
      style_1collector.addValueString("insertion", this.insertion);
      style_1collector.addValueString("font", this.font);
      stringbuilder.append("}");
      return stringbuilder.toString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Style)) {
         return false;
      } else {
         Style style = (Style)object;
         return this.isBold() == style.isBold() && Objects.equals(this.getColor(), style.getColor()) && this.isItalic() == style.isItalic() && this.isObfuscated() == style.isObfuscated() && this.isStrikethrough() == style.isStrikethrough() && this.isUnderlined() == style.isUnderlined() && Objects.equals(this.getClickEvent(), style.getClickEvent()) && Objects.equals(this.getHoverEvent(), style.getHoverEvent()) && Objects.equals(this.getInsertion(), style.getInsertion()) && Objects.equals(this.getFont(), style.getFont());
      }
   }

   public int hashCode() {
      return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
   }

   public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
      @Nullable
      public Style deserialize(JsonElement jsonelement, Type type, JsonDeserializationContext jsondeserializationcontext) throws JsonParseException {
         if (jsonelement.isJsonObject()) {
            JsonObject jsonobject = jsonelement.getAsJsonObject();
            if (jsonobject == null) {
               return null;
            } else {
               Boolean obool = getOptionalFlag(jsonobject, "bold");
               Boolean obool1 = getOptionalFlag(jsonobject, "italic");
               Boolean obool2 = getOptionalFlag(jsonobject, "underlined");
               Boolean obool3 = getOptionalFlag(jsonobject, "strikethrough");
               Boolean obool4 = getOptionalFlag(jsonobject, "obfuscated");
               TextColor textcolor = getTextColor(jsonobject);
               String s = getInsertion(jsonobject);
               ClickEvent clickevent = getClickEvent(jsonobject);
               HoverEvent hoverevent = getHoverEvent(jsonobject);
               ResourceLocation resourcelocation = getFont(jsonobject);
               return new Style(textcolor, obool, obool1, obool2, obool3, obool4, clickevent, hoverevent, s, resourcelocation);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static ResourceLocation getFont(JsonObject jsonobject) {
         if (jsonobject.has("font")) {
            String s = GsonHelper.getAsString(jsonobject, "font");

            try {
               return new ResourceLocation(s);
            } catch (ResourceLocationException var3) {
               throw new JsonSyntaxException("Invalid font name: " + s);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static HoverEvent getHoverEvent(JsonObject jsonobject) {
         if (jsonobject.has("hoverEvent")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "hoverEvent");
            HoverEvent hoverevent = HoverEvent.deserialize(jsonobject1);
            if (hoverevent != null && hoverevent.getAction().isAllowedFromServer()) {
               return hoverevent;
            }
         }

         return null;
      }

      @Nullable
      private static ClickEvent getClickEvent(JsonObject jsonobject) {
         if (jsonobject.has("clickEvent")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "clickEvent");
            String s = GsonHelper.getAsString(jsonobject1, "action", (String)null);
            ClickEvent.Action clickevent_action = s == null ? null : ClickEvent.Action.getByName(s);
            String s1 = GsonHelper.getAsString(jsonobject1, "value", (String)null);
            if (clickevent_action != null && s1 != null && clickevent_action.isAllowedFromServer()) {
               return new ClickEvent(clickevent_action, s1);
            }
         }

         return null;
      }

      @Nullable
      private static String getInsertion(JsonObject jsonobject) {
         return GsonHelper.getAsString(jsonobject, "insertion", (String)null);
      }

      @Nullable
      private static TextColor getTextColor(JsonObject jsonobject) {
         if (jsonobject.has("color")) {
            String s = GsonHelper.getAsString(jsonobject, "color");
            return TextColor.parseColor(s);
         } else {
            return null;
         }
      }

      @Nullable
      private static Boolean getOptionalFlag(JsonObject jsonobject, String s) {
         return jsonobject.has(s) ? jsonobject.get(s).getAsBoolean() : null;
      }

      @Nullable
      public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonserializationcontext) {
         if (style.isEmpty()) {
            return null;
         } else {
            JsonObject jsonobject = new JsonObject();
            if (style.bold != null) {
               jsonobject.addProperty("bold", style.bold);
            }

            if (style.italic != null) {
               jsonobject.addProperty("italic", style.italic);
            }

            if (style.underlined != null) {
               jsonobject.addProperty("underlined", style.underlined);
            }

            if (style.strikethrough != null) {
               jsonobject.addProperty("strikethrough", style.strikethrough);
            }

            if (style.obfuscated != null) {
               jsonobject.addProperty("obfuscated", style.obfuscated);
            }

            if (style.color != null) {
               jsonobject.addProperty("color", style.color.serialize());
            }

            if (style.insertion != null) {
               jsonobject.add("insertion", jsonserializationcontext.serialize(style.insertion));
            }

            if (style.clickEvent != null) {
               JsonObject jsonobject1 = new JsonObject();
               jsonobject1.addProperty("action", style.clickEvent.getAction().getName());
               jsonobject1.addProperty("value", style.clickEvent.getValue());
               jsonobject.add("clickEvent", jsonobject1);
            }

            if (style.hoverEvent != null) {
               jsonobject.add("hoverEvent", style.hoverEvent.serialize());
            }

            if (style.font != null) {
               jsonobject.addProperty("font", style.font.toString());
            }

            return jsonobject;
         }
      }
   }
}
