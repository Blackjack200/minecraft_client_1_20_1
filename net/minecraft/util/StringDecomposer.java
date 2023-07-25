package net.minecraft.util;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class StringDecomposer {
   private static final char REPLACEMENT_CHAR = '\ufffd';
   private static final Optional<Object> STOP_ITERATION = Optional.of(Unit.INSTANCE);

   private static boolean feedChar(Style style, FormattedCharSink formattedcharsink, int i, char c0) {
      return Character.isSurrogate(c0) ? formattedcharsink.accept(i, style, 65533) : formattedcharsink.accept(i, style, c0);
   }

   public static boolean iterate(String s, Style style, FormattedCharSink formattedcharsink) {
      int i = s.length();

      for(int j = 0; j < i; ++j) {
         char c0 = s.charAt(j);
         if (Character.isHighSurrogate(c0)) {
            if (j + 1 >= i) {
               if (!formattedcharsink.accept(j, style, 65533)) {
                  return false;
               }
               break;
            }

            char c1 = s.charAt(j + 1);
            if (Character.isLowSurrogate(c1)) {
               if (!formattedcharsink.accept(j, style, Character.toCodePoint(c0, c1))) {
                  return false;
               }

               ++j;
            } else if (!formattedcharsink.accept(j, style, 65533)) {
               return false;
            }
         } else if (!feedChar(style, formattedcharsink, j, c0)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateBackwards(String s, Style style, FormattedCharSink formattedcharsink) {
      int i = s.length();

      for(int j = i - 1; j >= 0; --j) {
         char c0 = s.charAt(j);
         if (Character.isLowSurrogate(c0)) {
            if (j - 1 < 0) {
               if (!formattedcharsink.accept(0, style, 65533)) {
                  return false;
               }
               break;
            }

            char c1 = s.charAt(j - 1);
            if (Character.isHighSurrogate(c1)) {
               --j;
               if (!formattedcharsink.accept(j, style, Character.toCodePoint(c1, c0))) {
                  return false;
               }
            } else if (!formattedcharsink.accept(j, style, 65533)) {
               return false;
            }
         } else if (!feedChar(style, formattedcharsink, j, c0)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateFormatted(String s, Style style, FormattedCharSink formattedcharsink) {
      return iterateFormatted(s, 0, style, formattedcharsink);
   }

   public static boolean iterateFormatted(String s, int i, Style style, FormattedCharSink formattedcharsink) {
      return iterateFormatted(s, i, style, style, formattedcharsink);
   }

   public static boolean iterateFormatted(String s, int i, Style style, Style style1, FormattedCharSink formattedcharsink) {
      int j = s.length();
      Style style2 = style;

      for(int k = i; k < j; ++k) {
         char c0 = s.charAt(k);
         if (c0 == 167) {
            if (k + 1 >= j) {
               break;
            }

            char c1 = s.charAt(k + 1);
            ChatFormatting chatformatting = ChatFormatting.getByCode(c1);
            if (chatformatting != null) {
               style2 = chatformatting == ChatFormatting.RESET ? style1 : style2.applyLegacyFormat(chatformatting);
            }

            ++k;
         } else if (Character.isHighSurrogate(c0)) {
            if (k + 1 >= j) {
               if (!formattedcharsink.accept(k, style2, 65533)) {
                  return false;
               }
               break;
            }

            char c2 = s.charAt(k + 1);
            if (Character.isLowSurrogate(c2)) {
               if (!formattedcharsink.accept(k, style2, Character.toCodePoint(c0, c2))) {
                  return false;
               }

               ++k;
            } else if (!formattedcharsink.accept(k, style2, 65533)) {
               return false;
            }
         } else if (!feedChar(style2, formattedcharsink, k, c0)) {
            return false;
         }
      }

      return true;
   }

   public static boolean iterateFormatted(FormattedText formattedtext, Style style, FormattedCharSink formattedcharsink) {
      return !formattedtext.visit((style1, s) -> iterateFormatted(s, 0, style1, formattedcharsink) ? Optional.empty() : STOP_ITERATION, style).isPresent();
   }

   public static String filterBrokenSurrogates(String s) {
      StringBuilder stringbuilder = new StringBuilder();
      iterate(s, Style.EMPTY, (i, style, j) -> {
         stringbuilder.appendCodePoint(j);
         return true;
      });
      return stringbuilder.toString();
   }

   public static String getPlainText(FormattedText formattedtext) {
      StringBuilder stringbuilder = new StringBuilder();
      iterateFormatted(formattedtext, Style.EMPTY, (i, style, j) -> {
         stringbuilder.appendCodePoint(j);
         return true;
      });
      return stringbuilder.toString();
   }
}
