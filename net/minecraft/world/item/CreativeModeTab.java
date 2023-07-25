package net.minecraft.world.item;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ItemLike;

public class CreativeModeTab {
   private final Component displayName;
   String backgroundSuffix = "items.png";
   boolean canScroll = true;
   boolean showTitle = true;
   boolean alignedRight = false;
   private final CreativeModeTab.Row row;
   private final int column;
   private final CreativeModeTab.Type type;
   @Nullable
   private ItemStack iconItemStack;
   private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndTagSet();
   private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndTagSet();
   @Nullable
   private Consumer<List<ItemStack>> searchTreeBuilder;
   private final Supplier<ItemStack> iconGenerator;
   private final CreativeModeTab.DisplayItemsGenerator displayItemsGenerator;

   CreativeModeTab(CreativeModeTab.Row creativemodetab_row, int i, CreativeModeTab.Type creativemodetab_type, Component component, Supplier<ItemStack> supplier, CreativeModeTab.DisplayItemsGenerator creativemodetab_displayitemsgenerator) {
      this.row = creativemodetab_row;
      this.column = i;
      this.displayName = component;
      this.iconGenerator = supplier;
      this.displayItemsGenerator = creativemodetab_displayitemsgenerator;
      this.type = creativemodetab_type;
   }

   public static CreativeModeTab.Builder builder(CreativeModeTab.Row creativemodetab_row, int i) {
      return new CreativeModeTab.Builder(creativemodetab_row, i);
   }

   public Component getDisplayName() {
      return this.displayName;
   }

   public ItemStack getIconItem() {
      if (this.iconItemStack == null) {
         this.iconItemStack = this.iconGenerator.get();
      }

      return this.iconItemStack;
   }

   public String getBackgroundSuffix() {
      return this.backgroundSuffix;
   }

   public boolean showTitle() {
      return this.showTitle;
   }

   public boolean canScroll() {
      return this.canScroll;
   }

   public int column() {
      return this.column;
   }

   public CreativeModeTab.Row row() {
      return this.row;
   }

   public boolean hasAnyItems() {
      return !this.displayItems.isEmpty();
   }

   public boolean shouldDisplay() {
      return this.type != CreativeModeTab.Type.CATEGORY || this.hasAnyItems();
   }

   public boolean isAlignedRight() {
      return this.alignedRight;
   }

   public CreativeModeTab.Type getType() {
      return this.type;
   }

   public void buildContents(CreativeModeTab.ItemDisplayParameters creativemodetab_itemdisplayparameters) {
      CreativeModeTab.ItemDisplayBuilder creativemodetab_itemdisplaybuilder = new CreativeModeTab.ItemDisplayBuilder(this, creativemodetab_itemdisplayparameters.enabledFeatures);
      ResourceKey var10000 = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + this));
      this.displayItemsGenerator.accept(creativemodetab_itemdisplayparameters, creativemodetab_itemdisplaybuilder);
      this.displayItems = creativemodetab_itemdisplaybuilder.tabContents;
      this.displayItemsSearchTab = creativemodetab_itemdisplaybuilder.searchTabContents;
      this.rebuildSearchTree();
   }

   public Collection<ItemStack> getDisplayItems() {
      return this.displayItems;
   }

   public Collection<ItemStack> getSearchTabDisplayItems() {
      return this.displayItemsSearchTab;
   }

   public boolean contains(ItemStack itemstack) {
      return this.displayItemsSearchTab.contains(itemstack);
   }

   public void setSearchTreeBuilder(Consumer<List<ItemStack>> consumer) {
      this.searchTreeBuilder = consumer;
   }

   public void rebuildSearchTree() {
      if (this.searchTreeBuilder != null) {
         this.searchTreeBuilder.accept(Lists.newArrayList(this.displayItemsSearchTab));
      }

   }

   public static class Builder {
      private static final CreativeModeTab.DisplayItemsGenerator EMPTY_GENERATOR = (creativemodetab_itemdisplayparameters, creativemodetab_output) -> {
      };
      private final CreativeModeTab.Row row;
      private final int column;
      private Component displayName = Component.empty();
      private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
      private CreativeModeTab.DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
      private boolean canScroll = true;
      private boolean showTitle = true;
      private boolean alignedRight = false;
      private CreativeModeTab.Type type = CreativeModeTab.Type.CATEGORY;
      private String backgroundSuffix = "items.png";

      public Builder(CreativeModeTab.Row creativemodetab_row, int i) {
         this.row = creativemodetab_row;
         this.column = i;
      }

      public CreativeModeTab.Builder title(Component component) {
         this.displayName = component;
         return this;
      }

      public CreativeModeTab.Builder icon(Supplier<ItemStack> supplier) {
         this.iconGenerator = supplier;
         return this;
      }

      public CreativeModeTab.Builder displayItems(CreativeModeTab.DisplayItemsGenerator creativemodetab_displayitemsgenerator) {
         this.displayItemsGenerator = creativemodetab_displayitemsgenerator;
         return this;
      }

      public CreativeModeTab.Builder alignedRight() {
         this.alignedRight = true;
         return this;
      }

      public CreativeModeTab.Builder hideTitle() {
         this.showTitle = false;
         return this;
      }

      public CreativeModeTab.Builder noScrollBar() {
         this.canScroll = false;
         return this;
      }

      protected CreativeModeTab.Builder type(CreativeModeTab.Type creativemodetab_type) {
         this.type = creativemodetab_type;
         return this;
      }

      public CreativeModeTab.Builder backgroundSuffix(String s) {
         this.backgroundSuffix = s;
         return this;
      }

      public CreativeModeTab build() {
         if ((this.type == CreativeModeTab.Type.HOTBAR || this.type == CreativeModeTab.Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
            throw new IllegalStateException("Special tabs can't have display items");
         } else {
            CreativeModeTab creativemodetab = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
            creativemodetab.alignedRight = this.alignedRight;
            creativemodetab.showTitle = this.showTitle;
            creativemodetab.canScroll = this.canScroll;
            creativemodetab.backgroundSuffix = this.backgroundSuffix;
            return creativemodetab;
         }
      }
   }

   @FunctionalInterface
   public interface DisplayItemsGenerator {
      void accept(CreativeModeTab.ItemDisplayParameters creativemodetab_itemdisplayparameters, CreativeModeTab.Output creativemodetab_output);
   }

   static class ItemDisplayBuilder implements CreativeModeTab.Output {
      public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndTagSet();
      public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndTagSet();
      private final CreativeModeTab tab;
      private final FeatureFlagSet featureFlagSet;

      public ItemDisplayBuilder(CreativeModeTab creativemodetab, FeatureFlagSet featureflagset) {
         this.tab = creativemodetab;
         this.featureFlagSet = featureflagset;
      }

      public void accept(ItemStack itemstack, CreativeModeTab.TabVisibility creativemodetab_tabvisibility) {
         if (itemstack.getCount() != 1) {
            throw new IllegalArgumentException("Stack size must be exactly 1");
         } else {
            boolean flag = this.tabContents.contains(itemstack) && creativemodetab_tabvisibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
            if (flag) {
               throw new IllegalStateException("Accidentally adding the same item stack twice " + itemstack.getDisplayName().getString() + " to a Creative Mode Tab: " + this.tab.getDisplayName().getString());
            } else {
               if (itemstack.getItem().isEnabled(this.featureFlagSet)) {
                  switch (creativemodetab_tabvisibility) {
                     case PARENT_AND_SEARCH_TABS:
                        this.tabContents.add(itemstack);
                        this.searchTabContents.add(itemstack);
                        break;
                     case PARENT_TAB_ONLY:
                        this.tabContents.add(itemstack);
                        break;
                     case SEARCH_TAB_ONLY:
                        this.searchTabContents.add(itemstack);
                  }
               }

            }
         }
      }
   }

   public static record ItemDisplayParameters(FeatureFlagSet enabledFeatures, boolean hasPermissions, HolderLookup.Provider holders) {
      final FeatureFlagSet enabledFeatures;

      public boolean needsUpdate(FeatureFlagSet featureflagset, boolean flag, HolderLookup.Provider holderlookup_provider) {
         return !this.enabledFeatures.equals(featureflagset) || this.hasPermissions != flag || this.holders != holderlookup_provider;
      }
   }

   public interface Output {
      void accept(ItemStack itemstack, CreativeModeTab.TabVisibility creativemodetab_tabvisibility);

      default void accept(ItemStack itemstack) {
         this.accept(itemstack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
      }

      default void accept(ItemLike itemlike, CreativeModeTab.TabVisibility creativemodetab_tabvisibility) {
         this.accept(new ItemStack(itemlike), creativemodetab_tabvisibility);
      }

      default void accept(ItemLike itemlike) {
         this.accept(new ItemStack(itemlike), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
      }

      default void acceptAll(Collection<ItemStack> collection, CreativeModeTab.TabVisibility creativemodetab_tabvisibility) {
         collection.forEach((itemstack) -> this.accept(itemstack, creativemodetab_tabvisibility));
      }

      default void acceptAll(Collection<ItemStack> collection) {
         this.acceptAll(collection, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
      }
   }

   public static enum Row {
      TOP,
      BOTTOM;
   }

   protected static enum TabVisibility {
      PARENT_AND_SEARCH_TABS,
      PARENT_TAB_ONLY,
      SEARCH_TAB_ONLY;
   }

   public static enum Type {
      CATEGORY,
      INVENTORY,
      HOTBAR,
      SEARCH;
   }
}
