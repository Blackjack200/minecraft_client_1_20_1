package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid {
   public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
   private static final int CACHE_SIZE = 200;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200) {
         protected void rehash(int i) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });
   private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> statedefinition_builder) {
      statedefinition_builder.add(FALLING);
   }

   public Vec3 getFlow(BlockGetter blockgetter, BlockPos blockpos, FluidState fluidstate) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         blockpos_mutableblockpos.setWithOffset(blockpos, direction);
         FluidState fluidstate1 = blockgetter.getFluidState(blockpos_mutableblockpos);
         if (this.affectsFlow(fluidstate1)) {
            float f = fluidstate1.getOwnHeight();
            float f1 = 0.0F;
            if (f == 0.0F) {
               if (!blockgetter.getBlockState(blockpos_mutableblockpos).blocksMotion()) {
                  BlockPos blockpos1 = blockpos_mutableblockpos.below();
                  FluidState fluidstate2 = blockgetter.getFluidState(blockpos1);
                  if (this.affectsFlow(fluidstate2)) {
                     f = fluidstate2.getOwnHeight();
                     if (f > 0.0F) {
                        f1 = fluidstate.getOwnHeight() - (f - 0.8888889F);
                     }
                  }
               }
            } else if (f > 0.0F) {
               f1 = fluidstate.getOwnHeight() - f;
            }

            if (f1 != 0.0F) {
               d0 += (double)((float)direction.getStepX() * f1);
               d1 += (double)((float)direction.getStepZ() * f1);
            }
         }
      }

      Vec3 vec3 = new Vec3(d0, 0.0D, d1);
      if (fluidstate.getValue(FALLING)) {
         for(Direction direction1 : Direction.Plane.HORIZONTAL) {
            blockpos_mutableblockpos.setWithOffset(blockpos, direction1);
            if (this.isSolidFace(blockgetter, blockpos_mutableblockpos, direction1) || this.isSolidFace(blockgetter, blockpos_mutableblockpos.above(), direction1)) {
               vec3 = vec3.normalize().add(0.0D, -6.0D, 0.0D);
               break;
            }
         }
      }

      return vec3.normalize();
   }

   private boolean affectsFlow(FluidState fluidstate) {
      return fluidstate.isEmpty() || fluidstate.getType().isSame(this);
   }

   protected boolean isSolidFace(BlockGetter blockgetter, BlockPos blockpos, Direction direction) {
      BlockState blockstate = blockgetter.getBlockState(blockpos);
      FluidState fluidstate = blockgetter.getFluidState(blockpos);
      if (fluidstate.getType().isSame(this)) {
         return false;
      } else if (direction == Direction.UP) {
         return true;
      } else {
         return blockstate.getBlock() instanceof IceBlock ? false : blockstate.isFaceSturdy(blockgetter, blockpos, direction);
      }
   }

   protected void spread(Level level, BlockPos blockpos, FluidState fluidstate) {
      if (!fluidstate.isEmpty()) {
         BlockState blockstate = level.getBlockState(blockpos);
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = level.getBlockState(blockpos1);
         FluidState fluidstate1 = this.getNewLiquid(level, blockpos1, blockstate1);
         if (this.canSpreadTo(level, blockpos, blockstate, Direction.DOWN, blockpos1, blockstate1, level.getFluidState(blockpos1), fluidstate1.getType())) {
            this.spreadTo(level, blockpos1, blockstate1, Direction.DOWN, fluidstate1);
            if (this.sourceNeighborCount(level, blockpos) >= 3) {
               this.spreadToSides(level, blockpos, fluidstate, blockstate);
            }
         } else if (fluidstate.isSource() || !this.isWaterHole(level, fluidstate1.getType(), blockpos, blockstate, blockpos1, blockstate1)) {
            this.spreadToSides(level, blockpos, fluidstate, blockstate);
         }

      }
   }

   private void spreadToSides(Level level, BlockPos blockpos, FluidState fluidstate, BlockState blockstate) {
      int i = fluidstate.getAmount() - this.getDropOff(level);
      if (fluidstate.getValue(FALLING)) {
         i = 7;
      }

      if (i > 0) {
         Map<Direction, FluidState> map = this.getSpread(level, blockpos, blockstate);

         for(Map.Entry<Direction, FluidState> map_entry : map.entrySet()) {
            Direction direction = map_entry.getKey();
            FluidState fluidstate1 = map_entry.getValue();
            BlockPos blockpos1 = blockpos.relative(direction);
            BlockState blockstate1 = level.getBlockState(blockpos1);
            if (this.canSpreadTo(level, blockpos, blockstate, direction, blockpos1, blockstate1, level.getFluidState(blockpos1), fluidstate1.getType())) {
               this.spreadTo(level, blockpos1, blockstate1, direction, fluidstate1);
            }
         }

      }
   }

   protected FluidState getNewLiquid(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = 0;
      int j = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         BlockState blockstate1 = level.getBlockState(blockpos1);
         FluidState fluidstate = blockstate1.getFluidState();
         if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, level, blockpos, blockstate, blockpos1, blockstate1)) {
            if (fluidstate.isSource()) {
               ++j;
            }

            i = Math.max(i, fluidstate.getAmount());
         }
      }

      if (this.canConvertToSource(level) && j >= 2) {
         BlockState blockstate2 = level.getBlockState(blockpos.below());
         FluidState fluidstate1 = blockstate2.getFluidState();
         if (blockstate2.isSolid() || this.isSourceBlockOfThisType(fluidstate1)) {
            return this.getSource(false);
         }
      }

      BlockPos blockpos2 = blockpos.above();
      BlockState blockstate3 = level.getBlockState(blockpos2);
      FluidState fluidstate2 = blockstate3.getFluidState();
      if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, level, blockpos, blockstate, blockpos2, blockstate3)) {
         return this.getFlowing(8, true);
      } else {
         int k = i - this.getDropOff(level);
         return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
      }
   }

   private boolean canPassThroughWall(Direction direction, BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, BlockPos blockpos1, BlockState blockstate1) {
      Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap1;
      if (!blockstate.getBlock().hasDynamicShape() && !blockstate1.getBlock().hasDynamicShape()) {
         object2bytelinkedopenhashmap1 = OCCLUSION_CACHE.get();
      } else {
         object2bytelinkedopenhashmap1 = null;
      }

      Block.BlockStatePairKey block_blockstatepairkey;
      if (object2bytelinkedopenhashmap1 != null) {
         block_blockstatepairkey = new Block.BlockStatePairKey(blockstate, blockstate1, direction);
         byte b0 = object2bytelinkedopenhashmap1.getAndMoveToFirst(block_blockstatepairkey);
         if (b0 != 127) {
            return b0 != 0;
         }
      } else {
         block_blockstatepairkey = null;
      }

      VoxelShape voxelshape = blockstate.getCollisionShape(blockgetter, blockpos);
      VoxelShape voxelshape1 = blockstate1.getCollisionShape(blockgetter, blockpos1);
      boolean flag = !Shapes.mergedFaceOccludes(voxelshape, voxelshape1, direction);
      if (object2bytelinkedopenhashmap1 != null) {
         if (object2bytelinkedopenhashmap1.size() == 200) {
            object2bytelinkedopenhashmap1.removeLastByte();
         }

         object2bytelinkedopenhashmap1.putAndMoveToFirst(block_blockstatepairkey, (byte)(flag ? 1 : 0));
      }

      return flag;
   }

   public abstract Fluid getFlowing();

   public FluidState getFlowing(int i, boolean flag) {
      return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(i)).setValue(FALLING, Boolean.valueOf(flag));
   }

   public abstract Fluid getSource();

   public FluidState getSource(boolean flag) {
      return this.getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(flag));
   }

   protected abstract boolean canConvertToSource(Level level);

   protected void spreadTo(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate, Direction direction, FluidState fluidstate) {
      if (blockstate.getBlock() instanceof LiquidBlockContainer) {
         ((LiquidBlockContainer)blockstate.getBlock()).placeLiquid(levelaccessor, blockpos, blockstate, fluidstate);
      } else {
         if (!blockstate.isAir()) {
            this.beforeDestroyingBlock(levelaccessor, blockpos, blockstate);
         }

         levelaccessor.setBlock(blockpos, fluidstate.createLegacyBlock(), 3);
      }

   }

   protected abstract void beforeDestroyingBlock(LevelAccessor levelaccessor, BlockPos blockpos, BlockState blockstate);

   private static short getCacheKey(BlockPos blockpos, BlockPos blockpos1) {
      int i = blockpos1.getX() - blockpos.getX();
      int j = blockpos1.getZ() - blockpos.getZ();
      return (short)((i + 128 & 255) << 8 | j + 128 & 255);
   }

   protected int getSlopeDistance(LevelReader levelreader, BlockPos blockpos, int i, Direction direction, BlockState blockstate, BlockPos blockpos1, Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap, Short2BooleanMap short2booleanmap) {
      int j = 1000;

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         if (direction1 != direction) {
            BlockPos blockpos2 = blockpos.relative(direction1);
            short short0 = getCacheKey(blockpos1, blockpos2);
            Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short0, (short2) -> {
               BlockState blockstate4 = levelreader.getBlockState(blockpos2);
               return Pair.of(blockstate4, blockstate4.getFluidState());
            });
            BlockState blockstate1 = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            if (this.canPassThrough(levelreader, this.getFlowing(), blockpos, blockstate, direction1, blockpos2, blockstate1, fluidstate)) {
               boolean flag = short2booleanmap.computeIfAbsent(short0, (short1) -> {
                  BlockPos blockpos4 = blockpos2.below();
                  BlockState blockstate3 = levelreader.getBlockState(blockpos4);
                  return this.isWaterHole(levelreader, this.getFlowing(), blockpos2, blockstate1, blockpos4, blockstate3);
               });
               if (flag) {
                  return i;
               }

               if (i < this.getSlopeFindDistance(levelreader)) {
                  int k = this.getSlopeDistance(levelreader, blockpos2, i + 1, direction1.getOpposite(), blockstate1, blockpos1, short2objectmap, short2booleanmap);
                  if (k < j) {
                     j = k;
                  }
               }
            }
         }
      }

      return j;
   }

   private boolean isWaterHole(BlockGetter blockgetter, Fluid fluid, BlockPos blockpos, BlockState blockstate, BlockPos blockpos1, BlockState blockstate1) {
      if (!this.canPassThroughWall(Direction.DOWN, blockgetter, blockpos, blockstate, blockpos1, blockstate1)) {
         return false;
      } else {
         return blockstate1.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(blockgetter, blockpos1, blockstate1, fluid);
      }
   }

   private boolean canPassThrough(BlockGetter blockgetter, Fluid fluid, BlockPos blockpos, BlockState blockstate, Direction direction, BlockPos blockpos1, BlockState blockstate1, FluidState fluidstate) {
      return !this.isSourceBlockOfThisType(fluidstate) && this.canPassThroughWall(direction, blockgetter, blockpos, blockstate, blockpos1, blockstate1) && this.canHoldFluid(blockgetter, blockpos1, blockstate1, fluid);
   }

   private boolean isSourceBlockOfThisType(FluidState fluidstate) {
      return fluidstate.getType().isSame(this) && fluidstate.isSource();
   }

   protected abstract int getSlopeFindDistance(LevelReader levelreader);

   private int sourceNeighborCount(LevelReader levelreader, BlockPos blockpos) {
      int i = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         FluidState fluidstate = levelreader.getFluidState(blockpos1);
         if (this.isSourceBlockOfThisType(fluidstate)) {
            ++i;
         }
      }

      return i;
   }

   protected Map<Direction, FluidState> getSpread(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = 1000;
      Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
      Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
      Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos1 = blockpos.relative(direction);
         short short0 = getCacheKey(blockpos, blockpos1);
         Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short0, (short2) -> {
            BlockState blockstate4 = level.getBlockState(blockpos1);
            return Pair.of(blockstate4, blockstate4.getFluidState());
         });
         BlockState blockstate1 = pair.getFirst();
         FluidState fluidstate = pair.getSecond();
         FluidState fluidstate1 = this.getNewLiquid(level, blockpos1, blockstate1);
         if (this.canPassThrough(level, fluidstate1.getType(), blockpos, blockstate, direction, blockpos1, blockstate1, fluidstate)) {
            BlockPos blockpos2 = blockpos1.below();
            boolean flag = short2booleanmap.computeIfAbsent(short0, (short1) -> {
               BlockState blockstate3 = level.getBlockState(blockpos2);
               return this.isWaterHole(level, this.getFlowing(), blockpos1, blockstate1, blockpos2, blockstate3);
            });
            int j;
            if (flag) {
               j = 0;
            } else {
               j = this.getSlopeDistance(level, blockpos1, 1, direction.getOpposite(), blockstate1, blockpos, short2objectmap, short2booleanmap);
            }

            if (j < i) {
               map.clear();
            }

            if (j <= i) {
               map.put(direction, fluidstate1);
               i = j;
            }
         }
      }

      return map;
   }

   private boolean canHoldFluid(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Fluid fluid) {
      Block block = blockstate.getBlock();
      if (block instanceof LiquidBlockContainer) {
         return ((LiquidBlockContainer)block).canPlaceLiquid(blockgetter, blockpos, blockstate, fluid);
      } else if (!(block instanceof DoorBlock) && !blockstate.is(BlockTags.SIGNS) && !blockstate.is(Blocks.LADDER) && !blockstate.is(Blocks.SUGAR_CANE) && !blockstate.is(Blocks.BUBBLE_COLUMN)) {
         if (!blockstate.is(Blocks.NETHER_PORTAL) && !blockstate.is(Blocks.END_PORTAL) && !blockstate.is(Blocks.END_GATEWAY) && !blockstate.is(Blocks.STRUCTURE_VOID)) {
            return !blockstate.blocksMotion();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean canSpreadTo(BlockGetter blockgetter, BlockPos blockpos, BlockState blockstate, Direction direction, BlockPos blockpos1, BlockState blockstate1, FluidState fluidstate, Fluid fluid) {
      return fluidstate.canBeReplacedWith(blockgetter, blockpos1, fluid, direction) && this.canPassThroughWall(direction, blockgetter, blockpos, blockstate, blockpos1, blockstate1) && this.canHoldFluid(blockgetter, blockpos1, blockstate1, fluid);
   }

   protected abstract int getDropOff(LevelReader levelreader);

   protected int getSpreadDelay(Level level, BlockPos blockpos, FluidState fluidstate, FluidState fluidstate1) {
      return this.getTickDelay(level);
   }

   public void tick(Level level, BlockPos blockpos, FluidState fluidstate) {
      if (!fluidstate.isSource()) {
         FluidState fluidstate1 = this.getNewLiquid(level, blockpos, level.getBlockState(blockpos));
         int i = this.getSpreadDelay(level, blockpos, fluidstate, fluidstate1);
         if (fluidstate1.isEmpty()) {
            fluidstate = fluidstate1;
            level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 3);
         } else if (!fluidstate1.equals(fluidstate)) {
            fluidstate = fluidstate1;
            BlockState blockstate = fluidstate1.createLegacyBlock();
            level.setBlock(blockpos, blockstate, 2);
            level.scheduleTick(blockpos, fluidstate1.getType(), i);
            level.updateNeighborsAt(blockpos, blockstate.getBlock());
         }
      }

      this.spread(level, blockpos, fluidstate);
   }

   protected static int getLegacyLevel(FluidState fluidstate) {
      return fluidstate.isSource() ? 0 : 8 - Math.min(fluidstate.getAmount(), 8) + (fluidstate.getValue(FALLING) ? 8 : 0);
   }

   private static boolean hasSameAbove(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos) {
      return fluidstate.getType().isSame(blockgetter.getFluidState(blockpos.above()).getType());
   }

   public float getHeight(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos) {
      return hasSameAbove(fluidstate, blockgetter, blockpos) ? 1.0F : fluidstate.getOwnHeight();
   }

   public float getOwnHeight(FluidState fluidstate) {
      return (float)fluidstate.getAmount() / 9.0F;
   }

   public abstract int getAmount(FluidState fluidstate);

   public VoxelShape getShape(FluidState fluidstate, BlockGetter blockgetter, BlockPos blockpos) {
      return fluidstate.getAmount() == 9 && hasSameAbove(fluidstate, blockgetter, blockpos) ? Shapes.block() : this.shapes.computeIfAbsent(fluidstate, (fluidstate1) -> Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)fluidstate1.getHeight(blockgetter, blockpos), 1.0D));
   }
}
