package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.Packet;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages.Update lastSeenMessages) implements Packet<ServerGamePacketListener> {
   public ServerboundChatCommandPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readUtf(256), friendlybytebuf.readInstant(), friendlybytebuf.readLong(), new ArgumentSignatures(friendlybytebuf), new LastSeenMessages.Update(friendlybytebuf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.command, 256);
      friendlybytebuf.writeInstant(this.timeStamp);
      friendlybytebuf.writeLong(this.salt);
      this.argumentSignatures.write(friendlybytebuf);
      this.lastSeenMessages.write(friendlybytebuf);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleChatCommand(this);
   }
}
