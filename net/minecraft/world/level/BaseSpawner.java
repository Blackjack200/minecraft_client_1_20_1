package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public abstract class BaseSpawner {
   public static final String SPAWN_DATA_TAG = "SpawnData";
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int EVENT_SPAWN = 1;
   private int spawnDelay = 20;
   private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
   @Nullable
   private SpawnData nextSpawnData;
   private double spin;
   private double oSpin;
   private int minSpawnDelay = 200;
   private int maxSpawnDelay = 800;
   private int spawnCount = 4;
   @Nullable
   private Entity displayEntity;
   private int maxNearbyEntities = 6;
   private int requiredPlayerRange = 16;
   private int spawnRange = 4;

   public void setEntityId(EntityType<?> entitytype, @Nullable Level level, RandomSource randomsource, BlockPos blockpos) {
      this.getOrCreateNextSpawnData(level, randomsource, blockpos).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entitytype).toString());
   }

   private boolean isNearPlayer(Level level, BlockPos blockpos) {
      return level.hasNearbyAlivePlayer((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.5D, (double)blockpos.getZ() + 0.5D, (double)this.requiredPlayerRange);
   }

   public void clientTick(Level level, BlockPos blockpos) {
      if (!this.isNearPlayer(level, blockpos)) {
         this.oSpin = this.spin;
      } else if (this.displayEntity != null) {
         RandomSource randomsource = level.getRandom();
         double d0 = (double)blockpos.getX() + randomsource.nextDouble();
         double d1 = (double)blockpos.getY() + randomsource.nextDouble();
         double d2 = (double)blockpos.getZ() + randomsource.nextDouble();
         level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         level.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         if (this.spawnDelay > 0) {
            --this.spawnDelay;
         }

         this.oSpin = this.spin;
         this.spin = (this.spin + (double)(1000.0F / ((float)this.spawnDelay + 200.0F))) % 360.0D;
      }

   }

   public void serverTick(ServerLevel serverlevel, BlockPos blockpos) {
      if (this.isNearPlayer(serverlevel, blockpos)) {
         if (this.spawnDelay == -1) {
            this.delay(serverlevel, blockpos);
         }

         if (this.spawnDelay > 0) {
            --this.spawnDelay;
         } else {
            boolean flag = false;
            RandomSource randomsource = serverlevel.getRandom();
            SpawnData spawndata = this.getOrCreateNextSpawnData(serverlevel, randomsource, blockpos);

            for(int i = 0; i < this.spawnCount; ++i) {
               CompoundTag compoundtag = spawndata.getEntityToSpawn();
               Optional<EntityType<?>> optional = EntityType.by(compoundtag);
               if (optional.isEmpty()) {
                  this.delay(serverlevel, blockpos);
                  return;
               }

               ListTag listtag = compoundtag.getList("Pos", 6);
               int j = listtag.size();
               double d0 = j >= 1 ? listtag.getDouble(0) : (double)blockpos.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.spawnRange + 0.5D;
               double d1 = j >= 2 ? listtag.getDouble(1) : (double)(blockpos.getY() + randomsource.nextInt(3) - 1);
               double d2 = j >= 3 ? listtag.getDouble(2) : (double)blockpos.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double)this.spawnRange + 0.5D;
               if (serverlevel.noCollision(optional.get().getAABB(d0, d1, d2))) {
                  BlockPos blockpos1 = BlockPos.containing(d0, d1, d2);
                  if (spawndata.getCustomSpawnRules().isPresent()) {
                     if (!optional.get().getCategory().isFriendly() && serverlevel.getDifficulty() == Difficulty.PEACEFUL) {
                        continue;
                     }

                     SpawnData.CustomSpawnRules spawndata_customspawnrules = spawndata.getCustomSpawnRules().get();
                     if (!spawndata_customspawnrules.blockLightLimit().isValueInRange(serverlevel.getBrightness(LightLayer.BLOCK, blockpos1)) || !spawndata_customspawnrules.skyLightLimit().isValueInRange(serverlevel.getBrightness(LightLayer.SKY, blockpos1))) {
                        continue;
                     }
                  } else if (!SpawnPlacements.checkSpawnRules(optional.get(), serverlevel, MobSpawnType.SPAWNER, blockpos1, serverlevel.getRandom())) {
                     continue;
                  }

                  Entity entity = EntityType.loadEntityRecursive(compoundtag, serverlevel, (entity1) -> {
                     entity1.moveTo(d0, d1, d2, entity1.getYRot(), entity1.getXRot());
                     return entity1;
                  });
                  if (entity == null) {
                     this.delay(serverlevel, blockpos);
                     return;
                  }

                  int k = serverlevel.getEntitiesOfClass(entity.getClass(), (new AABB((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), (double)(blockpos.getX() + 1), (double)(blockpos.getY() + 1), (double)(blockpos.getZ() + 1))).inflate((double)this.spawnRange)).size();
                  if (k >= this.maxNearbyEntities) {
                     this.delay(serverlevel, blockpos);
                     return;
                  }

                  entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), randomsource.nextFloat() * 360.0F, 0.0F);
                  if (entity instanceof Mob) {
                     Mob mob = (Mob)entity;
                     if (spawndata.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(serverlevel, MobSpawnType.SPAWNER) || !mob.checkSpawnObstruction(serverlevel)) {
                        continue;
                     }

                     if (spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8)) {
                        ((Mob)entity).finalizeSpawn(serverlevel, serverlevel.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, (SpawnGroupData)null, (CompoundTag)null);
                     }
                  }

                  if (!serverlevel.tryAddFreshEntityWithPassengers(entity)) {
                     this.delay(serverlevel, blockpos);
                     return;
                  }

                  serverlevel.levelEvent(2004, blockpos, 0);
                  serverlevel.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos1);
                  if (entity instanceof Mob) {
                     ((Mob)entity).spawnAnim();
                  }

                  flag = true;
               }
            }

            if (flag) {
               this.delay(serverlevel, blockpos);
            }

         }
      }
   }

   private void delay(Level level, BlockPos blockpos) {
      RandomSource randomsource = level.random;
      if (this.maxSpawnDelay <= this.minSpawnDelay) {
         this.spawnDelay = this.minSpawnDelay;
      } else {
         this.spawnDelay = this.minSpawnDelay + randomsource.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
      }

      this.spawnPotentials.getRandom(randomsource).ifPresent((weightedentry_wrapper) -> this.setNextSpawnData(level, blockpos, weightedentry_wrapper.getData()));
      this.broadcastEvent(level, blockpos, 1);
   }

   public void load(@Nullable Level level, BlockPos blockpos, CompoundTag compoundtag) {
      this.spawnDelay = compoundtag.getShort("Delay");
      boolean flag = compoundtag.contains("SpawnData", 10);
      if (flag) {
         SpawnData spawndata = SpawnData.CODEC.parse(NbtOps.INSTANCE, compoundtag.getCompound("SpawnData")).resultOrPartial((s1) -> LOGGER.warn("Invalid SpawnData: {}", (Object)s1)).orElseGet(SpawnData::new);
         this.setNextSpawnData(level, blockpos, spawndata);
      }

      boolean flag1 = compoundtag.contains("SpawnPotentials", 9);
      if (flag1) {
         ListTag listtag = compoundtag.getList("SpawnPotentials", 10);
         this.spawnPotentials = SpawnData.LIST_CODEC.parse(NbtOps.INSTANCE, listtag).resultOrPartial((s) -> LOGGER.warn("Invalid SpawnPotentials list: {}", (Object)s)).orElseGet(SimpleWeightedRandomList::empty);
      } else {
         this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData());
      }

      if (compoundtag.contains("MinSpawnDelay", 99)) {
         this.minSpawnDelay = compoundtag.getShort("MinSpawnDelay");
         this.maxSpawnDelay = compoundtag.getShort("MaxSpawnDelay");
         this.spawnCount = compoundtag.getShort("SpawnCount");
      }

      if (compoundtag.contains("MaxNearbyEntities", 99)) {
         this.maxNearbyEntities = compoundtag.getShort("MaxNearbyEntities");
         this.requiredPlayerRange = compoundtag.getShort("RequiredPlayerRange");
      }

      if (compoundtag.contains("SpawnRange", 99)) {
         this.spawnRange = compoundtag.getShort("SpawnRange");
      }

      this.displayEntity = null;
   }

   public CompoundTag save(CompoundTag compoundtag) {
      compoundtag.putShort("Delay", (short)this.spawnDelay);
      compoundtag.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
      compoundtag.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
      compoundtag.putShort("SpawnCount", (short)this.spawnCount);
      compoundtag.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
      compoundtag.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
      compoundtag.putShort("SpawnRange", (short)this.spawnRange);
      if (this.nextSpawnData != null) {
         compoundtag.put("SpawnData", SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, this.nextSpawnData).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData")));
      }

      compoundtag.put("SpawnPotentials", SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
      return compoundtag;
   }

   @Nullable
   public Entity getOrCreateDisplayEntity(Level level, RandomSource randomsource, BlockPos blockpos) {
      if (this.displayEntity == null) {
         CompoundTag compoundtag = this.getOrCreateNextSpawnData(level, randomsource, blockpos).getEntityToSpawn();
         if (!compoundtag.contains("id", 8)) {
            return null;
         }

         this.displayEntity = EntityType.loadEntityRecursive(compoundtag, level, Function.identity());
         if (compoundtag.size() == 1 && this.displayEntity instanceof Mob) {
         }
      }

      return this.displayEntity;
   }

   public boolean onEventTriggered(Level level, int i) {
      if (i == 1) {
         if (level.isClientSide) {
            this.spawnDelay = this.minSpawnDelay;
         }

         return true;
      } else {
         return false;
      }
   }

   protected void setNextSpawnData(@Nullable Level level, BlockPos blockpos, SpawnData spawndata) {
      this.nextSpawnData = spawndata;
   }

   private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource randomsource, BlockPos blockpos) {
      if (this.nextSpawnData != null) {
         return this.nextSpawnData;
      } else {
         this.setNextSpawnData(level, blockpos, this.spawnPotentials.getRandom(randomsource).map(WeightedEntry.Wrapper::getData).orElseGet(SpawnData::new));
         return this.nextSpawnData;
      }
   }

   public abstract void broadcastEvent(Level level, BlockPos blockpos, int i);

   public double getSpin() {
      return this.spin;
   }

   public double getoSpin() {
      return this.oSpin;
   }
}
