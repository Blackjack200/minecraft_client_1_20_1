package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import net.minecraft.network.chat.Style;

@FunctionalInterface
public interface FormattedCharSequence {
   FormattedCharSequence EMPTY = (formattedcharsink) -> true;

   boolean accept(FormattedCharSink formattedcharsink);

   static FormattedCharSequence codepoint(int i, Style style) {
      return (formattedcharsink) -> formattedcharsink.accept(0, style, i);
   }

   static FormattedCharSequence forward(String s, Style style) {
      return s.isEmpty() ? EMPTY : (formattedcharsink) -> StringDecomposer.iterate(s, style, formattedcharsink);
   }

   static FormattedCharSequence forward(String s, Style style, Int2IntFunction int2intfunction) {
      return s.isEmpty() ? EMPTY : (formattedcharsink) -> StringDecomposer.iterate(s, style, decorateOutput(formattedcharsink, int2intfunction));
   }

   static FormattedCharSequence backward(String s, Style style) {
      return s.isEmpty() ? EMPTY : (formattedcharsink) -> StringDecomposer.iterateBackwards(s, style, formattedcharsink);
   }

   static FormattedCharSequence backward(String s, Style style, Int2IntFunction int2intfunction) {
      return s.isEmpty() ? EMPTY : (formattedcharsink) -> StringDecomposer.iterateBackwards(s, style, decorateOutput(formattedcharsink, int2intfunction));
   }

   static FormattedCharSink decorateOutput(FormattedCharSink formattedcharsink, Int2IntFunction int2intfunction) {
      return (i, style, j) -> formattedcharsink.accept(i, style, int2intfunction.apply(Integer.valueOf(j)));
   }

   static FormattedCharSequence composite() {
      return EMPTY;
   }

   static FormattedCharSequence composite(FormattedCharSequence formattedcharsequence) {
      return formattedcharsequence;
   }

   static FormattedCharSequence composite(FormattedCharSequence formattedcharsequence, FormattedCharSequence formattedcharsequence1) {
      return fromPair(formattedcharsequence, formattedcharsequence1);
   }

   static FormattedCharSequence composite(FormattedCharSequence... aformattedcharsequence) {
      return fromList(ImmutableList.copyOf(aformattedcharsequence));
   }

   static FormattedCharSequence composite(List<FormattedCharSequence> list) {
      int i = list.size();
      switch (i) {
         case 0:
            return EMPTY;
         case 1:
            return list.get(0);
         case 2:
            return fromPair(list.get(0), list.get(1));
         default:
            return fromList(ImmutableList.copyOf(list));
      }
   }

   static FormattedCharSequence fromPair(FormattedCharSequence formattedcharsequence, FormattedCharSequence formattedcharsequence1) {
      return (formattedcharsink) -> formattedcharsequence.accept(formattedcharsink) && formattedcharsequence1.accept(formattedcharsink);
   }

   static FormattedCharSequence fromList(List<FormattedCharSequence> list) {
      return (formattedcharsink) -> {
         for(FormattedCharSequence formattedcharsequence : list) {
            if (!formattedcharsequence.accept(formattedcharsink)) {
               return false;
            }
         }

         return true;
      };
   }
}
