package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class GameModeSwitcherScreen extends Screen {
   static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = new ResourceLocation("textures/gui/container/gamemode_switcher.png");
   private static final int SPRITE_SHEET_WIDTH = 128;
   private static final int SPRITE_SHEET_HEIGHT = 128;
   private static final int SLOT_AREA = 26;
   private static final int SLOT_PADDING = 5;
   private static final int SLOT_AREA_PADDED = 31;
   private static final int HELP_TIPS_OFFSET_Y = 5;
   private static final int ALL_SLOTS_WIDTH = GameModeSwitcherScreen.GameModeIcon.values().length * 31 - 5;
   private static final Component SELECT_KEY = Component.translatable("debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA));
   private final GameModeSwitcherScreen.GameModeIcon previousHovered;
   private GameModeSwitcherScreen.GameModeIcon currentlyHovered;
   private int firstMouseX;
   private int firstMouseY;
   private boolean setFirstMousePos;
   private final List<GameModeSwitcherScreen.GameModeSlot> slots = Lists.newArrayList();

   public GameModeSwitcherScreen() {
      super(GameNarrator.NO_TITLE);
      this.previousHovered = GameModeSwitcherScreen.GameModeIcon.getFromGameType(this.getDefaultSelected());
      this.currentlyHovered = this.previousHovered;
   }

   private GameType getDefaultSelected() {
      MultiPlayerGameMode multiplayergamemode = Minecraft.getInstance().gameMode;
      GameType gametype = multiplayergamemode.getPreviousPlayerMode();
      if (gametype != null) {
         return gametype;
      } else {
         return multiplayergamemode.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
      }
   }

   protected void init() {
      super.init();
      this.currentlyHovered = this.previousHovered;

      for(int i = 0; i < GameModeSwitcherScreen.GameModeIcon.VALUES.length; ++i) {
         GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen_gamemodeicon = GameModeSwitcherScreen.GameModeIcon.VALUES[i];
         this.slots.add(new GameModeSwitcherScreen.GameModeSlot(gamemodeswitcherscreen_gamemodeicon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 31, this.height / 2 - 31));
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (!this.checkToClose()) {
         guigraphics.pose().pushPose();
         RenderSystem.enableBlend();
         int k = this.width / 2 - 62;
         int l = this.height / 2 - 31 - 27;
         guigraphics.blit(GAMEMODE_SWITCHER_LOCATION, k, l, 0.0F, 0.0F, 125, 75, 128, 128);
         guigraphics.pose().popPose();
         super.render(guigraphics, i, j, f);
         guigraphics.drawCenteredString(this.font, this.currentlyHovered.getName(), this.width / 2, this.height / 2 - 31 - 20, -1);
         guigraphics.drawCenteredString(this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, 16777215);
         if (!this.setFirstMousePos) {
            this.firstMouseX = i;
            this.firstMouseY = j;
            this.setFirstMousePos = true;
         }

         boolean flag = this.firstMouseX == i && this.firstMouseY == j;

         for(GameModeSwitcherScreen.GameModeSlot gamemodeswitcherscreen_gamemodeslot : this.slots) {
            gamemodeswitcherscreen_gamemodeslot.render(guigraphics, i, j, f);
            gamemodeswitcherscreen_gamemodeslot.setSelected(this.currentlyHovered == gamemodeswitcherscreen_gamemodeslot.icon);
            if (!flag && gamemodeswitcherscreen_gamemodeslot.isHoveredOrFocused()) {
               this.currentlyHovered = gamemodeswitcherscreen_gamemodeslot.icon;
            }
         }

      }
   }

   private void switchToHoveredGameMode() {
      switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
   }

   private static void switchToHoveredGameMode(Minecraft minecraft, GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen_gamemodeicon) {
      if (minecraft.gameMode != null && minecraft.player != null) {
         GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen_gamemodeicon1 = GameModeSwitcherScreen.GameModeIcon.getFromGameType(minecraft.gameMode.getPlayerMode());
         if (minecraft.player.hasPermissions(2) && gamemodeswitcherscreen_gamemodeicon != gamemodeswitcherscreen_gamemodeicon1) {
            minecraft.player.connection.sendUnsignedCommand(gamemodeswitcherscreen_gamemodeicon.getCommand());
         }

      }
   }

   private boolean checkToClose() {
      if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
         this.switchToHoveredGameMode();
         this.minecraft.setScreen((Screen)null);
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 293) {
         this.setFirstMousePos = false;
         this.currentlyHovered = this.currentlyHovered.getNext();
         return true;
      } else {
         return super.keyPressed(i, j, k);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   static enum GameModeIcon {
      CREATIVE(Component.translatable("gameMode.creative"), "gamemode creative", new ItemStack(Blocks.GRASS_BLOCK)),
      SURVIVAL(Component.translatable("gameMode.survival"), "gamemode survival", new ItemStack(Items.IRON_SWORD)),
      ADVENTURE(Component.translatable("gameMode.adventure"), "gamemode adventure", new ItemStack(Items.MAP)),
      SPECTATOR(Component.translatable("gameMode.spectator"), "gamemode spectator", new ItemStack(Items.ENDER_EYE));

      protected static final GameModeSwitcherScreen.GameModeIcon[] VALUES = values();
      private static final int ICON_AREA = 16;
      protected static final int ICON_TOP_LEFT = 5;
      final Component name;
      final String command;
      final ItemStack renderStack;

      private GameModeIcon(Component component, String s, ItemStack itemstack) {
         this.name = component;
         this.command = s;
         this.renderStack = itemstack;
      }

      void drawIcon(GuiGraphics guigraphics, int i, int j) {
         guigraphics.renderItem(this.renderStack, i, j);
      }

      Component getName() {
         return this.name;
      }

      String getCommand() {
         return this.command;
      }

      GameModeSwitcherScreen.GameModeIcon getNext() {
         GameModeSwitcherScreen.GameModeIcon var10000;
         switch (this) {
            case CREATIVE:
               var10000 = SURVIVAL;
               break;
            case SURVIVAL:
               var10000 = ADVENTURE;
               break;
            case ADVENTURE:
               var10000 = SPECTATOR;
               break;
            case SPECTATOR:
               var10000 = CREATIVE;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      static GameModeSwitcherScreen.GameModeIcon getFromGameType(GameType gametype) {
         GameModeSwitcherScreen.GameModeIcon var10000;
         switch (gametype) {
            case SPECTATOR:
               var10000 = SPECTATOR;
               break;
            case SURVIVAL:
               var10000 = SURVIVAL;
               break;
            case CREATIVE:
               var10000 = CREATIVE;
               break;
            case ADVENTURE:
               var10000 = ADVENTURE;
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }
   }

   public class GameModeSlot extends AbstractWidget {
      final GameModeSwitcherScreen.GameModeIcon icon;
      private boolean isSelected;

      public GameModeSlot(GameModeSwitcherScreen.GameModeIcon gamemodeswitcherscreen_gamemodeicon, int i, int j) {
         super(i, j, 26, 26, gamemodeswitcherscreen_gamemodeicon.getName());
         this.icon = gamemodeswitcherscreen_gamemodeicon;
      }

      public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
         this.drawSlot(guigraphics);
         this.icon.drawIcon(guigraphics, this.getX() + 5, this.getY() + 5);
         if (this.isSelected) {
            this.drawSelection(guigraphics);
         }

      }

      public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
         this.defaultButtonNarrationText(narrationelementoutput);
      }

      public boolean isHoveredOrFocused() {
         return super.isHoveredOrFocused() || this.isSelected;
      }

      public void setSelected(boolean flag) {
         this.isSelected = flag;
      }

      private void drawSlot(GuiGraphics guigraphics) {
         guigraphics.blit(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION, this.getX(), this.getY(), 0.0F, 75.0F, 26, 26, 128, 128);
      }

      private void drawSelection(GuiGraphics guigraphics) {
         guigraphics.blit(GameModeSwitcherScreen.GAMEMODE_SWITCHER_LOCATION, this.getX(), this.getY(), 26.0F, 75.0F, 26, 26, 128, 128);
      }
   }
}
