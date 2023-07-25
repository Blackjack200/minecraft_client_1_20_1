package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class CreateFlatWorldScreen extends Screen {
   private static final int SLOT_TEX_SIZE = 128;
   private static final int SLOT_BG_SIZE = 18;
   private static final int SLOT_STAT_HEIGHT = 20;
   private static final int SLOT_BG_X = 1;
   private static final int SLOT_BG_Y = 1;
   private static final int SLOT_FG_X = 2;
   private static final int SLOT_FG_Y = 2;
   protected final CreateWorldScreen parent;
   private final Consumer<FlatLevelGeneratorSettings> applySettings;
   FlatLevelGeneratorSettings generator;
   private Component columnType;
   private Component columnHeight;
   private CreateFlatWorldScreen.DetailsList list;
   private Button deleteLayerButton;

   public CreateFlatWorldScreen(CreateWorldScreen createworldscreen, Consumer<FlatLevelGeneratorSettings> consumer, FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      super(Component.translatable("createWorld.customize.flat.title"));
      this.parent = createworldscreen;
      this.applySettings = consumer;
      this.generator = flatlevelgeneratorsettings;
   }

   public FlatLevelGeneratorSettings settings() {
      return this.generator;
   }

   public void setConfig(FlatLevelGeneratorSettings flatlevelgeneratorsettings) {
      this.generator = flatlevelgeneratorsettings;
   }

   protected void init() {
      this.columnType = Component.translatable("createWorld.customize.flat.tile");
      this.columnHeight = Component.translatable("createWorld.customize.flat.height");
      this.list = new CreateFlatWorldScreen.DetailsList();
      this.addWidget(this.list);
      this.deleteLayerButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.flat.removeLayer"), (button3) -> {
         if (this.hasValidSelection()) {
            List<FlatLayerInfo> list = this.generator.getLayersInfo();
            int i = this.list.children().indexOf(this.list.getSelected());
            int j = list.size() - i - 1;
            list.remove(j);
            this.list.setSelected(list.isEmpty() ? null : this.list.children().get(Math.min(i, list.size() - 1)));
            this.generator.updateLayers();
            this.list.resetRows();
            this.updateButtonValidity();
         }
      }).bounds(this.width / 2 - 155, this.height - 52, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets"), (button2) -> {
         this.minecraft.setScreen(new PresetFlatWorldScreen(this));
         this.generator.updateLayers();
         this.updateButtonValidity();
      }).bounds(this.width / 2 + 5, this.height - 52, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button1) -> {
         this.applySettings.accept(this.generator);
         this.minecraft.setScreen(this.parent);
         this.generator.updateLayers();
      }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
         this.minecraft.setScreen(this.parent);
         this.generator.updateLayers();
      }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
      this.generator.updateLayers();
      this.updateButtonValidity();
   }

   void updateButtonValidity() {
      this.deleteLayerButton.active = this.hasValidSelection();
   }

   private boolean hasValidSelection() {
      return this.list.getSelected() != null;
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      this.list.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      int k = this.width / 2 - 92 - 16;
      guigraphics.drawString(this.font, this.columnType, k, 32, 16777215);
      guigraphics.drawString(this.font, this.columnHeight, k + 2 + 213 - this.font.width(this.columnHeight), 32, 16777215);
      super.render(guigraphics, i, j, f);
   }

   class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
      static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");

      public DetailsList() {
         super(CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height, 43, CreateFlatWorldScreen.this.height - 60, 24);

         for(int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++i) {
            this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
         }

      }

      public void setSelected(@Nullable CreateFlatWorldScreen.DetailsList.Entry createflatworldscreen_detailslist_entry) {
         super.setSelected(createflatworldscreen_detailslist_entry);
         CreateFlatWorldScreen.this.updateButtonValidity();
      }

      protected int getScrollbarPosition() {
         return this.width - 70;
      }

      public void resetRows() {
         int i = this.children().indexOf(this.getSelected());
         this.clearEntries();

         for(int j = 0; j < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); ++j) {
            this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
         }

         List<CreateFlatWorldScreen.DetailsList.Entry> list = this.children();
         if (i >= 0 && i < list.size()) {
            this.setSelected(list.get(i));
         }

      }

      class Entry extends ObjectSelectionList.Entry<CreateFlatWorldScreen.DetailsList.Entry> {
         public void render(GuiGraphics guigraphics, int i, int j, int k, int l, int i1, int j1, int k1, boolean flag, float f) {
            FlatLayerInfo flatlayerinfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - i - 1);
            BlockState blockstate = flatlayerinfo.getBlockState();
            ItemStack itemstack = this.getDisplayItem(blockstate);
            this.blitSlot(guigraphics, k, j, itemstack);
            guigraphics.drawString(CreateFlatWorldScreen.this.font, itemstack.getHoverName(), k + 18 + 5, j + 3, 16777215, false);
            Component component;
            if (i == 0) {
               component = Component.translatable("createWorld.customize.flat.layer.top", flatlayerinfo.getHeight());
            } else if (i == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
               component = Component.translatable("createWorld.customize.flat.layer.bottom", flatlayerinfo.getHeight());
            } else {
               component = Component.translatable("createWorld.customize.flat.layer", flatlayerinfo.getHeight());
            }

            guigraphics.drawString(CreateFlatWorldScreen.this.font, component, k + 2 + 213 - CreateFlatWorldScreen.this.font.width(component), j + 3, 16777215, false);
         }

         private ItemStack getDisplayItem(BlockState blockstate) {
            Item item = blockstate.getBlock().asItem();
            if (item == Items.AIR) {
               if (blockstate.is(Blocks.WATER)) {
                  item = Items.WATER_BUCKET;
               } else if (blockstate.is(Blocks.LAVA)) {
                  item = Items.LAVA_BUCKET;
               }
            }

            return new ItemStack(item);
         }

         public Component getNarration() {
            FlatLayerInfo flatlayerinfo = CreateFlatWorldScreen.this.generator.getLayersInfo().get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - DetailsList.this.children().indexOf(this) - 1);
            ItemStack itemstack = this.getDisplayItem(flatlayerinfo.getBlockState());
            return (Component)(!itemstack.isEmpty() ? Component.translatable("narrator.select", itemstack.getHoverName()) : CommonComponents.EMPTY);
         }

         public boolean mouseClicked(double d0, double d1, int i) {
            if (i == 0) {
               DetailsList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }

         private void blitSlot(GuiGraphics guigraphics, int i, int j, ItemStack itemstack) {
            this.blitSlotBg(guigraphics, i + 1, j + 1);
            if (!itemstack.isEmpty()) {
               guigraphics.renderFakeItem(itemstack, i + 2, j + 2);
            }

         }

         private void blitSlotBg(GuiGraphics guigraphics, int i, int j) {
            guigraphics.blit(CreateFlatWorldScreen.DetailsList.STATS_ICON_LOCATION, i, j, 0, 0.0F, 0.0F, 18, 18, 128, 128);
         }
      }
   }
}
