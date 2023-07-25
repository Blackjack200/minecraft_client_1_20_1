package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ClientboundDamageEventPacket(int entityId, int sourceTypeId, int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition) implements Packet<ClientGamePacketListener> {
   public ClientboundDamageEventPacket(Entity entity, DamageSource damagesource) {
      this(entity.getId(), entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getId(damagesource.type()), damagesource.getEntity() != null ? damagesource.getEntity().getId() : -1, damagesource.getDirectEntity() != null ? damagesource.getDirectEntity().getId() : -1, Optional.ofNullable(damagesource.sourcePositionRaw()));
   }

   public ClientboundDamageEventPacket(FriendlyByteBuf friendlybytebuf) {
      this(friendlybytebuf.readVarInt(), friendlybytebuf.readVarInt(), readOptionalEntityId(friendlybytebuf), readOptionalEntityId(friendlybytebuf), friendlybytebuf.readOptional((friendlybytebuf1) -> new Vec3(friendlybytebuf1.readDouble(), friendlybytebuf1.readDouble(), friendlybytebuf1.readDouble())));
   }

   private static void writeOptionalEntityId(FriendlyByteBuf friendlybytebuf, int i) {
      friendlybytebuf.writeVarInt(i + 1);
   }

   private static int readOptionalEntityId(FriendlyByteBuf friendlybytebuf) {
      return friendlybytebuf.readVarInt() - 1;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.entityId);
      friendlybytebuf.writeVarInt(this.sourceTypeId);
      writeOptionalEntityId(friendlybytebuf, this.sourceCauseId);
      writeOptionalEntityId(friendlybytebuf, this.sourceDirectId);
      friendlybytebuf.writeOptional(this.sourcePosition, (friendlybytebuf1, vec3) -> {
         friendlybytebuf1.writeDouble(vec3.x());
         friendlybytebuf1.writeDouble(vec3.y());
         friendlybytebuf1.writeDouble(vec3.z());
      });
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleDamageEvent(this);
   }

   public DamageSource getSource(Level level) {
      Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(this.sourceTypeId).get();
      if (this.sourcePosition.isPresent()) {
         return new DamageSource(holder, this.sourcePosition.get());
      } else {
         Entity entity = level.getEntity(this.sourceCauseId);
         Entity entity1 = level.getEntity(this.sourceDirectId);
         return new DamageSource(holder, entity1, entity);
      }
   }
}
