package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager extends Behavior<Villager> {
   private static final int INTERACT_DIST_SQR = 5;
   private static final float SPEED_MODIFIER = 0.5F;
   private Set<Item> trades = ImmutableSet.of();

   public TradeWithVillager() {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      return BehaviorUtils.targetIsValid(villager.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.checkExtraStartConditions(serverlevel, villager);
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      Villager villager1 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager1, 0.5F);
      this.trades = figureOutWhatIAmWillingToTrade(villager, villager1);
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      Villager villager1 = (Villager)villager.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      if (!(villager.distanceToSqr(villager1) > 5.0D)) {
         BehaviorUtils.lockGazeAndWalkToEachOther(villager, villager1, 0.5F);
         villager.gossip(serverlevel, villager1, i);
         if (villager.hasExcessFood() && (villager.getVillagerData().getProfession() == VillagerProfession.FARMER || villager1.wantsMoreFood())) {
            throwHalfStack(villager, Villager.FOOD_POINTS.keySet(), villager1);
         }

         if (villager1.getVillagerData().getProfession() == VillagerProfession.FARMER && villager.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getMaxStackSize() / 2) {
            throwHalfStack(villager, ImmutableSet.of(Items.WHEAT), villager1);
         }

         if (!this.trades.isEmpty() && villager.getInventory().hasAnyOf(this.trades)) {
            throwHalfStack(villager, this.trades, villager1);
         }

      }
   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
   }

   private static Set<Item> figureOutWhatIAmWillingToTrade(Villager villager, Villager villager1) {
      ImmutableSet<Item> immutableset = villager1.getVillagerData().getProfession().requestedItems();
      ImmutableSet<Item> immutableset1 = villager.getVillagerData().getProfession().requestedItems();
      return immutableset.stream().filter((item) -> !immutableset1.contains(item)).collect(Collectors.toSet());
   }

   private static void throwHalfStack(Villager villager, Set<Item> set, LivingEntity livingentity) {
      SimpleContainer simplecontainer = villager.getInventory();
      ItemStack itemstack = ItemStack.EMPTY;
      int i = 0;

      while(i < simplecontainer.getContainerSize()) {
         ItemStack itemstack1;
         Item item;
         int j;
         label28: {
            itemstack1 = simplecontainer.getItem(i);
            if (!itemstack1.isEmpty()) {
               item = itemstack1.getItem();
               if (set.contains(item)) {
                  if (itemstack1.getCount() > itemstack1.getMaxStackSize() / 2) {
                     j = itemstack1.getCount() / 2;
                     break label28;
                  }

                  if (itemstack1.getCount() > 24) {
                     j = itemstack1.getCount() - 24;
                     break label28;
                  }
               }
            }

            ++i;
            continue;
         }

         itemstack1.shrink(j);
         itemstack = new ItemStack(item, j);
         break;
      }

      if (!itemstack.isEmpty()) {
         BehaviorUtils.throwItem(villager, itemstack, livingentity.position());
      }

   }
}
