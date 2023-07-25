package net.minecraft.util;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnUtil {
   public static <T extends Mob> Optional<T> trySpawnMob(EntityType<T> entitytype, MobSpawnType mobspawntype, ServerLevel serverlevel, BlockPos blockpos, int i, int j, int k, SpawnUtil.Strategy spawnutil_strategy) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = blockpos.mutable();

      for(int l = 0; l < i; ++l) {
         int i1 = Mth.randomBetweenInclusive(serverlevel.random, -j, j);
         int j1 = Mth.randomBetweenInclusive(serverlevel.random, -j, j);
         blockpos_mutableblockpos.setWithOffset(blockpos, i1, k, j1);
         if (serverlevel.getWorldBorder().isWithinBounds(blockpos_mutableblockpos) && moveToPossibleSpawnPosition(serverlevel, k, blockpos_mutableblockpos, spawnutil_strategy)) {
            T mob = entitytype.create(serverlevel, (CompoundTag)null, (Consumer<T>)null, blockpos_mutableblockpos, mobspawntype, false, false);
            if (mob != null) {
               if (mob.checkSpawnRules(serverlevel, mobspawntype) && mob.checkSpawnObstruction(serverlevel)) {
                  serverlevel.addFreshEntityWithPassengers(mob);
                  return Optional.of(mob);
               }

               mob.discard();
            }
         }
      }

      return Optional.empty();
   }

   private static boolean moveToPossibleSpawnPosition(ServerLevel serverlevel, int i, BlockPos.MutableBlockPos blockpos_mutableblockpos, SpawnUtil.Strategy spawnutil_strategy) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos1 = (new BlockPos.MutableBlockPos()).set(blockpos_mutableblockpos);
      BlockState blockstate = serverlevel.getBlockState(blockpos_mutableblockpos1);

      for(int j = i; j >= -i; --j) {
         blockpos_mutableblockpos.move(Direction.DOWN);
         blockpos_mutableblockpos1.setWithOffset(blockpos_mutableblockpos, Direction.UP);
         BlockState blockstate1 = serverlevel.getBlockState(blockpos_mutableblockpos);
         if (spawnutil_strategy.canSpawnOn(serverlevel, blockpos_mutableblockpos, blockstate1, blockpos_mutableblockpos1, blockstate)) {
            blockpos_mutableblockpos.move(Direction.UP);
            return true;
         }

         blockstate = blockstate1;
      }

      return false;
   }

   public interface Strategy {
      /** @deprecated */
      @Deprecated
      SpawnUtil.Strategy LEGACY_IRON_GOLEM = (serverlevel, blockpos, blockstate, blockpos1, blockstate1) -> {
         if (!blockstate.is(Blocks.COBWEB) && !blockstate.is(Blocks.CACTUS) && !blockstate.is(Blocks.GLASS_PANE) && !(blockstate.getBlock() instanceof StainedGlassPaneBlock) && !(blockstate.getBlock() instanceof StainedGlassBlock) && !(blockstate.getBlock() instanceof LeavesBlock) && !blockstate.is(Blocks.CONDUIT) && !blockstate.is(Blocks.ICE) && !blockstate.is(Blocks.TNT) && !blockstate.is(Blocks.GLOWSTONE) && !blockstate.is(Blocks.BEACON) && !blockstate.is(Blocks.SEA_LANTERN) && !blockstate.is(Blocks.FROSTED_ICE) && !blockstate.is(Blocks.TINTED_GLASS) && !blockstate.is(Blocks.GLASS)) {
            return (blockstate1.isAir() || blockstate1.liquid()) && (blockstate.isSolid() || blockstate.is(Blocks.POWDER_SNOW));
         } else {
            return false;
         }
      };
      SpawnUtil.Strategy ON_TOP_OF_COLLIDER = (serverlevel, blockpos, blockstate, blockpos1, blockstate1) -> blockstate1.getCollisionShape(serverlevel, blockpos1).isEmpty() && Block.isFaceFull(blockstate.getCollisionShape(serverlevel, blockpos), Direction.UP);

      boolean canSpawnOn(ServerLevel serverlevel, BlockPos blockpos, BlockState blockstate, BlockPos blockpos1, BlockState blockstate1);
   }
}
