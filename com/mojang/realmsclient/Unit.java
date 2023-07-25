package com.mojang.realmsclient;

import java.util.Locale;

public enum Unit {
   B,
   KB,
   MB,
   GB;

   private static final int BASE_UNIT = 1024;

   public static Unit getLargest(long i) {
      if (i < 1024L) {
         return B;
      } else {
         try {
            int j = (int)(Math.log((double)i) / Math.log(1024.0D));
            String s = String.valueOf("KMGTPE".charAt(j - 1));
            return valueOf(s + "B");
         } catch (Exception var4) {
            return GB;
         }
      }
   }

   public static double convertTo(long i, Unit unit) {
      return unit == B ? (double)i : (double)i / Math.pow(1024.0D, (double)unit.ordinal());
   }

   public static String humanReadable(long i) {
      int j = 1024;
      if (i < 1024L) {
         return i + " B";
      } else {
         int k = (int)(Math.log((double)i) / Math.log(1024.0D));
         String s = "" + "KMGTPE".charAt(k - 1);
         return String.format(Locale.ROOT, "%.1f %sB", (double)i / Math.pow(1024.0D, (double)k), s);
      }
   }

   public static String humanReadable(long i, Unit unit) {
      return String.format(Locale.ROOT, "%." + (unit == GB ? "1" : "0") + "f %s", convertTo(i, unit), unit.name());
   }
}
