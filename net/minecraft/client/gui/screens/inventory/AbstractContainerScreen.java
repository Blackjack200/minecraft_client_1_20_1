package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractContainerScreen<T extends AbstractContainerMenu> extends Screen implements MenuAccess<T> {
   public static final ResourceLocation INVENTORY_LOCATION = new ResourceLocation("textures/gui/container/inventory.png");
   private static final float SNAPBACK_SPEED = 100.0F;
   private static final int QUICKDROP_DELAY = 500;
   public static final int SLOT_ITEM_BLIT_OFFSET = 100;
   private static final int HOVER_ITEM_BLIT_OFFSET = 200;
   protected int imageWidth = 176;
   protected int imageHeight = 166;
   protected int titleLabelX;
   protected int titleLabelY;
   protected int inventoryLabelX;
   protected int inventoryLabelY;
   protected final T menu;
   protected final Component playerInventoryTitle;
   @Nullable
   protected Slot hoveredSlot;
   @Nullable
   private Slot clickedSlot;
   @Nullable
   private Slot snapbackEnd;
   @Nullable
   private Slot quickdropSlot;
   @Nullable
   private Slot lastClickSlot;
   protected int leftPos;
   protected int topPos;
   private boolean isSplittingStack;
   private ItemStack draggingItem = ItemStack.EMPTY;
   private int snapbackStartX;
   private int snapbackStartY;
   private long snapbackTime;
   private ItemStack snapbackItem = ItemStack.EMPTY;
   private long quickdropTime;
   protected final Set<Slot> quickCraftSlots = Sets.newHashSet();
   protected boolean isQuickCrafting;
   private int quickCraftingType;
   private int quickCraftingButton;
   private boolean skipNextRelease;
   private int quickCraftingRemainder;
   private long lastClickTime;
   private int lastClickButton;
   private boolean doubleclick;
   private ItemStack lastQuickMoved = ItemStack.EMPTY;

   public AbstractContainerScreen(T abstractcontainermenu, Inventory inventory, Component component) {
      super(component);
      this.menu = abstractcontainermenu;
      this.playerInventoryTitle = inventory.getDisplayName();
      this.skipNextRelease = true;
      this.titleLabelX = 8;
      this.titleLabelY = 6;
      this.inventoryLabelX = 8;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   protected void init() {
      this.leftPos = (this.width - this.imageWidth) / 2;
      this.topPos = (this.height - this.imageHeight) / 2;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      int k = this.leftPos;
      int l = this.topPos;
      this.renderBg(guigraphics, f, i, j);
      RenderSystem.disableDepthTest();
      super.render(guigraphics, i, j, f);
      guigraphics.pose().pushPose();
      guigraphics.pose().translate((float)k, (float)l, 0.0F);
      this.hoveredSlot = null;

      for(int i1 = 0; i1 < this.menu.slots.size(); ++i1) {
         Slot slot = this.menu.slots.get(i1);
         if (slot.isActive()) {
            this.renderSlot(guigraphics, slot);
         }

         if (this.isHovering(slot, (double)i, (double)j) && slot.isActive()) {
            this.hoveredSlot = slot;
            int j1 = slot.x;
            int k1 = slot.y;
            if (this.hoveredSlot.isHighlightable()) {
               renderSlotHighlight(guigraphics, j1, k1, 0);
            }
         }
      }

      this.renderLabels(guigraphics, i, j);
      ItemStack itemstack = this.draggingItem.isEmpty() ? this.menu.getCarried() : this.draggingItem;
      if (!itemstack.isEmpty()) {
         int l1 = 8;
         int i2 = this.draggingItem.isEmpty() ? 8 : 16;
         String s = null;
         if (!this.draggingItem.isEmpty() && this.isSplittingStack) {
            itemstack = itemstack.copyWithCount(Mth.ceil((float)itemstack.getCount() / 2.0F));
         } else if (this.isQuickCrafting && this.quickCraftSlots.size() > 1) {
            itemstack = itemstack.copyWithCount(this.quickCraftingRemainder);
            if (itemstack.isEmpty()) {
               s = ChatFormatting.YELLOW + "0";
            }
         }

         this.renderFloatingItem(guigraphics, itemstack, i - k - 8, j - l - i2, s);
      }

      if (!this.snapbackItem.isEmpty()) {
         float f1 = (float)(Util.getMillis() - this.snapbackTime) / 100.0F;
         if (f1 >= 1.0F) {
            f1 = 1.0F;
            this.snapbackItem = ItemStack.EMPTY;
         }

         int j2 = this.snapbackEnd.x - this.snapbackStartX;
         int k2 = this.snapbackEnd.y - this.snapbackStartY;
         int l2 = this.snapbackStartX + (int)((float)j2 * f1);
         int i3 = this.snapbackStartY + (int)((float)k2 * f1);
         this.renderFloatingItem(guigraphics, this.snapbackItem, l2, i3, (String)null);
      }

      guigraphics.pose().popPose();
      RenderSystem.enableDepthTest();
   }

   public static void renderSlotHighlight(GuiGraphics guigraphics, int i, int j, int k) {
      guigraphics.fillGradient(RenderType.guiOverlay(), i, j, i + 16, j + 16, -2130706433, -2130706433, k);
   }

   protected void renderTooltip(GuiGraphics guigraphics, int i, int j) {
      if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
         ItemStack itemstack = this.hoveredSlot.getItem();
         guigraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), i, j);
      }

   }

   protected List<Component> getTooltipFromContainerItem(ItemStack itemstack) {
      return getTooltipFromItem(this.minecraft, itemstack);
   }

   private void renderFloatingItem(GuiGraphics guigraphics, ItemStack itemstack, int i, int j, String s) {
      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, 232.0F);
      guigraphics.renderItem(itemstack, i, j);
      guigraphics.renderItemDecorations(this.font, itemstack, i, j - (this.draggingItem.isEmpty() ? 0 : 8), s);
      guigraphics.pose().popPose();
   }

   protected void renderLabels(GuiGraphics guigraphics, int i, int j) {
      guigraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
      guigraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
   }

   protected abstract void renderBg(GuiGraphics guigraphics, float f, int i, int j);

   private void renderSlot(GuiGraphics guigraphics, Slot slot) {
      int i = slot.x;
      int j = slot.y;
      ItemStack itemstack = slot.getItem();
      boolean flag = false;
      boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
      ItemStack itemstack1 = this.menu.getCarried();
      String s = null;
      if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
         itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
      } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemstack1.isEmpty()) {
         if (this.quickCraftSlots.size() == 1) {
            return;
         }

         if (AbstractContainerMenu.canItemQuickReplace(slot, itemstack1, true) && this.menu.canDragTo(slot)) {
            flag = true;
            int k = Math.min(itemstack1.getMaxStackSize(), slot.getMaxStackSize(itemstack1));
            int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
            int i1 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack1) + l;
            if (i1 > k) {
               i1 = k;
               s = ChatFormatting.YELLOW.toString() + k;
            }

            itemstack = itemstack1.copyWithCount(i1);
         } else {
            this.quickCraftSlots.remove(slot);
            this.recalculateQuickCraftRemaining();
         }
      }

      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, 100.0F);
      if (itemstack.isEmpty() && slot.isActive()) {
         Pair<ResourceLocation, ResourceLocation> pair = slot.getNoItemIcon();
         if (pair != null) {
            TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
            guigraphics.blit(i, j, 0, 16, 16, textureatlassprite);
            flag1 = true;
         }
      }

      if (!flag1) {
         if (flag) {
            guigraphics.fill(i, j, i + 16, j + 16, -2130706433);
         }

         guigraphics.renderItem(itemstack, i, j, slot.x + slot.y * this.imageWidth);
         guigraphics.renderItemDecorations(this.font, itemstack, i, j, s);
      }

      guigraphics.pose().popPose();
   }

   private void recalculateQuickCraftRemaining() {
      ItemStack itemstack = this.menu.getCarried();
      if (!itemstack.isEmpty() && this.isQuickCrafting) {
         if (this.quickCraftingType == 2) {
            this.quickCraftingRemainder = itemstack.getMaxStackSize();
         } else {
            this.quickCraftingRemainder = itemstack.getCount();

            for(Slot slot : this.quickCraftSlots) {
               ItemStack itemstack1 = slot.getItem();
               int i = itemstack1.isEmpty() ? 0 : itemstack1.getCount();
               int j = Math.min(itemstack.getMaxStackSize(), slot.getMaxStackSize(itemstack));
               int k = Math.min(AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack) + i, j);
               this.quickCraftingRemainder -= k - i;
            }

         }
      }
   }

   @Nullable
   private Slot findSlot(double d0, double d1) {
      for(int i = 0; i < this.menu.slots.size(); ++i) {
         Slot slot = this.menu.slots.get(i);
         if (this.isHovering(slot, d0, d1) && slot.isActive()) {
            return slot;
         }
      }

      return null;
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (super.mouseClicked(d0, d1, i)) {
         return true;
      } else {
         boolean flag = this.minecraft.options.keyPickItem.matchesMouse(i) && this.minecraft.gameMode.hasInfiniteItems();
         Slot slot = this.findSlot(d0, d1);
         long j = Util.getMillis();
         this.doubleclick = this.lastClickSlot == slot && j - this.lastClickTime < 250L && this.lastClickButton == i;
         this.skipNextRelease = false;
         if (i != 0 && i != 1 && !flag) {
            this.checkHotbarMouseClicked(i);
         } else {
            int k = this.leftPos;
            int l = this.topPos;
            boolean flag1 = this.hasClickedOutside(d0, d1, k, l, i);
            int i1 = -1;
            if (slot != null) {
               i1 = slot.index;
            }

            if (flag1) {
               i1 = -999;
            }

            if (this.minecraft.options.touchscreen().get() && flag1 && this.menu.getCarried().isEmpty()) {
               this.onClose();
               return true;
            }

            if (i1 != -1) {
               if (this.minecraft.options.touchscreen().get()) {
                  if (slot != null && slot.hasItem()) {
                     this.clickedSlot = slot;
                     this.draggingItem = ItemStack.EMPTY;
                     this.isSplittingStack = i == 1;
                  } else {
                     this.clickedSlot = null;
                  }
               } else if (!this.isQuickCrafting) {
                  if (this.menu.getCarried().isEmpty()) {
                     if (flag) {
                        this.slotClicked(slot, i1, i, ClickType.CLONE);
                     } else {
                        boolean flag2 = i1 != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
                        ClickType clicktype = ClickType.PICKUP;
                        if (flag2) {
                           this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
                           clicktype = ClickType.QUICK_MOVE;
                        } else if (i1 == -999) {
                           clicktype = ClickType.THROW;
                        }

                        this.slotClicked(slot, i1, i, clicktype);
                     }

                     this.skipNextRelease = true;
                  } else {
                     this.isQuickCrafting = true;
                     this.quickCraftingButton = i;
                     this.quickCraftSlots.clear();
                     if (i == 0) {
                        this.quickCraftingType = 0;
                     } else if (i == 1) {
                        this.quickCraftingType = 1;
                     } else if (flag) {
                        this.quickCraftingType = 2;
                     }
                  }
               }
            }
         }

         this.lastClickSlot = slot;
         this.lastClickTime = j;
         this.lastClickButton = i;
         return true;
      }
   }

   private void checkHotbarMouseClicked(int i) {
      if (this.hoveredSlot != null && this.menu.getCarried().isEmpty()) {
         if (this.minecraft.options.keySwapOffhand.matchesMouse(i)) {
            this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
            return;
         }

         for(int j = 0; j < 9; ++j) {
            if (this.minecraft.options.keyHotbarSlots[j].matchesMouse(i)) {
               this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, j, ClickType.SWAP);
            }
         }
      }

   }

   protected boolean hasClickedOutside(double d0, double d1, int i, int j, int k) {
      return d0 < (double)i || d1 < (double)j || d0 >= (double)(i + this.imageWidth) || d1 >= (double)(j + this.imageHeight);
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      Slot slot = this.findSlot(d0, d1);
      ItemStack itemstack = this.menu.getCarried();
      if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
         if (i == 0 || i == 1) {
            if (this.draggingItem.isEmpty()) {
               if (slot != this.clickedSlot && !this.clickedSlot.getItem().isEmpty()) {
                  this.draggingItem = this.clickedSlot.getItem().copy();
               }
            } else if (this.draggingItem.getCount() > 1 && slot != null && AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false)) {
               long j = Util.getMillis();
               if (this.quickdropSlot == slot) {
                  if (j - this.quickdropTime > 500L) {
                     this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                     this.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                     this.slotClicked(this.clickedSlot, this.clickedSlot.index, 0, ClickType.PICKUP);
                     this.quickdropTime = j + 750L;
                     this.draggingItem.shrink(1);
                  }
               } else {
                  this.quickdropSlot = slot;
                  this.quickdropTime = j;
               }
            }
         }
      } else if (this.isQuickCrafting && slot != null && !itemstack.isEmpty() && (itemstack.getCount() > this.quickCraftSlots.size() || this.quickCraftingType == 2) && AbstractContainerMenu.canItemQuickReplace(slot, itemstack, true) && slot.mayPlace(itemstack) && this.menu.canDragTo(slot)) {
         this.quickCraftSlots.add(slot);
         this.recalculateQuickCraftRemaining();
      }

      return true;
   }

   public boolean mouseReleased(double d0, double d1, int i) {
      Slot slot = this.findSlot(d0, d1);
      int j = this.leftPos;
      int k = this.topPos;
      boolean flag = this.hasClickedOutside(d0, d1, j, k, i);
      int l = -1;
      if (slot != null) {
         l = slot.index;
      }

      if (flag) {
         l = -999;
      }

      if (this.doubleclick && slot != null && i == 0 && this.menu.canTakeItemForPickAll(ItemStack.EMPTY, slot)) {
         if (hasShiftDown()) {
            if (!this.lastQuickMoved.isEmpty()) {
               for(Slot slot1 : this.menu.slots) {
                  if (slot1 != null && slot1.mayPickup(this.minecraft.player) && slot1.hasItem() && slot1.container == slot.container && AbstractContainerMenu.canItemQuickReplace(slot1, this.lastQuickMoved, true)) {
                     this.slotClicked(slot1, slot1.index, i, ClickType.QUICK_MOVE);
                  }
               }
            }
         } else {
            this.slotClicked(slot, l, i, ClickType.PICKUP_ALL);
         }

         this.doubleclick = false;
         this.lastClickTime = 0L;
      } else {
         if (this.isQuickCrafting && this.quickCraftingButton != i) {
            this.isQuickCrafting = false;
            this.quickCraftSlots.clear();
            this.skipNextRelease = true;
            return true;
         }

         if (this.skipNextRelease) {
            this.skipNextRelease = false;
            return true;
         }

         if (this.clickedSlot != null && this.minecraft.options.touchscreen().get()) {
            if (i == 0 || i == 1) {
               if (this.draggingItem.isEmpty() && slot != this.clickedSlot) {
                  this.draggingItem = this.clickedSlot.getItem();
               }

               boolean flag1 = AbstractContainerMenu.canItemQuickReplace(slot, this.draggingItem, false);
               if (l != -1 && !this.draggingItem.isEmpty() && flag1) {
                  this.slotClicked(this.clickedSlot, this.clickedSlot.index, i, ClickType.PICKUP);
                  this.slotClicked(slot, l, 0, ClickType.PICKUP);
                  if (this.menu.getCarried().isEmpty()) {
                     this.snapbackItem = ItemStack.EMPTY;
                  } else {
                     this.slotClicked(this.clickedSlot, this.clickedSlot.index, i, ClickType.PICKUP);
                     this.snapbackStartX = Mth.floor(d0 - (double)j);
                     this.snapbackStartY = Mth.floor(d1 - (double)k);
                     this.snapbackEnd = this.clickedSlot;
                     this.snapbackItem = this.draggingItem;
                     this.snapbackTime = Util.getMillis();
                  }
               } else if (!this.draggingItem.isEmpty()) {
                  this.snapbackStartX = Mth.floor(d0 - (double)j);
                  this.snapbackStartY = Mth.floor(d1 - (double)k);
                  this.snapbackEnd = this.clickedSlot;
                  this.snapbackItem = this.draggingItem;
                  this.snapbackTime = Util.getMillis();
               }

               this.clearDraggingState();
            }
         } else if (this.isQuickCrafting && !this.quickCraftSlots.isEmpty()) {
            this.slotClicked((Slot)null, -999, AbstractContainerMenu.getQuickcraftMask(0, this.quickCraftingType), ClickType.QUICK_CRAFT);

            for(Slot slot2 : this.quickCraftSlots) {
               this.slotClicked(slot2, slot2.index, AbstractContainerMenu.getQuickcraftMask(1, this.quickCraftingType), ClickType.QUICK_CRAFT);
            }

            this.slotClicked((Slot)null, -999, AbstractContainerMenu.getQuickcraftMask(2, this.quickCraftingType), ClickType.QUICK_CRAFT);
         } else if (!this.menu.getCarried().isEmpty()) {
            if (this.minecraft.options.keyPickItem.matchesMouse(i)) {
               this.slotClicked(slot, l, i, ClickType.CLONE);
            } else {
               boolean flag2 = l != -999 && (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344));
               if (flag2) {
                  this.lastQuickMoved = slot != null && slot.hasItem() ? slot.getItem().copy() : ItemStack.EMPTY;
               }

               this.slotClicked(slot, l, i, flag2 ? ClickType.QUICK_MOVE : ClickType.PICKUP);
            }
         }
      }

      if (this.menu.getCarried().isEmpty()) {
         this.lastClickTime = 0L;
      }

      this.isQuickCrafting = false;
      return true;
   }

   public void clearDraggingState() {
      this.draggingItem = ItemStack.EMPTY;
      this.clickedSlot = null;
   }

   private boolean isHovering(Slot slot, double d0, double d1) {
      return this.isHovering(slot.x, slot.y, 16, 16, d0, d1);
   }

   protected boolean isHovering(int i, int j, int k, int l, double d0, double d1) {
      int i1 = this.leftPos;
      int j1 = this.topPos;
      d0 -= (double)i1;
      d1 -= (double)j1;
      return d0 >= (double)(i - 1) && d0 < (double)(i + k + 1) && d1 >= (double)(j - 1) && d1 < (double)(j + l + 1);
   }

   protected void slotClicked(Slot slot, int i, int j, ClickType clicktype) {
      if (slot != null) {
         i = slot.index;
      }

      this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, i, j, clicktype, this.minecraft.player);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (super.keyPressed(i, j, k)) {
         return true;
      } else if (this.minecraft.options.keyInventory.matches(i, j)) {
         this.onClose();
         return true;
      } else {
         this.checkHotbarKeyPressed(i, j);
         if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            if (this.minecraft.options.keyPickItem.matches(i, j)) {
               this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 0, ClickType.CLONE);
            } else if (this.minecraft.options.keyDrop.matches(i, j)) {
               this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, hasControlDown() ? 1 : 0, ClickType.THROW);
            }
         }

         return true;
      }
   }

   protected boolean checkHotbarKeyPressed(int i, int j) {
      if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
         if (this.minecraft.options.keySwapOffhand.matches(i, j)) {
            this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, 40, ClickType.SWAP);
            return true;
         }

         for(int k = 0; k < 9; ++k) {
            if (this.minecraft.options.keyHotbarSlots[k].matches(i, j)) {
               this.slotClicked(this.hoveredSlot, this.hoveredSlot.index, k, ClickType.SWAP);
               return true;
            }
         }
      }

      return false;
   }

   public void removed() {
      if (this.minecraft.player != null) {
         this.menu.removed(this.minecraft.player);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   public final void tick() {
      super.tick();
      if (this.minecraft.player.isAlive() && !this.minecraft.player.isRemoved()) {
         this.containerTick();
      } else {
         this.minecraft.player.closeContainer();
      }

   }

   protected void containerTick() {
   }

   public T getMenu() {
      return this.menu;
   }

   public void onClose() {
      this.minecraft.player.closeContainer();
      super.onClose();
   }
}
