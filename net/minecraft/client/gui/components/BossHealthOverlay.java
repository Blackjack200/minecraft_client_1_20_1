package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;

public class BossHealthOverlay {
   private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
   private static final int BAR_WIDTH = 182;
   private static final int BAR_HEIGHT = 5;
   private static final int OVERLAY_OFFSET = 80;
   private final Minecraft minecraft;
   final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

   public BossHealthOverlay(Minecraft minecraft) {
      this.minecraft = minecraft;
   }

   public void render(GuiGraphics guigraphics) {
      if (!this.events.isEmpty()) {
         int i = guigraphics.guiWidth();
         int j = 12;

         for(LerpingBossEvent lerpingbossevent : this.events.values()) {
            int k = i / 2 - 91;
            this.drawBar(guigraphics, k, j, lerpingbossevent);
            Component component = lerpingbossevent.getName();
            int i1 = this.minecraft.font.width(component);
            int j1 = i / 2 - i1 / 2;
            int k1 = j - 9;
            guigraphics.drawString(this.minecraft.font, component, j1, k1, 16777215);
            j += 10 + 9;
            if (j >= guigraphics.guiHeight() / 3) {
               break;
            }
         }

      }
   }

   private void drawBar(GuiGraphics guigraphics, int i, int j, BossEvent bossevent) {
      this.drawBar(guigraphics, i, j, bossevent, 182, 0);
      int k = (int)(bossevent.getProgress() * 183.0F);
      if (k > 0) {
         this.drawBar(guigraphics, i, j, bossevent, k, 5);
      }

   }

   private void drawBar(GuiGraphics guigraphics, int i, int j, BossEvent bossevent, int k, int l) {
      guigraphics.blit(GUI_BARS_LOCATION, i, j, 0, bossevent.getColor().ordinal() * 5 * 2 + l, k, 5);
      if (bossevent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
         RenderSystem.enableBlend();
         guigraphics.blit(GUI_BARS_LOCATION, i, j, 0, 80 + (bossevent.getOverlay().ordinal() - 1) * 5 * 2 + l, k, 5);
         RenderSystem.disableBlend();
      }

   }

   public void update(ClientboundBossEventPacket clientboundbosseventpacket) {
      clientboundbosseventpacket.dispatch(new ClientboundBossEventPacket.Handler() {
         public void add(UUID uuid, Component component, float f, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay, boolean flag, boolean flag1, boolean flag2) {
            BossHealthOverlay.this.events.put(uuid, new LerpingBossEvent(uuid, component, f, bossevent_bossbarcolor, bossevent_bossbaroverlay, flag, flag1, flag2));
         }

         public void remove(UUID uuid) {
            BossHealthOverlay.this.events.remove(uuid);
         }

         public void updateProgress(UUID uuid, float f) {
            BossHealthOverlay.this.events.get(uuid).setProgress(f);
         }

         public void updateName(UUID uuid, Component component) {
            BossHealthOverlay.this.events.get(uuid).setName(component);
         }

         public void updateStyle(UUID uuid, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
            LerpingBossEvent lerpingbossevent = BossHealthOverlay.this.events.get(uuid);
            lerpingbossevent.setColor(bossevent_bossbarcolor);
            lerpingbossevent.setOverlay(bossevent_bossbaroverlay);
         }

         public void updateProperties(UUID uuid, boolean flag, boolean flag1, boolean flag2) {
            LerpingBossEvent lerpingbossevent = BossHealthOverlay.this.events.get(uuid);
            lerpingbossevent.setDarkenScreen(flag);
            lerpingbossevent.setPlayBossMusic(flag1);
            lerpingbossevent.setCreateWorldFog(flag2);
         }
      });
   }

   public void reset() {
      this.events.clear();
   }

   public boolean shouldPlayMusic() {
      if (!this.events.isEmpty()) {
         for(BossEvent bossevent : this.events.values()) {
            if (bossevent.shouldPlayBossMusic()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldDarkenScreen() {
      if (!this.events.isEmpty()) {
         for(BossEvent bossevent : this.events.values()) {
            if (bossevent.shouldDarkenScreen()) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean shouldCreateWorldFog() {
      if (!this.events.isEmpty()) {
         for(BossEvent bossevent : this.events.values()) {
            if (bossevent.shouldCreateWorldFog()) {
               return true;
            }
         }
      }

      return false;
   }
}
