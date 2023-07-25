package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class GameTestSequence {
   final GameTestInfo parent;
   private final List<GameTestEvent> events = Lists.newArrayList();
   private long lastTick;

   GameTestSequence(GameTestInfo gametestinfo) {
      this.parent = gametestinfo;
      this.lastTick = gametestinfo.getTick();
   }

   public GameTestSequence thenWaitUntil(Runnable runnable) {
      this.events.add(GameTestEvent.create(runnable));
      return this;
   }

   public GameTestSequence thenWaitUntil(long i, Runnable runnable) {
      this.events.add(GameTestEvent.create(i, runnable));
      return this;
   }

   public GameTestSequence thenIdle(int i) {
      return this.thenExecuteAfter(i, () -> {
      });
   }

   public GameTestSequence thenExecute(Runnable runnable) {
      this.events.add(GameTestEvent.create(() -> this.executeWithoutFail(runnable)));
      return this;
   }

   public GameTestSequence thenExecuteAfter(int i, Runnable runnable) {
      this.events.add(GameTestEvent.create(() -> {
         if (this.parent.getTick() < this.lastTick + (long)i) {
            throw new GameTestAssertException("Waiting");
         } else {
            this.executeWithoutFail(runnable);
         }
      }));
      return this;
   }

   public GameTestSequence thenExecuteFor(int i, Runnable runnable) {
      this.events.add(GameTestEvent.create(() -> {
         if (this.parent.getTick() < this.lastTick + (long)i) {
            this.executeWithoutFail(runnable);
            throw new GameTestAssertException("Waiting");
         }
      }));
      return this;
   }

   public void thenSucceed() {
      this.events.add(GameTestEvent.create(this.parent::succeed));
   }

   public void thenFail(Supplier<Exception> supplier) {
      this.events.add(GameTestEvent.create(() -> this.parent.fail(supplier.get())));
   }

   public GameTestSequence.Condition thenTrigger() {
      GameTestSequence.Condition gametestsequence_condition = new GameTestSequence.Condition();
      this.events.add(GameTestEvent.create(() -> gametestsequence_condition.trigger(this.parent.getTick())));
      return gametestsequence_condition;
   }

   public void tickAndContinue(long i) {
      try {
         this.tick(i);
      } catch (GameTestAssertException var4) {
      }

   }

   public void tickAndFailIfNotComplete(long i) {
      try {
         this.tick(i);
      } catch (GameTestAssertException var4) {
         this.parent.fail(var4);
      }

   }

   private void executeWithoutFail(Runnable runnable) {
      try {
         runnable.run();
      } catch (GameTestAssertException var3) {
         this.parent.fail(var3);
      }

   }

   private void tick(long i) {
      Iterator<GameTestEvent> iterator = this.events.iterator();

      while(iterator.hasNext()) {
         GameTestEvent gametestevent = iterator.next();
         gametestevent.assertion.run();
         iterator.remove();
         long j = i - this.lastTick;
         long k = this.lastTick;
         this.lastTick = i;
         if (gametestevent.expectedDelay != null && gametestevent.expectedDelay != j) {
            this.parent.fail(new GameTestAssertException("Succeeded in invalid tick: expected " + (k + gametestevent.expectedDelay) + ", but current tick is " + i));
            break;
         }
      }

   }

   public class Condition {
      private static final long NOT_TRIGGERED = -1L;
      private long triggerTime = -1L;

      void trigger(long i) {
         if (this.triggerTime != -1L) {
            throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
         } else {
            this.triggerTime = i;
         }
      }

      public void assertTriggeredThisTick() {
         long i = GameTestSequence.this.parent.getTick();
         if (this.triggerTime != i) {
            if (this.triggerTime == -1L) {
               throw new GameTestAssertException("Condition not triggered (t=" + i + ")");
            } else {
               throw new GameTestAssertException("Condition triggered at " + this.triggerTime + ", (t=" + i + ")");
            }
         }
      }
   }
}
