package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import org.slf4j.Logger;

public interface Signer {
   Logger LOGGER = LogUtils.getLogger();

   byte[] sign(SignatureUpdater signatureupdater);

   default byte[] sign(byte[] abyte) {
      return this.sign((SignatureUpdater)((signatureupdater_output) -> signatureupdater_output.update(abyte)));
   }

   static Signer from(PrivateKey privatekey, String s) {
      return (signatureupdater) -> {
         try {
            Signature signature = Signature.getInstance(s);
            signature.initSign(privatekey);
            signatureupdater.update(signature::update);
            return signature.sign();
         } catch (Exception var4) {
            throw new IllegalStateException("Failed to sign message", var4);
         }
      };
   }
}
