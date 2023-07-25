package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Node {
   public final int x;
   public final int y;
   public final int z;
   private final int hash;
   public int heapIdx = -1;
   public float g;
   public float h;
   public float f;
   @Nullable
   public Node cameFrom;
   public boolean closed;
   public float walkedDistance;
   public float costMalus;
   public BlockPathTypes type = BlockPathTypes.BLOCKED;

   public Node(int i, int j, int k) {
      this.x = i;
      this.y = j;
      this.z = k;
      this.hash = createHash(i, j, k);
   }

   public Node cloneAndMove(int i, int j, int k) {
      Node node = new Node(i, j, k);
      node.heapIdx = this.heapIdx;
      node.g = this.g;
      node.h = this.h;
      node.f = this.f;
      node.cameFrom = this.cameFrom;
      node.closed = this.closed;
      node.walkedDistance = this.walkedDistance;
      node.costMalus = this.costMalus;
      node.type = this.type;
      return node;
   }

   public static int createHash(int i, int j, int k) {
      return j & 255 | (i & 32767) << 8 | (k & 32767) << 24 | (i < 0 ? Integer.MIN_VALUE : 0) | (k < 0 ? '\u8000' : 0);
   }

   public float distanceTo(Node node) {
      float f = (float)(node.x - this.x);
      float f1 = (float)(node.y - this.y);
      float f2 = (float)(node.z - this.z);
      return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public float distanceToXZ(Node node) {
      float f = (float)(node.x - this.x);
      float f1 = (float)(node.z - this.z);
      return Mth.sqrt(f * f + f1 * f1);
   }

   public float distanceTo(BlockPos blockpos) {
      float f = (float)(blockpos.getX() - this.x);
      float f1 = (float)(blockpos.getY() - this.y);
      float f2 = (float)(blockpos.getZ() - this.z);
      return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
   }

   public float distanceToSqr(Node node) {
      float f = (float)(node.x - this.x);
      float f1 = (float)(node.y - this.y);
      float f2 = (float)(node.z - this.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   public float distanceToSqr(BlockPos blockpos) {
      float f = (float)(blockpos.getX() - this.x);
      float f1 = (float)(blockpos.getY() - this.y);
      float f2 = (float)(blockpos.getZ() - this.z);
      return f * f + f1 * f1 + f2 * f2;
   }

   public float distanceManhattan(Node node) {
      float f = (float)Math.abs(node.x - this.x);
      float f1 = (float)Math.abs(node.y - this.y);
      float f2 = (float)Math.abs(node.z - this.z);
      return f + f1 + f2;
   }

   public float distanceManhattan(BlockPos blockpos) {
      float f = (float)Math.abs(blockpos.getX() - this.x);
      float f1 = (float)Math.abs(blockpos.getY() - this.y);
      float f2 = (float)Math.abs(blockpos.getZ() - this.z);
      return f + f1 + f2;
   }

   public BlockPos asBlockPos() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public Vec3 asVec3() {
      return new Vec3((double)this.x, (double)this.y, (double)this.z);
   }

   public boolean equals(Object object) {
      if (!(object instanceof Node node)) {
         return false;
      } else {
         return this.hash == node.hash && this.x == node.x && this.y == node.y && this.z == node.z;
      }
   }

   public int hashCode() {
      return this.hash;
   }

   public boolean inOpenSet() {
      return this.heapIdx >= 0;
   }

   public String toString() {
      return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }

   public void writeToStream(FriendlyByteBuf friendlybytebuf) {
      friendlybytebuf.writeInt(this.x);
      friendlybytebuf.writeInt(this.y);
      friendlybytebuf.writeInt(this.z);
      friendlybytebuf.writeFloat(this.walkedDistance);
      friendlybytebuf.writeFloat(this.costMalus);
      friendlybytebuf.writeBoolean(this.closed);
      friendlybytebuf.writeEnum(this.type);
      friendlybytebuf.writeFloat(this.f);
   }

   public static Node createFromStream(FriendlyByteBuf friendlybytebuf) {
      Node node = new Node(friendlybytebuf.readInt(), friendlybytebuf.readInt(), friendlybytebuf.readInt());
      readContents(friendlybytebuf, node);
      return node;
   }

   protected static void readContents(FriendlyByteBuf friendlybytebuf, Node node) {
      node.walkedDistance = friendlybytebuf.readFloat();
      node.costMalus = friendlybytebuf.readFloat();
      node.closed = friendlybytebuf.readBoolean();
      node.type = friendlybytebuf.readEnum(BlockPathTypes.class);
      node.f = friendlybytebuf.readFloat();
   }
}
