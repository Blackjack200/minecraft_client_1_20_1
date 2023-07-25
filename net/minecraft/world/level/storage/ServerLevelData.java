package net.minecraft.world.level.storage;

import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public interface ServerLevelData extends WritableLevelData {
   String getLevelName();

   void setThundering(boolean flag);

   int getRainTime();

   void setRainTime(int i);

   void setThunderTime(int i);

   int getThunderTime();

   default void fillCrashReportCategory(CrashReportCategory crashreportcategory, LevelHeightAccessor levelheightaccessor) {
      WritableLevelData.super.fillCrashReportCategory(crashreportcategory, levelheightaccessor);
      crashreportcategory.setDetail("Level name", this::getLevelName);
      crashreportcategory.setDetail("Level game mode", () -> String.format(Locale.ROOT, "Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands()));
      crashreportcategory.setDetail("Level weather", () -> String.format(Locale.ROOT, "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()));
   }

   int getClearWeatherTime();

   void setClearWeatherTime(int i);

   int getWanderingTraderSpawnDelay();

   void setWanderingTraderSpawnDelay(int i);

   int getWanderingTraderSpawnChance();

   void setWanderingTraderSpawnChance(int i);

   @Nullable
   UUID getWanderingTraderId();

   void setWanderingTraderId(UUID uuid);

   GameType getGameType();

   void setWorldBorder(WorldBorder.Settings worldborder_settings);

   WorldBorder.Settings getWorldBorder();

   boolean isInitialized();

   void setInitialized(boolean flag);

   boolean getAllowCommands();

   void setGameType(GameType gametype);

   TimerQueue<MinecraftServer> getScheduledEvents();

   void setGameTime(long i);

   void setDayTime(long i);
}
