package net.minecraft.util.profiling;

import java.util.function.Supplier;
import net.minecraft.util.profiling.metrics.MetricCategory;

public interface ProfilerFiller {
   String ROOT = "root";

   void startTick();

   void endTick();

   void push(String s);

   void push(Supplier<String> supplier);

   void pop();

   void popPush(String s);

   void popPush(Supplier<String> supplier);

   void markForCharting(MetricCategory metriccategory);

   default void incrementCounter(String s) {
      this.incrementCounter(s, 1);
   }

   void incrementCounter(String s, int i);

   default void incrementCounter(Supplier<String> supplier) {
      this.incrementCounter(supplier, 1);
   }

   void incrementCounter(Supplier<String> supplier, int i);

   static ProfilerFiller tee(final ProfilerFiller profilerfiller, final ProfilerFiller profilerfiller1) {
      if (profilerfiller == InactiveProfiler.INSTANCE) {
         return profilerfiller1;
      } else {
         return profilerfiller1 == InactiveProfiler.INSTANCE ? profilerfiller : new ProfilerFiller() {
            public void startTick() {
               profilerfiller.startTick();
               profilerfiller1.startTick();
            }

            public void endTick() {
               profilerfiller.endTick();
               profilerfiller1.endTick();
            }

            public void push(String s) {
               profilerfiller.push(s);
               profilerfiller1.push(s);
            }

            public void push(Supplier<String> supplier) {
               profilerfiller.push(supplier);
               profilerfiller1.push(supplier);
            }

            public void markForCharting(MetricCategory metriccategory) {
               profilerfiller.markForCharting(metriccategory);
               profilerfiller1.markForCharting(metriccategory);
            }

            public void pop() {
               profilerfiller.pop();
               profilerfiller1.pop();
            }

            public void popPush(String s) {
               profilerfiller.popPush(s);
               profilerfiller1.popPush(s);
            }

            public void popPush(Supplier<String> supplier) {
               profilerfiller.popPush(supplier);
               profilerfiller1.popPush(supplier);
            }

            public void incrementCounter(String s, int i) {
               profilerfiller.incrementCounter(s, i);
               profilerfiller1.incrementCounter(s, i);
            }

            public void incrementCounter(Supplier<String> supplier, int i) {
               profilerfiller.incrementCounter(supplier, i);
               profilerfiller1.incrementCounter(supplier, i);
            }
         };
      }
   }
}
