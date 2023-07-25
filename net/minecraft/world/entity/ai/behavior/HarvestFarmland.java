package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HarvestFarmland extends Behavior<Villager> {
   private static final int HARVEST_DURATION = 200;
   public static final float SPEED_MODIFIER = 0.5F;
   @Nullable
   private BlockPos aboveFarmlandPos;
   private long nextOkStartTime;
   private int timeWorkedSoFar;
   private final List<BlockPos> validFarmlandAroundVillager = Lists.newArrayList();

   public HarvestFarmland() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.SECONDARY_JOB_SITE, MemoryStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      if (!serverlevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
         return false;
      } else if (villager.getVillagerData().getProfession() != VillagerProfession.FARMER) {
         return false;
      } else {
         BlockPos.MutableBlockPos blockpos_mutableblockpos = villager.blockPosition().mutable();
         this.validFarmlandAroundVillager.clear();

         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  blockpos_mutableblockpos.set(villager.getX() + (double)i, villager.getY() + (double)j, villager.getZ() + (double)k);
                  if (this.validPos(blockpos_mutableblockpos, serverlevel)) {
                     this.validFarmlandAroundVillager.add(new BlockPos(blockpos_mutableblockpos));
                  }
               }
            }
         }

         this.aboveFarmlandPos = this.getValidFarmland(serverlevel);
         return this.aboveFarmlandPos != null;
      }
   }

   @Nullable
   private BlockPos getValidFarmland(ServerLevel serverlevel) {
      return this.validFarmlandAroundVillager.isEmpty() ? null : this.validFarmlandAroundVillager.get(serverlevel.getRandom().nextInt(this.validFarmlandAroundVillager.size()));
   }

   private boolean validPos(BlockPos blockpos, ServerLevel serverlevel) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      Block block1 = serverlevel.getBlockState(blockpos.below()).getBlock();
      return block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockstate) || blockstate.isAir() && block1 instanceof FarmBlock;
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      if (i > this.nextOkStartTime && this.aboveFarmlandPos != null) {
         villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
         villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
      }

   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      this.timeWorkedSoFar = 0;
      this.nextOkStartTime = i + 40L;
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerToCenterThan(villager.position(), 1.0D)) {
         if (this.aboveFarmlandPos != null && i > this.nextOkStartTime) {
            BlockState blockstate = serverlevel.getBlockState(this.aboveFarmlandPos);
            Block block = blockstate.getBlock();
            Block block1 = serverlevel.getBlockState(this.aboveFarmlandPos.below()).getBlock();
            if (block instanceof CropBlock && ((CropBlock)block).isMaxAge(blockstate)) {
               serverlevel.destroyBlock(this.aboveFarmlandPos, true, villager);
            }

            if (blockstate.isAir() && block1 instanceof FarmBlock && villager.hasFarmSeeds()) {
               SimpleContainer simplecontainer = villager.getInventory();

               for(int j = 0; j < simplecontainer.getContainerSize(); ++j) {
                  ItemStack itemstack = simplecontainer.getItem(j);
                  boolean flag = false;
                  if (!itemstack.isEmpty() && itemstack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
                     Item blockstate1 = itemstack.getItem();
                     if (blockstate1 instanceof BlockItem) {
                        BlockItem blockitem = (BlockItem)blockstate1;
                        BlockState blockstate1 = blockitem.getBlock().defaultBlockState();
                        serverlevel.setBlockAndUpdate(this.aboveFarmlandPos, blockstate1);
                        serverlevel.gameEvent(GameEvent.BLOCK_PLACE, this.aboveFarmlandPos, GameEvent.Context.of(villager, blockstate1));
                        flag = true;
                     }
                  }

                  if (flag) {
                     serverlevel.playSound((Player)null, (double)this.aboveFarmlandPos.getX(), (double)this.aboveFarmlandPos.getY(), (double)this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                     itemstack.shrink(1);
                     if (itemstack.isEmpty()) {
                        simplecontainer.setItem(j, ItemStack.EMPTY);
                     }
                     break;
                  }
               }
            }

            if (block instanceof CropBlock && !((CropBlock)block).isMaxAge(blockstate)) {
               this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
               this.aboveFarmlandPos = this.getValidFarmland(serverlevel);
               if (this.aboveFarmlandPos != null) {
                  this.nextOkStartTime = i + 20L;
                  villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(this.aboveFarmlandPos), 0.5F, 1));
                  villager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.aboveFarmlandPos));
               }
            }
         }

         ++this.timeWorkedSoFar;
      }
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.timeWorkedSoFar < 200;
   }
}
