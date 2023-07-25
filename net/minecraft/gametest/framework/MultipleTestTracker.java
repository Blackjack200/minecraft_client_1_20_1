package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class MultipleTestTracker {
   private static final char NOT_STARTED_TEST_CHAR = ' ';
   private static final char ONGOING_TEST_CHAR = '_';
   private static final char SUCCESSFUL_TEST_CHAR = '+';
   private static final char FAILED_OPTIONAL_TEST_CHAR = 'x';
   private static final char FAILED_REQUIRED_TEST_CHAR = 'X';
   private final Collection<GameTestInfo> tests = Lists.newArrayList();
   @Nullable
   private final Collection<GameTestListener> listeners = Lists.newArrayList();

   public MultipleTestTracker() {
   }

   public MultipleTestTracker(Collection<GameTestInfo> collection) {
      this.tests.addAll(collection);
   }

   public void addTestToTrack(GameTestInfo gametestinfo) {
      this.tests.add(gametestinfo);
      this.listeners.forEach(gametestinfo::addListener);
   }

   public void addListener(GameTestListener gametestlistener) {
      this.listeners.add(gametestlistener);
      this.tests.forEach((gametestinfo) -> gametestinfo.addListener(gametestlistener));
   }

   public void addFailureListener(final Consumer<GameTestInfo> consumer) {
      this.addListener(new GameTestListener() {
         public void testStructureLoaded(GameTestInfo gametestinfo) {
         }

         public void testPassed(GameTestInfo gametestinfo) {
         }

         public void testFailed(GameTestInfo gametestinfo) {
            consumer.accept(gametestinfo);
         }
      });
   }

   public int getFailedRequiredCount() {
      return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).count();
   }

   public int getFailedOptionalCount() {
      return (int)this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).count();
   }

   public int getDoneCount() {
      return (int)this.tests.stream().filter(GameTestInfo::isDone).count();
   }

   public boolean hasFailedRequired() {
      return this.getFailedRequiredCount() > 0;
   }

   public boolean hasFailedOptional() {
      return this.getFailedOptionalCount() > 0;
   }

   public Collection<GameTestInfo> getFailedRequired() {
      return this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isRequired).collect(Collectors.toList());
   }

   public Collection<GameTestInfo> getFailedOptional() {
      return this.tests.stream().filter(GameTestInfo::hasFailed).filter(GameTestInfo::isOptional).collect(Collectors.toList());
   }

   public int getTotalCount() {
      return this.tests.size();
   }

   public boolean isDone() {
      return this.getDoneCount() == this.getTotalCount();
   }

   public String getProgressBar() {
      StringBuffer stringbuffer = new StringBuffer();
      stringbuffer.append('[');
      this.tests.forEach((gametestinfo) -> {
         if (!gametestinfo.hasStarted()) {
            stringbuffer.append(' ');
         } else if (gametestinfo.hasSucceeded()) {
            stringbuffer.append('+');
         } else if (gametestinfo.hasFailed()) {
            stringbuffer.append((char)(gametestinfo.isRequired() ? 'X' : 'x'));
         } else {
            stringbuffer.append('_');
         }

      });
      stringbuffer.append(']');
      return stringbuffer.toString();
   }

   public String toString() {
      return this.getProgressBar();
   }
}
