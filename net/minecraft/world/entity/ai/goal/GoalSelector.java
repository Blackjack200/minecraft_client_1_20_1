package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class GoalSelector {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal() {
      public boolean canUse() {
         return false;
      }
   }) {
      public boolean isRunning() {
         return false;
      }
   };
   private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap<>(Goal.Flag.class);
   private final Set<WrappedGoal> availableGoals = Sets.newLinkedHashSet();
   private final Supplier<ProfilerFiller> profiler;
   private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
   private int tickCount;
   private int newGoalRate = 3;

   public GoalSelector(Supplier<ProfilerFiller> supplier) {
      this.profiler = supplier;
   }

   public void addGoal(int i, Goal goal) {
      this.availableGoals.add(new WrappedGoal(i, goal));
   }

   @VisibleForTesting
   public void removeAllGoals(Predicate<Goal> predicate) {
      this.availableGoals.removeIf((wrappedgoal) -> predicate.test(wrappedgoal.getGoal()));
   }

   public void removeGoal(Goal goal) {
      this.availableGoals.stream().filter((wrappedgoal1) -> wrappedgoal1.getGoal() == goal).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
      this.availableGoals.removeIf((wrappedgoal) -> wrappedgoal.getGoal() == goal);
   }

   private static boolean goalContainsAnyFlags(WrappedGoal wrappedgoal, EnumSet<Goal.Flag> enumset) {
      for(Goal.Flag goal_flag : wrappedgoal.getFlags()) {
         if (enumset.contains(goal_flag)) {
            return true;
         }
      }

      return false;
   }

   private static boolean goalCanBeReplacedForAllFlags(WrappedGoal wrappedgoal, Map<Goal.Flag, WrappedGoal> map) {
      for(Goal.Flag goal_flag : wrappedgoal.getFlags()) {
         if (!map.getOrDefault(goal_flag, NO_GOAL).canBeReplacedBy(wrappedgoal)) {
            return false;
         }
      }

      return true;
   }

   public void tick() {
      ProfilerFiller profilerfiller = this.profiler.get();
      profilerfiller.push("goalCleanup");

      for(WrappedGoal wrappedgoal : this.availableGoals) {
         if (wrappedgoal.isRunning() && (goalContainsAnyFlags(wrappedgoal, this.disabledFlags) || !wrappedgoal.canContinueToUse())) {
            wrappedgoal.stop();
         }
      }

      Iterator<Map.Entry<Goal.Flag, WrappedGoal>> iterator = this.lockedFlags.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<Goal.Flag, WrappedGoal> map_entry = iterator.next();
         if (!map_entry.getValue().isRunning()) {
            iterator.remove();
         }
      }

      profilerfiller.pop();
      profilerfiller.push("goalUpdate");

      for(WrappedGoal wrappedgoal1 : this.availableGoals) {
         if (!wrappedgoal1.isRunning() && !goalContainsAnyFlags(wrappedgoal1, this.disabledFlags) && goalCanBeReplacedForAllFlags(wrappedgoal1, this.lockedFlags) && wrappedgoal1.canUse()) {
            for(Goal.Flag goal_flag : wrappedgoal1.getFlags()) {
               WrappedGoal wrappedgoal2 = this.lockedFlags.getOrDefault(goal_flag, NO_GOAL);
               wrappedgoal2.stop();
               this.lockedFlags.put(goal_flag, wrappedgoal1);
            }

            wrappedgoal1.start();
         }
      }

      profilerfiller.pop();
      this.tickRunningGoals(true);
   }

   public void tickRunningGoals(boolean flag) {
      ProfilerFiller profilerfiller = this.profiler.get();
      profilerfiller.push("goalTick");

      for(WrappedGoal wrappedgoal : this.availableGoals) {
         if (wrappedgoal.isRunning() && (flag || wrappedgoal.requiresUpdateEveryTick())) {
            wrappedgoal.tick();
         }
      }

      profilerfiller.pop();
   }

   public Set<WrappedGoal> getAvailableGoals() {
      return this.availableGoals;
   }

   public Stream<WrappedGoal> getRunningGoals() {
      return this.availableGoals.stream().filter(WrappedGoal::isRunning);
   }

   public void setNewGoalRate(int i) {
      this.newGoalRate = i;
   }

   public void disableControlFlag(Goal.Flag goal_flag) {
      this.disabledFlags.add(goal_flag);
   }

   public void enableControlFlag(Goal.Flag goal_flag) {
      this.disabledFlags.remove(goal_flag);
   }

   public void setControlFlag(Goal.Flag goal_flag, boolean flag) {
      if (flag) {
         this.enableControlFlag(goal_flag);
      } else {
         this.disableControlFlag(goal_flag);
      }

   }
}
