package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShowTradesToPlayer extends Behavior<Villager> {
   private static final int MAX_LOOK_TIME = 900;
   private static final int STARTING_LOOK_TIME = 40;
   @Nullable
   private ItemStack playerItemStack;
   private final List<ItemStack> displayItems = Lists.newArrayList();
   private int cycleCounter;
   private int displayIndex;
   private int lookTime;

   public ShowTradesToPlayer(int i, int j) {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT), i, j);
   }

   public boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      Brain<?> brain = villager.getBrain();
      if (!brain.getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent()) {
         return false;
      } else {
         LivingEntity livingentity = brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
         return livingentity.getType() == EntityType.PLAYER && villager.isAlive() && livingentity.isAlive() && !villager.isBaby() && villager.distanceToSqr(livingentity) <= 17.0D;
      }
   }

   public boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.checkExtraStartConditions(serverlevel, villager) && this.lookTime > 0 && villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).isPresent();
   }

   public void start(ServerLevel serverlevel, Villager villager, long i) {
      super.start(serverlevel, villager, i);
      this.lookAtTarget(villager);
      this.cycleCounter = 0;
      this.displayIndex = 0;
      this.lookTime = 40;
   }

   public void tick(ServerLevel serverlevel, Villager villager, long i) {
      LivingEntity livingentity = this.lookAtTarget(villager);
      this.findItemsToDisplay(livingentity, villager);
      if (!this.displayItems.isEmpty()) {
         this.displayCyclingItems(villager);
      } else {
         clearHeldItem(villager);
         this.lookTime = Math.min(this.lookTime, 40);
      }

      --this.lookTime;
   }

   public void stop(ServerLevel serverlevel, Villager villager, long i) {
      super.stop(serverlevel, villager, i);
      villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      clearHeldItem(villager);
      this.playerItemStack = null;
   }

   private void findItemsToDisplay(LivingEntity livingentity, Villager villager) {
      boolean flag = false;
      ItemStack itemstack = livingentity.getMainHandItem();
      if (this.playerItemStack == null || !ItemStack.isSameItem(this.playerItemStack, itemstack)) {
         this.playerItemStack = itemstack;
         flag = true;
         this.displayItems.clear();
      }

      if (flag && !this.playerItemStack.isEmpty()) {
         this.updateDisplayItems(villager);
         if (!this.displayItems.isEmpty()) {
            this.lookTime = 900;
            this.displayFirstItem(villager);
         }
      }

   }

   private void displayFirstItem(Villager villager) {
      displayAsHeldItem(villager, this.displayItems.get(0));
   }

   private void updateDisplayItems(Villager villager) {
      for(MerchantOffer merchantoffer : villager.getOffers()) {
         if (!merchantoffer.isOutOfStock() && this.playerItemStackMatchesCostOfOffer(merchantoffer)) {
            this.displayItems.add(merchantoffer.getResult());
         }
      }

   }

   private boolean playerItemStackMatchesCostOfOffer(MerchantOffer merchantoffer) {
      return ItemStack.isSameItem(this.playerItemStack, merchantoffer.getCostA()) || ItemStack.isSameItem(this.playerItemStack, merchantoffer.getCostB());
   }

   private static void clearHeldItem(Villager villager) {
      villager.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      villager.setDropChance(EquipmentSlot.MAINHAND, 0.085F);
   }

   private static void displayAsHeldItem(Villager villager, ItemStack itemstack) {
      villager.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
      villager.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
   }

   private LivingEntity lookAtTarget(Villager villager) {
      Brain<?> brain = villager.getBrain();
      LivingEntity livingentity = brain.getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingentity, true));
      return livingentity;
   }

   private void displayCyclingItems(Villager villager) {
      if (this.displayItems.size() >= 2 && ++this.cycleCounter >= 40) {
         ++this.displayIndex;
         this.cycleCounter = 0;
         if (this.displayIndex > this.displayItems.size() - 1) {
            this.displayIndex = 0;
         }

         displayAsHeldItem(villager, this.displayItems.get(this.displayIndex));
      }

   }
}
