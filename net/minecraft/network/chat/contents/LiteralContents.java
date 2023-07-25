package net.minecraft.network.chat.contents;

import java.util.Optional;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public record LiteralContents(String text) implements ComponentContents {
   public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
      return formattedtext_contentconsumer.accept(this.text);
   }

   public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
      return formattedtext_styledcontentconsumer.accept(style, this.text);
   }

   public String toString() {
      return "literal{" + this.text + "}";
   }
}
