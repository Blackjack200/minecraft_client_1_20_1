package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class ComponentRenderUtils {
   private static final FormattedCharSequence INDENT = FormattedCharSequence.codepoint(32, Style.EMPTY);

   private static String stripColor(String s) {
      return Minecraft.getInstance().options.chatColors().get() ? s : ChatFormatting.stripFormatting(s);
   }

   public static List<FormattedCharSequence> wrapComponents(FormattedText formattedtext, int i, Font font) {
      ComponentCollector componentcollector = new ComponentCollector();
      formattedtext.visit((style, s) -> {
         componentcollector.append(FormattedText.of(stripColor(s), style));
         return Optional.empty();
      }, Style.EMPTY);
      List<FormattedCharSequence> list = Lists.newArrayList();
      font.getSplitter().splitLines(componentcollector.getResultOrEmpty(), i, Style.EMPTY, (formattedtext1, obool) -> {
         FormattedCharSequence formattedcharsequence = Language.getInstance().getVisualOrder(formattedtext1);
         list.add(obool ? FormattedCharSequence.composite(INDENT, formattedcharsequence) : formattedcharsequence);
      });
      return (List<FormattedCharSequence>)(list.isEmpty() ? Lists.newArrayList(FormattedCharSequence.EMPTY) : list);
   }
}
