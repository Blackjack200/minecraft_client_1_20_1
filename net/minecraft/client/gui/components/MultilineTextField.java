package net.minecraft.client.gui.components;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;

public class MultilineTextField {
   public static final int NO_CHARACTER_LIMIT = Integer.MAX_VALUE;
   private static final int LINE_SEEK_PIXEL_BIAS = 2;
   private final Font font;
   private final List<MultilineTextField.StringView> displayLines = Lists.newArrayList();
   private String value;
   private int cursor;
   private int selectCursor;
   private boolean selecting;
   private int characterLimit = Integer.MAX_VALUE;
   private final int width;
   private Consumer<String> valueListener = (s) -> {
   };
   private Runnable cursorListener = () -> {
   };

   public MultilineTextField(Font font, int i) {
      this.font = font;
      this.width = i;
      this.setValue("");
   }

   public int characterLimit() {
      return this.characterLimit;
   }

   public void setCharacterLimit(int i) {
      if (i < 0) {
         throw new IllegalArgumentException("Character limit cannot be negative");
      } else {
         this.characterLimit = i;
      }
   }

   public boolean hasCharacterLimit() {
      return this.characterLimit != Integer.MAX_VALUE;
   }

   public void setValueListener(Consumer<String> consumer) {
      this.valueListener = consumer;
   }

   public void setCursorListener(Runnable runnable) {
      this.cursorListener = runnable;
   }

   public void setValue(String s) {
      this.value = this.truncateFullText(s);
      this.cursor = this.value.length();
      this.selectCursor = this.cursor;
      this.onValueChange();
   }

   public String value() {
      return this.value;
   }

   public void insertText(String s) {
      if (!s.isEmpty() || this.hasSelection()) {
         String s1 = this.truncateInsertionText(SharedConstants.filterText(s, true));
         MultilineTextField.StringView multilinetextfield_stringview = this.getSelected();
         this.value = (new StringBuilder(this.value)).replace(multilinetextfield_stringview.beginIndex, multilinetextfield_stringview.endIndex, s1).toString();
         this.cursor = multilinetextfield_stringview.beginIndex + s1.length();
         this.selectCursor = this.cursor;
         this.onValueChange();
      }
   }

   public void deleteText(int i) {
      if (!this.hasSelection()) {
         this.selectCursor = Mth.clamp(this.cursor + i, 0, this.value.length());
      }

      this.insertText("");
   }

   public int cursor() {
      return this.cursor;
   }

   public void setSelecting(boolean flag) {
      this.selecting = flag;
   }

   public MultilineTextField.StringView getSelected() {
      return new MultilineTextField.StringView(Math.min(this.selectCursor, this.cursor), Math.max(this.selectCursor, this.cursor));
   }

   public int getLineCount() {
      return this.displayLines.size();
   }

   public int getLineAtCursor() {
      for(int i = 0; i < this.displayLines.size(); ++i) {
         MultilineTextField.StringView multilinetextfield_stringview = this.displayLines.get(i);
         if (this.cursor >= multilinetextfield_stringview.beginIndex && this.cursor <= multilinetextfield_stringview.endIndex) {
            return i;
         }
      }

      return -1;
   }

   public MultilineTextField.StringView getLineView(int i) {
      return this.displayLines.get(Mth.clamp(i, 0, this.displayLines.size() - 1));
   }

   public void seekCursor(Whence whence, int i) {
      switch (whence) {
         case ABSOLUTE:
            this.cursor = i;
            break;
         case RELATIVE:
            this.cursor += i;
            break;
         case END:
            this.cursor = this.value.length() + i;
      }

      this.cursor = Mth.clamp(this.cursor, 0, this.value.length());
      this.cursorListener.run();
      if (!this.selecting) {
         this.selectCursor = this.cursor;
      }

   }

   public void seekCursorLine(int i) {
      if (i != 0) {
         int j = this.font.width(this.value.substring(this.getCursorLineView().beginIndex, this.cursor)) + 2;
         MultilineTextField.StringView multilinetextfield_stringview = this.getCursorLineView(i);
         int k = this.font.plainSubstrByWidth(this.value.substring(multilinetextfield_stringview.beginIndex, multilinetextfield_stringview.endIndex), j).length();
         this.seekCursor(Whence.ABSOLUTE, multilinetextfield_stringview.beginIndex + k);
      }
   }

   public void seekCursorToPoint(double d0, double d1) {
      int i = Mth.floor(d0);
      int j = Mth.floor(d1 / 9.0D);
      MultilineTextField.StringView multilinetextfield_stringview = this.displayLines.get(Mth.clamp(j, 0, this.displayLines.size() - 1));
      int k = this.font.plainSubstrByWidth(this.value.substring(multilinetextfield_stringview.beginIndex, multilinetextfield_stringview.endIndex), i).length();
      this.seekCursor(Whence.ABSOLUTE, multilinetextfield_stringview.beginIndex + k);
   }

   public boolean keyPressed(int i) {
      this.selecting = Screen.hasShiftDown();
      if (Screen.isSelectAll(i)) {
         this.cursor = this.value.length();
         this.selectCursor = 0;
         return true;
      } else if (Screen.isCopy(i)) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
         return true;
      } else if (Screen.isPaste(i)) {
         this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
         return true;
      } else if (Screen.isCut(i)) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
         this.insertText("");
         return true;
      } else {
         switch (i) {
            case 257:
            case 335:
               this.insertText("\n");
               return true;
            case 259:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield_stringview2 = this.getPreviousWord();
                  this.deleteText(multilinetextfield_stringview2.beginIndex - this.cursor);
               } else {
                  this.deleteText(-1);
               }

               return true;
            case 261:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield_stringview3 = this.getNextWord();
                  this.deleteText(multilinetextfield_stringview3.beginIndex - this.cursor);
               } else {
                  this.deleteText(1);
               }

               return true;
            case 262:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield_stringview1 = this.getNextWord();
                  this.seekCursor(Whence.ABSOLUTE, multilinetextfield_stringview1.beginIndex);
               } else {
                  this.seekCursor(Whence.RELATIVE, 1);
               }

               return true;
            case 263:
               if (Screen.hasControlDown()) {
                  MultilineTextField.StringView multilinetextfield_stringview = this.getPreviousWord();
                  this.seekCursor(Whence.ABSOLUTE, multilinetextfield_stringview.beginIndex);
               } else {
                  this.seekCursor(Whence.RELATIVE, -1);
               }

               return true;
            case 264:
               if (!Screen.hasControlDown()) {
                  this.seekCursorLine(1);
               }

               return true;
            case 265:
               if (!Screen.hasControlDown()) {
                  this.seekCursorLine(-1);
               }

               return true;
            case 266:
               this.seekCursor(Whence.ABSOLUTE, 0);
               return true;
            case 267:
               this.seekCursor(Whence.END, 0);
               return true;
            case 268:
               if (Screen.hasControlDown()) {
                  this.seekCursor(Whence.ABSOLUTE, 0);
               } else {
                  this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().beginIndex);
               }

               return true;
            case 269:
               if (Screen.hasControlDown()) {
                  this.seekCursor(Whence.END, 0);
               } else {
                  this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().endIndex);
               }

               return true;
            default:
               return false;
         }
      }
   }

   public Iterable<MultilineTextField.StringView> iterateLines() {
      return this.displayLines;
   }

   public boolean hasSelection() {
      return this.selectCursor != this.cursor;
   }

   @VisibleForTesting
   public String getSelectedText() {
      MultilineTextField.StringView multilinetextfield_stringview = this.getSelected();
      return this.value.substring(multilinetextfield_stringview.beginIndex, multilinetextfield_stringview.endIndex);
   }

   private MultilineTextField.StringView getCursorLineView() {
      return this.getCursorLineView(0);
   }

   private MultilineTextField.StringView getCursorLineView(int i) {
      int j = this.getLineAtCursor();
      if (j < 0) {
         throw new IllegalStateException("Cursor is not within text (cursor = " + this.cursor + ", length = " + this.value.length() + ")");
      } else {
         return this.displayLines.get(Mth.clamp(j + i, 0, this.displayLines.size() - 1));
      }
   }

   @VisibleForTesting
   public MultilineTextField.StringView getPreviousWord() {
      if (this.value.isEmpty()) {
         return MultilineTextField.StringView.EMPTY;
      } else {
         int i;
         for(i = Mth.clamp(this.cursor, 0, this.value.length() - 1); i > 0 && Character.isWhitespace(this.value.charAt(i - 1)); --i) {
         }

         while(i > 0 && !Character.isWhitespace(this.value.charAt(i - 1))) {
            --i;
         }

         return new MultilineTextField.StringView(i, this.getWordEndPosition(i));
      }
   }

   @VisibleForTesting
   public MultilineTextField.StringView getNextWord() {
      if (this.value.isEmpty()) {
         return MultilineTextField.StringView.EMPTY;
      } else {
         int i;
         for(i = Mth.clamp(this.cursor, 0, this.value.length() - 1); i < this.value.length() && !Character.isWhitespace(this.value.charAt(i)); ++i) {
         }

         while(i < this.value.length() && Character.isWhitespace(this.value.charAt(i))) {
            ++i;
         }

         return new MultilineTextField.StringView(i, this.getWordEndPosition(i));
      }
   }

   private int getWordEndPosition(int i) {
      int j;
      for(j = i; j < this.value.length() && !Character.isWhitespace(this.value.charAt(j)); ++j) {
      }

      return j;
   }

   private void onValueChange() {
      this.reflowDisplayLines();
      this.valueListener.accept(this.value);
      this.cursorListener.run();
   }

   private void reflowDisplayLines() {
      this.displayLines.clear();
      if (this.value.isEmpty()) {
         this.displayLines.add(MultilineTextField.StringView.EMPTY);
      } else {
         this.font.getSplitter().splitLines(this.value, this.width, Style.EMPTY, false, (style, i, j) -> this.displayLines.add(new MultilineTextField.StringView(i, j)));
         if (this.value.charAt(this.value.length() - 1) == '\n') {
            this.displayLines.add(new MultilineTextField.StringView(this.value.length(), this.value.length()));
         }

      }
   }

   private String truncateFullText(String s) {
      return this.hasCharacterLimit() ? StringUtil.truncateStringIfNecessary(s, this.characterLimit, false) : s;
   }

   private String truncateInsertionText(String s) {
      if (this.hasCharacterLimit()) {
         int i = this.characterLimit - this.value.length();
         return StringUtil.truncateStringIfNecessary(s, i, false);
      } else {
         return s;
      }
   }

   protected static record StringView(int beginIndex, int endIndex) {
      final int beginIndex;
      final int endIndex;
      static final MultilineTextField.StringView EMPTY = new MultilineTextField.StringView(0, 0);
   }
}
