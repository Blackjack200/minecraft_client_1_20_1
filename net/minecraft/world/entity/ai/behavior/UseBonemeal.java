package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class UseBonemeal extends Behavior<Villager> {
   private static final int BONEMEALING_DURATION = 80;
   private long nextWorkCycleTime;
   private long lastBonemealingSession;
   private int timeWorkedSoFar;
   private Optional<BlockPos> cropPos = Optional.empty();

   public UseBonemeal() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      if (villager.tickCount % 10 == 0 && (this.lastBonemealingSession == 0L || this.lastBonemealingSession + 160L <= (long)villager.tickCount)) {
         if (villager.getInventory().countItem(Items.BONE_MEAL) <= 0) {
            return false;
         } else {
            this.cropPos = this.pickNextTarget(serverlevel, villager);
            return this.cropPos.isPresent();
         }
      } else {
         return false;
      }
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.timeWorkedSoFar < 80 && this.cropPos.isPresent();
   }

   private Optional<BlockPos> pickNextTarget(ServerLevel serverlevel, Villager villager) {
      BlockPos.MutableBlockPos blockpos_mutableblockpos = new BlockPos.MutableBlockPos();
      Optional<BlockPos> optional = Optional.empty();
      int i = 0;

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               blockpos_mutableblockpos.setWithOffset(villager.blockPosition(), j, k, l);
               if (this.validPos(blockpos_mutableblockpos, serverlevel)) {
                  ++i;
                  if (serverlevel.random.nextInt(i) == 0) {
                     optional = Optional.of(blockpos_mutableblockpos.immutable());
                  }
               }
            }
         }
      }

      return optional;
   }

   private boolean validPos(BlockPos blockpos, ServerLevel serverlevel) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      return block instanceof CropBlock && !((CropBlock)block).isMaxAge(blockstate);
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      this.setCurrentCropAsTarget(villager);
      villager.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BONE_MEAL));
      this.nextWorkCycleTime = i;
      this.timeWorkedSoFar = 0;
   }

   private void setCurrentCropAsTarget(Villager villager) {
      this.cropPos.ifPresent((blockpos) -> {
         BlockPosTracker blockpostracker = new BlockPosTracker(blockpos);
         villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockpostracker);
         villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockpostracker, 0.5F, 1));
      });
   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      this.lastBonemealingSession = (long)villager.tickCount;
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      BlockPos blockpos = this.cropPos.get();
      if (i >= this.nextWorkCycleTime && blockpos.closerToCenterThan(villager.position(), 1.0D)) {
         ItemStack itemstack = ItemStack.EMPTY;
         SimpleContainer simplecontainer = villager.getInventory();
         int j = simplecontainer.getContainerSize();

         for(int k = 0; k < j; ++k) {
            ItemStack itemstack1 = simplecontainer.getItem(k);
            if (itemstack1.is(Items.BONE_MEAL)) {
               itemstack = itemstack1;
               break;
            }
         }

         if (!itemstack.isEmpty() && BoneMealItem.growCrop(itemstack, serverlevel, blockpos)) {
            serverlevel.levelEvent(1505, blockpos, 0);
            this.cropPos = this.pickNextTarget(serverlevel, villager);
            this.setCurrentCropAsTarget(villager);
            this.nextWorkCycleTime = i + 40L;
         }

         ++this.timeWorkedSoFar;
      }
   }
}
