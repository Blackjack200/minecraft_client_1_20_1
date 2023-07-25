package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
   private final byte[] keybytes;
   private final byte[] encryptedChallenge;

   public ServerboundKeyPacket(SecretKey secretkey, PublicKey publickey, byte[] abyte) throws CryptException {
      this.keybytes = Crypt.encryptUsingKey(publickey, secretkey.getEncoded());
      this.encryptedChallenge = Crypt.encryptUsingKey(publickey, abyte);
   }

   public ServerboundKeyPacket(FriendlyByteBuf friendlybytebuf) {
      this.keybytes = friendlybytebuf.readByteArray();
      this.encryptedChallenge = friendlybytebuf.readByteArray();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeByteArray(this.keybytes);
      friendlybytebuf.writeByteArray(this.encryptedChallenge);
   }

   public void handle(ServerLoginPacketListener serverloginpacketlistener) {
      serverloginpacketlistener.handleKey(this);
   }

   public SecretKey getSecretKey(PrivateKey privatekey) throws CryptException {
      return Crypt.decryptByteToSecretKey(privatekey, this.keybytes);
   }

   public boolean isChallengeValid(byte[] abyte, PrivateKey privatekey) {
      try {
         return Arrays.equals(abyte, Crypt.decryptUsingKey(privatekey, this.encryptedChallenge));
      } catch (CryptException var4) {
         return false;
      }
   }
}
