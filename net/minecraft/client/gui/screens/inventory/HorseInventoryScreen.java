package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;

public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
   private static final ResourceLocation HORSE_INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/horse.png");
   private final AbstractHorse horse;
   private float xMouse;
   private float yMouse;

   public HorseInventoryScreen(HorseInventoryMenu horseinventorymenu, Inventory inventory, AbstractHorse abstracthorse) {
      super(horseinventorymenu, inventory, abstracthorse.getDisplayName());
      this.horse = abstracthorse;
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(HORSE_INVENTORY_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      if (this.horse instanceof AbstractChestedHorse) {
         AbstractChestedHorse abstractchestedhorse = (AbstractChestedHorse)this.horse;
         if (abstractchestedhorse.hasChest()) {
            guigraphics.blit(HORSE_INVENTORY_LOCATION, k + 79, l + 17, 0, this.imageHeight, abstractchestedhorse.getInventoryColumns() * 18, 54);
         }
      }

      if (this.horse.isSaddleable()) {
         guigraphics.blit(HORSE_INVENTORY_LOCATION, k + 7, l + 35 - 18, 18, this.imageHeight + 54, 18, 18);
      }

      if (this.horse.canWearArmor()) {
         if (this.horse instanceof Llama) {
            guigraphics.blit(HORSE_INVENTORY_LOCATION, k + 7, l + 35, 36, this.imageHeight + 54, 18, 18);
         } else {
            guigraphics.blit(HORSE_INVENTORY_LOCATION, k + 7, l + 35, 0, this.imageHeight + 54, 18, 18);
         }
      }

      InventoryScreen.renderEntityInInventoryFollowsMouse(guigraphics, k + 51, l + 60, 17, (float)(k + 51) - this.xMouse, (float)(l + 75 - 50) - this.yMouse, this.horse);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.xMouse = (float)i;
      this.yMouse = (float)j;
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }
}
