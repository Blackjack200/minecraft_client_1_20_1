package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.Backup;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsBackupInfoScreen extends RealmsScreen {
   private static final Component UNKNOWN = Component.translatable("mco.backup.unknown");
   private final Screen lastScreen;
   final Backup backup;
   private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

   public RealmsBackupInfoScreen(Screen screen, Backup backup) {
      super(Component.translatable("mco.backup.info.title"));
      this.lastScreen = screen;
      this.backup = backup;
   }

   public void tick() {
   }

   public void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20).build());
      this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
      this.addWidget(this.backupInfoList);
      this.magicalSpecialHackyFocus(this.backupInfoList);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.lastScreen);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.backupInfoList.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
      super.render(guigraphics, i, j, f);
   }

   Component checkForSpecificMetadata(String s, String s1) {
      String s2 = s.toLowerCase(Locale.ROOT);
      if (s2.contains("game") && s2.contains("mode")) {
         return this.gameModeMetadata(s1);
      } else {
         return (Component)(s2.contains("game") && s2.contains("difficulty") ? this.gameDifficultyMetadata(s1) : Component.literal(s1));
      }
   }

   private Component gameDifficultyMetadata(String s) {
      try {
         return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(s)).getDisplayName();
      } catch (Exception var3) {
         return UNKNOWN;
      }
   }

   private Component gameModeMetadata(String s) {
      try {
         return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(s)).getShortDisplayName();
      } catch (Exception var3) {
         return UNKNOWN;
      }
   }

   class BackupInfoList extends ObjectSelectionList<RealmsBackupInfoScreen.BackupInfoListEntry> {
      public BackupInfoList(Minecraft minecraft) {
         super(minecraft, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
         this.setRenderSelection(false);
         if (RealmsBackupInfoScreen.this.backup.changeList != null) {
            RealmsBackupInfoScreen.this.backup.changeList.forEach((s, s1) -> this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(s, s1)));
         }

      }
   }

   class BackupInfoListEntry extends ObjectSelectionList.Entry<RealmsBackupInfoScreen.BackupInfoListEntry> {
      private static final Component TEMPLATE_NAME = Component.translatable("mco.backup.entry.templateName");
      private static final Component GAME_DIFFICULTY = Component.translatable("mco.backup.entry.gameDifficulty");
      private static final Component NAME = Component.translatable("mco.backup.entry.name");
      private static final Component GAME_SERVER_VERSION = Component.translatable("mco.backup.entry.gameServerVersion");
      private static final Component UPLOADED = Component.translatable("mco.backup.entry.uploaded");
      private static final Component ENABLED_PACK = Component.translatable("mco.backup.entry.enabledPack");
      private static final Component DESCRIPTION = Component.translatable("mco.backup.entry.description");
      private static final Component GAME_MODE = Component.translatable("mco.backup.entry.gameMode");
      private static final Component SEED = Component.translatable("mco.backup.entry.seed");
      private static final Component WORLD_TYPE = Component.translatable("mco.backup.entry.worldType");
      private static final Component UNDEFINED = Component.translatable("mco.backup.entry.undefined");
      private final String key;
      private final String value;

      public BackupInfoListEntry(String s, String s1) {
         this.key = s;
         this.value = s1;
      }

      public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
         guigraphics.drawString(RealmsBackupInfoScreen.this.font, this.translateKey(this.key), k, j, 10526880);
         guigraphics.drawString(RealmsBackupInfoScreen.this.font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), k, j + 12, 16777215);
      }

      private Component translateKey(String s) {
         Component var10000;
         switch (s) {
            case "template_name":
               var10000 = TEMPLATE_NAME;
               break;
            case "game_difficulty":
               var10000 = GAME_DIFFICULTY;
               break;
            case "name":
               var10000 = NAME;
               break;
            case "game_server_version":
               var10000 = GAME_SERVER_VERSION;
               break;
            case "uploaded":
               var10000 = UPLOADED;
               break;
            case "enabled_pack":
               var10000 = ENABLED_PACK;
               break;
            case "description":
               var10000 = DESCRIPTION;
               break;
            case "game_mode":
               var10000 = GAME_MODE;
               break;
            case "seed":
               var10000 = SEED;
               break;
            case "world_type":
               var10000 = WORLD_TYPE;
               break;
            default:
               var10000 = UNDEFINED;
         }

         return var10000;
      }

      public Component getNarration() {
         return Component.translatable("narrator.select", this.key + " " + this.value);
      }
   }
}
