package net.minecraft.client.gui.font;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

public class TextFieldHelper {
   private final Supplier<String> getMessageFn;
   private final Consumer<String> setMessageFn;
   private final Supplier<String> getClipboardFn;
   private final Consumer<String> setClipboardFn;
   private final Predicate<String> stringValidator;
   private int cursorPos;
   private int selectionPos;

   public TextFieldHelper(Supplier<String> supplier, Consumer<String> consumer, Supplier<String> supplier1, Consumer<String> consumer1, Predicate<String> predicate) {
      this.getMessageFn = supplier;
      this.setMessageFn = consumer;
      this.getClipboardFn = supplier1;
      this.setClipboardFn = consumer1;
      this.stringValidator = predicate;
      this.setCursorToEnd();
   }

   public static Supplier<String> createClipboardGetter(Minecraft minecraft) {
      return () -> getClipboardContents(minecraft);
   }

   public static String getClipboardContents(Minecraft minecraft) {
      return ChatFormatting.stripFormatting(minecraft.keyboardHandler.getClipboard().replaceAll("\\r", ""));
   }

   public static Consumer<String> createClipboardSetter(Minecraft minecraft) {
      return (s) -> setClipboardContents(minecraft, s);
   }

   public static void setClipboardContents(Minecraft minecraft, String s) {
      minecraft.keyboardHandler.setClipboard(s);
   }

   public boolean charTyped(char c0) {
      if (SharedConstants.isAllowedChatCharacter(c0)) {
         this.insertText(this.getMessageFn.get(), Character.toString(c0));
      }

      return true;
   }

   public boolean keyPressed(int i) {
      if (Screen.isSelectAll(i)) {
         this.selectAll();
         return true;
      } else if (Screen.isCopy(i)) {
         this.copy();
         return true;
      } else if (Screen.isPaste(i)) {
         this.paste();
         return true;
      } else if (Screen.isCut(i)) {
         this.cut();
         return true;
      } else {
         TextFieldHelper.CursorStep textfieldhelper_cursorstep = Screen.hasControlDown() ? TextFieldHelper.CursorStep.WORD : TextFieldHelper.CursorStep.CHARACTER;
         if (i == 259) {
            this.removeFromCursor(-1, textfieldhelper_cursorstep);
            return true;
         } else {
            if (i == 261) {
               this.removeFromCursor(1, textfieldhelper_cursorstep);
            } else {
               if (i == 263) {
                  this.moveBy(-1, Screen.hasShiftDown(), textfieldhelper_cursorstep);
                  return true;
               }

               if (i == 262) {
                  this.moveBy(1, Screen.hasShiftDown(), textfieldhelper_cursorstep);
                  return true;
               }

               if (i == 268) {
                  this.setCursorToStart(Screen.hasShiftDown());
                  return true;
               }

               if (i == 269) {
                  this.setCursorToEnd(Screen.hasShiftDown());
                  return true;
               }
            }

            return false;
         }
      }
   }

   private int clampToMsgLength(int i) {
      return Mth.clamp(i, 0, this.getMessageFn.get().length());
   }

   private void insertText(String s, String s1) {
      if (this.selectionPos != this.cursorPos) {
         s = this.deleteSelection(s);
      }

      this.cursorPos = Mth.clamp(this.cursorPos, 0, s.length());
      String s2 = (new StringBuilder(s)).insert(this.cursorPos, s1).toString();
      if (this.stringValidator.test(s2)) {
         this.setMessageFn.accept(s2);
         this.selectionPos = this.cursorPos = Math.min(s2.length(), this.cursorPos + s1.length());
      }

   }

   public void insertText(String s) {
      this.insertText(this.getMessageFn.get(), s);
   }

   private void resetSelectionIfNeeded(boolean flag) {
      if (!flag) {
         this.selectionPos = this.cursorPos;
      }

   }

   public void moveBy(int i, boolean flag, TextFieldHelper.CursorStep textfieldhelper_cursorstep) {
      switch (textfieldhelper_cursorstep) {
         case CHARACTER:
            this.moveByChars(i, flag);
            break;
         case WORD:
            this.moveByWords(i, flag);
      }

   }

   public void moveByChars(int i) {
      this.moveByChars(i, false);
   }

   public void moveByChars(int i, boolean flag) {
      this.cursorPos = Util.offsetByCodepoints(this.getMessageFn.get(), this.cursorPos, i);
      this.resetSelectionIfNeeded(flag);
   }

   public void moveByWords(int i) {
      this.moveByWords(i, false);
   }

   public void moveByWords(int i, boolean flag) {
      this.cursorPos = StringSplitter.getWordPosition(this.getMessageFn.get(), i, this.cursorPos, true);
      this.resetSelectionIfNeeded(flag);
   }

   public void removeFromCursor(int i, TextFieldHelper.CursorStep textfieldhelper_cursorstep) {
      switch (textfieldhelper_cursorstep) {
         case CHARACTER:
            this.removeCharsFromCursor(i);
            break;
         case WORD:
            this.removeWordsFromCursor(i);
      }

   }

   public void removeWordsFromCursor(int i) {
      int j = StringSplitter.getWordPosition(this.getMessageFn.get(), i, this.cursorPos, true);
      this.removeCharsFromCursor(j - this.cursorPos);
   }

   public void removeCharsFromCursor(int i) {
      String s = this.getMessageFn.get();
      if (!s.isEmpty()) {
         String s1;
         if (this.selectionPos != this.cursorPos) {
            s1 = this.deleteSelection(s);
         } else {
            int j = Util.offsetByCodepoints(s, this.cursorPos, i);
            int k = Math.min(j, this.cursorPos);
            int l = Math.max(j, this.cursorPos);
            s1 = (new StringBuilder(s)).delete(k, l).toString();
            if (i < 0) {
               this.selectionPos = this.cursorPos = k;
            }
         }

         this.setMessageFn.accept(s1);
      }

   }

   public void cut() {
      String s = this.getMessageFn.get();
      this.setClipboardFn.accept(this.getSelected(s));
      this.setMessageFn.accept(this.deleteSelection(s));
   }

   public void paste() {
      this.insertText(this.getMessageFn.get(), this.getClipboardFn.get());
      this.selectionPos = this.cursorPos;
   }

   public void copy() {
      this.setClipboardFn.accept(this.getSelected(this.getMessageFn.get()));
   }

   public void selectAll() {
      this.selectionPos = 0;
      this.cursorPos = this.getMessageFn.get().length();
   }

   private String getSelected(String s) {
      int i = Math.min(this.cursorPos, this.selectionPos);
      int j = Math.max(this.cursorPos, this.selectionPos);
      return s.substring(i, j);
   }

   private String deleteSelection(String s) {
      if (this.selectionPos == this.cursorPos) {
         return s;
      } else {
         int i = Math.min(this.cursorPos, this.selectionPos);
         int j = Math.max(this.cursorPos, this.selectionPos);
         String s1 = s.substring(0, i) + s.substring(j);
         this.selectionPos = this.cursorPos = i;
         return s1;
      }
   }

   public void setCursorToStart() {
      this.setCursorToStart(false);
   }

   public void setCursorToStart(boolean flag) {
      this.cursorPos = 0;
      this.resetSelectionIfNeeded(flag);
   }

   public void setCursorToEnd() {
      this.setCursorToEnd(false);
   }

   public void setCursorToEnd(boolean flag) {
      this.cursorPos = this.getMessageFn.get().length();
      this.resetSelectionIfNeeded(flag);
   }

   public int getCursorPos() {
      return this.cursorPos;
   }

   public void setCursorPos(int i) {
      this.setCursorPos(i, true);
   }

   public void setCursorPos(int i, boolean flag) {
      this.cursorPos = this.clampToMsgLength(i);
      this.resetSelectionIfNeeded(flag);
   }

   public int getSelectionPos() {
      return this.selectionPos;
   }

   public void setSelectionPos(int i) {
      this.selectionPos = this.clampToMsgLength(i);
   }

   public void setSelectionRange(int i, int j) {
      int k = this.getMessageFn.get().length();
      this.cursorPos = Mth.clamp(i, 0, k);
      this.selectionPos = Mth.clamp(j, 0, k);
   }

   public boolean isSelecting() {
      return this.cursorPos != this.selectionPos;
   }

   public static enum CursorStep {
      CHARACTER,
      WORD;
   }
}
