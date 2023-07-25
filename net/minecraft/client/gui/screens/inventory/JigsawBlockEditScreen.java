package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

public class JigsawBlockEditScreen extends Screen {
   private static final int MAX_LEVELS = 7;
   private static final Component JOINT_LABEL = Component.translatable("jigsaw_block.joint_label");
   private static final Component POOL_LABEL = Component.translatable("jigsaw_block.pool");
   private static final Component NAME_LABEL = Component.translatable("jigsaw_block.name");
   private static final Component TARGET_LABEL = Component.translatable("jigsaw_block.target");
   private static final Component FINAL_STATE_LABEL = Component.translatable("jigsaw_block.final_state");
   private final JigsawBlockEntity jigsawEntity;
   private EditBox nameEdit;
   private EditBox targetEdit;
   private EditBox poolEdit;
   private EditBox finalStateEdit;
   int levels;
   private boolean keepJigsaws = true;
   private CycleButton<JigsawBlockEntity.JointType> jointButton;
   private Button doneButton;
   private Button generateButton;
   private JigsawBlockEntity.JointType joint;

   public JigsawBlockEditScreen(JigsawBlockEntity jigsawblockentity) {
      super(GameNarrator.NO_TITLE);
      this.jigsawEntity = jigsawblockentity;
   }

   public void tick() {
      this.nameEdit.tick();
      this.targetEdit.tick();
      this.poolEdit.tick();
      this.finalStateEdit.tick();
   }

   private void onDone() {
      this.sendToServer();
      this.minecraft.setScreen((Screen)null);
   }

   private void onCancel() {
      this.minecraft.setScreen((Screen)null);
   }

   private void sendToServer() {
      this.minecraft.getConnection().send(new ServerboundSetJigsawBlockPacket(this.jigsawEntity.getBlockPos(), new ResourceLocation(this.nameEdit.getValue()), new ResourceLocation(this.targetEdit.getValue()), new ResourceLocation(this.poolEdit.getValue()), this.finalStateEdit.getValue(), this.joint));
   }

   private void sendGenerate() {
      this.minecraft.getConnection().send(new ServerboundJigsawGeneratePacket(this.jigsawEntity.getBlockPos(), this.levels, this.keepJigsaws));
   }

   public void onClose() {
      this.onCancel();
   }

   protected void init() {
      this.poolEdit = new EditBox(this.font, this.width / 2 - 152, 20, 300, 20, Component.translatable("jigsaw_block.pool"));
      this.poolEdit.setMaxLength(128);
      this.poolEdit.setValue(this.jigsawEntity.getPool().location().toString());
      this.poolEdit.setResponder((s2) -> this.updateValidity());
      this.addWidget(this.poolEdit);
      this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 55, 300, 20, Component.translatable("jigsaw_block.name"));
      this.nameEdit.setMaxLength(128);
      this.nameEdit.setValue(this.jigsawEntity.getName().toString());
      this.nameEdit.setResponder((s1) -> this.updateValidity());
      this.addWidget(this.nameEdit);
      this.targetEdit = new EditBox(this.font, this.width / 2 - 152, 90, 300, 20, Component.translatable("jigsaw_block.target"));
      this.targetEdit.setMaxLength(128);
      this.targetEdit.setValue(this.jigsawEntity.getTarget().toString());
      this.targetEdit.setResponder((s) -> this.updateValidity());
      this.addWidget(this.targetEdit);
      this.finalStateEdit = new EditBox(this.font, this.width / 2 - 152, 125, 300, 20, Component.translatable("jigsaw_block.final_state"));
      this.finalStateEdit.setMaxLength(256);
      this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
      this.addWidget(this.finalStateEdit);
      this.joint = this.jigsawEntity.getJoint();
      int i = this.font.width(JOINT_LABEL) + 10;
      this.jointButton = this.addRenderableWidget(CycleButton.builder(JigsawBlockEntity.JointType::getTranslatedName).withValues(JigsawBlockEntity.JointType.values()).withInitialValue(this.joint).displayOnlyValue().create(this.width / 2 - 152 + i, 150, 300 - i, 20, JOINT_LABEL, (cyclebutton1, jigsawblockentity_jointtype) -> this.joint = jigsawblockentity_jointtype));
      boolean flag = JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical();
      this.jointButton.active = flag;
      this.jointButton.visible = flag;
      this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 154, 180, 100, 20, CommonComponents.EMPTY, 0.0D) {
         {
            this.updateMessage();
         }

         protected void updateMessage() {
            this.setMessage(Component.translatable("jigsaw_block.levels", JigsawBlockEditScreen.this.levels));
         }

         protected void applyValue() {
            JigsawBlockEditScreen.this.levels = Mth.floor(Mth.clampedLerp(0.0D, 7.0D, this.value));
         }
      });
      this.addRenderableWidget(CycleButton.onOffBuilder(this.keepJigsaws).create(this.width / 2 - 50, 180, 100, 20, Component.translatable("jigsaw_block.keep_jigsaws"), (cyclebutton, obool) -> this.keepJigsaws = obool));
      this.generateButton = this.addRenderableWidget(Button.builder(Component.translatable("jigsaw_block.generate"), (button2) -> {
         this.onDone();
         this.sendGenerate();
      }).bounds(this.width / 2 + 54, 180, 100, 20).build());
      this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button1) -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
      this.setInitialFocus(this.poolEdit);
      this.updateValidity();
   }

   private void updateValidity() {
      boolean flag = ResourceLocation.isValidResourceLocation(this.nameEdit.getValue()) && ResourceLocation.isValidResourceLocation(this.targetEdit.getValue()) && ResourceLocation.isValidResourceLocation(this.poolEdit.getValue());
      this.doneButton.active = flag;
      this.generateButton.active = flag;
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.nameEdit.getValue();
      String s1 = this.targetEdit.getValue();
      String s2 = this.poolEdit.getValue();
      String s3 = this.finalStateEdit.getValue();
      int k = this.levels;
      JigsawBlockEntity.JointType jigsawblockentity_jointtype = this.joint;
      this.init(minecraft, i, j);
      this.nameEdit.setValue(s);
      this.targetEdit.setValue(s1);
      this.poolEdit.setValue(s2);
      this.finalStateEdit.setValue(s3);
      this.levels = k;
      this.joint = jigsawblockentity_jointtype;
      this.jointButton.setValue(jigsawblockentity_jointtype);
   }

   public boolean keyPressed(int i, int j, int k) {
      if (super.keyPressed(i, j, k)) {
         return true;
      } else if (!this.doneButton.active || i != 257 && i != 335) {
         return false;
      } else {
         this.onDone();
         return true;
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      guigraphics.drawString(this.font, POOL_LABEL, this.width / 2 - 153, 10, 10526880);
      this.poolEdit.render(guigraphics, i, j, f);
      guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 153, 45, 10526880);
      this.nameEdit.render(guigraphics, i, j, f);
      guigraphics.drawString(this.font, TARGET_LABEL, this.width / 2 - 153, 80, 10526880);
      this.targetEdit.render(guigraphics, i, j, f);
      guigraphics.drawString(this.font, FINAL_STATE_LABEL, this.width / 2 - 153, 115, 10526880);
      this.finalStateEdit.render(guigraphics, i, j, f);
      if (JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical()) {
         guigraphics.drawString(this.font, JOINT_LABEL, this.width / 2 - 153, 156, 16777215);
      }

      super.render(guigraphics, i, j, f);
   }
}
