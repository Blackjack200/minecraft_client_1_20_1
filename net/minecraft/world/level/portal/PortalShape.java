package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalShape {
   private static final int MIN_WIDTH = 2;
   public static final int MAX_WIDTH = 21;
   private static final int MIN_HEIGHT = 3;
   public static final int MAX_HEIGHT = 21;
   private static final BlockBehaviour.StatePredicate FRAME = (blockstate, blockgetter, blockpos) -> blockstate.is(Blocks.OBSIDIAN);
   private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
   private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0D;
   private final LevelAccessor level;
   private final Direction.Axis axis;
   private final Direction rightDir;
   private int numPortalBlocks;
   @Nullable
   private BlockPos bottomLeft;
   private int height;
   private final int width;

   public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor levelaccessor, BlockPos blockpos, Direction.Axis direction_axis) {
      return findPortalShape(levelaccessor, blockpos, (portalshape) -> portalshape.isValid() && portalshape.numPortalBlocks == 0, direction_axis);
   }

   public static Optional<PortalShape> findPortalShape(LevelAccessor levelaccessor, BlockPos blockpos, Predicate<PortalShape> predicate, Direction.Axis direction_axis) {
      Optional<PortalShape> optional = Optional.of(new PortalShape(levelaccessor, blockpos, direction_axis)).filter(predicate);
      if (optional.isPresent()) {
         return optional;
      } else {
         Direction.Axis direction_axis1 = direction_axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
         return Optional.of(new PortalShape(levelaccessor, blockpos, direction_axis1)).filter(predicate);
      }
   }

   public PortalShape(LevelAccessor levelaccessor, BlockPos blockpos, Direction.Axis direction_axis) {
      this.level = levelaccessor;
      this.axis = direction_axis;
      this.rightDir = direction_axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
      this.bottomLeft = this.calculateBottomLeft(blockpos);
      if (this.bottomLeft == null) {
         this.bottomLeft = blockpos;
         this.width = 1;
         this.height = 1;
      } else {
         this.width = this.calculateWidth();
         if (this.width > 0) {
            this.height = this.calculateHeight();
         }
      }

   }

   @Nullable
   private BlockPos calculateBottomLeft(BlockPos blockpos) {
      for(int i = Math.max(this.level.getMinBuildHeight(), blockpos.getY() - 21); blockpos.getY() > i && isEmpty(this.level.getBlockState(blockpos.below())); blockpos = blockpos.below()) {
      }

      Direction direction = this.rightDir.getOpposite();
      int j = this.getDistanceUntilEdgeAboveFrame(blockpos, direction) - 1;
      return j < 0 ? null : blockpos.relative(direction, j);
   }

   private int calculateWidth() {
      int i = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
      return i >= 2 && i <= 21 ? i : 0;
   }

   private int getDistanceUntilEdgeAboveFrame(BlockPos blockpos, Direction direction) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i <= 21; ++i) {
         blockpos_mutableblockpos.set(blockpos).move(direction, i);
         BlockState blockstate = this.level.getBlockState(blockpos_mutableblockpos);
         if (!isEmpty(blockstate)) {
            if (FRAME.test(blockstate, this.level, blockpos_mutableblockpos)) {
               return i;
            }
            break;
         }

         BlockState blockstate1 = this.level.getBlockState(blockpos_mutableblockpos.move(Direction.DOWN));
         if (!FRAME.test(blockstate1, this.level, blockpos_mutableblockpos)) {
            break;
         }
      }

      return 0;
   }

   private int calculateHeight() {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      int i = this.getDistanceUntilTop(blockpos_mutableblockpos);
      return i >= 3 && i <= 21 && this.hasTopFrame(blockpos_mutableblockpos, i) ? i : 0;
   }

   private boolean hasTopFrame(BlockPos.MutableBlockPos blockpos_mutableblockpos, int i) {
      for(int j = 0; j < this.width; ++j) {
         BlockPos.MutableBlockPos blockpos_mutableblockpos1 = blockpos_mutableblockpos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
         if (!FRAME.test(this.level.getBlockState(blockpos_mutableblockpos1), this.level, blockpos_mutableblockpos1)) {
            return false;
         }
      }

      return true;
   }

   private int getDistanceUntilTop(BlockPos.MutableBlockPos blockpos_mutableblockpos) {
      for(int i = 0; i < 21; ++i) {
         blockpos_mutableblockpos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
         if (!FRAME.test(this.level.getBlockState(blockpos_mutableblockpos), this.level, blockpos_mutableblockpos)) {
            return i;
         }

         blockpos_mutableblockpos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
         if (!FRAME.test(this.level.getBlockState(blockpos_mutableblockpos), this.level, blockpos_mutableblockpos)) {
            return i;
         }

         for(int j = 0; j < this.width; ++j) {
            blockpos_mutableblockpos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
            BlockState blockstate = this.level.getBlockState(blockpos_mutableblockpos);
            if (!isEmpty(blockstate)) {
               return i;
            }

            if (blockstate.is(Blocks.NETHER_PORTAL)) {
               ++this.numPortalBlocks;
            }
         }
      }

      return 21;
   }

   private static boolean isEmpty(BlockState blockstate) {
      return blockstate.isAir() || blockstate.is(BlockTags.FIRE) || blockstate.is(Blocks.NETHER_PORTAL);
   }

   public boolean isValid() {
      return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
   }

   public void createPortalBlocks() {
      BlockState blockstate = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
      BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach((blockpos) -> this.level.setBlock(blockpos, blockstate, 18));
   }

   public boolean isComplete() {
      return this.isValid() && this.numPortalBlocks == this.width * this.height;
   }

   public static Vec3 getRelativePosition(BlockUtil.FoundRectangle blockutil_foundrectangle, Direction.Axis direction_axis, Vec3 vec3, EntityDimensions entitydimensions) {
      double d0 = (double)blockutil_foundrectangle.axis1Size - (double)entitydimensions.width;
      double d1 = (double)blockutil_foundrectangle.axis2Size - (double)entitydimensions.height;
      BlockPos blockpos = blockutil_foundrectangle.minCorner;
      double d2;
      if (d0 > 0.0D) {
         float f = (float)blockpos.get(direction_axis) + entitydimensions.width / 2.0F;
         d2 = Mth.clamp(Mth.inverseLerp(vec3.get(direction_axis) - (double)f, 0.0D, d0), 0.0D, 1.0D);
      } else {
         d2 = 0.5D;
      }

      double d4;
      if (d1 > 0.0D) {
         Direction.Axis direction_axis1 = Direction.Axis.Y;
         d4 = Mth.clamp(Mth.inverseLerp(vec3.get(direction_axis1) - (double)blockpos.get(direction_axis1), 0.0D, d1), 0.0D, 1.0D);
      } else {
         d4 = 0.0D;
      }

      Direction.Axis direction_axis2 = direction_axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
      double d6 = vec3.get(direction_axis2) - ((double)blockpos.get(direction_axis2) + 0.5D);
      return new Vec3(d2, d4, d6);
   }

   public static PortalInfo createPortalInfo(ServerLevel serverlevel, BlockUtil.FoundRectangle blockutil_foundrectangle, Direction.Axis direction_axis, Vec3 vec3, Entity entity, Vec3 vec31, float f, float f1) {
      BlockPos blockpos = blockutil_foundrectangle.minCorner;
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      Direction.Axis direction_axis1 = blockstate.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
      double d0 = (double)blockutil_foundrectangle.axis1Size;
      double d1 = (double)blockutil_foundrectangle.axis2Size;
      EntityDimensions entitydimensions = entity.getDimensions(entity.getPose());
      int i = direction_axis == direction_axis1 ? 0 : 90;
      Vec3 vec32 = direction_axis == direction_axis1 ? vec31 : new Vec3(vec31.z, vec31.y, -vec31.x);
      double d2 = (double)entitydimensions.width / 2.0D + (d0 - (double)entitydimensions.width) * vec3.x();
      double d3 = (d1 - (double)entitydimensions.height) * vec3.y();
      double d4 = 0.5D + vec3.z();
      boolean flag = direction_axis1 == Direction.Axis.X;
      Vec3 vec33 = new Vec3((double)blockpos.getX() + (flag ? d2 : d4), (double)blockpos.getY() + d3, (double)blockpos.getZ() + (flag ? d4 : d2));
      Vec3 vec34 = findCollisionFreePosition(vec33, serverlevel, entity, entitydimensions);
      return new PortalInfo(vec34, vec32, f + (float)i, f1);
   }

   private static Vec3 findCollisionFreePosition(Vec3 vec3, ServerLevel serverlevel, Entity entity, EntityDimensions entitydimensions) {
      if (!(entitydimensions.width > 4.0F) && !(entitydimensions.height > 4.0F)) {
         double d0 = (double)entitydimensions.height / 2.0D;
         Vec3 vec31 = vec3.add(0.0D, d0, 0.0D);
         VoxelShape voxelshape = Shapes.create(AABB.ofSize(vec31, (double)entitydimensions.width, 0.0D, (double)entitydimensions.width).expandTowards(0.0D, 1.0D, 0.0D).inflate(1.0E-6D));
         Optional<Vec3> optional = serverlevel.findFreePosition(entity, voxelshape, vec31, (double)entitydimensions.width, (double)entitydimensions.height, (double)entitydimensions.width);
         Optional<Vec3> optional1 = optional.map((vec32) -> vec32.subtract(0.0D, d0, 0.0D));
         return optional1.orElse(vec3);
      } else {
         return vec3;
      }
   }
}
