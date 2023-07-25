package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
   private final GameProfile gameProfile;

   public ClientboundGameProfilePacket(GameProfile gameprofile) {
      this.gameProfile = gameprofile;
   }

   public ClientboundGameProfilePacket(FriendlyByteBuf friendlybytebuf) {
      this.gameProfile = friendlybytebuf.readGameProfile();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeGameProfile(this.gameProfile);
   }

   public void handle(ClientLoginPacketListener clientloginpacketlistener) {
      clientloginpacketlistener.handleGameProfile(this);
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }
}
