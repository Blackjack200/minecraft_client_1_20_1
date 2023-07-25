package com.mojang.realmsclient.exception;

public class RealmsHttpException extends RuntimeException {
   public RealmsHttpException(String s, Exception exception) {
      super(s, exception);
   }
}
