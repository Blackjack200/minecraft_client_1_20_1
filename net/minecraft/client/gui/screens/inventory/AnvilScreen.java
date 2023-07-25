package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnvilScreen extends ItemCombinerScreen<AnvilMenu> {
   private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("textures/gui/container/anvil.png");
   private static final Component TOO_EXPENSIVE_TEXT = Component.translatable("container.repair.expensive");
   private EditBox name;
   private final Player player;

   public AnvilScreen(AnvilMenu anvilmenu, Inventory inventory, Component component) {
      super(anvilmenu, inventory, component, ANVIL_LOCATION);
      this.player = inventory.player;
      this.titleLabelX = 60;
   }

   public void containerTick() {
      super.containerTick();
      this.name.tick();
   }

   protected void subInit() {
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      this.name = new EditBox(this.font, i + 62, j + 24, 103, 12, Component.translatable("container.repair"));
      this.name.setCanLoseFocus(false);
      this.name.setTextColor(-1);
      this.name.setTextColorUneditable(-1);
      this.name.setBordered(false);
      this.name.setMaxLength(50);
      this.name.setResponder(this::onNameChanged);
      this.name.setValue("");
      this.addWidget(this.name);
      this.setInitialFocus(this.name);
      this.name.setEditable(false);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.name.getValue();
      this.init(minecraft, i, j);
      this.name.setValue(s);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 256) {
         this.minecraft.player.closeContainer();
      }

      return !this.name.keyPressed(i, j, k) && !this.name.canConsumeInput() ? super.keyPressed(i, j, k) : true;
   }

   private void onNameChanged(String s) {
      Slot slot = this.menu.getSlot(0);
      if (slot.hasItem()) {
         String s1 = s;
         if (!slot.getItem().hasCustomHoverName() && s.equals(slot.getItem().getHoverName().getString())) {
            s1 = "";
         }

         if (this.menu.setItemName(s1)) {
            this.minecraft.player.connection.send(new ServerboundRenameItemPacket(s1));
         }

      }
   }

   protected void renderLabels(GuiGraphics guigraphics, int i, int j) {
      super.renderLabels(guigraphics, i, j);
      int k = this.menu.getCost();
      if (k > 0) {
         int l = 8453920;
         Component component;
         if (k >= 40 && !this.minecraft.player.getAbilities().instabuild) {
            component = TOO_EXPENSIVE_TEXT;
            l = 16736352;
         } else if (!this.menu.getSlot(2).hasItem()) {
            component = null;
         } else {
            component = Component.translatable("container.repair.cost", k);
            if (!this.menu.getSlot(2).mayPickup(this.player)) {
               l = 16736352;
            }
         }

         if (component != null) {
            int i1 = this.imageWidth - 8 - this.font.width(component) - 2;
            int j1 = 69;
            guigraphics.fill(i1 - 2, 67, this.imageWidth - 8, 79, 1325400064);
            guigraphics.drawString(this.font, component, i1, 69, l);
         }
      }

   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      super.renderBg(guigraphics, f, i, j);
      guigraphics.blit(ANVIL_LOCATION, this.leftPos + 59, this.topPos + 20, 0, this.imageHeight + (this.menu.getSlot(0).hasItem() ? 0 : 16), 110, 16);
   }

   public void renderFg(GuiGraphics guigraphics, int i, int j, float f) {
      this.name.render(guigraphics, i, j, f);
   }

   protected void renderErrorIcon(GuiGraphics guigraphics, int i, int j) {
      if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
         guigraphics.blit(ANVIL_LOCATION, i + 99, j + 45, this.imageWidth, 0, 28, 21);
      }

   }

   public void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
      if (i == 0) {
         this.name.setValue(itemstack.isEmpty() ? "" : itemstack.getHoverName().getString());
         this.name.setEditable(!itemstack.isEmpty());
         this.setFocused(this.name);
      }

   }
}
