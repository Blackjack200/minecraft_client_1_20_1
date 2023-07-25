package net.minecraft.client.gui.components;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class LerpingBossEvent extends BossEvent {
   private static final long LERP_MILLISECONDS = 100L;
   protected float targetPercent;
   protected long setTime;

   public LerpingBossEvent(UUID uuid, Component component, float f, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay, boolean flag, boolean flag1, boolean flag2) {
      super(uuid, component, bossevent_bossbarcolor, bossevent_bossbaroverlay);
      this.targetPercent = f;
      this.progress = f;
      this.setTime = Util.getMillis();
      this.setDarkenScreen(flag);
      this.setPlayBossMusic(flag1);
      this.setCreateWorldFog(flag2);
   }

   public void setProgress(float f) {
      this.progress = this.getProgress();
      this.targetPercent = f;
      this.setTime = Util.getMillis();
   }

   public float getProgress() {
      long i = Util.getMillis() - this.setTime;
      float f = Mth.clamp((float)i / 100.0F, 0.0F, 1.0F);
      return Mth.lerp(f, this.progress, this.targetPercent);
   }
}
