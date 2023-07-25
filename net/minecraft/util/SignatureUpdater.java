package net.minecraft.util;

import java.security.SignatureException;

@FunctionalInterface
public interface SignatureUpdater {
   void update(SignatureUpdater.Output signatureupdater_output) throws SignatureException;

   @FunctionalInterface
   public interface Output {
      void update(byte[] abyte) throws SignatureException;
   }
}
