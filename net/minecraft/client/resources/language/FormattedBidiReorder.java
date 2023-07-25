package net.minecraft.client.resources.language;

import com.google.common.collect.Lists;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.BidiRun;
import java.util.List;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.SubStringSource;
import net.minecraft.util.FormattedCharSequence;

public class FormattedBidiReorder {
   public static FormattedCharSequence reorder(FormattedText formattedtext, boolean flag) {
      SubStringSource substringsource = SubStringSource.create(formattedtext, UCharacter::getMirror, FormattedBidiReorder::shape);
      Bidi bidi = new Bidi(substringsource.getPlainText(), flag ? 127 : 126);
      bidi.setReorderingMode(0);
      List<FormattedCharSequence> list = Lists.newArrayList();
      int i = bidi.countRuns();

      for(int j = 0; j < i; ++j) {
         BidiRun bidirun = bidi.getVisualRun(j);
         list.addAll(substringsource.substring(bidirun.getStart(), bidirun.getLength(), bidirun.isOddRun()));
      }

      return FormattedCharSequence.composite(list);
   }

   private static String shape(String s) {
      try {
         return (new ArabicShaping(8)).shape(s);
      } catch (Exception var2) {
         return s;
      }
   }
}
