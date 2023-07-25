package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
   public static final int MAX_HASH_LENGTH = 40;
   private final String url;
   private final String hash;
   private final boolean required;
   @Nullable
   private final Component prompt;

   public ClientboundResourcePackPacket(String s, String s1, boolean flag, @Nullable Component component) {
      if (s1.length() > 40) {
         throw new IllegalArgumentException("Hash is too long (max 40, was " + s1.length() + ")");
      } else {
         this.url = s;
         this.hash = s1;
         this.required = flag;
         this.prompt = component;
      }
   }

   public ClientboundResourcePackPacket(FriendlyByteBuf friendlybytebuf) {
      this.url = friendlybytebuf.readUtf();
      this.hash = friendlybytebuf.readUtf(40);
      this.required = friendlybytebuf.readBoolean();
      this.prompt = friendlybytebuf.readNullable(FriendlyByteBuf::readComponent);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.url);
      friendlybytebuf.writeUtf(this.hash);
      friendlybytebuf.writeBoolean(this.required);
      friendlybytebuf.writeNullable(this.prompt, FriendlyByteBuf::writeComponent);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleResourcePack(this);
   }

   public String getUrl() {
      return this.url;
   }

   public String getHash() {
      return this.hash;
   }

   public boolean isRequired() {
      return this.required;
   }

   @Nullable
   public Component getPrompt() {
      return this.prompt;
   }
}
