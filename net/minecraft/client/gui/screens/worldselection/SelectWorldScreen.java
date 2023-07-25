package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.slf4j.Logger;

public class SelectWorldScreen extends Screen {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final WorldOptions TEST_OPTIONS = new WorldOptions((long)"test1".hashCode(), true, false);
   protected final Screen lastScreen;
   private Button deleteButton;
   private Button selectButton;
   private Button renameButton;
   private Button copyButton;
   protected EditBox searchBox;
   private WorldSelectionList list;

   public SelectWorldScreen(Screen screen) {
      super(Component.translatable("selectWorld.title"));
      this.lastScreen = screen;
   }

   public void tick() {
      this.searchBox.tick();
   }

   protected void init() {
      this.searchBox = new EditBox(this.font, this.width / 2 - 100, 22, 200, 20, this.searchBox, Component.translatable("selectWorld.search"));
      this.searchBox.setResponder((s) -> this.list.updateFilter(s));
      this.list = new WorldSelectionList(this, this.minecraft, this.width, this.height, 48, this.height - 64, 36, this.searchBox.getValue(), this.list);
      this.addWidget(this.searchBox);
      this.addWidget(this.list);
      this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.select"), (button5) -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::joinWorld)).bounds(this.width / 2 - 154, this.height - 52, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.create"), (button4) -> CreateWorldScreen.openFresh(this.minecraft, this)).bounds(this.width / 2 + 4, this.height - 52, 150, 20).build());
      this.renameButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit"), (button3) -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::editWorld)).bounds(this.width / 2 - 154, this.height - 28, 72, 20).build());
      this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.delete"), (button2) -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::deleteWorld)).bounds(this.width / 2 - 76, this.height - 28, 72, 20).build());
      this.copyButton = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.recreate"), (button1) -> this.list.getSelectedOpt().ifPresent(WorldSelectionList.WorldListEntry::recreateWorld)).bounds(this.width / 2 + 4, this.height - 28, 72, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 + 82, this.height - 28, 72, 20).build());
      this.updateButtonStatus(false, false);
      this.setInitialFocus(this.searchBox);
   }

   public boolean keyPressed(int i, int j, int k) {
      return super.keyPressed(i, j, k) ? true : this.searchBox.keyPressed(i, j, k);
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   public boolean charTyped(char c0, int i) {
      return this.searchBox.charTyped(c0, i);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.list.render(guigraphics, i, j, f);
      this.searchBox.render(guigraphics, i, j, f);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
      super.render(guigraphics, i, j, f);
   }

   public void updateButtonStatus(boolean flag, boolean flag1) {
      this.selectButton.active = flag;
      this.renameButton.active = flag;
      this.copyButton.active = flag;
      this.deleteButton.active = flag1;
   }

   public void removed() {
      if (this.list != null) {
         this.list.children().forEach(WorldSelectionList.Entry::close);
      }

   }
}
