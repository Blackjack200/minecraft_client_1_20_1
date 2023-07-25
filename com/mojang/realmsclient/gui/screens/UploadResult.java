package com.mojang.realmsclient.gui.screens;

import javax.annotation.Nullable;

public class UploadResult {
   public final int statusCode;
   @Nullable
   public final String errorMessage;

   UploadResult(int i, String s) {
      this.statusCode = i;
      this.errorMessage = s;
   }

   public static class Builder {
      private int statusCode = -1;
      private String errorMessage;

      public UploadResult.Builder withStatusCode(int i) {
         this.statusCode = i;
         return this;
      }

      public UploadResult.Builder withErrorMessage(@Nullable String s) {
         this.errorMessage = s;
         return this;
      }

      public UploadResult build() {
         return new UploadResult(this.statusCode, this.errorMessage);
      }
   }
}
