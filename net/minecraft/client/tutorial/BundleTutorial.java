package net.minecraft.client.tutorial;

import javax.annotation.Nullable;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BundleTutorial {
   private final Tutorial tutorial;
   private final Options options;
   @Nullable
   private TutorialToast toast;

   public BundleTutorial(Tutorial tutorial, Options options) {
      this.tutorial = tutorial;
      this.options = options;
   }

   private void showToast() {
      if (this.toast != null) {
         this.tutorial.removeTimedToast(this.toast);
      }

      Component component = Component.translatable("tutorial.bundleInsert.title");
      Component component1 = Component.translatable("tutorial.bundleInsert.description");
      this.toast = new TutorialToast(TutorialToast.Icons.RIGHT_CLICK, component, component1, true);
      this.tutorial.addTimedToast(this.toast, 160);
   }

   private void clearToast() {
      if (this.toast != null) {
         this.tutorial.removeTimedToast(this.toast);
         this.toast = null;
      }

      if (!this.options.hideBundleTutorial) {
         this.options.hideBundleTutorial = true;
         this.options.save();
      }

   }

   public void onInventoryAction(ItemStack itemstack, ItemStack itemstack1, ClickAction clickaction) {
      if (!this.options.hideBundleTutorial) {
         if (!itemstack.isEmpty() && itemstack1.is(Items.BUNDLE)) {
            if (clickaction == ClickAction.PRIMARY) {
               this.showToast();
            } else if (clickaction == ClickAction.SECONDARY) {
               this.clearToast();
            }
         } else if (itemstack.is(Items.BUNDLE) && !itemstack1.isEmpty() && clickaction == ClickAction.SECONDARY) {
            this.clearToast();
         }

      }
   }
}
