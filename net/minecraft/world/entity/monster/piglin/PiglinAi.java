package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BackUpIfTooClose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.CopyMemoryWithExpiry;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.behavior.DismountOrSkipMounting;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.behavior.GoToTargetLocation;
import net.minecraft.world.entity.ai.behavior.GoToWantedItem;
import net.minecraft.world.entity.ai.behavior.InteractWith;
import net.minecraft.world.entity.ai.behavior.InteractWithDoor;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.Mount;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTargetSometimes;
import net.minecraft.world.entity.ai.behavior.SetLookAndInteract;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetAwayFrom;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromAttackTargetIfTargetOutOfReach;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.StartCelebratingIfTargetDead;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;
import net.minecraft.world.entity.ai.behavior.StopBeingAngryIfTargetDead;
import net.minecraft.world.entity.ai.behavior.TriggerGate;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.Trigger;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class PiglinAi {
   public static final int REPELLENT_DETECTION_RANGE_HORIZONTAL = 8;
   public static final int REPELLENT_DETECTION_RANGE_VERTICAL = 4;
   public static final Item BARTERING_ITEM = Items.GOLD_INGOT;
   private static final int PLAYER_ANGER_RANGE = 16;
   private static final int ANGER_DURATION = 600;
   private static final int ADMIRE_DURATION = 120;
   private static final int MAX_DISTANCE_TO_WALK_TO_ITEM = 9;
   private static final int MAX_TIME_TO_WALK_TO_ITEM = 200;
   private static final int HOW_LONG_TIME_TO_DISABLE_ADMIRE_WALKING_IF_CANT_REACH_ITEM = 200;
   private static final int CELEBRATION_TIME = 300;
   protected static final UniformInt TIME_BETWEEN_HUNTS = TimeUtil.rangeOfSeconds(30, 120);
   private static final int BABY_FLEE_DURATION_AFTER_GETTING_HIT = 100;
   private static final int HIT_BY_PLAYER_MEMORY_TIMEOUT = 400;
   private static final int MAX_WALK_DISTANCE_TO_START_RIDING = 8;
   private static final UniformInt RIDE_START_INTERVAL = TimeUtil.rangeOfSeconds(10, 40);
   private static final UniformInt RIDE_DURATION = TimeUtil.rangeOfSeconds(10, 30);
   private static final UniformInt RETREAT_DURATION = TimeUtil.rangeOfSeconds(5, 20);
   private static final int MELEE_ATTACK_COOLDOWN = 20;
   private static final int EAT_COOLDOWN = 200;
   private static final int DESIRED_DISTANCE_FROM_ENTITY_WHEN_AVOIDING = 12;
   private static final int MAX_LOOK_DIST = 8;
   private static final int MAX_LOOK_DIST_FOR_PLAYER_HOLDING_LOVED_ITEM = 14;
   private static final int INTERACTION_RANGE = 8;
   private static final int MIN_DESIRED_DIST_FROM_TARGET_WHEN_HOLDING_CROSSBOW = 5;
   private static final float SPEED_WHEN_STRAFING_BACK_FROM_TARGET = 0.75F;
   private static final int DESIRED_DISTANCE_FROM_ZOMBIFIED = 6;
   private static final UniformInt AVOID_ZOMBIFIED_DURATION = TimeUtil.rangeOfSeconds(5, 7);
   private static final UniformInt BABY_AVOID_NEMESIS_DURATION = TimeUtil.rangeOfSeconds(5, 7);
   private static final float PROBABILITY_OF_CELEBRATION_DANCE = 0.1F;
   private static final float SPEED_MULTIPLIER_WHEN_AVOIDING = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_MOUNTING = 0.8F;
   private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_WANTED_ITEM = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_GOING_TO_CELEBRATE_LOCATION = 1.0F;
   private static final float SPEED_MULTIPLIER_WHEN_DANCING = 0.6F;
   private static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.6F;

   protected static Brain<?> makeBrain(Piglin piglin, Brain<Piglin> brain) {
      initCoreActivity(brain);
      initIdleActivity(brain);
      initAdmireItemActivity(brain);
      initFightActivity(piglin, brain);
      initCelebrateActivity(brain);
      initRetreatActivity(brain);
      initRideHoglinActivity(brain);
      brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
      brain.setDefaultActivity(Activity.IDLE);
      brain.useDefaultActivity();
      return brain;
   }

   protected static void initMemories(Piglin piglin, RandomSource randomsource) {
      int i = TIME_BETWEEN_HUNTS.sample(randomsource);
      piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)i);
   }

   private static void initCoreActivity(Brain<Piglin> brain) {
      brain.addActivity(Activity.CORE, 0, ImmutableList.of(new LookAtTargetSink(45, 90), new MoveToTargetSink(), InteractWithDoor.create(), babyAvoidNemesis(), avoidZombified(), StopHoldingItemIfNoLongerAdmiring.create(), StartAdmiringItemIfSeen.create(120), StartCelebratingIfTargetDead.create(300, PiglinAi::wantsToDance), StopBeingAngryIfTargetDead.create()));
   }

   private static void initIdleActivity(Brain<Piglin> brain) {
      brain.addActivity(Activity.IDLE, 10, ImmutableList.of(SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F), StartAttacking.<Piglin>create(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf(Piglin::canHunt, StartHuntingHoglin.create()), avoidRepellent(), babySometimesRideBabyHoglin(), createIdleLookBehaviors(), createIdleMovementBehaviors(), SetLookAndInteract.create(EntityType.PLAYER, 4)));
   }

   private static void initFightActivity(Piglin piglin, Brain<Piglin> brain) {
      brain.addActivityAndRemoveMemoryWhenStopped(Activity.FIGHT, 10, ImmutableList.of(StopAttackingIfTargetInvalid.create((livingentity2) -> !isNearestValidAttackTarget(piglin, livingentity2)), BehaviorBuilder.triggerIf(PiglinAi::hasCrossbow, BackUpIfTooClose.create(5, 0.75F)), SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.0F), MeleeAttack.create(20), new CrossbowAttack(), RememberIfHoglinWasKilled.create(), EraseMemoryIf.create(PiglinAi::isNearZombified, MemoryModuleType.ATTACK_TARGET)), MemoryModuleType.ATTACK_TARGET);
   }

   private static void initCelebrateActivity(Brain<Piglin> brain) {
      brain.addActivityAndRemoveMemoryWhenStopped(Activity.CELEBRATE, 10, ImmutableList.of(avoidRepellent(), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 14.0F), StartAttacking.<Piglin>create(AbstractPiglin::isAdult, PiglinAi::findNearestValidAttackTarget), BehaviorBuilder.triggerIf((piglin) -> !piglin.isDancing(), GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 2, 1.0F)), BehaviorBuilder.triggerIf(Piglin::isDancing, GoToTargetLocation.create(MemoryModuleType.CELEBRATE_LOCATION, 4, 0.6F)), new RunOne<LivingEntity>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(RandomStroll.stroll(0.6F, 2, 1), 1), Pair.of(new DoNothing(10, 20), 1)))), MemoryModuleType.CELEBRATE_LOCATION);
   }

   private static void initAdmireItemActivity(Brain<Piglin> brain) {
      brain.addActivityAndRemoveMemoryWhenStopped(Activity.ADMIRE_ITEM, 10, ImmutableList.of(GoToWantedItem.create(PiglinAi::isNotHoldingLovedItemInOffHand, 1.0F, true, 9), StopAdmiringIfItemTooFarAway.create(9), StopAdmiringIfTiredOfTryingToReachItem.create(200, 200)), MemoryModuleType.ADMIRING_ITEM);
   }

   private static void initRetreatActivity(Brain<Piglin> brain) {
      brain.addActivityAndRemoveMemoryWhenStopped(Activity.AVOID, 10, ImmutableList.of(SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.0F, 12, true), createIdleLookBehaviors(), createIdleMovementBehaviors(), EraseMemoryIf.<PathfinderMob>create(PiglinAi::wantsToStopFleeing, MemoryModuleType.AVOID_TARGET)), MemoryModuleType.AVOID_TARGET);
   }

   private static void initRideHoglinActivity(Brain<Piglin> brain) {
      brain.addActivityAndRemoveMemoryWhenStopped(Activity.RIDE, 10, ImmutableList.of(Mount.create(0.8F), SetEntityLookTarget.create(PiglinAi::isPlayerHoldingLovedItem, 8.0F), BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(Entity::isPassenger), TriggerGate.triggerOneShuffled(ImmutableList.<Pair<? extends Trigger<? super LivingEntity>, Integer>>builder().addAll(createLookBehaviors()).add(Pair.of(BehaviorBuilder.triggerIf((piglin1) -> true), 1)).build())), DismountOrSkipMounting.<LivingEntity>create(8, PiglinAi::wantsToStopRiding)), MemoryModuleType.RIDE_TARGET);
   }

   private static ImmutableList<Pair<OneShot<LivingEntity>, Integer>> createLookBehaviors() {
      return ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 1), Pair.of(SetEntityLookTarget.create(EntityType.PIGLIN, 8.0F), 1), Pair.of(SetEntityLookTarget.create(8.0F), 1));
   }

   private static RunOne<LivingEntity> createIdleLookBehaviors() {
      return new RunOne<>(ImmutableList.<Pair<? extends BehaviorControl<? super LivingEntity>, Integer>>builder().addAll(createLookBehaviors()).add(Pair.of(new DoNothing(30, 60), 1)).build());
   }

   private static RunOne<Piglin> createIdleMovementBehaviors() {
      return new RunOne<>(ImmutableList.of(Pair.of(RandomStroll.stroll(0.6F), 2), Pair.of(InteractWith.of(EntityType.PIGLIN, 8, MemoryModuleType.INTERACTION_TARGET, 0.6F, 2), 2), Pair.of(BehaviorBuilder.triggerIf(PiglinAi::doesntSeeAnyPlayerHoldingLovedItem, SetWalkTargetFromLookTarget.create(0.6F, 3)), 2), Pair.of(new DoNothing(30, 60), 1)));
   }

   private static BehaviorControl<PathfinderMob> avoidRepellent() {
      return SetWalkTargetAwayFrom.pos(MemoryModuleType.NEAREST_REPELLENT, 1.0F, 8, false);
   }

   private static BehaviorControl<Piglin> babyAvoidNemesis() {
      return CopyMemoryWithExpiry.create(Piglin::isBaby, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.AVOID_TARGET, BABY_AVOID_NEMESIS_DURATION);
   }

   private static BehaviorControl<Piglin> avoidZombified() {
      return CopyMemoryWithExpiry.create(PiglinAi::isNearZombified, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.AVOID_TARGET, AVOID_ZOMBIFIED_DURATION);
   }

   protected static void updateActivity(Piglin piglin) {
      Brain<Piglin> brain = piglin.getBrain();
      Activity activity = brain.getActiveNonCoreActivity().orElse((Activity)null);
      brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.ADMIRE_ITEM, Activity.FIGHT, Activity.AVOID, Activity.CELEBRATE, Activity.RIDE, Activity.IDLE));
      Activity activity1 = brain.getActiveNonCoreActivity().orElse((Activity)null);
      if (activity != activity1) {
         getSoundForCurrentActivity(piglin).ifPresent(piglin::playSoundEvent);
      }

      piglin.setAggressive(brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET));
      if (!brain.hasMemoryValue(MemoryModuleType.RIDE_TARGET) && isBabyRidingBaby(piglin)) {
         piglin.stopRiding();
      }

      if (!brain.hasMemoryValue(MemoryModuleType.CELEBRATE_LOCATION)) {
         brain.eraseMemory(MemoryModuleType.DANCING);
      }

      piglin.setDancing(brain.hasMemoryValue(MemoryModuleType.DANCING));
   }

   private static boolean isBabyRidingBaby(Piglin piglin) {
      if (!piglin.isBaby()) {
         return false;
      } else {
         Entity entity = piglin.getVehicle();
         return entity instanceof Piglin && ((Piglin)entity).isBaby() || entity instanceof Hoglin && ((Hoglin)entity).isBaby();
      }
   }

   protected static void pickUpItem(Piglin piglin, ItemEntity itementity) {
      stopWalking(piglin);
      ItemStack itemstack;
      if (itementity.getItem().is(Items.GOLD_NUGGET)) {
         piglin.take(itementity, itementity.getItem().getCount());
         itemstack = itementity.getItem();
         itementity.discard();
      } else {
         piglin.take(itementity, 1);
         itemstack = removeOneItemFromItemEntity(itementity);
      }

      if (isLovedItem(itemstack)) {
         piglin.getBrain().eraseMemory(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM);
         holdInOffhand(piglin, itemstack);
         admireGoldItem(piglin);
      } else if (isFood(itemstack) && !hasEatenRecently(piglin)) {
         eat(piglin);
      } else {
         boolean flag = !piglin.equipItemIfPossible(itemstack).equals(ItemStack.EMPTY);
         if (!flag) {
            putInInventory(piglin, itemstack);
         }
      }
   }

   private static void holdInOffhand(Piglin piglin, ItemStack itemstack) {
      if (isHoldingItemInOffHand(piglin)) {
         piglin.spawnAtLocation(piglin.getItemInHand(InteractionHand.OFF_HAND));
      }

      piglin.holdInOffHand(itemstack);
   }

   private static ItemStack removeOneItemFromItemEntity(ItemEntity itementity) {
      ItemStack itemstack = itementity.getItem();
      ItemStack itemstack1 = itemstack.split(1);
      if (itemstack.isEmpty()) {
         itementity.discard();
      } else {
         itementity.setItem(itemstack);
      }

      return itemstack1;
   }

   protected static void stopHoldingOffHandItem(Piglin piglin, boolean flag) {
      ItemStack itemstack = piglin.getItemInHand(InteractionHand.OFF_HAND);
      piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
      if (piglin.isAdult()) {
         boolean flag1 = isBarterCurrency(itemstack);
         if (flag && flag1) {
            throwItems(piglin, getBarterResponseItems(piglin));
         } else if (!flag1) {
            boolean flag2 = !piglin.equipItemIfPossible(itemstack).isEmpty();
            if (!flag2) {
               putInInventory(piglin, itemstack);
            }
         }
      } else {
         boolean flag3 = !piglin.equipItemIfPossible(itemstack).isEmpty();
         if (!flag3) {
            ItemStack itemstack1 = piglin.getMainHandItem();
            if (isLovedItem(itemstack1)) {
               putInInventory(piglin, itemstack1);
            } else {
               throwItems(piglin, Collections.singletonList(itemstack1));
            }

            piglin.holdInMainHand(itemstack);
         }
      }

   }

   protected static void cancelAdmiring(Piglin piglin) {
      if (isAdmiringItem(piglin) && !piglin.getOffhandItem().isEmpty()) {
         piglin.spawnAtLocation(piglin.getOffhandItem());
         piglin.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
      }

   }

   private static void putInInventory(Piglin piglin, ItemStack itemstack) {
      ItemStack itemstack1 = piglin.addToInventory(itemstack);
      throwItemsTowardRandomPos(piglin, Collections.singletonList(itemstack1));
   }

   private static void throwItems(Piglin piglin, List<ItemStack> list) {
      Optional<Player> optional = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
      if (optional.isPresent()) {
         throwItemsTowardPlayer(piglin, optional.get(), list);
      } else {
         throwItemsTowardRandomPos(piglin, list);
      }

   }

   private static void throwItemsTowardRandomPos(Piglin piglin, List<ItemStack> list) {
      throwItemsTowardPos(piglin, list, getRandomNearbyPos(piglin));
   }

   private static void throwItemsTowardPlayer(Piglin piglin, Player player, List<ItemStack> list) {
      throwItemsTowardPos(piglin, list, player.position());
   }

   private static void throwItemsTowardPos(Piglin piglin, List<ItemStack> list, Vec3 vec3) {
      if (!list.isEmpty()) {
         piglin.swing(InteractionHand.OFF_HAND);

         for(ItemStack itemstack : list) {
            BehaviorUtils.throwItem(piglin, itemstack, vec3.add(0.0D, 1.0D, 0.0D));
         }
      }

   }

   private static List<ItemStack> getBarterResponseItems(Piglin piglin) {
      LootTable loottable = piglin.level().getServer().getLootData().getLootTable(BuiltInLootTables.PIGLIN_BARTERING);
      List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel)piglin.level())).withParameter(LootContextParams.THIS_ENTITY, piglin).create(LootContextParamSets.PIGLIN_BARTER));
      return list;
   }

   private static boolean wantsToDance(LivingEntity livingentity, LivingEntity livingentity1) {
      if (livingentity1.getType() != EntityType.HOGLIN) {
         return false;
      } else {
         return RandomSource.create(livingentity.level().getGameTime()).nextFloat() < 0.1F;
      }
   }

   protected static boolean wantsToPickup(Piglin piglin, ItemStack itemstack) {
      if (piglin.isBaby() && itemstack.is(ItemTags.IGNORED_BY_PIGLIN_BABIES)) {
         return false;
      } else if (itemstack.is(ItemTags.PIGLIN_REPELLENTS)) {
         return false;
      } else if (isAdmiringDisabled(piglin) && piglin.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
         return false;
      } else if (isBarterCurrency(itemstack)) {
         return isNotHoldingLovedItemInOffHand(piglin);
      } else {
         boolean flag = piglin.canAddToInventory(itemstack);
         if (itemstack.is(Items.GOLD_NUGGET)) {
            return flag;
         } else if (isFood(itemstack)) {
            return !hasEatenRecently(piglin) && flag;
         } else if (!isLovedItem(itemstack)) {
            return piglin.canReplaceCurrentItem(itemstack);
         } else {
            return isNotHoldingLovedItemInOffHand(piglin) && flag;
         }
      }
   }

   protected static boolean isLovedItem(ItemStack itemstack) {
      return itemstack.is(ItemTags.PIGLIN_LOVED);
   }

   private static boolean wantsToStopRiding(Piglin piglin, Entity entity) {
      if (!(entity instanceof Mob mob)) {
         return false;
      } else {
         return !mob.isBaby() || !mob.isAlive() || wasHurtRecently(piglin) || wasHurtRecently(mob) || mob instanceof Piglin && mob.getVehicle() == null;
      }
   }

   private static boolean isNearestValidAttackTarget(Piglin piglin, LivingEntity livingentity) {
      return findNearestValidAttackTarget(piglin).filter((livingentity2) -> livingentity2 == livingentity).isPresent();
   }

   private static boolean isNearZombified(Piglin piglin1) {
      Brain<Piglin> brain1 = piglin1.getBrain();
      if (brain1.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED)) {
         LivingEntity livingentity = brain1.getMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED).get();
         return piglin1.closerThan(livingentity, 6.0D);
      } else {
         return false;
      }
   }

   private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Piglin piglin) {
      Brain<Piglin> brain1 = piglin.getBrain();
      if (isNearZombified(piglin)) {
         return Optional.empty();
      } else {
         Optional<LivingEntity> optional = BehaviorUtils.getLivingEntityFromUUIDMemory(piglin, MemoryModuleType.ANGRY_AT);
         if (optional.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(piglin, optional.get())) {
            return optional;
         } else {
            if (brain1.hasMemoryValue(MemoryModuleType.UNIVERSAL_ANGER)) {
               Optional<Player> optional1 = brain1.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER);
               if (optional1.isPresent()) {
                  return optional1;
               }
            }

            Optional<Mob> optional2 = brain1.getMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS);
            if (optional2.isPresent()) {
               return optional2;
            } else {
               Optional<Player> optional3 = brain1.getMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD);
               return optional3.isPresent() && Sensor.isEntityAttackable(piglin, optional3.get()) ? optional3 : Optional.empty();
            }
         }
      }
   }

   public static void angerNearbyPiglins(Player player, boolean flag) {
      List<Piglin> list = player.level().getEntitiesOfClass(Piglin.class, player.getBoundingBox().inflate(16.0D));
      list.stream().filter(PiglinAi::isIdle).filter((piglin1) -> !flag || BehaviorUtils.canSee(piglin1, player)).forEach((piglin) -> {
         if (piglin.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            setAngerTargetToNearestTargetablePlayerIfFound(piglin, player);
         } else {
            setAngerTarget(piglin, player);
         }

      });
   }

   public static InteractionResult mobInteract(Piglin piglin, Player player, InteractionHand interactionhand) {
      ItemStack itemstack = player.getItemInHand(interactionhand);
      if (canAdmire(piglin, itemstack)) {
         ItemStack itemstack1 = itemstack.split(1);
         holdInOffhand(piglin, itemstack1);
         admireGoldItem(piglin);
         stopWalking(piglin);
         return InteractionResult.CONSUME;
      } else {
         return InteractionResult.PASS;
      }
   }

   protected static boolean canAdmire(Piglin piglin, ItemStack itemstack) {
      return !isAdmiringDisabled(piglin) && !isAdmiringItem(piglin) && piglin.isAdult() && isBarterCurrency(itemstack);
   }

   protected static void wasHurtBy(Piglin piglin, LivingEntity livingentity) {
      if (!(livingentity instanceof Piglin)) {
         if (isHoldingItemInOffHand(piglin)) {
            stopHoldingOffHandItem(piglin, false);
         }

         Brain<Piglin> brain = piglin.getBrain();
         brain.eraseMemory(MemoryModuleType.CELEBRATE_LOCATION);
         brain.eraseMemory(MemoryModuleType.DANCING);
         brain.eraseMemory(MemoryModuleType.ADMIRING_ITEM);
         if (livingentity instanceof Player) {
            brain.setMemoryWithExpiry(MemoryModuleType.ADMIRING_DISABLED, true, 400L);
         }

         getAvoidTarget(piglin).ifPresent((livingentity2) -> {
            if (livingentity2.getType() != livingentity.getType()) {
               brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
            }

         });
         if (piglin.isBaby()) {
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingentity, 100L);
            if (Sensor.isEntityAttackableIgnoringLineOfSight(piglin, livingentity)) {
               broadcastAngerTarget(piglin, livingentity);
            }

         } else if (livingentity.getType() == EntityType.HOGLIN && hoglinsOutnumberPiglins(piglin)) {
            setAvoidTargetAndDontHuntForAWhile(piglin, livingentity);
            broadcastRetreat(piglin, livingentity);
         } else {
            maybeRetaliate(piglin, livingentity);
         }
      }
   }

   protected static void maybeRetaliate(AbstractPiglin abstractpiglin, LivingEntity livingentity) {
      if (!abstractpiglin.getBrain().isActive(Activity.AVOID)) {
         if (Sensor.isEntityAttackableIgnoringLineOfSight(abstractpiglin, livingentity)) {
            if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(abstractpiglin, livingentity, 4.0D)) {
               if (livingentity.getType() == EntityType.PLAYER && abstractpiglin.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
                  setAngerTargetToNearestTargetablePlayerIfFound(abstractpiglin, livingentity);
                  broadcastUniversalAnger(abstractpiglin);
               } else {
                  setAngerTarget(abstractpiglin, livingentity);
                  broadcastAngerTarget(abstractpiglin, livingentity);
               }

            }
         }
      }
   }

   public static Optional<SoundEvent> getSoundForCurrentActivity(Piglin piglin) {
      return piglin.getBrain().getActiveNonCoreActivity().map((activity) -> getSoundForActivity(piglin, activity));
   }

   private static SoundEvent getSoundForActivity(Piglin piglin, Activity activity) {
      if (activity == Activity.FIGHT) {
         return SoundEvents.PIGLIN_ANGRY;
      } else if (piglin.isConverting()) {
         return SoundEvents.PIGLIN_RETREAT;
      } else if (activity == Activity.AVOID && isNearAvoidTarget(piglin)) {
         return SoundEvents.PIGLIN_RETREAT;
      } else if (activity == Activity.ADMIRE_ITEM) {
         return SoundEvents.PIGLIN_ADMIRING_ITEM;
      } else if (activity == Activity.CELEBRATE) {
         return SoundEvents.PIGLIN_CELEBRATE;
      } else if (seesPlayerHoldingLovedItem(piglin)) {
         return SoundEvents.PIGLIN_JEALOUS;
      } else {
         return isNearRepellent(piglin) ? SoundEvents.PIGLIN_RETREAT : SoundEvents.PIGLIN_AMBIENT;
      }
   }

   private static boolean isNearAvoidTarget(Piglin piglin) {
      Brain<Piglin> brain = piglin.getBrain();
      return !brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? false : brain.getMemory(MemoryModuleType.AVOID_TARGET).get().closerThan(piglin, 12.0D);
   }

   protected static List<AbstractPiglin> getVisibleAdultPiglins(Piglin piglin) {
      return piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   private static List<AbstractPiglin> getAdultPiglins(AbstractPiglin abstractpiglin) {
      return abstractpiglin.getBrain().getMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS).orElse(ImmutableList.of());
   }

   public static boolean isWearingGold(LivingEntity livingentity) {
      for(ItemStack itemstack : livingentity.getArmorSlots()) {
         Item item = itemstack.getItem();
         if (item instanceof ArmorItem && ((ArmorItem)item).getMaterial() == ArmorMaterials.GOLD) {
            return true;
         }
      }

      return false;
   }

   private static void stopWalking(Piglin piglin) {
      piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      piglin.getNavigation().stop();
   }

   private static BehaviorControl<LivingEntity> babySometimesRideBabyHoglin() {
      SetEntityLookTargetSometimes.Ticker setentitylooktargetsometimes_ticker = new SetEntityLookTargetSometimes.Ticker(RIDE_START_INTERVAL);
      return CopyMemoryWithExpiry.create((livingentity) -> livingentity.isBaby() && setentitylooktargetsometimes_ticker.tickDownAndCheck(livingentity.level().random), MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.RIDE_TARGET, RIDE_DURATION);
   }

   protected static void broadcastAngerTarget(AbstractPiglin abstractpiglin, LivingEntity livingentity) {
      getAdultPiglins(abstractpiglin).forEach((abstractpiglin1) -> {
         if (livingentity.getType() != EntityType.HOGLIN || abstractpiglin1.canHunt() && ((Hoglin)livingentity).canBeHunted()) {
            setAngerTargetIfCloserThanCurrent(abstractpiglin1, livingentity);
         }
      });
   }

   protected static void broadcastUniversalAnger(AbstractPiglin abstractpiglin) {
      getAdultPiglins(abstractpiglin).forEach((abstractpiglin1) -> getNearestVisibleTargetablePlayer(abstractpiglin1).ifPresent((player) -> setAngerTarget(abstractpiglin1, player)));
   }

   protected static void setAngerTarget(AbstractPiglin abstractpiglin, LivingEntity livingentity) {
      if (Sensor.isEntityAttackableIgnoringLineOfSight(abstractpiglin, livingentity)) {
         abstractpiglin.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
         abstractpiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, livingentity.getUUID(), 600L);
         if (livingentity.getType() == EntityType.HOGLIN && abstractpiglin.canHunt()) {
            dontKillAnyMoreHoglinsForAWhile(abstractpiglin);
         }

         if (livingentity.getType() == EntityType.PLAYER && abstractpiglin.level().getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER)) {
            abstractpiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.UNIVERSAL_ANGER, true, 600L);
         }

      }
   }

   private static void setAngerTargetToNearestTargetablePlayerIfFound(AbstractPiglin abstractpiglin, LivingEntity livingentity) {
      Optional<Player> optional = getNearestVisibleTargetablePlayer(abstractpiglin);
      if (optional.isPresent()) {
         setAngerTarget(abstractpiglin, optional.get());
      } else {
         setAngerTarget(abstractpiglin, livingentity);
      }

   }

   private static void setAngerTargetIfCloserThanCurrent(AbstractPiglin abstractpiglin, LivingEntity livingentity) {
      Optional<LivingEntity> optional = getAngerTarget(abstractpiglin);
      LivingEntity livingentity1 = BehaviorUtils.getNearestTarget(abstractpiglin, optional, livingentity);
      if (!optional.isPresent() || optional.get() != livingentity1) {
         setAngerTarget(abstractpiglin, livingentity1);
      }
   }

   private static Optional<LivingEntity> getAngerTarget(AbstractPiglin abstractpiglin) {
      return BehaviorUtils.getLivingEntityFromUUIDMemory(abstractpiglin, MemoryModuleType.ANGRY_AT);
   }

   public static Optional<LivingEntity> getAvoidTarget(Piglin piglin) {
      return piglin.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET) ? piglin.getBrain().getMemory(MemoryModuleType.AVOID_TARGET) : Optional.empty();
   }

   public static Optional<Player> getNearestVisibleTargetablePlayer(AbstractPiglin abstractpiglin) {
      return abstractpiglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) ? abstractpiglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER) : Optional.empty();
   }

   private static void broadcastRetreat(Piglin piglin, LivingEntity livingentity) {
      getVisibleAdultPiglins(piglin).stream().filter((abstractpiglin1) -> abstractpiglin1 instanceof Piglin).forEach((abstractpiglin) -> retreatFromNearestTarget((Piglin)abstractpiglin, livingentity));
   }

   private static void retreatFromNearestTarget(Piglin piglin, LivingEntity livingentity) {
      Brain<Piglin> brain = piglin.getBrain();
      LivingEntity livingentity1 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.AVOID_TARGET), livingentity);
      livingentity1 = BehaviorUtils.getNearestTarget(piglin, brain.getMemory(MemoryModuleType.ATTACK_TARGET), livingentity1);
      setAvoidTargetAndDontHuntForAWhile(piglin, livingentity1);
   }

   private static boolean wantsToStopFleeing(Piglin piglin) {
      Brain<Piglin> brain1 = piglin.getBrain();
      if (!brain1.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
         return true;
      } else {
         LivingEntity livingentity = brain1.getMemory(MemoryModuleType.AVOID_TARGET).get();
         EntityType<?> entitytype = livingentity.getType();
         if (entitytype == EntityType.HOGLIN) {
            return piglinsEqualOrOutnumberHoglins(piglin);
         } else if (isZombified(entitytype)) {
            return !brain1.isMemoryValue(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, livingentity);
         } else {
            return false;
         }
      }
   }

   private static boolean piglinsEqualOrOutnumberHoglins(Piglin piglin) {
      return !hoglinsOutnumberPiglins(piglin);
   }

   private static boolean hoglinsOutnumberPiglins(Piglin piglin) {
      int i = piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT).orElse(0) + 1;
      int j = piglin.getBrain().getMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT).orElse(0);
      return j > i;
   }

   private static void setAvoidTargetAndDontHuntForAWhile(Piglin piglin, LivingEntity livingentity) {
      piglin.getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
      piglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
      piglin.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, livingentity, (long)RETREAT_DURATION.sample(piglin.level().random));
      dontKillAnyMoreHoglinsForAWhile(piglin);
   }

   protected static void dontKillAnyMoreHoglinsForAWhile(AbstractPiglin abstractpiglin) {
      abstractpiglin.getBrain().setMemoryWithExpiry(MemoryModuleType.HUNTED_RECENTLY, true, (long)TIME_BETWEEN_HUNTS.sample(abstractpiglin.level().random));
   }

   private static void eat(Piglin piglin) {
      piglin.getBrain().setMemoryWithExpiry(MemoryModuleType.ATE_RECENTLY, true, 200L);
   }

   private static Vec3 getRandomNearbyPos(Piglin piglin) {
      Vec3 vec3 = LandRandomPos.getPos(piglin, 4, 2);
      return vec3 == null ? piglin.position() : vec3;
   }

   private static boolean hasEatenRecently(Piglin piglin) {
      return piglin.getBrain().hasMemoryValue(MemoryModuleType.ATE_RECENTLY);
   }

   protected static boolean isIdle(AbstractPiglin abstractpiglin) {
      return abstractpiglin.getBrain().isActive(Activity.IDLE);
   }

   private static boolean hasCrossbow(LivingEntity livingentity1) {
      return livingentity1.isHolding(Items.CROSSBOW);
   }

   private static void admireGoldItem(LivingEntity livingentity) {
      livingentity.getBrain().setMemoryWithExpiry(MemoryModuleType.ADMIRING_ITEM, true, 120L);
   }

   private static boolean isAdmiringItem(Piglin piglin) {
      return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_ITEM);
   }

   private static boolean isBarterCurrency(ItemStack itemstack) {
      return itemstack.is(BARTERING_ITEM);
   }

   private static boolean isFood(ItemStack itemstack) {
      return itemstack.is(ItemTags.PIGLIN_FOOD);
   }

   private static boolean isNearRepellent(Piglin piglin) {
      return piglin.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_REPELLENT);
   }

   private static boolean seesPlayerHoldingLovedItem(LivingEntity livingentity) {
      return livingentity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM);
   }

   private static boolean doesntSeeAnyPlayerHoldingLovedItem(LivingEntity livingentity) {
      return !seesPlayerHoldingLovedItem(livingentity);
   }

   public static boolean isPlayerHoldingLovedItem(LivingEntity livingentity) {
      return livingentity.getType() == EntityType.PLAYER && livingentity.isHolding(PiglinAi::isLovedItem);
   }

   private static boolean isAdmiringDisabled(Piglin piglin) {
      return piglin.getBrain().hasMemoryValue(MemoryModuleType.ADMIRING_DISABLED);
   }

   private static boolean wasHurtRecently(LivingEntity livingentity) {
      return livingentity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
   }

   private static boolean isHoldingItemInOffHand(Piglin piglin) {
      return !piglin.getOffhandItem().isEmpty();
   }

   private static boolean isNotHoldingLovedItemInOffHand(Piglin piglin) {
      return piglin.getOffhandItem().isEmpty() || !isLovedItem(piglin.getOffhandItem());
   }

   public static boolean isZombified(EntityType<?> entitytype) {
      return entitytype == EntityType.ZOMBIFIED_PIGLIN || entitytype == EntityType.ZOGLIN;
   }
}
