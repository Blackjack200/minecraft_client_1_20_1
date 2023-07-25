package net.minecraft.client.telemetry;

import com.google.common.base.Suppliers;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

public class ClientTelemetryManager implements AutoCloseable {
   private static final AtomicInteger THREAD_COUNT = new AtomicInteger(1);
   private static final Executor EXECUTOR = Executors.newSingleThreadExecutor((runnable) -> {
      Thread thread = new Thread(runnable);
      thread.setName("Telemetry-Sender-#" + THREAD_COUNT.getAndIncrement());
      return thread;
   });
   private final UserApiService userApiService;
   private final TelemetryPropertyMap deviceSessionProperties;
   private final Path logDirectory;
   private final CompletableFuture<Optional<TelemetryLogManager>> logManager;
   private final Supplier<TelemetryEventSender> outsideSessionSender = Suppliers.memoize(this::createEventSender);

   public ClientTelemetryManager(Minecraft minecraft, UserApiService userapiservice, User user) {
      this.userApiService = userapiservice;
      TelemetryPropertyMap.Builder telemetrypropertymap_builder = TelemetryPropertyMap.builder();
      user.getXuid().ifPresent((s1) -> telemetrypropertymap_builder.put(TelemetryProperty.USER_ID, s1));
      user.getClientId().ifPresent((s) -> telemetrypropertymap_builder.put(TelemetryProperty.CLIENT_ID, s));
      telemetrypropertymap_builder.put(TelemetryProperty.MINECRAFT_SESSION_ID, UUID.randomUUID());
      telemetrypropertymap_builder.put(TelemetryProperty.GAME_VERSION, SharedConstants.getCurrentVersion().getId());
      telemetrypropertymap_builder.put(TelemetryProperty.OPERATING_SYSTEM, Util.getPlatform().telemetryName());
      telemetrypropertymap_builder.put(TelemetryProperty.PLATFORM, System.getProperty("os.name"));
      telemetrypropertymap_builder.put(TelemetryProperty.CLIENT_MODDED, Minecraft.checkModStatus().shouldReportAsModified());
      telemetrypropertymap_builder.putIfNotNull(TelemetryProperty.LAUNCHER_NAME, System.getProperty("minecraft.launcher.brand"));
      this.deviceSessionProperties = telemetrypropertymap_builder.build();
      this.logDirectory = minecraft.gameDirectory.toPath().resolve("logs/telemetry");
      this.logManager = TelemetryLogManager.open(this.logDirectory);
   }

   public WorldSessionTelemetryManager createWorldSessionManager(boolean flag, @Nullable Duration duration, @Nullable String s) {
      return new WorldSessionTelemetryManager(this.createEventSender(), flag, duration, s);
   }

   public TelemetryEventSender getOutsideSessionSender() {
      return this.outsideSessionSender.get();
   }

   private TelemetryEventSender createEventSender() {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         return TelemetryEventSender.DISABLED;
      } else {
         TelemetrySession telemetrysession = this.userApiService.newTelemetrySession(EXECUTOR);
         if (!telemetrysession.isEnabled()) {
            return TelemetryEventSender.DISABLED;
         } else {
            CompletableFuture<Optional<TelemetryEventLogger>> completablefuture = this.logManager.thenCompose((optional1) -> optional1.map(TelemetryLogManager::openLogger).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
            return (telemetryeventtype, consumer) -> {
               if (!telemetryeventtype.isOptIn() || Minecraft.getInstance().telemetryOptInExtra()) {
                  TelemetryPropertyMap.Builder telemetrypropertymap_builder1 = TelemetryPropertyMap.builder();
                  telemetrypropertymap_builder1.putAll(this.deviceSessionProperties);
                  telemetrypropertymap_builder1.put(TelemetryProperty.EVENT_TIMESTAMP_UTC, Instant.now());
                  telemetrypropertymap_builder1.put(TelemetryProperty.OPT_IN, telemetryeventtype.isOptIn());
                  consumer.accept(telemetrypropertymap_builder1);
                  TelemetryEventInstance telemetryeventinstance = new TelemetryEventInstance(telemetryeventtype, telemetrypropertymap_builder1.build());
                  completablefuture.thenAccept((optional) -> {
                     if (!optional.isEmpty()) {
                        optional.get().log(telemetryeventinstance);
                        telemetryeventinstance.export(telemetrysession).send();
                     }
                  });
               }
            };
         }
      }
   }

   public Path getLogDirectory() {
      return this.logDirectory;
   }

   public void close() {
      this.logManager.thenAccept((optional) -> optional.ifPresent(TelemetryLogManager::close));
   }
}
