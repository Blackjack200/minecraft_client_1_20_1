package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class SubStringSource {
   private final String plainText;
   private final List<Style> charStyles;
   private final Int2IntFunction reverseCharModifier;

   private SubStringSource(String s, List<Style> list, Int2IntFunction int2intfunction) {
      this.plainText = s;
      this.charStyles = ImmutableList.copyOf(list);
      this.reverseCharModifier = int2intfunction;
   }

   public String getPlainText() {
      return this.plainText;
   }

   public List<FormattedCharSequence> substring(int i, int j, boolean flag) {
      if (j == 0) {
         return ImmutableList.of();
      } else {
         List<FormattedCharSequence> list = Lists.newArrayList();
         Style style = this.charStyles.get(i);
         int k = i;

         for(int l = 1; l < j; ++l) {
            int i1 = i + l;
            Style style1 = this.charStyles.get(i1);
            if (!style1.equals(style)) {
               String s = this.plainText.substring(k, i1);
               list.add(flag ? FormattedCharSequence.backward(s, style, this.reverseCharModifier) : FormattedCharSequence.forward(s, style));
               style = style1;
               k = i1;
            }
         }

         if (k < i + j) {
            String s1 = this.plainText.substring(k, i + j);
            list.add(flag ? FormattedCharSequence.backward(s1, style, this.reverseCharModifier) : FormattedCharSequence.forward(s1, style));
         }

         return flag ? Lists.reverse(list) : list;
      }
   }

   public static SubStringSource create(FormattedText formattedtext) {
      return create(formattedtext, (i) -> i, (s) -> s);
   }

   public static SubStringSource create(FormattedText formattedtext, Int2IntFunction int2intfunction, UnaryOperator<String> unaryoperator) {
      StringBuilder stringbuilder = new StringBuilder();
      List<Style> list = Lists.newArrayList();
      formattedtext.visit((style, s) -> {
         StringDecomposer.iterateFormatted(s, style, (i, style1, j) -> {
            stringbuilder.appendCodePoint(j);
            int k = Character.charCount(j);

            for(int l = 0; l < k; ++l) {
               list.add(style1);
            }

            return true;
         });
         return Optional.empty();
      }, Style.EMPTY);
      return new SubStringSource(unaryoperator.apply(stringbuilder.toString()), list, int2intfunction);
   }
}
