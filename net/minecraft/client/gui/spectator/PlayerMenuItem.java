package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.resources.ResourceLocation;

public class PlayerMenuItem implements SpectatorMenuItem {
   private final GameProfile profile;
   private final ResourceLocation location;
   private final Component name;

   public PlayerMenuItem(GameProfile gameprofile) {
      this.profile = gameprofile;
      Minecraft minecraft = Minecraft.getInstance();
      this.location = minecraft.getSkinManager().getInsecureSkinLocation(gameprofile);
      this.name = Component.literal(gameprofile.getName());
   }

   public void selectItem(SpectatorMenu spectatormenu) {
      Minecraft.getInstance().getConnection().send(new ServerboundTeleportToEntityPacket(this.profile.getId()));
   }

   public Component getName() {
      return this.name;
   }

   public void renderIcon(GuiGraphics guigraphics, float f, int i) {
      guigraphics.setColor(1.0F, 1.0F, 1.0F, (float)i / 255.0F);
      PlayerFaceRenderer.draw(guigraphics, this.location, 2, 2, 12);
      guigraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public boolean isEnabled() {
      return true;
   }
}
