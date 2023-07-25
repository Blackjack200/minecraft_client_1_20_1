package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public class ClientboundAddEntityPacket implements Packet<ClientGamePacketListener> {
   private static final double MAGICAL_QUANTIZATION = 8000.0D;
   private static final double LIMIT = 3.9D;
   private final int id;
   private final UUID uuid;
   private final EntityType<?> type;
   private final double x;
   private final double y;
   private final double z;
   private final int xa;
   private final int ya;
   private final int za;
   private final byte xRot;
   private final byte yRot;
   private final byte yHeadRot;
   private final int data;

   public ClientboundAddEntityPacket(Entity entity) {
      this(entity, 0);
   }

   public ClientboundAddEntityPacket(Entity entity, int i) {
      this(entity.getId(), entity.getUUID(), entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot(), entity.getType(), i, entity.getDeltaMovement(), (double)entity.getYHeadRot());
   }

   public ClientboundAddEntityPacket(Entity entity, int i, BlockPos blockpos) {
      this(entity.getId(), entity.getUUID(), (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), entity.getXRot(), entity.getYRot(), entity.getType(), i, entity.getDeltaMovement(), (double)entity.getYHeadRot());
   }

   public ClientboundAddEntityPacket(int i, UUID uuid, double d0, double d1, double d2, float f, float f1, EntityType<?> entitytype, int j, Vec3 vec3, double d3) {
      this.id = i;
      this.uuid = uuid;
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.xRot = (byte)Mth.floor(f * 256.0F / 360.0F);
      this.yRot = (byte)Mth.floor(f1 * 256.0F / 360.0F);
      this.yHeadRot = (byte)Mth.floor(d3 * 256.0D / 360.0D);
      this.type = entitytype;
      this.data = j;
      this.xa = (int)(Mth.clamp(vec3.x, -3.9D, 3.9D) * 8000.0D);
      this.ya = (int)(Mth.clamp(vec3.y, -3.9D, 3.9D) * 8000.0D);
      this.za = (int)(Mth.clamp(vec3.z, -3.9D, 3.9D) * 8000.0D);
   }

   public ClientboundAddEntityPacket(FriendlyByteBuf friendlybytebuf) {
      this.id = friendlybytebuf.readVarInt();
      this.uuid = friendlybytebuf.readUUID();
      this.type = friendlybytebuf.readById(BuiltInRegistries.ENTITY_TYPE);
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.xRot = friendlybytebuf.readByte();
      this.yRot = friendlybytebuf.readByte();
      this.yHeadRot = friendlybytebuf.readByte();
      this.data = friendlybytebuf.readVarInt();
      this.xa = friendlybytebuf.readShort();
      this.ya = friendlybytebuf.readShort();
      this.za = friendlybytebuf.readShort();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeVarInt(this.id);
      friendlybytebuf.writeUUID(this.uuid);
      friendlybytebuf.writeId(BuiltInRegistries.ENTITY_TYPE, this.type);
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeByte(this.xRot);
      friendlybytebuf.writeByte(this.yRot);
      friendlybytebuf.writeByte(this.yHeadRot);
      friendlybytebuf.writeVarInt(this.data);
      friendlybytebuf.writeShort(this.xa);
      friendlybytebuf.writeShort(this.ya);
      friendlybytebuf.writeShort(this.za);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleAddEntity(this);
   }

   public int getId() {
      return this.id;
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public EntityType<?> getType() {
      return this.type;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public double getXa() {
      return (double)this.xa / 8000.0D;
   }

   public double getYa() {
      return (double)this.ya / 8000.0D;
   }

   public double getZa() {
      return (double)this.za / 8000.0D;
   }

   public float getXRot() {
      return (float)(this.xRot * 360) / 256.0F;
   }

   public float getYRot() {
      return (float)(this.yRot * 360) / 256.0F;
   }

   public float getYHeadRot() {
      return (float)(this.yHeadRot * 360) / 256.0F;
   }

   public int getData() {
      return this.data;
   }
}
