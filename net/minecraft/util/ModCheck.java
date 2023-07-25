package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModCheck(ModCheck.Confidence confidence, String description) {
   public static ModCheck identify(String s, Supplier<String> supplier, String s1, Class<?> oclass) {
      String s2 = supplier.get();
      if (!s.equals(s2)) {
         return new ModCheck(ModCheck.Confidence.DEFINITELY, s1 + " brand changed to '" + s2 + "'");
      } else {
         return oclass.getSigners() == null ? new ModCheck(ModCheck.Confidence.VERY_LIKELY, s1 + " jar signature invalidated") : new ModCheck(ModCheck.Confidence.PROBABLY_NOT, s1 + " jar signature and brand is untouched");
      }
   }

   public boolean shouldReportAsModified() {
      return this.confidence.shouldReportAsModified;
   }

   public ModCheck merge(ModCheck modcheck) {
      return new ModCheck(ObjectUtils.max(this.confidence, modcheck.confidence), this.description + "; " + modcheck.description);
   }

   public String fullDescription() {
      return this.confidence.description + " " + this.description;
   }

   public static enum Confidence {
      PROBABLY_NOT("Probably not.", false),
      VERY_LIKELY("Very likely;", true),
      DEFINITELY("Definitely;", true);

      final String description;
      final boolean shouldReportAsModified;

      private Confidence(String s, boolean flag) {
         this.description = s;
         this.shouldReportAsModified = flag;
      }
   }
}
