package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;

public abstract class ItemCombinerScreen<T extends ItemCombinerMenu> extends AbstractContainerScreen<T> implements ContainerListener {
   private final ResourceLocation menuResource;

   public ItemCombinerScreen(T itemcombinermenu, Inventory inventory, Component component, ResourceLocation resourcelocation) {
      super(itemcombinermenu, inventory, component);
      this.menuResource = resourcelocation;
   }

   protected void subInit() {
   }

   protected void init() {
      super.init();
      this.subInit();
      this.menu.addSlotListener(this);
   }

   public void removed() {
      super.removed();
      this.menu.removeSlotListener(this);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      this.renderFg(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   protected void renderFg(GuiGraphics guigraphics, int i, int j, float f) {
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      guigraphics.blit(this.menuResource, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      this.renderErrorIcon(guigraphics, this.leftPos, this.topPos);
   }

   protected abstract void renderErrorIcon(GuiGraphics guigraphics, int i, int j);

   public void dataChanged(AbstractContainerMenu abstractcontainermenu, int i, int j) {
   }

   public void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
   }
}
