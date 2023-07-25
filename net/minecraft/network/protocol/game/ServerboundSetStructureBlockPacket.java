package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class ServerboundSetStructureBlockPacket implements Packet<ServerGamePacketListener> {
   private static final int FLAG_IGNORE_ENTITIES = 1;
   private static final int FLAG_SHOW_AIR = 2;
   private static final int FLAG_SHOW_BOUNDING_BOX = 4;
   private final BlockPos pos;
   private final StructureBlockEntity.UpdateType updateType;
   private final StructureMode mode;
   private final String name;
   private final BlockPos offset;
   private final Vec3i size;
   private final Mirror mirror;
   private final Rotation rotation;
   private final String data;
   private final boolean ignoreEntities;
   private final boolean showAir;
   private final boolean showBoundingBox;
   private final float integrity;
   private final long seed;

   public ServerboundSetStructureBlockPacket(BlockPos blockpos, StructureBlockEntity.UpdateType structureblockentity_updatetype, StructureMode structuremode, String s, BlockPos blockpos1, Vec3i vec3i, Mirror mirror, Rotation rotation, String s1, boolean flag, boolean flag1, boolean flag2, float f, long i) {
      this.pos = blockpos;
      this.updateType = structureblockentity_updatetype;
      this.mode = structuremode;
      this.name = s;
      this.offset = blockpos1;
      this.size = vec3i;
      this.mirror = mirror;
      this.rotation = rotation;
      this.data = s1;
      this.ignoreEntities = flag;
      this.showAir = flag1;
      this.showBoundingBox = flag2;
      this.integrity = f;
      this.seed = i;
   }

   public ServerboundSetStructureBlockPacket(FriendlyByteBuf friendlybytebuf) {
      this.pos = friendlybytebuf.readBlockPos();
      this.updateType = friendlybytebuf.readEnum(StructureBlockEntity.UpdateType.class);
      this.mode = friendlybytebuf.readEnum(StructureMode.class);
      this.name = friendlybytebuf.readUtf();
      int i = 48;
      this.offset = new BlockPos(Mth.clamp(friendlybytebuf.readByte(), -48, 48), Mth.clamp(friendlybytebuf.readByte(), -48, 48), Mth.clamp(friendlybytebuf.readByte(), -48, 48));
      int j = 48;
      this.size = new Vec3i(Mth.clamp(friendlybytebuf.readByte(), 0, 48), Mth.clamp(friendlybytebuf.readByte(), 0, 48), Mth.clamp(friendlybytebuf.readByte(), 0, 48));
      this.mirror = friendlybytebuf.readEnum(Mirror.class);
      this.rotation = friendlybytebuf.readEnum(Rotation.class);
      this.data = friendlybytebuf.readUtf(128);
      this.integrity = Mth.clamp(friendlybytebuf.readFloat(), 0.0F, 1.0F);
      this.seed = friendlybytebuf.readVarLong();
      int k = friendlybytebuf.readByte();
      this.ignoreEntities = (k & 1) != 0;
      this.showAir = (k & 2) != 0;
      this.showBoundingBox = (k & 4) != 0;
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeBlockPos(this.pos);
      friendlybytebuf.writeEnum(this.updateType);
      friendlybytebuf.writeEnum(this.mode);
      friendlybytebuf.writeUtf(this.name);
      friendlybytebuf.writeByte(this.offset.getX());
      friendlybytebuf.writeByte(this.offset.getY());
      friendlybytebuf.writeByte(this.offset.getZ());
      friendlybytebuf.writeByte(this.size.getX());
      friendlybytebuf.writeByte(this.size.getY());
      friendlybytebuf.writeByte(this.size.getZ());
      friendlybytebuf.writeEnum(this.mirror);
      friendlybytebuf.writeEnum(this.rotation);
      friendlybytebuf.writeUtf(this.data);
      friendlybytebuf.writeFloat(this.integrity);
      friendlybytebuf.writeVarLong(this.seed);
      int i = 0;
      if (this.ignoreEntities) {
         i |= 1;
      }

      if (this.showAir) {
         i |= 2;
      }

      if (this.showBoundingBox) {
         i |= 4;
      }

      friendlybytebuf.writeByte(i);
   }

   public void handle(ServerGamePacketListener servergamepacketlistener) {
      servergamepacketlistener.handleSetStructureBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public StructureBlockEntity.UpdateType getUpdateType() {
      return this.updateType;
   }

   public StructureMode getMode() {
      return this.mode;
   }

   public String getName() {
      return this.name;
   }

   public BlockPos getOffset() {
      return this.offset;
   }

   public Vec3i getSize() {
      return this.size;
   }

   public Mirror getMirror() {
      return this.mirror;
   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public String getData() {
      return this.data;
   }

   public boolean isIgnoreEntities() {
      return this.ignoreEntities;
   }

   public boolean isShowAir() {
      return this.showAir;
   }

   public boolean isShowBoundingBox() {
      return this.showBoundingBox;
   }

   public float getIntegrity() {
      return this.integrity;
   }

   public long getSeed() {
      return this.seed;
   }
}
