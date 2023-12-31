package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Locale;

public class RegionPingResult extends ValueObject implements ReflectionBasedSerialization {
   @SerializedName("regionName")
   private final String regionName;
   @SerializedName("ping")
   private final int ping;

   public RegionPingResult(String s, int i) {
      this.regionName = s;
      this.ping = i;
   }

   public int ping() {
      return this.ping;
   }

   public String toString() {
      return String.format(Locale.ROOT, "%s --> %.2f ms", this.regionName, (float)this.ping);
   }
}
