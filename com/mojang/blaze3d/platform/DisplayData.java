package com.mojang.blaze3d.platform;

import java.util.OptionalInt;

public class DisplayData {
   public final int width;
   public final int height;
   public final OptionalInt fullscreenWidth;
   public final OptionalInt fullscreenHeight;
   public final boolean isFullscreen;

   public DisplayData(int i, int j, OptionalInt optionalint, OptionalInt optionalint1, boolean flag) {
      this.width = i;
      this.height = j;
      this.fullscreenWidth = optionalint;
      this.fullscreenHeight = optionalint1;
      this.isFullscreen = flag;
   }
}
