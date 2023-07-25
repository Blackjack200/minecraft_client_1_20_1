package com.mojang.realmsclient.util;

public class WorldGenerationInfo {
   private final String seed;
   private final LevelType levelType;
   private final boolean generateStructures;

   public WorldGenerationInfo(String s, LevelType leveltype, boolean flag) {
      this.seed = s;
      this.levelType = leveltype;
      this.generateStructures = flag;
   }

   public String getSeed() {
      return this.seed;
   }

   public LevelType getLevelType() {
      return this.levelType;
   }

   public boolean shouldGenerateStructures() {
      return this.generateStructures;
   }
}
