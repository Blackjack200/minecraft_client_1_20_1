package net.minecraft.client.model;

import net.minecraft.client.renderer.RenderType;

public abstract class SkullModelBase extends Model {
   public SkullModelBase() {
      super(RenderType::entityTranslucent);
   }

   public abstract void setupAnim(float f, float f1, float f2);
}
