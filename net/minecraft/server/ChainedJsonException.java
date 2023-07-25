package net.minecraft.server;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class ChainedJsonException extends IOException {
   private final List<ChainedJsonException.Entry> entries = Lists.newArrayList();
   private final String message;

   public ChainedJsonException(String s) {
      this.entries.add(new ChainedJsonException.Entry());
      this.message = s;
   }

   public ChainedJsonException(String s, Throwable throwable) {
      super(throwable);
      this.entries.add(new ChainedJsonException.Entry());
      this.message = s;
   }

   public void prependJsonKey(String s) {
      this.entries.get(0).addJsonKey(s);
   }

   public void setFilenameAndFlush(String s) {
      (this.entries.get(0)).filename = s;
      this.entries.add(0, new ChainedJsonException.Entry());
   }

   public String getMessage() {
      return "Invalid " + this.entries.get(this.entries.size() - 1) + ": " + this.message;
   }

   public static ChainedJsonException forException(Exception exception) {
      if (exception instanceof ChainedJsonException) {
         return (ChainedJsonException)exception;
      } else {
         String s = exception.getMessage();
         if (exception instanceof FileNotFoundException) {
            s = "File not found";
         }

         return new ChainedJsonException(s, exception);
      }
   }

   public static class Entry {
      @Nullable
      String filename;
      private final List<String> jsonKeys = Lists.newArrayList();

      Entry() {
      }

      void addJsonKey(String s) {
         this.jsonKeys.add(0, s);
      }

      @Nullable
      public String getFilename() {
         return this.filename;
      }

      public String getJsonKeys() {
         return StringUtils.join(this.jsonKeys, "->");
      }

      public String toString() {
         if (this.filename != null) {
            return this.jsonKeys.isEmpty() ? this.filename : this.filename + " " + this.getJsonKeys();
         } else {
            return this.jsonKeys.isEmpty() ? "(Unknown file)" : "(Unknown file) " + this.getJsonKeys();
         }
      }
   }
}
