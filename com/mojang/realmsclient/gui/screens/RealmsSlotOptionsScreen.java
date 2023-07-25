package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

public class RealmsSlotOptionsScreen extends RealmsScreen {
   private static final int DEFAULT_DIFFICULTY = 2;
   public static final List<Difficulty> DIFFICULTIES = ImmutableList.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
   private static final int DEFAULT_GAME_MODE = 0;
   public static final List<GameType> GAME_MODES = ImmutableList.of(GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE);
   private static final Component NAME_LABEL = Component.translatable("mco.configure.world.edit.slot.name");
   static final Component SPAWN_PROTECTION_TEXT = Component.translatable("mco.configure.world.spawnProtection");
   private static final Component SPAWN_WARNING_TITLE = Component.translatable("mco.configure.world.spawn_toggle.title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
   private EditBox nameEdit;
   protected final RealmsConfigureWorldScreen parent;
   private int column1X;
   private int columnWidth;
   private final RealmsWorldOptions options;
   private final RealmsServer.WorldType worldType;
   private Difficulty difficulty;
   private GameType gameMode;
   private final String defaultSlotName;
   private String worldName;
   private boolean pvp;
   private boolean spawnNPCs;
   private boolean spawnAnimals;
   private boolean spawnMonsters;
   int spawnProtection;
   private boolean commandBlocks;
   private boolean forceGameMode;
   RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;

   public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen realmsconfigureworldscreen, RealmsWorldOptions realmsworldoptions, RealmsServer.WorldType realmsserver_worldtype, int i) {
      super(Component.translatable("mco.configure.world.buttons.options"));
      this.parent = realmsconfigureworldscreen;
      this.options = realmsworldoptions;
      this.worldType = realmsserver_worldtype;
      this.difficulty = findByIndex(DIFFICULTIES, realmsworldoptions.difficulty, 2);
      this.gameMode = findByIndex(GAME_MODES, realmsworldoptions.gameMode, 0);
      this.defaultSlotName = realmsworldoptions.getDefaultSlotName(i);
      this.setWorldName(realmsworldoptions.getSlotName(i));
      if (realmsserver_worldtype == RealmsServer.WorldType.NORMAL) {
         this.pvp = realmsworldoptions.pvp;
         this.spawnProtection = realmsworldoptions.spawnProtection;
         this.forceGameMode = realmsworldoptions.forceGameMode;
         this.spawnAnimals = realmsworldoptions.spawnAnimals;
         this.spawnMonsters = realmsworldoptions.spawnMonsters;
         this.spawnNPCs = realmsworldoptions.spawnNPCs;
         this.commandBlocks = realmsworldoptions.commandBlocks;
      } else {
         this.pvp = true;
         this.spawnProtection = 0;
         this.forceGameMode = false;
         this.spawnAnimals = true;
         this.spawnMonsters = true;
         this.spawnNPCs = true;
         this.commandBlocks = true;
      }

   }

   public void tick() {
      this.nameEdit.tick();
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.setScreen(this.parent);
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   private static <T> T findByIndex(List<T> list, int i, int j) {
      try {
         return list.get(i);
      } catch (IndexOutOfBoundsException var4) {
         return list.get(j);
      }
   }

   private static <T> int findIndex(List<T> list, T object, int i) {
      int j = list.indexOf(object);
      return j == -1 ? i : j;
   }

   public void init() {
      this.columnWidth = 170;
      this.column1X = this.width / 2 - this.columnWidth;
      int i = this.width / 2 + 10;
      if (this.worldType != RealmsServer.WorldType.NORMAL) {
         Component component;
         if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
            component = Component.translatable("mco.configure.world.edit.subscreen.adventuremap");
         } else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
            component = Component.translatable("mco.configure.world.edit.subscreen.inspiration");
         } else {
            component = Component.translatable("mco.configure.world.edit.subscreen.experience");
         }

         this.addLabel(new RealmsLabel(component, this.width / 2, 26, 16711680));
      }

      this.nameEdit = new EditBox(this.minecraft.font, this.column1X + 2, row(1), this.columnWidth - 4, 20, (EditBox)null, Component.translatable("mco.configure.world.edit.slot.name"));
      this.nameEdit.setMaxLength(10);
      this.nameEdit.setValue(this.worldName);
      this.nameEdit.setResponder(this::setWorldName);
      this.magicalSpecialHackyFocus(this.nameEdit);
      CycleButton<Boolean> cyclebutton = this.addRenderableWidget(CycleButton.onOffBuilder(this.pvp).create(i, row(1), this.columnWidth, 20, Component.translatable("mco.configure.world.pvp"), (cyclebutton11, obool5) -> this.pvp = obool5));
      this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues(GAME_MODES).withInitialValue(this.gameMode).create(this.column1X, row(3), this.columnWidth, 20, Component.translatable("selectWorld.gameMode"), (cyclebutton10, gametype) -> this.gameMode = gametype));
      Component component3 = Component.translatable("mco.configure.world.spawn_toggle.message");
      CycleButton<Boolean> cyclebutton1 = this.addRenderableWidget(CycleButton.onOffBuilder(this.spawnAnimals).create(i, row(3), this.columnWidth, 20, Component.translatable("mco.configure.world.spawnAnimals"), this.confirmDangerousOption(component3, (obool4) -> this.spawnAnimals = obool4)));
      CycleButton<Boolean> cyclebutton2 = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters).create(i, row(5), this.columnWidth, 20, Component.translatable("mco.configure.world.spawnMonsters"), this.confirmDangerousOption(component3, (obool3) -> this.spawnMonsters = obool3));
      this.addRenderableWidget(CycleButton.builder(Difficulty::getDisplayName).withValues(DIFFICULTIES).withInitialValue(this.difficulty).create(this.column1X, row(5), this.columnWidth, 20, Component.translatable("options.difficulty"), (cyclebutton9, difficulty) -> {
         this.difficulty = difficulty;
         if (this.worldType == RealmsServer.WorldType.NORMAL) {
            boolean flag = this.difficulty != Difficulty.PEACEFUL;
            cyclebutton2.active = flag;
            cyclebutton2.setValue(flag && this.spawnMonsters);
         }

      }));
      this.addRenderableWidget(cyclebutton2);
      this.spawnProtectionButton = this.addRenderableWidget(new RealmsSlotOptionsScreen.SettingsSlider(this.column1X, row(7), this.columnWidth, this.spawnProtection, 0.0F, 16.0F));
      CycleButton<Boolean> cyclebutton3 = this.addRenderableWidget(CycleButton.onOffBuilder(this.spawnNPCs).create(i, row(7), this.columnWidth, 20, Component.translatable("mco.configure.world.spawnNPCs"), this.confirmDangerousOption(Component.translatable("mco.configure.world.spawn_toggle.message.npc"), (obool2) -> this.spawnNPCs = obool2)));
      CycleButton<Boolean> cyclebutton4 = this.addRenderableWidget(CycleButton.onOffBuilder(this.forceGameMode).create(this.column1X, row(9), this.columnWidth, 20, Component.translatable("mco.configure.world.forceGameMode"), (cyclebutton7, obool1) -> this.forceGameMode = obool1));
      CycleButton<Boolean> cyclebutton5 = this.addRenderableWidget(CycleButton.onOffBuilder(this.commandBlocks).create(i, row(9), this.columnWidth, 20, Component.translatable("mco.configure.world.commandBlocks"), (cyclebutton6, obool) -> this.commandBlocks = obool));
      if (this.worldType != RealmsServer.WorldType.NORMAL) {
         cyclebutton.active = false;
         cyclebutton1.active = false;
         cyclebutton3.active = false;
         cyclebutton2.active = false;
         this.spawnProtectionButton.active = false;
         cyclebutton5.active = false;
         cyclebutton4.active = false;
      }

      if (this.difficulty == Difficulty.PEACEFUL) {
         cyclebutton2.active = false;
      }

      this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.done"), (button1) -> this.saveSettings()).bounds(this.column1X, row(13), this.columnWidth, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.parent)).bounds(i, row(13), this.columnWidth, 20).build());
      this.addWidget(this.nameEdit);
   }

   private CycleButton.OnValueChange<Boolean> confirmDangerousOption(Component component, Consumer<Boolean> consumer) {
      return (cyclebutton, obool) -> {
         if (obool) {
            consumer.accept(true);
         } else {
            this.minecraft.setScreen(new ConfirmScreen((flag) -> {
               if (flag) {
                  consumer.accept(false);
               }

               this.minecraft.setScreen(this);
            }, SPAWN_WARNING_TITLE, component, CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
         }

      };
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 17, 16777215);
      guigraphics.drawString(this.font, NAME_LABEL, this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2, row(0) - 5, 16777215, false);
      this.nameEdit.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
   }

   private void setWorldName(String s) {
      if (s.equals(this.defaultSlotName)) {
         this.worldName = "";
      } else {
         this.worldName = s;
      }

   }

   private void saveSettings() {
      int i = findIndex(DIFFICULTIES, this.difficulty, 2);
      int j = findIndex(GAME_MODES, this.gameMode, 0);
      if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP && this.worldType != RealmsServer.WorldType.EXPERIENCE && this.worldType != RealmsServer.WorldType.INSPIRATION) {
         boolean flag = this.worldType == RealmsServer.WorldType.NORMAL && this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters;
         this.parent.saveSlotSettings(new RealmsWorldOptions(this.pvp, this.spawnAnimals, flag, this.spawnNPCs, this.spawnProtection, this.commandBlocks, i, j, this.forceGameMode, this.worldName));
      } else {
         this.parent.saveSlotSettings(new RealmsWorldOptions(this.options.pvp, this.options.spawnAnimals, this.options.spawnMonsters, this.options.spawnNPCs, this.options.spawnProtection, this.options.commandBlocks, i, j, this.options.forceGameMode, this.worldName));
      }

   }

   class SettingsSlider extends AbstractSliderButton {
      private final double minValue;
      private final double maxValue;

      public SettingsSlider(int i, int j, int k, int l, float f, float f1) {
         super(i, j, k, 20, CommonComponents.EMPTY, 0.0D);
         this.minValue = (double)f;
         this.maxValue = (double)f1;
         this.value = (double)((Mth.clamp((float)l, f, f1) - f) / (f1 - f));
         this.updateMessage();
      }

      public void applyValue() {
         if (RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
            RealmsSlotOptionsScreen.this.spawnProtection = (int)Mth.lerp(Mth.clamp(this.value, 0.0D, 1.0D), this.minValue, this.maxValue);
         }
      }

      protected void updateMessage() {
         this.setMessage(CommonComponents.optionNameValue(RealmsSlotOptionsScreen.SPAWN_PROTECTION_TEXT, (Component)(RealmsSlotOptionsScreen.this.spawnProtection == 0 ? CommonComponents.OPTION_OFF : Component.literal(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))));
      }

      public void onClick(double d0, double d1) {
      }

      public void onRelease(double d0, double d1) {
      }
   }
}
