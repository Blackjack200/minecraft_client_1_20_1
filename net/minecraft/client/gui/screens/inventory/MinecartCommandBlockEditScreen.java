package net.minecraft.client.gui.screens.inventory;

import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;

public class MinecartCommandBlockEditScreen extends AbstractCommandBlockEditScreen {
   private final BaseCommandBlock commandBlock;

   public MinecartCommandBlockEditScreen(BaseCommandBlock basecommandblock) {
      this.commandBlock = basecommandblock;
   }

   public BaseCommandBlock getCommandBlock() {
      return this.commandBlock;
   }

   int getPreviousY() {
      return 150;
   }

   protected void init() {
      super.init();
      this.commandEdit.setValue(this.getCommandBlock().getCommand());
   }

   protected void populateAndSendPacket(BaseCommandBlock basecommandblock) {
      if (basecommandblock instanceof MinecartCommandBlock.MinecartCommandBase minecartcommandblock_minecartcommandbase) {
         this.minecraft.getConnection().send(new ServerboundSetCommandMinecartPacket(minecartcommandblock_minecartcommandbase.getMinecart().getId(), this.commandEdit.getValue(), basecommandblock.isTrackOutput()));
      }

   }
}
