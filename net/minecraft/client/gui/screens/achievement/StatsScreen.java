package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class StatsScreen extends Screen implements StatsUpdateListener {
   private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
   private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
   protected final Screen lastScreen;
   private StatsScreen.GeneralStatisticsList statsList;
   StatsScreen.ItemStatisticsList itemStatsList;
   private StatsScreen.MobsStatisticsList mobsStatsList;
   final StatsCounter stats;
   @Nullable
   private ObjectSelectionList<?> activeList;
   private boolean isLoading = true;
   private static final int SLOT_TEX_SIZE = 128;
   private static final int SLOT_BG_SIZE = 18;
   private static final int SLOT_STAT_HEIGHT = 20;
   private static final int SLOT_BG_X = 1;
   private static final int SLOT_BG_Y = 1;
   private static final int SLOT_FG_X = 2;
   private static final int SLOT_FG_Y = 2;
   private static final int SLOT_LEFT_INSERT = 40;
   private static final int SLOT_TEXT_OFFSET = 5;
   private static final int SORT_NONE = 0;
   private static final int SORT_DOWN = -1;
   private static final int SORT_UP = 1;

   public StatsScreen(Screen screen, StatsCounter statscounter) {
      super(Component.translatable("gui.stats"));
      this.lastScreen = screen;
      this.stats = statscounter;
   }

   protected void init() {
      this.isLoading = true;
      this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
   }

   public void initLists() {
      this.statsList = new StatsScreen.GeneralStatisticsList(this.minecraft);
      this.itemStatsList = new StatsScreen.ItemStatisticsList(this.minecraft);
      this.mobsStatsList = new StatsScreen.MobsStatisticsList(this.minecraft);
   }

   public void initButtons() {
      this.addRenderableWidget(Button.builder(Component.translatable("stat.generalButton"), (button5) -> this.setActiveList(this.statsList)).bounds(this.width / 2 - 120, this.height - 52, 80, 20).build());
      Button button = this.addRenderableWidget(Button.builder(Component.translatable("stat.itemsButton"), (button4) -> this.setActiveList(this.itemStatsList)).bounds(this.width / 2 - 40, this.height - 52, 80, 20).build());
      Button button1 = this.addRenderableWidget(Button.builder(Component.translatable("stat.mobsButton"), (button3) -> this.setActiveList(this.mobsStatsList)).bounds(this.width / 2 + 40, this.height - 52, 80, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button2) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
      if (this.itemStatsList.children().isEmpty()) {
         button.active = false;
      }

      if (this.mobsStatsList.children().isEmpty()) {
         button1.active = false;
      }

   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.isLoading) {
         this.renderBackground(guigraphics);
         guigraphics.drawCenteredString(this.font, PENDING_TEXT, this.width / 2, this.height / 2, 16777215);
         guigraphics.drawCenteredString(this.font, LOADING_SYMBOLS[(int)(Util.getMillis() / 150L % (long)LOADING_SYMBOLS.length)], this.width / 2, this.height / 2 + 9 * 2, 16777215);
      } else {
         this.getActiveList().render(guigraphics, i, j, f);
         guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
         super.render(guigraphics, i, j, f);
      }

   }

   public void onStatsUpdated() {
      if (this.isLoading) {
         this.initLists();
         this.initButtons();
         this.setActiveList(this.statsList);
         this.isLoading = false;
      }

   }

   public boolean isPauseScreen() {
      return !this.isLoading;
   }

   @Nullable
   public ObjectSelectionList<?> getActiveList() {
      return this.activeList;
   }

   public void setActiveList(@Nullable ObjectSelectionList<?> objectselectionlist) {
      if (this.activeList != null) {
         this.removeWidget(this.activeList);
      }

      if (objectselectionlist != null) {
         this.addWidget(objectselectionlist);
         this.activeList = objectselectionlist;
      }

   }

   static String getTranslationKey(Stat<ResourceLocation> stat) {
      return "stat." + stat.getValue().toString().replace(':', '.');
   }

   int getColumnX(int i) {
      return 115 + 40 * i;
   }

   void blitSlot(GuiGraphics guigraphics, int i, int j, Item item) {
      this.blitSlotIcon(guigraphics, i + 1, j + 1, 0, 0);
      guigraphics.renderFakeItem(item.getDefaultInstance(), i + 2, j + 2);
   }

   void blitSlotIcon(GuiGraphics guigraphics, int i, int j, int k, int l) {
      guigraphics.blit(STATS_ICON_LOCATION, i, j, 0, (float)k, (float)l, 18, 18, 128, 128);
   }

   class GeneralStatisticsList extends ObjectSelectionList<StatsScreen.GeneralStatisticsList.Entry> {
      public GeneralStatisticsList(Minecraft minecraft) {
         super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
         ObjectArrayList<Stat<ResourceLocation>> objectarraylist = new ObjectArrayList<>(Stats.CUSTOM.iterator());
         objectarraylist.sort(Comparator.comparing((stat1) -> I18n.get(StatsScreen.getTranslationKey(stat1))));

         for(Stat<ResourceLocation> stat : objectarraylist) {
            this.addEntry(new StatsScreen.GeneralStatisticsList.Entry(stat));
         }

      }

      protected void renderBackground(GuiGraphics guigraphics) {
         StatsScreen.this.renderBackground(guigraphics);
      }

      class Entry extends ObjectSelectionList.Entry<StatsScreen.GeneralStatisticsList.Entry> {
         private final Stat<ResourceLocation> stat;
         private final Component statDisplay;

         Entry(Stat<ResourceLocation> stat) {
            this.stat = stat;
            this.statDisplay = Component.translatable(StatsScreen.getTranslationKey(stat));
         }

         private String getValueText() {
            return this.stat.format(StatsScreen.this.stats.getValue(this.stat));
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            guigraphics.drawString(StatsScreen.this.font, this.statDisplay, k + 2, j + 1, i % 2 == 0 ? 16777215 : 9474192);
            String s = this.getValueText();
            guigraphics.drawString(StatsScreen.this.font, s, k + 2 + 213 - StatsScreen.this.font.width(s), j + 1, i % 2 == 0 ? 16777215 : 9474192);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText()));
         }
      }
   }

   class ItemStatisticsList extends ObjectSelectionList<StatsScreen.ItemStatisticsList.ItemRow> {
      protected final List<StatType<Block>> blockColumns;
      protected final List<StatType<Item>> itemColumns;
      private final int[] iconOffsets = new int[]{3, 4, 1, 2, 5, 6};
      protected int headerPressed = -1;
      protected final Comparator<StatsScreen.ItemStatisticsList.ItemRow> itemStatSorter = new StatsScreen.ItemStatisticsList.ItemRowComparator();
      @Nullable
      protected StatType<?> sortColumn;
      protected int sortOrder;

      public ItemStatisticsList(Minecraft minecraft) {
         super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
         this.blockColumns = Lists.newArrayList();
         this.blockColumns.add(Stats.BLOCK_MINED);
         this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
         this.setRenderHeader(true, 20);
         Set<Item> set = Sets.newIdentityHashSet();

         for(Item item : BuiltInRegistries.ITEM) {
            boolean flag = false;

            for(StatType<Item> stattype : this.itemColumns) {
               if (stattype.contains(item) && StatsScreen.this.stats.getValue(stattype.get(item)) > 0) {
                  flag = true;
               }
            }

            if (flag) {
               set.add(item);
            }
         }

         for(Block block : BuiltInRegistries.BLOCK) {
            boolean flag1 = false;

            for(StatType<Block> stattype1 : this.blockColumns) {
               if (stattype1.contains(block) && StatsScreen.this.stats.getValue(stattype1.get(block)) > 0) {
                  flag1 = true;
               }
            }

            if (flag1) {
               set.add(block.asItem());
            }
         }

         set.remove(Items.AIR);

         for(Item item1 : set) {
            this.addEntry(new StatsScreen.ItemStatisticsList.ItemRow(item1));
         }

      }

      protected void renderHeader(GuiGraphics guigraphics, int i, int j) {
         if (!this.minecraft.mouseHandler.isLeftPressed()) {
            this.headerPressed = -1;
         }

         for(int k = 0; k < this.iconOffsets.length; ++k) {
            StatsScreen.this.blitSlotIcon(guigraphics, i + StatsScreen.this.getColumnX(k) - 18, j + 1, 0, this.headerPressed == k ? 0 : 18);
         }

         if (this.sortColumn != null) {
            int l = StatsScreen.this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
            int i1 = this.sortOrder == 1 ? 2 : 1;
            StatsScreen.this.blitSlotIcon(guigraphics, i + l, j + 1, 18 * i1, 0);
         }

         for(int j1 = 0; j1 < this.iconOffsets.length; ++j1) {
            int k1 = this.headerPressed == j1 ? 1 : 0;
            StatsScreen.this.blitSlotIcon(guigraphics, i + StatsScreen.this.getColumnX(j1) - 18 + k1, j + 1 + k1, 18 * this.iconOffsets[j1], 18);
         }

      }

      public int getRowWidth() {
         return 375;
      }

      protected int getScrollbarPosition() {
         return this.width / 2 + 140;
      }

      protected void renderBackground(GuiGraphics guigraphics) {
         StatsScreen.this.renderBackground(guigraphics);
      }

      protected void clickedHeader(int i, int j) {
         this.headerPressed = -1;

         for(int k = 0; k < this.iconOffsets.length; ++k) {
            int l = i - StatsScreen.this.getColumnX(k);
            if (l >= -36 && l <= 0) {
               this.headerPressed = k;
               break;
            }
         }

         if (this.headerPressed >= 0) {
            this.sortByColumn(this.getColumn(this.headerPressed));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

      }

      private StatType<?> getColumn(int i) {
         return i < this.blockColumns.size() ? this.blockColumns.get(i) : this.itemColumns.get(i - this.blockColumns.size());
      }

      private int getColumnIndex(StatType<?> stattype) {
         int i = this.blockColumns.indexOf(stattype);
         if (i >= 0) {
            return i;
         } else {
            int j = this.itemColumns.indexOf(stattype);
            return j >= 0 ? j + this.blockColumns.size() : -1;
         }
      }

      protected void renderDecorations(GuiGraphics guigraphics, int i, int j) {
         if (j >= this.y0 && j <= this.y1) {
            StatsScreen.ItemStatisticsList.ItemRow statsscreen_itemstatisticslist_itemrow = this.getHovered();
            int k = (this.width - this.getRowWidth()) / 2;
            if (statsscreen_itemstatisticslist_itemrow != null) {
               if (i < k + 40 || i > k + 40 + 20) {
                  return;
               }

               Item item = statsscreen_itemstatisticslist_itemrow.getItem();
               this.renderMousehoverTooltip(guigraphics, this.getString(item), i, j);
            } else {
               Component component = null;
               int l = i - k;

               for(int i1 = 0; i1 < this.iconOffsets.length; ++i1) {
                  int j1 = StatsScreen.this.getColumnX(i1);
                  if (l >= j1 - 18 && l <= j1) {
                     component = this.getColumn(i1).getDisplayName();
                     break;
                  }
               }

               this.renderMousehoverTooltip(guigraphics, component, i, j);
            }

         }
      }

      protected void renderMousehoverTooltip(GuiGraphics guigraphics, @Nullable Component component, int i, int j) {
         if (component != null) {
            int k = i + 12;
            int l = j - 12;
            int i1 = StatsScreen.this.font.width(component);
            guigraphics.fillGradient(k - 3, l - 3, k + i1 + 3, l + 8 + 3, -1073741824, -1073741824);
            guigraphics.pose().pushPose();
            guigraphics.pose().translate(0.0F, 0.0F, 400.0F);
            guigraphics.drawString(StatsScreen.this.font, component, k, l, -1);
            guigraphics.pose().popPose();
         }
      }

      protected Component getString(Item item) {
         return item.getDescription();
      }

      protected void sortByColumn(StatType<?> stattype) {
         if (stattype != this.sortColumn) {
            this.sortColumn = stattype;
            this.sortOrder = -1;
         } else if (this.sortOrder == -1) {
            this.sortOrder = 1;
         } else {
            this.sortColumn = null;
            this.sortOrder = 0;
         }

         this.children().sort(this.itemStatSorter);
      }

      class ItemRow extends ObjectSelectionList.Entry<StatsScreen.ItemStatisticsList.ItemRow> {
         private final Item item;

         ItemRow(Item item) {
            this.item = item;
         }

         public Item getItem() {
            return this.item;
         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            StatsScreen.this.blitSlot(guigraphics, k + 40, j, this.item);

            for(int l1 = 0; l1 < StatsScreen.this.itemStatsList.blockColumns.size(); ++l1) {
               Stat<Block> stat;
               if (this.item instanceof BlockItem) {
                  stat = StatsScreen.this.itemStatsList.blockColumns.get(l1).get(((BlockItem)this.item).getBlock());
               } else {
                  stat = null;
               }

               this.renderStat(guigraphics, stat, k + StatsScreen.this.getColumnX(l1), j, i % 2 == 0);
            }

            for(int i2 = 0; i2 < StatsScreen.this.itemStatsList.itemColumns.size(); ++i2) {
               this.renderStat(guigraphics, StatsScreen.this.itemStatsList.itemColumns.get(i2).get(this.item), k + StatsScreen.this.getColumnX(i2 + StatsScreen.this.itemStatsList.blockColumns.size()), j, i % 2 == 0);
            }

         }

         protected void renderStat(GuiGraphics guigraphics, @Nullable Stat<?> stat, int i, int j, boolean flag) {
            String s = stat == null ? "-" : stat.format(StatsScreen.this.stats.getValue(stat));
            guigraphics.drawString(StatsScreen.this.font, s, i - StatsScreen.this.font.width(s), j + 5, flag ? 16777215 : 9474192);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", this.item.getDescription());
         }
      }

      class ItemRowComparator implements Comparator<StatsScreen.ItemStatisticsList.ItemRow> {
         public int compare(StatsScreen.ItemStatisticsList.ItemRow statsscreen_itemstatisticslist_itemrow, StatsScreen.ItemStatisticsList.ItemRow statsscreen_itemstatisticslist_itemrow1) {
            Item item = statsscreen_itemstatisticslist_itemrow.getItem();
            Item item1 = statsscreen_itemstatisticslist_itemrow1.getItem();
            int i;
            int j;
            if (ItemStatisticsList.this.sortColumn == null) {
               i = 0;
               j = 0;
            } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
               StatType<Block> stattype = ItemStatisticsList.this.sortColumn;
               i = item instanceof BlockItem ? StatsScreen.this.stats.getValue(stattype, ((BlockItem)item).getBlock()) : -1;
               j = item1 instanceof BlockItem ? StatsScreen.this.stats.getValue(stattype, ((BlockItem)item1).getBlock()) : -1;
            } else {
               StatType<Item> stattype1 = ItemStatisticsList.this.sortColumn;
               i = StatsScreen.this.stats.getValue(stattype1, item);
               j = StatsScreen.this.stats.getValue(stattype1, item1);
            }

            return i == j ? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item1)) : ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
         }
      }
   }

   class MobsStatisticsList extends ObjectSelectionList<StatsScreen.MobsStatisticsList.MobRow> {
      public MobsStatisticsList(Minecraft minecraft) {
         super(minecraft, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 9 * 4);

         for(EntityType<?> entitytype : BuiltInRegistries.ENTITY_TYPE) {
            if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entitytype)) > 0 || StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entitytype)) > 0) {
               this.addEntry(new StatsScreen.MobsStatisticsList.MobRow(entitytype));
            }
         }

      }

      protected void renderBackground(GuiGraphics guigraphics) {
         StatsScreen.this.renderBackground(guigraphics);
      }

      class MobRow extends ObjectSelectionList.Entry<StatsScreen.MobsStatisticsList.MobRow> {
         private final Component mobName;
         private final Component kills;
         private final boolean hasKills;
         private final Component killedBy;
         private final boolean wasKilledBy;

         public MobRow(EntityType<?> entitytype) {
            this.mobName = entitytype.getDescription();
            int i = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entitytype));
            if (i == 0) {
               this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
               this.hasKills = false;
            } else {
               this.kills = Component.translatable("stat_type.minecraft.killed", i, this.mobName);
               this.hasKills = true;
            }

            int j = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entitytype));
            if (j == 0) {
               this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
               this.wasKilledBy = false;
            } else {
               this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, j);
               this.wasKilledBy = true;
            }

         }

         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            guigraphics.drawString(StatsScreen.this.font, this.mobName, k + 2, j + 1, 16777215);
            guigraphics.drawString(StatsScreen.this.font, this.kills, k + 2 + 10, j + 1 + 9, this.hasKills ? 9474192 : 6316128);
            guigraphics.drawString(StatsScreen.this.font, this.killedBy, k + 2 + 10, j + 1 + 9 * 2, this.wasKilledBy ? 9474192 : 6316128);
         }

         public Component getNarration() {
            return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
         }
      }
   }
}
