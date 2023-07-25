package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;

public class WrappedGoal extends Goal {
   private final Goal goal;
   private final int priority;
   private boolean isRunning;

   public WrappedGoal(int i, Goal goal) {
      this.priority = i;
      this.goal = goal;
   }

   public boolean canBeReplacedBy(WrappedGoal wrappedgoal) {
      return this.isInterruptable() && wrappedgoal.getPriority() < this.getPriority();
   }

   public boolean canUse() {
      return this.goal.canUse();
   }

   public boolean canContinueToUse() {
      return this.goal.canContinueToUse();
   }

   public boolean isInterruptable() {
      return this.goal.isInterruptable();
   }

   public void start() {
      if (!this.isRunning) {
         this.isRunning = true;
         this.goal.start();
      }
   }

   public void stop() {
      if (this.isRunning) {
         this.isRunning = false;
         this.goal.stop();
      }
   }

   public boolean requiresUpdateEveryTick() {
      return this.goal.requiresUpdateEveryTick();
   }

   protected int adjustedTickDelay(int i) {
      return this.goal.adjustedTickDelay(i);
   }

   public void tick() {
      this.goal.tick();
   }

   public void setFlags(EnumSet<Goal.Flag> enumset) {
      this.goal.setFlags(enumset);
   }

   public EnumSet<Goal.Flag> getFlags() {
      return this.goal.getFlags();
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   public int getPriority() {
      return this.priority;
   }

   public Goal getGoal() {
      return this.goal;
   }

   public boolean equals(@Nullable Object object) {
      if (this == object) {
         return true;
      } else {
         return object != null && this.getClass() == object.getClass() ? this.goal.equals(((WrappedGoal)object).goal) : false;
      }
   }

   public int hashCode() {
      return this.goal.hashCode();
   }
}
