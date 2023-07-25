package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum GraphicsStatus implements OptionEnum {
   FAST(0, "options.graphics.fast"),
   FANCY(1, "options.graphics.fancy"),
   FABULOUS(2, "options.graphics.fabulous");

   private static final IntFunction<GraphicsStatus> BY_ID = ByIdMap.continuous(GraphicsStatus::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
   private final int id;
   private final String key;

   private GraphicsStatus(int i, String s) {
      this.id = i;
      this.key = s;
   }

   public int getId() {
      return this.id;
   }

   public String getKey() {
      return this.key;
   }

   public String toString() {
      String var10000;
      switch (this) {
         case FAST:
            var10000 = "fast";
            break;
         case FANCY:
            var10000 = "fancy";
            break;
         case FABULOUS:
            var10000 = "fabulous";
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public static GraphicsStatus byId(int i) {
      return BY_ID.apply(i);
   }
}
