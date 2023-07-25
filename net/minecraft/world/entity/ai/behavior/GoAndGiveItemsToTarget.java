package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {
   private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
   private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;
   private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
   private final float speedModifier;

   public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> function, float f, int i) {
      super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.REGISTERED), i);
      this.targetPositionGetter = function;
      this.speedModifier = f;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, E livingentity) {
      return this.canThrowItemToTarget(livingentity);
   }

   protected boolean canStillUse(ServerLevel serverlevel, E livingentity, long i) {
      return this.canThrowItemToTarget(livingentity);
   }

   protected void start(ServerLevel serverlevel, E livingentity, long i) {
      this.targetPositionGetter.apply(livingentity).ifPresent((positiontracker) -> BehaviorUtils.setWalkAndLookTargetMemories(livingentity, positiontracker, this.speedModifier, 3));
   }

   protected void tick(ServerLevel serverlevel, E livingentity, long i) {
      Optional<PositionTracker> optional = this.targetPositionGetter.apply(livingentity);
      if (!optional.isEmpty()) {
         PositionTracker positiontracker = optional.get();
         double d0 = positiontracker.currentPosition().distanceTo(livingentity.getEyePosition());
         if (d0 < 3.0D) {
            ItemStack itemstack = livingentity.getInventory().removeItem(0, 1);
            if (!itemstack.isEmpty()) {
               throwItem(livingentity, itemstack, getThrowPosition(positiontracker));
               if (livingentity instanceof Allay) {
                  Allay allay = (Allay)livingentity;
                  AllayAi.getLikedPlayer(allay).ifPresent((serverplayer) -> this.triggerDropItemOnBlock(positiontracker, itemstack, serverplayer));
               }

               livingentity.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
            }
         }

      }
   }

   private void triggerDropItemOnBlock(PositionTracker positiontracker, ItemStack itemstack, ServerPlayer serverplayer) {
      BlockPos blockpos = positiontracker.currentBlockPosition().below();
      CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger(serverplayer, blockpos, itemstack);
   }

   private boolean canThrowItemToTarget(E livingentity) {
      if (livingentity.getInventory().isEmpty()) {
         return false;
      } else {
         Optional<PositionTracker> optional = this.targetPositionGetter.apply(livingentity);
         return optional.isPresent();
      }
   }

   private static Vec3 getThrowPosition(PositionTracker positiontracker) {
      return positiontracker.currentPosition().add(0.0D, 1.0D, 0.0D);
   }

   public static void throwItem(LivingEntity livingentity, ItemStack itemstack, Vec3 vec3) {
      Vec3 vec31 = new Vec3((double)0.2F, (double)0.3F, (double)0.2F);
      BehaviorUtils.throwItem(livingentity, itemstack, vec3, vec31, 0.2F);
      Level level = livingentity.level();
      if (level.getGameTime() % 7L == 0L && level.random.nextDouble() < 0.9D) {
         float f = Util.getRandom(Allay.THROW_SOUND_PITCHES, level.getRandom());
         level.playSound((Player)null, livingentity, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 1.0F, f);
      }

   }
}
