package net.minecraft.util.thread;

public abstract class ReentrantBlockableEventLoop<R extends Runnable> extends BlockableEventLoop<R> {
   private int reentrantCount;

   public ReentrantBlockableEventLoop(String s) {
      super(s);
   }

   public boolean scheduleExecutables() {
      return this.runningTask() || super.scheduleExecutables();
   }

   protected boolean runningTask() {
      return this.reentrantCount != 0;
   }

   public void doRunTask(R runnable) {
      ++this.reentrantCount;

      try {
         super.doRunTask(runnable);
      } finally {
         --this.reentrantCount;
      }

   }
}
