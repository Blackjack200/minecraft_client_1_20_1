package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.phys.Vec3;

public class RandomSwimmingGoal extends RandomStrollGoal {
   public RandomSwimmingGoal(PathfinderMob pathfindermob, double d0, int i) {
      super(pathfindermob, d0, i);
   }

   @Nullable
   protected Vec3 getPosition() {
      return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
   }
}
