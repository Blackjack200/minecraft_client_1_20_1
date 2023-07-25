package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class MerchantScreen extends AbstractContainerScreen<MerchantMenu> {
   private static final ResourceLocation VILLAGER_LOCATION = new ResourceLocation("textures/gui/container/villager2.png");
   private static final int TEXTURE_WIDTH = 512;
   private static final int TEXTURE_HEIGHT = 256;
   private static final int MERCHANT_MENU_PART_X = 99;
   private static final int PROGRESS_BAR_X = 136;
   private static final int PROGRESS_BAR_Y = 16;
   private static final int SELL_ITEM_1_X = 5;
   private static final int SELL_ITEM_2_X = 35;
   private static final int BUY_ITEM_X = 68;
   private static final int LABEL_Y = 6;
   private static final int NUMBER_OF_OFFER_BUTTONS = 7;
   private static final int TRADE_BUTTON_X = 5;
   private static final int TRADE_BUTTON_HEIGHT = 20;
   private static final int TRADE_BUTTON_WIDTH = 88;
   private static final int SCROLLER_HEIGHT = 27;
   private static final int SCROLLER_WIDTH = 6;
   private static final int SCROLL_BAR_HEIGHT = 139;
   private static final int SCROLL_BAR_TOP_POS_Y = 18;
   private static final int SCROLL_BAR_START_X = 94;
   private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
   private static final Component LEVEL_SEPARATOR = Component.literal(" - ");
   private static final Component DEPRECATED_TOOLTIP = Component.translatable("merchant.deprecated");
   private int shopItem;
   private final MerchantScreen.TradeOfferButton[] tradeOfferButtons = new MerchantScreen.TradeOfferButton[7];
   int scrollOff;
   private boolean isDragging;

   public MerchantScreen(MerchantMenu merchantmenu, Inventory inventory, Component component) {
      super(merchantmenu, inventory, component);
      this.imageWidth = 276;
      this.inventoryLabelX = 107;
   }

   private void postButtonClick() {
      this.menu.setSelectionHint(this.shopItem);
      this.menu.tryMoveItems(this.shopItem);
      this.minecraft.getConnection().send(new ServerboundSelectTradePacket(this.shopItem));
   }

   protected void init() {
      super.init();
      int i = (this.width - this.imageWidth) / 2;
      int j = (this.height - this.imageHeight) / 2;
      int k = j + 16 + 2;

      for(int l = 0; l < 7; ++l) {
         this.tradeOfferButtons[l] = this.addRenderableWidget(new MerchantScreen.TradeOfferButton(i + 5, k, l, (button) -> {
            if (button instanceof MerchantScreen.TradeOfferButton) {
               this.shopItem = ((MerchantScreen.TradeOfferButton)button).getIndex() + this.scrollOff;
               this.postButtonClick();
            }

         }));
         k += 20;
      }

   }

   protected void renderLabels(GuiGraphics guigraphics, int i, int j) {
      int k = this.menu.getTraderLevel();
      if (k > 0 && k <= 5 && this.menu.showProgressBar()) {
         Component component = this.title.copy().append(LEVEL_SEPARATOR).append(Component.translatable("merchant.level." + k));
         int l = this.font.width(component);
         int i1 = 49 + this.imageWidth / 2 - l / 2;
         guigraphics.drawString(this.font, component, i1, 6, 4210752, false);
      } else {
         guigraphics.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, 6, 4210752, false);
      }

      guigraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
      int j1 = this.font.width(TRADES_LABEL);
      guigraphics.drawString(this.font, TRADES_LABEL, 5 - j1 / 2 + 48, 6, 4210752, false);
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      int k = (this.width - this.imageWidth) / 2;
      int l = (this.height - this.imageHeight) / 2;
      guigraphics.blit(VILLAGER_LOCATION, k, l, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
      MerchantOffers merchantoffers = this.menu.getOffers();
      if (!merchantoffers.isEmpty()) {
         int i1 = this.shopItem;
         if (i1 < 0 || i1 >= merchantoffers.size()) {
            return;
         }

         MerchantOffer merchantoffer = merchantoffers.get(i1);
         if (merchantoffer.isOutOfStock()) {
            guigraphics.blit(VILLAGER_LOCATION, this.leftPos + 83 + 99, this.topPos + 35, 0, 311.0F, 0.0F, 28, 21, 512, 256);
         }
      }

   }

   private void renderProgressBar(GuiGraphics guigraphics, int i, int j, MerchantOffer merchantoffer) {
      int k = this.menu.getTraderLevel();
      int l = this.menu.getTraderXp();
      if (k < 5) {
         guigraphics.blit(VILLAGER_LOCATION, i + 136, j + 16, 0, 0.0F, 186.0F, 102, 5, 512, 256);
         int i1 = VillagerData.getMinXpPerLevel(k);
         if (l >= i1 && VillagerData.canLevelUp(k)) {
            int j1 = 100;
            float f = 100.0F / (float)(VillagerData.getMaxXpPerLevel(k) - i1);
            int k1 = Math.min(Mth.floor(f * (float)(l - i1)), 100);
            guigraphics.blit(VILLAGER_LOCATION, i + 136, j + 16, 0, 0.0F, 191.0F, k1 + 1, 5, 512, 256);
            int l1 = this.menu.getFutureTraderXp();
            if (l1 > 0) {
               int i2 = Math.min(Mth.floor((float)l1 * f), 100 - k1);
               guigraphics.blit(VILLAGER_LOCATION, i + 136 + k1 + 1, j + 16 + 1, 0, 2.0F, 182.0F, i2, 3, 512, 256);
            }

         }
      }
   }

   private void renderScroller(GuiGraphics guigraphics, int i, int j, MerchantOffers merchantoffers) {
      int k = merchantoffers.size() + 1 - 7;
      if (k > 1) {
         int l = 139 - (27 + (k - 1) * 139 / k);
         int i1 = 1 + l / k + 139 / k;
         int j1 = 113;
         int k1 = Math.min(113, this.scrollOff * i1);
         if (this.scrollOff == k - 1) {
            k1 = 113;
         }

         guigraphics.blit(VILLAGER_LOCATION, i + 94, j + 18 + k1, 0, 0.0F, 199.0F, 6, 27, 512, 256);
      } else {
         guigraphics.blit(VILLAGER_LOCATION, i + 94, j + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);
      MerchantOffers merchantoffers = this.menu.getOffers();
      if (!merchantoffers.isEmpty()) {
         int k = (this.width - this.imageWidth) / 2;
         int l = (this.height - this.imageHeight) / 2;
         int i1 = l + 16 + 1;
         int j1 = k + 5 + 5;
         this.renderScroller(guigraphics, k, l, merchantoffers);
         int k1 = 0;

         for(MerchantOffer merchantoffer : merchantoffers) {
            if (this.canScroll(merchantoffers.size()) && (k1 < this.scrollOff || k1 >= 7 + this.scrollOff)) {
               ++k1;
            } else {
               ItemStack itemstack = merchantoffer.getBaseCostA();
               ItemStack itemstack1 = merchantoffer.getCostA();
               ItemStack itemstack2 = merchantoffer.getCostB();
               ItemStack itemstack3 = merchantoffer.getResult();
               guigraphics.pose().pushPose();
               guigraphics.pose().translate(0.0F, 0.0F, 100.0F);
               int l1 = i1 + 2;
               this.renderAndDecorateCostA(guigraphics, itemstack1, itemstack, j1, l1);
               if (!itemstack2.isEmpty()) {
                  guigraphics.renderFakeItem(itemstack2, k + 5 + 35, l1);
                  guigraphics.renderItemDecorations(this.font, itemstack2, k + 5 + 35, l1);
               }

               this.renderButtonArrows(guigraphics, merchantoffer, k, l1);
               guigraphics.renderFakeItem(itemstack3, k + 5 + 68, l1);
               guigraphics.renderItemDecorations(this.font, itemstack3, k + 5 + 68, l1);
               guigraphics.pose().popPose();
               i1 += 20;
               ++k1;
            }
         }

         int i2 = this.shopItem;
         MerchantOffer merchantoffer1 = merchantoffers.get(i2);
         if (this.menu.showProgressBar()) {
            this.renderProgressBar(guigraphics, k, l, merchantoffer1);
         }

         if (merchantoffer1.isOutOfStock() && this.isHovering(186, 35, 22, 21, (double)i, (double)j) && this.menu.canRestock()) {
            guigraphics.renderTooltip(this.font, DEPRECATED_TOOLTIP, i, j);
         }

         for(MerchantScreen.TradeOfferButton merchantscreen_tradeofferbutton : this.tradeOfferButtons) {
            if (merchantscreen_tradeofferbutton.isHoveredOrFocused()) {
               merchantscreen_tradeofferbutton.renderToolTip(guigraphics, i, j);
            }

            merchantscreen_tradeofferbutton.visible = merchantscreen_tradeofferbutton.index < this.menu.getOffers().size();
         }

         RenderSystem.enableDepthTest();
      }

      this.renderTooltip(guigraphics, i, j);
   }

   private void renderButtonArrows(GuiGraphics guigraphics, MerchantOffer merchantoffer, int i, int j) {
      RenderSystem.enableBlend();
      if (merchantoffer.isOutOfStock()) {
         guigraphics.blit(VILLAGER_LOCATION, i + 5 + 35 + 20, j + 3, 0, 25.0F, 171.0F, 10, 9, 512, 256);
      } else {
         guigraphics.blit(VILLAGER_LOCATION, i + 5 + 35 + 20, j + 3, 0, 15.0F, 171.0F, 10, 9, 512, 256);
      }

   }

   private void renderAndDecorateCostA(GuiGraphics guigraphics, ItemStack itemstack, ItemStack itemstack1, int i, int j) {
      guigraphics.renderFakeItem(itemstack, i, j);
      if (itemstack1.getCount() == itemstack.getCount()) {
         guigraphics.renderItemDecorations(this.font, itemstack, i, j);
      } else {
         guigraphics.renderItemDecorations(this.font, itemstack1, i, j, itemstack1.getCount() == 1 ? "1" : null);
         guigraphics.renderItemDecorations(this.font, itemstack, i + 14, j, itemstack.getCount() == 1 ? "1" : null);
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0F, 0.0F, 300.0F);
         guigraphics.blit(VILLAGER_LOCATION, i + 7, j + 12, 0, 0.0F, 176.0F, 9, 2, 512, 256);
         guigraphics.pose().popPose();
      }

   }

   private boolean canScroll(int i) {
      return i > 7;
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      int i = this.menu.getOffers().size();
      if (this.canScroll(i)) {
         int j = i - 7;
         this.scrollOff = Mth.clamp((int)((double)this.scrollOff - d2), 0, j);
      }

      return true;
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      int j = this.menu.getOffers().size();
      if (this.isDragging) {
         int k = this.topPos + 18;
         int l = k + 139;
         int i1 = j - 7;
         float f = ((float)d1 - (float)k - 13.5F) / ((float)(l - k) - 27.0F);
         f = f * (float)i1 + 0.5F;
         this.scrollOff = Mth.clamp((int)f, 0, i1);
         return true;
      } else {
         return super.mouseDragged(d0, d1, i, d2, d3);
      }
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      this.isDragging = false;
      int j = (this.width - this.imageWidth) / 2;
      int k = (this.height - this.imageHeight) / 2;
      if (this.canScroll(this.menu.getOffers().size()) && d0 > (double)(j + 94) && d0 < (double)(j + 94 + 6) && d1 > (double)(k + 18) && d1 <= (double)(k + 18 + 139 + 1)) {
         this.isDragging = true;
      }

      return super.mouseClicked(d0, d1, i);
   }

   class TradeOfferButton extends Button {
      final int index;

      public TradeOfferButton(int i, int j, int k, Button.OnPress button_onpress) {
         super(i, j, 88, 20, CommonComponents.EMPTY, button_onpress, DEFAULT_NARRATION);
         this.index = k;
         this.visible = false;
      }

      public int getIndex() {
         return this.index;
      }

      public void renderToolTip(GuiGraphics guigraphics, int i, int j) {
         if (this.isHovered && MerchantScreen.this.menu.getOffers().size() > this.index + MerchantScreen.this.scrollOff) {
            if (i < this.getX() + 20) {
               ItemStack itemstack = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getCostA();
               guigraphics.renderTooltip(MerchantScreen.this.font, itemstack, i, j);
            } else if (i < this.getX() + 50 && i > this.getX() + 30) {
               ItemStack itemstack1 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getCostB();
               if (!itemstack1.isEmpty()) {
                  guigraphics.renderTooltip(MerchantScreen.this.font, itemstack1, i, j);
               }
            } else if (i > this.getX() + 65) {
               ItemStack itemstack2 = MerchantScreen.this.menu.getOffers().get(this.index + MerchantScreen.this.scrollOff).getResult();
               guigraphics.renderTooltip(MerchantScreen.this.font, itemstack2, i, j);
            }
         }

      }
   }
}
