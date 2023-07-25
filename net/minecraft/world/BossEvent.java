package net.minecraft.world;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class BossEvent {
   private final UUID id;
   protected Component name;
   protected float progress;
   protected BossEvent.BossBarColor color;
   protected BossEvent.BossBarOverlay overlay;
   protected boolean darkenScreen;
   protected boolean playBossMusic;
   protected boolean createWorldFog;

   public BossEvent(UUID uuid, Component component, BossEvent.BossBarColor bossevent_bossbarcolor, BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
      this.id = uuid;
      this.name = component;
      this.color = bossevent_bossbarcolor;
      this.overlay = bossevent_bossbaroverlay;
      this.progress = 1.0F;
   }

   public UUID getId() {
      return this.id;
   }

   public Component getName() {
      return this.name;
   }

   public void setName(Component component) {
      this.name = component;
   }

   public float getProgress() {
      return this.progress;
   }

   public void setProgress(float f) {
      this.progress = f;
   }

   public BossEvent.BossBarColor getColor() {
      return this.color;
   }

   public void setColor(BossEvent.BossBarColor bossevent_bossbarcolor) {
      this.color = bossevent_bossbarcolor;
   }

   public BossEvent.BossBarOverlay getOverlay() {
      return this.overlay;
   }

   public void setOverlay(BossEvent.BossBarOverlay bossevent_bossbaroverlay) {
      this.overlay = bossevent_bossbaroverlay;
   }

   public boolean shouldDarkenScreen() {
      return this.darkenScreen;
   }

   public BossEvent setDarkenScreen(boolean flag) {
      this.darkenScreen = flag;
      return this;
   }

   public boolean shouldPlayBossMusic() {
      return this.playBossMusic;
   }

   public BossEvent setPlayBossMusic(boolean flag) {
      this.playBossMusic = flag;
      return this;
   }

   public BossEvent setCreateWorldFog(boolean flag) {
      this.createWorldFog = flag;
      return this;
   }

   public boolean shouldCreateWorldFog() {
      return this.createWorldFog;
   }

   public static enum BossBarColor {
      PINK("pink", ChatFormatting.RED),
      BLUE("blue", ChatFormatting.BLUE),
      RED("red", ChatFormatting.DARK_RED),
      GREEN("green", ChatFormatting.GREEN),
      YELLOW("yellow", ChatFormatting.YELLOW),
      PURPLE("purple", ChatFormatting.DARK_BLUE),
      WHITE("white", ChatFormatting.WHITE);

      private final String name;
      private final ChatFormatting formatting;

      private BossBarColor(String s, ChatFormatting chatformatting) {
         this.name = s;
         this.formatting = chatformatting;
      }

      public ChatFormatting getFormatting() {
         return this.formatting;
      }

      public String getName() {
         return this.name;
      }

      public static BossEvent.BossBarColor byName(String s) {
         for(BossEvent.BossBarColor bossevent_bossbarcolor : values()) {
            if (bossevent_bossbarcolor.name.equals(s)) {
               return bossevent_bossbarcolor;
            }
         }

         return WHITE;
      }
   }

   public static enum BossBarOverlay {
      PROGRESS("progress"),
      NOTCHED_6("notched_6"),
      NOTCHED_10("notched_10"),
      NOTCHED_12("notched_12"),
      NOTCHED_20("notched_20");

      private final String name;

      private BossBarOverlay(String s) {
         this.name = s;
      }

      public String getName() {
         return this.name;
      }

      public static BossEvent.BossBarOverlay byName(String s) {
         for(BossEvent.BossBarOverlay bossevent_bossbaroverlay : values()) {
            if (bossevent_bossbaroverlay.name.equals(s)) {
               return bossevent_bossbaroverlay;
            }
         }

         return PROGRESS;
      }
   }
}
