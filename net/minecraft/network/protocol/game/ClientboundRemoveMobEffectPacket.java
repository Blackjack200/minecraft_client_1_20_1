package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRemoveMobEffectPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final MobEffect effect;

   public ClientboundRemoveMobEffectPacket(int i, MobEffect mobeffect) {
      this.entityId = i;
      this.effect = mobeffect;
   }

   public ClientboundRemoveMobEffectPacket(FriendlyByteBuf friendlybytebuf) {
      this.entityId = friendlybytebuf.readVarInt();
      this.effect = friendlybytebuf.readById(BuiltInRegistries.MOB_EFFECT);
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.entityId);
      friendlybytebuf.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleRemoveMobEffect(this);
   }

   @Nullable
   public Entity getEntity(Level level) {
      return level.getEntity(this.entityId);
   }

   @Nullable
   public MobEffect getEffect() {
      return this.effect;
   }
}
