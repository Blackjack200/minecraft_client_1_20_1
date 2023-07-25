package net.minecraft.client.gui.spectator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public interface SpectatorMenuItem {
   void selectItem(SpectatorMenu spectatormenu);

   Component getName();

   void renderIcon(GuiGraphics guigraphics, float f, int i);

   boolean isEnabled();
}
