package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientboundPlayerLookAtPacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final int entity;
   private final EntityAnchorArgument.Anchor fromAnchor;
   private final EntityAnchorArgument.Anchor toAnchor;
   private final boolean atEntity;

   public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor entityanchorargument_anchor, double d0, double d1, double d2) {
      this.fromAnchor = entityanchorargument_anchor;
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.entity = 0;
      this.atEntity = false;
      this.toAnchor = null;
   }

   public ClientboundPlayerLookAtPacket(EntityAnchorArgument.Anchor entityanchorargument_anchor, Entity entity, EntityAnchorArgument.Anchor entityanchorargument_anchor1) {
      this.fromAnchor = entityanchorargument_anchor;
      this.entity = entity.getId();
      this.toAnchor = entityanchorargument_anchor1;
      Vec3 vec3 = entityanchorargument_anchor1.apply(entity);
      this.x = vec3.x;
      this.y = vec3.y;
      this.z = vec3.z;
      this.atEntity = true;
   }

   public ClientboundPlayerLookAtPacket(FriendlyByteBuf friendlybytebuf) {
      this.fromAnchor = friendlybytebuf.readEnum(EntityAnchorArgument.Anchor.class);
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.atEntity = friendlybytebuf.readBoolean();
      if (this.atEntity) {
         this.entity = friendlybytebuf.readVarInt();
         this.toAnchor = friendlybytebuf.readEnum(EntityAnchorArgument.Anchor.class);
      } else {
         this.entity = 0;
         this.toAnchor = null;
      }

   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeEnum(this.fromAnchor);
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeBoolean(this.atEntity);
      if (this.atEntity) {
         friendlybytebuf.writeVarInt(this.entity);
         friendlybytebuf.writeEnum(this.toAnchor);
      }

   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleLookAt(this);
   }

   public EntityAnchorArgument.Anchor getFromAnchor() {
      return this.fromAnchor;
   }

   @Nullable
   public Vec3 getPosition(Level level) {
      if (this.atEntity) {
         Entity entity = level.getEntity(this.entity);
         return entity == null ? new Vec3(this.x, this.y, this.z) : this.toAnchor.apply(entity);
      } else {
         return new Vec3(this.x, this.y, this.z);
      }
   }
}
