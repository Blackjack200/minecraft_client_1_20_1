package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;

public class RealmsWorldResetDto extends ValueObject implements ReflectionBasedSerialization {
   @SerializedName("seed")
   private final String seed;
   @SerializedName("worldTemplateId")
   private final long worldTemplateId;
   @SerializedName("levelType")
   private final int levelType;
   @SerializedName("generateStructures")
   private final boolean generateStructures;

   public RealmsWorldResetDto(String s, long i, int j, boolean flag) {
      this.seed = s;
      this.worldTemplateId = i;
      this.levelType = j;
      this.generateStructures = flag;
   }
}
