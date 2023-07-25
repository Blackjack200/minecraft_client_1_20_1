package net.minecraft.client;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

public class StringSplitter {
   final StringSplitter.WidthProvider widthProvider;

   public StringSplitter(StringSplitter.WidthProvider stringsplitter_widthprovider) {
      this.widthProvider = stringsplitter_widthprovider;
   }

   public float stringWidth(@Nullable String s) {
      if (s == null) {
         return 0.0F;
      } else {
         MutableFloat mutablefloat = new MutableFloat();
         StringDecomposer.iterateFormatted(s, Style.EMPTY, (i, style, j) -> {
            mutablefloat.add(this.widthProvider.getWidth(j, style));
            return true;
         });
         return mutablefloat.floatValue();
      }
   }

   public float stringWidth(FormattedText formattedtext) {
      MutableFloat mutablefloat = new MutableFloat();
      StringDecomposer.iterateFormatted(formattedtext, Style.EMPTY, (i, style, j) -> {
         mutablefloat.add(this.widthProvider.getWidth(j, style));
         return true;
      });
      return mutablefloat.floatValue();
   }

   public float stringWidth(FormattedCharSequence formattedcharsequence) {
      MutableFloat mutablefloat = new MutableFloat();
      formattedcharsequence.accept((i, style, j) -> {
         mutablefloat.add(this.widthProvider.getWidth(j, style));
         return true;
      });
      return mutablefloat.floatValue();
   }

   public int plainIndexAtWidth(String s, int i, Style style) {
      StringSplitter.WidthLimitedCharSink stringsplitter_widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)i);
      StringDecomposer.iterate(s, style, stringsplitter_widthlimitedcharsink);
      return stringsplitter_widthlimitedcharsink.getPosition();
   }

   public String plainHeadByWidth(String s, int i, Style style) {
      return s.substring(0, this.plainIndexAtWidth(s, i, style));
   }

   public String plainTailByWidth(String s, int i, Style style) {
      MutableFloat mutablefloat = new MutableFloat();
      MutableInt mutableint = new MutableInt(s.length());
      StringDecomposer.iterateBackwards(s, style, (k, style1, l) -> {
         float f = mutablefloat.addAndGet(this.widthProvider.getWidth(l, style1));
         if (f > (float)i) {
            return false;
         } else {
            mutableint.setValue(k);
            return true;
         }
      });
      return s.substring(mutableint.intValue());
   }

   public int formattedIndexByWidth(String s, int i, Style style) {
      StringSplitter.WidthLimitedCharSink stringsplitter_widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)i);
      StringDecomposer.iterateFormatted(s, style, stringsplitter_widthlimitedcharsink);
      return stringsplitter_widthlimitedcharsink.getPosition();
   }

   @Nullable
   public Style componentStyleAtWidth(FormattedText formattedtext, int i) {
      StringSplitter.WidthLimitedCharSink stringsplitter_widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)i);
      return formattedtext.visit((style, s) -> StringDecomposer.iterateFormatted(s, style, stringsplitter_widthlimitedcharsink) ? Optional.empty() : Optional.of(style), Style.EMPTY).orElse((Style)null);
   }

   @Nullable
   public Style componentStyleAtWidth(FormattedCharSequence formattedcharsequence, int i) {
      StringSplitter.WidthLimitedCharSink stringsplitter_widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)i);
      MutableObject<Style> mutableobject = new MutableObject<>();
      formattedcharsequence.accept((j, style, k) -> {
         if (!stringsplitter_widthlimitedcharsink.accept(j, style, k)) {
            mutableobject.setValue(style);
            return false;
         } else {
            return true;
         }
      });
      return mutableobject.getValue();
   }

   public String formattedHeadByWidth(String s, int i, Style style) {
      return s.substring(0, this.formattedIndexByWidth(s, i, style));
   }

   public FormattedText headByWidth(FormattedText formattedtext, int i, Style style) {
      final StringSplitter.WidthLimitedCharSink stringsplitter_widthlimitedcharsink = new StringSplitter.WidthLimitedCharSink((float)i);
      return formattedtext.visit(new FormattedText.StyledContentConsumer<FormattedText>() {
         private final ComponentCollector collector = new ComponentCollector();

         public Optional<FormattedText> accept(Style style, String s) {
            stringsplitter_widthlimitedcharsink.resetPosition();
            if (!StringDecomposer.iterateFormatted(s, style, stringsplitter_widthlimitedcharsink)) {
               String s1 = s.substring(0, stringsplitter_widthlimitedcharsink.getPosition());
               if (!s1.isEmpty()) {
                  this.collector.append(FormattedText.of(s1, style));
               }

               return Optional.of(this.collector.getResultOrEmpty());
            } else {
               if (!s.isEmpty()) {
                  this.collector.append(FormattedText.of(s, style));
               }

               return Optional.empty();
            }
         }
      }, style).orElse(formattedtext);
   }

   public int findLineBreak(String s, int i, Style style) {
      StringSplitter.LineBreakFinder stringsplitter_linebreakfinder = new StringSplitter.LineBreakFinder((float)i);
      StringDecomposer.iterateFormatted(s, style, stringsplitter_linebreakfinder);
      return stringsplitter_linebreakfinder.getSplitPosition();
   }

   public static int getWordPosition(String s, int i, int j, boolean flag) {
      int k = j;
      boolean flag1 = i < 0;
      int l = Math.abs(i);

      for(int i1 = 0; i1 < l; ++i1) {
         if (flag1) {
            while(flag && k > 0 && (s.charAt(k - 1) == ' ' || s.charAt(k - 1) == '\n')) {
               --k;
            }

            while(k > 0 && s.charAt(k - 1) != ' ' && s.charAt(k - 1) != '\n') {
               --k;
            }
         } else {
            int j1 = s.length();
            int k1 = s.indexOf(32, k);
            int l1 = s.indexOf(10, k);
            if (k1 == -1 && l1 == -1) {
               k = -1;
            } else if (k1 != -1 && l1 != -1) {
               k = Math.min(k1, l1);
            } else if (k1 != -1) {
               k = k1;
            } else {
               k = l1;
            }

            if (k == -1) {
               k = j1;
            } else {
               while(flag && k < j1 && (s.charAt(k) == ' ' || s.charAt(k) == '\n')) {
                  ++k;
               }
            }
         }
      }

      return k;
   }

   public void splitLines(String s, int i, Style style, boolean flag, StringSplitter.LinePosConsumer stringsplitter_lineposconsumer) {
      int j = 0;
      int k = s.length();

      StringSplitter.LineBreakFinder stringsplitter_linebreakfinder;
      for(Style style1 = style; j < k; style1 = stringsplitter_linebreakfinder.getSplitStyle()) {
         stringsplitter_linebreakfinder = new StringSplitter.LineBreakFinder((float)i);
         boolean flag1 = StringDecomposer.iterateFormatted(s, j, style1, style, stringsplitter_linebreakfinder);
         if (flag1) {
            stringsplitter_lineposconsumer.accept(style1, j, k);
            break;
         }

         int l = stringsplitter_linebreakfinder.getSplitPosition();
         char c0 = s.charAt(l);
         int i1 = c0 != '\n' && c0 != ' ' ? l : l + 1;
         stringsplitter_lineposconsumer.accept(style1, j, flag ? i1 : l);
         j = i1;
      }

   }

   public List<FormattedText> splitLines(String s, int i, Style style) {
      List<FormattedText> list = Lists.newArrayList();
      this.splitLines(s, i, style, false, (style1, j, k) -> list.add(FormattedText.of(s.substring(j, k), style1)));
      return list;
   }

   public List<FormattedText> splitLines(FormattedText formattedtext, int i, Style style) {
      List<FormattedText> list = Lists.newArrayList();
      this.splitLines(formattedtext, i, style, (formattedtext1, obool) -> list.add(formattedtext1));
      return list;
   }

   public List<FormattedText> splitLines(FormattedText formattedtext, int i, Style style, FormattedText formattedtext1) {
      List<FormattedText> list = Lists.newArrayList();
      this.splitLines(formattedtext, i, style, (formattedtext3, obool) -> list.add(obool ? FormattedText.composite(formattedtext1, formattedtext3) : formattedtext3));
      return list;
   }

   public void splitLines(FormattedText formattedtext, int i, Style style, BiConsumer<FormattedText, Boolean> biconsumer) {
      List<StringSplitter.LineComponent> list = Lists.newArrayList();
      formattedtext.visit((style2, s) -> {
         if (!s.isEmpty()) {
            list.add(new StringSplitter.LineComponent(s, style2));
         }

         return Optional.empty();
      }, style);
      StringSplitter.FlatComponents stringsplitter_flatcomponents = new StringSplitter.FlatComponents(list);
      boolean flag = true;
      boolean flag1 = false;
      boolean flag2 = false;

      while(flag) {
         flag = false;
         StringSplitter.LineBreakFinder stringsplitter_linebreakfinder = new StringSplitter.LineBreakFinder((float)i);

         for(StringSplitter.LineComponent stringsplitter_linecomponent : stringsplitter_flatcomponents.parts) {
            boolean flag3 = StringDecomposer.iterateFormatted(stringsplitter_linecomponent.contents, 0, stringsplitter_linecomponent.style, style, stringsplitter_linebreakfinder);
            if (!flag3) {
               int j = stringsplitter_linebreakfinder.getSplitPosition();
               Style style1 = stringsplitter_linebreakfinder.getSplitStyle();
               char c0 = stringsplitter_flatcomponents.charAt(j);
               boolean flag4 = c0 == '\n';
               boolean flag5 = flag4 || c0 == ' ';
               flag1 = flag4;
               FormattedText formattedtext1 = stringsplitter_flatcomponents.splitAt(j, flag5 ? 1 : 0, style1);
               biconsumer.accept(formattedtext1, flag2);
               flag2 = !flag4;
               flag = true;
               break;
            }

            stringsplitter_linebreakfinder.addToOffset(stringsplitter_linecomponent.contents.length());
         }
      }

      FormattedText formattedtext2 = stringsplitter_flatcomponents.getRemainder();
      if (formattedtext2 != null) {
         biconsumer.accept(formattedtext2, flag2);
      } else if (flag1) {
         biconsumer.accept(FormattedText.EMPTY, false);
      }

   }

   static class FlatComponents {
      final List<StringSplitter.LineComponent> parts;
      private String flatParts;

      public FlatComponents(List<StringSplitter.LineComponent> list) {
         this.parts = list;
         this.flatParts = list.stream().map((stringsplitter_linecomponent) -> stringsplitter_linecomponent.contents).collect(Collectors.joining());
      }

      public char charAt(int i) {
         return this.flatParts.charAt(i);
      }

      public FormattedText splitAt(int i, int j, Style style) {
         ComponentCollector componentcollector = new ComponentCollector();
         ListIterator<StringSplitter.LineComponent> listiterator = this.parts.listIterator();
         int k = i;
         boolean flag = false;

         while(listiterator.hasNext()) {
            StringSplitter.LineComponent stringsplitter_linecomponent = listiterator.next();
            String s = stringsplitter_linecomponent.contents;
            int l = s.length();
            if (!flag) {
               if (k > l) {
                  componentcollector.append(stringsplitter_linecomponent);
                  listiterator.remove();
                  k -= l;
               } else {
                  String s1 = s.substring(0, k);
                  if (!s1.isEmpty()) {
                     componentcollector.append(FormattedText.of(s1, stringsplitter_linecomponent.style));
                  }

                  k += j;
                  flag = true;
               }
            }

            if (flag) {
               if (k <= l) {
                  String s2 = s.substring(k);
                  if (s2.isEmpty()) {
                     listiterator.remove();
                  } else {
                     listiterator.set(new StringSplitter.LineComponent(s2, style));
                  }
                  break;
               }

               listiterator.remove();
               k -= l;
            }
         }

         this.flatParts = this.flatParts.substring(i + j);
         return componentcollector.getResultOrEmpty();
      }

      @Nullable
      public FormattedText getRemainder() {
         ComponentCollector componentcollector = new ComponentCollector();
         this.parts.forEach(componentcollector::append);
         this.parts.clear();
         return componentcollector.getResult();
      }
   }

   class LineBreakFinder implements FormattedCharSink {
      private final float maxWidth;
      private int lineBreak = -1;
      private Style lineBreakStyle = Style.EMPTY;
      private boolean hadNonZeroWidthChar;
      private float width;
      private int lastSpace = -1;
      private Style lastSpaceStyle = Style.EMPTY;
      private int nextChar;
      private int offset;

      public LineBreakFinder(float f) {
         this.maxWidth = Math.max(f, 1.0F);
      }

      public boolean accept(int i, Style style, int j) {
         int k = i + this.offset;
         switch (j) {
            case 10:
               return this.finishIteration(k, style);
            case 32:
               this.lastSpace = k;
               this.lastSpaceStyle = style;
            default:
               float f = StringSplitter.this.widthProvider.getWidth(j, style);
               this.width += f;
               if (this.hadNonZeroWidthChar && this.width > this.maxWidth) {
                  return this.lastSpace != -1 ? this.finishIteration(this.lastSpace, this.lastSpaceStyle) : this.finishIteration(k, style);
               } else {
                  this.hadNonZeroWidthChar |= f != 0.0F;
                  this.nextChar = k + Character.charCount(j);
                  return true;
               }
         }
      }

      private boolean finishIteration(int i, Style style) {
         this.lineBreak = i;
         this.lineBreakStyle = style;
         return false;
      }

      private boolean lineBreakFound() {
         return this.lineBreak != -1;
      }

      public int getSplitPosition() {
         return this.lineBreakFound() ? this.lineBreak : this.nextChar;
      }

      public Style getSplitStyle() {
         return this.lineBreakStyle;
      }

      public void addToOffset(int i) {
         this.offset += i;
      }
   }

   static class LineComponent implements FormattedText {
      final String contents;
      final Style style;

      public LineComponent(String s, Style style) {
         this.contents = s;
         this.style = style;
      }

      public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
         return formattedtext_contentconsumer.accept(this.contents);
      }

      public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
         return formattedtext_styledcontentconsumer.accept(this.style.applyTo(style), this.contents);
      }
   }

   @FunctionalInterface
   public interface LinePosConsumer {
      void accept(Style style, int i, int j);
   }

   class WidthLimitedCharSink implements FormattedCharSink {
      private float maxWidth;
      private int position;

      public WidthLimitedCharSink(float f) {
         this.maxWidth = f;
      }

      public boolean accept(int i, Style style, int j) {
         this.maxWidth -= StringSplitter.this.widthProvider.getWidth(j, style);
         if (this.maxWidth >= 0.0F) {
            this.position = i + Character.charCount(j);
            return true;
         } else {
            return false;
         }
      }

      public int getPosition() {
         return this.position;
      }

      public void resetPosition() {
         this.position = 0;
      }
   }

   @FunctionalInterface
   public interface WidthProvider {
      float getWidth(int i, Style style);
   }
}
