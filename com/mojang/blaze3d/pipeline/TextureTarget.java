package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.systems.RenderSystem;

public class TextureTarget extends RenderTarget {
   public TextureTarget(int i, int j, boolean flag, boolean flag1) {
      super(flag);
      RenderSystem.assertOnRenderThreadOrInit();
      this.resize(i, j, flag1);
   }
}
