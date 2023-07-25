package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;

public interface LevelAccessor extends CommonLevelAccessor, LevelTimeAccess {
   default long dayTime() {
      return this.getLevelData().getDayTime();
   }

   long nextSubTickCount();

   LevelTickAccess<Block> getBlockTicks();

   private <T> ScheduledTick<T> createTick(BlockPos blockpos, T object, int i, TickPriority tickpriority) {
      return new ScheduledTick<>(object, blockpos, this.getLevelData().getGameTime() + (long)i, tickpriority, this.nextSubTickCount());
   }

   private <T> ScheduledTick<T> createTick(BlockPos blockpos, T object, int i) {
      return new ScheduledTick<>(object, blockpos, this.getLevelData().getGameTime() + (long)i, this.nextSubTickCount());
   }

   default void scheduleTick(BlockPos blockpos, Block block, int i, TickPriority tickpriority) {
      this.getBlockTicks().schedule(this.createTick(blockpos, block, i, tickpriority));
   }

   default void scheduleTick(BlockPos blockpos, Block block, int i) {
      this.getBlockTicks().schedule(this.createTick(blockpos, block, i));
   }

   LevelTickAccess<Fluid> getFluidTicks();

   default void scheduleTick(BlockPos blockpos, Fluid fluid, int i, TickPriority tickpriority) {
      this.getFluidTicks().schedule(this.createTick(blockpos, fluid, i, tickpriority));
   }

   default void scheduleTick(BlockPos blockpos, Fluid fluid, int i) {
      this.getFluidTicks().schedule(this.createTick(blockpos, fluid, i));
   }

   LevelData getLevelData();

   DifficultyInstance getCurrentDifficultyAt(BlockPos blockpos);

   @Nullable
   MinecraftServer getServer();

   default Difficulty getDifficulty() {
      return this.getLevelData().getDifficulty();
   }

   ChunkSource getChunkSource();

   default boolean hasChunk(int i, int j) {
      return this.getChunkSource().hasChunk(i, j);
   }

   RandomSource getRandom();

   default void blockUpdated(BlockPos blockpos, Block block) {
   }

   default void neighborShapeChanged(Direction direction, BlockState blockstate, BlockPos blockpos, BlockPos blockpos1, int i, int j) {
      NeighborUpdater.executeShapeUpdate(this, direction, blockstate, blockpos, blockpos1, i, j - 1);
   }

   default void playSound(@Nullable Player player, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource) {
      this.playSound(player, blockpos, soundevent, soundsource, 1.0F, 1.0F);
   }

   void playSound(@Nullable Player player, BlockPos blockpos, SoundEvent soundevent, SoundSource soundsource, float f, float f1);

   void addParticle(ParticleOptions particleoptions, double d0, double d1, double d2, double d3, double d4, double d5);

   void levelEvent(@Nullable Player player, int i, BlockPos blockpos, int j);

   default void levelEvent(int i, BlockPos blockpos, int j) {
      this.levelEvent((Player)null, i, blockpos, j);
   }

   void gameEvent(GameEvent gameevent, Vec3 vec3, GameEvent.Context gameevent_context);

   default void gameEvent(@Nullable Entity entity, GameEvent gameevent, Vec3 vec3) {
      this.gameEvent(gameevent, vec3, new GameEvent.Context(entity, (BlockState)null));
   }

   default void gameEvent(@Nullable Entity entity, GameEvent gameevent, BlockPos blockpos) {
      this.gameEvent(gameevent, blockpos, new GameEvent.Context(entity, (BlockState)null));
   }

   default void gameEvent(GameEvent gameevent, BlockPos blockpos, GameEvent.Context gameevent_context) {
      this.gameEvent(gameevent, Vec3.atCenterOf(blockpos), gameevent_context);
   }
}
