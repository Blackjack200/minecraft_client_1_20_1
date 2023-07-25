package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class StructureBlockEditScreen extends Screen {
   private static final Component NAME_LABEL = Component.translatable("structure_block.structure_name");
   private static final Component POSITION_LABEL = Component.translatable("structure_block.position");
   private static final Component SIZE_LABEL = Component.translatable("structure_block.size");
   private static final Component INTEGRITY_LABEL = Component.translatable("structure_block.integrity");
   private static final Component CUSTOM_DATA_LABEL = Component.translatable("structure_block.custom_data");
   private static final Component INCLUDE_ENTITIES_LABEL = Component.translatable("structure_block.include_entities");
   private static final Component DETECT_SIZE_LABEL = Component.translatable("structure_block.detect_size");
   private static final Component SHOW_AIR_LABEL = Component.translatable("structure_block.show_air");
   private static final Component SHOW_BOUNDING_BOX_LABEL = Component.translatable("structure_block.show_boundingbox");
   private static final ImmutableList<StructureMode> ALL_MODES = ImmutableList.copyOf(StructureMode.values());
   private static final ImmutableList<StructureMode> DEFAULT_MODES = ALL_MODES.stream().filter((structuremode) -> structuremode != StructureMode.DATA).collect(ImmutableList.toImmutableList());
   private final StructureBlockEntity structure;
   private Mirror initialMirror = Mirror.NONE;
   private Rotation initialRotation = Rotation.NONE;
   private StructureMode initialMode = StructureMode.DATA;
   private boolean initialEntityIgnoring;
   private boolean initialShowAir;
   private boolean initialShowBoundingBox;
   private EditBox nameEdit;
   private EditBox posXEdit;
   private EditBox posYEdit;
   private EditBox posZEdit;
   private EditBox sizeXEdit;
   private EditBox sizeYEdit;
   private EditBox sizeZEdit;
   private EditBox integrityEdit;
   private EditBox seedEdit;
   private EditBox dataEdit;
   private Button saveButton;
   private Button loadButton;
   private Button rot0Button;
   private Button rot90Button;
   private Button rot180Button;
   private Button rot270Button;
   private Button detectButton;
   private CycleButton<Boolean> includeEntitiesButton;
   private CycleButton<Mirror> mirrorButton;
   private CycleButton<Boolean> toggleAirButton;
   private CycleButton<Boolean> toggleBoundingBox;
   private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

   public StructureBlockEditScreen(StructureBlockEntity structureblockentity) {
      super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
      this.structure = structureblockentity;
      this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
   }

   public void tick() {
      this.nameEdit.tick();
      this.posXEdit.tick();
      this.posYEdit.tick();
      this.posZEdit.tick();
      this.sizeXEdit.tick();
      this.sizeYEdit.tick();
      this.sizeZEdit.tick();
      this.integrityEdit.tick();
      this.seedEdit.tick();
      this.dataEdit.tick();
   }

   private void onDone() {
      if (this.sendToServer(StructureBlockEntity.UpdateType.UPDATE_DATA)) {
         this.minecraft.setScreen((Screen)null);
      }

   }

   private void onCancel() {
      this.structure.setMirror(this.initialMirror);
      this.structure.setRotation(this.initialRotation);
      this.structure.setMode(this.initialMode);
      this.structure.setIgnoreEntities(this.initialEntityIgnoring);
      this.structure.setShowAir(this.initialShowAir);
      this.structure.setShowBoundingBox(this.initialShowBoundingBox);
      this.minecraft.setScreen((Screen)null);
   }

   protected void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button8) -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button7) -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
      this.initialMirror = this.structure.getMirror();
      this.initialRotation = this.structure.getRotation();
      this.initialMode = this.structure.getMode();
      this.initialEntityIgnoring = this.structure.isIgnoreEntities();
      this.initialShowAir = this.structure.getShowAir();
      this.initialShowBoundingBox = this.structure.getShowBoundingBox();
      this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), (button6) -> {
         if (this.structure.getMode() == StructureMode.SAVE) {
            this.sendToServer(StructureBlockEntity.UpdateType.SAVE_AREA);
            this.minecraft.setScreen((Screen)null);
         }

      }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
      this.loadButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), (button5) -> {
         if (this.structure.getMode() == StructureMode.LOAD) {
            this.sendToServer(StructureBlockEntity.UpdateType.LOAD_AREA);
            this.minecraft.setScreen((Screen)null);
         }

      }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
      this.addRenderableWidget(CycleButton.builder((structuremode1) -> Component.translatable("structure_block.mode." + structuremode1.getSerializedName())).withValues(DEFAULT_MODES, ALL_MODES).displayOnlyValue().withInitialValue(this.initialMode).create(this.width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (cyclebutton4, structuremode) -> {
         this.structure.setMode(structuremode);
         this.updateMode(structuremode);
      }));
      this.detectButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.detect_size"), (button4) -> {
         if (this.structure.getMode() == StructureMode.SAVE) {
            this.sendToServer(StructureBlockEntity.UpdateType.SCAN_AREA);
            this.minecraft.setScreen((Screen)null);
         }

      }).bounds(this.width / 2 + 4 + 100, 120, 50, 20).build());
      this.includeEntitiesButton = this.addRenderableWidget(CycleButton.onOffBuilder(!this.structure.isIgnoreEntities()).displayOnlyValue().create(this.width / 2 + 4 + 100, 160, 50, 20, INCLUDE_ENTITIES_LABEL, (cyclebutton3, obool2) -> this.structure.setIgnoreEntities(!obool2)));
      this.mirrorButton = this.addRenderableWidget(CycleButton.builder(Mirror::symbol).withValues(Mirror.values()).displayOnlyValue().withInitialValue(this.initialMirror).create(this.width / 2 - 20, 185, 40, 20, Component.literal("MIRROR"), (cyclebutton2, mirror) -> this.structure.setMirror(mirror)));
      this.toggleAirButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.getShowAir()).displayOnlyValue().create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_LABEL, (cyclebutton1, obool1) -> this.structure.setShowAir(obool1)));
      this.toggleBoundingBox = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.getShowBoundingBox()).displayOnlyValue().create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_LABEL, (cyclebutton, obool) -> this.structure.setShowBoundingBox(obool)));
      this.rot0Button = this.addRenderableWidget(Button.builder(Component.literal("0"), (button3) -> {
         this.structure.setRotation(Rotation.NONE);
         this.updateDirectionButtons();
      }).bounds(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
      this.rot90Button = this.addRenderableWidget(Button.builder(Component.literal("90"), (button2) -> {
         this.structure.setRotation(Rotation.CLOCKWISE_90);
         this.updateDirectionButtons();
      }).bounds(this.width / 2 - 1 - 40 - 20, 185, 40, 20).build());
      this.rot180Button = this.addRenderableWidget(Button.builder(Component.literal("180"), (button1) -> {
         this.structure.setRotation(Rotation.CLOCKWISE_180);
         this.updateDirectionButtons();
      }).bounds(this.width / 2 + 1 + 20, 185, 40, 20).build());
      this.rot270Button = this.addRenderableWidget(Button.builder(Component.literal("270"), (button) -> {
         this.structure.setRotation(Rotation.COUNTERCLOCKWISE_90);
         this.updateDirectionButtons();
      }).bounds(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());
      this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, Component.translatable("structure_block.structure_name")) {
         public boolean charTyped(char c0, int i) {
            return !StructureBlockEditScreen.this.isValidCharacterForName(this.getValue(), c0, this.getCursorPosition()) ? false : super.charTyped(c0, i);
         }
      };
      this.nameEdit.setMaxLength(128);
      this.nameEdit.setValue(this.structure.getStructureName());
      this.addWidget(this.nameEdit);
      BlockPos blockpos = this.structure.getStructurePos();
      this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
      this.posXEdit.setMaxLength(15);
      this.posXEdit.setValue(Integer.toString(blockpos.getX()));
      this.addWidget(this.posXEdit);
      this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
      this.posYEdit.setMaxLength(15);
      this.posYEdit.setValue(Integer.toString(blockpos.getY()));
      this.addWidget(this.posYEdit);
      this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
      this.posZEdit.setMaxLength(15);
      this.posZEdit.setValue(Integer.toString(blockpos.getZ()));
      this.addWidget(this.posZEdit);
      Vec3i vec3i = this.structure.getStructureSize();
      this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
      this.sizeXEdit.setMaxLength(15);
      this.sizeXEdit.setValue(Integer.toString(vec3i.getX()));
      this.addWidget(this.sizeXEdit);
      this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
      this.sizeYEdit.setMaxLength(15);
      this.sizeYEdit.setValue(Integer.toString(vec3i.getY()));
      this.addWidget(this.sizeYEdit);
      this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
      this.sizeZEdit.setMaxLength(15);
      this.sizeZEdit.setValue(Integer.toString(vec3i.getZ()));
      this.addWidget(this.sizeZEdit);
      this.integrityEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.integrity.integrity"));
      this.integrityEdit.setMaxLength(15);
      this.integrityEdit.setValue(this.decimalFormat.format((double)this.structure.getIntegrity()));
      this.addWidget(this.integrityEdit);
      this.seedEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.integrity.seed"));
      this.seedEdit.setMaxLength(31);
      this.seedEdit.setValue(Long.toString(this.structure.getSeed()));
      this.addWidget(this.seedEdit);
      this.dataEdit = new EditBox(this.font, this.width / 2 - 152, 120, 240, 20, Component.translatable("structure_block.custom_data"));
      this.dataEdit.setMaxLength(128);
      this.dataEdit.setValue(this.structure.getMetaData());
      this.addWidget(this.dataEdit);
      this.updateDirectionButtons();
      this.updateMode(this.initialMode);
      this.setInitialFocus(this.nameEdit);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.nameEdit.getValue();
      String s1 = this.posXEdit.getValue();
      String s2 = this.posYEdit.getValue();
      String s3 = this.posZEdit.getValue();
      String s4 = this.sizeXEdit.getValue();
      String s5 = this.sizeYEdit.getValue();
      String s6 = this.sizeZEdit.getValue();
      String s7 = this.integrityEdit.getValue();
      String s8 = this.seedEdit.getValue();
      String s9 = this.dataEdit.getValue();
      this.init(minecraft, i, j);
      this.nameEdit.setValue(s);
      this.posXEdit.setValue(s1);
      this.posYEdit.setValue(s2);
      this.posZEdit.setValue(s3);
      this.sizeXEdit.setValue(s4);
      this.sizeYEdit.setValue(s5);
      this.sizeZEdit.setValue(s6);
      this.integrityEdit.setValue(s7);
      this.seedEdit.setValue(s8);
      this.dataEdit.setValue(s9);
   }

   private void updateDirectionButtons() {
      this.rot0Button.active = true;
      this.rot90Button.active = true;
      this.rot180Button.active = true;
      this.rot270Button.active = true;
      switch (this.structure.getRotation()) {
         case NONE:
            this.rot0Button.active = false;
            break;
         case CLOCKWISE_180:
            this.rot180Button.active = false;
            break;
         case COUNTERCLOCKWISE_90:
            this.rot270Button.active = false;
            break;
         case CLOCKWISE_90:
            this.rot90Button.active = false;
      }

   }

   private void updateMode(StructureMode structuremode) {
      this.nameEdit.setVisible(false);
      this.posXEdit.setVisible(false);
      this.posYEdit.setVisible(false);
      this.posZEdit.setVisible(false);
      this.sizeXEdit.setVisible(false);
      this.sizeYEdit.setVisible(false);
      this.sizeZEdit.setVisible(false);
      this.integrityEdit.setVisible(false);
      this.seedEdit.setVisible(false);
      this.dataEdit.setVisible(false);
      this.saveButton.visible = false;
      this.loadButton.visible = false;
      this.detectButton.visible = false;
      this.includeEntitiesButton.visible = false;
      this.mirrorButton.visible = false;
      this.rot0Button.visible = false;
      this.rot90Button.visible = false;
      this.rot180Button.visible = false;
      this.rot270Button.visible = false;
      this.toggleAirButton.visible = false;
      this.toggleBoundingBox.visible = false;
      switch (structuremode) {
         case SAVE:
            this.nameEdit.setVisible(true);
            this.posXEdit.setVisible(true);
            this.posYEdit.setVisible(true);
            this.posZEdit.setVisible(true);
            this.sizeXEdit.setVisible(true);
            this.sizeYEdit.setVisible(true);
            this.sizeZEdit.setVisible(true);
            this.saveButton.visible = true;
            this.detectButton.visible = true;
            this.includeEntitiesButton.visible = true;
            this.toggleAirButton.visible = true;
            break;
         case LOAD:
            this.nameEdit.setVisible(true);
            this.posXEdit.setVisible(true);
            this.posYEdit.setVisible(true);
            this.posZEdit.setVisible(true);
            this.integrityEdit.setVisible(true);
            this.seedEdit.setVisible(true);
            this.loadButton.visible = true;
            this.includeEntitiesButton.visible = true;
            this.mirrorButton.visible = true;
            this.rot0Button.visible = true;
            this.rot90Button.visible = true;
            this.rot180Button.visible = true;
            this.rot270Button.visible = true;
            this.toggleBoundingBox.visible = true;
            this.updateDirectionButtons();
            break;
         case CORNER:
            this.nameEdit.setVisible(true);
            break;
         case DATA:
            this.dataEdit.setVisible(true);
      }

   }

   private boolean sendToServer(StructureBlockEntity.UpdateType structureblockentity_updatetype) {
      BlockPos blockpos = new BlockPos(this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue()));
      Vec3i vec3i = new Vec3i(this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue()));
      float f = this.parseIntegrity(this.integrityEdit.getValue());
      long i = this.parseSeed(this.seedEdit.getValue());
      this.minecraft.getConnection().send(new ServerboundSetStructureBlockPacket(this.structure.getBlockPos(), structureblockentity_updatetype, this.structure.getMode(), this.nameEdit.getValue(), blockpos, vec3i, this.structure.getMirror(), this.structure.getRotation(), this.dataEdit.getValue(), this.structure.isIgnoreEntities(), this.structure.getShowAir(), this.structure.getShowBoundingBox(), f, i));
      return true;
   }

   private long parseSeed(String s) {
      try {
         return Long.valueOf(s);
      } catch (NumberFormatException var3) {
         return 0L;
      }
   }

   private float parseIntegrity(String s) {
      try {
         return Float.valueOf(s);
      } catch (NumberFormatException var3) {
         return 1.0F;
      }
   }

   private int parseCoordinate(String s) {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException var3) {
         return 0;
      }
   }

   public void onClose() {
      this.onCancel();
   }

   public boolean keyPressed(int i, int j, int k) {
      if (super.keyPressed(i, j, k)) {
         return true;
      } else if (i != 257 && i != 335) {
         return false;
      } else {
         this.onDone();
         return true;
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      this.renderBackground(guigraphics);
      StructureMode structuremode = this.structure.getMode();
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 16777215);
      if (structuremode != StructureMode.DATA) {
         guigraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 153, 30, 10526880);
         this.nameEdit.render(guigraphics, i, j, f);
      }

      if (structuremode == StructureMode.LOAD || structuremode == StructureMode.SAVE) {
         guigraphics.drawString(this.font, POSITION_LABEL, this.width / 2 - 153, 70, 10526880);
         this.posXEdit.render(guigraphics, i, j, f);
         this.posYEdit.render(guigraphics, i, j, f);
         this.posZEdit.render(guigraphics, i, j, f);
         guigraphics.drawString(this.font, INCLUDE_ENTITIES_LABEL, this.width / 2 + 154 - this.font.width(INCLUDE_ENTITIES_LABEL), 150, 10526880);
      }

      if (structuremode == StructureMode.SAVE) {
         guigraphics.drawString(this.font, SIZE_LABEL, this.width / 2 - 153, 110, 10526880);
         this.sizeXEdit.render(guigraphics, i, j, f);
         this.sizeYEdit.render(guigraphics, i, j, f);
         this.sizeZEdit.render(guigraphics, i, j, f);
         guigraphics.drawString(this.font, DETECT_SIZE_LABEL, this.width / 2 + 154 - this.font.width(DETECT_SIZE_LABEL), 110, 10526880);
         guigraphics.drawString(this.font, SHOW_AIR_LABEL, this.width / 2 + 154 - this.font.width(SHOW_AIR_LABEL), 70, 10526880);
      }

      if (structuremode == StructureMode.LOAD) {
         guigraphics.drawString(this.font, INTEGRITY_LABEL, this.width / 2 - 153, 110, 10526880);
         this.integrityEdit.render(guigraphics, i, j, f);
         this.seedEdit.render(guigraphics, i, j, f);
         guigraphics.drawString(this.font, SHOW_BOUNDING_BOX_LABEL, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_LABEL), 70, 10526880);
      }

      if (structuremode == StructureMode.DATA) {
         guigraphics.drawString(this.font, CUSTOM_DATA_LABEL, this.width / 2 - 153, 110, 10526880);
         this.dataEdit.render(guigraphics, i, j, f);
      }

      guigraphics.drawString(this.font, structuremode.getDisplayName(), this.width / 2 - 153, 174, 10526880);
      super.render(guigraphics, i, j, f);
   }

   public boolean isPauseScreen() {
      return false;
   }
}
