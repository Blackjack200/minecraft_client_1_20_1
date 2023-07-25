package net.minecraft.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Predicate;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;

public class AdvancementVisibilityEvaluator {
   private static final int VISIBILITY_DEPTH = 2;

   private static AdvancementVisibilityEvaluator.VisibilityRule evaluateVisibilityRule(Advancement advancement, boolean flag) {
      DisplayInfo displayinfo = advancement.getDisplay();
      if (displayinfo == null) {
         return AdvancementVisibilityEvaluator.VisibilityRule.HIDE;
      } else if (flag) {
         return AdvancementVisibilityEvaluator.VisibilityRule.SHOW;
      } else {
         return displayinfo.isHidden() ? AdvancementVisibilityEvaluator.VisibilityRule.HIDE : AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE;
      }
   }

   private static boolean evaluateVisiblityForUnfinishedNode(Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack) {
      for(int i = 0; i <= 2; ++i) {
         AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator_visibilityrule = stack.peek(i);
         if (advancementvisibilityevaluator_visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.SHOW) {
            return true;
         }

         if (advancementvisibilityevaluator_visibilityrule == AdvancementVisibilityEvaluator.VisibilityRule.HIDE) {
            return false;
         }
      }

      return false;
   }

   private static boolean evaluateVisibility(Advancement advancement, Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack, Predicate<Advancement> predicate, AdvancementVisibilityEvaluator.Output advancementvisibilityevaluator_output) {
      boolean flag = predicate.test(advancement);
      AdvancementVisibilityEvaluator.VisibilityRule advancementvisibilityevaluator_visibilityrule = evaluateVisibilityRule(advancement, flag);
      boolean flag1 = flag;
      stack.push(advancementvisibilityevaluator_visibilityrule);

      for(Advancement advancement1 : advancement.getChildren()) {
         flag1 |= evaluateVisibility(advancement1, stack, predicate, advancementvisibilityevaluator_output);
      }

      boolean flag2 = flag1 || evaluateVisiblityForUnfinishedNode(stack);
      stack.pop();
      advancementvisibilityevaluator_output.accept(advancement, flag2);
      return flag1;
   }

   public static void evaluateVisibility(Advancement advancement, Predicate<Advancement> predicate, AdvancementVisibilityEvaluator.Output advancementvisibilityevaluator_output) {
      Advancement advancement1 = advancement.getRoot();
      Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

      for(int i = 0; i <= 2; ++i) {
         stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
      }

      evaluateVisibility(advancement1, stack, predicate, advancementvisibilityevaluator_output);
   }

   @FunctionalInterface
   public interface Output {
      void accept(Advancement advancement, boolean flag);
   }

   static enum VisibilityRule {
      SHOW,
      HIDE,
      NO_CHANGE;
   }
}
