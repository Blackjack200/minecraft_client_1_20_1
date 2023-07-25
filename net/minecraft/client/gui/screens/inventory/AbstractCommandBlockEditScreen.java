package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BaseCommandBlock;

public abstract class AbstractCommandBlockEditScreen extends Screen {
   private static final Component SET_COMMAND_LABEL = Component.translatable("advMode.setCommand");
   private static final Component COMMAND_LABEL = Component.translatable("advMode.command");
   private static final Component PREVIOUS_OUTPUT_LABEL = Component.translatable("advMode.previousOutput");
   protected EditBox commandEdit;
   protected EditBox previousEdit;
   protected Button doneButton;
   protected Button cancelButton;
   protected CycleButton<Boolean> outputButton;
   CommandSuggestions commandSuggestions;

   public AbstractCommandBlockEditScreen() {
      super(GameNarrator.NO_TITLE);
   }

   public void tick() {
      this.commandEdit.tick();
      if (!this.getCommandBlock().isValid()) {
         this.onClose();
      }

   }

   abstract BaseCommandBlock getCommandBlock();

   abstract int getPreviousY();

   protected void init() {
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button1) -> this.onDone()).bounds(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20).build());
      this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onClose()).bounds(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20).build());
      boolean flag = this.getCommandBlock().isTrackOutput();
      this.outputButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("O"), Component.literal("X")).withInitialValue(flag).displayOnlyValue().create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, Component.translatable("advMode.trackOutput"), (cyclebutton, obool) -> {
         BaseCommandBlock basecommandblock = this.getCommandBlock();
         basecommandblock.setTrackOutput(obool);
         this.updatePreviousOutput(obool);
      }));
      this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, Component.translatable("advMode.command")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.commandEdit.setMaxLength(32500);
      this.commandEdit.setResponder(this::onEdited);
      this.addWidget(this.commandEdit);
      this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, Component.translatable("advMode.previousOutput"));
      this.previousEdit.setMaxLength(32500);
      this.previousEdit.setEditable(false);
      this.previousEdit.setValue("-");
      this.addWidget(this.previousEdit);
      this.setInitialFocus(this.commandEdit);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
      this.commandSuggestions.setAllowSuggestions(true);
      this.commandSuggestions.updateCommandInfo();
      this.updatePreviousOutput(flag);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.commandEdit.getValue();
      this.init(minecraft, i, j);
      this.commandEdit.setValue(s);
      this.commandSuggestions.updateCommandInfo();
   }

   protected void updatePreviousOutput(boolean flag) {
      this.previousEdit.setValue(flag ? this.getCommandBlock().getLastOutput().getString() : "-");
   }

   protected void onDone() {
      BaseCommandBlock basecommandblock = this.getCommandBlock();
      this.populateAndSendPacket(basecommandblock);
      if (!basecommandblock.isTrackOutput()) {
         basecommandblock.setLastOutput((Component)null);
      }

      this.minecraft.setScreen((Screen)null);
   }

   protected abstract void populateAndSendPacket(BaseCommandBlock basecommandblock);

   private void onEdited(String s) {
      this.commandSuggestions.updateCommandInfo();
   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.commandSuggestions.keyPressed(i, j, k)) {
         return true;
      } else if (super.keyPressed(i, j, k)) {
         return true;
      } else if (i != 257 && i != 335) {
         return false;
      } else {
         this.onDone();
         return true;
      }
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      return this.commandSuggestions.mouseScrolled(d2) ? true : super.mouseScrolled(d0, d1, d2);
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      return this.commandSuggestions.mouseClicked(d0, d1, i) ? true : super.mouseClicked(d0, d1, i);
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, SET_COMMAND_LABEL, this.width / 2, 20, 16777215);
      guigraphics.drawString(this.font, COMMAND_LABEL, this.width / 2 - 150, 40, 10526880);
      this.commandEdit.render(guigraphics, i, j, f);
      int k = 75;
      if (!this.previousEdit.getValue().isEmpty()) {
         k += 5 * 9 + 1 + this.getPreviousY() - 135;
         guigraphics.drawString(this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150, k + 4, 10526880);
         this.previousEdit.render(guigraphics, i, j, f);
      }

      super.render(guigraphics, i, j, f);
      this.commandSuggestions.render(guigraphics, i, j);
   }
}
