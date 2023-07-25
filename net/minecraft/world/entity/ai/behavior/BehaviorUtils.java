package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
   private BehaviorUtils() {
   }

   public static void lockGazeAndWalkToEachOther(LivingEntity livingentity, LivingEntity livingentity1, float f) {
      lookAtEachOther(livingentity, livingentity1);
      setWalkAndLookTargetMemoriesToEachOther(livingentity, livingentity1, f);
   }

   public static boolean entityIsVisible(Brain<?> brain, LivingEntity livingentity) {
      Optional<NearestVisibleLivingEntities> optional = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
      return optional.isPresent() && optional.get().contains(livingentity);
   }

   public static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memorymoduletype, EntityType<?> entitytype) {
      return targetIsValid(brain, memorymoduletype, (livingentity) -> livingentity.getType() == entitytype);
   }

   private static boolean targetIsValid(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memorymoduletype, Predicate<LivingEntity> predicate) {
      return brain.getMemory(memorymoduletype).filter(predicate).filter(LivingEntity::isAlive).filter((livingentity) -> entityIsVisible(brain, livingentity)).isPresent();
   }

   private static void lookAtEachOther(LivingEntity livingentity, LivingEntity livingentity1) {
      lookAtEntity(livingentity, livingentity1);
      lookAtEntity(livingentity1, livingentity);
   }

   public static void lookAtEntity(LivingEntity livingentity, LivingEntity livingentity1) {
      livingentity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(livingentity1, true));
   }

   private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity livingentity, LivingEntity livingentity1, float f) {
      int i = 2;
      setWalkAndLookTargetMemories(livingentity, livingentity1, f, 2);
      setWalkAndLookTargetMemories(livingentity1, livingentity, f, 2);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity livingentity, Entity entity, float f, int i) {
      setWalkAndLookTargetMemories(livingentity, new EntityTracker(entity, true), f, i);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity livingentity, BlockPos blockpos, float f, int i) {
      setWalkAndLookTargetMemories(livingentity, new BlockPosTracker(blockpos), f, i);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity livingentity, PositionTracker positiontracker, float f, int i) {
      WalkTarget walktarget = new WalkTarget(positiontracker, f, i);
      livingentity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, positiontracker);
      livingentity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void throwItem(LivingEntity livingentity, ItemStack itemstack, Vec3 vec3) {
      Vec3 vec31 = new Vec3((double)0.3F, (double)0.3F, (double)0.3F);
      throwItem(livingentity, itemstack, vec3, vec31, 0.3F);
   }

   public static void throwItem(LivingEntity livingentity, ItemStack itemstack, Vec3 vec3, Vec3 vec31, float f) {
      double d0 = livingentity.getEyeY() - (double)f;
      ItemEntity itementity = new ItemEntity(livingentity.level(), livingentity.getX(), d0, livingentity.getZ(), itemstack);
      itementity.setThrower(livingentity.getUUID());
      Vec3 vec32 = vec3.subtract(livingentity.position());
      vec32 = vec32.normalize().multiply(vec31.x, vec31.y, vec31.z);
      itementity.setDeltaMovement(vec32);
      itementity.setDefaultPickUpDelay();
      livingentity.level().addFreshEntity(itementity);
   }

   public static SectionPos findSectionClosestToVillage(ServerLevel serverlevel, SectionPos sectionpos, int i) {
      int j = serverlevel.sectionsToVillage(sectionpos);
      return SectionPos.cube(sectionpos, i).filter((sectionpos1) -> serverlevel.sectionsToVillage(sectionpos1) < j).min(Comparator.comparingInt(serverlevel::sectionsToVillage)).orElse(sectionpos);
   }

   public static boolean isWithinAttackRange(Mob mob, LivingEntity livingentity, int i) {
      Item j = mob.getMainHandItem().getItem();
      if (j instanceof ProjectileWeaponItem projectileweaponitem) {
         if (mob.canFireProjectileWeapon(projectileweaponitem)) {
            int j = projectileweaponitem.getDefaultProjectileRange() - i;
            return mob.closerThan(livingentity, (double)j);
         }
      }

      return mob.isWithinMeleeAttackRange(livingentity);
   }

   public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity livingentity, LivingEntity livingentity1, double d0) {
      Optional<LivingEntity> optional = livingentity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
      if (optional.isEmpty()) {
         return false;
      } else {
         double d1 = livingentity.distanceToSqr(optional.get().position());
         double d2 = livingentity.distanceToSqr(livingentity1.position());
         return d2 > d1 + d0 * d0;
      }
   }

   public static boolean canSee(LivingEntity livingentity, LivingEntity livingentity1) {
      Brain<?> brain = livingentity.getBrain();
      return !brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES) ? false : brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(livingentity1);
   }

   public static LivingEntity getNearestTarget(LivingEntity livingentity, Optional<LivingEntity> optional, LivingEntity livingentity1) {
      return optional.isEmpty() ? livingentity1 : getTargetNearestMe(livingentity, optional.get(), livingentity1);
   }

   public static LivingEntity getTargetNearestMe(LivingEntity livingentity, LivingEntity livingentity1, LivingEntity livingentity2) {
      Vec3 vec3 = livingentity1.position();
      Vec3 vec31 = livingentity2.position();
      return livingentity.distanceToSqr(vec3) < livingentity.distanceToSqr(vec31) ? livingentity1 : livingentity2;
   }

   public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity livingentity, MemoryModuleType<UUID> memorymoduletype) {
      Optional<UUID> optional = livingentity.getBrain().getMemory(memorymoduletype);
      return optional.map((uuid) -> ((ServerLevel)livingentity.level()).getEntity(uuid)).map((entity) -> {
         LivingEntity var10000;
         if (entity instanceof LivingEntity livingentity1) {
            var10000 = livingentity1;
         } else {
            var10000 = null;
         }

         return var10000;
      });
   }

   @Nullable
   public static Vec3 getRandomSwimmablePos(PathfinderMob pathfindermob, int i, int j) {
      Vec3 vec3 = DefaultRandomPos.getPos(pathfindermob, i, j);

      for(int k = 0; vec3 != null && !pathfindermob.level().getBlockState(BlockPos.containing(vec3)).isPathfindable(pathfindermob.level(), BlockPos.containing(vec3), PathComputationType.WATER) && k++ < 10; vec3 = DefaultRandomPos.getPos(pathfindermob, i, j)) {
      }

      return vec3;
   }

   public static boolean isBreeding(LivingEntity livingentity) {
      return livingentity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }
}
