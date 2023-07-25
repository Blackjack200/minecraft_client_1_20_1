package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class CommandBlockEditScreen extends AbstractCommandBlockEditScreen {
   private final CommandBlockEntity autoCommandBlock;
   private CycleButton<CommandBlockEntity.Mode> modeButton;
   private CycleButton<Boolean> conditionalButton;
   private CycleButton<Boolean> autoexecButton;
   private CommandBlockEntity.Mode mode = CommandBlockEntity.Mode.REDSTONE;
   private boolean conditional;
   private boolean autoexec;

   public CommandBlockEditScreen(CommandBlockEntity commandblockentity) {
      this.autoCommandBlock = commandblockentity;
   }

   BaseCommandBlock getCommandBlock() {
      return this.autoCommandBlock.getCommandBlock();
   }

   int getPreviousY() {
      return 135;
   }

   protected void init() {
      super.init();
      this.modeButton = this.addRenderableWidget(CycleButton.builder((commandblockentity_mode1) -> {
         MutableComponent var10000;
         switch (commandblockentity_mode1) {
            case SEQUENCE:
               var10000 = Component.translatable("advMode.mode.sequence");
               break;
            case AUTO:
               var10000 = Component.translatable("advMode.mode.auto");
               break;
            case REDSTONE:
               var10000 = Component.translatable("advMode.mode.redstone");
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }).withValues(CommandBlockEntity.Mode.values()).displayOnlyValue().withInitialValue(this.mode).create(this.width / 2 - 50 - 100 - 4, 165, 100, 20, Component.translatable("advMode.mode"), (cyclebutton2, commandblockentity_mode) -> this.mode = commandblockentity_mode));
      this.conditionalButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("advMode.mode.conditional"), Component.translatable("advMode.mode.unconditional")).displayOnlyValue().withInitialValue(this.conditional).create(this.width / 2 - 50, 165, 100, 20, Component.translatable("advMode.type"), (cyclebutton1, obool1) -> this.conditional = obool1));
      this.autoexecButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("advMode.mode.autoexec.bat"), Component.translatable("advMode.mode.redstoneTriggered")).displayOnlyValue().withInitialValue(this.autoexec).create(this.width / 2 + 50 + 4, 165, 100, 20, Component.translatable("advMode.triggering"), (cyclebutton, obool) -> this.autoexec = obool));
      this.enableControls(false);
   }

   private void enableControls(boolean flag) {
      this.doneButton.active = flag;
      this.outputButton.active = flag;
      this.modeButton.active = flag;
      this.conditionalButton.active = flag;
      this.autoexecButton.active = flag;
   }

   public void updateGui() {
      BaseCommandBlock basecommandblock = this.autoCommandBlock.getCommandBlock();
      this.commandEdit.setValue(basecommandblock.getCommand());
      boolean flag = basecommandblock.isTrackOutput();
      this.mode = this.autoCommandBlock.getMode();
      this.conditional = this.autoCommandBlock.isConditional();
      this.autoexec = this.autoCommandBlock.isAutomatic();
      this.outputButton.setValue(flag);
      this.modeButton.setValue(this.mode);
      this.conditionalButton.setValue(this.conditional);
      this.autoexecButton.setValue(this.autoexec);
      this.updatePreviousOutput(flag);
      this.enableControls(true);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      super.resize(minecraft, i, j);
      this.enableControls(true);
   }

   protected void populateAndSendPacket(BaseCommandBlock basecommandblock) {
      this.minecraft.getConnection().send(new ServerboundSetCommandBlockPacket(BlockPos.containing(basecommandblock.getPosition()), this.commandEdit.getValue(), this.mode, basecommandblock.isTrackOutput(), this.conditional, this.autoexec));
   }
}
