package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
   private final Optional<MobEffect> primary;
   private final Optional<MobEffect> secondary;

   public ServerboundSetBeaconPacket(Optional<MobEffect> optional, Optional<MobEffect> optional1) {
      this.primary = optional;
      this.secondary = optional1;
   }

   public ServerboundSetBeaconPacket(FriendlyByteBuf friendlybytebuf) {
      this.primary = friendlybytebuf.readOptional((friendlybytebuf2) -> friendlybytebuf2.readById(BuiltInRegistries.MOB_EFFECT));
      this.secondary = friendlybytebuf.readOptional((friendlybytebuf1) -> friendlybytebuf1.readById(BuiltInRegistries.MOB_EFFECT));
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeOptional(this.primary, (friendlybytebuf2, mobeffect1) -> friendlybytebuf2.writeId(BuiltInRegistries.MOB_EFFECT, mobeffect1));
      friendlybytebuf.writeOptional(this.secondary, (friendlybytebuf1, mobeffect) -> friendlybytebuf1.writeId(BuiltInRegistries.MOB_EFFECT, mobeffect));
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSetBeaconPacket(this);
   }

   public Optional<MobEffect> getPrimary() {
      return this.primary;
   }

   public Optional<MobEffect> getSecondary() {
      return this.secondary;
   }
}
