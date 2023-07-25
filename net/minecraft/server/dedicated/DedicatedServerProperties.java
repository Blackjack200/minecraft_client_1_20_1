package net.minecraft.server.dedicated;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

public class DedicatedServerProperties extends Settings<DedicatedServerProperties> {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
   private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
   public final boolean onlineMode = this.get("online-mode", true);
   public final boolean preventProxyConnections = this.get("prevent-proxy-connections", false);
   public final String serverIp = this.get("server-ip", "");
   public final boolean spawnAnimals = this.get("spawn-animals", true);
   public final boolean spawnNpcs = this.get("spawn-npcs", true);
   public final boolean pvp = this.get("pvp", true);
   public final boolean allowFlight = this.get("allow-flight", false);
   public final String motd = this.get("motd", "A Minecraft Server");
   public final boolean forceGameMode = this.get("force-gamemode", false);
   public final boolean enforceWhitelist = this.get("enforce-whitelist", false);
   public final Difficulty difficulty = this.get("difficulty", dispatchNumberOrString(Difficulty::byId, Difficulty::byName), Difficulty::getKey, Difficulty.EASY);
   public final GameType gamemode = this.get("gamemode", dispatchNumberOrString(GameType::byId, GameType::byName), GameType::getName, GameType.SURVIVAL);
   public final String levelName = this.get("level-name", "world");
   public final int serverPort = this.get("server-port", 25565);
   @Nullable
   public final Boolean announcePlayerAchievements = this.getLegacyBoolean("announce-player-achievements");
   public final boolean enableQuery = this.get("enable-query", false);
   public final int queryPort = this.get("query.port", 25565);
   public final boolean enableRcon = this.get("enable-rcon", false);
   public final int rconPort = this.get("rcon.port", 25575);
   public final String rconPassword = this.get("rcon.password", "");
   public final boolean hardcore = this.get("hardcore", false);
   public final boolean allowNether = this.get("allow-nether", true);
   public final boolean spawnMonsters = this.get("spawn-monsters", true);
   public final boolean useNativeTransport = this.get("use-native-transport", true);
   public final boolean enableCommandBlock = this.get("enable-command-block", false);
   public final int spawnProtection = this.get("spawn-protection", 16);
   public final int opPermissionLevel = this.get("op-permission-level", 4);
   public final int functionPermissionLevel = this.get("function-permission-level", 2);
   public final long maxTickTime = this.get("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
   public final int maxChainedNeighborUpdates = this.get("max-chained-neighbor-updates", 1000000);
   public final int rateLimitPacketsPerSecond = this.get("rate-limit", 0);
   public final int viewDistance = this.get("view-distance", 10);
   public final int simulationDistance = this.get("simulation-distance", 10);
   public final int maxPlayers = this.get("max-players", 20);
   public final int networkCompressionThreshold = this.get("network-compression-threshold", 256);
   public final boolean broadcastRconToOps = this.get("broadcast-rcon-to-ops", true);
   public final boolean broadcastConsoleToOps = this.get("broadcast-console-to-ops", true);
   public final int maxWorldSize = this.get("max-world-size", (integer1) -> Mth.clamp(integer1, 1, 29999984), 29999984);
   public final boolean syncChunkWrites = this.get("sync-chunk-writes", true);
   public final boolean enableJmxMonitoring = this.get("enable-jmx-monitoring", false);
   public final boolean enableStatus = this.get("enable-status", true);
   public final boolean hideOnlinePlayers = this.get("hide-online-players", false);
   public final int entityBroadcastRangePercentage = this.get("entity-broadcast-range-percentage", (integer) -> Mth.clamp(integer, 10, 1000), 100);
   public final String textFilteringConfig = this.get("text-filtering-config", "");
   public final Optional<MinecraftServer.ServerResourcePackInfo> serverResourcePackInfo;
   public final DataPackConfig initialDataPackConfiguration;
   public final Settings<DedicatedServerProperties>.MutableValue<Integer> playerIdleTimeout = this.getMutable("player-idle-timeout", 0);
   public final Settings<DedicatedServerProperties>.MutableValue<Boolean> whiteList = this.getMutable("white-list", false);
   public final boolean enforceSecureProfile = this.get("enforce-secure-profile", true);
   private final DedicatedServerProperties.WorldDimensionData worldDimensionData;
   public final WorldOptions worldOptions;

   public DedicatedServerProperties(Properties properties) {
      super(properties);
      String s = this.get("level-seed", "");
      boolean flag = this.get("generate-structures", true);
      long i = WorldOptions.parseSeed(s).orElse(WorldOptions.randomSeed());
      this.worldOptions = new WorldOptions(i, flag, false);
      this.worldDimensionData = new DedicatedServerProperties.WorldDimensionData(this.get("generator-settings", (s2) -> GsonHelper.parse(!s2.isEmpty() ? s2 : "{}"), new JsonObject()), this.get("level-type", (s1) -> s1.toLowerCase(Locale.ROOT), WorldPresets.NORMAL.location().toString()));
      this.serverResourcePackInfo = getServerPackInfo(this.get("resource-pack", ""), this.get("resource-pack-sha1", ""), this.getLegacyString("resource-pack-hash"), this.get("require-resource-pack", false), this.get("resource-pack-prompt", ""));
      this.initialDataPackConfiguration = getDatapackConfig(this.get("initial-enabled-packs", String.join(",", WorldDataConfiguration.DEFAULT.dataPacks().getEnabled())), this.get("initial-disabled-packs", String.join(",", WorldDataConfiguration.DEFAULT.dataPacks().getDisabled())));
   }

   public static DedicatedServerProperties fromFile(Path path) {
      return new DedicatedServerProperties(loadFromFile(path));
   }

   protected DedicatedServerProperties reload(RegistryAccess registryaccess, Properties properties) {
      return new DedicatedServerProperties(properties);
   }

   @Nullable
   private static Component parseResourcePackPrompt(String s) {
      if (!Strings.isNullOrEmpty(s)) {
         try {
            return Component.Serializer.fromJson(s);
         } catch (Exception var2) {
            LOGGER.warn("Failed to parse resource pack prompt '{}'", s, var2);
         }
      }

      return null;
   }

   private static Optional<MinecraftServer.ServerResourcePackInfo> getServerPackInfo(String s, String s1, @Nullable String s2, boolean flag, String s3) {
      if (s.isEmpty()) {
         return Optional.empty();
      } else {
         String s4;
         if (!s1.isEmpty()) {
            s4 = s1;
            if (!Strings.isNullOrEmpty(s2)) {
               LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            }
         } else if (!Strings.isNullOrEmpty(s2)) {
            LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
            s4 = s2;
         } else {
            s4 = "";
         }

         if (s4.isEmpty()) {
            LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
         } else if (!SHA1.matcher(s4).matches()) {
            LOGGER.warn("Invalid sha1 for resource-pack-sha1");
         }

         Component component = parseResourcePackPrompt(s3);
         return Optional.of(new MinecraftServer.ServerResourcePackInfo(s, s4, flag, component));
      }
   }

   private static DataPackConfig getDatapackConfig(String s, String s1) {
      List<String> list = COMMA_SPLITTER.splitToList(s);
      List<String> list1 = COMMA_SPLITTER.splitToList(s1);
      return new DataPackConfig(list, list1);
   }

   private static FeatureFlagSet getFeatures(String s) {
      return FeatureFlags.REGISTRY.fromNames(COMMA_SPLITTER.splitToStream(s).mapMulti((s1, consumer) -> {
         ResourceLocation resourcelocation = ResourceLocation.tryParse(s1);
         if (resourcelocation == null) {
            LOGGER.warn("Invalid resource location {}, ignoring", (Object)s1);
         } else {
            consumer.accept(resourcelocation);
         }

      }).collect(Collectors.toList()));
   }

   public WorldDimensions createDimensions(RegistryAccess registryaccess) {
      return this.worldDimensionData.create(registryaccess);
   }

   static record WorldDimensionData(JsonObject generatorSettings, String levelType) {
      private static final Map<String, ResourceKey<WorldPreset>> LEGACY_PRESET_NAMES = Map.of("default", WorldPresets.NORMAL, "largebiomes", WorldPresets.LARGE_BIOMES);

      public WorldDimensions create(RegistryAccess registryaccess) {
         Registry<WorldPreset> registry = registryaccess.registryOrThrow(Registries.WORLD_PRESET);
         Holder.Reference<WorldPreset> holder_reference = registry.getHolder(WorldPresets.NORMAL).or(() -> registry.holders().findAny()).orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default preset"));
         Holder<WorldPreset> holder = Optional.ofNullable(ResourceLocation.tryParse(this.levelType)).map((resourcelocation) -> ResourceKey.create(Registries.WORLD_PRESET, resourcelocation)).or(() -> Optional.ofNullable(LEGACY_PRESET_NAMES.get(this.levelType))).flatMap(registry::getHolder).orElseGet(() -> {
            DedicatedServerProperties.LOGGER.warn("Failed to parse level-type {}, defaulting to {}", this.levelType, holder_reference.key().location());
            return holder_reference;
         });
         WorldDimensions worlddimensions = holder.value().createWorldDimensions();
         if (holder.is(WorldPresets.FLAT)) {
            RegistryOps<JsonElement> registryops = RegistryOps.create(JsonOps.INSTANCE, registryaccess);
            Optional<FlatLevelGeneratorSettings> optional = FlatLevelGeneratorSettings.CODEC.parse(new Dynamic<>(registryops, this.generatorSettings())).resultOrPartial(DedicatedServerProperties.LOGGER::error);
            if (optional.isPresent()) {
               return worlddimensions.replaceOverworldGenerator(registryaccess, new FlatLevelSource(optional.get()));
            }
         }

         return worlddimensions;
      }
   }
}
