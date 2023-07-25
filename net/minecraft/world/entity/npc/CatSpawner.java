package net.minecraft.world.entity.npc;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.AABB;

public class CatSpawner implements CustomSpawner {
   private static final int TICK_DELAY = 1200;
   private int nextTick;

   public int tick(ServerLevel serverlevel, boolean flag, boolean flag1) {
      if (flag1 && serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
         --this.nextTick;
         if (this.nextTick > 0) {
            return 0;
         } else {
            this.nextTick = 1200;
            Player player = serverlevel.getRandomPlayer();
            if (player == null) {
               return 0;
            } else {
               RandomSource randomsource = serverlevel.random;
               int i = (8 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
               int j = (8 + randomsource.nextInt(24)) * (randomsource.nextBoolean() ? -1 : 1);
               BlockPos blockpos = player.blockPosition().offset(i, 0, j);
               int k = 10;
               if (!serverlevel.hasChunksAt(blockpos.getX() - 10, blockpos.getZ() - 10, blockpos.getX() + 10, blockpos.getZ() + 10)) {
                  return 0;
               } else {
                  if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, serverlevel, blockpos, EntityType.CAT)) {
                     if (serverlevel.isCloseToVillage(blockpos, 2)) {
                        return this.spawnInVillage(serverlevel, blockpos);
                     }

                     if (serverlevel.structureManager().getStructureWithPieceAt(blockpos, StructureTags.CATS_SPAWN_IN).isValid()) {
                        return this.spawnInHut(serverlevel, blockpos);
                     }
                  }

                  return 0;
               }
            }
         }
      } else {
         return 0;
      }
   }

   private int spawnInVillage(ServerLevel serverlevel, BlockPos blockpos) {
      int i = 48;
      if (serverlevel.getPoiManager().getCountInRange((holder) -> holder.is(PoiTypes.HOME), blockpos, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L) {
         List<Cat> list = serverlevel.getEntitiesOfClass(Cat.class, (new AABB(blockpos)).inflate(48.0D, 8.0D, 48.0D));
         if (list.size() < 5) {
            return this.spawnCat(blockpos, serverlevel);
         }
      }

      return 0;
   }

   private int spawnInHut(ServerLevel serverlevel, BlockPos blockpos) {
      int i = 16;
      List<Cat> list = serverlevel.getEntitiesOfClass(Cat.class, (new AABB(blockpos)).inflate(16.0D, 8.0D, 16.0D));
      return list.size() < 1 ? this.spawnCat(blockpos, serverlevel) : 0;
   }

   private int spawnCat(BlockPos blockpos, ServerLevel serverlevel) {
      Cat cat = EntityType.CAT.create(serverlevel);
      if (cat == null) {
         return 0;
      } else {
         cat.finalizeSpawn(serverlevel, serverlevel.getCurrentDifficultyAt(blockpos), MobSpawnType.NATURAL, (SpawnGroupData)null, (CompoundTag)null);
         cat.moveTo(blockpos, 0.0F, 0.0F);
         serverlevel.addFreshEntityWithPassengers(cat);
         return 1;
      }
   }
}
