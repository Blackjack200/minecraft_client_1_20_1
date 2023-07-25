package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class CreativeModeInventoryScreen extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
   private static final ResourceLocation CREATIVE_TABS_LOCATION = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
   private static final String GUI_CREATIVE_TAB_PREFIX = "textures/gui/container/creative_inventory/tab_";
   private static final String CUSTOM_SLOT_LOCK = "CustomCreativeLock";
   private static final int NUM_ROWS = 5;
   private static final int NUM_COLS = 9;
   private static final int TAB_WIDTH = 26;
   private static final int TAB_HEIGHT = 32;
   private static final int SCROLLER_WIDTH = 12;
   private static final int SCROLLER_HEIGHT = 15;
   static final SimpleContainer CONTAINER = new SimpleContainer(45);
   private static final Component TRASH_SLOT_TOOLTIP = Component.translatable("inventory.binSlot");
   private static final int TEXT_COLOR = 16777215;
   private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
   private float scrollOffs;
   private boolean scrolling;
   private EditBox searchBox;
   @Nullable
   private List<Slot> originalSlots;
   @Nullable
   private Slot destroyItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTextInput;
   private boolean hasClickedOutside;
   private final Set<TagKey<Item>> visibleTags = new HashSet<>();
   private final boolean displayOperatorCreativeTab;

   public CreativeModeInventoryScreen(Player player, FeatureFlagSet featureflagset, boolean flag) {
      super(new CreativeModeInventoryScreen.ItemPickerMenu(player), player.getInventory(), CommonComponents.EMPTY);
      player.containerMenu = this.menu;
      this.imageHeight = 136;
      this.imageWidth = 195;
      this.displayOperatorCreativeTab = flag;
      CreativeModeTabs.tryRebuildTabContents(featureflagset, this.hasPermissions(player), player.level().registryAccess());
   }

   private boolean hasPermissions(Player player) {
      return player.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
   }

   private void tryRefreshInvalidatedTabs(FeatureFlagSet featureflagset, boolean flag, HolderLookup.Provider holderlookup_provider) {
      if (CreativeModeTabs.tryRebuildTabContents(featureflagset, flag, holderlookup_provider)) {
         for(CreativeModeTab creativemodetab : CreativeModeTabs.allTabs()) {
            Collection<ItemStack> collection = creativemodetab.getDisplayItems();
            if (creativemodetab == selectedTab) {
               if (creativemodetab.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
                  this.selectTab(CreativeModeTabs.getDefaultTab());
               } else {
                  this.refreshCurrentTabContents(collection);
               }
            }
         }
      }

   }

   private void refreshCurrentTabContents(Collection<ItemStack> collection) {
      int i = this.menu.getRowIndexForScroll(this.scrollOffs);
      (this.menu).items.clear();
      if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
         this.refreshSearchResults();
      } else {
         (this.menu).items.addAll(collection);
      }

      this.scrollOffs = this.menu.getScrollForRowIndex(i);
      this.menu.scrollTo(this.scrollOffs);
   }

   public void containerTick() {
      super.containerTick();
      if (this.minecraft != null) {
         if (this.minecraft.player != null) {
            this.tryRefreshInvalidatedTabs(this.minecraft.player.connection.enabledFeatures(), this.hasPermissions(this.minecraft.player), this.minecraft.player.level().registryAccess());
         }

         if (!this.minecraft.gameMode.hasInfiniteItems()) {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
         } else {
            this.searchBox.tick();
         }

      }
   }

   protected void slotClicked(@Nullable Slot slot, int i, int j, ClickType clicktype) {
      if (this.isCreativeSlot(slot)) {
         this.searchBox.moveCursorToEnd();
         this.searchBox.setHighlightPos(0);
      }

      boolean flag = clicktype == ClickType.QUICK_MOVE;
      clicktype = i == -999 && clicktype == ClickType.PICKUP ? ClickType.THROW : clicktype;
      if (slot == null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && clicktype != ClickType.QUICK_CRAFT) {
         if (!this.menu.getCarried().isEmpty() && this.hasClickedOutside) {
            if (j == 0) {
               this.minecraft.player.drop(this.menu.getCarried(), true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
               this.menu.setCarried(ItemStack.EMPTY);
            }

            if (j == 1) {
               ItemStack itemstack9 = this.menu.getCarried().split(1);
               this.minecraft.player.drop(itemstack9, true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack9);
            }
         }
      } else {
         if (slot != null && !slot.mayPickup(this.minecraft.player)) {
            return;
         }

         if (slot == this.destroyItemSlot && flag) {
            for(int k = 0; k < this.minecraft.player.inventoryMenu.getItems().size(); ++k) {
               this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, k);
            }
         } else if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            if (slot == this.destroyItemSlot) {
               this.menu.setCarried(ItemStack.EMPTY);
            } else if (clicktype == ClickType.THROW && slot != null && slot.hasItem()) {
               ItemStack itemstack = slot.remove(j == 0 ? 1 : slot.getItem().getMaxStackSize());
               ItemStack itemstack1 = slot.getItem();
               this.minecraft.player.drop(itemstack, true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack);
               this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack1, ((CreativeModeInventoryScreen.SlotWrapper)slot).target.index);
            } else if (clicktype == ClickType.THROW && !this.menu.getCarried().isEmpty()) {
               this.minecraft.player.drop(this.menu.getCarried(), true);
               this.minecraft.gameMode.handleCreativeModeItemDrop(this.menu.getCarried());
               this.menu.setCarried(ItemStack.EMPTY);
            } else {
               this.minecraft.player.inventoryMenu.clicked(slot == null ? i : ((CreativeModeInventoryScreen.SlotWrapper)slot).target.index, j, clicktype, this.minecraft.player);
               this.minecraft.player.inventoryMenu.broadcastChanges();
            }
         } else if (clicktype != ClickType.QUICK_CRAFT && slot.container == CONTAINER) {
            ItemStack itemstack2 = this.menu.getCarried();
            ItemStack itemstack3 = slot.getItem();
            if (clicktype == ClickType.SWAP) {
               if (!itemstack3.isEmpty()) {
                  this.minecraft.player.getInventory().setItem(j, itemstack3.copyWithCount(itemstack3.getMaxStackSize()));
                  this.minecraft.player.inventoryMenu.broadcastChanges();
               }

               return;
            }

            if (clicktype == ClickType.CLONE) {
               if (this.menu.getCarried().isEmpty() && slot.hasItem()) {
                  ItemStack itemstack4 = slot.getItem();
                  this.menu.setCarried(itemstack4.copyWithCount(itemstack4.getMaxStackSize()));
               }

               return;
            }

            if (clicktype == ClickType.THROW) {
               if (!itemstack3.isEmpty()) {
                  ItemStack itemstack5 = itemstack3.copyWithCount(j == 0 ? 1 : itemstack3.getMaxStackSize());
                  this.minecraft.player.drop(itemstack5, true);
                  this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack5);
               }

               return;
            }

            if (!itemstack2.isEmpty() && !itemstack3.isEmpty() && ItemStack.isSameItemSameTags(itemstack2, itemstack3)) {
               if (j == 0) {
                  if (flag) {
                     itemstack2.setCount(itemstack2.getMaxStackSize());
                  } else if (itemstack2.getCount() < itemstack2.getMaxStackSize()) {
                     itemstack2.grow(1);
                  }
               } else {
                  itemstack2.shrink(1);
               }
            } else if (!itemstack3.isEmpty() && itemstack2.isEmpty()) {
               int l = flag ? itemstack3.getMaxStackSize() : itemstack3.getCount();
               this.menu.setCarried(itemstack3.copyWithCount(l));
            } else if (j == 0) {
               this.menu.setCarried(ItemStack.EMPTY);
            } else if (!this.menu.getCarried().isEmpty()) {
               this.menu.getCarried().shrink(1);
            }
         } else if (this.menu != null) {
            ItemStack itemstack6 = slot == null ? ItemStack.EMPTY : this.menu.getSlot(slot.index).getItem();
            this.menu.clicked(slot == null ? i : slot.index, j, clicktype, this.minecraft.player);
            if (AbstractContainerMenu.getQuickcraftHeader(j) == 2) {
               for(int i1 = 0; i1 < 9; ++i1) {
                  this.minecraft.gameMode.handleCreativeModeItemAdd(this.menu.getSlot(45 + i1).getItem(), 36 + i1);
               }
            } else if (slot != null) {
               ItemStack itemstack7 = this.menu.getSlot(slot.index).getItem();
               this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack7, slot.index - (this.menu).slots.size() + 9 + 36);
               int j1 = 45 + j;
               if (clicktype == ClickType.SWAP) {
                  this.minecraft.gameMode.handleCreativeModeItemAdd(itemstack6, j1 - (this.menu).slots.size() + 9 + 36);
               } else if (clicktype == ClickType.THROW && !itemstack6.isEmpty()) {
                  ItemStack itemstack8 = itemstack6.copyWithCount(j == 0 ? 1 : itemstack6.getMaxStackSize());
                  this.minecraft.player.drop(itemstack8, true);
                  this.minecraft.gameMode.handleCreativeModeItemDrop(itemstack8);
               }

               this.minecraft.player.inventoryMenu.broadcastChanges();
            }
         }
      }

   }

   private boolean isCreativeSlot(@Nullable Slot slot) {
      return slot != null && slot.container == CONTAINER;
   }

   protected void init() {
      if (this.minecraft.gameMode.hasInfiniteItems()) {
         super.init();
         this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
         this.searchBox.setMaxLength(50);
         this.searchBox.setBordered(false);
         this.searchBox.setVisible(false);
         this.searchBox.setTextColor(16777215);
         this.addWidget(this.searchBox);
         CreativeModeTab creativemodetab = selectedTab;
         selectedTab = CreativeModeTabs.getDefaultTab();
         this.selectTab(creativemodetab);
         this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
         this.listener = new CreativeInventoryListener(this.minecraft);
         this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
         if (!selectedTab.shouldDisplay()) {
            this.selectTab(CreativeModeTabs.getDefaultTab());
         }
      } else {
         this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
      }

   }

   public void resize(Minecraft minecraft, int i, int j) {
      int k = this.menu.getRowIndexForScroll(this.scrollOffs);
      String s = this.searchBox.getValue();
      this.init(minecraft, i, j);
      this.searchBox.setValue(s);
      if (!this.searchBox.getValue().isEmpty()) {
         this.refreshSearchResults();
      }

      this.scrollOffs = this.menu.getScrollForRowIndex(k);
      this.menu.scrollTo(this.scrollOffs);
   }

   public void removed() {
      super.removed();
      if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
         this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
      }

   }

   public boolean charTyped(char c0, int i) {
      if (this.ignoreTextInput) {
         return false;
      } else if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
         return false;
      } else {
         String s = this.searchBox.getValue();
         if (this.searchBox.charTyped(c0, i)) {
            if (!Objects.equals(s, this.searchBox.getValue())) {
               this.refreshSearchResults();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      this.ignoreTextInput = false;
      if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
         if (this.minecraft.options.keyChat.matches(i, j)) {
            this.ignoreTextInput = true;
            this.selectTab(CreativeModeTabs.searchTab());
            return true;
         } else {
            return super.keyPressed(i, j, k);
         }
      } else {
         boolean flag = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
         boolean flag1 = InputConstants.getKey(i, j).getNumericKeyValue().isPresent();
         if (flag && flag1 && this.checkHotbarKeyPressed(i, j)) {
            this.ignoreTextInput = true;
            return true;
         } else {
            String s = this.searchBox.getValue();
            if (this.searchBox.keyPressed(i, j, k)) {
               if (!Objects.equals(s, this.searchBox.getValue())) {
                  this.refreshSearchResults();
               }

               return true;
            } else {
               return this.searchBox.isFocused() && this.searchBox.isVisible() && i != 256 ? true : super.keyPressed(i, j, k);
            }
         }
      }
   }

   public boolean keyReleased(int i, int j, int k) {
      this.ignoreTextInput = false;
      return super.keyReleased(i, j, k);
   }

   private void refreshSearchResults() {
      (this.menu).items.clear();
      this.visibleTags.clear();
      String s = this.searchBox.getValue();
      if (s.isEmpty()) {
         (this.menu).items.addAll(selectedTab.getDisplayItems());
      } else {
         SearchTree<ItemStack> searchtree;
         if (s.startsWith("#")) {
            s = s.substring(1);
            searchtree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_TAGS);
            this.updateVisibleTags(s);
         } else {
            searchtree = this.minecraft.getSearchTree(SearchRegistry.CREATIVE_NAMES);
         }

         (this.menu).items.addAll(searchtree.search(s.toLowerCase(Locale.ROOT)));
      }

      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
   }

   private void updateVisibleTags(String s) {
      int i = s.indexOf(58);
      Predicate<ResourceLocation> predicate;
      if (i == -1) {
         predicate = (resourcelocation1) -> resourcelocation1.getPath().contains(s);
      } else {
         String s1 = s.substring(0, i).trim();
         String s2 = s.substring(i + 1).trim();
         predicate = (resourcelocation) -> resourcelocation.getNamespace().contains(s1) && resourcelocation.getPath().contains(s2);
      }

      BuiltInRegistries.ITEM.getTagNames().filter((tagkey) -> predicate.test(tagkey.location())).forEach(this.visibleTags::add);
   }

   protected void renderLabels(GuiGraphics guigraphics, int i, int j) {
      if (selectedTab.showTitle()) {
         guigraphics.drawString(this.font, selectedTab.getDisplayName(), 8, 6, 4210752, false);
      }

   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (i == 0) {
         double d2 = d0 - (double)this.leftPos;
         double d3 = d1 - (double)this.topPos;

         for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
            if (this.checkTabClicked(creativemodetab, d2, d3)) {
               return true;
            }
         }

         if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(d0, d1)) {
            this.scrolling = this.canScroll();
            return true;
         }
      }

      return super.mouseClicked(d0, d1, i);
   }

   public boolean mouseReleased(double d0, double d1, int i) {
      if (i == 0) {
         double d2 = d0 - (double)this.leftPos;
         double d3 = d1 - (double)this.topPos;
         this.scrolling = false;

         for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
            if (this.checkTabClicked(creativemodetab, d2, d3)) {
               this.selectTab(creativemodetab);
               return true;
            }
         }
      }

      return super.mouseReleased(d0, d1, i);
   }

   private boolean canScroll() {
      return selectedTab.canScroll() && this.menu.canScroll();
   }

   private void selectTab(CreativeModeTab creativemodetab) {
      CreativeModeTab creativemodetab1 = selectedTab;
      selectedTab = creativemodetab;
      this.quickCraftSlots.clear();
      (this.menu).items.clear();
      this.clearDraggingState();
      if (selectedTab.getType() == CreativeModeTab.Type.HOTBAR) {
         HotbarManager hotbarmanager = this.minecraft.getHotbarManager();

         for(int i = 0; i < 9; ++i) {
            Hotbar hotbar = hotbarmanager.get(i);
            if (hotbar.isEmpty()) {
               for(int j = 0; j < 9; ++j) {
                  if (j == i) {
                     ItemStack itemstack = new ItemStack(Items.PAPER);
                     itemstack.getOrCreateTagElement("CustomCreativeLock");
                     Component component = this.minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
                     Component component1 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                     itemstack.setHoverName(Component.translatable("inventory.hotbarInfo", component1, component));
                     (this.menu).items.add(itemstack);
                  } else {
                     (this.menu).items.add(ItemStack.EMPTY);
                  }
               }
            } else {
               (this.menu).items.addAll(hotbar);
            }
         }
      } else if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
         (this.menu).items.addAll(selectedTab.getDisplayItems());
      }

      if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
         AbstractContainerMenu abstractcontainermenu = this.minecraft.player.inventoryMenu;
         if (this.originalSlots == null) {
            this.originalSlots = ImmutableList.copyOf((this.menu).slots);
         }

         (this.menu).slots.clear();

         for(int k = 0; k < abstractcontainermenu.slots.size(); ++k) {
            int k1;
            int l1;
            if (k >= 5 && k < 9) {
               int l = k - 5;
               int i1 = l / 2;
               int j1 = l % 2;
               k1 = 54 + i1 * 54;
               l1 = 6 + j1 * 27;
            } else if (k >= 0 && k < 5) {
               k1 = -2000;
               l1 = -2000;
            } else if (k == 45) {
               k1 = 35;
               l1 = 20;
            } else {
               int i3 = k - 9;
               int j3 = i3 % 9;
               int k3 = i3 / 9;
               k1 = 9 + j3 * 18;
               if (k >= 36) {
                  l1 = 112;
               } else {
                  l1 = 54 + k3 * 18;
               }
            }

            Slot slot = new CreativeModeInventoryScreen.SlotWrapper(abstractcontainermenu.slots.get(k), k, k1, l1);
            (this.menu).slots.add(slot);
         }

         this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
         (this.menu).slots.add(this.destroyItemSlot);
      } else if (creativemodetab1.getType() == CreativeModeTab.Type.INVENTORY) {
         (this.menu).slots.clear();
         (this.menu).slots.addAll(this.originalSlots);
         this.originalSlots = null;
      }

      if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
         this.searchBox.setVisible(true);
         this.searchBox.setCanLoseFocus(false);
         this.searchBox.setFocused(true);
         if (creativemodetab1 != creativemodetab) {
            this.searchBox.setValue("");
         }

         this.refreshSearchResults();
      } else {
         this.searchBox.setVisible(false);
         this.searchBox.setCanLoseFocus(true);
         this.searchBox.setFocused(false);
         this.searchBox.setValue("");
      }

      this.scrollOffs = 0.0F;
      this.menu.scrollTo(0.0F);
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      if (!this.canScroll()) {
         return false;
      } else {
         this.scrollOffs = this.menu.subtractInputFromScroll(this.scrollOffs, d2);
         this.menu.scrollTo(this.scrollOffs);
         return true;
      }
   }

   protected boolean hasClickedOutside(double d0, double d1, int i, int j, int k) {
      boolean flag = d0 < (double)i || d1 < (double)j || d0 >= (double)(i + this.imageWidth) || d1 >= (double)(j + this.imageHeight);
      this.hasClickedOutside = flag && !this.checkTabClicked(selectedTab, d0, d1);
      return this.hasClickedOutside;
   }

   protected boolean insideScrollbar(double d0, double d1) {
      int i = this.leftPos;
      int j = this.topPos;
      int k = i + 175;
      int l = j + 18;
      int i1 = k + 14;
      int j1 = l + 112;
      return d0 >= (double)k && d1 >= (double)l && d0 < (double)i1 && d1 < (double)j1;
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (this.scrolling) {
         int j = this.topPos + 18;
         int k = j + 112;
         this.scrollOffs = ((float)d1 - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
         this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
         this.menu.scrollTo(this.scrollOffs);
         return true;
      } else {
         return super.mouseDragged(d0, d1, i, d2, d3);
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      super.render(guigraphics, i, j, f);

      for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
         if (this.checkTabHovering(guigraphics, creativemodetab, i, j)) {
            break;
         }
      }

      if (this.destroyItemSlot != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, (double)i, (double)j)) {
         guigraphics.renderTooltip(this.font, TRASH_SLOT_TOOLTIP, i, j);
      }

      this.renderTooltip(guigraphics, i, j);
   }

   public List<Component> getTooltipFromContainerItem(ItemStack itemstack) {
      boolean flag = this.hoveredSlot != null && this.hoveredSlot instanceof CreativeModeInventoryScreen.CustomCreativeSlot;
      boolean flag1 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
      boolean flag2 = selectedTab.getType() == CreativeModeTab.Type.SEARCH;
      TooltipFlag.Default tooltipflag_default = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
      TooltipFlag tooltipflag = flag ? tooltipflag_default.asCreative() : tooltipflag_default;
      List<Component> list = itemstack.getTooltipLines(this.minecraft.player, tooltipflag);
      if (flag1 && flag) {
         return list;
      } else {
         List<Component> list1 = Lists.newArrayList(list);
         if (flag2 && flag) {
            this.visibleTags.forEach((tagkey) -> {
               if (itemstack.is(tagkey)) {
                  list1.add(1, Component.literal("#" + tagkey.location()).withStyle(ChatFormatting.DARK_PURPLE));
               }

            });
         }

         int i = 1;

         for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
            if (creativemodetab.getType() != CreativeModeTab.Type.SEARCH && creativemodetab.contains(itemstack)) {
               list1.add(i++, creativemodetab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
         }

         return list1;
      }
   }

   protected void renderBg(GuiGraphics guigraphics, float f, int i, int j) {
      for(CreativeModeTab creativemodetab : CreativeModeTabs.tabs()) {
         if (creativemodetab != selectedTab) {
            this.renderTabButton(guigraphics, creativemodetab);
         }
      }

      guigraphics.blit(new ResourceLocation("textures/gui/container/creative_inventory/tab_" + selectedTab.getBackgroundSuffix()), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
      this.searchBox.render(guigraphics, i, j, f);
      int k = this.leftPos + 175;
      int l = this.topPos + 18;
      int i1 = l + 112;
      if (selectedTab.canScroll()) {
         guigraphics.blit(CREATIVE_TABS_LOCATION, k, l + (int)((float)(i1 - l - 17) * this.scrollOffs), 232 + (this.canScroll() ? 0 : 12), 0, 12, 15);
      }

      this.renderTabButton(guigraphics, selectedTab);
      if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
         InventoryScreen.renderEntityInInventoryFollowsMouse(guigraphics, this.leftPos + 88, this.topPos + 45, 20, (float)(this.leftPos + 88 - i), (float)(this.topPos + 45 - 30 - j), this.minecraft.player);
      }

   }

   private int getTabX(CreativeModeTab creativemodetab) {
      int i = creativemodetab.column();
      int j = 27;
      int k = 27 * i;
      if (creativemodetab.isAlignedRight()) {
         k = this.imageWidth - 27 * (7 - i) + 1;
      }

      return k;
   }

   private int getTabY(CreativeModeTab creativemodetab) {
      int i = 0;
      if (creativemodetab.row() == CreativeModeTab.Row.TOP) {
         i -= 32;
      } else {
         i += this.imageHeight;
      }

      return i;
   }

   protected boolean checkTabClicked(CreativeModeTab creativemodetab, double d0, double d1) {
      int i = this.getTabX(creativemodetab);
      int j = this.getTabY(creativemodetab);
      return d0 >= (double)i && d0 <= (double)(i + 26) && d1 >= (double)j && d1 <= (double)(j + 32);
   }

   protected boolean checkTabHovering(GuiGraphics guigraphics, CreativeModeTab creativemodetab, int i, int j) {
      int k = this.getTabX(creativemodetab);
      int l = this.getTabY(creativemodetab);
      if (this.isHovering(k + 3, l + 3, 21, 27, (double)i, (double)j)) {
         guigraphics.renderTooltip(this.font, creativemodetab.getDisplayName(), i, j);
         return true;
      } else {
         return false;
      }
   }

   protected void renderTabButton(GuiGraphics guigraphics, CreativeModeTab creativemodetab) {
      boolean flag = creativemodetab == selectedTab;
      boolean flag1 = creativemodetab.row() == CreativeModeTab.Row.TOP;
      int i = creativemodetab.column();
      int j = i * 26;
      int k = 0;
      int l = this.leftPos + this.getTabX(creativemodetab);
      int i1 = this.topPos;
      int j1 = 32;
      if (flag) {
         k += 32;
      }

      if (flag1) {
         i1 -= 28;
      } else {
         k += 64;
         i1 += this.imageHeight - 4;
      }

      guigraphics.blit(CREATIVE_TABS_LOCATION, l, i1, j, k, 26, 32);
      guigraphics.pose().pushPose();
      guigraphics.pose().translate(0.0F, 0.0F, 100.0F);
      l += 5;
      i1 += 8 + (flag1 ? 1 : -1);
      ItemStack itemstack = creativemodetab.getIconItem();
      guigraphics.renderItem(itemstack, l, i1);
      guigraphics.renderItemDecorations(this.font, itemstack, l, i1);
      guigraphics.pose().popPose();
   }

   public boolean isInventoryOpen() {
      return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
   }

   public static void handleHotbarLoadOrSave(Minecraft minecraft, int i, boolean flag, boolean flag1) {
      LocalPlayer localplayer = minecraft.player;
      HotbarManager hotbarmanager = minecraft.getHotbarManager();
      Hotbar hotbar = hotbarmanager.get(i);
      if (flag) {
         for(int j = 0; j < Inventory.getSelectionSize(); ++j) {
            ItemStack itemstack = hotbar.get(j);
            ItemStack itemstack1 = itemstack.isItemEnabled(localplayer.level().enabledFeatures()) ? itemstack.copy() : ItemStack.EMPTY;
            localplayer.getInventory().setItem(j, itemstack1);
            minecraft.gameMode.handleCreativeModeItemAdd(itemstack1, 36 + j);
         }

         localplayer.inventoryMenu.broadcastChanges();
      } else if (flag1) {
         for(int k = 0; k < Inventory.getSelectionSize(); ++k) {
            hotbar.set(k, localplayer.getInventory().getItem(k).copy());
         }

         Component component = minecraft.options.keyHotbarSlots[i].getTranslatedKeyMessage();
         Component component1 = minecraft.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
         Component component2 = Component.translatable("inventory.hotbarSaved", component1, component);
         minecraft.gui.setOverlayMessage(component2, false);
         minecraft.getNarrator().sayNow(component2);
         hotbarmanager.save();
      }

   }

   static class CustomCreativeSlot extends Slot {
      public CustomCreativeSlot(Container container, int i, int j, int k) {
         super(container, i, j, k);
      }

      public boolean mayPickup(Player player) {
         ItemStack itemstack = this.getItem();
         if (super.mayPickup(player) && !itemstack.isEmpty()) {
            return itemstack.isItemEnabled(player.level().enabledFeatures()) && itemstack.getTagElement("CustomCreativeLock") == null;
         } else {
            return itemstack.isEmpty();
         }
      }
   }

   public static class ItemPickerMenu extends AbstractContainerMenu {
      public final NonNullList<ItemStack> items = NonNullList.create();
      private final AbstractContainerMenu inventoryMenu;

      public ItemPickerMenu(Player player) {
         super((MenuType<?>)null, 0);
         this.inventoryMenu = player.inventoryMenu;
         Inventory inventory = player.getInventory();

         for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 9; ++j) {
               this.addSlot(new CreativeModeInventoryScreen.CustomCreativeSlot(CreativeModeInventoryScreen.CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
            }
         }

         for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 9 + k * 18, 112));
         }

         this.scrollTo(0.0F);
      }

      public boolean stillValid(Player player) {
         return true;
      }

      protected int calculateRowCount() {
         return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
      }

      protected int getRowIndexForScroll(float f) {
         return Math.max((int)((double)(f * (float)this.calculateRowCount()) + 0.5D), 0);
      }

      protected float getScrollForRowIndex(int i) {
         return Mth.clamp((float)i / (float)this.calculateRowCount(), 0.0F, 1.0F);
      }

      protected float subtractInputFromScroll(float f, double d0) {
         return Mth.clamp(f - (float)(d0 / (double)this.calculateRowCount()), 0.0F, 1.0F);
      }

      public void scrollTo(float f) {
         int i = this.getRowIndexForScroll(f);

         for(int j = 0; j < 5; ++j) {
            for(int k = 0; k < 9; ++k) {
               int l = k + (j + i) * 9;
               if (l >= 0 && l < this.items.size()) {
                  CreativeModeInventoryScreen.CONTAINER.setItem(k + j * 9, this.items.get(l));
               } else {
                  CreativeModeInventoryScreen.CONTAINER.setItem(k + j * 9, ItemStack.EMPTY);
               }
            }
         }

      }

      public boolean canScroll() {
         return this.items.size() > 45;
      }

      public ItemStack quickMoveStack(Player player, int i) {
         if (i >= this.slots.size() - 9 && i < this.slots.size()) {
            Slot slot = this.slots.get(i);
            if (slot != null && slot.hasItem()) {
               slot.setByPlayer(ItemStack.EMPTY);
            }
         }

         return ItemStack.EMPTY;
      }

      public boolean canTakeItemForPickAll(ItemStack itemstack, Slot slot) {
         return slot.container != CreativeModeInventoryScreen.CONTAINER;
      }

      public boolean canDragTo(Slot slot) {
         return slot.container != CreativeModeInventoryScreen.CONTAINER;
      }

      public ItemStack getCarried() {
         return this.inventoryMenu.getCarried();
      }

      public void setCarried(ItemStack itemstack) {
         this.inventoryMenu.setCarried(itemstack);
      }
   }

   static class SlotWrapper extends Slot {
      final Slot target;

      public SlotWrapper(Slot slot, int i, int j, int k) {
         super(slot.container, i, j, k);
         this.target = slot;
      }

      public void onTake(Player player, ItemStack itemstack) {
         this.target.onTake(player, itemstack);
      }

      public boolean mayPlace(ItemStack itemstack) {
         return this.target.mayPlace(itemstack);
      }

      public ItemStack getItem() {
         return this.target.getItem();
      }

      public boolean hasItem() {
         return this.target.hasItem();
      }

      public void setByPlayer(ItemStack itemstack) {
         this.target.setByPlayer(itemstack);
      }

      public void set(ItemStack itemstack) {
         this.target.set(itemstack);
      }

      public void setChanged() {
         this.target.setChanged();
      }

      public int getMaxStackSize() {
         return this.target.getMaxStackSize();
      }

      public int getMaxStackSize(ItemStack itemstack) {
         return this.target.getMaxStackSize(itemstack);
      }

      @Nullable
      public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
         return this.target.getNoItemIcon();
      }

      public ItemStack remove(int i) {
         return this.target.remove(i);
      }

      public boolean isActive() {
         return this.target.isActive();
      }

      public boolean mayPickup(Player player) {
         return this.target.mayPickup(player);
      }
   }
}
