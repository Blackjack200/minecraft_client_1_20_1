package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.StringRepresentable;

public enum ChatFormatting implements StringRepresentable {
   BLACK("BLACK", '0', 0, 0),
   DARK_BLUE("DARK_BLUE", '1', 1, 170),
   DARK_GREEN("DARK_GREEN", '2', 2, 43520),
   DARK_AQUA("DARK_AQUA", '3', 3, 43690),
   DARK_RED("DARK_RED", '4', 4, 11141120),
   DARK_PURPLE("DARK_PURPLE", '5', 5, 11141290),
   GOLD("GOLD", '6', 6, 16755200),
   GRAY("GRAY", '7', 7, 11184810),
   DARK_GRAY("DARK_GRAY", '8', 8, 5592405),
   BLUE("BLUE", '9', 9, 5592575),
   GREEN("GREEN", 'a', 10, 5635925),
   AQUA("AQUA", 'b', 11, 5636095),
   RED("RED", 'c', 12, 16733525),
   LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 16733695),
   YELLOW("YELLOW", 'e', 14, 16777045),
   WHITE("WHITE", 'f', 15, 16777215),
   OBFUSCATED("OBFUSCATED", 'k', true),
   BOLD("BOLD", 'l', true),
   STRIKETHROUGH("STRIKETHROUGH", 'm', true),
   UNDERLINE("UNDERLINE", 'n', true),
   ITALIC("ITALIC", 'o', true),
   RESET("RESET", 'r', -1, (Integer)null);

   public static final Codec<ChatFormatting> CODEC = StringRepresentable.fromEnum(ChatFormatting::values);
   public static final char PREFIX_CODE = '\u00a7';
   private static final Map<String, ChatFormatting> FORMATTING_BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((chatformatting) -> cleanName(chatformatting.name), (chatformatting) -> chatformatting));
   private static final Pattern STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
   private final String name;
   private final char code;
   private final boolean isFormat;
   private final String toString;
   private final int id;
   @Nullable
   private final Integer color;

   private static String cleanName(String s) {
      return s.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
   }

   private ChatFormatting(String s, char c0, int i, @Nullable Integer integer) {
      this(s, c0, false, i, integer);
   }

   private ChatFormatting(String s, char c0, boolean flag) {
      this(s, c0, flag, -1, (Integer)null);
   }

   private ChatFormatting(String s, char c0, boolean flag, int i, @Nullable Integer integer) {
      this.name = s;
      this.code = c0;
      this.isFormat = flag;
      this.id = i;
      this.color = integer;
      this.toString = "\u00a7" + c0;
   }

   public char getChar() {
      return this.code;
   }

   public int getId() {
      return this.id;
   }

   public boolean isFormat() {
      return this.isFormat;
   }

   public boolean isColor() {
      return !this.isFormat && this != RESET;
   }

   @Nullable
   public Integer getColor() {
      return this.color;
   }

   public String getName() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public String toString() {
      return this.toString;
   }

   @Nullable
   public static String stripFormatting(@Nullable String s) {
      return s == null ? null : STRIP_FORMATTING_PATTERN.matcher(s).replaceAll("");
   }

   @Nullable
   public static ChatFormatting getByName(@Nullable String s) {
      return s == null ? null : FORMATTING_BY_NAME.get(cleanName(s));
   }

   @Nullable
   public static ChatFormatting getById(int i) {
      if (i < 0) {
         return RESET;
      } else {
         for(ChatFormatting chatformatting : values()) {
            if (chatformatting.getId() == i) {
               return chatformatting;
            }
         }

         return null;
      }
   }

   @Nullable
   public static ChatFormatting getByCode(char c0) {
      char c1 = Character.toString(c0).toLowerCase(Locale.ROOT).charAt(0);

      for(ChatFormatting chatformatting : values()) {
         if (chatformatting.code == c1) {
            return chatformatting;
         }
      }

      return null;
   }

   public static Collection<String> getNames(boolean flag, boolean flag1) {
      List<String> list = Lists.newArrayList();

      for(ChatFormatting chatformatting : values()) {
         if ((!chatformatting.isColor() || flag) && (!chatformatting.isFormat() || flag1)) {
            list.add(chatformatting.getName());
         }
      }

      return list;
   }

   public String getSerializedName() {
      return this.getName();
   }
}
