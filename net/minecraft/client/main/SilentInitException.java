package net.minecraft.client.main;

public class SilentInitException extends RuntimeException {
   public SilentInitException(String s) {
      super(s);
   }

   public SilentInitException(String s, Throwable throwable) {
      super(s, throwable);
   }
}
