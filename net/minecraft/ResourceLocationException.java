package net.minecraft;

import org.apache.commons.lang3.StringEscapeUtils;

public class ResourceLocationException extends RuntimeException {
   public ResourceLocationException(String s) {
      super(StringEscapeUtils.escapeJava(s));
   }

   public ResourceLocationException(String s, Throwable throwable) {
      super(StringEscapeUtils.escapeJava(s), throwable);
   }
}
