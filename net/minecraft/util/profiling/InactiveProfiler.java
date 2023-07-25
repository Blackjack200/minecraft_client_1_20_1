package net.minecraft.util.profiling;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;

public class InactiveProfiler implements ProfileCollector {
   public static final InactiveProfiler INSTANCE = new InactiveProfiler();

   private InactiveProfiler() {
   }

   public void startTick() {
   }

   public void endTick() {
   }

   public void push(String s) {
   }

   public void push(Supplier<String> supplier) {
   }

   public void markForCharting(MetricCategory metriccategory) {
   }

   public void pop() {
   }

   public void popPush(String s) {
   }

   public void popPush(Supplier<String> supplier) {
   }

   public void incrementCounter(String s, int i) {
   }

   public void incrementCounter(Supplier<String> supplier, int i) {
   }

   public ProfileResults getResults() {
      return EmptyProfileResults.EMPTY;
   }

   @Nullable
   public ActiveProfiler.PathEntry getEntry(String s) {
      return null;
   }

   public Set<Pair<String, MetricCategory>> getChartedPaths() {
      return ImmutableSet.of();
   }
}
