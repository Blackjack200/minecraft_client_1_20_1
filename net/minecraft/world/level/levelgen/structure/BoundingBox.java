package net.minecraft.world.level.levelgen.structure;

import com.google.common.base.MoreObjects;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.slf4j.Logger;

public class BoundingBox {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Codec<BoundingBox> CODEC = Codec.INT_STREAM.comapFlatMap((intstream) -> Util.fixedSize(intstream, 6).map((aint) -> new BoundingBox(aint[0], aint[1], aint[2], aint[3], aint[4], aint[5])), (boundingbox) -> IntStream.of(boundingbox.minX, boundingbox.minY, boundingbox.minZ, boundingbox.maxX, boundingbox.maxY, boundingbox.maxZ)).stable();
   private int minX;
   private int minY;
   private int minZ;
   private int maxX;
   private int maxY;
   private int maxZ;

   public BoundingBox(BlockPos blockpos) {
      this(blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX(), blockpos.getY(), blockpos.getZ());
   }

   public BoundingBox(int i, int j, int k, int l, int i1, int j1) {
      this.minX = i;
      this.minY = j;
      this.minZ = k;
      this.maxX = l;
      this.maxY = i1;
      this.maxZ = j1;
      if (l < i || i1 < j || j1 < k) {
         String s = "Invalid bounding box data, inverted bounds for: " + this;
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw new IllegalStateException(s);
         }

         LOGGER.error(s);
         this.minX = Math.min(i, l);
         this.minY = Math.min(j, i1);
         this.minZ = Math.min(k, j1);
         this.maxX = Math.max(i, l);
         this.maxY = Math.max(j, i1);
         this.maxZ = Math.max(k, j1);
      }

   }

   public static BoundingBox fromCorners(Vec3i vec3i, Vec3i vec3i1) {
      return new BoundingBox(Math.min(vec3i.getX(), vec3i1.getX()), Math.min(vec3i.getY(), vec3i1.getY()), Math.min(vec3i.getZ(), vec3i1.getZ()), Math.max(vec3i.getX(), vec3i1.getX()), Math.max(vec3i.getY(), vec3i1.getY()), Math.max(vec3i.getZ(), vec3i1.getZ()));
   }

   public static BoundingBox infinite() {
      return new BoundingBox(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public static BoundingBox orientBox(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, Direction direction) {
      switch (direction) {
         case SOUTH:
         default:
            return new BoundingBox(i + l, j + i1, k + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + i2 - 1 + j1);
         case NORTH:
            return new BoundingBox(i + l, j + i1, k - i2 + 1 + j1, i + k1 - 1 + l, j + l1 - 1 + i1, k + j1);
         case WEST:
            return new BoundingBox(i - i2 + 1 + j1, j + i1, k + l, i + j1, j + l1 - 1 + i1, k + k1 - 1 + l);
         case EAST:
            return new BoundingBox(i + j1, j + i1, k + l, i + i2 - 1 + j1, j + l1 - 1 + i1, k + k1 - 1 + l);
      }
   }

   public boolean intersects(BoundingBox boundingbox) {
      return this.maxX >= boundingbox.minX && this.minX <= boundingbox.maxX && this.maxZ >= boundingbox.minZ && this.minZ <= boundingbox.maxZ && this.maxY >= boundingbox.minY && this.minY <= boundingbox.maxY;
   }

   public boolean intersects(int i, int j, int k, int l) {
      return this.maxX >= i && this.minX <= k && this.maxZ >= j && this.minZ <= l;
   }

   public static Optional<BoundingBox> encapsulatingPositions(Iterable<BlockPos> iterable) {
      Iterator<BlockPos> iterator = iterable.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BoundingBox boundingbox = new BoundingBox(iterator.next());
         iterator.forEachRemaining(boundingbox::encapsulate);
         return Optional.of(boundingbox);
      }
   }

   public static Optional<BoundingBox> encapsulatingBoxes(Iterable<BoundingBox> iterable) {
      Iterator<BoundingBox> iterator = iterable.iterator();
      if (!iterator.hasNext()) {
         return Optional.empty();
      } else {
         BoundingBox boundingbox = iterator.next();
         BoundingBox boundingbox1 = new BoundingBox(boundingbox.minX, boundingbox.minY, boundingbox.minZ, boundingbox.maxX, boundingbox.maxY, boundingbox.maxZ);
         iterator.forEachRemaining(boundingbox1::encapsulate);
         return Optional.of(boundingbox1);
      }
   }

   /** @deprecated */
   @Deprecated
   public BoundingBox encapsulate(BoundingBox boundingbox) {
      this.minX = Math.min(this.minX, boundingbox.minX);
      this.minY = Math.min(this.minY, boundingbox.minY);
      this.minZ = Math.min(this.minZ, boundingbox.minZ);
      this.maxX = Math.max(this.maxX, boundingbox.maxX);
      this.maxY = Math.max(this.maxY, boundingbox.maxY);
      this.maxZ = Math.max(this.maxZ, boundingbox.maxZ);
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BoundingBox encapsulate(BlockPos blockpos) {
      this.minX = Math.min(this.minX, blockpos.getX());
      this.minY = Math.min(this.minY, blockpos.getY());
      this.minZ = Math.min(this.minZ, blockpos.getZ());
      this.maxX = Math.max(this.maxX, blockpos.getX());
      this.maxY = Math.max(this.maxY, blockpos.getY());
      this.maxZ = Math.max(this.maxZ, blockpos.getZ());
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BoundingBox move(int i, int j, int k) {
      this.minX += i;
      this.minY += j;
      this.minZ += k;
      this.maxX += i;
      this.maxY += j;
      this.maxZ += k;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BoundingBox move(Vec3i vec3i) {
      return this.move(vec3i.getX(), vec3i.getY(), vec3i.getZ());
   }

   public BoundingBox moved(int i, int j, int k) {
      return new BoundingBox(this.minX + i, this.minY + j, this.minZ + k, this.maxX + i, this.maxY + j, this.maxZ + k);
   }

   public BoundingBox inflatedBy(int i) {
      return new BoundingBox(this.minX() - i, this.minY() - i, this.minZ() - i, this.maxX() + i, this.maxY() + i, this.maxZ() + i);
   }

   public boolean isInside(Vec3i vec3i) {
      return this.isInside(vec3i.getX(), vec3i.getY(), vec3i.getZ());
   }

   public boolean isInside(int i, int j, int k) {
      return i >= this.minX && i <= this.maxX && k >= this.minZ && k <= this.maxZ && j >= this.minY && j <= this.maxY;
   }

   public Vec3i getLength() {
      return new Vec3i(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
   }

   public int getXSpan() {
      return this.maxX - this.minX + 1;
   }

   public int getYSpan() {
      return this.maxY - this.minY + 1;
   }

   public int getZSpan() {
      return this.maxZ - this.minZ + 1;
   }

   public BlockPos getCenter() {
      return new BlockPos(this.minX + (this.maxX - this.minX + 1) / 2, this.minY + (this.maxY - this.minY + 1) / 2, this.minZ + (this.maxZ - this.minZ + 1) / 2);
   }

   public void forAllCorners(Consumer<BlockPos> consumer) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      consumer.accept(blockpos_mutableblockpos.set(this.maxX, this.maxY, this.maxZ));
      consumer.accept(blockpos_mutableblockpos.set(this.minX, this.maxY, this.maxZ));
      consumer.accept(blockpos_mutableblockpos.set(this.maxX, this.minY, this.maxZ));
      consumer.accept(blockpos_mutableblockpos.set(this.minX, this.minY, this.maxZ));
      consumer.accept(blockpos_mutableblockpos.set(this.maxX, this.maxY, this.minZ));
      consumer.accept(blockpos_mutableblockpos.set(this.minX, this.maxY, this.minZ));
      consumer.accept(blockpos_mutableblockpos.set(this.maxX, this.minY, this.minZ));
      consumer.accept(blockpos_mutableblockpos.set(this.minX, this.minY, this.minZ));
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("minX", this.minX).add("minY", this.minY).add("minZ", this.minZ).add("maxX", this.maxX).add("maxY", this.maxY).add("maxZ", this.maxZ).toString();
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof BoundingBox)) {
         return false;
      } else {
         BoundingBox boundingbox = (BoundingBox)object;
         return this.minX == boundingbox.minX && this.minY == boundingbox.minY && this.minZ == boundingbox.minZ && this.maxX == boundingbox.maxX && this.maxY == boundingbox.maxY && this.maxZ == boundingbox.maxZ;
      }
   }

   public int hashCode() {
      return Objects.hash(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
   }

   public int minX() {
      return this.minX;
   }

   public int minY() {
      return this.minY;
   }

   public int minZ() {
      return this.minZ;
   }

   public int maxX() {
      return this.maxX;
   }

   public int maxY() {
      return this.maxY;
   }

   public int maxZ() {
      return this.maxZ;
   }
}
