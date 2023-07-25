package com.mojang.realmsclient.gui.task;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.TimeSource;
import org.slf4j.Logger;

public class DataFetcher {
   static final Logger LOGGER = LogUtils.getLogger();
   final Executor executor;
   final TimeUnit resolution;
   final TimeSource timeSource;

   public DataFetcher(Executor executor, TimeUnit timeunit, TimeSource timesource) {
      this.executor = executor;
      this.resolution = timeunit;
      this.timeSource = timesource;
   }

   public <T> DataFetcher.Task<T> createTask(String s, Callable<T> callable, Duration duration, RepeatedDelayStrategy repeateddelaystrategy) {
      long i = this.resolution.convert(duration);
      if (i == 0L) {
         throw new IllegalArgumentException("Period of " + duration + " too short for selected resolution of " + this.resolution);
      } else {
         return new DataFetcher.Task<>(s, callable, i, repeateddelaystrategy);
      }
   }

   public DataFetcher.Subscription createSubscription() {
      return new DataFetcher.Subscription();
   }

   static record ComputationResult<T>(Either<T, Exception> value, long time) {
      final long time;
   }

   class SubscribedTask<T> {
      private final DataFetcher.Task<T> task;
      private final Consumer<T> output;
      private long lastCheckTime = -1L;

      SubscribedTask(DataFetcher.Task<T> datafetcher_task, Consumer<T> consumer) {
         this.task = datafetcher_task;
         this.output = consumer;
      }

      void update(long i) {
         this.task.updateIfNeeded(i);
         this.runCallbackIfNeeded();
      }

      void runCallbackIfNeeded() {
         DataFetcher.SuccessfulComputationResult<T> datafetcher_successfulcomputationresult = this.task.lastResult;
         if (datafetcher_successfulcomputationresult != null && this.lastCheckTime < datafetcher_successfulcomputationresult.time) {
            this.output.accept(datafetcher_successfulcomputationresult.value);
            this.lastCheckTime = datafetcher_successfulcomputationresult.time;
         }

      }

      void runCallback() {
         DataFetcher.SuccessfulComputationResult<T> datafetcher_successfulcomputationresult = this.task.lastResult;
         if (datafetcher_successfulcomputationresult != null) {
            this.output.accept(datafetcher_successfulcomputationresult.value);
            this.lastCheckTime = datafetcher_successfulcomputationresult.time;
         }

      }

      void reset() {
         this.task.reset();
         this.lastCheckTime = -1L;
      }
   }

   public class Subscription {
      private final List<DataFetcher.SubscribedTask<?>> subscriptions = new ArrayList<>();

      public <T> void subscribe(DataFetcher.Task<T> datafetcher_task, Consumer<T> consumer) {
         DataFetcher.SubscribedTask<T> datafetcher_subscribedtask = DataFetcher.this.new SubscribedTask<>(datafetcher_task, consumer);
         this.subscriptions.add(datafetcher_subscribedtask);
         datafetcher_subscribedtask.runCallbackIfNeeded();
      }

      public void forceUpdate() {
         for(DataFetcher.SubscribedTask<?> datafetcher_subscribedtask : this.subscriptions) {
            datafetcher_subscribedtask.runCallback();
         }

      }

      public void tick() {
         for(DataFetcher.SubscribedTask<?> datafetcher_subscribedtask : this.subscriptions) {
            datafetcher_subscribedtask.update(DataFetcher.this.timeSource.get(DataFetcher.this.resolution));
         }

      }

      public void reset() {
         for(DataFetcher.SubscribedTask<?> datafetcher_subscribedtask : this.subscriptions) {
            datafetcher_subscribedtask.reset();
         }

      }
   }

   static record SuccessfulComputationResult<T>(T value, long time) {
      final T value;
      final long time;
   }

   public class Task<T> {
      private final String id;
      private final Callable<T> updater;
      private final long period;
      private final RepeatedDelayStrategy repeatStrategy;
      @Nullable
      private CompletableFuture<DataFetcher.ComputationResult<T>> pendingTask;
      @Nullable
      DataFetcher.SuccessfulComputationResult<T> lastResult;
      private long nextUpdate = -1L;

      Task(String s, Callable<T> callable, long i, RepeatedDelayStrategy repeateddelaystrategy) {
         this.id = s;
         this.updater = callable;
         this.period = i;
         this.repeatStrategy = repeateddelaystrategy;
      }

      void updateIfNeeded(long i) {
         if (this.pendingTask != null) {
            DataFetcher.ComputationResult<T> datafetcher_computationresult = this.pendingTask.getNow((DataFetcher.ComputationResult<T>)null);
            if (datafetcher_computationresult == null) {
               return;
            }

            this.pendingTask = null;
            long j = datafetcher_computationresult.time;
            datafetcher_computationresult.value().ifLeft((object1) -> {
               this.lastResult = new DataFetcher.SuccessfulComputationResult<>(object1, j);
               this.nextUpdate = j + this.period * this.repeatStrategy.delayCyclesAfterSuccess();
            }).ifRight((exception1) -> {
               long j1 = this.repeatStrategy.delayCyclesAfterFailure();
               DataFetcher.LOGGER.warn("Failed to process task {}, will repeat after {} cycles", this.id, j1, exception1);
               this.nextUpdate = j + this.period * j1;
            });
         }

         if (this.nextUpdate <= i) {
            this.pendingTask = CompletableFuture.supplyAsync(() -> {
               try {
                  T object = this.updater.call();
                  long k = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                  return new DataFetcher.ComputationResult<>(Either.left(object), k);
               } catch (Exception var4) {
                  long l = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                  return new DataFetcher.ComputationResult<>(Either.right(var4), l);
               }
            }, DataFetcher.this.executor);
         }

      }

      public void reset() {
         this.pendingTask = null;
         this.lastResult = null;
         this.nextUpdate = -1L;
      }
   }
}
