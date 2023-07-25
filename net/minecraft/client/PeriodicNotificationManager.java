package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class PeriodicNotificationManager extends SimplePreparableReloadListener<Map<String, List<PeriodicNotificationManager.Notification>>> implements AutoCloseable {
   private static final Codec<Map<String, List<PeriodicNotificationManager.Notification>>> CODEC = Codec.unboundedMap(Codec.STRING, RecordCodecBuilder.create((recordcodecbuilder_instance) -> recordcodecbuilder_instance.group(Codec.LONG.optionalFieldOf("delay", Long.valueOf(0L)).forGetter(PeriodicNotificationManager.Notification::delay), Codec.LONG.fieldOf("period").forGetter(PeriodicNotificationManager.Notification::period), Codec.STRING.fieldOf("title").forGetter(PeriodicNotificationManager.Notification::title), Codec.STRING.fieldOf("message").forGetter(PeriodicNotificationManager.Notification::message)).apply(recordcodecbuilder_instance, PeriodicNotificationManager.Notification::new)).listOf());
   private static final Logger LOGGER = LogUtils.getLogger();
   private final ResourceLocation notifications;
   private final Object2BooleanFunction<String> selector;
   @Nullable
   private java.util.Timer timer;
   @Nullable
   private PeriodicNotificationManager.NotificationTask notificationTask;

   public PeriodicNotificationManager(ResourceLocation resourcelocation, Object2BooleanFunction<String> object2booleanfunction) {
      this.notifications = resourcelocation;
      this.selector = object2booleanfunction;
   }

   protected Map<String, List<PeriodicNotificationManager.Notification>> prepare(ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      try {
         Reader reader = resourcemanager.openAsReader(this.notifications);

         Map var4;
         try {
            var4 = CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).result().orElseThrow();
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }

         return var4;
      } catch (Exception var8) {
         LOGGER.warn("Failed to load {}", this.notifications, var8);
         return ImmutableMap.of();
      }
   }

   protected void apply(Map<String, List<PeriodicNotificationManager.Notification>> map, ResourceManager resourcemanager, ProfilerFiller profilerfiller) {
      List<PeriodicNotificationManager.Notification> list = map.entrySet().stream().filter((map_entry) -> this.selector.apply(map_entry.getKey())).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toList());
      if (list.isEmpty()) {
         this.stopTimer();
      } else if (list.stream().anyMatch((periodicnotificationmanager_notification) -> periodicnotificationmanager_notification.period == 0L)) {
         Util.logAndPauseIfInIde("A periodic notification in " + this.notifications + " has a period of zero minutes");
         this.stopTimer();
      } else {
         long i = this.calculateInitialDelay(list);
         long j = this.calculateOptimalPeriod(list, i);
         if (this.timer == null) {
            this.timer = new java.util.Timer();
         }

         if (this.notificationTask == null) {
            this.notificationTask = new PeriodicNotificationManager.NotificationTask(list, i, j);
         } else {
            this.notificationTask = this.notificationTask.reset(list, j);
         }

         this.timer.scheduleAtFixedRate(this.notificationTask, TimeUnit.MINUTES.toMillis(i), TimeUnit.MINUTES.toMillis(j));
      }
   }

   public void close() {
      this.stopTimer();
   }

   private void stopTimer() {
      if (this.timer != null) {
         this.timer.cancel();
      }

   }

   private long calculateOptimalPeriod(List<PeriodicNotificationManager.Notification> list, long i) {
      return list.stream().mapToLong((periodicnotificationmanager_notification) -> {
         long k = periodicnotificationmanager_notification.delay - i;
         return LongMath.gcd(k, periodicnotificationmanager_notification.period);
      }).reduce(LongMath::gcd).orElseThrow(() -> new IllegalStateException("Empty notifications from: " + this.notifications));
   }

   private long calculateInitialDelay(List<PeriodicNotificationManager.Notification> list) {
      return list.stream().mapToLong((periodicnotificationmanager_notification) -> periodicnotificationmanager_notification.delay).min().orElse(0L);
   }

   public static record Notification(long delay, long period, String title, String message) {
      final long delay;
      final long period;
      final String title;
      final String message;

      public Notification(long i, long j, String s, String s1) {
         this.delay = i != 0L ? i : j;
         this.period = j;
         this.title = s;
         this.message = s1;
      }
   }

   static class NotificationTask extends TimerTask {
      private final Minecraft minecraft = Minecraft.getInstance();
      private final List<PeriodicNotificationManager.Notification> notifications;
      private final long period;
      private final AtomicLong elapsed;

      public NotificationTask(List<PeriodicNotificationManager.Notification> list, long i, long j) {
         this.notifications = list;
         this.period = j;
         this.elapsed = new AtomicLong(i);
      }

      public PeriodicNotificationManager.NotificationTask reset(List<PeriodicNotificationManager.Notification> list, long i) {
         this.cancel();
         return new PeriodicNotificationManager.NotificationTask(list, this.elapsed.get(), i);
      }

      public void run() {
         long i = this.elapsed.getAndAdd(this.period);
         long j = this.elapsed.get();

         for(PeriodicNotificationManager.Notification periodicnotificationmanager_notification : this.notifications) {
            if (i >= periodicnotificationmanager_notification.delay) {
               long k = i / periodicnotificationmanager_notification.period;
               long l = j / periodicnotificationmanager_notification.period;
               if (k != l) {
                  this.minecraft.execute(() -> SystemToast.add(Minecraft.getInstance().getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable(periodicnotificationmanager_notification.title, k), Component.translatable(periodicnotificationmanager_notification.message, k)));
                  return;
               }
            }
         }

      }
   }
}
