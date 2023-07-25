package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;

public final class TextColor {
   private static final String CUSTOM_COLOR_PREFIX = "#";
   public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap((s) -> {
      TextColor textcolor = parseColor(s);
      return textcolor != null ? DataResult.success(textcolor) : DataResult.error(() -> "String is not a valid color name or hex color code");
   }, TextColor::serialize);
   private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = Stream.of(ChatFormatting.values()).filter(ChatFormatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), (chatformatting) -> new TextColor(chatformatting.getColor(), chatformatting.getName())));
   private static final Map<String, TextColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap((textcolor) -> textcolor.name, Function.identity()));
   private final int value;
   @Nullable
   private final String name;

   private TextColor(int i, String s) {
      this.value = i;
      this.name = s;
   }

   private TextColor(int i) {
      this.value = i;
      this.name = null;
   }

   public int getValue() {
      return this.value;
   }

   public String serialize() {
      return this.name != null ? this.name : this.formatValue();
   }

   private String formatValue() {
      return String.format(Locale.ROOT, "#%06X", this.value);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         TextColor textcolor = (TextColor)object;
         return this.value == textcolor.value;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.value, this.name);
   }

   public String toString() {
      return this.name != null ? this.name : this.formatValue();
   }

   @Nullable
   public static TextColor fromLegacyFormat(ChatFormatting chatformatting) {
      return LEGACY_FORMAT_TO_COLOR.get(chatformatting);
   }

   public static TextColor fromRgb(int i) {
      return new TextColor(i);
   }

   @Nullable
   public static TextColor parseColor(String s) {
      if (s.startsWith("#")) {
         try {
            int i = Integer.parseInt(s.substring(1), 16);
            return fromRgb(i);
         } catch (NumberFormatException var2) {
            return null;
         }
      } else {
         return NAMED_COLORS.get(s);
      }
   }
}
