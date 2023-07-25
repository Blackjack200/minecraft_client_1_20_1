package net.minecraft.client.telemetry;

import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public record TelemetryProperty<T>(String id, String exportKey, Codec<T> codec, TelemetryProperty.Exporter<T> exporter) {
   private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
   public static final TelemetryProperty<String> USER_ID = string("user_id", "userId");
   public static final TelemetryProperty<String> CLIENT_ID = string("client_id", "clientId");
   public static final TelemetryProperty<UUID> MINECRAFT_SESSION_ID = uuid("minecraft_session_id", "deviceSessionId");
   public static final TelemetryProperty<String> GAME_VERSION = string("game_version", "buildDisplayName");
   public static final TelemetryProperty<String> OPERATING_SYSTEM = string("operating_system", "buildPlatform");
   public static final TelemetryProperty<String> PLATFORM = string("platform", "platform");
   public static final TelemetryProperty<Boolean> CLIENT_MODDED = bool("client_modded", "clientModded");
   public static final TelemetryProperty<String> LAUNCHER_NAME = string("launcher_name", "launcherName");
   public static final TelemetryProperty<UUID> WORLD_SESSION_ID = uuid("world_session_id", "worldSessionId");
   public static final TelemetryProperty<Boolean> SERVER_MODDED = bool("server_modded", "serverModded");
   public static final TelemetryProperty<TelemetryProperty.ServerType> SERVER_TYPE = create("server_type", "serverType", TelemetryProperty.ServerType.CODEC, (telemetrypropertycontainer, s, telemetryproperty_servertype) -> telemetrypropertycontainer.addProperty(s, telemetryproperty_servertype.getSerializedName()));
   public static final TelemetryProperty<Boolean> OPT_IN = bool("opt_in", "isOptional");
   public static final TelemetryProperty<Instant> EVENT_TIMESTAMP_UTC = create("event_timestamp_utc", "eventTimestampUtc", ExtraCodecs.INSTANT_ISO8601, (telemetrypropertycontainer, s, instant) -> telemetrypropertycontainer.addProperty(s, TIMESTAMP_FORMATTER.format(instant)));
   public static final TelemetryProperty<TelemetryProperty.GameMode> GAME_MODE = create("game_mode", "playerGameMode", TelemetryProperty.GameMode.CODEC, (telemetrypropertycontainer, s, telemetryproperty_gamemode) -> telemetrypropertycontainer.addProperty(s, telemetryproperty_gamemode.id()));
   public static final TelemetryProperty<String> REALMS_MAP_CONTENT = string("realms_map_content", "realmsMapContent");
   public static final TelemetryProperty<Integer> SECONDS_SINCE_LOAD = integer("seconds_since_load", "secondsSinceLoad");
   public static final TelemetryProperty<Integer> TICKS_SINCE_LOAD = integer("ticks_since_load", "ticksSinceLoad");
   public static final TelemetryProperty<LongList> FRAME_RATE_SAMPLES = longSamples("frame_rate_samples", "serializedFpsSamples");
   public static final TelemetryProperty<LongList> RENDER_TIME_SAMPLES = longSamples("render_time_samples", "serializedRenderTimeSamples");
   public static final TelemetryProperty<LongList> USED_MEMORY_SAMPLES = longSamples("used_memory_samples", "serializedUsedMemoryKbSamples");
   public static final TelemetryProperty<Integer> NUMBER_OF_SAMPLES = integer("number_of_samples", "numSamples");
   public static final TelemetryProperty<Integer> RENDER_DISTANCE = integer("render_distance", "renderDistance");
   public static final TelemetryProperty<Integer> DEDICATED_MEMORY_KB = integer("dedicated_memory_kb", "dedicatedMemoryKb");
   public static final TelemetryProperty<Integer> WORLD_LOAD_TIME_MS = integer("world_load_time_ms", "worldLoadTimeMs");
   public static final TelemetryProperty<Boolean> NEW_WORLD = bool("new_world", "newWorld");
   public static final TelemetryProperty<GameLoadTimesEvent.Measurement> LOAD_TIME_TOTAL_TIME_MS = gameLoadMeasurement("load_time_total_time_ms", "loadTimeTotalTimeMs");
   public static final TelemetryProperty<GameLoadTimesEvent.Measurement> LOAD_TIME_PRE_WINDOW_MS = gameLoadMeasurement("load_time_pre_window_ms", "loadTimePreWindowMs");
   public static final TelemetryProperty<GameLoadTimesEvent.Measurement> LOAD_TIME_BOOTSTRAP_MS = gameLoadMeasurement("load_time_bootstrap_ms", "loadTimeBootstrapMs");
   public static final TelemetryProperty<GameLoadTimesEvent.Measurement> LOAD_TIME_LOADING_OVERLAY_MS = gameLoadMeasurement("load_time_loading_overlay_ms", "loadTimeLoadingOverlayMs");
   public static final TelemetryProperty<String> ADVANCEMENT_ID = string("advancement_id", "advancementId");
   public static final TelemetryProperty<Long> ADVANCEMENT_GAME_TIME = makeLong("advancement_game_time", "advancementGameTime");

   public static <T> TelemetryProperty<T> create(String s, String s1, Codec<T> codec, TelemetryProperty.Exporter<T> telemetryproperty_exporter) {
      return new TelemetryProperty<>(s, s1, codec, telemetryproperty_exporter);
   }

   public static TelemetryProperty<Boolean> bool(String s, String s1) {
      return create(s, s1, Codec.BOOL, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty<String> string(String s, String s1) {
      return create(s, s1, Codec.STRING, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty<Integer> integer(String s, String s1) {
      return create(s, s1, Codec.INT, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty<Long> makeLong(String s, String s1) {
      return create(s, s1, Codec.LONG, TelemetryPropertyContainer::addProperty);
   }

   public static TelemetryProperty<UUID> uuid(String s, String s1) {
      return create(s, s1, UUIDUtil.STRING_CODEC, (telemetrypropertycontainer, s2, uuid) -> telemetrypropertycontainer.addProperty(s2, uuid.toString()));
   }

   public static TelemetryProperty<GameLoadTimesEvent.Measurement> gameLoadMeasurement(String s, String s1) {
      return create(s, s1, GameLoadTimesEvent.Measurement.CODEC, (telemetrypropertycontainer, s2, gameloadtimesevent_measurement) -> telemetrypropertycontainer.addProperty(s2, gameloadtimesevent_measurement.millis()));
   }

   public static TelemetryProperty<LongList> longSamples(String s, String s1) {
      return create(s, s1, Codec.LONG.listOf().xmap(LongArrayList::new, Function.identity()), (telemetrypropertycontainer, s2, longlist) -> telemetrypropertycontainer.addProperty(s2, longlist.longStream().mapToObj(String::valueOf).collect(Collectors.joining(";"))));
   }

   public void export(TelemetryPropertyMap telemetrypropertymap, TelemetryPropertyContainer telemetrypropertycontainer) {
      T object = telemetrypropertymap.get(this);
      if (object != null) {
         this.exporter.apply(telemetrypropertycontainer, this.exportKey, object);
      } else {
         telemetrypropertycontainer.addNullProperty(this.exportKey);
      }

   }

   public MutableComponent title() {
      return Component.translatable("telemetry.property." + this.id + ".title");
   }

   public String toString() {
      return "TelemetryProperty[" + this.id + "]";
   }

   public interface Exporter<T> {
      void apply(TelemetryPropertyContainer telemetrypropertycontainer, String s, T object);
   }

   public static enum GameMode implements StringRepresentable {
      SURVIVAL("survival", 0),
      CREATIVE("creative", 1),
      ADVENTURE("adventure", 2),
      SPECTATOR("spectator", 6),
      HARDCORE("hardcore", 99);

      public static final Codec<TelemetryProperty.GameMode> CODEC = StringRepresentable.fromEnum(TelemetryProperty.GameMode::values);
      private final String key;
      private final int id;

      private GameMode(String s, int i) {
         this.key = s;
         this.id = i;
      }

      public int id() {
         return this.id;
      }

      public String getSerializedName() {
         return this.key;
      }
   }

   public static enum ServerType implements StringRepresentable {
      REALM("realm"),
      LOCAL("local"),
      OTHER("server");

      public static final Codec<TelemetryProperty.ServerType> CODEC = StringRepresentable.fromEnum(TelemetryProperty.ServerType::values);
      private final String key;

      private ServerType(String s) {
         this.key = s;
      }

      public String getSerializedName() {
         return this.key;
      }
   }
}
