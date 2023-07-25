package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;

public class Swim extends Behavior<Mob> {
   private final float chance;

   public Swim(float f) {
      super(ImmutableMap.of());
      this.chance = f;
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Mob mob) {
      return mob.isInWater() && mob.getFluidHeight(FluidTags.WATER) > mob.getFluidJumpThreshold() || mob.isInLava();
   }

   protected boolean canStillUse(ServerLevel serverlevel, Mob mob, long i) {
      return this.checkExtraStartConditions(serverlevel, mob);
   }

   protected void tick(ServerLevel serverlevel, Mob mob, long i) {
      if (mob.getRandom().nextFloat() < this.chance) {
         mob.getJumpControl().jump();
      }

   }
}
