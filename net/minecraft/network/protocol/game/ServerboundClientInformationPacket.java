package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public record ServerboundClientInformationPacket(String language, int viewDistance, ChatVisiblity chatVisibility, boolean chatColors, int modelCustomisation, HumanoidArm mainHand, boolean textFilteringEnabled, boolean allowsListing) implements Packet<ServerGamePacketListener> {
   public static final int MAX_LANGUAGE_LENGTH = 16;

   public ServerboundClientInformationPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readUtf(16), friendlybytebuf.readByte(), friendlybytebuf.readEnum(ChatVisiblity.class), friendlybytebuf.readBoolean(), friendlybytebuf.readUnsignedByte(), friendlybytebuf.readEnum(HumanoidArm.class), friendlybytebuf.readBoolean(), friendlybytebuf.readBoolean());
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeUtf(this.language);
      friendlybytebuf.writeByte(this.viewDistance);
      friendlybytebuf.writeEnum(this.chatVisibility);
      friendlybytebuf.writeBoolean(this.chatColors);
      friendlybytebuf.writeByte(this.modelCustomisation);
      friendlybytebuf.writeEnum(this.mainHand);
      friendlybytebuf.writeBoolean(this.textFilteringEnabled);
      friendlybytebuf.writeBoolean(this.allowsListing);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleClientInformation(this);
   }
}
