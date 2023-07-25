package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

public class ClientboundStopSoundPacket implements Packet<ClientGamePacketListener> {
   private static final int HAS_SOURCE = 1;
   private static final int HAS_SOUND = 2;
   @Nullable
   private final ResourceLocation name;
   @Nullable
   private final SoundSource source;

   public ClientboundStopSoundPacket(@Nullable ResourceLocation resourcelocation, @Nullable SoundSource soundsource) {
      this.name = resourcelocation;
      this.source = soundsource;
   }

   public ClientboundStopSoundPacket(FriendlyByteBuf friendlybytebuf) {
      int i = friendlybytebuf.readByte();
      if ((i & 1) > 0) {
         this.source = friendlybytebuf.readEnum(SoundSource.class);
      } else {
         this.source = null;
      }

      if ((i & 2) > 0) {
         this.name = friendlybytebuf.readResourceLocation();
      } else {
         this.name = null;
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      if (this.source != null) {
         if (this.name != null) {
            friendlybytebuf.writeByte(3);
            friendlybytebuf.writeEnum(this.source);
            friendlybytebuf.writeResourceLocation(this.name);
         } else {
            friendlybytebuf.writeByte(1);
            friendlybytebuf.writeEnum(this.source);
         }
      } else if (this.name != null) {
         friendlybytebuf.writeByte(2);
         friendlybytebuf.writeResourceLocation(this.name);
      } else {
         friendlybytebuf.writeByte(0);
      }

   }

   @Nullable
   public ResourceLocation getName() {
      return this.name;
   }

   @Nullable
   public SoundSource getSource() {
      return this.source;
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleStopSoundEvent(this);
   }
}
