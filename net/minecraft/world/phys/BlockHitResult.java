package net.minecraft.world.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult {
   private final Direction direction;
   private final BlockPos blockPos;
   private final boolean miss;
   private final boolean inside;

   public static BlockHitResult miss(Vec3 vec3, Direction direction, BlockPos blockpos) {
      return new BlockHitResult(true, vec3, direction, blockpos, false);
   }

   public BlockHitResult(Vec3 vec3, Direction direction, BlockPos blockpos, boolean flag) {
      this(false, vec3, direction, blockpos, flag);
   }

   private BlockHitResult(boolean flag, Vec3 vec3, Direction direction, BlockPos blockpos, boolean flag1) {
      super(vec3);
      this.miss = flag;
      this.direction = direction;
      this.blockPos = blockpos;
      this.inside = flag1;
   }

   public BlockHitResult withDirection(Direction direction) {
      return new BlockHitResult(this.miss, this.location, direction, this.blockPos, this.inside);
   }

   public BlockHitResult withPosition(BlockPos blockpos) {
      return new BlockHitResult(this.miss, this.location, this.direction, blockpos, this.inside);
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public HitResult.Type getType() {
      return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
   }

   public boolean isInside() {
      return this.inside;
   }
}
