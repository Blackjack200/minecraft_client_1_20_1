package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ClientboundExplodePacket implements Packet<ClientGamePacketListener> {
   private final double x;
   private final double y;
   private final double z;
   private final float power;
   private final List<BlockPos> toBlow;
   private final float knockbackX;
   private final float knockbackY;
   private final float knockbackZ;

   public ClientboundExplodePacket(double d0, double d1, double d2, float f, List<BlockPos> list, @Nullable Vec3 vec3) {
      this.x = d0;
      this.y = d1;
      this.z = d2;
      this.power = f;
      this.toBlow = Lists.newArrayList(list);
      if (vec3 != null) {
         this.knockbackX = (float)vec3.x;
         this.knockbackY = (float)vec3.y;
         this.knockbackZ = (float)vec3.z;
      } else {
         this.knockbackX = 0.0F;
         this.knockbackY = 0.0F;
         this.knockbackZ = 0.0F;
      }

   }

   public ClientboundExplodePacket(FriendlyByteBuf friendlybytebuf) {
      this.x = friendlybytebuf.readDouble();
      this.y = friendlybytebuf.readDouble();
      this.z = friendlybytebuf.readDouble();
      this.power = friendlybytebuf.readFloat();
      int i = Mth.floor(this.x);
      int j = Mth.floor(this.y);
      int k = Mth.floor(this.z);
      this.toBlow = friendlybytebuf.readList((friendlybytebuf1) -> {
         int k1 = friendlybytebuf1.readByte() + i;
         int l1 = friendlybytebuf1.readByte() + j;
         int i2 = friendlybytebuf1.readByte() + k;
         return new BlockPos(k1, l1, i2);
      });
      this.knockbackX = friendlybytebuf.readFloat();
      this.knockbackY = friendlybytebuf.readFloat();
      this.knockbackZ = friendlybytebuf.readFloat();
   }

   public void write(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeDouble(this.x);
      friendlybytebuf.writeDouble(this.y);
      friendlybytebuf.writeDouble(this.z);
      friendlybytebuf.writeFloat(this.power);
      int i = Mth.floor(this.x);
      int j = Mth.floor(this.y);
      int k = Mth.floor(this.z);
      friendlybytebuf.writeCollection(this.toBlow, (friendlybytebuf1, blockpos) -> {
         int k1 = blockpos.getX() - i;
         int l1 = blockpos.getY() - j;
         int i2 = blockpos.getZ() - k;
         friendlybytebuf1.writeByte(k1);
         friendlybytebuf1.writeByte(l1);
         friendlybytebuf1.writeByte(i2);
      });
      friendlybytebuf.writeFloat(this.knockbackX);
      friendlybytebuf.writeFloat(this.knockbackY);
      friendlybytebuf.writeFloat(this.knockbackZ);
   }

   public void handle(ClientGamePacketListener clientgamepacketlistener) {
      clientgamepacketlistener.handleExplosion(this);
   }

   public float getKnockbackX() {
      return this.knockbackX;
   }

   public float getKnockbackY() {
      return this.knockbackY;
   }

   public float getKnockbackZ() {
      return this.knockbackZ;
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

   public float getPower() {
      return this.power;
   }

   public List<BlockPos> getToBlow() {
      return this.toBlow;
   }
}
