package net.minecraft.client.resources.language;

import java.util.IllegalFormatException;
import net.minecraft.locale.Language;

public class I18n {
   private static volatile Language language = Language.getInstance();

   private I18n() {
   }

   static void setLanguage(Language language) {
      I18n.language = language;
   }

   public static String get(String s, Object... aobject) {
      String s1 = language.getOrDefault(s);

      try {
         return String.format(s1, aobject);
      } catch (IllegalFormatException var4) {
         return "Format error: " + s1;
      }
   }

   public static boolean exists(String s) {
      return language.has(s);
   }
}
