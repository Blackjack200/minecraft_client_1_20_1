package net.minecraft.client.gui.components;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class EditBox extends AbstractWidget implements Renderable {
   public static final int BACKWARDS = -1;
   public static final int FORWARDS = 1;
   private static final int CURSOR_INSERT_WIDTH = 1;
   private static final int CURSOR_INSERT_COLOR = -3092272;
   private static final String CURSOR_APPEND_CHARACTER = "_";
   public static final int DEFAULT_TEXT_COLOR = 14737632;
   private static final int BORDER_COLOR_FOCUSED = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = -16777216;
   private final Font font;
   private String value = "";
   private int maxLength = 32;
   private int frame;
   private boolean bordered = true;
   private boolean canLoseFocus = true;
   private boolean isEditable = true;
   private boolean shiftPressed;
   private int displayPos;
   private int cursorPos;
   private int highlightPos;
   private int textColor = 14737632;
   private int textColorUneditable = 7368816;
   @Nullable
   private String suggestion;
   @Nullable
   private Consumer<String> responder;
   private Predicate<String> filter = Objects::nonNull;
   private BiFunction<String, Integer, FormattedCharSequence> formatter = (s, integer) -> FormattedCharSequence.forward(s, Style.EMPTY);
   @Nullable
   private Component hint;

   public EditBox(Font font, int i, int j, int k, int l, Component component) {
      this(font, i, j, k, l, (EditBox)null, component);
   }

   public EditBox(Font font, int i, int j, int k, int l, @Nullable EditBox editbox, Component component) {
      super(i, j, k, l, component);
      this.font = font;
      if (editbox != null) {
         this.setValue(editbox.getValue());
      }

   }

   public void setResponder(Consumer<String> consumer) {
      this.responder = consumer;
   }

   public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> bifunction) {
      this.formatter = bifunction;
   }

   public void tick() {
      ++this.frame;
   }

   protected MutableComponent createNarrationMessage() {
      Component component = this.getMessage();
      return Component.translatable("gui.narrate.editBox", component, this.value);
   }

   public void setValue(String s) {
      if (this.filter.test(s)) {
         if (s.length() > this.maxLength) {
            this.value = s.substring(0, this.maxLength);
         } else {
            this.value = s;
         }

         this.moveCursorToEnd();
         this.setHighlightPos(this.cursorPos);
         this.onValueChange(s);
      }
   }

   public String getValue() {
      return this.value;
   }

   public String getHighlighted() {
      int i = Math.min(this.cursorPos, this.highlightPos);
      int j = Math.max(this.cursorPos, this.highlightPos);
      return this.value.substring(i, j);
   }

   public void setFilter(Predicate<String> predicate) {
      this.filter = predicate;
   }

   public void insertText(String s) {
      int i = Math.min(this.cursorPos, this.highlightPos);
      int j = Math.max(this.cursorPos, this.highlightPos);
      int k = this.maxLength - this.value.length() - (i - j);
      String s1 = SharedConstants.filterText(s);
      int l = s1.length();
      if (k < l) {
         s1 = s1.substring(0, k);
         l = k;
      }

      String s2 = (new StringBuilder(this.value)).replace(i, j, s1).toString();
      if (this.filter.test(s2)) {
         this.value = s2;
         this.setCursorPosition(i + l);
         this.setHighlightPos(this.cursorPos);
         this.onValueChange(this.value);
      }
   }

   private void onValueChange(String s) {
      if (this.responder != null) {
         this.responder.accept(s);
      }

   }

   private void deleteText(int i) {
      if (Screen.hasControlDown()) {
         this.deleteWords(i);
      } else {
         this.deleteChars(i);
      }

   }

   public void deleteWords(int i) {
      if (!this.value.isEmpty()) {
         if (this.highlightPos != this.cursorPos) {
            this.insertText("");
         } else {
            this.deleteChars(this.getWordPosition(i) - this.cursorPos);
         }
      }
   }

   public void deleteChars(int i) {
      if (!this.value.isEmpty()) {
         if (this.highlightPos != this.cursorPos) {
            this.insertText("");
         } else {
            int j = this.getCursorPos(i);
            int k = Math.min(j, this.cursorPos);
            int l = Math.max(j, this.cursorPos);
            if (k != l) {
               String s = (new StringBuilder(this.value)).delete(k, l).toString();
               if (this.filter.test(s)) {
                  this.value = s;
                  this.moveCursorTo(k);
               }
            }
         }
      }
   }

   public int getWordPosition(int i) {
      return this.getWordPosition(i, this.getCursorPosition());
   }

   private int getWordPosition(int i, int j) {
      return this.getWordPosition(i, j, true);
   }

   private int getWordPosition(int i, int j, boolean flag) {
      int k = j;
      boolean flag1 = i < 0;
      int l = Math.abs(i);

      for(int i1 = 0; i1 < l; ++i1) {
         if (!flag1) {
            int j1 = this.value.length();
            k = this.value.indexOf(32, k);
            if (k == -1) {
               k = j1;
            } else {
               while(flag && k < j1 && this.value.charAt(k) == ' ') {
                  ++k;
               }
            }
         } else {
            while(flag && k > 0 && this.value.charAt(k - 1) == ' ') {
               --k;
            }

            while(k > 0 && this.value.charAt(k - 1) != ' ') {
               --k;
            }
         }
      }

      return k;
   }

   public void moveCursor(int i) {
      this.moveCursorTo(this.getCursorPos(i));
   }

   private int getCursorPos(int i) {
      return Util.offsetByCodepoints(this.value, this.cursorPos, i);
   }

   public void moveCursorTo(int i) {
      this.setCursorPosition(i);
      if (!this.shiftPressed) {
         this.setHighlightPos(this.cursorPos);
      }

      this.onValueChange(this.value);
   }

   public void setCursorPosition(int i) {
      this.cursorPos = Mth.clamp(i, 0, this.value.length());
   }

   public void moveCursorToStart() {
      this.moveCursorTo(0);
   }

   public void moveCursorToEnd() {
      this.moveCursorTo(this.value.length());
   }

   public boolean keyPressed(int i, int j, int k) {
      if (!this.canConsumeInput()) {
         return false;
      } else {
         this.shiftPressed = Screen.hasShiftDown();
         if (Screen.isSelectAll(i)) {
            this.moveCursorToEnd();
            this.setHighlightPos(0);
            return true;
         } else if (Screen.isCopy(i)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
         } else if (Screen.isPaste(i)) {
            if (this.isEditable) {
               this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }

            return true;
         } else if (Screen.isCut(i)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (this.isEditable) {
               this.insertText("");
            }

            return true;
         } else {
            switch (i) {
               case 259:
                  if (this.isEditable) {
                     this.shiftPressed = false;
                     this.deleteText(-1);
                     this.shiftPressed = Screen.hasShiftDown();
                  }

                  return true;
               case 260:
               case 264:
               case 265:
               case 266:
               case 267:
               default:
                  return false;
               case 261:
                  if (this.isEditable) {
                     this.shiftPressed = false;
                     this.deleteText(1);
                     this.shiftPressed = Screen.hasShiftDown();
                  }

                  return true;
               case 262:
                  if (Screen.hasControlDown()) {
                     this.moveCursorTo(this.getWordPosition(1));
                  } else {
                     this.moveCursor(1);
                  }

                  return true;
               case 263:
                  if (Screen.hasControlDown()) {
                     this.moveCursorTo(this.getWordPosition(-1));
                  } else {
                     this.moveCursor(-1);
                  }

                  return true;
               case 268:
                  this.moveCursorToStart();
                  return true;
               case 269:
                  this.moveCursorToEnd();
                  return true;
            }
         }
      }
   }

   public boolean canConsumeInput() {
      return this.isVisible() && this.isFocused() && this.isEditable();
   }

   public boolean charTyped(char c0, int i) {
      if (!this.canConsumeInput()) {
         return false;
      } else if (SharedConstants.isAllowedChatCharacter(c0)) {
         if (this.isEditable) {
            this.insertText(Character.toString(c0));
         }

         return true;
      } else {
         return false;
      }
   }

   public void onClick(double d0, double d1) {
      int i = Mth.floor(d0) - this.getX();
      if (this.bordered) {
         i -= 4;
      }

      String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
      this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos);
   }

   public void playDownSound(SoundManager soundmanager) {
   }

   public void renderWidget(GuiGraphics guigraphics, int i, int j, float f) {
      if (this.isVisible()) {
         if (this.isBordered()) {
            int k = this.isFocused() ? -1 : -6250336;
            guigraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, k);
            guigraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
         }

         int l = this.isEditable ? this.textColor : this.textColorUneditable;
         int i1 = this.cursorPos - this.displayPos;
         int j1 = this.highlightPos - this.displayPos;
         String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
         boolean flag = i1 >= 0 && i1 <= s.length();
         boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
         int k1 = this.bordered ? this.getX() + 4 : this.getX();
         int l1 = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
         int i2 = k1;
         if (j1 > s.length()) {
            j1 = s.length();
         }

         if (!s.isEmpty()) {
            String s1 = flag ? s.substring(0, i1) : s;
            i2 = guigraphics.drawString(this.font, this.formatter.apply(s1, this.displayPos), k1, l1, l);
         }

         boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
         int j2 = i2;
         if (!flag) {
            j2 = i1 > 0 ? k1 + this.width : k1;
         } else if (flag2) {
            j2 = i2 - 1;
            --i2;
         }

         if (!s.isEmpty() && flag && i1 < s.length()) {
            guigraphics.drawString(this.font, this.formatter.apply(s.substring(i1), this.cursorPos), i2, l1, l);
         }

         if (this.hint != null && s.isEmpty() && !this.isFocused()) {
            guigraphics.drawString(this.font, this.hint, i2, l1, l);
         }

         if (!flag2 && this.suggestion != null) {
            guigraphics.drawString(this.font, this.suggestion, j2 - 1, l1, -8355712);
         }

         if (flag1) {
            if (flag2) {
               guigraphics.fill(RenderType.guiOverlay(), j2, l1 - 1, j2 + 1, l1 + 1 + 9, -3092272);
            } else {
               guigraphics.drawString(this.font, "_", j2, l1, l);
            }
         }

         if (j1 != i1) {
            int k2 = k1 + this.font.width(s.substring(0, j1));
            this.renderHighlight(guigraphics, j2, l1 - 1, k2 - 1, l1 + 1 + 9);
         }

      }
   }

   private void renderHighlight(GuiGraphics guigraphics, int i, int j, int k, int l) {
      if (i < k) {
         int i1 = i;
         i = k;
         k = i1;
      }

      if (j < l) {
         int j1 = j;
         j = l;
         l = j1;
      }

      if (k > this.getX() + this.width) {
         k = this.getX() + this.width;
      }

      if (i > this.getX() + this.width) {
         i = this.getX() + this.width;
      }

      guigraphics.fill(RenderType.guiTextHighlight(), i, j, k, l, -16776961);
   }

   public void setMaxLength(int i) {
      this.maxLength = i;
      if (this.value.length() > i) {
         this.value = this.value.substring(0, i);
         this.onValueChange(this.value);
      }

   }

   private int getMaxLength() {
      return this.maxLength;
   }

   public int getCursorPosition() {
      return this.cursorPos;
   }

   private boolean isBordered() {
      return this.bordered;
   }

   public void setBordered(boolean flag) {
      this.bordered = flag;
   }

   public void setTextColor(int i) {
      this.textColor = i;
   }

   public void setTextColorUneditable(int i) {
      this.textColorUneditable = i;
   }

   @Nullable
   public ComponentPath nextFocusPath(FocusNavigationEvent focusnavigationevent) {
      return this.visible && this.isEditable ? super.nextFocusPath(focusnavigationevent) : null;
   }

   public boolean isMouseOver(double d0, double d1) {
      return this.visible && d0 >= (double)this.getX() && d0 < (double)(this.getX() + this.width) && d1 >= (double)this.getY() && d1 < (double)(this.getY() + this.height);
   }

   public void setFocused(boolean flag) {
      if (this.canLoseFocus || flag) {
         super.setFocused(flag);
         if (flag) {
            this.frame = 0;
         }

      }
   }

   private boolean isEditable() {
      return this.isEditable;
   }

   public void setEditable(boolean flag) {
      this.isEditable = flag;
   }

   public int getInnerWidth() {
      return this.isBordered() ? this.width - 8 : this.width;
   }

   public void setHighlightPos(int i) {
      int j = this.value.length();
      this.highlightPos = Mth.clamp(i, 0, j);
      if (this.font != null) {
         if (this.displayPos > j) {
            this.displayPos = j;
         }

         int k = this.getInnerWidth();
         String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), k);
         int l = s.length() + this.displayPos;
         if (this.highlightPos == this.displayPos) {
            this.displayPos -= this.font.plainSubstrByWidth(this.value, k, true).length();
         }

         if (this.highlightPos > l) {
            this.displayPos += this.highlightPos - l;
         } else if (this.highlightPos <= this.displayPos) {
            this.displayPos -= this.displayPos - this.highlightPos;
         }

         this.displayPos = Mth.clamp(this.displayPos, 0, j);
      }

   }

   public void setCanLoseFocus(boolean flag) {
      this.canLoseFocus = flag;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean flag) {
      this.visible = flag;
   }

   public void setSuggestion(@Nullable String s) {
      this.suggestion = s;
   }

   public int getScreenX(int i) {
      return i > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, i));
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
   }

   public void setHint(Component component) {
      this.hint = component;
   }
}
