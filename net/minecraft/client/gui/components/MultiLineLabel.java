package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public interface MultiLineLabel {
   MultiLineLabel EMPTY = new MultiLineLabel() {
      public int renderCentered(GuiGraphics guigraphics, int i, int j) {
         return j;
      }

      public int renderCentered(GuiGraphics guigraphics, int i, int j, int k, int l) {
         return j;
      }

      public int renderLeftAligned(GuiGraphics guigraphics, int i, int j, int k, int l) {
         return j;
      }

      public int renderLeftAlignedNoShadow(GuiGraphics guigraphics, int i, int j, int k, int l) {
         return j;
      }

      public void renderBackgroundCentered(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
      }

      public int getLineCount() {
         return 0;
      }

      public int getWidth() {
         return 0;
      }
   };

   static MultiLineLabel create(Font font, FormattedText formattedtext, int i) {
      return createFixed(font, font.split(formattedtext, i).stream().map((formattedcharsequence) -> new MultiLineLabel.TextWithWidth(formattedcharsequence, font.width(formattedcharsequence))).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel create(Font font, FormattedText formattedtext, int i, int j) {
      return createFixed(font, font.split(formattedtext, i).stream().limit((long)j).map((formattedcharsequence) -> new MultiLineLabel.TextWithWidth(formattedcharsequence, font.width(formattedcharsequence))).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel create(Font font, Component... acomponent) {
      return createFixed(font, Arrays.stream(acomponent).map(Component::getVisualOrderText).map((formattedcharsequence) -> new MultiLineLabel.TextWithWidth(formattedcharsequence, font.width(formattedcharsequence))).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel create(Font font, List<Component> list) {
      return createFixed(font, list.stream().map(Component::getVisualOrderText).map((formattedcharsequence) -> new MultiLineLabel.TextWithWidth(formattedcharsequence, font.width(formattedcharsequence))).collect(ImmutableList.toImmutableList()));
   }

   static MultiLineLabel createFixed(final Font font, final List<MultiLineLabel.TextWithWidth> list) {
      return list.isEmpty() ? EMPTY : new MultiLineLabel() {
         private final int width = list.stream().mapToInt((multilinelabel_textwithwidth) -> multilinelabel_textwithwidth.width).max().orElse(0);

         public int renderCentered(GuiGraphics guigraphics, int i, int j) {
            return this.renderCentered(guigraphics, i, j, 9, 16777215);
         }

         public int renderCentered(GuiGraphics guigraphics, int i, int j, int k, int l) {
            int i1 = j;

            for(MultiLineLabel.TextWithWidth multilinelabel_textwithwidth : list) {
               guigraphics.drawString(font, multilinelabel_textwithwidth.text, i - multilinelabel_textwithwidth.width / 2, i1, l);
               i1 += k;
            }

            return i1;
         }

         public int renderLeftAligned(GuiGraphics guigraphics, int i, int j, int k, int l) {
            int i1 = j;

            for(MultiLineLabel.TextWithWidth multilinelabel_textwithwidth : list) {
               guigraphics.drawString(font, multilinelabel_textwithwidth.text, i, i1, l);
               i1 += k;
            }

            return i1;
         }

         public int renderLeftAlignedNoShadow(GuiGraphics guigraphics, int i, int j, int k, int l) {
            int i1 = j;

            for(MultiLineLabel.TextWithWidth multilinelabel_textwithwidth : list) {
               guigraphics.drawString(font, multilinelabel_textwithwidth.text, i, i1, l, false);
               i1 += k;
            }

            return i1;
         }

         public void renderBackgroundCentered(GuiGraphics guigraphics, int i, int j, int k, int l, int i1) {
            int j1 = list.stream().mapToInt((multilinelabel_textwithwidth) -> multilinelabel_textwithwidth.width).max().orElse(0);
            if (j1 > 0) {
               guigraphics.fill(i - j1 / 2 - l, j - l, i + j1 / 2 + l, j + list.size() * k + l, i1);
            }

         }

         public int getLineCount() {
            return list.size();
         }

         public int getWidth() {
            return this.width;
         }
      };
   }

   int renderCentered(GuiGraphics guigraphics, int i, int j);

   int renderCentered(GuiGraphics guigraphics, int i, int j, int k, int l);

   int renderLeftAligned(GuiGraphics guigraphics, int i, int j, int k, int l);

   int renderLeftAlignedNoShadow(GuiGraphics guigraphics, int i, int j, int k, int l);

   void renderBackgroundCentered(GuiGraphics guigraphics, int i, int j, int k, int l, int i1);

   int getLineCount();

   int getWidth();

   public static class TextWithWidth {
      final FormattedCharSequence text;
      final int width;

      TextWithWidth(FormattedCharSequence formattedcharsequence, int i) {
         this.text = formattedcharsequence;
         this.width = i;
      }
   }
}
