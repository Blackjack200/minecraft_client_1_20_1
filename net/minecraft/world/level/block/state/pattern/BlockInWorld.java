package net.minecraft.world.level.block.state.pattern;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInWorld {
   private final LevelReader level;
   private final BlockPos pos;
   private final boolean loadChunks;
   @Nullable
   private BlockState state;
   @Nullable
   private BlockEntity entity;
   private boolean cachedEntity;

   public BlockInWorld(LevelReader levelreader, BlockPos blockpos, boolean flag) {
      this.level = levelreader;
      this.pos = blockpos.immutable();
      this.loadChunks = flag;
   }

   public BlockState getState() {
      if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos))) {
         this.state = this.level.getBlockState(this.pos);
      }

      return this.state;
   }

   @Nullable
   public BlockEntity getEntity() {
      if (this.entity == null && !this.cachedEntity) {
         this.entity = this.level.getBlockEntity(this.pos);
         this.cachedEntity = true;
      }

      return this.entity;
   }

   public LevelReader getLevel() {
      return this.level;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public static Predicate<BlockInWorld> hasState(Predicate<BlockState> predicate) {
      return (blockinworld) -> blockinworld != null && predicate.test(blockinworld.getState());
   }
}
