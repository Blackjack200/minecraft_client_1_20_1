package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;

public class LecternScreen extends BookViewScreen implements MenuAccess<LecternMenu> {
   private final LecternMenu menu;
   private final ContainerListener listener = new ContainerListener() {
      public void slotChanged(AbstractContainerMenu abstractcontainermenu, int i, ItemStack itemstack) {
         LecternScreen.this.bookChanged();
      }

      public void dataChanged(AbstractContainerMenu abstractcontainermenu, int i, int j) {
         if (i == 0) {
            LecternScreen.this.pageChanged();
         }

      }
   };

   public LecternScreen(LecternMenu lecternmenu, Inventory inventory, Component component) {
      this.menu = lecternmenu;
   }

   public LecternMenu getMenu() {
      return this.menu;
   }

   protected void init() {
      super.init();
      this.menu.addSlotListener(this.listener);
   }

   public void onClose() {
      this.minecraft.player.closeContainer();
      super.onClose();
   }

   public void removed() {
      super.removed();
      this.menu.removeSlotListener(this.listener);
   }

   protected void createMenuControls() {
      if (this.minecraft.player.mayBuild()) {
         this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button1) -> this.onClose()).bounds(this.width / 2 - 100, 196, 98, 20).build());
         this.addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"), (button) -> this.sendButtonClick(3)).bounds(this.width / 2 + 2, 196, 98, 20).build());
      } else {
         super.createMenuControls();
      }

   }

   protected void pageBack() {
      this.sendButtonClick(1);
   }

   protected void pageForward() {
      this.sendButtonClick(2);
   }

   protected boolean forcePage(int i) {
      if (i != this.menu.getPage()) {
         this.sendButtonClick(100 + i);
         return true;
      } else {
         return false;
      }
   }

   private void sendButtonClick(int i) {
      this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, i);
   }

   public boolean isPauseScreen() {
      return false;
   }

   void bookChanged() {
      ItemStack itemstack = this.menu.getBook();
      this.setBookAccess(BookViewScreen.BookAccess.fromItem(itemstack));
   }

   void pageChanged() {
      this.setPage(this.menu.getPage());
   }

   protected void closeScreen() {
      this.minecraft.player.closeContainer();
   }
}
