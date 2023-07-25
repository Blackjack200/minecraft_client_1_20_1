package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;

public abstract class AbstractSignEditScreen extends Screen {
   private final SignBlockEntity sign;
   private SignText text;
   private final String[] messages;
   private final boolean isFrontText;
   protected final WoodType woodType;
   private int frame;
   private int line;
   @Nullable
   private TextFieldHelper signField;

   public AbstractSignEditScreen(SignBlockEntity signblockentity, boolean flag, boolean flag1) {
      this(signblockentity, flag, flag1, Component.translatable("sign.edit"));
   }

   public AbstractSignEditScreen(SignBlockEntity signblockentity, boolean flag, boolean flag1, Component component) {
      super(component);
      this.sign = signblockentity;
      this.text = signblockentity.getText(flag);
      this.isFrontText = flag;
      this.woodType = SignBlock.getWoodType(signblockentity.getBlockState().getBlock());
      this.messages = IntStream.range(0, 4).mapToObj((j) -> this.text.getMessage(j, flag1)).map(Component::getString).toArray((i) -> new String[i]);
   }

   protected void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
      this.signField = new TextFieldHelper(() -> this.messages[this.line], this::setMessage, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), (s1) -> this.minecraft.font.width(s1) <= this.sign.getMaxTextLineWidth());
   }

   public void tick() {
      ++this.frame;
      if (!this.isValid()) {
         this.onDone();
      }

   }

   private boolean isValid() {
      return this.minecraft != null && this.minecraft.player != null && !this.sign.isRemoved() && !this.sign.playerIsTooFarAwayToEdit(this.minecraft.player.getUUID());
   }

   public boolean keyPressed(int i, int j, int k) {
      if (i == 265) {
         this.line = this.line - 1 & 3;
         this.signField.setCursorToEnd();
         return true;
      } else if (i != 264 && i != 257 && i != 335) {
         return this.signField.keyPressed(i) ? true : super.keyPressed(i, j, k);
      } else {
         this.line = this.line + 1 & 3;
         this.signField.setCursorToEnd();
         return true;
      }
   }

   public boolean charTyped(char c0, int i) {
      this.signField.charTyped(c0);
      return true;
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      Lighting.setupForFlatItems();
      this.renderBackground(guigraphics);
      guigraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
      this.renderSign(guigraphics);
      Lighting.setupFor3DItems();
      super.render(guigraphics, i, j, f);
   }

   public void onClose() {
      this.onDone();
   }

   public void removed() {
      ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
      if (clientpacketlistener != null) {
         clientpacketlistener.send(new ServerboundSignUpdatePacket(this.sign.getBlockPos(), this.isFrontText, this.messages[0], this.messages[1], this.messages[2], this.messages[3]));
      }

   }

   public boolean isPauseScreen() {
      return false;
   }

   protected abstract void renderSignBackground(GuiGraphics guigraphics, BlockState blockstate);

   protected abstract Vector3f getSignTextScale();

   protected void offsetSign(GuiGraphics guigraphics, BlockState blockstate) {
      guigraphics.pose().translate((float)this.width / 2.0F, 90.0F, 50.0F);
   }

   private void renderSign(GuiGraphics guigraphics) {
      BlockState blockstate = this.sign.getBlockState();
      guigraphics.pose().pushPose();
      this.offsetSign(guigraphics, blockstate);
      guigraphics.pose().pushPose();
      this.renderSignBackground(guigraphics, blockstate);
      guigraphics.pose().popPose();
      this.renderSignText(guigraphics);
      guigraphics.pose().popPose();
   }

   private void renderSignText(GuiGraphics guigraphics) {
      guigraphics.pose().translate(0.0F, 0.0F, 4.0F);
      Vector3f vector3f = this.getSignTextScale();
      guigraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());
      int i = this.text.getColor().getTextColor();
      boolean flag = this.frame / 6 % 2 == 0;
      int j = this.signField.getCursorPos();
      int k = this.signField.getSelectionPos();
      int l = 4 * this.sign.getTextLineHeight() / 2;
      int i1 = this.line * this.sign.getTextLineHeight() - l;

      for(int j1 = 0; j1 < this.messages.length; ++j1) {
         String s = this.messages[j1];
         if (s != null) {
            if (this.font.isBidirectional()) {
               s = this.font.bidirectionalShaping(s);
            }

            int k1 = -this.font.width(s) / 2;
            guigraphics.drawString(this.font, s, k1, j1 * this.sign.getTextLineHeight() - l, i, false);
            if (j1 == this.line && j >= 0 && flag) {
               int l1 = this.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));
               int i2 = l1 - this.font.width(s) / 2;
               if (j >= s.length()) {
                  guigraphics.drawString(this.font, "_", i2, i1, i, false);
               }
            }
         }
      }

      for(int j2 = 0; j2 < this.messages.length; ++j2) {
         String s1 = this.messages[j2];
         if (s1 != null && j2 == this.line && j >= 0) {
            int k2 = this.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
            int l2 = k2 - this.font.width(s1) / 2;
            if (flag && j < s1.length()) {
               guigraphics.fill(l2, i1 - 1, l2 + 1, i1 + this.sign.getTextLineHeight(), -16777216 | i);
            }

            if (k != j) {
               int i3 = Math.min(j, k);
               int j3 = Math.max(j, k);
               int k3 = this.font.width(s1.substring(0, i3)) - this.font.width(s1) / 2;
               int l3 = this.font.width(s1.substring(0, j3)) - this.font.width(s1) / 2;
               int i4 = Math.min(k3, l3);
               int j4 = Math.max(k3, l3);
               guigraphics.fill(RenderType.guiTextHighlight(), i4, i1, j4, i1 + this.sign.getTextLineHeight(), -16776961);
            }
         }
      }

   }

   private void setMessage(String s) {
      this.messages[this.line] = s;
      this.text = this.text.setMessage(this.line, Component.literal(s));
      this.sign.setText(this.text, this.isFrontText);
   }

   private void onDone() {
      this.minecraft.setScreen((Screen)null);
   }
}
