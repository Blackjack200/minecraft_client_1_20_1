package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.slf4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected static final String PLAYER = "Player";
   protected static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
   private LevelSettings settings;
   private final WorldOptions worldOptions;
   private final PrimaryLevelData.SpecialWorldProperty specialWorldProperty;
   private final Lifecycle worldGenSettingsLifecycle;
   private int xSpawn;
   private int ySpawn;
   private int zSpawn;
   private float spawnAngle;
   private long gameTime;
   private long dayTime;
   @Nullable
   private final DataFixer fixerUpper;
   private final int playerDataVersion;
   private boolean upgradedPlayerTag;
   @Nullable
   private CompoundTag loadedPlayerTag;
   private final int version;
   private int clearWeatherTime;
   private boolean raining;
   private int rainTime;
   private boolean thundering;
   private int thunderTime;
   private boolean initialized;
   private boolean difficultyLocked;
   private WorldBorder.Settings worldBorder;
   private EndDragonFight.Data endDragonFightData;
   @Nullable
   private CompoundTag customBossEvents;
   private int wanderingTraderSpawnDelay;
   private int wanderingTraderSpawnChance;
   @Nullable
   private UUID wanderingTraderId;
   private final Set<String> knownServerBrands;
   private boolean wasModded;
   private final Set<String> removedFeatureFlags;
   private final TimerQueue<MinecraftServer> scheduledEvents;

   private PrimaryLevelData(@Nullable DataFixer datafixer, int i, @Nullable CompoundTag compoundtag, boolean flag, int j, int k, int l, float f, long i1, long j1, int k1, int l1, int i2, boolean flag1, int j2, boolean flag2, boolean flag3, boolean flag4, WorldBorder.Settings worldborder_settings, int k2, int l2, @Nullable UUID uuid, Set<String> set, Set<String> set1, TimerQueue<MinecraftServer> timerqueue, @Nullable CompoundTag compoundtag1, EndDragonFight.Data enddragonfight_data, LevelSettings levelsettings, WorldOptions worldoptions, PrimaryLevelData.SpecialWorldProperty primaryleveldata_specialworldproperty, Lifecycle lifecycle) {
      this.fixerUpper = datafixer;
      this.wasModded = flag;
      this.xSpawn = j;
      this.ySpawn = k;
      this.zSpawn = l;
      this.spawnAngle = f;
      this.gameTime = i1;
      this.dayTime = j1;
      this.version = k1;
      this.clearWeatherTime = l1;
      this.rainTime = i2;
      this.raining = flag1;
      this.thunderTime = j2;
      this.thundering = flag2;
      this.initialized = flag3;
      this.difficultyLocked = flag4;
      this.worldBorder = worldborder_settings;
      this.wanderingTraderSpawnDelay = k2;
      this.wanderingTraderSpawnChance = l2;
      this.wanderingTraderId = uuid;
      this.knownServerBrands = set;
      this.removedFeatureFlags = set1;
      this.loadedPlayerTag = compoundtag;
      this.playerDataVersion = i;
      this.scheduledEvents = timerqueue;
      this.customBossEvents = compoundtag1;
      this.endDragonFightData = enddragonfight_data;
      this.settings = levelsettings;
      this.worldOptions = worldoptions;
      this.specialWorldProperty = primaryleveldata_specialworldproperty;
      this.worldGenSettingsLifecycle = lifecycle;
   }

   public PrimaryLevelData(LevelSettings levelsettings, WorldOptions worldoptions, PrimaryLevelData.SpecialWorldProperty primaryleveldata_specialworldproperty, Lifecycle lifecycle) {
      this((DataFixer)null, SharedConstants.getCurrentVersion().getDataVersion().getVersion(), (CompoundTag)null, false, 0, 0, 0, 0.0F, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, WorldBorder.DEFAULT_SETTINGS, 0, 0, (UUID)null, Sets.newLinkedHashSet(), new HashSet<>(), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS), (CompoundTag)null, EndDragonFight.Data.DEFAULT, levelsettings.copy(), worldoptions, primaryleveldata_specialworldproperty, lifecycle);
   }

   public static <T> PrimaryLevelData parse(Dynamic<T> dynamic, DataFixer datafixer, int i, @Nullable CompoundTag compoundtag, LevelSettings levelsettings, LevelVersion levelversion, PrimaryLevelData.SpecialWorldProperty primaryleveldata_specialworldproperty, WorldOptions worldoptions, Lifecycle lifecycle) {
      long j = dynamic.get("Time").asLong(0L);
      return new PrimaryLevelData(datafixer, i, compoundtag, dynamic.get("WasModded").asBoolean(false), dynamic.get("SpawnX").asInt(0), dynamic.get("SpawnY").asInt(0), dynamic.get("SpawnZ").asInt(0), dynamic.get("SpawnAngle").asFloat(0.0F), j, dynamic.get("DayTime").asLong(j), levelversion.levelDataVersion(), dynamic.get("clearWeatherTime").asInt(0), dynamic.get("rainTime").asInt(0), dynamic.get("raining").asBoolean(false), dynamic.get("thunderTime").asInt(0), dynamic.get("thundering").asBoolean(false), dynamic.get("initialized").asBoolean(true), dynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Settings.read(dynamic, WorldBorder.DEFAULT_SETTINGS), dynamic.get("WanderingTraderSpawnDelay").asInt(0), dynamic.get("WanderingTraderSpawnChance").asInt(0), dynamic.get("WanderingTraderId").read(UUIDUtil.CODEC).result().orElse((UUID)null), dynamic.get("ServerBrands").asStream().flatMap((dynamic2) -> dynamic2.asString().result().stream()).collect(Collectors.toCollection(Sets::newLinkedHashSet)), dynamic.get("removed_features").asStream().flatMap((dynamic1) -> dynamic1.asString().result().stream()).collect(Collectors.toSet()), new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, dynamic.get("ScheduledEvents").asStream()), (CompoundTag)dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), dynamic.get("DragonFight").read(EndDragonFight.Data.CODEC).resultOrPartial(LOGGER::error).orElse(EndDragonFight.Data.DEFAULT), levelsettings, worldoptions, primaryleveldata_specialworldproperty, lifecycle);
   }

   public CompoundTag createTag(RegistryAccess registryaccess, @Nullable CompoundTag compoundtag) {
      this.updatePlayerTag();
      if (compoundtag == null) {
         compoundtag = this.loadedPlayerTag;
      }

      CompoundTag compoundtag1 = new CompoundTag();
      this.setTagData(registryaccess, compoundtag1, compoundtag);
      return compoundtag1;
   }

   private void setTagData(RegistryAccess registryaccess, CompoundTag compoundtag, @Nullable CompoundTag compoundtag1) {
      compoundtag.put("ServerBrands", stringCollectionToTag(this.knownServerBrands));
      compoundtag.putBoolean("WasModded", this.wasModded);
      if (!this.removedFeatureFlags.isEmpty()) {
         compoundtag.put("removed_features", stringCollectionToTag(this.removedFeatureFlags));
      }

      CompoundTag compoundtag2 = new CompoundTag();
      compoundtag2.putString("Name", SharedConstants.getCurrentVersion().getName());
      compoundtag2.putInt("Id", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
      compoundtag2.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
      compoundtag2.putString("Series", SharedConstants.getCurrentVersion().getDataVersion().getSeries());
      compoundtag.put("Version", compoundtag2);
      NbtUtils.addCurrentDataVersion(compoundtag);
      DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, registryaccess);
      WorldGenSettings.encode(dynamicops, this.worldOptions, registryaccess).resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error)).ifPresent((tag1) -> compoundtag.put("WorldGenSettings", tag1));
      compoundtag.putInt("GameType", this.settings.gameType().getId());
      compoundtag.putInt("SpawnX", this.xSpawn);
      compoundtag.putInt("SpawnY", this.ySpawn);
      compoundtag.putInt("SpawnZ", this.zSpawn);
      compoundtag.putFloat("SpawnAngle", this.spawnAngle);
      compoundtag.putLong("Time", this.gameTime);
      compoundtag.putLong("DayTime", this.dayTime);
      compoundtag.putLong("LastPlayed", Util.getEpochMillis());
      compoundtag.putString("LevelName", this.settings.levelName());
      compoundtag.putInt("version", 19133);
      compoundtag.putInt("clearWeatherTime", this.clearWeatherTime);
      compoundtag.putInt("rainTime", this.rainTime);
      compoundtag.putBoolean("raining", this.raining);
      compoundtag.putInt("thunderTime", this.thunderTime);
      compoundtag.putBoolean("thundering", this.thundering);
      compoundtag.putBoolean("hardcore", this.settings.hardcore());
      compoundtag.putBoolean("allowCommands", this.settings.allowCommands());
      compoundtag.putBoolean("initialized", this.initialized);
      this.worldBorder.write(compoundtag);
      compoundtag.putByte("Difficulty", (byte)this.settings.difficulty().getId());
      compoundtag.putBoolean("DifficultyLocked", this.difficultyLocked);
      compoundtag.put("GameRules", this.settings.gameRules().createTag());
      compoundtag.put("DragonFight", Util.getOrThrow(EndDragonFight.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.endDragonFightData), IllegalStateException::new));
      if (compoundtag1 != null) {
         compoundtag.put("Player", compoundtag1);
      }

      DataResult<Tag> dataresult = WorldDataConfiguration.CODEC.encodeStart(NbtOps.INSTANCE, this.settings.getDataConfiguration());
      dataresult.get().ifLeft((tag) -> compoundtag.merge((CompoundTag)tag)).ifRight((dataresult_partialresult) -> LOGGER.warn("Failed to encode configuration {}", (Object)dataresult_partialresult.message()));
      if (this.customBossEvents != null) {
         compoundtag.put("CustomBossEvents", this.customBossEvents);
      }

      compoundtag.put("ScheduledEvents", this.scheduledEvents.store());
      compoundtag.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
      compoundtag.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
      if (this.wanderingTraderId != null) {
         compoundtag.putUUID("WanderingTraderId", this.wanderingTraderId);
      }

   }

   private static ListTag stringCollectionToTag(Set<String> set) {
      ListTag listtag = new ListTag();
      set.stream().map(StringTag::valueOf).forEach(listtag::add);
      return listtag;
   }

   public int getXSpawn() {
      return this.xSpawn;
   }

   public int getYSpawn() {
      return this.ySpawn;
   }

   public int getZSpawn() {
      return this.zSpawn;
   }

   public float getSpawnAngle() {
      return this.spawnAngle;
   }

   public long getGameTime() {
      return this.gameTime;
   }

   public long getDayTime() {
      return this.dayTime;
   }

   private void updatePlayerTag() {
      if (!this.upgradedPlayerTag && this.loadedPlayerTag != null) {
         if (this.playerDataVersion < SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
            if (this.fixerUpper == null) {
               throw (NullPointerException)Util.pauseInIde(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
            }

            this.loadedPlayerTag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, this.loadedPlayerTag, this.playerDataVersion);
         }

         this.upgradedPlayerTag = true;
      }
   }

   public CompoundTag getLoadedPlayerTag() {
      this.updatePlayerTag();
      return this.loadedPlayerTag;
   }

   public void setXSpawn(int i) {
      this.xSpawn = i;
   }

   public void setYSpawn(int i) {
      this.ySpawn = i;
   }

   public void setZSpawn(int i) {
      this.zSpawn = i;
   }

   public void setSpawnAngle(float f) {
      this.spawnAngle = f;
   }

   public void setGameTime(long i) {
      this.gameTime = i;
   }

   public void setDayTime(long i) {
      this.dayTime = i;
   }

   public void setSpawn(BlockPos blockpos, float f) {
      this.xSpawn = blockpos.getX();
      this.ySpawn = blockpos.getY();
      this.zSpawn = blockpos.getZ();
      this.spawnAngle = f;
   }

   public String getLevelName() {
      return this.settings.levelName();
   }

   public int getVersion() {
      return this.version;
   }

   public int getClearWeatherTime() {
      return this.clearWeatherTime;
   }

   public void setClearWeatherTime(int i) {
      this.clearWeatherTime = i;
   }

   public boolean isThundering() {
      return this.thundering;
   }

   public void setThundering(boolean flag) {
      this.thundering = flag;
   }

   public int getThunderTime() {
      return this.thunderTime;
   }

   public void setThunderTime(int i) {
      this.thunderTime = i;
   }

   public boolean isRaining() {
      return this.raining;
   }

   public void setRaining(boolean flag) {
      this.raining = flag;
   }

   public int getRainTime() {
      return this.rainTime;
   }

   public void setRainTime(int i) {
      this.rainTime = i;
   }

   public GameType getGameType() {
      return this.settings.gameType();
   }

   public void setGameType(GameType gametype) {
      this.settings = this.settings.withGameType(gametype);
   }

   public boolean isHardcore() {
      return this.settings.hardcore();
   }

   public boolean getAllowCommands() {
      return this.settings.allowCommands();
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public void setInitialized(boolean flag) {
      this.initialized = flag;
   }

   public GameRules getGameRules() {
      return this.settings.gameRules();
   }

   public WorldBorder.Settings getWorldBorder() {
      return this.worldBorder;
   }

   public void setWorldBorder(WorldBorder.Settings worldborder_settings) {
      this.worldBorder = worldborder_settings;
   }

   public Difficulty getDifficulty() {
      return this.settings.difficulty();
   }

   public void setDifficulty(Difficulty difficulty) {
      this.settings = this.settings.withDifficulty(difficulty);
   }

   public boolean isDifficultyLocked() {
      return this.difficultyLocked;
   }

   public void setDifficultyLocked(boolean flag) {
      this.difficultyLocked = flag;
   }

   public TimerQueue<MinecraftServer> getScheduledEvents() {
      return this.scheduledEvents;
   }

   public void fillCrashReportCategory(CrashReportCategory crashreportcategory, LevelHeightAccessor levelheightaccessor) {
      ServerLevelData.super.fillCrashReportCategory(crashreportcategory, levelheightaccessor);
      WorldData.super.fillCrashReportCategory(crashreportcategory);
   }

   public WorldOptions worldGenOptions() {
      return this.worldOptions;
   }

   public boolean isFlatWorld() {
      return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.FLAT;
   }

   public boolean isDebugWorld() {
      return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
   }

   public Lifecycle worldGenSettingsLifecycle() {
      return this.worldGenSettingsLifecycle;
   }

   public EndDragonFight.Data endDragonFightData() {
      return this.endDragonFightData;
   }

   public void setEndDragonFightData(EndDragonFight.Data enddragonfight_data) {
      this.endDragonFightData = enddragonfight_data;
   }

   public WorldDataConfiguration getDataConfiguration() {
      return this.settings.getDataConfiguration();
   }

   public void setDataConfiguration(WorldDataConfiguration worlddataconfiguration) {
      this.settings = this.settings.withDataConfiguration(worlddataconfiguration);
   }

   @Nullable
   public CompoundTag getCustomBossEvents() {
      return this.customBossEvents;
   }

   public void setCustomBossEvents(@Nullable CompoundTag compoundtag) {
      this.customBossEvents = compoundtag;
   }

   public int getWanderingTraderSpawnDelay() {
      return this.wanderingTraderSpawnDelay;
   }

   public void setWanderingTraderSpawnDelay(int i) {
      this.wanderingTraderSpawnDelay = i;
   }

   public int getWanderingTraderSpawnChance() {
      return this.wanderingTraderSpawnChance;
   }

   public void setWanderingTraderSpawnChance(int i) {
      this.wanderingTraderSpawnChance = i;
   }

   @Nullable
   public UUID getWanderingTraderId() {
      return this.wanderingTraderId;
   }

   public void setWanderingTraderId(UUID uuid) {
      this.wanderingTraderId = uuid;
   }

   public void setModdedInfo(String s, boolean flag) {
      this.knownServerBrands.add(s);
      this.wasModded |= flag;
   }

   public boolean wasModded() {
      return this.wasModded;
   }

   public Set<String> getKnownServerBrands() {
      return ImmutableSet.copyOf(this.knownServerBrands);
   }

   public Set<String> getRemovedFeatureFlags() {
      return Set.copyOf(this.removedFeatureFlags);
   }

   public ServerLevelData overworldData() {
      return this;
   }

   public LevelSettings getLevelSettings() {
      return this.settings.copy();
   }

   /** @deprecated */
   @Deprecated
   public static enum SpecialWorldProperty {
      NONE,
      FLAT,
      DEBUG;
   }
}
