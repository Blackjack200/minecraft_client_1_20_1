package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
   private final String serverId;
   private final byte[] publicKey;
   private final byte[] challenge;

   public ClientboundHelloPacket(String s, byte[] abyte, byte[] abyte1) {
      this.serverId = s;
      this.publicKey = abyte;
      this.challenge = abyte1;
   }

   public ClientboundHelloPacket(FriendlyByteBuf friendlybytebuf) {
      this.serverId = friendlybytebuf.readUtf(20);
      this.publicKey = friendlybytebuf.readByteArray();
      this.challenge = friendlybytebuf.readByteArray();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.serverId);
      friendlybytebuf.writeByteArray(this.publicKey);
      friendlybytebuf.writeByteArray(this.challenge);
   }

   public void handle(ClientLoginPacketListener clientloginpacketlistener) {
      clientloginpacketlistener.handleHello(this);
   }

   public String getServerId() {
      return this.serverId;
   }

   public PublicKey getPublicKey() throws CryptException {
      return Crypt.byteToPublicKey(this.publicKey);
   }

   public byte[] getChallenge() {
      return this.challenge;
   }
}
