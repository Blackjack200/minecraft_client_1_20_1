package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity {
   private final BaseSpawner spawner = new BaseSpawner() {
      public void broadcastEvent(Level level, BlockPos blockpos, int i) {
         level.blockEvent(blockpos, Blocks.SPAWNER, i, 0);
      }

      public void setNextSpawnData(@Nullable Level level, BlockPos blockpos, SpawnData spawndata) {
         super.setNextSpawnData(level, blockpos, spawndata);
         if (level != null) {
            BlockState blockstate = level.getBlockState(blockpos);
            level.sendBlockUpdated(blockpos, blockstate, blockstate, 4);
         }

      }
   };

   public SpawnerBlockEntity(BlockPos blockpos, BlockState blockstate) {
      super(BlockEntityType.MOB_SPAWNER, blockpos, blockstate);
   }

   public void load(CompoundTag compoundtag) {
      super.load(compoundtag);
      this.spawner.load(this.level, this.worldPosition, compoundtag);
   }

   protected void saveAdditional(CompoundTag compoundtag) {
      super.saveAdditional(compoundtag);
      this.spawner.save(compoundtag);
   }

   public static void clientTick(Level level, BlockPos blockpos, BlockState blockstate, SpawnerBlockEntity spawnerblockentity) {
      spawnerblockentity.spawner.clientTick(level, blockpos);
   }

   public static void serverTick(Level level, BlockPos blockpos, BlockState blockstate, SpawnerBlockEntity spawnerblockentity) {
      spawnerblockentity.spawner.serverTick((ServerLevel)level, blockpos);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public CompoundTag getUpdateTag() {
      CompoundTag compoundtag = this.saveWithoutMetadata();
      compoundtag.remove("SpawnPotentials");
      return compoundtag;
   }

   public boolean triggerEvent(int i, int j) {
      return this.spawner.onEventTriggered(this.level, i) ? true : super.triggerEvent(i, j);
   }

   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public void setEntityId(EntityType<?> entitytype, RandomSource randomsource) {
      this.spawner.setEntityId(entitytype, this.level, randomsource, this.worldPosition);
   }

   public BaseSpawner getSpawner() {
      return this.spawner;
   }
}
