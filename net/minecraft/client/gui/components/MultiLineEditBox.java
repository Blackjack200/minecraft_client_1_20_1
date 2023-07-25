package net.minecraft.client.gui.components;

import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

public class MultiLineEditBox extends AbstractScrollWidget {
   private static final int CURSOR_INSERT_WIDTH = 1;
   private static final int CURSOR_INSERT_COLOR = -3092272;
   private static final String CURSOR_APPEND_CHARACTER = "_";
   private static final int TEXT_COLOR = -2039584;
   private static final int PLACEHOLDER_TEXT_COLOR = -857677600;
   private final Font font;
   private final Component placeholder;
   private final MultilineTextField textField;
   private int frame;

   public MultiLineEditBox(Font font, int i, int j, int k, int l, Component component, Component component1) {
      super(i, j, k, l, component1);
      this.font = font;
      this.placeholder = component;
      this.textField = new MultilineTextField(font, k - this.totalInnerPadding());
      this.textField.setCursorListener(this::scrollToCursor);
   }

   public void setCharacterLimit(int i) {
      this.textField.setCharacterLimit(i);
   }

   public void setValueListener(Consumer<String> consumer) {
      this.textField.setValueListener(consumer);
   }

   public void setValue(String s) {
      this.textField.setValue(s);
   }

   public String getValue() {
      return this.textField.value();
   }

   public void tick() {
      ++this.frame;
   }

   public void updateWidgetNarration(NarrationElementOutput narrationelementoutput) {
      narrationelementoutput.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
   }

   public boolean mouseClicked(double d0, double d1, int i) {
      if (super.mouseClicked(d0, d1, i)) {
         return true;
      } else if (this.withinContentAreaPoint(d0, d1) && i == 0) {
         this.textField.setSelecting(Screen.hasShiftDown());
         this.seekCursorScreen(d0, d1);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseDragged(double d0, double d1, int i, double d2, double d3) {
      if (super.mouseDragged(d0, d1, i, d2, d3)) {
         return true;
      } else if (this.withinContentAreaPoint(d0, d1) && i == 0) {
         this.textField.setSelecting(true);
         this.seekCursorScreen(d0, d1);
         this.textField.setSelecting(Screen.hasShiftDown());
         return true;
      } else {
         return false;
      }
   }

   public boolean keyPressed(int i, int j, int k) {
      return this.textField.keyPressed(i);
   }

   public boolean charTyped(char c0, int i) {
      if (this.visible && this.isFocused() && SharedConstants.isAllowedChatCharacter(c0)) {
         this.textField.insertText(Character.toString(c0));
         return true;
      } else {
         return false;
      }
   }

   protected void renderContents(GuiGraphics guigraphics, int i, int j, float f) {
      String s = this.textField.value();
      if (s.isEmpty() && !this.isFocused()) {
         guigraphics.drawWordWrap(this.font, this.placeholder, this.getX() + this.innerPadding(), this.getY() + this.innerPadding(), this.width - this.totalInnerPadding(), -857677600);
      } else {
         int k = this.textField.cursor();
         boolean flag = this.isFocused() && this.frame / 6 % 2 == 0;
         boolean flag1 = k < s.length();
         int l = 0;
         int i1 = 0;
         int j1 = this.getY() + this.innerPadding();

         for(MultilineTextField.StringView multilinetextfield_stringview : this.textField.iterateLines()) {
            boolean flag2 = this.withinContentAreaTopBottom(j1, j1 + 9);
            if (flag && flag1 && k >= multilinetextfield_stringview.beginIndex() && k <= multilinetextfield_stringview.endIndex()) {
               if (flag2) {
                  l = guigraphics.drawString(this.font, s.substring(multilinetextfield_stringview.beginIndex(), k), this.getX() + this.innerPadding(), j1, -2039584) - 1;
                  guigraphics.fill(l, j1 - 1, l + 1, j1 + 1 + 9, -3092272);
                  guigraphics.drawString(this.font, s.substring(k, multilinetextfield_stringview.endIndex()), l, j1, -2039584);
               }
            } else {
               if (flag2) {
                  l = guigraphics.drawString(this.font, s.substring(multilinetextfield_stringview.beginIndex(), multilinetextfield_stringview.endIndex()), this.getX() + this.innerPadding(), j1, -2039584) - 1;
               }

               i1 = j1;
            }

            j1 += 9;
         }

         if (flag && !flag1 && this.withinContentAreaTopBottom(i1, i1 + 9)) {
            guigraphics.drawString(this.font, "_", l, i1, -3092272);
         }

         if (this.textField.hasSelection()) {
            MultilineTextField.StringView multilinetextfield_stringview1 = this.textField.getSelected();
            int k1 = this.getX() + this.innerPadding();
            j1 = this.getY() + this.innerPadding();

            for(MultilineTextField.StringView multilinetextfield_stringview2 : this.textField.iterateLines()) {
               if (multilinetextfield_stringview1.beginIndex() > multilinetextfield_stringview2.endIndex()) {
                  j1 += 9;
               } else {
                  if (multilinetextfield_stringview2.beginIndex() > multilinetextfield_stringview1.endIndex()) {
                     break;
                  }

                  if (this.withinContentAreaTopBottom(j1, j1 + 9)) {
                     int l1 = this.font.width(s.substring(multilinetextfield_stringview2.beginIndex(), Math.max(multilinetextfield_stringview1.beginIndex(), multilinetextfield_stringview2.beginIndex())));
                     int i2;
                     if (multilinetextfield_stringview1.endIndex() > multilinetextfield_stringview2.endIndex()) {
                        i2 = this.width - this.innerPadding();
                     } else {
                        i2 = this.font.width(s.substring(multilinetextfield_stringview2.beginIndex(), multilinetextfield_stringview1.endIndex()));
                     }

                     this.renderHighlight(guigraphics, k1 + l1, j1, k1 + i2, j1 + 9);
                  }

                  j1 += 9;
               }
            }
         }

      }
   }

   protected void renderDecorations(GuiGraphics guigraphics) {
      super.renderDecorations(guigraphics);
      if (this.textField.hasCharacterLimit()) {
         int i = this.textField.characterLimit();
         Component component = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), i);
         guigraphics.drawString(this.font, component, this.getX() + this.width - this.font.width(component), this.getY() + this.height + 4, 10526880);
      }

   }

   public int getInnerHeight() {
      return 9 * this.textField.getLineCount();
   }

   protected boolean scrollbarVisible() {
      return (double)this.textField.getLineCount() > this.getDisplayableLineCount();
   }

   protected double scrollRate() {
      return 9.0D / 2.0D;
   }

   private void renderHighlight(GuiGraphics guigraphics, int i, int j, int k, int l) {
      guigraphics.fill(RenderType.guiTextHighlight(), i, j, k, l, -16776961);
   }

   private void scrollToCursor() {
      double d0 = this.scrollAmount();
      MultilineTextField.StringView multilinetextfield_stringview = this.textField.getLineView((int)(d0 / 9.0D));
      if (this.textField.cursor() <= multilinetextfield_stringview.beginIndex()) {
         d0 = (double)(this.textField.getLineAtCursor() * 9);
      } else {
         MultilineTextField.StringView multilinetextfield_stringview1 = this.textField.getLineView((int)((d0 + (double)this.height) / 9.0D) - 1);
         if (this.textField.cursor() > multilinetextfield_stringview1.endIndex()) {
            d0 = (double)(this.textField.getLineAtCursor() * 9 - this.height + 9 + this.totalInnerPadding());
         }
      }

      this.setScrollAmount(d0);
   }

   private double getDisplayableLineCount() {
      return (double)(this.height - this.totalInnerPadding()) / 9.0D;
   }

   private void seekCursorScreen(double d0, double d1) {
      double d2 = d0 - (double)this.getX() - (double)this.innerPadding();
      double d3 = d1 - (double)this.getY() - (double)this.innerPadding() + this.scrollAmount();
      this.textField.seekCursorToPoint(d2, d3);
   }
}
