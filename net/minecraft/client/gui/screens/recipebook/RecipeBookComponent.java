package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.recipebook.PlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeBookComponent implements PlaceRecipe<Ingredient>, Renderable, GuiEventListener, NarratableEntry, RecipeShownListener {
   protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
   private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
   public static final int IMAGE_WIDTH = 147;
   public static final int IMAGE_HEIGHT = 166;
   private static final int OFFSET_X_POSITION = 86;
   private static final Component ONLY_CRAFTABLES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.craftable");
   private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
   private int xOffset;
   private int width;
   private int height;
   protected final GhostRecipe ghostRecipe = new GhostRecipe();
   private final List<RecipeBookTabButton> tabButtons = Lists.newArrayList();
   @Nullable
   private RecipeBookTabButton selectedTab;
   protected StateSwitchingButton filterButton;
   protected RecipeBookMenu<?> menu;
   protected Minecraft minecraft;
   @Nullable
   private EditBox searchBox;
   private String lastSearch = "";
   private ClientRecipeBook book;
   private final RecipeBookPage recipeBookPage = new RecipeBookPage();
   private final StackedContents stackedContents = new StackedContents();
   private int timesInventoryChanged;
   private boolean ignoreTextInput;
   private boolean visible;
   private boolean widthTooNarrow;

   public void init(int i, int j, Minecraft minecraft, boolean flag, RecipeBookMenu<?> recipebookmenu) {
      this.minecraft = minecraft;
      this.width = i;
      this.height = j;
      this.menu = recipebookmenu;
      this.widthTooNarrow = flag;
      minecraft.player.containerMenu = recipebookmenu;
      this.book = minecraft.player.getRecipeBook();
      this.timesInventoryChanged = minecraft.player.getInventory().getTimesChanged();
      this.visible = this.isVisibleAccordingToBookData();
      if (this.visible) {
         this.initVisuals();
      }

   }

   public void initVisuals() {
      this.xOffset = this.widthTooNarrow ? 0 : 86;
      int i = (this.width - 147) / 2 - this.xOffset;
      int j = (this.height - 166) / 2;
      this.stackedContents.clear();
      this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
      this.menu.fillCraftSlotsStackedContents(this.stackedContents);
      String s = this.searchBox != null ? this.searchBox.getValue() : "";
      this.searchBox = new EditBox(this.minecraft.font, i + 26, j + 14, 79, 9 + 3, Component.translatable("itemGroup.search"));
      this.searchBox.setMaxLength(50);
      this.searchBox.setVisible(true);
      this.searchBox.setTextColor(16777215);
      this.searchBox.setValue(s);
      this.searchBox.setHint(SEARCH_HINT);
      this.recipeBookPage.init(this.minecraft, i, j);
      this.recipeBookPage.addListener(this);
      this.filterButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, this.book.isFiltering(this.menu));
      this.updateFilterButtonTooltip();
      this.initFilterButtonTextures();
      this.tabButtons.clear();

      for(RecipeBookCategories recipebookcategories : RecipeBookCategories.getCategories(this.menu.getRecipeBookType())) {
         this.tabButtons.add(new RecipeBookTabButton(recipebookcategories));
      }

      if (this.selectedTab != null) {
         this.selectedTab = this.tabButtons.stream().filter((recipebooktabbutton) -> recipebooktabbutton.getCategory().equals(this.selectedTab.getCategory())).findFirst().orElse((RecipeBookTabButton)null);
      }

      if (this.selectedTab == null) {
         this.selectedTab = this.tabButtons.get(0);
      }

      this.selectedTab.setStateTriggered(true);
      this.updateCollections(false);
      this.updateTabs();
   }

   private void updateFilterButtonTooltip() {
      this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
   }

   protected void initFilterButtonTextures() {
      this.filterButton.initTextureValues(152, 41, 28, 18, RECIPE_BOOK_LOCATION);
   }

   public int updateScreenPosition(int i, int j) {
      int k;
      if (this.isVisible() && !this.widthTooNarrow) {
         k = 177 + (i - j - 200) / 2;
      } else {
         k = (i - j) / 2;
      }

      return k;
   }

   public void toggleVisibility() {
      this.setVisible(!this.isVisible());
   }

   public boolean isVisible() {
      return this.visible;
   }

   private boolean isVisibleAccordingToBookData() {
      return this.book.isOpen(this.menu.getRecipeBookType());
   }

   protected void setVisible(boolean flag) {
      if (flag) {
         this.initVisuals();
      }

      this.visible = flag;
      this.book.setOpen(this.menu.getRecipeBookType(), flag);
      if (!flag) {
         this.recipeBookPage.setInvisible();
      }

      this.sendUpdateSettings();
   }

   public void slotClicked(@Nullable Slot slot) {
      if (slot != null && slot.index < this.menu.getSize()) {
         this.ghostRecipe.clear();
         if (this.isVisible()) {
            this.updateStackedContents();
         }
      }

   }

   private void updateCollections(boolean flag) {
      List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
      list.forEach((recipecollection4) -> recipecollection4.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book));
      List<RecipeCollection> list1 = Lists.newArrayList(list);
      list1.removeIf((recipecollection3) -> !recipecollection3.hasKnownRecipes());
      list1.removeIf((recipecollection2) -> !recipecollection2.hasFitting());
      String s = this.searchBox.getValue();
      if (!s.isEmpty()) {
         ObjectSet<RecipeCollection> objectset = new ObjectLinkedOpenHashSet<>(this.minecraft.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
         list1.removeIf((recipecollection1) -> !objectset.contains(recipecollection1));
      }

      if (this.book.isFiltering(this.menu)) {
         list1.removeIf((recipecollection) -> !recipecollection.hasCraftable());
      }

      this.recipeBookPage.updateCollections(list1, flag);
   }

   private void updateTabs() {
      int i = (this.width - 147) / 2 - this.xOffset - 30;
      int j = (this.height - 166) / 2 + 3;
      int k = 27;
      int l = 0;

      for(RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
         RecipeBookCategories recipebookcategories = recipebooktabbutton.getCategory();
         if (recipebookcategories != RecipeBookCategories.CRAFTING_SEARCH && recipebookcategories != RecipeBookCategories.FURNACE_SEARCH) {
            if (recipebooktabbutton.updateVisibility(this.book)) {
               recipebooktabbutton.setPosition(i, j + 27 * l++);
               recipebooktabbutton.startAnimation(this.minecraft);
            }
         } else {
            recipebooktabbutton.visible = true;
            recipebooktabbutton.setPosition(i, j + 27 * l++);
         }
      }

   }

   public void tick() {
      boolean flag = this.isVisibleAccordingToBookData();
      if (this.isVisible() != flag) {
         this.setVisible(flag);
      }

      if (this.isVisible()) {
         if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
            this.updateStackedContents();
            this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
         }

         this.searchBox.tick();
      }
   }

   private void updateStackedContents() {
      this.stackedContents.clear();
      this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
      this.menu.fillCraftSlotsStackedContents(this.stackedContents);
      this.updateCollections(false);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.isVisible()) {
         guigraphics.pose().pushPose();
         guigraphics.pose().translate(0.0F, 0.0F, 100.0F);
         int k = (this.width - 147) / 2 - this.xOffset;
         int l = (this.height - 166) / 2;
         guigraphics.blit(RECIPE_BOOK_LOCATION, k, l, 1, 1, 147, 166);
         this.searchBox.render(guigraphics, i, j, f);

         for(RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
            recipebooktabbutton.render(guigraphics, i, j, f);
         }

         this.filterButton.render(guigraphics, i, j, f);
         this.recipeBookPage.render(guigraphics, k, l, i, j, f);
         guigraphics.pose().popPose();
      }
   }

   public void renderTooltip(GuiGraphics guigraphics, int i, int j, int k, int l) {
      if (this.isVisible()) {
         this.recipeBookPage.renderTooltip(guigraphics, k, l);
         this.renderGhostRecipeTooltip(guigraphics, i, j, k, l);
      }
   }

   protected Component getRecipeFilterName() {
      return ONLY_CRAFTABLES_TOOLTIP;
   }

   private void renderGhostRecipeTooltip(GuiGraphics guigraphics, int i, int j, int k, int l) {
      ItemStack itemstack = null;

      for(int i1 = 0; i1 < this.ghostRecipe.size(); ++i1) {
         GhostRecipe.GhostIngredient ghostrecipe_ghostingredient = this.ghostRecipe.get(i1);
         int j1 = ghostrecipe_ghostingredient.getX() + i;
         int k1 = ghostrecipe_ghostingredient.getY() + j;
         if (k >= j1 && l >= k1 && k < j1 + 16 && l < k1 + 16) {
            itemstack = ghostrecipe_ghostingredient.getItem();
         }
      }

      if (itemstack != null && this.minecraft.screen != null) {
         guigraphics.renderComponentTooltip(this.minecraft.font, Screen.getTooltipFromItem(this.minecraft, itemstack), k, l);
      }

   }

   public void renderGhostRecipe(GuiGraphics guigraphics, int i, int j, boolean flag, float f) {
      this.ghostRecipe.render(guigraphics, this.minecraft, i, j, flag, f);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (this.recipeBookPage.mouseClicked(d0, d1, i, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
            Recipe<?> recipe = this.recipeBookPage.getLastClickedRecipe();
            RecipeCollection recipecollection = this.recipeBookPage.getLastClickedRecipeCollection();
            if (recipe != null && recipecollection != null) {
               if (!recipecollection.isCraftable(recipe) && this.ghostRecipe.getRecipe() == recipe) {
                  return false;
               }

               this.ghostRecipe.clear();
               this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, recipe, Screen.hasShiftDown());
               if (!this.isOffsetNextToMainGUI()) {
                  this.setVisible(false);
               }
            }

            return true;
         } else if (this.searchBox.mouseClicked(d0, d1, i)) {
            this.searchBox.setFocused(true);
            return true;
         } else {
            this.searchBox.setFocused(false);
            if (this.filterButton.mouseClicked(d0, d1, i)) {
               boolean flag = this.toggleFiltering();
               this.filterButton.setStateTriggered(flag);
               this.updateFilterButtonTooltip();
               this.sendUpdateSettings();
               this.updateCollections(false);
               return true;
            } else {
               for(RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                  if (recipebooktabbutton.mouseClicked(d0, d1, i)) {
                     if (this.selectedTab != recipebooktabbutton) {
                        if (this.selectedTab != null) {
                           this.selectedTab.setStateTriggered(false);
                        }

                        this.selectedTab = recipebooktabbutton;
                        this.selectedTab.setStateTriggered(true);
                        this.updateCollections(true);
                     }

                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   private boolean toggleFiltering() {
      RecipeBookType recipebooktype = this.menu.getRecipeBookType();
      boolean flag = !this.book.isFiltering(recipebooktype);
      this.book.setFiltering(recipebooktype, flag);
      return flag;
   }

   public boolean hasClickedOutside(double d0, double d1, int i, int j, int k, int l, int i1) {
      if (!this.isVisible()) {
         return true;
      } else {
         boolean flag = d0 < (double)i || d1 < (double)j || d0 >= (double)(i + k) || d1 >= (double)(j + l);
         boolean flag1 = (double)(i - 147) < d0 && d0 < (double)i && (double)j < d1 && d1 < (double)(j + l);
         return flag && !flag1 && !this.selectedTab.isHoveredOrFocused();
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      this.ignoreTextInput = false;
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (i == 256 && !this.isOffsetNextToMainGUI()) {
            this.setVisible(false);
            return true;
         } else if (this.searchBox.keyPressed(i, j, k)) {
            this.checkSearchStringUpdate();
            return true;
         } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && i != 256) {
            return true;
         } else if (this.minecraft.options.keyChat.matches(i, j) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocused(true);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean keyReleased(int i, int j, int k) {
      this.ignoreTextInput = false;
      return GuiEventListener.super.keyReleased(i, j, k);
   }

   public boolean charTyped(char c0, int i) {
      if (this.ignoreTextInput) {
         return false;
      } else if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (this.searchBox.charTyped(c0, i)) {
            this.checkSearchStringUpdate();
            return true;
         } else {
            return GuiEventListener.super.charTyped(c0, i);
         }
      } else {
         return false;
      }
   }

   public boolean isMouseOver(double d0, double d1) {
      return false;
   }

   public void setFocused(boolean flag) {
   }

   public boolean isFocused() {
      return false;
   }

   private void checkSearchStringUpdate() {
      String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
      this.pirateSpeechForThePeople(s);
      if (!s.equals(this.lastSearch)) {
         this.updateCollections(false);
         this.lastSearch = s;
      }

   }

   private void pirateSpeechForThePeople(String s) {
      if ("excitedze".equals(s)) {
         LanguageManager languagemanager = this.minecraft.getLanguageManager();
         String s1 = "en_pt";
         LanguageInfo languageinfo = languagemanager.getLanguage("en_pt");
         if (languageinfo == null || languagemanager.getSelected().equals("en_pt")) {
            return;
         }

         languagemanager.setSelected("en_pt");
         this.minecraft.options.languageCode = "en_pt";
         this.minecraft.reloadResourcePacks();
         this.minecraft.options.save();
      }

   }

   private boolean isOffsetNextToMainGUI() {
      return this.xOffset == 86;
   }

   public void recipesUpdated() {
      this.updateTabs();
      if (this.isVisible()) {
         this.updateCollections(false);
      }

   }

   public void recipesShown(List<Recipe<?>> list) {
      for(Recipe<?> recipe : list) {
         this.minecraft.player.removeRecipeHighlight(recipe);
      }

   }

   public void setupGhostRecipe(Recipe<?> recipe, List<Slot> list) {
      ItemStack itemstack = recipe.getResultItem(this.minecraft.level.registryAccess());
      this.ghostRecipe.setRecipe(recipe);
      this.ghostRecipe.addIngredient(Ingredient.of(itemstack), (list.get(0)).x, (list.get(0)).y);
      this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
   }

   public void addItemToSlot(Iterator<Ingredient> iterator, int i, int j, int k, int l) {
      Ingredient ingredient = iterator.next();
      if (!ingredient.isEmpty()) {
         Slot slot = this.menu.slots.get(i);
         this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
      }

   }

   protected void sendUpdateSettings() {
      if (this.minecraft.getConnection() != null) {
         RecipeBookType recipebooktype = this.menu.getRecipeBookType();
         boolean flag = this.book.getBookSettings().isOpen(recipebooktype);
         boolean flag1 = this.book.getBookSettings().isFiltering(recipebooktype);
         this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(recipebooktype, flag, flag1));
      }

   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      return this.visible ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
   }

   public void updateNarration(NarrationElementOutput narrationelementoutput) {
      List<NarratableEntry> list = Lists.newArrayList();
      this.recipeBookPage.listButtons((abstractwidget) -> {
         if (abstractwidget.isActive()) {
            list.add(abstractwidget);
         }

      });
      list.add(this.searchBox);
      list.add(this.filterButton);
      list.addAll(this.tabButtons);
      Screen.NarratableSearchResult screen_narratablesearchresult = Screen.findNarratableWidget(list, (NarratableEntry)null);
      if (screen_narratablesearchresult != null) {
         screen_narratablesearchresult.entry.updateNarration(narrationelementoutput.nest());
      }

   }
}
