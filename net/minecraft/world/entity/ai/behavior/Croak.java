package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.frog.Frog;

public class Croak extends Behavior<Frog> {
   private static final int CROAK_TICKS = 60;
   private static final int TIME_OUT_DURATION = 100;
   private int croakCounter;

   public Croak() {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 100);
   }

   protected boolean checkExtraStartConditions(ServerLevel serverlevel, Frog frog) {
      return frog.getPose() == Pose.STANDING;
   }

   protected boolean canStillUse(ServerLevel serverlevel, Frog frog, long i) {
      return this.croakCounter < 60;
   }

   protected void start(ServerLevel serverlevel, Frog frog, long i) {
      if (!frog.isInWaterOrBubble() && !frog.isInLava()) {
         frog.setPose(Pose.CROAKING);
         this.croakCounter = 0;
      }
   }

   protected void stop(ServerLevel serverlevel, Frog frog, long i) {
      frog.setPose(Pose.STANDING);
   }

   protected void tick(ServerLevel serverlevel, Frog frog, long i) {
      ++this.croakCounter;
   }
}
