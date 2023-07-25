package net.minecraft.network.chat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Unit;

public interface FormattedText {
   Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
   FormattedText EMPTY = new FormattedText() {
      public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
         return Optional.empty();
      }

      public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
         return Optional.empty();
      }
   };

   <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer);

   <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style);

   static FormattedText of(final String s) {
      return new FormattedText() {
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
            return formattedtext_contentconsumer.accept(s);
         }

         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
            return formattedtext_styledcontentconsumer.accept(style, s);
         }
      };
   }

   static FormattedText of(final String s, final Style style) {
      return new FormattedText() {
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
            return formattedtext_contentconsumer.accept(s);
         }

         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style stylex) {
            return formattedtext_styledcontentconsumer.accept(style.applyTo(style), s);
         }
      };
   }

   static FormattedText composite(FormattedText... aformattedtext) {
      return composite(ImmutableList.copyOf(aformattedtext));
   }

   static FormattedText composite(final List<? extends FormattedText> list) {
      return new FormattedText() {
         public <T> Optional<T> visit(FormattedText.ContentConsumer<T> formattedtext_contentconsumer) {
            for(FormattedText formattedtext : list) {
               Optional<T> optional = formattedtext.visit(formattedtext_contentconsumer);
               if (optional.isPresent()) {
                  return optional;
               }
            }

            return Optional.empty();
         }

         public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> formattedtext_styledcontentconsumer, Style style) {
            for(FormattedText formattedtext : list) {
               Optional<T> optional = formattedtext.visit(formattedtext_styledcontentconsumer, style);
               if (optional.isPresent()) {
                  return optional;
               }
            }

            return Optional.empty();
         }
      };
   }

   default String getString() {
      StringBuilder stringbuilder = new StringBuilder();
      this.visit((s) -> {
         stringbuilder.append(s);
         return Optional.empty();
      });
      return stringbuilder.toString();
   }

   public interface ContentConsumer<T> {
      Optional<T> accept(String s);
   }

   public interface StyledContentConsumer<T> {
      Optional<T> accept(Style style, String s);
   }
}
