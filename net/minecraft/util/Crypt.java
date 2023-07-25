package net.minecraft.util;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.FriendlyByteBuf;

public class Crypt {
   private static final String SYMMETRIC_ALGORITHM = "AES";
   private static final int SYMMETRIC_BITS = 128;
   private static final String ASYMMETRIC_ALGORITHM = "RSA";
   private static final int ASYMMETRIC_BITS = 1024;
   private static final String BYTE_ENCODING = "ISO_8859_1";
   private static final String HASH_ALGORITHM = "SHA-1";
   public static final String SIGNING_ALGORITHM = "SHA256withRSA";
   public static final int SIGNATURE_BYTES = 256;
   private static final String PEM_RSA_PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
   private static final String PEM_RSA_PRIVATE_KEY_FOOTER = "-----END RSA PRIVATE KEY-----";
   public static final String RSA_PUBLIC_KEY_HEADER = "-----BEGIN RSA PUBLIC KEY-----";
   private static final String RSA_PUBLIC_KEY_FOOTER = "-----END RSA PUBLIC KEY-----";
   public static final String MIME_LINE_SEPARATOR = "\n";
   public static final Base64.Encoder MIME_ENCODER = Base64.getMimeEncoder(76, "\n".getBytes(StandardCharsets.UTF_8));
   public static final Codec<PublicKey> PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap((s) -> {
      try {
         return DataResult.success(stringToRsaPublicKey(s));
      } catch (CryptException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, Crypt::rsaPublicKeyToString);
   public static final Codec<PrivateKey> PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap((s) -> {
      try {
         return DataResult.success(stringToPemRsaPrivateKey(s));
      } catch (CryptException var2) {
         return DataResult.error(var2::getMessage);
      }
   }, Crypt::pemRsaPrivateKeyToString);

   public static SecretKey generateSecretKey() throws CryptException {
      try {
         KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
         keygenerator.init(128);
         return keygenerator.generateKey();
      } catch (Exception var1) {
         throw new CryptException(var1);
      }
   }

   public static KeyPair generateKeyPair() throws CryptException {
      try {
         KeyPairGenerator keypairgenerator = KeyPairGenerator.getInstance("RSA");
         keypairgenerator.initialize(1024);
         return keypairgenerator.generateKeyPair();
      } catch (Exception var1) {
         throw new CryptException(var1);
      }
   }

   public static byte[] digestData(String s, PublicKey publickey, SecretKey secretkey) throws CryptException {
      try {
         return digestData(s.getBytes("ISO_8859_1"), secretkey.getEncoded(), publickey.getEncoded());
      } catch (Exception var4) {
         throw new CryptException(var4);
      }
   }

   private static byte[] digestData(byte[]... abyte) throws Exception {
      MessageDigest messagedigest = MessageDigest.getInstance("SHA-1");

      for(byte[] abyte1 : abyte) {
         messagedigest.update(abyte1);
      }

      return messagedigest.digest();
   }

   private static <T extends Key> T rsaStringToKey(String s, String s1, String s2, Crypt.ByteArrayToKeyFunction<T> crypt_bytearraytokeyfunction) throws CryptException {
      int i = s.indexOf(s1);
      if (i != -1) {
         i += s1.length();
         int j = s.indexOf(s2, i);
         s = s.substring(i, j + 1);
      }

      try {
         return crypt_bytearraytokeyfunction.apply(Base64.getMimeDecoder().decode(s));
      } catch (IllegalArgumentException var6) {
         throw new CryptException(var6);
      }
   }

   public static PrivateKey stringToPemRsaPrivateKey(String s) throws CryptException {
      return rsaStringToKey(s, "-----BEGIN RSA PRIVATE KEY-----", "-----END RSA PRIVATE KEY-----", Crypt::byteToPrivateKey);
   }

   public static PublicKey stringToRsaPublicKey(String s) throws CryptException {
      return rsaStringToKey(s, "-----BEGIN RSA PUBLIC KEY-----", "-----END RSA PUBLIC KEY-----", Crypt::byteToPublicKey);
   }

   public static String rsaPublicKeyToString(PublicKey publickey) {
      if (!"RSA".equals(publickey.getAlgorithm())) {
         throw new IllegalArgumentException("Public key must be RSA");
      } else {
         return "-----BEGIN RSA PUBLIC KEY-----\n" + MIME_ENCODER.encodeToString(publickey.getEncoded()) + "\n-----END RSA PUBLIC KEY-----\n";
      }
   }

   public static String pemRsaPrivateKeyToString(PrivateKey privatekey) {
      if (!"RSA".equals(privatekey.getAlgorithm())) {
         throw new IllegalArgumentException("Private key must be RSA");
      } else {
         return "-----BEGIN RSA PRIVATE KEY-----\n" + MIME_ENCODER.encodeToString(privatekey.getEncoded()) + "\n-----END RSA PRIVATE KEY-----\n";
      }
   }

   private static PrivateKey byteToPrivateKey(byte[] abyte) throws CryptException {
      try {
         EncodedKeySpec encodedkeyspec = new PKCS8EncodedKeySpec(abyte);
         KeyFactory keyfactory = KeyFactory.getInstance("RSA");
         return keyfactory.generatePrivate(encodedkeyspec);
      } catch (Exception var3) {
         throw new CryptException(var3);
      }
   }

   public static PublicKey byteToPublicKey(byte[] abyte) throws CryptException {
      try {
         EncodedKeySpec encodedkeyspec = new X509EncodedKeySpec(abyte);
         KeyFactory keyfactory = KeyFactory.getInstance("RSA");
         return keyfactory.generatePublic(encodedkeyspec);
      } catch (Exception var3) {
         throw new CryptException(var3);
      }
   }

   public static SecretKey decryptByteToSecretKey(PrivateKey privatekey, byte[] abyte) throws CryptException {
      byte[] abyte1 = decryptUsingKey(privatekey, abyte);

      try {
         return new SecretKeySpec(abyte1, "AES");
      } catch (Exception var4) {
         throw new CryptException(var4);
      }
   }

   public static byte[] encryptUsingKey(Key key, byte[] abyte) throws CryptException {
      return cipherData(1, key, abyte);
   }

   public static byte[] decryptUsingKey(Key key, byte[] abyte) throws CryptException {
      return cipherData(2, key, abyte);
   }

   private static byte[] cipherData(int i, Key key, byte[] abyte) throws CryptException {
      try {
         return setupCipher(i, key.getAlgorithm(), key).doFinal(abyte);
      } catch (Exception var4) {
         throw new CryptException(var4);
      }
   }

   private static Cipher setupCipher(int i, String s, Key key) throws Exception {
      Cipher cipher = Cipher.getInstance(s);
      cipher.init(i, key);
      return cipher;
   }

   public static Cipher getCipher(int i, Key key) throws CryptException {
      try {
         Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
         cipher.init(i, key, new IvParameterSpec(key.getEncoded()));
         return cipher;
      } catch (Exception var3) {
         throw new CryptException(var3);
      }
   }

   interface ByteArrayToKeyFunction<T extends Key> {
      T apply(byte[] abyte) throws CryptException;
   }

   public static record SaltSignaturePair(long salt, byte[] signature) {
      public static final Crypt.SaltSignaturePair EMPTY = new Crypt.SaltSignaturePair(0L, ByteArrays.EMPTY_ARRAY);

      public SaltSignaturePair(FriendlyByteBuf friendlybytebuf) {
         this(friendlybytebuf.readLong(), friendlybytebuf.readByteArray());
      }

      public boolean isValid() {
         return this.signature.length > 0;
      }

      public static void write(FriendlyByteBuf friendlybytebuf, Crypt.SaltSignaturePair crypt_saltsignaturepair) {
         friendlybytebuf.writeLong(crypt_saltsignaturepair.salt);
         friendlybytebuf.writeByteArray(crypt_saltsignaturepair.signature);
      }

      public byte[] saltAsBytes() {
         return Longs.toByteArray(this.salt);
      }
   }

   public static class SaltSupplier {
      private static final SecureRandom secureRandom = new SecureRandom();

      public static long getLong() {
         return secureRandom.nextLong();
      }
   }
}
