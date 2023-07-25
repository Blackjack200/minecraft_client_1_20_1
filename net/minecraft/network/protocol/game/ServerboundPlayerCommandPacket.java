package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ServerboundPlayerCommandPacket implements Packet<ServerGamePacketListener> {
   private final int id;
   private final ServerboundPlayerCommandPacket.Action action;
   private final int data;

   public ServerboundPlayerCommandPacket(Entity entity, ServerboundPlayerCommandPacket.Action serverboundplayercommandpacket_action) {
      this(entity, serverboundplayercommandpacket_action, 0);
   }

   public ServerboundPlayerCommandPacket(Entity entity, ServerboundPlayerCommandPacket.Action serverboundplayercommandpacket_action, int i) {
      this.id = entity.getId();
      this.action = serverboundplayercommandpacket_action;
      this.data = i;
   }

   public ServerboundPlayerCommandPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      this.action = friendlybytebuf.readEnum(ServerboundPlayerCommandPacket.Action.class);
      this.data = friendlybytebuf.readVarInt();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeEnum(this.action);
      friendlybytebuf.writeVarInt(this.data);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handlePlayerCommand(this);
   }

   public int getId() {
      return this.id;
   }

   public ServerboundPlayerCommandPacket.Action getAction() {
      return this.action;
   }

   public int getData() {
      return this.data;
   }

   public static enum Action {
      PRESS_SHIFT_KEY,
      RELEASE_SHIFT_KEY,
      STOP_SLEEPING,
      START_SPRINTING,
      STOP_SPRINTING,
      START_RIDING_JUMP,
      STOP_RIDING_JUMP,
      OPEN_INVENTORY,
      START_FALL_FLYING;
   }
}
