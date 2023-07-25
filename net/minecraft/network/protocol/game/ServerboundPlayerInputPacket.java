package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerInputPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_JUMPING = 1;
   private static final int FLAG_SHIFT_KEY_DOWN = 2;
   private final float xxa;
   private final float zza;
   private final boolean isJumping;
   private final boolean isShiftKeyDown;

   public ServerboundPlayerInputPacket(float f, float f1, boolean flag, boolean flag1) {
      this.xxa = f;
      this.zza = f1;
      this.isJumping = flag;
      this.isShiftKeyDown = flag1;
   }

   public ServerboundPlayerInputPacket(FriendlyByteBuf friendlybytebuf) {
      this.xxa = friendlybytebuf.readFloat();
      this.zza = friendlybytebuf.readFloat();
      byte b0 = friendlybytebuf.readByte();
      this.isJumping = (b0 & 1) > 0;
      this.isShiftKeyDown = (b0 & 2) > 0;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeFloat(this.xxa);
      friendlybytebuf.writeFloat(this.zza);
      byte b0 = 0;
      if (this.isJumping) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isShiftKeyDown) {
         b0 = (byte)(b0 | 2);
      }

      friendlybytebuf.writeByte(b0);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePlayerInput(this);
   }

   public float getXxa() {
      return this.xxa;
   }

   public float getZza() {
      return this.zza;
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public boolean isShiftKeyDown() {
      return this.isShiftKeyDown;
   }
}
