package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonMovingBlockEntity extends BlockEntity {
   private static final int TICKS_TO_EXTEND = 2;
   private static final double PUSH_OFFSET = 0.01D;
   public static final double TICK_MOVEMENT = 0.51D;
   private BlockState movedState = Blocks.AIR.defaultBlockState();
   private Direction direction;
   private boolean extending;
   private boolean isSourcePiston;
   private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);
   private float progress;
   private float progressO;
   private long lastTicked;
   private int deathTicks;

   public PistonMovingBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.PISTON, blockpos, blockstate);
   }

   public PistonMovingBlockEntity(BlockPos blockpos, BlockState blockstate, BlockState blockstate1, Direction direction, boolean flag, boolean flag1) {
      this(blockpos, blockstate);
      this.movedState = blockstate1;
      this.direction = direction;
      this.extending = flag;
      this.isSourcePiston = flag1;
   }

   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public boolean isExtending() {
      return this.extending;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public boolean isSourcePiston() {
      return this.isSourcePiston;
   }

   public float getProgress(float f) {
      if (f > 1.0F) {
         f = 1.0F;
      }

      return Mth.lerp(f, this.progressO, this.progress);
   }

   public float getXOff(float f) {
      return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(f));
   }

   public float getYOff(float f) {
      return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(f));
   }

   public float getZOff(float f) {
      return (float)this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(f));
   }

   private float getExtendedProgress(float f) {
      return this.extending ? f - 1.0F : 1.0F - f;
   }

   private BlockState getCollisionRelatedBlockState() {
      return !this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof PistonBaseBlock ? Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.progress > 0.25F)).setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT).setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING)) : this.movedState;
   }

   private static void moveCollidedEntities(Level level, BlockPos blockpos, float f, PistonMovingBlockEntity pistonmovingblockentity) {
      Direction direction = pistonmovingblockentity.getMovementDirection();
      double d0 = (double)(f - pistonmovingblockentity.progress);
      VoxelShape voxelshape = pistonmovingblockentity.getCollisionRelatedBlockState().getCollisionShape(level, blockpos);
      if (!voxelshape.isEmpty()) {
         AABB aabb = moveByPositionAndProgress(blockpos, voxelshape.bounds(), pistonmovingblockentity);
         List<Entity> list = level.getEntities((Entity)null, PistonMath.getMovementArea(aabb, direction, d0).minmax(aabb));
         if (!list.isEmpty()) {
            List<AABB> list1 = voxelshape.toAabbs();
            boolean flag = pistonmovingblockentity.movedState.is(Blocks.SLIME_BLOCK);
            Iterator var12 = list.iterator();

            while(true) {
               Entity entity;
               while(true) {
                  if (!var12.hasNext()) {
                     return;
                  }

                  entity = (Entity)var12.next();
                  if (entity.getPistonPushReaction() != PushReaction.IGNORE) {
                     if (!flag) {
                        break;
                     }

                     if (!(entity instanceof ServerPlayer)) {
                        Vec3 vec3 = entity.getDeltaMovement();
                        double d1 = vec3.x;
                        double d2 = vec3.y;
                        double d3 = vec3.z;
                        switch (direction.getAxis()) {
                           case X:
                              d1 = (double)direction.getStepX();
                              break;
                           case Y:
                              d2 = (double)direction.getStepY();
                              break;
                           case Z:
                              d3 = (double)direction.getStepZ();
                        }

                        entity.setDeltaMovement(d1, d2, d3);
                        break;
                     }
                  }
               }

               double d4 = 0.0D;

               for(AABB aabb1 : list1) {
                  AABB aabb2 = PistonMath.getMovementArea(moveByPositionAndProgress(blockpos, aabb1, pistonmovingblockentity), direction, d0);
                  AABB aabb3 = entity.getBoundingBox();
                  if (aabb2.intersects(aabb3)) {
                     d4 = Math.max(d4, getMovement(aabb2, direction, aabb3));
                     if (d4 >= d0) {
                        break;
                     }
                  }
               }

               if (!(d4 <= 0.0D)) {
                  d4 = Math.min(d4, d0) + 0.01D;
                  moveEntityByPiston(direction, entity, d4, direction);
                  if (!pistonmovingblockentity.extending && pistonmovingblockentity.isSourcePiston) {
                     fixEntityWithinPistonBase(blockpos, entity, direction, d0);
                  }
               }
            }
         }
      }
   }

   private static void moveEntityByPiston(Direction direction, Entity entity, double d0, Direction direction1) {
      NOCLIP.set(direction);
      entity.move(MoverType.PISTON, new Vec3(d0 * (double)direction1.getStepX(), d0 * (double)direction1.getStepY(), d0 * (double)direction1.getStepZ()));
      NOCLIP.set((Direction)null);
   }

   private static void moveStuckEntities(Level level, BlockPos blockpos, float f, PistonMovingBlockEntity pistonmovingblockentity) {
      if (pistonmovingblockentity.isStickyForEntities()) {
         Direction direction = pistonmovingblockentity.getMovementDirection();
         if (direction.getAxis().isHorizontal()) {
            double d0 = pistonmovingblockentity.movedState.getCollisionShape(level, blockpos).max(Direction.Axis.Y);
            AABB aabb = moveByPositionAndProgress(blockpos, new AABB(0.0D, d0, 0.0D, 1.0D, 1.5000010000000001D, 1.0D), pistonmovingblockentity);
            double d1 = (double)(f - pistonmovingblockentity.progress);

            for(Entity entity : level.getEntities((Entity)null, aabb, (entity1) -> matchesStickyCritera(aabb, entity1, blockpos))) {
               moveEntityByPiston(direction, entity, d1, direction);
            }

         }
      }
   }

   private static boolean matchesStickyCritera(AABB aabb, Entity entity, BlockPos blockpos) {
      return entity.getPistonPushReaction() == PushReaction.NORMAL && entity.onGround() && (entity.isSupportedBy(blockpos) || entity.getX() >= aabb.minX && entity.getX() <= aabb.maxX && entity.getZ() >= aabb.minZ && entity.getZ() <= aabb.maxZ);
   }

   private boolean isStickyForEntities() {
      return this.movedState.is(Blocks.HONEY_BLOCK);
   }

   public Direction getMovementDirection() {
      return this.extending ? this.direction : this.direction.getOpposite();
   }

   private static double getMovement(AABB aabb, Direction direction, AABB aabb1) {
      switch (direction) {
         case EAST:
            return aabb.maxX - aabb1.minX;
         case WEST:
            return aabb1.maxX - aabb.minX;
         case UP:
         default:
            return aabb.maxY - aabb1.minY;
         case DOWN:
            return aabb1.maxY - aabb.minY;
         case SOUTH:
            return aabb.maxZ - aabb1.minZ;
         case NORTH:
            return aabb1.maxZ - aabb.minZ;
      }
   }

   private static AABB moveByPositionAndProgress(BlockPos blockpos, AABB aabb, PistonMovingBlockEntity pistonmovingblockentity) {
      double d0 = (double)pistonmovingblockentity.getExtendedProgress(pistonmovingblockentity.progress);
      return aabb.move((double)blockpos.getX() + d0 * (double)pistonmovingblockentity.direction.getStepX(), (double)blockpos.getY() + d0 * (double)pistonmovingblockentity.direction.getStepY(), (double)blockpos.getZ() + d0 * (double)pistonmovingblockentity.direction.getStepZ());
   }

   private static void fixEntityWithinPistonBase(BlockPos blockpos, Entity entity, Direction direction, double d0) {
      AABB aabb = entity.getBoundingBox();
      AABB aabb1 = Shapes.block().bounds().move(blockpos);
      if (aabb.intersects(aabb1)) {
         Direction direction1 = direction.getOpposite();
         double d1 = getMovement(aabb1, direction1, aabb) + 0.01D;
         double d2 = getMovement(aabb1, direction1, aabb.intersect(aabb1)) + 0.01D;
         if (Math.abs(d1 - d2) < 0.01D) {
            d1 = Math.min(d1, d0) + 0.01D;
            moveEntityByPiston(direction, entity, d1, direction1);
         }
      }

   }

   public BlockState getMovedState() {
      return this.movedState;
   }

   public void finalTick() {
      if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide)) {
         this.progress = 1.0F;
         this.progressO = this.progress;
         this.level.removeBlockEntity(this.worldPosition);
         this.setRemoved();
         if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
            BlockState blockstate;
            if (this.isSourcePiston) {
               blockstate = Blocks.AIR.defaultBlockState();
            } else {
               blockstate = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
            }

            this.level.setBlock(this.worldPosition, blockstate, 3);
            this.level.neighborChanged(this.worldPosition, blockstate.getBlock(), this.worldPosition);
         }
      }

   }

   public static void tick(Level level, BlockPos blockpos, BlockState blockstate, PistonMovingBlockEntity pistonmovingblockentity) {
      pistonmovingblockentity.lastTicked = level.getGameTime();
      pistonmovingblockentity.progressO = pistonmovingblockentity.progress;
      if (pistonmovingblockentity.progressO >= 1.0F) {
         if (level.isClientSide && pistonmovingblockentity.deathTicks < 5) {
            ++pistonmovingblockentity.deathTicks;
         } else {
            level.removeBlockEntity(blockpos);
            pistonmovingblockentity.setRemoved();
            if (level.getBlockState(blockpos).is(Blocks.MOVING_PISTON)) {
               BlockState blockstate1 = Block.updateFromNeighbourShapes(pistonmovingblockentity.movedState, level, blockpos);
               if (blockstate1.isAir()) {
                  level.setBlock(blockpos, pistonmovingblockentity.movedState, 84);
                  Block.updateOrDestroy(pistonmovingblockentity.movedState, blockstate1, level, blockpos, 3);
               } else {
                  if (blockstate1.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate1.getValue(BlockStateProperties.WATERLOGGED)) {
                     blockstate1 = blockstate1.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
                  }

                  level.setBlock(blockpos, blockstate1, 67);
                  level.neighborChanged(blockpos, blockstate1.getBlock(), blockpos);
               }
            }

         }
      } else {
         float f = pistonmovingblockentity.progress + 0.5F;
         moveCollidedEntities(level, blockpos, f, pistonmovingblockentity);
         moveStuckEntities(level, blockpos, f, pistonmovingblockentity);
         pistonmovingblockentity.progress = f;
         if (pistonmovingblockentity.progress >= 1.0F) {
            pistonmovingblockentity.progress = 1.0F;
         }

      }
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      HolderGetter<Block> holdergetter = (HolderGetter<Block>)(this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup());
      this.movedState = NbtUtils.readBlockState(holdergetter, compoundtag.getCompound("blockState"));
      this.direction = Direction.from3DDataValue(compoundtag.getInt("facing"));
      this.progress = compoundtag.getFloat("progress");
      this.progressO = this.progress;
      this.extending = compoundtag.getBoolean("extending");
      this.isSourcePiston = compoundtag.getBoolean("source");
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      compoundtag.put("blockState", NbtUtils.writeBlockState(this.movedState));
      compoundtag.putInt("facing", this.direction.get3DDataValue());
      compoundtag.putFloat("progress", this.progressO);
      compoundtag.putBoolean("extending", this.extending);
      compoundtag.putBoolean("source", this.isSourcePiston);
   }

   public VoxelShape getCollisionShape(BlockGetter blockgetter, BlockPos blockpos) {
      VoxelShape voxelshape;
      if (!this.extending && this.isSourcePiston && this.movedState.getBlock() instanceof PistonBaseBlock) {
         voxelshape = this.movedState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true)).getCollisionShape(blockgetter, blockpos);
      } else {
         voxelshape = Shapes.empty();
      }

      Direction direction = NOCLIP.get();
      if ((double)this.progress < 1.0D && direction == this.getMovementDirection()) {
         return voxelshape;
      } else {
         BlockState blockstate;
         if (this.isSourcePiston()) {
            blockstate = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, this.direction).setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 0.25F));
         } else {
            blockstate = this.movedState;
         }

         float f = this.getExtendedProgress(this.progress);
         double d0 = (double)((float)this.direction.getStepX() * f);
         double d1 = (double)((float)this.direction.getStepY() * f);
         double d2 = (double)((float)this.direction.getStepZ() * f);
         return Shapes.or(voxelshape, blockstate.getCollisionShape(blockgetter, blockpos).move(d0, d1, d2));
      }
   }

   public long getLastTicked() {
      return this.lastTicked;
   }

   public void setLevel(Level level) {
      super.setLevel(level);
      if (level.holderLookup(Registries.BLOCK).get(this.movedState.getBlock().builtInRegistryHolder().key()).isEmpty()) {
         this.movedState = Blocks.AIR.defaultBlockState();
      }

   }
}
