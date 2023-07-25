package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
   private static final int CHECK_TICK_DELAY = 5;
   private int openCount;

   protected abstract void onOpen(Level level, BlockPos blockpos, BlockState blockstate);

   protected abstract void onClose(Level level, BlockPos blockpos, BlockState blockstate);

   protected abstract void openerCountChanged(Level level, BlockPos blockpos, BlockState blockstate, int i, int j);

   protected abstract boolean isOwnContainer(Player player);

   public void incrementOpeners(Player player, Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.openCount++;
      if (i == 0) {
         this.onOpen(level, blockpos, blockstate);
         level.gameEvent(player, GameEvent.CONTAINER_OPEN, blockpos);
         scheduleRecheck(level, blockpos, blockstate);
      }

      this.openerCountChanged(level, blockpos, blockstate, i, this.openCount);
   }

   public void decrementOpeners(Player player, Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.openCount--;
      if (this.openCount == 0) {
         this.onClose(level, blockpos, blockstate);
         level.gameEvent(player, GameEvent.CONTAINER_CLOSE, blockpos);
      }

      this.openerCountChanged(level, blockpos, blockstate, i, this.openCount);
   }

   private int getOpenCount(Level level, BlockPos blockpos) {
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      float f = 5.0F;
      AABB aabb = new AABB((double)((float)i - 5.0F), (double)((float)j - 5.0F), (double)((float)k - 5.0F), (double)((float)(i + 1) + 5.0F), (double)((float)(j + 1) + 5.0F), (double)((float)(k + 1) + 5.0F));
      return level.getEntities(EntityTypeTest.forClass(Player.class), aabb, this::isOwnContainer).size();
   }

   public void recheckOpeners(Level level, BlockPos blockpos, BlockState blockstate) {
      int i = this.getOpenCount(level, blockpos);
      int j = this.openCount;
      if (j != i) {
         boolean flag = i != 0;
         boolean flag1 = j != 0;
         if (flag && !flag1) {
            this.onOpen(level, blockpos, blockstate);
            level.gameEvent((Entity)null, GameEvent.CONTAINER_OPEN, blockpos);
         } else if (!flag) {
            this.onClose(level, blockpos, blockstate);
            level.gameEvent((Entity)null, GameEvent.CONTAINER_CLOSE, blockpos);
         }

         this.openCount = i;
      }

      this.openerCountChanged(level, blockpos, blockstate, j, i);
      if (i > 0) {
         scheduleRecheck(level, blockpos, blockstate);
      }

   }

   public int getOpenerCount() {
      return this.openCount;
   }

   private static void scheduleRecheck(Level level, BlockPos blockpos, BlockState blockstate) {
      level.scheduleTick(blockpos, blockstate.getBlock(), 5);
   }
}
