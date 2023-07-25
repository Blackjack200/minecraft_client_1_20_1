package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class WaterAvoidingRandomStrollGoal extends RandomStrollGoal {
   public static final float PROBABILITY = 0.001F;
   protected final float probability;

   public WaterAvoidingRandomStrollGoal(PathfinderMob pathfindermob, double d0) {
      this(pathfindermob, d0, 0.001F);
   }

   public WaterAvoidingRandomStrollGoal(PathfinderMob pathfindermob, double d0, float f) {
      super(pathfindermob, d0);
      this.probability = f;
   }

   @Nullable
   protected Vec3 getPosition() {
      if (this.mob.isInWaterOrBubble()) {
         Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
         return vec3 == null ? super.getPosition() : vec3;
      } else {
         return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7) : super.getPosition();
      }
   }
}
