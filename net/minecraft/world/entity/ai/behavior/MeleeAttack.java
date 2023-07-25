package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack {
   public static OneShot<Mob> create(int i) {
      return BehaviorBuilder.create((behaviorbuilder_instance) -> behaviorbuilder_instance.<MemoryAccessor, MemoryAccessor, MemoryAccessor, MemoryAccessor>group(behaviorbuilder_instance.registered(MemoryModuleType.LOOK_TARGET), behaviorbuilder_instance.present(MemoryModuleType.ATTACK_TARGET), behaviorbuilder_instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN), behaviorbuilder_instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply(behaviorbuilder_instance, (memoryaccessor, memoryaccessor1, memoryaccessor2, memoryaccessor3) -> (serverlevel, mob, i1) -> {
               LivingEntity livingentity = behaviorbuilder_instance.get(memoryaccessor1);
               if (!isHoldingUsableProjectileWeapon(mob) && mob.isWithinMeleeAttackRange(livingentity) && behaviorbuilder_instance.<NearestVisibleLivingEntities>get(memoryaccessor3).contains(livingentity)) {
                  memoryaccessor.set(new EntityTracker(livingentity, true));
                  mob.swing(InteractionHand.MAIN_HAND);
                  mob.doHurtTarget(livingentity);
                  memoryaccessor2.setWithExpiry(true, (long)i);
                  return true;
               } else {
                  return false;
               }
            }));
   }

   private static boolean isHoldingUsableProjectileWeapon(Mob mob) {
      return mob.isHolding((itemstack) -> {
         Item item = itemstack.getItem();
         return item instanceof ProjectileWeaponItem && mob.canFireProjectileWeapon((ProjectileWeaponItem)item);
      });
   }
}
