package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatPacket(UUID sender, int index, @Nullable MessageSignature signature, SignedMessageBody.Packed body, @Nullable Component unsignedContent, FilterMask filterMask, ChatType.BoundNetwork chatType) implements Packet<ClientGamePacketListener> {
   public ClientboundPlayerChatPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readUUID(), friendlybytebuf.readVarInt(), friendlybytebuf.readNullable(MessageSignature::read), new SignedMessageBody.Packed(friendlybytebuf), friendlybytebuf.readNullable(FriendlyByteBuf::readComponent), FilterMask.read(friendlybytebuf), new ChatType.BoundNetwork(friendlybytebuf));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUUID(this.sender);
      friendlybytebuf.writeVarInt(this.index);
      friendlybytebuf.writeNullable(this.signature, MessageSignature::write);
      this.body.write(friendlybytebuf);
      friendlybytebuf.writeNullable(this.unsignedContent, FriendlyByteBuf::writeComponent);
      FilterMask.write(friendlybytebuf, this.filterMask);
      this.chatType.write(friendlybytebuf);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePlayerChat(this);
   }

   public boolean isSkippable() {
      return true;
   }
}
