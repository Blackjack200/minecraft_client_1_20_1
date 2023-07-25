package net.minecraft.client;

import net.minecraft.util.OptionEnum;

public enum CloudStatus implements OptionEnum {
   OFF(0, "options.off"),
   FAST(1, "options.clouds.fast"),
   FANCY(2, "options.clouds.fancy");

   private final int id;
   private final String key;

   private CloudStatus(int i, String s) {
      this.id = i;
      this.key = s;
   }

   public int getId() {
      return this.id;
   }

   public String getKey() {
      return this.key;
   }
}
