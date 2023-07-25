package net.minecraft.network.protocol.game;

import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatPacket(String message, Instant timeStamp, long salt, @Nullable MessageSignature signature, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readUtf(256), friendlybytebuf.readInstant(), friendlybytebuf.readLong(), friendlybytebuf.readNullable(MessageSignature::read), new LastSeenMessages.Update(friendlybytebuf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.message, 256);
      friendlybytebuf.writeInstant(this.timeStamp);
      friendlybytebuf.writeLong(this.salt);
      friendlybytebuf.writeNullable(this.signature, MessageSignature::write);
      this.lastSeenMessages.write(friendlybytebuf);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleChat(this);
   }
}
