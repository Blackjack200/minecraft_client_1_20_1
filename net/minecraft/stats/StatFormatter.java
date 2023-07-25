package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.Util;

public interface StatFormatter {
   DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("########0.00"), (decimalformat) -> decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));
   StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
   StatFormatter DIVIDE_BY_TEN = (i) -> DECIMAL_FORMAT.format((double)i * 0.1D);
   StatFormatter DISTANCE = (i) -> {
      double d0 = (double)i / 100.0D;
      double d1 = d0 / 1000.0D;
      if (d1 > 0.5D) {
         return DECIMAL_FORMAT.format(d1) + " km";
      } else {
         return d0 > 0.5D ? DECIMAL_FORMAT.format(d0) + " m" : i + " cm";
      }
   };
   StatFormatter TIME = (i) -> {
      double d0 = (double)i / 20.0D;
      double d1 = d0 / 60.0D;
      double d2 = d1 / 60.0D;
      double d3 = d2 / 24.0D;
      double d4 = d3 / 365.0D;
      if (d4 > 0.5D) {
         return DECIMAL_FORMAT.format(d4) + " y";
      } else if (d3 > 0.5D) {
         return DECIMAL_FORMAT.format(d3) + " d";
      } else if (d2 > 0.5D) {
         return DECIMAL_FORMAT.format(d2) + " h";
      } else {
         return d1 > 0.5D ? DECIMAL_FORMAT.format(d1) + " m" : d0 + " s";
      }
   };

   String format(int i);
}
