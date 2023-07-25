package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ClientboundPlayerAbilitiesPacket implements Packet<ClientGamePacketListener> {
   private static final int FLAG_INVULNERABLE = 1;
   private static final int FLAG_FLYING = 2;
   private static final int FLAG_CAN_FLY = 4;
   private static final int FLAG_INSTABUILD = 8;
   private final boolean invulnerable;
   private final boolean isFlying;
   private final boolean canFly;
   private final boolean instabuild;
   private final float flyingSpeed;
   private final float walkingSpeed;

   public ClientboundPlayerAbilitiesPacket(Abilities abilities) {
      this.invulnerable = abilities.invulnerable;
      this.isFlying = abilities.flying;
      this.canFly = abilities.mayfly;
      this.instabuild = abilities.instabuild;
      this.flyingSpeed = abilities.getFlyingSpeed();
      this.walkingSpeed = abilities.getWalkingSpeed();
   }

   public ClientboundPlayerAbilitiesPacket(FriendlyByteBuf friendlybytebuf) {
      byte b0 = friendlybytebuf.readByte();
      this.invulnerable = (b0 & 1) != 0;
      this.isFlying = (b0 & 2) != 0;
      this.canFly = (b0 & 4) != 0;
      this.instabuild = (b0 & 8) != 0;
      this.flyingSpeed = friendlybytebuf.readFloat();
      this.walkingSpeed = friendlybytebuf.readFloat();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      byte b0 = 0;
      if (this.invulnerable) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isFlying) {
         b0 = (byte)(b0 | 2);
      }

      if (this.canFly) {
         b0 = (byte)(b0 | 4);
      }

      if (this.instabuild) {
         b0 = (byte)(b0 | 8);
      }

      friendlybytebuf.writeByte(b0);
      friendlybytebuf.writeFloat(this.flyingSpeed);
      friendlybytebuf.writeFloat(this.walkingSpeed);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handlePlayerAbilities(this);
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public boolean isFlying() {
      return this.isFlying;
   }

   public boolean canFly() {
      return this.canFly;
   }

   public boolean canInstabuild() {
      return this.instabuild;
   }

   public float getFlyingSpeed() {
      return this.flyingSpeed;
   }

   public float getWalkingSpeed() {
      return this.walkingSpeed;
   }
}
