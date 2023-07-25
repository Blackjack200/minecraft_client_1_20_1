package net.minecraft.world.level;

import com.mojang.serialization.Dynamic;
import net.minecraft.world.Difficulty;

public final class LevelSettings {
   private final String levelName;
   private final GameType gameType;
   private final boolean hardcore;
   private final Difficulty difficulty;
   private final boolean allowCommands;
   private final GameRules gameRules;
   private final WorldDataConfiguration dataConfiguration;

   public LevelSettings(String s, GameType gametype, boolean flag, Difficulty difficulty, boolean flag1, GameRules gamerules, WorldDataConfiguration worlddataconfiguration) {
      this.levelName = s;
      this.gameType = gametype;
      this.hardcore = flag;
      this.difficulty = difficulty;
      this.allowCommands = flag1;
      this.gameRules = gamerules;
      this.dataConfiguration = worlddataconfiguration;
   }

   public static LevelSettings parse(Dynamic<?> dynamic, WorldDataConfiguration worlddataconfiguration) {
      GameType gametype = GameType.byId(dynamic.get("GameType").asInt(0));
      return new LevelSettings(dynamic.get("LevelName").asString(""), gametype, dynamic.get("hardcore").asBoolean(false), dynamic.get("Difficulty").asNumber().map((number) -> Difficulty.byId(number.byteValue())).result().orElse(Difficulty.NORMAL), dynamic.get("allowCommands").asBoolean(gametype == GameType.CREATIVE), new GameRules(dynamic.get("GameRules")), worlddataconfiguration);
   }

   public String levelName() {
      return this.levelName;
   }

   public GameType gameType() {
      return this.gameType;
   }

   public boolean hardcore() {
      return this.hardcore;
   }

   public Difficulty difficulty() {
      return this.difficulty;
   }

   public boolean allowCommands() {
      return this.allowCommands;
   }

   public GameRules gameRules() {
      return this.gameRules;
   }

   public WorldDataConfiguration getDataConfiguration() {
      return this.dataConfiguration;
   }

   public LevelSettings withGameType(GameType gametype) {
      return new LevelSettings(this.levelName, gametype, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataConfiguration);
   }

   public LevelSettings withDifficulty(Difficulty difficulty) {
      return new LevelSettings(this.levelName, this.gameType, this.hardcore, difficulty, this.allowCommands, this.gameRules, this.dataConfiguration);
   }

   public LevelSettings withDataConfiguration(WorldDataConfiguration worlddataconfiguration) {
      return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, worlddataconfiguration);
   }

   public LevelSettings copy() {
      return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.dataConfiguration);
   }
}
