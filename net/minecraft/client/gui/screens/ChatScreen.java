package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

public class ChatScreen extends Screen {
   public static final double MOUSE_SCROLL_SPEED = 7.0D;
   private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
   private static final int TOOLTIP_MAX_WIDTH = 210;
   private String historyBuffer = "";
   private int historyPos = -1;
   protected EditBox input;
   private String initial;
   CommandSuggestions commandSuggestions;

   public ChatScreen(String s) {
      super(Component.translatable("chat_screen.title"));
      this.initial = s;
   }

   protected void init() {
      this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
      this.input = new EditBox(this.minecraft.fontFilterFishy, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
         protected MutableComponent createNarrationMessage() {
            return super.createNarrationMessage().append(ChatScreen.this.commandSuggestions.getNarrationMessage());
         }
      };
      this.input.setMaxLength(256);
      this.input.setBordered(false);
      this.input.setValue(this.initial);
      this.input.setResponder(this::onEdited);
      this.input.setCanLoseFocus(false);
      this.addWidget(this.input);
      this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
      this.commandSuggestions.updateCommandInfo();
      this.setInitialFocus(this.input);
   }

   public void resize(Minecraft minecraft, int i, int j) {
      String s = this.input.getValue();
      this.init(minecraft, i, j);
      this.setChatLine(s);
      this.commandSuggestions.updateCommandInfo();
   }

   public void removed() {
      this.minecraft.gui.getChat().resetChatScroll();
   }

   public void tick() {
      this.input.tick();
   }

   private void onEdited(String s) {
      String s1 = this.input.getValue();
      this.commandSuggestions.setAllowSuggestions(!s1.equals(this.initial));
      this.commandSuggestions.updateCommandInfo();
   }

   public boolean keyPressed(int i, int j, int k) {
      if (this.commandSuggestions.keyPressed(i, j, k)) {
         return true;
      } else if (super.keyPressed(i, j, k)) {
         return true;
      } else if (i == 256) {
         this.minecraft.setScreen((Screen)null);
         return true;
      } else if (i != 257 && i != 335) {
         if (i == 265) {
            this.moveInHistory(-1);
            return true;
         } else if (i == 264) {
            this.moveInHistory(1);
            return true;
         } else if (i == 266) {
            this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
            return true;
         } else if (i == 267) {
            this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
            return true;
         } else {
            return false;
         }
      } else {
         if (this.handleChatInput(this.input.getValue(), true)) {
            this.minecraft.setScreen((Screen)null);
         }

         return true;
      }
   }

   public boolean mouseScrolled(double d0, double d1, double d2) {
      d2 = Mth.clamp(d2, -1.0D, 1.0D);
      if (this.commandSuggestions.mouseScrolled(d2)) {
         return true;
      } else {
         if (!hasShiftDown()) {
            d2 *= 7.0D;
         }

         this.minecraft.gui.getChat().scrollChat((int)d2);
         return true;
      }
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (this.commandSuggestions.mouseClicked((double)((int)d0), (double)((int)d1), i)) {
         return true;
      } else {
         if (i == 0) {
            ChatComponent chatcomponent = this.minecraft.gui.getChat();
            if (chatcomponent.handleChatQueueClicked(d0, d1)) {
               return true;
            }

            Style style = this.getComponentStyleAt(d0, d1);
            if (style != null && this.handleComponentClicked(style)) {
               this.initial = this.input.getValue();
               return true;
            }
         }

         return this.input.mouseClicked(d0, d1, i) ? true : super.mouseClicked(d0, d1, i);
      }
   }

   protected void insertText(String s, boolean flag) {
      if (flag) {
         this.input.setValue(s);
      } else {
         this.input.insertText(s);
      }

   }

   public void moveInHistory(int i) {
      int j = this.historyPos + i;
      int k = this.minecraft.gui.getChat().getRecentChat().size();
      j = Mth.clamp(j, 0, k);
      if (j != this.historyPos) {
         if (j == k) {
            this.historyPos = k;
            this.input.setValue(this.historyBuffer);
         } else {
            if (this.historyPos == k) {
               this.historyBuffer = this.input.getValue();
            }

            this.input.setValue(this.minecraft.gui.getChat().getRecentChat().get(j));
            this.commandSuggestions.setAllowSuggestions(false);
            this.historyPos = j;
         }
      }
   }

   public void render(GuiGraphics guigraphics, int i, int j, float f) {
      guigraphics.fill(2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
      this.input.render(guigraphics, i, j, f);
      super.render(guigraphics, i, j, f);
      this.commandSuggestions.render(guigraphics, i, j);
      GuiMessageTag guimessagetag = this.minecraft.gui.getChat().getMessageTagAt((double)i, (double)j);
      if (guimessagetag != null && guimessagetag.text() != null) {
         guigraphics.renderTooltip(this.font, this.font.split(guimessagetag.text(), 210), i, j);
      } else {
         Style style = this.getComponentStyleAt((double)i, (double)j);
         if (style != null && style.getHoverEvent() != null) {
            guigraphics.renderComponentHoverEffect(this.font, style, i, j);
         }
      }

   }

   public boolean isPauseScreen() {
      return false;
   }

   private void setChatLine(String s) {
      this.input.setValue(s);
   }

   protected void updateNarrationState(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, this.getTitle());
      narrationelementoutput.add(NarratedElementType.USAGE, USAGE_TEXT);
      String s = this.input.getValue();
      if (!s.isEmpty()) {
         narrationelementoutput.nest().add(NarratedElementType.TITLE, (Component)Component.translatable("chat_screen.message", s));
      }

   }

   @Nullable
   private Style getComponentStyleAt(double d0, double d1) {
      return this.minecraft.gui.getChat().getClickedComponentStyleAt(d0, d1);
   }

   public boolean handleChatInput(String s, boolean flag) {
      s = this.normalizeChatMessage(s);
      if (s.isEmpty()) {
         return true;
      } else {
         if (flag) {
            this.minecraft.gui.getChat().addRecentChat(s);
         }

         if (s.startsWith("/")) {
            this.minecraft.player.connection.sendCommand(s.substring(1));
         } else {
            this.minecraft.player.connection.sendChat(s);
         }

         return true;
      }
   }

   public String normalizeChatMessage(String s) {
      return StringUtil.trimChatMessage(StringUtils.normalizeSpace(s.trim()));
   }
}
