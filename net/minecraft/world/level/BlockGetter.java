package net.minecraft.world.level;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface BlockGetter extends LevelHeightAccessor {
   @Nullable
   BlockEntity getBlockEntity(BlockPos blockpos);

   default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos blockpos, BlockEntityType<T> blockentitytype) {
      BlockEntity blockentity = this.getBlockEntity(blockpos);
      return blockentity != null && blockentity.getType() == blockentitytype ? Optional.of((T)blockentity) : Optional.empty();
   }

   BlockState getBlockState(BlockPos blockpos);

   FluidState getFluidState(BlockPos blockpos);

   default int getLightEmission(BlockPos blockpos) {
      return this.getBlockState(blockpos).getLightEmission();
   }

   default int getMaxLightLevel() {
      return 15;
   }

   default Stream<BlockState> getBlockStates(AABB aabb) {
      return BlockPos.betweenClosedStream(aabb).map(this::getBlockState);
   }

   default BlockHitResult isBlockInLine(ClipBlockStateContext clipblockstatecontext) {
      return traverseBlocks(clipblockstatecontext.getFrom(), clipblockstatecontext.getTo(), clipblockstatecontext, (clipblockstatecontext2, blockpos) -> {
         BlockState blockstate = this.getBlockState(blockpos);
         Vec3 vec31 = clipblockstatecontext2.getFrom().subtract(clipblockstatecontext2.getTo());
         return clipblockstatecontext2.isTargetBlock().test(blockstate) ? new BlockHitResult(clipblockstatecontext2.getTo(), Direction.getNearest(vec31.x, vec31.y, vec31.z), BlockPos.containing(clipblockstatecontext2.getTo()), false) : null;
      }, (clipblockstatecontext1) -> {
         Vec3 vec3 = clipblockstatecontext1.getFrom().subtract(clipblockstatecontext1.getTo());
         return BlockHitResult.miss(clipblockstatecontext1.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipblockstatecontext1.getTo()));
      });
   }

   default BlockHitResult clip(ClipContext clipcontext) {
      return traverseBlocks(clipcontext.getFrom(), clipcontext.getTo(), clipcontext, (clipcontext2, blockpos) -> {
         BlockState blockstate = this.getBlockState(blockpos);
         FluidState fluidstate = this.getFluidState(blockpos);
         Vec3 vec31 = clipcontext2.getFrom();
         Vec3 vec32 = clipcontext2.getTo();
         VoxelShape voxelshape = clipcontext2.getBlockShape(blockstate, this, blockpos);
         BlockHitResult blockhitresult = this.clipWithInteractionOverride(vec31, vec32, blockpos, voxelshape, blockstate);
         VoxelShape voxelshape1 = clipcontext2.getFluidShape(fluidstate, this, blockpos);
         BlockHitResult blockhitresult1 = voxelshape1.clip(vec31, vec32, blockpos);
         double d0 = blockhitresult == null ? Double.MAX_VALUE : clipcontext2.getFrom().distanceToSqr(blockhitresult.getLocation());
         double d1 = blockhitresult1 == null ? Double.MAX_VALUE : clipcontext2.getFrom().distanceToSqr(blockhitresult1.getLocation());
         return d0 <= d1 ? blockhitresult : blockhitresult1;
      }, (clipcontext1) -> {
         Vec3 vec3 = clipcontext1.getFrom().subtract(clipcontext1.getTo());
         return BlockHitResult.miss(clipcontext1.getTo(), Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(clipcontext1.getTo()));
      });
   }

   @Nullable
   default BlockHitResult clipWithInteractionOverride(Vec3 vec3, Vec3 vec31, BlockPos blockpos, VoxelShape voxelshape, BlockState blockstate) {
      BlockHitResult blockhitresult = voxelshape.clip(vec3, vec31, blockpos);
      if (blockhitresult != null) {
         BlockHitResult blockhitresult1 = blockstate.getInteractionShape(this, blockpos).clip(vec3, vec31, blockpos);
         if (blockhitresult1 != null && blockhitresult1.getLocation().subtract(vec3).lengthSqr() < blockhitresult.getLocation().subtract(vec3).lengthSqr()) {
            return blockhitresult.withDirection(blockhitresult1.getDirection());
         }
      }

      return blockhitresult;
   }

   default double getBlockFloorHeight(VoxelShape voxelshape, Supplier<VoxelShape> supplier) {
      if (!voxelshape.isEmpty()) {
         return voxelshape.max(Direction.Axis.Y);
      } else {
         double d0 = supplier.get().max(Direction.Axis.Y);
         return d0 >= 1.0D ? d0 - 1.0D : Double.NEGATIVE_INFINITY;
      }
   }

   default double getBlockFloorHeight(BlockPos blockpos) {
      return this.getBlockFloorHeight(this.getBlockState(blockpos).getCollisionShape(this, blockpos), () -> {
         BlockPos blockpos2 = blockpos.below();
         return this.getBlockState(blockpos2).getCollisionShape(this, blockpos2);
      });
   }

   static <T, C> T traverseBlocks(Vec3 vec3, Vec3 vec31, C object, BiFunction<C, BlockPos, T> bifunction, Function<C, T> function) {
      if (vec3.equals(vec31)) {
         return function.apply(object);
      } else {
         double d0 = Mth.lerp(-1.0E-7D, vec31.x, vec3.x);
         double d1 = Mth.lerp(-1.0E-7D, vec31.y, vec3.y);
         double d2 = Mth.lerp(-1.0E-7D, vec31.z, vec3.z);
         double d3 = Mth.lerp(-1.0E-7D, vec3.x, vec31.x);
         double d4 = Mth.lerp(-1.0E-7D, vec3.y, vec31.y);
         double d5 = Mth.lerp(-1.0E-7D, vec3.z, vec31.z);
         int i = Mth.floor(d3);
         int j = Mth.floor(d4);
         int k = Mth.floor(d5);
         BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos(i, j, k);
         T object1 = bifunction.apply(object, blockpos_mutableblockpos);
         if (object1 != null) {
            return object1;
         } else {
            double d6 = d0 - d3;
            double d7 = d1 - d4;
            double d8 = d2 - d5;
            int l = Mth.sign(d6);
            int i1 = Mth.sign(d7);
            int j1 = Mth.sign(d8);
            double d9 = l == 0 ? Double.MAX_VALUE : (double)l / d6;
            double d10 = i1 == 0 ? Double.MAX_VALUE : (double)i1 / d7;
            double d11 = j1 == 0 ? Double.MAX_VALUE : (double)j1 / d8;
            double d12 = d9 * (l > 0 ? 1.0D - Mth.frac(d3) : Mth.frac(d3));
            double d13 = d10 * (i1 > 0 ? 1.0D - Mth.frac(d4) : Mth.frac(d4));
            double d14 = d11 * (j1 > 0 ? 1.0D - Mth.frac(d5) : Mth.frac(d5));

            while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
               if (d12 < d13) {
                  if (d12 < d14) {
                     i += l;
                     d12 += d9;
                  } else {
                     k += j1;
                     d14 += d11;
                  }
               } else if (d13 < d14) {
                  j += i1;
                  d13 += d10;
               } else {
                  k += j1;
                  d14 += d11;
               }

               T object2 = bifunction.apply(object, blockpos_mutableblockpos.set(i, j, k));
               if (object2 != null) {
                  return object2;
               }
            }

            return function.apply(object);
         }
      }
   }
}
