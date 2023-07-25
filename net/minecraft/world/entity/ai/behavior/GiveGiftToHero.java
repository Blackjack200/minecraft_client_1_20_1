package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class GiveGiftToHero extends Behavior<Villager> {
   private static final int THROW_GIFT_AT_DISTANCE = 5;
   private static final int MIN_TIME_BETWEEN_GIFTS = 600;
   private static final int MAX_TIME_BETWEEN_GIFTS = 6600;
   private static final int TIME_TO_DELAY_FOR_HEAD_TO_FINISH_TURNING = 20;
   private static final Map<VillagerProfession, ResourceLocation> GIFTS = Util.make(Maps.newHashMap(), (hashmap) -> {
      hashmap.put(VillagerProfession.ARMORER, BuiltInLootTables.ARMORER_GIFT);
      hashmap.put(VillagerProfession.BUTCHER, BuiltInLootTables.BUTCHER_GIFT);
      hashmap.put(VillagerProfession.CARTOGRAPHER, BuiltInLootTables.CARTOGRAPHER_GIFT);
      hashmap.put(VillagerProfession.CLERIC, BuiltInLootTables.CLERIC_GIFT);
      hashmap.put(VillagerProfession.FARMER, BuiltInLootTables.FARMER_GIFT);
      hashmap.put(VillagerProfession.FISHERMAN, BuiltInLootTables.FISHERMAN_GIFT);
      hashmap.put(VillagerProfession.FLETCHER, BuiltInLootTables.FLETCHER_GIFT);
      hashmap.put(VillagerProfession.LEATHERWORKER, BuiltInLootTables.LEATHERWORKER_GIFT);
      hashmap.put(VillagerProfession.LIBRARIAN, BuiltInLootTables.LIBRARIAN_GIFT);
      hashmap.put(VillagerProfession.MASON, BuiltInLootTables.MASON_GIFT);
      hashmap.put(VillagerProfession.SHEPHERD, BuiltInLootTables.SHEPHERD_GIFT);
      hashmap.put(VillagerProfession.TOOLSMITH, BuiltInLootTables.TOOLSMITH_GIFT);
      hashmap.put(VillagerProfession.WEAPONSMITH, BuiltInLootTables.WEAPONSMITH_GIFT);
   });
   private static final float SPEED_MODIFIER = 0.5F;
   private int timeUntilNextGift = 600;
   private boolean giftGivenDuringThisRun;
   private long timeSinceStart;

   public GiveGiftToHero(int i) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.VALUE_PRESENT), i);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Villager villager) {
      if (!this.isHeroVisible(villager)) {
         return false;
      } else if (this.timeUntilNextGift > 0) {
         --this.timeUntilNextGift;
         return false;
      } else {
         return true;
      }
   }

   protected void start(ServerLevel serverlevel, Villager villager, long i) {
      this.giftGivenDuringThisRun = false;
      this.timeSinceStart = i;
      Player player = this.getNearestTargetableHero(villager).get();
      villager.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, player);
      BehaviorUtils.lookAtEntity(villager, player);
   }

   protected boolean canStillUse(ServerLevel serverlevel, Villager villager, long i) {
      return this.isHeroVisible(villager) && !this.giftGivenDuringThisRun;
   }

   protected void tick(ServerLevel serverlevel, Villager villager, long i) {
      Player player = this.getNearestTargetableHero(villager).get();
      BehaviorUtils.lookAtEntity(villager, player);
      if (this.isWithinThrowingDistance(villager, player)) {
         if (i - this.timeSinceStart > 20L) {
            this.throwGift(villager, player);
            this.giftGivenDuringThisRun = true;
         }
      } else {
         BehaviorUtils.setWalkAndLookTargetMemories(villager, player, 0.5F, 5);
      }

   }

   protected void stop(ServerLevel serverlevel, Villager villager, long i) {
      this.timeUntilNextGift = calculateTimeUntilNextGift(serverlevel);
      villager.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   private void throwGift(Villager villager, LivingEntity livingentity) {
      for(ItemStack itemstack : this.getItemToThrow(villager)) {
         BehaviorUtils.throwItem(villager, itemstack, livingentity.position());
      }

   }

   private List<ItemStack> getItemToThrow(Villager villager) {
      if (villager.isBaby()) {
         return ImmutableList.of(new ItemStack(Items.POPPY));
      } else {
         VillagerProfession villagerprofession = villager.getVillagerData().getProfession();
         if (GIFTS.containsKey(villagerprofession)) {
            LootTable loottable = villager.level().getServer().getLootData().getLootTable(GIFTS.get(villagerprofession));
            LootParams lootparams = (new LootParams.Builder((ServerLevel)villager.level())).withParameter(LootContextParams.ORIGIN, villager.position()).withParameter(LootContextParams.THIS_ENTITY, villager).create(LootContextParamSets.GIFT);
            return loottable.getRandomItems(lootparams);
         } else {
            return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
         }
      }
   }

   private boolean isHeroVisible(Villager villager) {
      return this.getNearestTargetableHero(villager).isPresent();
   }

   private Optional<Player> getNearestTargetableHero(Villager villager) {
      return villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
   }

   private boolean isHero(Player player) {
      return player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE);
   }

   private boolean isWithinThrowingDistance(Villager villager, Player player) {
      BlockPos blockpos = player.blockPosition();
      BlockPos blockpos1 = villager.blockPosition();
      return blockpos1.closerThan(blockpos, 5.0D);
   }

   private static int calculateTimeUntilNextGift(ServerLevel serverlevel) {
      return 600 + serverlevel.random.nextInt(6001);
   }
}
