package net.minecraft.client.gui.screens.inventory;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class CartographyTableScreen extends AbstractContainerScreen<CartographyTableMenu> {
   private static final ResourceLocation BG_LOCATION = new ResourceLocation("textures/gui/container/cartography_table.png");

   public CartographyTableScreen(CartographyTableMenu cartographytablemenu, Inventory inventory, Component component) {
      super(cartographytablemenu, inventory, component);
      this.titleLabelY -= 2;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      super.render(guigraphics, i, j, f);
      this.renderTooltip(guigraphics, i, j);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      this.renderBackground(guigraphics);
      int k = this.leftPos;
      int l = this.topPos;
      guigraphics.blit(BG_LOCATION, k, l, 0, 0, this.imageWidth, this.imageHeight);
      ItemStack itemstack = this.menu.getSlot(1).getItem();
      boolean flag = itemstack.is(Items.MAP);
      boolean flag1 = itemstack.is(Items.PAPER);
      boolean flag2 = itemstack.is(Items.GLASS_PANE);
      ItemStack itemstack1 = this.menu.getSlot(0).getItem();
      boolean flag3 = false;
      Integer integer;
      MapItemSavedData mapitemsaveddata;
      if (itemstack1.is(Items.FILLED_MAP)) {
         integer = MapItem.getMapId(itemstack1);
         mapitemsaveddata = MapItem.getSavedData(integer, this.minecraft.level);
         if (mapitemsaveddata != null) {
            if (mapitemsaveddata.locked) {
               flag3 = true;
               if (flag1 || flag2) {
                  guigraphics.blit(BG_LOCATION, k + 35, l + 31, this.imageWidth + 50, 132, 28, 21);
               }
            }

            if (flag1 && mapitemsaveddata.scale >= 4) {
               flag3 = true;
               guigraphics.blit(BG_LOCATION, k + 35, l + 31, this.imageWidth + 50, 132, 28, 21);
            }
         }
      } else {
         integer = null;
         mapitemsaveddata = null;
      }

      this.renderResultingMap(guigraphics, integer, mapitemsaveddata, flag, flag1, flag2, flag3);
   }

   private void renderResultingMap(GuiGraphics guigraphics, @Nullable Integer integer, @Nullable MapItemSavedData mapitemsaveddata, boolean flag, boolean flag1, boolean flag2, boolean flag3) {
      int i = this.leftPos;
      int j = this.topPos;
      if (flag1 && !flag3) {
         guigraphics.blit(BG_LOCATION, i + 67, j + 13, this.imageWidth, 66, 66, 66);
         this.renderMap(guigraphics, integer, mapitemsaveddata, i + 85, j + 31, 0.226F);
      } else if (flag) {
         guigraphics.blit(BG_LOCATION, i + 67 + 16, j + 13, this.imageWidth, 132, 50, 66);
         this.renderMap(guigraphics, integer, mapitemsaveddata, i + 86, j + 16, 0.34F);
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0F, 0.0F, 1.0F);
         guigraphics.blit(BG_LOCATION, i + 67, j + 13 + 16, this.imageWidth, 132, 50, 66);
         this.renderMap(guigraphics, integer, mapitemsaveddata, i + 70, j + 32, 0.34F);
         guigraphics.pose().popPose();
      } else if (flag2) {
         guigraphics.blit(BG_LOCATION, i + 67, j + 13, this.imageWidth, 0, 66, 66);
         this.renderMap(guigraphics, integer, mapitemsaveddata, i + 71, j + 17, 0.45F);
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0F, 0.0F, 1.0F);
         guigraphics.blit(BG_LOCATION, i + 66, j + 12, 0, this.imageHeight, 66, 66);
         guigraphics.pose().popPose();
      } else {
         guigraphics.blit(BG_LOCATION, i + 67, j + 13, this.imageWidth, 0, 66, 66);
         this.renderMap(guigraphics, integer, mapitemsaveddata, i + 71, j + 17, 0.45F);
      }

   }

   private void renderMap(GuiGraphics guigraphics, @Nullable Integer integer, @Nullable MapItemSavedData mapitemsaveddata, int i, int j, float f) {
      if (integer != null && mapitemsaveddata != null) {
         guigraphics.pose().pushPose();
         guigraphics.pose().translate((float)i, (float)j, 1.0F);
         guigraphics.pose().scale(f, f, 1.0F);
         this.minecraft.gameRenderer.getMapRenderer().render(guigraphics.pose(), guigraphics.bufferSource(), integer, mapitemsaveddata, true, 15728880);
         guigraphics.flush();
         guigraphics.pose().popPose();
      }

   }
}
