package net.minecraft.client.gui.spectator.categories;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeleportToTeamMenuCategory implements SpectatorMenuCategory, SpectatorMenuItem {
   private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.team_teleport");
   private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.team_teleport.prompt");
   private final List<SpectatorMenuItem> items;

   public TeleportToTeamMenuCategory() {
      Minecraft minecraft = Minecraft.getInstance();
      this.items = createTeamEntries(minecraft, minecraft.level.getScoreboard());
   }

   private static List<SpectatorMenuItem> createTeamEntries(Minecraft minecraft, Scoreboard scoreboard) {
      return scoreboard.getPlayerTeams().stream().flatMap((playerteam) -> TeleportToTeamMenuCategory.TeamSelectionItem.create(minecraft, playerteam).stream()).toList();
   }

   public List<SpectatorMenuItem> getItems() {
      return this.items;
   }

   public Component getPrompt() {
      return TELEPORT_PROMPT;
   }

   public void selectItem(SpectatorMenu spectatormenu) {
      spectatormenu.selectCategory(this);
   }

   public Component getName() {
      return TELEPORT_TEXT;
   }

   public void renderIcon(GuiGraphics guigraphics, float f, int i) {
      guigraphics.blit(SpectatorGui.SPECTATOR_LOCATION, 0, 0, 16.0F, 0.0F, 16, 16, 256, 256);
   }

   public boolean isEnabled() {
      return !this.items.isEmpty();
   }

   static class TeamSelectionItem implements SpectatorMenuItem {
      private final PlayerTeam team;
      private final ResourceLocation iconSkin;
      private final List<PlayerInfo> players;

      private TeamSelectionItem(PlayerTeam playerteam, List<PlayerInfo> list, ResourceLocation resourcelocation) {
         this.team = playerteam;
         this.players = list;
         this.iconSkin = resourcelocation;
      }

      public static Optional<SpectatorMenuItem> create(Minecraft minecraft, PlayerTeam playerteam) {
         List<PlayerInfo> list = new ArrayList<>();

         for(String s : playerteam.getPlayers()) {
            PlayerInfo playerinfo = minecraft.getConnection().getPlayerInfo(s);
            if (playerinfo != null && playerinfo.getGameMode() != GameType.SPECTATOR) {
               list.add(playerinfo);
            }
         }

         if (list.isEmpty()) {
            return Optional.empty();
         } else {
            GameProfile gameprofile = list.get(RandomSource.create().nextInt(list.size())).getProfile();
            ResourceLocation resourcelocation = minecraft.getSkinManager().getInsecureSkinLocation(gameprofile);
            return Optional.of(new TeleportToTeamMenuCategory.TeamSelectionItem(playerteam, list, resourcelocation));
         }
      }

      public void selectItem(SpectatorMenu spectatormenu) {
         spectatormenu.selectCategory(new TeleportToPlayerMenuCategory(this.players));
      }

      public Component getName() {
         return this.team.getDisplayName();
      }

      public void renderIcon(GuiGraphics guigraphics, float f, int i) {
         Integer integer = this.team.getColor().getColor();
         if (integer != null) {
            float f1 = (float)(integer >> 16 & 255) / 255.0F;
            float f2 = (float)(integer >> 8 & 255) / 255.0F;
            float f3 = (float)(integer & 255) / 255.0F;
            guigraphics.fill(1, 1, 15, 15, Mth.color(f1 * f, f2 * f, f3 * f) | i << 24);
         }

         guigraphics.setColor(f, f, f, (float)i / 255.0F);
         PlayerFaceRenderer.draw(guigraphics, this.iconSkin, 2, 2, 12);
         guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      public boolean isEnabled() {
         return true;
      }
   }
}
