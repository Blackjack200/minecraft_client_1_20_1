package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class PiglinSpecificSensor extends Sensor<LivingEntity> {
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_REPELLENT);
   }

   protected void doTick(ServerLevel serverlevel, LivingEntity livingentity) {
      Brain<?> brain = livingentity.getBrain();
      brain.setMemory(MemoryModuleType.NEAREST_REPELLENT, findNearestRepellent(serverlevel, livingentity));
      Optional<Mob> optional = Optional.empty();
      Optional<Hoglin> optional1 = Optional.empty();
      Optional<Hoglin> optional2 = Optional.empty();
      Optional<Piglin> optional3 = Optional.empty();
      Optional<LivingEntity> optional4 = Optional.empty();
      Optional<Player> optional5 = Optional.empty();
      Optional<Player> optional6 = Optional.empty();
      int i = 0;
      List<AbstractPiglin> list = Lists.newArrayList();
      List<AbstractPiglin> list1 = Lists.newArrayList();
      NearestVisibleLivingEntities nearestvisiblelivingentities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

      for(LivingEntity livingentity1 : nearestvisiblelivingentities.findAll((livingentity3) -> true)) {
         if (livingentity1 instanceof Hoglin hoglin) {
            if (hoglin.isBaby() && optional2.isEmpty()) {
               optional2 = Optional.of(hoglin);
            } else if (hoglin.isAdult()) {
               ++i;
               if (optional1.isEmpty() && hoglin.canBeHunted()) {
                  optional1 = Optional.of(hoglin);
               }
            }
         } else if (livingentity1 instanceof PiglinBrute piglinbrute) {
            list.add(piglinbrute);
         } else if (livingentity1 instanceof Piglin piglin) {
            if (piglin.isBaby() && optional3.isEmpty()) {
               optional3 = Optional.of(piglin);
            } else if (piglin.isAdult()) {
               list.add(piglin);
            }
         } else if (livingentity1 instanceof Player player) {
            if (optional5.isEmpty() && !PiglinAi.isWearingGold(player) && livingentity.canAttack(livingentity1)) {
               optional5 = Optional.of(player);
            }

            if (optional6.isEmpty() && !player.isSpectator() && PiglinAi.isPlayerHoldingLovedItem(player)) {
               optional6 = Optional.of(player);
            }
         } else if (!optional.isEmpty() || !(livingentity1 instanceof WitherSkeleton) && !(livingentity1 instanceof WitherBoss)) {
            if (optional4.isEmpty() && PiglinAi.isZombified(livingentity1.getType())) {
               optional4 = Optional.of(livingentity1);
            }
         } else {
            optional = Optional.of((Mob)livingentity1);
         }
      }

      for(LivingEntity livingentity2 : brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of())) {
         if (livingentity2 instanceof AbstractPiglin abstractpiglin) {
            if (abstractpiglin.isAdult()) {
               list1.add(abstractpiglin);
            }
         }
      }

      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, optional1);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, optional2);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, optional4);
      brain.setMemory(MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, optional5);
      brain.setMemory(MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, optional6);
      brain.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, list1);
      brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, list);
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, list.size());
      brain.setMemory(MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, i);
   }

   private static Optional<BlockPos> findNearestRepellent(ServerLevel serverlevel, LivingEntity livingentity) {
      return BlockPos.findClosestMatch(livingentity.blockPosition(), 8, 4, (blockpos) -> isValidRepellent(serverlevel, blockpos));
   }

   private static boolean isValidRepellent(ServerLevel serverlevel, BlockPos blockpos) {
      BlockState blockstate = serverlevel.getBlockState(blockpos);
      boolean flag = blockstate.is(BlockTags.PIGLIN_REPELLENTS);
      return flag && blockstate.is(Blocks.SOUL_CAMPFIRE) ? CampfireBlock.isLitCampfire(blockstate) : flag;
   }
}
